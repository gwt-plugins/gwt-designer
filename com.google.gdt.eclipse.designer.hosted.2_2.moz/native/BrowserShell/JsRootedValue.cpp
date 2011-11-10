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

#include "JsRootedValue.h"

jsval JsRootedValue::JSVAL_VOID = 0x16; // default to v1.9.1

// Initialize static value used to hold the JavaScript String class.
JSClass* JsRootedValue::stringClass = 0;

// Initialize static reference to the sole JSRuntime value in Gecko.
JSRuntime* JsRootedValue::runtime = 0;

// Static stack of JavaScript execution contexts.
std::stack<JSContext*> JsRootedValue::contextStack;

/*
 * Actually get the stringClass pointer from JavaScript.
 */
void JsRootedValue::fetchStringClass() const {
  Tracer tracer("JsRootedValue::fetchStringClass");
  JSContext* cx = currentContext();
  jsval val = JS_GetEmptyStringValue(cx);
  JSObject* obj;
  // on error, leave stringClass null
  if (!JS_ValueToObject(cx, val, &obj)) return;
  if (!obj) {
    tracer.log("ensureStringClass: null object");
    return;
  }
  stringClass = JS_GET_CLASS(cx, obj);
  tracer.log("stringClass=%p", stringClass);
}

bool ExceptionCheck(JNIEnv *env) {
	jobject exception = env->ExceptionOccurred();
	if (exception) {
		env->DeleteLocalRef(exception);
		return true;
    }
	return false;
}

