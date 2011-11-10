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

#include <iostream>
#include "gwt-jni.h"
#include "utils.h"
#include "JStringWrap.h"
#include "java-dispatch.h"
#include "trace.h"

/*
 *
 */
JSContextRef ToJSContextRef(JNIEnv *env, jobject context) {
  return reinterpret_cast<JSContextRef>(unwrap_pointer(env, context));
}

/*
 *
 */
JSValueRef ToJSValueRef(JNIEnv *env, jobject value) {
  return reinterpret_cast<JSValueRef>(unwrap_pointer(env, value));
}

/*
 *
 */
JSObjectRef ToJSObjectRef(JNIEnv *env, JSContextRef jsContext, jobject object,
    JSValueRef* jsException) {
  JSValueRef jsValue = reinterpret_cast<JSValueRef>(unwrap_pointer(env, object));
  if (!jsValue || !JSValueIsObject(jsContext, jsValue)) {
    return NULL;
  }
  return JSValueToObject(jsContext, jsValue, jsException);
}

/*
 *
 */
JSObjectRef ToJSObjectRef(JSContextRef jsContext, JSValueRef jsValue,
    JSValueRef* jsException) {
  if (!jsValue || !JSValueIsObject(jsContext, jsValue)) {
    return NULL;
  }
  return JSValueToObject(jsContext, jsValue, jsException);
}

/*
 *
 */
JSObjectRef GetStringConstructor(JSContextRef jsContext,
    JSValueRef* jsException) {
  // This could only be cached relative to jsContext.
  JSStringRef script = JSStringCreateWithUTF8CString("(String)");
  JSValueRef ctorVal = JSEvaluateScript(jsContext, script, NULL, NULL, 0, jsException);
  JSStringRelease(script);
  return ToJSObjectRef(jsContext, ctorVal, jsException);
}

/*
 *
 */
bool IsObjectOfStringConstructor(JSContextRef jsContext, JSValueRef jsValue,
    JSValueRef* jsException) {
  JSObjectRef jsObject = ToJSObjectRef(jsContext, jsValue, jsException);
  if (!jsObject) {
    return false;
  }
  JSObjectRef stringCtor = GetStringConstructor(jsContext, jsException);
  if (!stringCtor) {
    return false;
  }
  return JSValueIsInstanceOfConstructor(jsContext, jsObject, stringCtor,
      jsException);
}

#if 0 // For debugging purposes only.
void PrintJSString(JSStringRef jsString)
{
  size_t length = JSStringGetMaximumUTF8CStringSize(jsString);
  char* buffer = new char[length];
  JSStringGetUTF8CString(jsString, buffer, length);
  std::cerr << "JSString: " << buffer << std::endl;
  delete[] buffer;
}

void PrintJSValue(JSContextRef jsContext, JSValueRef jsValue)
{
  JSValueRef jsException = NULL;
  JSStringRef jsResult = JSValueToStringCopy(jsContext, jsValue,
      &jsException);
  if (!jsException && jsValue) {
    PrintJSString(jsResult);
  } else {
    std::cerr << "Could not convert the value to string." << std::endl;
  }
}
#endif

/*
 *
 */
JSStringRef ToJSStringRef(JNIEnv* env, jstring jstr) {
  if (!jstr) {
    return NULL;
  }

  JStringWrap jstrw(env, jstr);
  if (!jstrw.jstr()) {
    return NULL;
  }

  return JSStringCreateWithCharacters(
#ifdef _WIN32
      reinterpret_cast
#else
      static_cast
#endif
      <const JSChar*>(jstrw.jstr()),
      static_cast<size_t>(jstrw.length()));
}

extern "C" {

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(isJsNull)
    (JNIEnv *env, jclass klass, jobject context, jobject value) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  JSValueRef jsValue = ToJSValueRef(env, value);
  if (!jsContext || !jsValue) {
    TR_FAIL();
    return JNI_FALSE;
  }
  TR_LEAVE();
  return static_cast<jboolean>(JSValueIsNull(jsContext, jsValue));
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(isJsUndefined)
    (JNIEnv *env, jclass klass, jobject context, jobject value) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  JSValueRef jsValue = ToJSValueRef(env, value);
  if (!jsContext || !jsValue) {
    TR_FAIL();
    return JNI_FALSE;
  }
  TR_LEAVE();
  return static_cast<jboolean>(JSValueIsUndefined(jsContext, jsValue));
}

/*
 *
 */
