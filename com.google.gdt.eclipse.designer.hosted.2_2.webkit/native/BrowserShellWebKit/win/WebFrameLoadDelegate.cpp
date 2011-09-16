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
#include "WebFrameLoadDelegate.h"
#include "utils.h"

WebFrameLoadDelegate::WebFrameLoadDelegate(jobject callback, JNIEnv* env) : m_refCount(1), m_env(env)
{
	m_callback = m_env->NewGlobalRef(callback);
}
WebFrameLoadDelegate::~WebFrameLoadDelegate()
{
	m_env->DeleteGlobalRef(m_callback);
}

HRESULT STDMETHODCALLTYPE WebFrameLoadDelegate::didClearWindowObject(IWebView *webView, JSContextRef context, JSObjectRef windowScriptObject, IWebFrame* frame) 
{
	IWebFrame* mainFrame = NULL;
	if (!FAILED(webView->mainFrame(&mainFrame))) {
		if (mainFrame == frame) {
			JSContextRef context = mainFrame->globalContext();
			jobject jwindowScriptOpbject = wrap_pointer(m_env, context);
			m_env->CallVoidMethod(m_callback, m_BrowserShell_windowScriptObjectAvailable, jwindowScriptOpbject);
		}
		mainFrame->Release();
	}
	return S_OK;
}
HRESULT STDMETHODCALLTYPE WebFrameLoadDelegate::didFinishLoadForFrame(IWebView* webView, IWebFrame* frame)
{
	m_env->CallVoidMethod(m_callback, m_BrowserShell_doneLoading, 0, NULL);
	return S_OK;
}
HRESULT STDMETHODCALLTYPE WebFrameLoadDelegate::didFailLoadWithError(IWebView *webView, IWebError *error, IWebFrame *forFrame)
{
	int errorCode;
	error->code(&errorCode);
	m_env->CallVoidMethod(m_callback, m_BrowserShell_doneLoading, errorCode, NULL);
	return S_OK;
}

HRESULT STDMETHODCALLTYPE WebFrameLoadDelegate::QueryInterface(REFIID riid, void** ppvObject)
{
    *ppvObject = 0;
	if (IsEqualGUID(riid, IID_IUnknown)) {
        *ppvObject = static_cast<IWebFrameLoadDelegate*>(this);
	} else if (IsEqualGUID(riid, IID_IWebFrameLoadDelegate)) {
        *ppvObject = static_cast<IWebFrameLoadDelegate*>(this);
	} else {
        return E_NOINTERFACE;
	}

    AddRef();
    return S_OK;
}

ULONG STDMETHODCALLTYPE WebFrameLoadDelegate::AddRef(void)
{
    return ++m_refCount;
}

ULONG STDMETHODCALLTYPE WebFrameLoadDelegate::Release(void)
{
    ULONG newRef = --m_refCount;
	if (!newRef) {
        delete this;
	}

    return newRef;
}
