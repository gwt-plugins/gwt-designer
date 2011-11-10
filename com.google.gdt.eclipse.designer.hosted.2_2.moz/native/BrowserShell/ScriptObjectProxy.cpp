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
#include "ScriptObjectProxy.h"
#include "nsPIDOMWindow190.h" 
#include "nsPIDOMWindow191.h"
#include "nsPIDOMWindow192.h"

ScriptObjectProxy::ScriptObjectProxy() { }
ScriptObjectProxy::~ScriptObjectProxy() { }

class ScriptObjectProxy1713 : public ScriptObjectProxy {
	nsCOMPtr<nsIScriptGlobalObject1713> m_scriptObject;
public:
	ScriptObjectProxy1713(nsIDOMWindow *domWindow) {
		nsCOMPtr<nsIDOMWindow> topWindow;
		domWindow->GetTop(getter_AddRefs(topWindow));
		m_scriptObject = do_QueryInterface(topWindow);
	}
	virtual JSContext* GetNativeContext() {
		nsCOMPtr<nsIScriptContext1713> scriptContext(m_scriptObject->GetContext());
		JSContext* cx = reinterpret_cast<JSContext*>(scriptContext->GetNativeContext());
		return cx;
	}
	virtual JSObject* GetGlobalJSObject() {
		JSObject* go = reinterpret_cast<JSObject*>(m_scriptObject->GetGlobalJSObject());
		return go;
	}
	virtual nsresult EvaluateString(const jchar* script) {
		nsString scriptString;
		scriptString = script;
		nsString aRetValue;
		PRBool aIsUndefined;
		nsCOMPtr<nsIScriptContext1713> scriptContext(m_scriptObject->GetContext());
		return scriptContext->EvaluateString(
			scriptString,
      		GetGlobalJSObject(), 
      		NULL, 
      		"", 
      		0, 
      		NULL,
      		aRetValue,
      		&aIsUndefined);
	}
};
class ScriptObjectProxy2004 : public ScriptObjectProxy {
	nsCOMPtr<nsIScriptGlobalObject2004> m_scriptObject;
public:
	ScriptObjectProxy2004(nsIDOMWindow *domWindow) {
		nsCOMPtr<nsIDOMWindow> topWindow;
		domWindow->GetTop(getter_AddRefs(topWindow));
		m_scriptObject = do_QueryInterface(topWindow);
	}
	virtual JSContext* GetNativeContext() {
		nsCOMPtr<nsIScriptContext2004> scriptContext(m_scriptObject->GetContext());
		JSContext* cx = reinterpret_cast<JSContext*>(scriptContext->GetNativeContext());	
		return cx;
	}
	virtual JSObject* GetGlobalJSObject() {
		JSObject* go = reinterpret_cast<JSObject*>(m_scriptObject->GetGlobalJSObject());
		return go;
	}
	virtual nsresult EvaluateString(const jchar* script) {
		nsString scriptString;
		scriptString = script;
		nsString aRetValue;
		PRBool aIsUndefined;
		nsCOMPtr<nsIScriptContext2004> scriptContext(m_scriptObject->GetContext());
		return scriptContext->EvaluateString(
			scriptString,
      		GetGlobalJSObject(), 
      		NULL, 
      		"", 
      		0, 
      		NULL,
      		&aRetValue,
      		&aIsUndefined);
  	}
};
class ScriptObjectProxy30b5 : public ScriptObjectProxy {
	nsCOMPtr<nsIScriptGlobalObject30b5> m_scriptObject;
public:
	ScriptObjectProxy30b5(nsIDOMWindow *domWindow) {
		nsCOMPtr<nsIDOMWindow> topWindow;
		domWindow->GetTop(getter_AddRefs(topWindow));
		// There is the feature in Mozilla: the window object consists of two parts: 'inner' and 'outer'
		// https://developer.mozilla.org/En/SpiderMonkey/Split_object
		// But it's the only 'outer' window is avalable using public API, so we was forced to use the only it.
		// It worked until 'eval' method occurred in the script body (GXT (ext.js) use it).
		// Current (25.09.2009) implementation has the "an awful gross hack" in jsobj.cpp::js_CheckScopeChainValidity()
		// function (http://mxr.mozilla.org/mozilla1.9.1/source/js/src/jsobj.cpp#1151) which allows 'eval' in 
		// the 'inner' window only.
		// The workaround is to use internal interfaces to get 'inner' window.
		{
			// try 1.9.0.x version
			nsISupports *result;
			nsresult rv = topWindow->QueryInterface(nsPIDOMWindow190::GetIID(), (void**)&result);
			if (NS_SUCCEEDED(rv)) {
				nsPIDOMWindow190 *pWindow = (nsPIDOMWindow190 *)result;
				nsPIDOMWindow190 *innerWindow = pWindow->GetCurrentInnerWindow();
				if (innerWindow) {
					m_scriptObject = do_QueryInterface(innerWindow);
					result->Release();
					return;
				}
				result->Release();
			}
		}
		{
			// try 1.9.1.x version
			nsISupports *result;
			nsresult rv = topWindow->QueryInterface(nsPIDOMWindow191::GetIID(), (void**)&result);
			if (NS_SUCCEEDED(rv)) {
				nsPIDOMWindow191 *pWindow = (nsPIDOMWindow191 *)result;
				nsPIDOMWindow191 *innerWindow = pWindow->GetCurrentInnerWindow();
				if (innerWindow) {
					m_scriptObject = do_QueryInterface(innerWindow);
					result->Release();
					return;
				}
				result->Release();
			}
		}
		// no luck, use 'outer' window (GXT wouldn't work)
		m_scriptObject = do_QueryInterface(topWindow);
	}
	virtual JSContext* GetNativeContext() {
		nsCOMPtr<nsIScriptContext30b5> scriptContext(m_scriptObject->GetContext());
		JSContext* cx = reinterpret_cast<JSContext*>(scriptContext->GetNativeContext());	
		return cx;
	}
	virtual JSObject* GetGlobalJSObject() {
		JSObject* go = reinterpret_cast<JSObject*>(m_scriptObject->GetGlobalJSObject());
		return go;
	}
	virtual nsresult EvaluateString(const jchar* script) {
		nsString scriptString;
		scriptString = script;
		nsString aRetValue;
		PRBool aIsUndefined;
		nsCOMPtr<nsIScriptContext30b5> scriptContext(m_scriptObject->GetContext());
		return scriptContext->EvaluateString(
			scriptString,
      		GetGlobalJSObject(), 
      		NULL, 
      		"", 
      		0, 
      		0,
      		&aRetValue,
      		&aIsUndefined);
  	}
};

