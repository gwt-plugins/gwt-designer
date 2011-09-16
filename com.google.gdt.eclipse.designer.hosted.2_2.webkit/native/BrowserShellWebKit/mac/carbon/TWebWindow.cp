#include <WebKit/HIWebView.h>
#include "TWebWindow.h"
#include "gwt-jni-carbon.h"

#define TRACE_RESOURCE_LOAD		0


static const EventTypeSpec kWindowEvents[] = {
	{ kEventClassCommand, kEventCommandProcess },
	{ kEventClassWindow, kEventWindowDrawContent },
	{ kEventClassWindow, kEventWindowClose },
};


const ControlID		kWebViewID = { 'WEBW', 2 };

#define kControlStaticTextIsMultilineTag    'stim'


TWebWindow::TWebWindow(jobject callback, JNIEnv* env)
	: TWindow(kDocumentWindowClass, kWindowCompositingAttribute| kWindowStandardDocumentAttributes | kWindowStandardHandlerAttribute, Rect() )
{
	ControlRef			contentView, root;
	HIRect				contentRect, viewRect;
	HIRect				frame;
	fIsComposited = TRUE;
	fFrameLoadelegate = NULL;
	fController = NULL;
	m_webScriptObject = NULL;
	
	HIWebViewCreate( &fWebView );
	
	HIViewFindByID( HIViewGetRoot( GetWindowRef() ), kHIViewWindowContentID, &contentView );

	HIViewGetBounds( contentView, &frame );
	HIViewSetFrame( fWebView, &frame );
	
	// If you are using a non-composited window, you embed in the traditional
	// root control, gotten via GetRootControl. Else you would just embed in the
	// content view we already fetched above. Currently, this example is not using
	// a composited window.
	
	GetRootControl( GetWindowRef(), &root );
	HIViewAddSubview( root, fWebView );
	ApplyBindToControlLayout (fWebView, root); 
	HIViewSetVisible( fWebView, true );
	
	fController = HIWebViewGetWebView( fWebView );
	[fController retain];
	{
		if ([fController respondsToSelector:@selector(setShouldCloseWithWindow:)]) {
			[fController setShouldCloseWithWindow: YES];
		}
	}
	fFrameLoadelegate = [[MyFrameLoadAdapter alloc] initWithCallback: callback andJNI:env];
	[fController setFrameLoadDelegate:fFrameLoadelegate];

	// Get the real content view for this purpose if we're not in composited mode, since
	// the traditional root control spans all of QuickDraw space, which isn't very useful.

	if ( !fIsComposited )
		HIViewFindByID( HIViewGetRoot( GetWindowRef() ), kHIViewWindowContentID, &contentView );

	HIViewGetBounds( contentView, &contentRect );
	HIViewGetFrame( fWebView, &viewRect );

	RegisterForEvents( GetEventTypeCount( kWindowEvents ), kWindowEvents );
	
	// Need a Panther check here
	//ChangeWindowAttributes( GetWindowRef(), kWindowAsyncDragAttribute , 0 );
	
	TWebWindow* temp = this;
	SetWindowProperty( GetWindowRef(), 'WEBW', 1, sizeof( TWebWindow * ), (const void *)&temp );
}

TWebWindow::~TWebWindow()
{
	if ( fFrameLoadelegate ) {
		[fFrameLoadelegate release];
		UnregisterForEvents( GetEventTypeCount( kWindowEvents ), kWindowEvents );
	}
	if (fController) {
		[fController release];
	}
}

OSStatus TWebWindow::ApplyBindToControlLayout(HIViewRef webView, HIViewRef toView) {
	OSStatus err;

	HILayoutInfo layoutInfo;
	layoutInfo.version = kHILayoutInfoVersionZero;
	err = HIViewGetLayoutInfo(webView, &layoutInfo);
	
	layoutInfo.binding.top.toView = toView;  // NULL means parent
	layoutInfo.binding.top.kind = kHILayoutBindTop;
	layoutInfo.binding.left.toView = toView;
	layoutInfo.binding.left.kind = kHILayoutBindLeft;
	layoutInfo.binding.bottom.toView = toView;
	layoutInfo.binding.bottom.kind = kHILayoutBindBottom;
	layoutInfo.binding.right.toView = toView;
	layoutInfo.binding.right.kind = kHILayoutBindRight;
	
	err = HIViewSetLayoutInfo( webView, &layoutInfo );
	
	return err;
}
//-------------------------------------------------------------------------------------
//	TWebWindow::GetFromWindowRef
//-------------------------------------------------------------------------------------
//	Given a window ref, get the corresponding TWebWindow object, if any.
//
TWebWindow*
TWebWindow::GetFromWindowRef( WindowRef inWindow )
{
	TWebWindow*	object = NULL;
	
	GetWindowProperty( inWindow, 'WEBW', 1, sizeof( TWebWindow * ), NULL, &object );
	
	return object;
}



//-------------------------------------------------------------------------------------
//	TWebWindow::LoadURL
//-------------------------------------------------------------------------------------
//	Load the specified URL into the main frame.
//
void
TWebWindow::LoadURL( CFURLRef inURL )
{
	
    NSURLRequest*	request = [NSURLRequest requestWithURL:(NSURL*)inURL];

	LoadRequest( request );
}

//-------------------------------------------------------------------------------------
//	TWebWindow::LoadRequest
//-------------------------------------------------------------------------------------
//	Load the specified request into the main frame.
//
void
TWebWindow::LoadRequest( NSURLRequest* inRequest )
{
	m_webScriptObject = NULL;
    WebFrame* 		mainFrame;
	mainFrame = [fController mainFrame];
	[mainFrame loadRequest:inRequest];
}


//-------------------------------------------------------------------------------------
//	TWebWindow::HandleCommand
//-------------------------------------------------------------------------------------
//	Handle a command.
//
Boolean TWebWindow::HandleCommand(const HICommand& command) {
	Boolean		handled = false;
	
	switch( command.commandID )
	{
		default:
			handled = TWindow::HandleCommand( command );
			break;
	}
	
	return handled;
}

//-------------------------------------------------------------------------------------
//	TWebWindow::HandleEvent
//-------------------------------------------------------------------------------------
//	General handler. Branches out to other sub-handlers for clarity.
//
OSStatus
TWebWindow::HandleEvent( EventHandlerCallRef inCallRef, TCarbonEvent& inEvent )
{
	OSStatus result = eventNotHandledErr;

	// This is a workaround that's only needed for Panther and earlier.  It makes inline input in text fields in HIWebView work correctly.
    [NSApp setWindowsNeedUpdate:YES];
    
	switch ( inEvent.GetClass() )
	{
		case kEventClassWindow:
			switch( inEvent.GetKind() )
			{
				case kEventWindowClose:
					Hide();
					return noErr;
			}
			break;
			
		default:
			result = TWindow::HandleEvent( inCallRef, inEvent );
			break;
	}

	return result;
}


HIViewRef TWebWindow::GetWebViewRef() {
	return fWebView;
}




