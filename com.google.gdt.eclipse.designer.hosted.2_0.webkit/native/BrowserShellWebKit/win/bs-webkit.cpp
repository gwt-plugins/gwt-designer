/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "gwt-jni-win32.h"
#include "utils.h"
#include "WebWindow.h"
#include <WebKit/WebKit.h>

JNIEnv* gEnv;
JavaVM* gJavaVM;
jclass c_BrowserShell;

jmethodID m_BrowserShell_windowScriptObjectAvailable;
jmethodID m_BrowserShell_doneLoading;
jmethodID m_BrowserShell_scriptAlert;

static WebWindow* getWebWindow(JNIEnv *env, jobject jwnd) {
	return (WebWindow*)unwrap_pointer(env, jwnd);
}

static HBITMAP printWindow(HWND hWnd) {
    HDC hDCMem = CreateCompatibleDC(NULL);

    RECT rect;
    GetWindowRect(hWnd, &rect);

    HBITMAP hBmp = NULL;
    {
        HDC hDC = GetDC(hWnd);
        hBmp = CreateCompatibleBitmap(hDC, rect.right - rect.left, rect.bottom - rect.top);
        ReleaseDC(hWnd, hDC);
    }

    HGDIOBJ hOld = SelectObject(hDCMem, hBmp);
    SendMessage(hWnd, WM_PRINT, (WPARAM)hDCMem, PRF_CHILDREN | PRF_CLIENT | PRF_ERASEBKGND | PRF_NONCLIENT | PRF_OWNED);

    SelectObject(hDCMem, hOld);
    DeleteObject(hDCMem);
	return  hBmp;
}

extern "C" { 
JNIEXPORT jboolean JNICALL OS_NATIVE(_1init)
	(JNIEnv *env, jclass clazz, jclass llClass)
{
	gEnv = env;
	env->GetJavaVM(&gJavaVM);
	//
	c_BrowserShell = (jclass)env->NewGlobalRef(llClass);
	if (!gJavaVM || !c_BrowserShell || env->ExceptionCheck()) {
		return JNI_FALSE;
	}
	
	m_BrowserShell_windowScriptObjectAvailable = env->GetMethodID(c_BrowserShell, "windowScriptObjectAvailable", "(Ljava/lang/Number;)V");
	m_BrowserShell_doneLoading = env->GetMethodID(c_BrowserShell, "doneLoading", "(ILjava/lang/String;)V");
	m_BrowserShell_scriptAlert = env->GetMethodID(c_BrowserShell, "scriptAlert", "(Ljava/lang/String;)V");

	if (!m_BrowserShell_windowScriptObjectAvailable ||
		!m_BrowserShell_doneLoading ||
		!m_BrowserShell_scriptAlert ||
		env->ExceptionCheck()) {
		return JNI_FALSE;
	}
	WebWindow::Init();
	return JNI_TRUE;
}

JNIEXPORT jobject JNICALL OS_NATIVE(_1create)
	(JNIEnv *env, jclass jclazz, jobject jcallback)
{
	// always check for no_proxy env variable
	checkNoProxy();

	WebWindow *wnd = new WebWindow();
	if (wnd->initWithCallback(jcallback, env)) {
		return wrap_pointer(env, wnd);
	}
	delete wnd;
	return NULL;
}

JNIEXPORT void JNICALL OS_NATIVE(_1release)
	(JNIEnv *env, jclass that, jobject jwnd)
{
	WebWindow *win = getWebWindow(env, jwnd);
	delete win;
}

JNIEXPORT void JNICALL OS_NATIVE(_1setBounds)
	(JNIEnv *env, jclass that, jobject jwnd, jint x, jint y, jint width, jint height)
{
	HWND wnd = getWebWindow(env, jwnd)->getWindowHandle();
	SetWindowPos(wnd, HWND_BOTTOM, x, y, width, height, SWP_NOACTIVATE | SWP_NOZORDER);
}

JNIEXPORT void JNICALL OS_NATIVE(_1getBounds)
	(JNIEnv *env, jclass that, jobject jwnd, jshortArray jbounds)
{
	RECT rect;
	GetWindowRect(getWebWindow(env, jwnd)->getWindowHandle(), &rect);
	jshort values[4];
	values[0] = (jshort)rect.left;
	values[1] = (jshort)(rect.right - rect.left);
	values[2] = (jshort)rect.top;
	values[3] = (jshort)(rect.bottom - rect.top);
	env->SetShortArrayRegion(jbounds, 0, 4, values);
}

JNIEXPORT void JNICALL OS_NATIVE(_1computeTrim)
	(JNIEnv *env, jclass that, jobject jwnd, jshortArray jbounds)
{
	HWND wnd = getWebWindow(env, jwnd)->getWindowHandle();
	jshort b[4];
	env->GetShortArrayRegion(jbounds, 0, 4, b);
	RECT rect;
	SetRect (&rect, b[0] /*x*/, b[1] /*y*/, b[0] + b[2] /*x + width*/, b[1] + b[3] /*y + height*/);
	LONG_PTR bits1 = GetWindowLongPtr(wnd, GWL_STYLE);
	LONG_PTR bits2 = GetWindowLongPtr(wnd, GWL_EXSTYLE);
	AdjustWindowRectEx(&rect, (DWORD)bits1, FALSE, (DWORD)bits2);

	jshort values[4];
	values[0] = (jshort)rect.left;
	values[1] = (jshort)rect.top;
	values[2] = (jshort)(rect.right - rect.left);
	values[3] = (jshort)(rect.bottom - rect.top);
	env->SetShortArrayRegion(jbounds, 0, 4, values);
}

JNIEXPORT void JNICALL OS_NATIVE(_1setVisible)
	(JNIEnv *env, jclass that, jobject jwnd, jboolean jvisible)
{
	WebWindow* wnd = getWebWindow(env, jwnd);
	if (jvisible == JNI_TRUE) {
		wnd->show();
	} else {
		wnd->hide();
	}
}

JNIEXPORT void JNICALL OS_NATIVE(_1showAsPreview) 
	(JNIEnv *env, jclass that, jobject jwnd, jobject jshell)
{
	WebWindow* wnd = getWebWindow(env, jwnd);
	wnd->showAsPreview((HWND)unwrap_pointer(env, jshell));
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1isVisible)
	(JNIEnv* env, jclass that, jobject jwnd)
{
	WebWindow* wnd = getWebWindow(env, jwnd);
	HWND hwnd = wnd->getWindowHandle();
	return IsWindowVisible(hwnd) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL OS_NATIVE(_1setUrl)
	(JNIEnv* env, jclass that, jobject jwnd, jstring jlocation)
{
	jsize length = env->GetStringLength(jlocation);
	const jchar* jchars = env->GetStringChars(jlocation, NULL);
	BSTR bstr = SysAllocStringLen((const OLECHAR*)jchars, length);
	env->ReleaseStringChars(jlocation, jchars);
    getWebWindow(env, jwnd)->loadURL(bstr);
	SysFreeString(bstr);
}

JNIEXPORT jobject JNICALL OS_NATIVE(_1makeShot)
(JNIEnv *env, jclass jclazz, jobject jwnd)
{
	WebWindow* wnd = getWebWindow(env, jwnd);
	HBITMAP hBmp = printWindow(wnd->getWebViewWindowHandle());
	return wrap_pointer(env, hBmp);
}

} // extern "C"