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
#include "WebWindow.h"
#include <commctrl.h>
#include <objbase.h>
#include <shlwapi.h>
#include <wininet.h>
#include <WebKit/WebKitCOMAPI.h>

#define WBP_WINDOW_CLASS TEXT("__WBP_PREVIEW_WINDOW_CLASS")
#define WBP_WINDOW_TITLE TEXT("Preview Window")

void WebWindow::Init() {
	// 
    INITCOMMONCONTROLSEX InitCtrlEx;
    InitCtrlEx.dwSize = sizeof(INITCOMMONCONTROLSEX);
    InitCtrlEx.dwICC  = 0x00004000; //ICC_STANDARD_CLASSES;
    InitCommonControlsEx(&InitCtrlEx);
	//
    WNDCLASSEX wcex;
    wcex.cbSize = sizeof(WNDCLASSEX);
    wcex.style          = CS_HREDRAW | CS_VREDRAW;
    wcex.lpfnWndProc    = WndProc;
    wcex.cbClsExtra     = 0;
    wcex.cbWndExtra     = sizeof(void*);
    wcex.hInstance      = GetModuleHandle(NULL);
    wcex.hIcon          = 0;
    wcex.hCursor        = 0;
    wcex.hbrBackground  = 0;
    wcex.lpszMenuName   = 0;
    wcex.lpszClassName  = WBP_WINDOW_CLASS;
    wcex.hIconSm        = 0;
    RegisterClassEx(&wcex);
	//
	OleInitialize(NULL);
}

WebWindow::WebWindow() : m_webView(0), m_viewWindow(0) { 
}

BOOL WebWindow::initWithCallback(jobject callback, JNIEnv* env) {
	m_wnd = CreateWindowEx(0, WBP_WINDOW_CLASS, WBP_WINDOW_TITLE, WS_OVERLAPPEDWINDOW,
		CW_USEDEFAULT, 0, CW_USEDEFAULT, 0, NULL, NULL, GetModuleHandle(NULL), NULL);

	SetLastError(0);
    LONG_PTR setWindowLongValue = SetWindowLongPtr(m_wnd, 0, (LONG_PTR)this);
	if (setWindowLongValue == 0 && GetLastError() != 0) {
		return FALSE;
	}
	//
	HRESULT hr = WebKitCreateInstance(CLSID_WebView, 0, IID_IWebView, (void**)&m_webView);
	if (FAILED(hr)) {
		goto cleanup;
	}
	//
	WebFrameLoadDelegate* frameLoadDelegate = new WebFrameLoadDelegate(callback, env);
    hr = m_webView->setFrameLoadDelegate(frameLoadDelegate);
	if (FAILED(hr)) {
        goto cleanup;
	}
	frameLoadDelegate->Release(); 	// don't own this more
	//
	WebUIDelegate* webUIDelegate = new WebUIDelegate(callback, env);
    hr = m_webView->setUIDelegate(webUIDelegate);
	if (FAILED (hr)) {
        goto cleanup;
	}
	webUIDelegate->Release();
	//
	hr = m_webView->setHostWindow((OLE_HANDLE)m_wnd);
	if (FAILED(hr)) {
        goto cleanup;
	}
	//
    RECT clientRect;
    GetClientRect(m_wnd, &clientRect);
    hr = m_webView->initWithFrame(clientRect, 0, 0);
	if (FAILED(hr)) {
        goto cleanup;
	}
	//
    IWebViewPrivate* viewExt = NULL;
    hr = m_webView->QueryInterface(IID_IWebViewPrivate, (void**)&viewExt);
	if (FAILED(hr)) {
        goto cleanup;
	}
	//
    hr = viewExt->viewWindow((OLE_HANDLE*) &m_viewWindow);
    viewExt->Release();
	if (FAILED(hr) || !m_viewWindow) {
		goto cleanup;
	}
	// setup developers extras
	IWebPreferences *prefs = NULL;
	if (SUCCEEDED(m_webView->preferences(&prefs))) {
		IWebPreferencesPrivate* privatePrefs = NULL;
		if (SUCCEEDED(prefs->QueryInterface(IID_IWebPreferencesPrivate, (void**)&privatePrefs))) {
			privatePrefs->setDeveloperExtrasEnabled(TRUE);
			privatePrefs->Release();
		}
		prefs->Release();
	}
	return TRUE;
cleanup:
	if (m_webView) {
		m_webView->Release();
	}
	return FALSE;
}
WebWindow::~WebWindow() {
	m_webView->Release();
	DestroyWindow(m_wnd);
}

HWND WebWindow::getWindowHandle() const {
	return m_wnd;
}

HWND WebWindow::getWebViewWindowHandle() const {
	return m_viewWindow;
}

