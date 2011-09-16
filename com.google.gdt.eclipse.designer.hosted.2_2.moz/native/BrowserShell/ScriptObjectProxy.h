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
#ifndef ScriptObjectProxy_h__
#define ScriptObjectProxy_h__

#include "jni.h"
#include "mozilla-headers.h"
#include "nsIScriptGlobalObject1713.h"
#include "nsIScriptContext1713.h"
#include "nsIScriptGlobalObject2004.h"
#include "nsIScriptContext2004.h"
#include "nsIScriptGlobalObject30b5.h"
#include "nsIScriptContext30b5.h"
#include "nsIScriptGlobalObject192.h"
#include "nsIScriptContext192.h"

class ScriptObjectProxy {
protected:
	ScriptObjectProxy();
public:
	static ScriptObjectProxy* GetScriptObjectProxy(nsIDOMWindow *domWindow);
	virtual JSContext* GetNativeContext() = 0;
	virtual JSObject* GetGlobalJSObject() = 0;
	virtual nsresult EvaluateString(const jchar* script) = 0;
	virtual ~ScriptObjectProxy();
};

#endif //ScriptObjectProxy_h__
