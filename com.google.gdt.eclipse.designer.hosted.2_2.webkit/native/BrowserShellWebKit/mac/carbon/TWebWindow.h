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

#include <Carbon/Carbon.h>
#include <WebKit/WebKit.h>
#include "TWindow.h"
#include "MyFrameLoadAdapter.h"

class TWebWindow : public TWindow
{
	public:
		TWebWindow(jobject callback, JNIEnv* env);
		virtual ~TWebWindow();

		void				LoadURL( CFURLRef inURL );
		void				LoadRequest( NSURLRequest* inRequest );

		void				FrameLoadStarted( WebDataSource* dataSource );
		void				ReceivedPageTitle( CFStringRef title, WebDataSource* dataSource );
		void				FrameLoadDone( NSError* error, WebDataSource* dataSource );
		static TWebWindow*	GetFromWindowRef( WindowRef inWindow );
		//
		HIViewRef			GetWebViewRef();
	protected:
		
		virtual OSStatus	HandleEvent( EventHandlerCallRef handler, TCarbonEvent& inEvent );

		virtual Boolean		HandleCommand( const HICommand& command );
                
	private:
        OSStatus			ApplyBindToControlLayout(HIViewRef webView, HIViewRef toView);

		WebScriptObject*	m_webScriptObject;
		WebView*			fController;
		HIViewRef			fWebView;
		//
		MyFrameLoadAdapter*		fFrameLoadelegate;
		
		bool				fIsComposited;
};