JNIEXPORT jobject JNICALL OS_NATIVE_LL(getJsUndefined)
    (JNIEnv *env, jclass klass, jobject context) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  if (!jsContext) {
    return wrap_pointer(env, NULL);
  }

  JSValueRef jsUndefined = JSValueMakeUndefined(jsContext);
  JSValueProtectChecked(jsContext, jsUndefined);
  TR_LEAVE();
  return wrap_pointer(env, jsUndefined);
}

/*
 *
 */
JNIEXPORT jobject JNICALL OS_NATIVE_LL(getJsNull)
    (JNIEnv *env, jclass klass, jobject context) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  if (!jsContext) {
    return wrap_pointer(env, NULL);
  }
  JSValueRef jsNull = JSValueMakeNull(jsContext);
  JSValueProtectChecked(jsContext, jsNull);
  TR_LEAVE();
  return wrap_pointer(env, jsNull);
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(isJsBoolean)
    (JNIEnv *env, jclass klass, jobject context, jobject value) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  JSValueRef jsValue = ToJSValueRef(env, value);
  if (!jsContext || !jsValue) {
    TR_FAIL();
    return JNI_FALSE;
  }
  TR_LEAVE();
  return static_cast<jboolean>(JSValueIsBoolean(jsContext, jsValue));
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(isJsNumber)
    (JNIEnv *env, jclass klass, jobject context, jobject value) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  JSValueRef jsValue = ToJSValueRef(env, value);
  if (!jsContext || !jsValue) {
    TR_FAIL();
    return JNI_FALSE;
  }
  TR_LEAVE();
  return static_cast<jboolean>(JSValueIsNumber(jsContext, jsValue));
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(toJsBooleanImpl)
    (JNIEnv *env, jclass klass, jobject context, jboolean jValue, jobject rval) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  if (!jsContext) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSValueRef jsValue = JSValueMakeBoolean(jsContext, jValue == JNI_TRUE ? true : false);

  add_to_List(env, rval, wrap_pointer(env, jsValue));
  if (env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }
  JSValueProtectChecked(jsContext, jsValue);

  TR_LEAVE();
  return JNI_TRUE;
}

/*
 *
 */