class ScriptObjectProxy192 : public ScriptObjectProxy {
	nsCOMPtr<nsIScriptGlobalObject192> m_scriptObject;
public:
	ScriptObjectProxy192(nsIDOMWindow *domWindow) {
		nsCOMPtr<nsIDOMWindow> topWindow;
		domWindow->GetTop(getter_AddRefs(topWindow));
		// There is the feature in Mozilla: the window object consists of two parts: 'inner' and 'outer'
		// https://developer.mozilla.org/En/SpiderMonkey/Split_object
		// But it's the only 'outer' window is avalable using public API, so we was forced to use the only it.
		// It worked until 'eval' method occurred in the script body (GXT (ext.js) use it).
		// Current (22.04.2010) implementation has the "an awful gross hack" in jsobj.cpp::js_CheckScopeChainValidity()
		// function (http://mxr.mozilla.org/mozilla1.9.2/source/js/src/jsobj.cpp#1168) which allows 'eval' in 
		// the 'inner' window only.
		// The workaround is to use internal interfaces to get 'inner' window.
		{
			// try 1.9.2.x version
			nsISupports *result;
			nsresult rv = topWindow->QueryInterface(nsPIDOMWindow192::GetIID(), (void**)&result);
			if (NS_SUCCEEDED(rv)) {
				nsPIDOMWindow192 *pWindow = (nsPIDOMWindow192 *)result;
				nsPIDOMWindow192 *innerWindow = pWindow->GetCurrentInnerWindow();
				if (innerWindow) {
					m_scriptObject = do_QueryInterface(innerWindow);
					result->Release();
					return;
				}
				result->Release();
			}
		}
		// no luck, use 'outer' window (GXT wouldn't work)
		m_scriptObject = do_QueryInterface(topWindow);
	}
	virtual JSContext* GetNativeContext() {
		nsCOMPtr<nsIScriptContext192> scriptContext(m_scriptObject->GetContext());
		JSContext* cx = reinterpret_cast<JSContext*>(scriptContext->GetNativeContext());	
		return cx;
	}
	virtual JSObject* GetGlobalJSObject() {
		JSObject* go = reinterpret_cast<JSObject*>(m_scriptObject->GetGlobalJSObject());
		return go;
	}
	virtual nsresult EvaluateString(const jchar* script) {
		nsString scriptString;
		scriptString = script;
		nsString aRetValue;
		PRBool aIsUndefined;
		nsCOMPtr<nsIScriptContext192> scriptContext(m_scriptObject->GetContext());
		return scriptContext->EvaluateString(
			scriptString,
      		GetGlobalJSObject(), 
      		NULL, 
      		"", 
      		0, 
      		0,
      		&aRetValue,
      		&aIsUndefined);
  	}
};

ScriptObjectProxy* ScriptObjectProxy::GetScriptObjectProxy(nsIDOMWindow *domWindow) {
	// check for Mozilla 1.7, FireFox 1.0 versions
//	fprintf(stderr, "Attempting Firefox 1.0...");
	{
		nsISupports *result;
		nsresult rv = domWindow->QueryInterface(nsIScriptGlobalObject1713::GetIID(), (void**)&result);
		if (NS_SUCCEEDED(rv)) {
//	fprintf(stderr, "found!\n");fflush(stderr);
			result->Release();
			return new ScriptObjectProxy1713(domWindow);
		}
	}
	// check for FireFox 1.5, 2.0 versions, xulrunner 1.8
//	fprintf(stderr, "not found.\nAttempting Firefox 1.5 or 2.0...");fflush(stderr);
	{
		nsISupports *result;
		nsresult rv = domWindow->QueryInterface(nsIScriptGlobalObject2004::GetIID(), (void**)&result);
		if (NS_SUCCEEDED(rv)) {
//	fprintf(stderr, "found!\n");fflush(stderr);
			result->Release();
			return new ScriptObjectProxy2004(domWindow);
		}
	}
	// check for FireFox 3, xulrunner 1.9
//	fprintf(stderr, "not found.\nAttempting Firefox 3.0...");fflush(stderr);
	{
		nsISupports *result;
		nsresult rv = domWindow->QueryInterface(nsIScriptGlobalObject30b5::GetIID(), (void**)&result);
		if (NS_SUCCEEDED(rv)) {
//	fprintf(stderr, "found!\n");fflush(stderr);
			result->Release();
			return new ScriptObjectProxy30b5(domWindow);
		}
	}
	{
		nsISupports *result;
		nsresult rv = domWindow->QueryInterface(nsIScriptGlobalObject192::GetIID(), (void**)&result);
		if (NS_SUCCEEDED(rv)) {
			result->Release();
			return new ScriptObjectProxy192(domWindow);
		}
	}
	// Unknown version
	return NULL;
}


