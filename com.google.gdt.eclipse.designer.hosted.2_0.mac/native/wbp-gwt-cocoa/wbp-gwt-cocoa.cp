/*
 *  BrowserShell.c
 *  CarbonWeb
 *
 *  Created by Alexander Mitin on 21.05.07.
 *  Copyright 2007 Instantiations, Inc. All rights reserved.
 *
 */

#include "wbp-gwt-cocoa.h"
#include "JStringWrapper.h"
#import "NSBrowserShell.h"

JNIEnv* gEnv;
JavaVM* gJavaVM;
jclass c_BrowserShell;

jmethodID m_BrowserShell_windowScriptObjectAvailable;
jmethodID m_BrowserShell_doneLoading;

#define WEB_WINDOW(a) (((NSBrowserShell *)(a))) 

jstring createJavaString(JNIEnv* env, NSString* nsString) {
	unichar *buffer = (unichar*)malloc([nsString length] * sizeof(unichar));
	[nsString getCharacters:buffer];
	jstring result = env->NewString((const jchar*)buffer, [nsString length]);
	free(buffer);
	return result;
}

extern "C" { 
JNIEXPORT jboolean JNICALL OS_NATIVE(_1init)
(JNIEnv *env, jclass, jclass llClass)
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
	
JNIEXPORT jlong JNICALL OS_NATIVE(_1create)
	(JNIEnv *env, jclass, jobject jcallback)
{
	NSWindow* shell = [[NSBrowserShell alloc] initWithCallback:jcallback];
	return (jlong) shell;
}

JNIEXPORT void JNICALL OS_NATIVE(_1release)
	(JNIEnv *env, jclass that, jlong jwnd)
{
	[WEB_WINDOW(jwnd) release];
}

JNIEXPORT void JNICALL OS_NATIVE(_1setBounds)
	(JNIEnv *env, jclass that, jlong jwnd, jint x, jint y, jint width, jint height)
{
	NSRect bounds = NSMakeRect(x, y, width, height);
	[WEB_WINDOW(jwnd) setFrame:bounds display:YES];
}

JNIEXPORT void JNICALL OS_NATIVE(_1getBounds)
	(JNIEnv *env, jclass that, jlong jwnd, jshortArray jbounds)
{
	 NSRect frame = [WEB_WINDOW(jwnd) frame];
	jshort values[4];
	values[0] = frame.origin.x;
	values[1] = frame.origin.y;
	values[2] = frame.size.width;
	values[3] = frame.size.height;
	env->SetShortArrayRegion(jbounds, 0, 4, values);
}

JNIEXPORT void JNICALL OS_NATIVE(_1computeTrim)
	(JNIEnv *env, jclass that, jlong jwnd, jshortArray jbounds)
{
	jshort inValues[4];
	env->GetShortArrayRegion(jbounds, 0, 4, inValues);
	
	NSRect frame = NSMakeRect(inValues[0], inValues[1], inValues[2], inValues[3]);
	
	frame = [WEB_WINDOW(jwnd) frameRectForContentRect:frame];
	jshort values[4];
	values[0] = frame.origin.x;
	values[1] = frame.origin.y;
	values[2] = frame.size.width;
	values[3] = frame.size.height;
	env->SetShortArrayRegion(jbounds, 0, 4, values);
}

JNIEXPORT void JNICALL OS_NATIVE(_1setVisible)
	(JNIEnv *env, jclass that, jlong jwnd, jboolean jvisible)
{
	if (jvisible == JNI_TRUE) {
		[WEB_WINDOW(jwnd) makeKeyAndOrderFront:WEB_WINDOW(jwnd)];
	} else {
		[WEB_WINDOW(jwnd) orderOut:WEB_WINDOW(jwnd)];
	}
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1isVisible)
	(JNIEnv *env, jclass that, jlong jwnd)
{
	NSWindow* window = WEB_WINDOW(jwnd);
	return [window isVisible] == YES ? JNI_TRUE : JNI_FALSE;
}


JNIEXPORT void JNICALL OS_NATIVE(_1setUrl)
	(JNIEnv *env, jclass that, jlong jwnd, jstring jlocation)
{
	const char* location = env->GetStringUTFChars(jlocation, NULL);
	NSString* url = [NSString stringWithUTF8String:location];
	[WEB_WINDOW(jwnd) setUrl:url];
	env->ReleaseStringUTFChars(jlocation, location);
}
	
// TODO remove it
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
	JNIEXPORT jlong JNICALL OS_NATIVE(_1makeWindowShot)
	(JNIEnv * env, jobject this_, jlong windowHandle)
	{	
		WebView* webView = [WEB_WINDOW(windowHandle) webView];
		[webView setNeedsDisplay:YES];
		NSImage *image = captureViewImage(webView);
		return (jlong)image;
	}	
	
} // extern "C"