void WebWindow::loadURL(BSTR urlBStr) {
    IWebFrame* frame = NULL;
    IWebMutableURLRequest* request = NULL;

    static BSTR methodBStr = SysAllocString(TEXT("GET"));

    if (urlBStr && urlBStr[0] && (PathFileExists(urlBStr) || PathIsUNC(urlBStr))) {
        TCHAR fileURL[INTERNET_MAX_URL_LENGTH];
        DWORD fileURLLength = sizeof(fileURL) / sizeof(fileURL[0]);

		if (SUCCEEDED(UrlCreateFromPath(urlBStr, fileURL, &fileURLLength, 0))) {
            SysReAllocString(&urlBStr, fileURL);
		}
    }
    HRESULT hr = m_webView->mainFrame(&frame);
	if (FAILED(hr)) {
        goto exit;
	}
    hr = WebKitCreateInstance(CLSID_WebMutableURLRequest, 0, IID_IWebMutableURLRequest, (void**)&request);
	if (FAILED(hr)) {
        goto exit;
	}
    hr = request->initWithURL(urlBStr, WebURLRequestUseProtocolCachePolicy, 60);
	if (FAILED(hr)) {
        goto exit;
	}
    hr = request->setHTTPMethod(methodBStr);
	if (FAILED(hr)) {
        goto exit;
	}
    hr = frame->loadRequest(request);
	if (FAILED(hr)) {
        goto exit;
	}
exit:
	if (frame) {
        frame->Release();
	}
	if (request) {
        request->Release();
	}
}

void WebWindow::resizeContents() {
    RECT rcClient;
    GetClientRect(m_wnd, &rcClient);
    MoveWindow(m_viewWindow, 0, 0, rcClient.right, rcClient.bottom, TRUE);
}
void WebWindow::show() {
	ShowWindow(m_wnd, SW_SHOWNORMAL);
}
void WebWindow::hide() {
	ShowWindow(m_wnd, SW_HIDE);
}

void WebWindow::showAsPreview(HWND mainShellWindow) {
//	printf("1: %p\n", mainShellWindow);fflush(stdout);
	if (!mainShellWindow) {
		return;
	}
	int x = 0, y = 0;
	// center window in the monitor with Eclipse
	HMONITOR monitor = MonitorFromWindow(mainShellWindow, MONITOR_DEFAULTTONEAREST);
	if (monitor != NULL) {
//		printf("2: %p\n", monitor);fflush(stdout);
		MONITORINFO mi = {0};
		mi.cbSize = sizeof(MONITORINFO);
		if (GetMonitorInfo(monitor, &mi)) {
//			printf("3: \n");fflush(stdout);
//			printf("4: %p\n", m_wnd);fflush(stdout);
			// get preview window sizes
			RECT rect;
			if (GetWindowRect(m_wnd, &rect)) {
//				printf("5: %p\n", m_wnd);fflush(stdout);
				int w = rect.right - rect.left;
				int h = rect.bottom - rect.top;
				// calc center in the monitor client area
				RECT mrc = mi.rcWork;
				x = mrc.left + (mrc.right - mrc.left - w) / 2;
				y = mrc.top  + (mrc.bottom - mrc.top - h) / 2;
			} else {
				printf("GetWindowRect(m_wnd, &rect) failed!\n");fflush(stdout);
			}
		} else {
			printf("GetMonitorInfo(monitor, &mi) failed!\n");fflush(stdout);
		}
	} else {
		printf("MonitorFromWindow failed!\n");fflush(stdout);
	}
	// show up
//	printf("6: %d, %d\n", x, y);fflush(stdout);
	SetWindowPos(m_wnd, 0, x, y, 0, 0, SWP_NOSIZE | SWP_SHOWWINDOW | SWP_NOZORDER);
//	printf("7: \n");fflush(stdout);
	BringWindowToTop(m_wnd);
//	printf("8: \n");fflush(stdout);
	SetActiveWindow(m_wnd);
//	printf("9: \n");fflush(stdout);
	SetFocus(m_wnd);
//	printf("success: \n");fflush(stdout);
}

LRESULT CALLBACK WebWindow::WndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam) {
	LONG_PTR longPtr = GetWindowLongPtr(hWnd, 0);
	WebWindow* webWindow = reinterpret_cast<WebWindow*>(longPtr);
	if (!webWindow || webWindow->getWindowHandle() != hWnd) { // some paranoid here :)
		// won't process
        return DefWindowProc(hWnd, message, wParam, lParam);
	}
	switch (message) {
	case WM_SIZE:
		{
			// keep filling the entire client area
			webWindow->resizeContents();
		}
		break; 
	case WM_CLOSE:
		{
			// prevent close
			webWindow->hide();
		}
		break; 
    default:
        return DefWindowProc(hWnd, message, wParam, lParam);
    }
    return 0;
}