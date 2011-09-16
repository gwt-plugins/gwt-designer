//
//  NSBrowserShell.m
//  wbp-gwt-cocoa
//
//  Created by Alexander Mitin on 5/26/09.
//  Copyright 2009 Instantiations, Inc. All rights reserved.
//

#import "NSBrowserShell.h"
#import "MyFrameLoadAdapter.h"

@implementation NSBrowserShell

- (id)initWithCallback:(jobject)callbackObject andJNI:(JNIEnv*)env
{
	[[NSUserDefaults standardUserDefaults]registerDefaults:[NSDictionary dictionaryWithObjectsAndKeys:
									   @"YES", @"WebKitDeveloperExtras", nil]];
	
	NSUInteger styleMask = NSTitledWindowMask | NSClosableWindowMask |	NSResizableWindowMask;
	[self initWithContentRect:NSMakeRect(0, 0, 0, 0) styleMask:styleMask backing:NSBackingStoreBuffered defer:NO];
	NSRect frame = [self frame];
	
	frame = [self frameRectForContentRect:frame];
	m_webView = [[WebView alloc] initWithFrame:NSMakeRect(0, 0, frame.size.width, frame.size.height)];
	id delegate = [[MyFrameLoadAdapter alloc] initWithCallback:callbackObject andJNI:env];
	[m_webView setFrameLoadDelegate:delegate];
	[m_webView setUIDelegate:delegate];
	[m_webView setAutoresizingMask:NSViewWidthSizable|NSViewHeightSizable];
	[self setContentView:m_webView];
	
	return self;
}

- (void) setUrl:(NSString*)url
{
	[m_webView setMainFrameURL:url];
}
- (WebView*) webView
{
	return m_webView;
}
- (BOOL)windowShouldClose:(id)window
{
	[window orderOut:self];
	return NO;
}

@end
