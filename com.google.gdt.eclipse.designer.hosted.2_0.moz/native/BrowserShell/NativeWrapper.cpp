/*
 * Copyright 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * Defines the JavaScript classes gwt_nativewrapper_class and
 * gwt_functionwrapper_class, which interface via JNI to Java objects.
 */

#include <jni.h>
#include "JsRootedValue.h"
#include "gwt-jni.h"
#include "Tracer.h"

/*
 * Helper function to get reference Java attributes from Javascript.
 * 
 * cx - JSContext pointer
 * obj - JavaScript object which is a wrapped Java object
 * id - property name, as a jsval string
 * dispClass - output parameter of DispatchMethod subclass
 * dispObj - output parameter of Java object
 * jident - output parameter of property name as a Java string
 */
static JSBool getJavaPropertyStats(JSContext *cx, JSObject *obj, jsval id,
    jclass& dispClass, jobject& dispObj, jstring& jident)
{
  Tracer tracer("getJavaPropertyStats");
  if (!JSVAL_IS_STRING(id) && !JSVAL_IS_NUMBER(id)) {
    tracer.setFail("id is not a string or numeric value");
    return JS_FALSE;
  }
  
  JSString* jsName = NULL;
  if (!JSVAL_IS_STRING(id)) {
    jsName = JS_ValueToString(cx, id);
  } else {
    jsName = JSVAL_TO_STRING(id);
  }
  jident = savedJNIEnv->NewString(JS_GetStringChars(jsName),
      JS_GetStringLength(jsName));
  if (!jident || ExceptionCheck(savedJNIEnv)) {
    tracer.setFail("unable to create Java string");
    return JS_FALSE;
  }

  dispObj = reinterpret_cast<jobject>(JS_GetPrivate(cx, obj));
  if (!dispObj) {
    tracer.setFail("can't get dispatch object");
    return JS_FALSE;
  }

  dispClass = savedJNIEnv->GetObjectClass(dispObj);
  if (ExceptionCheck(savedJNIEnv)) {
    tracer.setFail("can't get class of dispatch object");
    return JS_FALSE;
  }

  return JS_TRUE;
}
  
/*
 * Returns the value of a field on a Java object.
 * 
 * context - JavaScript context
 * clazz - class of obj
 * obj - Java object to retreive field from
 * fieldName - name of field on Java object to retrieve
 * 
 * Returns null on failure.  Caller is responsible for deleting
 * returned JsRootedValue when done with it.
 */
static JsRootedValue* GetFieldAsRootedValue(JSContext* cx, jclass clazz,
    jobject obj, jstring fieldName)
{
  Tracer tracer("GetFieldAsRootedValue");
  JsRootedValue::ContextManager context(cx);
  jmethodID getFieldMeth = savedJNIEnv->GetMethodID(clazz, "getField", GET_FIELD_SIGNATURE);
  if (!getFieldMeth || ExceptionCheck(savedJNIEnv)) {
    return 0;
  }

  JsRootedValue* jsRootedValue = new JsRootedValue();
  savedJNIEnv->CallVoidMethod(obj, getFieldMeth, fieldName,
    reinterpret_cast<JHANDLE>(jsRootedValue));
  if (ExceptionCheck(savedJNIEnv)) {
    delete jsRootedValue;
    return 0;
  }
  return jsRootedValue;
}
  
/*
 * Sets the value of a field on a Java object.
 * 
 * context - JavaScript context
 * clazz - class of obj
 * obj - Java object to store into field
 * fieldName - name of field on Java object to store into
 * jsRootedValue - the value to store in the field
 * 
 * returns true on success, false on failure
 */
static bool SetFieldFromRootedValue(JSContext* cx, jclass clazz,
    jobject obj, jstring fieldName, JsRootedValue* jsRootedValue)
{
  Tracer tracer("SetFieldAsRootedValue");
  JsRootedValue::ContextManager context(cx);
  jmethodID getFieldMeth = savedJNIEnv->GetMethodID(clazz, "setField", SET_FIELD_SIGNATURE);
  if (!getFieldMeth || ExceptionCheck(savedJNIEnv)) {
    return false;
  }

  savedJNIEnv->CallVoidMethod(obj, getFieldMeth, fieldName,
    reinterpret_cast<JHANDLE>(jsRootedValue));
  if (ExceptionCheck(savedJNIEnv)) {
    return false;
  }

  return true;
}

