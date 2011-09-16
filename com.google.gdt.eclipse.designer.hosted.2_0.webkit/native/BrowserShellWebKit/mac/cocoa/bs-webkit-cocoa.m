#include "gwt-jni-cocoa.h"
#include "NSBrowserShell.h"

JNIEnv* gEnv;
JavaVM* gJavaVM;
jclass c_BrowserShell;

jmethodID m_BrowserShell_windowScriptObjectAvailable;
jmethodID m_BrowserShell_doneLoading;
jmethodID m_BrowserShell_scriptAlert;

JNIEXPORT jboolean JNICALL OS_NATIVE(_1init)
	(JNIEnv *env, jclass clazz, jclass llClass)
{
	gEnv = env;
	(*env)->GetJavaVM(env, &gJavaVM);
	//
	c_BrowserShell =  (*env)->NewGlobalRef(env, llClass);
	if (!gJavaVM || !c_BrowserShell || (*env)->ExceptionCheck(env)) {
		return JNI_FALSE;
	}
	
	m_BrowserShell_windowScriptObjectAvailable = (*env)->GetMethodID(env, c_BrowserShell, "windowScriptObjectAvailable", "(Ljava/lang/Number;)V");
	m_BrowserShell_doneLoading = (*env)->GetMethodID(env, c_BrowserShell, "doneLoading", "(ILjava/lang/String;)V");
	m_BrowserShell_scriptAlert = (*env)->GetMethodID(env, c_BrowserShell, "scriptAlert", "(Ljava/lang/String;)V");

	if (!m_BrowserShell_windowScriptObjectAvailable ||
		!m_BrowserShell_doneLoading ||
		!m_BrowserShell_scriptAlert ||
		(*env)->ExceptionCheck(env)) {
		return JNI_FALSE;
	}
	
	return JNI_TRUE;
}

JNIEXPORT jobject JNICALL OS_NATIVE(_1create)
	(JNIEnv *env, jclass jclazz, jobject jcallback)
{
	NSWindow* shell = [[NSBrowserShell alloc] initWithCallback:jcallback andJNI:env];
	return wrap_pointer(env, shell);
}

JNIEXPORT void JNICALL OS_NATIVE(_1release)
	(JNIEnv *env, jclass that, jobject jwnd)
{
	NSWindow* shell = unwrap_pointer(env, jwnd);
	[shell release];
}

JNIEXPORT void JNICALL OS_NATIVE(_1setBounds)
	(JNIEnv *env, jclass that, jobject jwnd, jint x, jint y, jint width, jint height)
{
	NSRect bounds = NSMakeRect(x, y, width, height);
	NSBrowserShell* shell = unwrap_pointer(env, jwnd);
	[shell setFrame:bounds display:YES];
}

JNIEXPORT void JNICALL OS_NATIVE(_1getBounds)
	(JNIEnv *env, jclass that, jobject jwnd, jshortArray jbounds)
{
	NSBrowserShell* shell = unwrap_pointer(env, jwnd);
	NSRect frame = [shell frame];
	jshort values[4];
	values[0] = frame.origin.x;
	values[1] = frame.origin.y;
	values[2] = frame.size.width;
	values[3] = frame.size.height;
	(*env)->SetShortArrayRegion(env, jbounds, 0, 4, values);
}

JNIEXPORT void JNICALL OS_NATIVE(_1computeTrim)
	(JNIEnv *env, jclass that, jobject jwnd, jshortArray jbounds)
{
	jshort inValues[4];
	(*env)->GetShortArrayRegion(env, jbounds, 0, 4, inValues);
	
	NSRect frame = NSMakeRect(inValues[0], inValues[1], inValues[2], inValues[3]);

	NSBrowserShell* shell = unwrap_pointer(env, jwnd);
	frame = [shell frameRectForContentRect:frame];
	jshort values[4];
	values[0] = frame.origin.x;
	values[1] = frame.origin.y;
	values[2] = frame.size.width;
	values[3] = frame.size.height;
	(*env)->SetShortArrayRegion(env, jbounds, 0, 4, values);
}

