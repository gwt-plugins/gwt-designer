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
#include "WebUIDelegate.h"
#include "utils.h"

WebUIDelegate::WebUIDelegate(jobject callback, JNIEnv* env) : m_refCount(1), m_env(env)
{
	m_callback = m_env->NewGlobalRef(callback);
}
WebUIDelegate::~WebUIDelegate()
{
	m_env->DeleteGlobalRef(m_callback);
}

HRESULT WebUIDelegate::QueryInterface(REFIID riid, void** ppvObject)
{
    *ppvObject = 0;
	if (IsEqualIID(riid, IID_IUnknown)) {
        *ppvObject = static_cast<IWebUIDelegate*>(this);
	} else if (IsEqualIID(riid, IID_IWebUIDelegate)) {
        *ppvObject = static_cast<IWebUIDelegate*>(this);
	} else {
        return E_NOINTERFACE;
	}

    AddRef();
    return S_OK;
}

ULONG WebUIDelegate::AddRef(void)
{
    return ++m_refCount;
}

ULONG WebUIDelegate::Release(void)
{
    ULONG newRef = --m_refCount;
	if (!newRef) {
        delete this;
	}

    return newRef;
}

HRESULT STDMETHODCALLTYPE WebUIDelegate::runJavaScriptAlertPanelWithMessage(IWebView* webView, BSTR message)
{ 
	m_env->CallVoidMethod(m_callback, m_BrowserShell_scriptAlert, (message == NULL ? NULL : m_env->NewString((jchar*)message, wcslen(message))));
	return S_OK; 
}
