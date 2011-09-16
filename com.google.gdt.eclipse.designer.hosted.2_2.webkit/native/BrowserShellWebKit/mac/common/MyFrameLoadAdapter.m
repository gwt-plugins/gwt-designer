#include "MyFrameLoadAdapter.h"

jstring createJavaString(JNIEnv* env, NSString* nsString) {
	unichar *buffer = (unichar*)malloc([nsString length] * sizeof(unichar));
	[nsString getCharacters:buffer];
	jstring result = (*env)->NewString(env, (const jchar*)buffer, [nsString length]);
	free(buffer);
	return result;
}

static WebFrame* getGlobalContextForWindowObject0(WebFrame* frame, WebScriptObject* windowObject) {
	if (frame == nil) {
		return nil;
	}
	WebScriptObject* frameWindowObject = [frame windowObject];
	if (frameWindowObject == nil) {
		return nil;
	}
	if (frameWindowObject == windowObject) {
		return frame;
	}
	NSArray* childFrames = [frame childFrames];
	NSUInteger count = [childFrames count];
	NSUInteger i = 0;
	for (; i < count; ++i) {
		WebFrame* child = [childFrames objectAtIndex:i];
		WebFrame* foundFrame = getGlobalContextForWindowObject0(child, windowObject);
		if (foundFrame != nil) {
			return foundFrame;
		}
	}
	return nil;
}

// Finds a JSGlobalContextRef by searching the WebFrame tree for
// a particular WebScriptObject (objective-c wrapper around the
// global object).
JSContextRef getGlobalContextForWindowObject(WebView* webView, WebScriptObject* windowObject) {
	if (webView == nil) {
		return nil;
	}
	WebFrame* mainFrame = [webView mainFrame];
	if (mainFrame == nil) {
		return nil;
	}
	WebFrame* frame = getGlobalContextForWindowObject0(mainFrame, windowObject);
	if (frame == nil) {
		return nil;
	}
	return [frame globalContext];
}


@implementation MyFrameLoadAdapter

- initWithCallback: (jobject)callbackObject andJNI:(JNIEnv*)env
{
    [super init];
	m_env = env;
	m_callback = (*m_env)->NewGlobalRef(m_env, callbackObject);
    return self;
}
- (void)dealloc 
{
	(*m_env)->DeleteGlobalRef(m_env, m_callback);
	[super dealloc];
}	
- (void)webView:(WebView *)sender windowScriptObjectAvailable:(WebScriptObject *)windowScriptObject 
{
	JSContextRef context = getGlobalContextForWindowObject(sender, windowScriptObject);
	jobject jwindowScriptOpbject = wrap_pointer(m_env, context);
	(*m_env)->CallVoidMethod(m_env, m_callback, m_BrowserShell_windowScriptObjectAvailable, jwindowScriptOpbject);
}

- (void)webView: (WebView *)wv didFinishLoadForFrame:(WebFrame *)frame
{
	(*m_env)->CallVoidMethod(m_env, m_callback, m_BrowserShell_doneLoading, 0, NULL);
}

- (void)webView: (WebView *)wv didFailLoadWithError:(NSError *)error forFrame:(WebFrame *)frame
{
	NSString *desc = [error localizedDescription];
	(*m_env)->CallVoidMethod(m_env, m_callback, m_BrowserShell_doneLoading, [error code], (desc == NULL ? NULL : createJavaString(m_env, desc)));
}

/*- (void)webView: (WebView *)wv didStartProvisionalLoadForFrame:(WebFrame *)frame
{
}
- (void)webView: (WebView *)wv didReceiveServerRedirectForProvisionalLoadForFrame:(WebFrame *)frame
{
}
- (void)webView: (WebView *)wv didCommitLoadForFrame:(WebFrame *)frame
{
}
- (void)webView: (WebView *)wv didReceiveTitle:(NSString *)title forFrame:(WebFrame *)frame
{
}
- (void)webView: (WebView *)wv didReceiveIcon:(NSImage *)image forFrame:(WebFrame *)frame
{
}
- (void)webView: (WebView *)wv didFailProvisionalLoadWithError:(NSError *)error forFrame:(WebFrame *)frame
{
}
- (void)webView: (WebView *)wv didChangeLocationWithinPageForFrame:(WebFrame *)frame
{
}
- (void)webView: (WebView *)wv willPerformClientRedirectToURL:(NSURL *)URL delay:(NSTimeInterval)seconds fireDate:(NSDate *)date forFrame:(WebFrame *)frame
{
}
- (void)webView: (WebView *)webView didCancelClientRedirectForFrame:(WebFrame *)frame;
{
}
*/

// WebUIDelegate
- (void)webView:(WebView *)sender runJavaScriptAlertPanelWithMessage:(NSString *)message initiatedByFrame:(WebFrame *)frame
{
	(*m_env)->CallVoidMethod(m_env, m_callback, m_BrowserShell_scriptAlert, (message == NULL ? NULL : createJavaString(m_env, message)));
}
@end