JNIEXPORT void JNICALL OS_NATIVE(_1setVisible)
	(JNIEnv *env, jclass that, jobject jwnd, jboolean jvisible)
{
	NSWindow* window = unwrap_pointer(env, jwnd);
	if (jvisible == JNI_TRUE) {
		[window makeKeyAndOrderFront:window];
	} else {
		[window orderOut:window];
	}
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1isVisible)
	(JNIEnv* env, jclass that, jobject jwnd)
{
	NSWindow* window = unwrap_pointer(env, jwnd);
	return [window isVisible] == YES ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL OS_NATIVE(_1setUrl)
	(JNIEnv* env, jclass that, jobject jwnd, jstring jlocation)
{
	const char* location = (*env)->GetStringUTFChars(env, jlocation, NULL);
	NSString* url = [NSString stringWithUTF8String:location];
	NSBrowserShell* shell = unwrap_pointer(env, jwnd);
	[shell setUrl:url];
	(*env)->ReleaseStringUTFChars(env, jlocation, location);
}

JNIEXPORT jobject JNICALL OS_NATIVE(_1showAsPreview)
	(JNIEnv *envir, jobject that, jobject jwnd)
{
	return NULL;
}

// Screenshot
static NSImage *createImage(NSBitmapImageRep *bitmapRep) {
	NSImage *image = [[NSImage alloc] init];
	[image addRepresentation:bitmapRep];
	[bitmapRep release];
	
	NSSize imageSize = [image size];
	
	NSBitmapImageRep *compatibleRep = [NSBitmapImageRep alloc];
	[compatibleRep 
	 initWithBitmapDataPlanes:NULL 
	 pixelsWide:imageSize.width 
	 pixelsHigh:imageSize.height 
	 bitsPerSample:8 
	 samplesPerPixel:3 
	 hasAlpha:NO 
	 isPlanar:NO 
	 colorSpaceName:NSDeviceRGBColorSpace 
	 bitmapFormat:NSAlphaFirstBitmapFormat | NSAlphaNonpremultipliedBitmapFormat 
	 bytesPerRow:imageSize.width * 4 
	 bitsPerPixel:32];
	[NSGraphicsContext saveGraphicsState];
	[NSGraphicsContext setCurrentContext:[NSGraphicsContext graphicsContextWithBitmapImageRep:compatibleRep]];
	NSRect rect = NSMakeRect(0, 0, imageSize.width, imageSize.height);
	[image drawAtPoint:rect.origin fromRect:rect operation:NSCompositeSourceOver fraction:1.0];
	[image release];
	[NSGraphicsContext restoreGraphicsState];
	
	NSImage* imageHandle = [[NSImage alloc] initWithSize:imageSize];
	[imageHandle addRepresentation:compatibleRep];
	[compatibleRep release];
	
	return imageHandle;
}

static NSImage* captureViewImage(NSView *view) {
	NSRect bounds = [view bounds];
	NSSize imageSize = bounds.size;
	
	[view lockFocus];
	[view display];
	NSBitmapImageRep* bitmapRep = [[NSBitmapImageRep alloc] initWithFocusedViewRect:
								   NSMakeRect(0, 0, imageSize.width, imageSize.height)];
	[view unlockFocus];
	return createImage(bitmapRep);
}


JNIEXPORT jobject JNICALL OS_NATIVE(_1makeShot)
(JNIEnv *env, jclass jclazz, jobject jwnd)
{
	NSBrowserShell* shell = unwrap_pointer(env, jwnd);
	CGFloat alpha = [shell alphaValue];
	[shell setAlphaValue: 0.0];
	WebView* webView = [shell webView];
	[webView setNeedsDisplay:YES];
	NSImage *image = captureViewImage(webView);
	[shell setAlphaValue:alpha];
	if (image != nil) {
		return wrap_pointer(env, image);
	}
	return NULL;
}
