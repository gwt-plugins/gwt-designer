#include "TWindow.h"

const EventTypeSpec	kEvents[] =
{ 
	{ kEventClassWindow, kEventWindowClose },
	{ kEventClassWindow, kEventWindowActivated },
	{ kEventClassWindow, kEventWindowDeactivated },
	{ kEventClassWindow, kEventWindowDrawContent },
	{ kEventClassWindow, kEventWindowBoundsChanged },
	{ kEventClassWindow, kEventWindowGetIdealSize },
	{ kEventClassCommand, kEventCommandProcess },
	{ kEventClassCommand, kEventCommandUpdateStatus }
};

// Our base class for creating a Mac OS window which is Carbon Event-savvy.

TWindow::TWindow()
{
	fWindow = NULL;
	fPort = NULL;
	fHandler = NULL;
}

TWindow::TWindow( WindowClass inClass, WindowAttributes inAttributes, const Rect& inBounds )
{
	OSStatus		err;
	WindowRef		window;
	
	err = CreateNewWindow( 	inClass,
							inAttributes,
							&inBounds,
							&window );

	InitWithPlatformWindow( window );							
}

TWindow::TWindow( CFStringRef nibName, CFStringRef name )
{
	OSStatus		err;
	WindowRef		window;
	IBNibRef		ref;
	
	err = CreateNibReference( nibName, &ref );
	if ( err == noErr )
	{
		err = CreateWindowFromNib( ref, name, &window );
		DisposeNibReference( ref );
	}
	
	InitWithPlatformWindow( window );							
}


TWindow::TWindow( WindowRef window )
{
	InitWithPlatformWindow( window );
}

void
TWindow::InitWithPlatformWindow( WindowRef window )
{
	WindowClass		theClass;
	
	fWindow = window;
	fPort = GetWindowPort( fWindow );

	SetWindowKind( fWindow, 2001 );
	SetWRefCon( fWindow, (long)this );
	
	ChangeWindowAttributes( fWindow, kWindowStandardHandlerAttribute, 0 );
	InstallWindowEventHandler( fWindow, GetEventHandlerProc(), GetEventTypeCount( kEvents ),
						kEvents, this, &fHandler );

	GetWindowClass( GetWindowRef(), &theClass );
}

TWindow::~TWindow()
{	
	if ( fWindow )
		DisposeWindow( fWindow );
}

void
TWindow::PlatformWindowDisposed()
{
	fWindow = NULL;
	fPort = NULL;
}

#pragma mark -

void
TWindow::Close()
{
	Hide();
	delete this;
}

CGrafPtr
TWindow::GetPort() const
{
	return fPort;
}

WindowRef
TWindow::GetWindowRef() const
{
	return fWindow;
}

void
TWindow::SetTitle( CFStringRef inTitle )
{
	SetWindowTitleWithCFString( fWindow, inTitle );
}

CFStringRef
TWindow::CopyTitle() const
{
	CFStringRef	outTitle;
	
	CopyWindowTitleAsCFString( fWindow, &outTitle );
	
	return outTitle;
}

void
TWindow::SetAlternateTitle( CFStringRef inTitle )
{
	SetWindowAlternateTitle( fWindow, inTitle );
}

CFStringRef
TWindow::CopyAlternateTitle() const
{
	CFStringRef	outTitle;
	
	CopyWindowAlternateTitle( fWindow, &outTitle );
	
	return outTitle;
}

void
TWindow::Show()
{
	ShowWindow( fWindow );
	
	if ( GetWindowRef() == ActiveNonFloatingWindow() )
	{
		WindowActivationScope		scope;
	
		GetWindowActivationScope( GetWindowRef(), &scope );
		if ( scope == kWindowActivationScopeAll )
			AdvanceKeyboardFocus( GetWindowRef() );
	}
}

void
TWindow::Hide()
{
	HideWindow( fWindow );
}

bool
TWindow::IsVisible() const
{
	return IsWindowVisible( fWindow );
}

void
TWindow::Select()
{
	SelectWindow( fWindow );
}

void
TWindow::Draw()
{
}

void
TWindow::Activated()
{
}

void
TWindow::Deactivated()
{
}

void
TWindow::Moved()
{
}

void
TWindow::Resized()
{
}

Point
TWindow::GetIdealSize()
{
	Point size = { 0, 0 };
	
	return size;
}

#pragma mark -

void
TWindow::MoveTopLeftOfContentTo( SInt16 x, SInt16 y )
{
	MoveWindow( fWindow, x, y, false );
}

void
TWindow::SetContentSize( SInt16 x, SInt16 y )
{
	SizeWindow( fWindow, x, y, true );
}

void
TWindow::SetBounds( const Rect& bounds )
{
	SetWindowBounds( fWindow, kWindowStructureRgn, &bounds );
}

#pragma mark -

void
TWindow::InvalidateArea( RgnHandle region )
{
	InvalWindowRgn( fWindow, region );
}

void
TWindow::InvalidateArea( const Rect& rect )
{
	InvalWindowRect( fWindow, &rect );
}

void
TWindow::ValidateArea( RgnHandle region )
{
	ValidWindowRgn( fWindow, region );
}

void
TWindow::ValidateArea( const Rect& rect )
{
	ValidWindowRect( fWindow, &rect );
}

#pragma mark -

Boolean
TWindow::UpdateCommandStatus( const HICommand& inCommand )
{
	if ( inCommand.commandID == kHICommandClose )
	{
		EnableMenuCommand( NULL, inCommand.commandID );
		return true;
	}
	
	return false; // not handled
}

