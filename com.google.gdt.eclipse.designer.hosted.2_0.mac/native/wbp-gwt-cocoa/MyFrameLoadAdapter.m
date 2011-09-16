#include "MyFrameLoadAdapter.h"
#include "wbp-gwt-cocoa.h"

@implementation MyFrameLoadAdapter

- initWithCallback: (jobject)callbackObject
{
    [super init];
	m_callback = gEnv->NewGlobalRef(callbackObject);
    return self;
}
- (void)dealloc 
{
	gEnv->DeleteGlobalRef(m_callback);
	[super dealloc];
}	
- (void)webView:(WebView *)sender windowScriptObjectAvailable:(WebScriptObject *)windowScriptObject 
{
	jlong value = (jlong)windowScriptObject;
	gEnv->CallVoidMethod(m_callback, m_BrowserShell_windowScriptObjectAvailable, value);
}

- (void)webView: (WebView *)wv didFinishLoadForFrame:(WebFrame *)frame
{
	gEnv->CallVoidMethod(m_callback, m_BrowserShell_doneLoading, 0, NULL);
}

- (void)webView: (WebView *)wv didFailLoadWithError:(NSError *)error forFrame:(WebFrame *)frame
{
	NSString *desc = [error localizedDescription];
	gEnv->CallVoidMethod(m_callback, m_BrowserShell_doneLoading, [error code], (desc == NULL ? NULL : createJavaString(gEnv, desc)));
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
@end

