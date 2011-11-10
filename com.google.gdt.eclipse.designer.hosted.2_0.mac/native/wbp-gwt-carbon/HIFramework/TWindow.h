/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

#pragma once

#if !BUILDING_FOR_CARBON_8
	#include <Carbon/Carbon.h>
#else
	#include <Carbon.h>
#endif

#include "TCarbonEvent.h"

#define PURE_VIRTUAL	0

class TWindow
{
	public:
			TWindow( CFStringRef inNib, CFStringRef inName );
			TWindow( WindowClass inClass, WindowAttributes inAttributes, const Rect& bounds );
			TWindow( WindowRef inWindow );
		virtual ~TWindow();
		
		virtual void		Close();
		
		CGrafPtr			GetPort() const;
		WindowRef			GetWindowRef() const;
		
		void				SetTitle( CFStringRef inTitle );
		CFStringRef			CopyTitle() const;
		
		void				SetAlternateTitle( CFStringRef inTitle );
		CFStringRef			CopyAlternateTitle() const;
		
		virtual void		Show();
		virtual void		Hide();
		bool				IsVisible() const;
		
		void				Select();
		
		void				MoveTopLeftOfContentTo( SInt16 x, SInt16 y );
		void				SetContentSize( SInt16 x, SInt16 y );
		void				SetBounds( const Rect& bounds );
		
		void				InvalidateArea( RgnHandle region );
		void				InvalidateArea( const Rect& rect );
		void				ValidateArea( RgnHandle region );
		void				ValidateArea( const Rect& rect );
		
		void				UpdateNow();
		
		void				SetDefaultButton( ControlRef control );
		void				SetCancelButton( ControlRef control );
		
		void				EnableControlByID( ControlID theID );
		void				DisableControlByID( ControlID theID );

		void				ShowControlByID( ControlID theID );
		void				HideControlByID( ControlID theID );

	protected:
		virtual void		Draw();
		
		virtual void		Activated();
		virtual void		Deactivated();
		
		virtual void		Moved();
		virtual void		Resized();

		virtual Point		GetIdealSize();
		
		virtual Boolean		UpdateCommandStatus( const HICommand& command );
		virtual Boolean		HandleCommand( const HICommand& command );
		
		void				RegisterForEvents( UInt32 numEvents, const EventTypeSpec* list );
		void				UnregisterForEvents( UInt32 numEvents, const EventTypeSpec* list );
		
		virtual OSStatus	HandleEvent( EventHandlerCallRef inCallRef, TCarbonEvent& inEvent );

		void				PlatformWindowDisposed();
		
		TWindow();
		
		void						InitWithPlatformWindow( WindowRef window );

	private:
	
		static pascal OSStatus		EventHandlerProc( EventHandlerCallRef handler, EventRef event, void* userData );
		static EventHandlerUPP		GetEventHandlerProc();

		
		WindowRef			fWindow;
		CGrafPtr			fPort;
		EventHandlerRef		fHandler;
};

		
