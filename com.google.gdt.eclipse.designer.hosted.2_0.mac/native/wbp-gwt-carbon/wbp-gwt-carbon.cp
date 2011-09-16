/*
 *  BrowserShell.c
 *  CarbonWeb
 *
 *  Created by Alexander Mitin on 21.05.07.
 *  Copyright 2007 Instantiations, Inc. All rights reserved.
 *
 */
#include "wbp-gwt-carbon.h"
#include "JStringWrapper.h"
#include "TWebWindow.h"

JNIEnv* gEnv;
JavaVM* gJavaVM;
jclass c_BrowserShell;

jmethodID m_BrowserShell_windowScriptObjectAvailable;
jmethodID m_BrowserShell_doneLoading;

// TODO: rip this out when completely switching to D2
static jint captureImage(HIViewRef controlRef, WindowRef windowRef) {
	// The image handle of the resulting image
	CGImageRef imageHandle = (CGImageRef) 0;
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
  	return (jint)imageHandle;
}

#define WEB_WINDOW(a) (((TWebWindow *)(a))) 

jstring createJavaString(JNIEnv* env, NSString* nsString) {
	unichar *buffer = (unichar*)malloc([nsString length] * sizeof(unichar));
	[nsString getCharacters:buffer];
	jstring result = env->NewString((const jchar*)buffer, [nsString length]);
	free(buffer);
	return result;
}

extern "C" { 
JNIEXPORT jint JNICALL OS_NATIVE(_1makeShot)
(JNIEnv *env, jclass that, jint jwnd)
{
	WindowRef windowRef = WEB_WINDOW(jwnd)->GetWindowRef();
	HIViewRef controlRef = WEB_WINDOW(jwnd)->GetWebViewRef();
	return captureImage(controlRef, windowRef);
}
	
	
JNIEXPORT jint JNICALL OS_NATIVE(_1create)
	(JNIEnv *env, jclass, jobject jcallback)
{
	WebInitForCarbon();
	TWebWindow *wnd = new TWebWindow(jcallback);
	return (jint) wnd;
}

JNIEXPORT void JNICALL OS_NATIVE(_1release)
	(JNIEnv *env, jclass that, jint jwnd)
{
	WEB_WINDOW(jwnd)->Close();
}

JNIEXPORT void JNICALL OS_NATIVE(_1setBounds)
	(JNIEnv *env, jclass that, jint jwnd, jint jleft, jint jright, jint jtop, jint jbottom)
{
	Rect bounds;
	bounds.left = jleft;
	bounds.right = jright;
	bounds.top = jtop;
	bounds.bottom = jbottom;
	WEB_WINDOW(jwnd)->SetBounds(bounds);
}

JNIEXPORT void JNICALL OS_NATIVE(_1getBounds)
	(JNIEnv *env, jclass that, jint jwnd, jshortArray jbounds)
{
	Rect rect;
	GetWindowBounds(WEB_WINDOW(jwnd)->GetWindowRef(), kWindowStructureRgn, &rect);
	env->SetShortArrayRegion(jbounds, 0, 4, (const jshort*)&rect);
}

JNIEXPORT void JNICALL OS_NATIVE(_1getWebViewBounds)
	(JNIEnv *env, jclass that, jint jwnd, jfloatArray jbounds)
{
	CGRect rect;
	HIViewGetBounds(WEB_WINDOW(jwnd)->GetWebViewRef(), &rect);
	env->SetFloatArrayRegion(jbounds, 0, 1, (const jfloat*)&rect.origin.x);
	env->SetFloatArrayRegion(jbounds, 1, 1, (const jfloat*)&rect.origin.y);
	env->SetFloatArrayRegion(jbounds, 2, 1, (const jfloat*)&rect.size.width);
	env->SetFloatArrayRegion(jbounds, 3, 1, (const jfloat*)&rect.size.height);
}

JNIEXPORT void JNICALL OS_NATIVE(_1computeTrim)
	(JNIEnv *env, jclass that, jint jwnd, jshortArray jbounds)
{
	Rect rect;
	GetWindowStructureWidths(WEB_WINDOW(jwnd)->GetWindowRef(), &rect);
	env->SetShortArrayRegion(jbounds, 0, 4, (const jshort*)&rect);
}

JNIEXPORT void JNICALL OS_NATIVE(_1setVisible)
	(JNIEnv *env, jclass that, jint jwnd, jboolean jvisible)
{
	if (jvisible == JNI_TRUE) {
		WEB_WINDOW(jwnd)->Show();
	} else {
		WEB_WINDOW(jwnd)->Hide();
	}
}

JNIEXPORT void JNICALL OS_NATIVE(_1selectWindow)
	(JNIEnv *env, jclass that, jint jwnd)
{
	WEB_WINDOW(jwnd)->Select();
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1isVisible)
(JNIEnv *env, jclass that, jint jwnd)
{
	return WEB_WINDOW(jwnd)->IsVisible() ? JNI_TRUE : JNI_FALSE;
}
	
JNIEXPORT void JNICALL OS_NATIVE(_1setUrl)
	(JNIEnv *env, jclass that, jint jwnd, jstring jlocation)
{
	const char* location = env->GetStringUTFChars(jlocation, NULL);
	CFStringRef cfLocation = CFStringCreateWithBytes(NULL, (const UInt8*)location, strlen(location), NULL, false);
	CFURLRef url = CFURLCreateWithString(NULL, cfLocation, NULL);
	WEB_WINDOW(jwnd)->LoadURL(url);
	CFRelease(url);
	CFRelease(cfLocation);
	env->ReleaseStringUTFChars(jlocation, location);
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1init)
	(JNIEnv* env, jclass, jclass llClass)
{
	gEnv = env;
	env->GetJavaVM(&gJavaVM);
	//
	c_BrowserShell =  static_cast<jclass>(env->NewGlobalRef(llClass));
	if (!gJavaVM || !c_BrowserShell || env->ExceptionCheck()) {
		return JNI_FALSE;
	}
	
	m_BrowserShell_windowScriptObjectAvailable = env->GetMethodID(c_BrowserShell, "windowScriptObjectAvailable", "(J)V");
	m_BrowserShell_doneLoading = env->GetMethodID(c_BrowserShell, "doneLoading", "(ILjava/lang/String;)V");
	
	if (!m_BrowserShell_windowScriptObjectAvailable ||
		!m_BrowserShell_doneLoading ||
		env->ExceptionCheck()) {
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

} // extern "C"
