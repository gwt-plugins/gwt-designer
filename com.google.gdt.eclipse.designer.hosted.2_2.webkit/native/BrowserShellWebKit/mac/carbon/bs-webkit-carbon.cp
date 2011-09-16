#include "gwt-jni-carbon.h"
#include "utils.h"
#include "TWebWindow.h"
#include <Carbon/Carbon.h>
#include <WebKit/WebKit.h>
#include <WebKit/HIWebView.h>

JNIEnv* gEnv;
JavaVM* gJavaVM;
jclass c_BrowserShell;

jmethodID m_BrowserShell_windowScriptObjectAvailable;
jmethodID m_BrowserShell_doneLoading;
jmethodID m_BrowserShell_scriptAlert;

static TWebWindow* getWebWindow(JNIEnv *env, jobject jwnd) {
	return (TWebWindow*)unwrap_pointer(env, jwnd);
}

static CGImageRef captureImage(HIViewRef controlRef, WindowRef windowRef) {
	// The image handle of the resulting image
	CGImageRef imageHandle = NULL;
	if (!IsValidWindowPtr(windowRef)) {
		windowRef = NULL;
	}
	// Check to see if the controlHandle is a valid view ref
	if (HIViewIsValid(controlRef)) {
		// Bounds rectangle of resulting image
		HIRect boundsRect;
		// Pointer to persist the current front process
		ProcessSerialNumber originalFrontProcess;
		// Pointer to persist the current running process (the SWT remote VM)
		ProcessSerialNumber swtVMProcess;
		// Grab the current front process (The current foreground application in OS X)
		GetFrontProcess(&originalFrontProcess);
		// Grab the process id of the SWT remote VM
		GetCurrentProcess(&swtVMProcess);
		// Set the SWT VM process as the front process		
		SetFrontProcess(&swtVMProcess);
		// Show the window and select it
		if (windowRef != NULL) {
			// Select the window			
			SelectWindow(windowRef);
		}
		// Create an image capture of the SWT VM
		HIViewCreateOffscreenImage(controlRef, 0, &boundsRect, &imageHandle);
		// Restore the old front process
		SetFrontProcess(&originalFrontProcess);
	}
	// Return the handle of the created image to Java
  	return imageHandle;
}