Boolean
TWindow::HandleCommand( const HICommand& inCommand )
{
//#if 0
	if ( inCommand.commandID == kHICommandClose )
	{
		EventRef		event;
		
		if ( CreateEvent( NULL, kEventClassWindow, kEventWindowClose,
				GetCurrentEventTime(), 0, &event ) == noErr )
		{
			WindowRef	window = GetWindowRef();
			SetEventParameter( event, kEventParamDirectObject, typeWindowRef,
					sizeof( WindowRef ), &window );
			SendEventToEventTarget( event, GetWindowEventTarget( window ) );
			ReleaseEvent( event );
		}
		return true;
	}
//#endif	
	return false; // not handled
}

//------------------------------------------------------------------------------------
//	¥ UpdateNow
//------------------------------------------------------------------------------------
//	Send ourselves an update event. This will cause our Draw method to be called.
//
void
TWindow::UpdateNow()
{
	if ( IsWindowUpdatePending( GetWindowRef() ) )
	{
		OSStatus	err;
		EventRef	event;
		
		err = CreateEvent( NULL, kEventClassWindow, kEventWindowUpdate,
				GetCurrentEventTime(), 0, &event );
		if ( err == noErr )
		{
			WindowRef	window = GetWindowRef();
			
			SetEventParameter( event, kEventParamDirectObject, typeWindowRef,
				sizeof( WindowRef ), &window );
		
			SendEventToEventTarget( event, GetWindowEventTarget( window ) );
			ReleaseEvent( event );
		}
	}
}

#pragma mark -

void
TWindow::SetDefaultButton( ControlRef control )
{
	SetWindowDefaultButton( fWindow, control );
}

void
TWindow::SetCancelButton( ControlRef control )
{
	SetWindowCancelButton( fWindow, control );
}

#pragma mark -

OSStatus
TWindow::HandleEvent( EventHandlerCallRef inRef, TCarbonEvent& inEvent )
{
	OSStatus	result = eventNotHandledErr;
	UInt32		attributes;
	HICommand	command;
	
	switch ( inEvent.GetClass() )
	{
		case kEventClassCommand:
			{
				inEvent.GetParameter( kEventParamDirectObject, &command );
			
				switch ( inEvent.GetKind() )
				{
					case kEventCommandProcess:
						if ( this->HandleCommand( command ) )
							result = noErr;
						break;
					
					case kEventCommandUpdateStatus:
						if ( this->UpdateCommandStatus( command ) )
							result = noErr;
						break;
				}
			}
			break;
			
		case kEventClassWindow:
			switch ( inEvent.GetKind() )
			{
				case kEventWindowClose:
					this->Close();
					result = noErr;
					break;
				
				case kEventWindowDrawContent:
					::CallNextEventHandler( inRef, inEvent.GetEventRef() );
					this->Draw();
					result = noErr;
					break;
				
				case kEventWindowActivated:
					::CallNextEventHandler( inRef, inEvent.GetEventRef() );
					this->Activated();
					result = noErr;
					break;
				
				case kEventWindowDeactivated:
					::CallNextEventHandler( inRef, inEvent.GetEventRef() );
					this->Deactivated();
					result = noErr;
					break;
				
				case kEventWindowBoundsChanged:
					if ( inEvent.GetParameter( kEventParamAttributes, &attributes ) )
					{
						if ( attributes & kWindowBoundsChangeSizeChanged )
						{
							this->Resized();
							result = noErr;
						}
						else if ( attributes & kWindowBoundsChangeOriginChanged )
						{
							this->Moved();
							result = noErr;
						}
					}
					break;
				
				case kEventWindowGetIdealSize:
					{
						Point	size = this->GetIdealSize();
						
						if ( (size.h != 0) && (size.v != 0) )
						{
							inEvent.SetParameter( kEventParamDimensions, size );
							result = noErr;
						}
					}
					break;
			}
	};
	
	return result;
}

void
TWindow::RegisterForEvents( UInt32 numEvents, const EventTypeSpec* list )
{
	AddEventTypesToHandler( fHandler, numEvents, list );
}

void
TWindow::UnregisterForEvents( UInt32 numEvents, const EventTypeSpec* list )
{
	RemoveEventTypesFromHandler( fHandler, numEvents, list );
}

EventHandlerUPP
TWindow::GetEventHandlerProc()
{
	static EventHandlerUPP handlerProc = NULL;
	
	if ( handlerProc == NULL )
		handlerProc = NewEventHandlerUPP( EventHandlerProc );
	
	return handlerProc;
}

pascal OSStatus
TWindow::EventHandlerProc( EventHandlerCallRef handler, EventRef event, void* userData )
{
	TCarbonEvent	tempEvent( event );
	TWindow*		window = (TWindow*)userData;
	
	return window->HandleEvent( handler, tempEvent );
}

void
TWindow::EnableControlByID( ControlID theID )
{
	ControlRef	control;
	OSStatus	err;
	
	err = ::GetControlByID( GetWindowRef(), &theID, &control );
	if ( err == noErr )
		EnableControl( control );
}

void
TWindow::DisableControlByID( ControlID theID )
{
	ControlRef	control;
	OSStatus	err;
	
	err = ::GetControlByID( GetWindowRef(), &theID, &control );
	if ( err == noErr )
		DisableControl( control );
}

void
TWindow::ShowControlByID( ControlID theID )
{
	ControlRef	control;
	OSStatus	err;
	
	err = ::GetControlByID( GetWindowRef(), &theID, &control );
	if ( err == noErr )
		ShowControl( control );
}

void
TWindow::HideControlByID( ControlID theID )
{
	ControlRef	control;
	OSStatus	err;
	
	err = ::GetControlByID( GetWindowRef(), &theID, &control );
	if ( err == noErr )
		HideControl( control );
}