JNIEXPORT void JNICALL OS_NATIVE_LL(gcUnprotect)
    (JNIEnv *env, jclass klass, jobject context, jobject value) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  JSValueRef jsValue = ToJSValueRef(env, value);
  if (!jsContext || !jsValue) {
    return;
  }
  JSValueUnprotectChecked(jsContext, jsValue);
  TR_LEAVE();
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(toJsNumberImpl)
    (JNIEnv *env, jclass klass, jobject context, jdouble jValue, jobject rval) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  if (!jsContext) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSValueRef jsValue = JSValueMakeNumber(jsContext, static_cast<jdouble>(jValue));

  add_to_List(env, rval, wrap_pointer(env, jsValue));
  if (env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSValueProtectChecked(jsContext, jsValue);

  TR_LEAVE();
  return JNI_TRUE;
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(executeScriptWithInfoImpl)
    (JNIEnv *env, jclass klass, jobject context, jstring jScript, jstring jUrl,
    jint jLine, jobject rval) {
  TR_ENTER();
  JSValueRef jsException = NULL;

  JSContextRef jsContext = ToJSContextRef(env, context);
  if (!jsContext) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSStringRef jsScript = ToJSStringRef(env, jScript);
  if (!jsScript) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSStringRef jsUrl = ToJSStringRef(env, jUrl);

  // Evaluate will set this to global object.
  JSValueRef jsResult = JSEvaluateScript(jsContext, jsScript, NULL, jsUrl,
      static_cast<int>(jLine), &jsException);
  if (jsException) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSStringRelease(jsScript);
  if (jsUrl) {
    JSStringRelease(jsUrl);
  }

  add_to_List(env, rval, wrap_pointer(env, jsResult));
  if (env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSValueProtectChecked(jsContext, jsResult);

  TR_LEAVE();
  return JNI_TRUE;
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(toJsStringImpl)
    (JNIEnv *env, jclass klass, jobject context, jstring jValue, jobject rval) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  if (!jsContext) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSStringRef jsString = ToJSStringRef(env, jValue);
  if (!jsString) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSValueRef jsValue = JSValueMakeString(jsContext, jsString);
  JSStringRelease(jsString);

  add_to_List(env, rval, wrap_pointer(env, jsValue));
  if (env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSValueProtectChecked(jsContext, jsValue);

  TR_LEAVE();
  return JNI_TRUE;
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(isJsStringImpl)
    (JNIEnv *env, jclass klass, jobject context, jobject value, jbooleanArray rval) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  JSValueRef jsValue = ToJSValueRef(env, value);
  if (!jsContext || !jsValue) {
    TR_FAIL();
    return JNI_FALSE;
  }

  bool isString = JSValueIsString(jsContext, jsValue);
  if (!isString) {
    JSValueRef jsException = NULL;
    isString = IsObjectOfStringConstructor(jsContext, jsValue, &jsException);
    if (jsException) {
      TR_FAIL();
      return JNI_FALSE;
    }
  }

  jboolean jIsString = static_cast<jboolean>(isString);
  env->SetBooleanArrayRegion(rval, 0, 1, &jIsString);
  if (env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }

  TR_LEAVE();
  return JNI_TRUE;
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(invokeImpl)
    (JNIEnv *env, jclass klass, jobject context, jobject scriptValue,
    jstring jMethodName, jobject thisVal, jobject jArgs, jint jArgsLength,
    jobject rval) {
  TR_ENTER();

  JSContextRef jsContext = ToJSContextRef(env, context);
  if (!jsContext) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSObjectRef jsScriptObj = ToJSObjectRef(jsContext, reinterpret_cast<JSValueRef>(unwrap_pointer(env, scriptValue)), NULL);
  if (!jsScriptObj) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSValueRef jsThisVal = ToJSValueRef(env, thisVal);
  JSObjectRef jsThisObj = NULL;
  // If thisVal is null, jsNull, or jsUndefined use the script object
  // as this.
  if (!jsThisVal || JSValueIsNull(jsContext, jsThisVal)
      || JSValueIsUndefined(jsContext, jsThisVal)) {
    jsThisObj = jsScriptObj;
  } else {
    // If we are given a value, ensure that it is an object.
    jsThisObj = ToJSObjectRef(jsContext, jsThisVal, NULL);
    if (!jsThisObj) {
      TR_FAIL();
      return JNI_FALSE;
    }
  }

  JSStringRef jsMethodName = ToJSStringRef(env, jMethodName);
  if (!jsMethodName) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSObjectRef jsMethod = ToJSObjectRef(jsContext, JSObjectGetProperty(jsContext,
      jsScriptObj, jsMethodName, NULL), NULL);
  if (!jsMethod || !JSObjectIsFunction(jsContext, jsMethod)) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSStringRelease(jsMethodName);

  JSValueRef* jsArgs = new JSValueRef[static_cast<size_t>(jArgsLength)];
  for (jint idx = 0; idx < jArgsLength; ++idx) {
  	jsArgs[idx] = reinterpret_cast<JSValueRef>(get_from_List_unwrapped(env, jArgs, idx));
  }
  if (env->ExceptionCheck()) {
    TR_FAIL();
    delete[] jsArgs;
    return JNI_FALSE;
  }

  JSValueRef jsException = NULL;
  JSValueRef jsResult = JSObjectCallAsFunction(jsContext, jsMethod, jsThisObj,
      static_cast<size_t>(jArgsLength), jsArgs, &jsException);
  if (jsException) {
    TR_FAIL();
    delete[] jsArgs;
    return JNI_FALSE;
  }
  delete[] jsArgs;
  add_to_List(env, rval, wrap_pointer(env, jsResult));
  if (env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSValueProtectChecked(jsContext, jsResult);

  TR_LEAVE();
  return JNI_TRUE;
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(isJsObject)
    (JNIEnv *env, jclass klass, jobject context, jobject value) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  JSValueRef jsValue = ToJSValueRef(env, value);
  if (!jsContext || !jsValue) {
    TR_FAIL();
    return JNI_FALSE;
  }

  TR_LEAVE();
  return static_cast<jboolean>(JSValueIsObject(jsContext, jsValue));
}

JNIEXPORT jboolean JNICALL OS_NATIVE_LL(toBooleanImpl)
    (JNIEnv *env, jclass klass, jobject context, jobject value, jbooleanArray rval) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  JSValueRef jsValue = ToJSValueRef(env, value);
  if (!jsContext || !jsValue) {
    TR_FAIL();
    return JNI_FALSE;
  }

  jboolean jResult = static_cast<jboolean>(JSValueToBoolean(jsContext, jsValue));
  env->SetBooleanArrayRegion(rval, 0, 1, &jResult);
  if (env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }

  TR_LEAVE();
  return JNI_TRUE;
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(toDoubleImpl)
    (JNIEnv *env, jclass klass, jobject context, jobject value, jdoubleArray rval) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  JSValueRef jsValue = ToJSValueRef(env, value);
  if (!jsContext || !jsValue) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSValueRef jsException = NULL;
  double result = JSValueToNumber(jsContext, jsValue, &jsException);
  if (jsException) {
    TR_FAIL();
    return JNI_FALSE;
  }

  env->SetDoubleArrayRegion(rval, 0, 1, static_cast<jdouble*>(&result));
  if (env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }

  TR_LEAVE();
  return JNI_TRUE;
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(toStringImpl)
    (JNIEnv *env, jclass klass, jobject context, jobject value, jobjectArray rval) {
  TR_ENTER();
  JSValueRef jsException = NULL;
  JSContextRef jsContext = ToJSContextRef(env, context);
  JSValueRef jsValue = ToJSValueRef(env, value);
  if (!jsContext || !jsValue) {
    TR_FAIL();
    return JNI_FALSE;
  }

  jstring jResult = NULL;
   // Convert all objects to their string representation, EXCEPT
   // null and undefined which will be returned as a true NULL.
  if (!JSValueIsNull(jsContext, jsValue) &&
      !JSValueIsUndefined(jsContext, jsValue)) {
    JSStringRef jsResult = JSValueToStringCopy(jsContext, jsValue, &jsException);
    if (jsException) {
      TR_FAIL();
      return JNI_FALSE;
    }

    jResult = env->NewString(
#ifdef _WIN32
      reinterpret_cast
#else
      static_cast
#endif
      <const jchar*>(JSStringGetCharactersPtr(jsResult)),
      static_cast<jsize>(JSStringGetLength(jsResult)));
    if (env->ExceptionCheck()) {
      TR_FAIL();
      return JNI_FALSE;
    }

    JSStringRelease(jsResult);
  }

  env->SetObjectArrayElement(rval, 0, jResult);
  if (env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }

  TR_LEAVE();
  return JNI_TRUE;
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(wrapDispatchObjectImpl)
    (JNIEnv *env, jclass klass, jobject context, jobject dispatch, jobject rval) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  if (!jsContext) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSObjectRef jsDispatch = gwt::DispatchObjectCreate(jsContext, dispatch);
  if (!jsDispatch || env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }
  add_to_List(env, rval, wrap_pointer(env, jsDispatch));
  if (env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSValueProtectChecked(jsContext, jsDispatch);

  TR_LEAVE();
  return JNI_TRUE;
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(unwrapDispatchObjectImpl)
    (JNIEnv *env, jclass klass, jobject context, jobject value, jobject rval) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  JSValueRef jsValue = ToJSValueRef(env, value);
  if (!jsContext || !jsValue) {
    TR_FAIL();
    return JNI_FALSE;
  }

  if (!JSValueIsObjectOfClass(jsContext, jsValue, gwt::GetDispatchObjectClass())) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSObjectRef jsObject = ToJSObjectRef(jsContext, jsValue, NULL);
  if (!jsObject) {
    TR_FAIL();
    return JNI_FALSE;
  }

  add_to_List(env, rval, reinterpret_cast<jobject>(JSObjectGetPrivate(jsObject)));
  if (env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }

  TR_LEAVE();
  return JNI_TRUE;
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(initImpl)
    (JNIEnv *env, jclass klass, jclass dispatchObjectClass,
    jclass dispatchMethodClass, jclass lowLevelClass) {
  TR_ENTER();
  TR_LEAVE();
  return static_cast<jboolean>(gwt::Initialize(env, dispatchObjectClass, dispatchMethodClass, lowLevelClass));
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(wrapDispatchMethodImpl)
    (JNIEnv *env, jclass klass, jobject context, jstring name, jobject jDispatch,
    jobject rval) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  if (!jsContext) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JStringWrap nameWrap(env, name);
  std::string nameStr(nameWrap.str());
  JSObjectRef jsDispatch = gwt::DispatchMethodCreate(jsContext, nameStr,
      jDispatch);
  if (!jsDispatch || env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }
  add_to_List(env, rval, wrap_pointer(env, jsDispatch));

  if (env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSValueProtectChecked(jsContext, jsDispatch);

  TR_LEAVE();
  return JNI_TRUE;
}

/*
 *
 */
JNIEXPORT jstring JNICALL OS_NATIVE_LL(getTypeString)
    (JNIEnv *env, jclass klass, jobject context, jobject value) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  JSValueRef jsValue = ToJSValueRef(env, value);
  if (!jsContext || !jsValue) {
    return NULL;
  }

  switch (JSValueGetType(jsContext, jsValue)) {
    case kJSTypeUndefined:
      return env->NewStringUTF("undefined");
    case kJSTypeNull:
      return env->NewStringUTF("null");
    case kJSTypeBoolean:
      return env->NewStringUTF("boolean");
    case kJSTypeNumber:
      return env->NewStringUTF("number");
    case kJSTypeString:
      return env->NewStringUTF("string");
    case kJSTypeObject:
      return (JSValueIsObjectOfClass(jsContext, jsValue, gwt::GetDispatchObjectClass()))
        ? env->NewStringUTF("Java object") : env->NewStringUTF("JavaScript object");
    default:
      return env->NewStringUTF("unknown");
  }
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(isDispatchObjectImpl)
    (JNIEnv *env, jclass klass, jobject context, jobject value, jbooleanArray rval) {
  TR_ENTER();
  JSContextRef jsContext = ToJSContextRef(env, context);
  JSValueRef jsValue = ToJSValueRef(env, value);
  if (!jsContext || !jsValue) {
    TR_FAIL();
    return JNI_FALSE;
  }

  jboolean jIsDispatchObject = static_cast<jboolean>(JSValueIsObjectOfClass(
      jsContext, jsValue, gwt::GetDispatchObjectClass()));
  env->SetBooleanArrayRegion(rval, 0, 1, &jIsDispatchObject);
  if (env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }

  TR_LEAVE();
  return JNI_TRUE;
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(getGlobalJsObjectImpl)
    (JNIEnv *env, jclass klass, jobject context, jobject rval) {
  TR_ENTER();

  JSContextRef jsContext = ToJSContextRef(env, context);
  if (!jsContext) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSObjectRef jsGlobalObject = JSContextGetGlobalObject(jsContext);
  add_to_List(env, rval, wrap_pointer(env, jsGlobalObject));
		
  if (env->ExceptionCheck()) {
    TR_FAIL();
    return JNI_FALSE;
  }

  JSValueProtectChecked(jsContext, jsGlobalObject);

  TR_LEAVE();
  return JNI_TRUE;
}

/*
 *
 */
JNIEXPORT void JNICALL OS_NATIVE_LL(gcProtect)
    (JNIEnv *env, jclass klass, jobject context, jobject value) {
  TR_ENTER();

  JSContextRef jsContext = ToJSContextRef(env, context);
  JSValueRef jsValue = ToJSValueRef(env, value);
  if (!jsContext || !jsValue) {
    return;
  }

  JSValueProtectChecked(jsContext, jsValue);
  TR_LEAVE();
}

/*
 *
 */
JNIEXPORT void JNICALL OS_NATIVE_LL(retainJsGlobalContext)
    (JNIEnv *env, jclass klass, jobject context) {
  TR_ENTER();
  JSGlobalContextRef jsContext = (JSGlobalContextRef)unwrap_pointer(env, context);
  if (!jsContext) {
    TR_FAIL();
    return;
  }
  JSGlobalContextRetain(jsContext);
  TR_LEAVE();
}

/*
 *
 */
JNIEXPORT void JNICALL OS_NATIVE_LL(releaseJsGlobalContext)
    (JNIEnv *env, jclass klass, jobject context) {
  TR_ENTER();
  JSGlobalContextRef jsContext = (JSGlobalContextRef)unwrap_pointer(env, context);
  if (!jsContext) {
    TR_FAIL();
    return;
  }
  JSGlobalContextRelease(jsContext);
  TR_LEAVE();
}

/*
 *
 */
JNIEXPORT jboolean JNICALL OS_NATIVE_LL(isGcProtected)
    (JNIEnv *env, jclass klass, jobject value) {
  JSValueRef jsValue = ToJSValueRef(env, value);
  TR_ENTER();
  TR_LEAVE();
  return static_cast<jboolean>(JSValueIsProtected(jsValue));
}

JNIEXPORT jboolean JNICALL OS_NATIVE_LL(isJsValueProtectionCheckingEnabledImpl)
    (JNIEnv *env, jclass klass) {
  TR_ENTER();
  TR_LEAVE();
  return static_cast<jboolean>(JSValueProtectCheckingIsEnabled());
}

} // extern "C"