extern "C" { 
JNIEXPORT jboolean JNICALL OS_NATIVE(_1init)
	(JNIEnv *env, jclass clazz, jclass llClass)
{
	gEnv = env;
	env->GetJavaVM(&gJavaVM);
	//
	c_BrowserShell = (jclass)env->NewGlobalRef(llClass);
	if (!gJavaVM || !c_BrowserShell || env->ExceptionCheck()) {
		return JNI_FALSE;
	}
	
	m_BrowserShell_windowScriptObjectAvailable = env->GetMethodID(c_BrowserShell, "windowScriptObjectAvailable", "(Ljava/lang/Number;)V");
	m_BrowserShell_doneLoading = env->GetMethodID(c_BrowserShell, "doneLoading", "(ILjava/lang/String;)V");
	m_BrowserShell_scriptAlert = env->GetMethodID(c_BrowserShell, "scriptAlert", "(Ljava/lang/String;)V");

	if (!m_BrowserShell_windowScriptObjectAvailable ||
		!m_BrowserShell_doneLoading ||
		!m_BrowserShell_scriptAlert ||
		env->ExceptionCheck()) {
		return JNI_FALSE;
	}
	
	return JNI_TRUE;
}

JNIEXPORT jobject JNICALL OS_NATIVE(_1create)
	(JNIEnv *env, jclass jclazz, jobject jcallback)
{
	TWebWindow *wnd = new TWebWindow(jcallback, env);
	return wrap_pointer(env, wnd);
}

JNIEXPORT void JNICALL OS_NATIVE(_1release)
	(JNIEnv *env, jclass that, jobject jwnd)
{
	getWebWindow(env, jwnd)->Close();
}

JNIEXPORT void JNICALL OS_NATIVE(_1setBounds)
	(JNIEnv *env, jclass that, jobject jwnd, jint x, jint y, jint width, jint height)
{
	Rect bounds;
	bounds.left = x;
	bounds.right = x + width;
	bounds.top = y;
	bounds.bottom = y + height;
	getWebWindow(env, jwnd)->SetBounds(bounds);
}

JNIEXPORT void JNICALL OS_NATIVE(_1getBounds)
	(JNIEnv *env, jclass that, jobject jwnd, jshortArray jbounds)
{
	Rect rect;
	GetWindowBounds(getWebWindow(env, jwnd)->GetWindowRef(), kWindowStructureRgn, &rect);
	jshort values[4];
	values[0] = rect.left;
	values[1] = rect.right - rect.left;
	values[2] = rect.top;
	values[3] = rect.bottom - rect.top;
	env->SetShortArrayRegion(jbounds, 0, 4, values);
}

JNIEXPORT void JNICALL OS_NATIVE(_1computeTrim)
	(JNIEnv *env, jclass that, jobject jwnd, jshortArray jbounds)
{
	Rect rect;
	GetWindowStructureWidths(getWebWindow(env, jwnd)->GetWindowRef(), &rect);
	jshort values[4];
	values[0] = rect.top;
	values[1] = rect.left;
	values[2] = rect.bottom;
	values[3] = rect.right;
	env->SetShortArrayRegion(jbounds, 0, 4, values);
}

JNIEXPORT void JNICALL OS_NATIVE(_1setVisible)
	(JNIEnv *env, jclass that, jobject jwnd, jboolean jvisible)
{
	TWebWindow* wnd = getWebWindow(env, jwnd);
	if (jvisible == JNI_TRUE) {
		wnd->Show();
	} else {
		wnd->Hide();
	}
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1isVisible)
	(JNIEnv* env, jclass that, jobject jwnd)
{
	return getWebWindow(env, jwnd)->IsVisible() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL OS_NATIVE(_1setUrl)
	(JNIEnv* env, jclass that, jobject jwnd, jstring jlocation)
{
	const char* location = env->GetStringUTFChars(jlocation, NULL);
	CFStringRef cfLocation = CFStringCreateWithBytes(NULL, (const UInt8*)location, strlen(location), NULL, false);
	CFURLRef url = CFURLCreateWithString(NULL, cfLocation, NULL);
	getWebWindow(env, jwnd)->LoadURL(url);
	CFRelease(url);
	CFRelease(cfLocation);
	env->ReleaseStringUTFChars(jlocation, location);
}

JNIEXPORT jobject JNICALL OS_NATIVE(_1makeShot)
(JNIEnv *env, jclass jclazz, jobject jwnd)
{
	TWebWindow* wnd = getWebWindow(env, jwnd);
	WindowRef windowRef = wnd->GetWindowRef();
	HIViewRef controlRef = wnd->GetWebViewRef();
	CGImageRef img = captureImage(controlRef, windowRef);
	return img != NULL ? wrap_pointer(env, img) : NULL;
}
	
JNIEXPORT void JNICALL OS_NATIVE(_1getWebViewBounds)
(JNIEnv *env, jclass that, jobject jwnd, jfloatArray jbounds)
{
	TWebWindow* wnd = getWebWindow(env, jwnd);
	CGRect rect;
	HIViewGetBounds(wnd->GetWebViewRef(), &rect);
	env->SetFloatArrayRegion(jbounds, 0, 1, (const jfloat*)&rect.origin.x);
	env->SetFloatArrayRegion(jbounds, 1, 1, (const jfloat*)&rect.origin.y);
	env->SetFloatArrayRegion(jbounds, 2, 1, (const jfloat*)&rect.size.width);
	env->SetFloatArrayRegion(jbounds, 3, 1, (const jfloat*)&rect.size.height);
}

JNIEXPORT void JNICALL OS_NATIVE(_1selectWindow)
(JNIEnv *env, jclass that, jobject jwnd)
{
	getWebWindow(env, jwnd)->Select();
}
	
} // extern "C"