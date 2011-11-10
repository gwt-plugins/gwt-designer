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
#include "gwt-jni-win32.h"
#include <WebKit/WebKit.h>
#include "WebFrameLoadDelegate.h"
#include "WebUIDelegate.h"

class WebWindow {
	HWND m_wnd;
	HWND m_viewWindow;
	IWebView* m_webView;

	void resizeContents();

public:
	WebWindow();
	virtual ~WebWindow();

	BOOL initWithCallback(jobject callback, JNIEnv* env);
	void loadURL(BSTR urlBStr);
	void show();
	void hide();
	void showAsPreview(HWND mainShellWindow);
	HWND getWindowHandle() const;
	HWND getWebViewWindowHandle() const;

	static void Init();
	static LRESULT CALLBACK WndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam);
};