static JSBool gwt_nativewrapper_getProperty(JSContext *cx,
    JSObject *obj, jsval id, jsval *vp)
{
  Tracer tracer("gwt_nativewrapper_getProperty");
  tracer.log("context=%p", cx);
  if (!JsRootedValue::isVoid(*vp)) {
    return JS_TRUE;
  }
  JsRootedValue::ContextManager context(cx);

  jclass dispClass;
  jobject dispObj;
  jstring ident;
  if (!getJavaPropertyStats(cx, obj, id, dispClass, dispObj, ident)) {
    tracer.setFail("getJavaPropertyStats failed");
    return JS_FALSE;
  }
  JsRootedValue* js_rooted_value = GetFieldAsRootedValue(cx, dispClass,
      dispObj, ident);
  if (!js_rooted_value) {
    tracer.setFail("can't get field");
    return JS_FALSE;
  }
  *vp = js_rooted_value->getValue();
  return JS_TRUE;
}

static void gwt_nativewrapper_finalize(JSContext *cx,
    JSObject *obj)
{
  Tracer tracer("gwt_nativewrapper_finalize");
  jobject dispObj = reinterpret_cast<jobject>(JS_GetPrivate(cx, obj));
  if (dispObj) {
    // Remove this pairing from the global map.
    jmethodID removeMethod = savedJNIEnv->GetStaticMethodID(lowLevelMozClass, "removeJsvalForObject",
      "(Ljava/lang/Object;)V");
    if (!removeMethod || ExceptionCheck(savedJNIEnv)) {
      tracer.setFail("Cannot GetMethodID for removeJsvalForObject");
      return;
    }
    savedJNIEnv->CallStaticVoidMethod(lowLevelMozClass, removeMethod, dispObj);
    if (ExceptionCheck(savedJNIEnv)) {
      tracer.setFail("Exception calling removeJsvalForObject");
      return;
    }
    savedJNIEnv->DeleteGlobalRef(dispObj);
  }
}

static JSBool gwt_nativewrapper_setProperty(JSContext *cx, JSObject *obj, jsval id, jsval *vp)
{
  Tracer tracer("gwt_nativewrapper_setProperty");
  tracer.log("context=%p", cx);
  JsRootedValue::ContextManager context(cx);
  jclass dispClass;
  jobject dispObj;
  jstring ident;
  if (!getJavaPropertyStats(cx, obj, id, dispClass, dispObj, ident)) {
    tracer.setFail("getJavaPropertyStats failed");
    return JS_FALSE;
  }
  JsRootedValue* js_rooted_value = new JsRootedValue(*vp); 
  if (!SetFieldFromRootedValue(cx, dispClass, dispObj, ident,
      js_rooted_value)) {
    tracer.setFail("can't set field");
    return JS_FALSE;
  }
  return JS_TRUE;
}

JSClass gwt_nativewrapper_class = {
  "gwt_nativewrapper_class", JSCLASS_HAS_PRIVATE,
  JS_PropertyStub, JS_PropertyStub, gwt_nativewrapper_getProperty,
  gwt_nativewrapper_setProperty, JS_EnumerateStub, JS_ResolveStub,
  JS_ConvertStub, gwt_nativewrapper_finalize,
  JSCLASS_NO_OPTIONAL_MEMBERS
};

JSClass gwt_functionwrapper_class = {
  "gwt_functionwrapper_class", JSCLASS_HAS_PRIVATE,
  JS_PropertyStub, JS_PropertyStub, JS_PropertyStub, JS_PropertyStub,
  JS_EnumerateStub, JS_ResolveStub, JS_ConvertStub, gwt_nativewrapper_finalize,
  JSCLASS_NO_OPTIONAL_MEMBERS
};
