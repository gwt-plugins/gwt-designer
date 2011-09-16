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

#include <jni.h>
#include "JsRootedValue.h"
#include "JStringWrap.h"
#include "gwt-jni.h"
#include "js-classes.h"
#include "Tracer.h"

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
  jmethodID getFieldMeth = savedJNIEnv->GetMethodID(clazz, "getField",
      "(Ljava/lang/String;I)V");
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

/*
 * Throws a HostedModeException with the specified message.
 */
static void ThrowHostedModeException(JNIEnv* jniEnv, const char* msg) {
  jclass exceptionClass
      = jniEnv->FindClass("com/google/gwt/dev/shell/HostedModeException");
  jniEnv->ThrowNew(exceptionClass, msg);
}

/*
 * Types of jsvals.
 */
enum JsValueType {
  JSVAL_TYPE_VOID=0,
  JSVAL_TYPE_NULL,
  JSVAL_TYPE_BOOLEAN,
  JSVAL_TYPE_NUMBER,
  JSVAL_TYPE_STRING,
  JSVAL_TYPE_OBJECT,
  JSVAL_TYPE_UNKNOWN,
};

/*
 * Names of jsval types -- must match the order of the enum above.
 */
static const char* JsValueTypeStrings[]={
  "undefined",
  "null",
  "boolean",
  "number",
  "string",
  "object",
  "unknown",
};

/*
 * Return the type of a jsval.
 */
static JsValueType GetValueType(jsval val) {
  if(JsRootedValue::isVoid(val)) {
    return JSVAL_TYPE_VOID;
  } else if(JSVAL_IS_NULL(val)) {
    return JSVAL_TYPE_NULL;
  } else if(JSVAL_IS_BOOLEAN(val)) {
    return JSVAL_TYPE_BOOLEAN;
  } else if(JsRootedValue::isNumber(val)) {
    return JSVAL_TYPE_NUMBER;
  } else if(JSVAL_IS_STRING(val)) {
    return JSVAL_TYPE_STRING;
  } else if(JSVAL_IS_OBJECT(val)) {
    return JSVAL_TYPE_OBJECT;
  } else {
    return JSVAL_TYPE_UNKNOWN;
  } 
}

/*
 * Called from JavaScript to call a Java method that has previously been
 * wrapped.  See _setWrappedFunction for use.
 */ 
static JSBool invokeJavaMethod(JSContext *cx, JSObject *obj, uintN argc,
    jsval *argv, jsval *rval)
{
  Tracer tracer("invokeJavaMethod");
  JsRootedValue::ContextManager context(cx);

  // I kid you not; this is how XPConnect gets their function object so they can
  // multiplex dispatch the call from a common site.  See XPCDispObject.cpp(466)
  //
  // I now have a secondary confirmation that this trick is legit.
  // brandon@mozilla.org writes:
  //
  // argv[-2] is part of the JS API, unabstracted.  Just as argv[0] is the
  // first argument (if argc != 0), argv[-1] is the |this| parameter (equal
  // to OBJECT_TO_JSVAL(obj) in a native method with the standard |obj|
  // second formal parameter name), and argv[-2] is the callee object, tagged
  // as a jsval.
  if (JS_TypeOfValue(cx, argv[-2]) != JSTYPE_FUNCTION) {
    tracer.setFail("not a function type");
    return JS_FALSE;
  }
  JSObject* funObj = JSVAL_TO_OBJECT(argv[-2]);

  // Pull the wrapper object out of the funObj's reserved slot
  jsval jsCleanupObj;
  if (!JS_GetReservedSlot(cx, funObj, 0, &jsCleanupObj)) {
    tracer.setFail("JS_GetReservedSlot failed");
    return JS_FALSE;
  }
  JSObject* cleanupObj = JSVAL_TO_OBJECT(jsCleanupObj);
  if (!cleanupObj) {
    tracer.setFail("cleanupObj is null");
    return JS_FALSE;
  }

  // Get DispatchMethod instance out of the wrapper object
  jobject dispMeth =
      reinterpret_cast<jobject>(JS_GetPrivate(cx, cleanupObj));
  if (!dispMeth) {
    tracer.setFail("dispMeth is null");
    return JS_FALSE;
  }
  jclass dispClass = savedJNIEnv->GetObjectClass(dispMeth);
  if (!dispClass || ExceptionCheck(savedJNIEnv)) {
    tracer.setFail("GetObjectClass returns null");
    return JS_FALSE;
  }

  // lookup the invoke method on the dispatch object
  jmethodID invokeID =
      savedJNIEnv->GetMethodID(dispClass, "invoke", INVOKE_SIGNATURE);
  if (!invokeID || ExceptionCheck(savedJNIEnv)) {
    tracer.setFail("GetMethodID failed");
    return JS_FALSE;
  }

  // create an array of integers to hold the JsRootedValue pointers passed
  // to the invoke method
#ifdef WBP_ARCH64
  jlongArray args = savedJNIEnv->NewLongArray(argc);
#else
  jintArray args = savedJNIEnv->NewIntArray(argc);
#endif
  if (!args || ExceptionCheck(savedJNIEnv)) {
    tracer.setFail("NewIntArray failed");
    return JS_FALSE;
  }

  // these arguments are already rooted by the JS interpreter, but we
  // can't easily take advantage of that without complicating the JsRootedValue
  // interface.
  
  // argv[-1] is OBJECT_TO_JSVAL(this)
  JsRootedValue* jsThis = new JsRootedValue(argv[-1]);
  tracer.log("jsthis=%p, RV=%p", argv[-1], jsThis);

  // create JsRootedValues for arguments  
  JsRootedValue *jsArgs[argc]; 
  for (uintN i = 0; i < argc; ++i) {
    jsArgs[i] = new JsRootedValue(argv[i]);
  }
#ifdef WBP_ARCH64
  savedJNIEnv->SetLongArrayRegion(args, 0, argc, reinterpret_cast<jlong*>(jsArgs));
#else
  savedJNIEnv->SetIntArrayRegion(args, 0, argc, reinterpret_cast<jint*>(jsArgs));
#endif
  if (ExceptionCheck(savedJNIEnv)) {
    tracer.setFail("SetIntArrayRegion failed");
    return JS_FALSE;
  }
  
  // slot for return value
  JsRootedValue* jsReturnVal = new JsRootedValue();

  // TODO(jat): small window here where invocation may fail before Java
  // takes ownership of the JsRootedValue objects.  One solution would be
  // to reference-count them between Java and C++ (so the reference count
  // would always be 0, 1, or 2).  Also setField has a similar problem.
  // I plan to fix this when switching away from Java holding pointers to
  // C++ objects as part of the fix for 64-bit support (which we could
  // accomplish inefficiently by changing int to long everywhere, but there
  // are other 64-bit issues to resolve and we need to reduce the number of
  // roots the JS interpreter has to search.
  
  // call Java method
  savedJNIEnv->CallVoidMethod(dispMeth, invokeID, reinterpret_cast<OS_HANDLE>(jsThis),
      args, reinterpret_cast<OS_HANDLE>(jsReturnVal));
  
  JSBool returnValue = JS_TRUE;
  
  if (ExceptionCheck(savedJNIEnv)) {
    tracer.log("dispMeth=%p", dispMeth);
    tracer.setFail("java exception is active:");
    jobject exception = savedJNIEnv->ExceptionOccurred();
    if (exception) {
      fprintf(stderr, "Exception occurred in MethodDispatch.invoke:\n");
      savedJNIEnv->ExceptionDescribe();
      savedJNIEnv->DeleteLocalRef(exception);
    }
    returnValue = JS_FALSE;
  } else if (JS_IsExceptionPending(cx)) {
    tracer.setFail("js exception is active");
    returnValue = JS_FALSE;
  }

  // extract return value
  *rval = jsReturnVal->getValue();

#if 0
  // NOTE: C++ objects are not cleaned up here because Java now owns them.
  // TODO(jat): if reference-counted, they *do* need to be Released here.
  
  // free JsRootedValues
  for (uintN i = 0; i < argc; ++i) {
    delete jsArgs[i];
  }
  delete jsThis;
  delete jsReturnVal;
#endif

  return returnValue;
}

/*
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _createJsRootedValue()
 * Signature: ()I
 */
extern "C" JNIEXPORT JHANDLE JNICALL
OS_NATIVE_JSV(_1createJsRootedValue)
  (JNIEnv* jniEnv, jclass)
{
  Tracer tracer("JsValueMoz._createJsRootedValue");
  JsRootedValue* jsRootedValue = new JsRootedValue(JsRootedValue::JSVAL_VOID);
  return reinterpret_cast<JHANDLE>(jsRootedValue);
}

/*
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _copyJsRootedValue()
 * Signature: (I)I
 */
extern "C" JNIEXPORT JHANDLE JNICALL
OS_NATIVE_JSV(_1copyJsRootedValue)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  const JsRootedValue* jsRootedValue = reinterpret_cast<const JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._copyJsRootedValue", jsRootedValue);
  JsRootedValue* newRootedValue = new JsRootedValue(*jsRootedValue);
  return reinterpret_cast<JHANDLE>(newRootedValue);
}

/*
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _destroyJsRootedValue()
 * Signature: (I)V
 */
extern "C" JNIEXPORT void JNICALL
OS_NATIVE_JSV(_1destroyJsRootedValue)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._destroyJsRootedValue", jsRootedValue);
  delete jsRootedValue;
}

/*
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _getBoolean()
 * Signature: (I)Z
 * 
 * TODO(jat): unboxing Javascript Boolean type?
 */
extern "C" JNIEXPORT jboolean JNICALL
OS_NATIVE_JSV(_1getBoolean)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._getBoolean", jsRootedValue);
  return jsRootedValue->getBoolean();
}

/**
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _getInt()
 * Signature: (I)I
 * 
 * @see com.google.gwt.dev.shell.moz.JsValueMoz#getInt()
 */
extern "C" JNIEXPORT jint JNICALL
OS_NATIVE_JSV(_1getInt)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._getInt", jsRootedValue);
  int val = jsRootedValue->getInt();
  tracer.log("value=%d", val);
  return val;
}

/**
 * Return a Javascript number as a Java double.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _getNumber()
 * Signature: (I)D
 */
extern "C" JNIEXPORT jdouble JNICALL
OS_NATIVE_JSV(_1getNumber)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._getNumber", jsRootedValue);
  return jsRootedValue->getDouble();
}

/**
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _getJsval()
 * Signature: (I)I
 */
extern "C" JNIEXPORT JHANDLE JNICALL
OS_NATIVE_JSV(_1getJsval)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._getObjectPointer", jsRootedValue);
  JHANDLE val = jsRootedValue->getValue();
  tracer.log("value=%d", val);
  return val;
}

/**
 * Return a Javascript string as a Java string.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _getString()
 * Signature: (I)Ljava/lang/String;
 * 
 * Note that this relies on jschar being assignment compatible with jchar
 */
extern "C" JNIEXPORT jstring JNICALL
OS_NATIVE_JSV(_1getString)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._getString", jsRootedValue);
  const JSString* str = jsRootedValue->getString();
  int len = JS_GetStringLength(const_cast<JSString*>(str));
  jstring javaStr =jniEnv->NewString(reinterpret_cast<const jchar*>(
      JS_GetStringChars(const_cast<JSString*>(str))), len);
  return javaStr;
}

/*
 * Returns a human-readable Java string describing the type of a
 * JavaScript object.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _getTypeString
 * Signature: (I)Ljava/lang/String;
 */
extern "C" JNIEXPORT jstring JNICALL
OS_NATIVE_JSV(_1getTypeString)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._getTypeString", jsRootedValue);
  jsval val = jsRootedValue->getValue();
  JSContext* cx = JsRootedValue::currentContext();
  JsValueType valueType = GetValueType(val);
  const char* typeString = 0;
  char buf[256];
  if(valueType == JSVAL_TYPE_OBJECT) {
    JSObject* jsObject = JSVAL_TO_OBJECT(val);
    JSClass* objClass = JS_GET_CLASS(cx, jsObject);
    if (JS_InstanceOf(cx, jsObject,
        &gwt_nativewrapper_class, 0)) {
      typeString = "Java object";
    } else {
      snprintf(buf, sizeof(buf), "class %s", objClass->name);
      typeString = buf;
    }
  } else {
    typeString = JsValueTypeStrings[valueType];
  }
  jstring returnValue = jniEnv->NewStringUTF(typeString);
  return returnValue;
}

/*
 * Unwraps a wrapped Java object from a JS object.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _getWrappedJavaObject
 * Signature: (I)Ljava/lang/Object;
 */
extern "C" JNIEXPORT jobject JNICALL
OS_NATIVE_JSV(_1getWrappedJavaObject)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._getWrappedJavaObject", jsRootedValue);
  JSObject* jsObject = jsRootedValue->getObject();
  if(!jsObject) {
    tracer.throwHostedModeException(jniEnv, "Javascript value not an object");
    return 0;
  }
  JSContext* cx = JsRootedValue::currentContext();
  if(!JS_InstanceOf(cx, jsObject, &gwt_nativewrapper_class, 0)) {
    tracer.throwHostedModeException(jniEnv,
      "Javascript object not a Java object");
    return 0;
  } 
  jobject javaObject
      = reinterpret_cast<jobject>(JS_GetPrivate(cx, jsObject));
  return javaObject;
} 

/*
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _isBoolean()
 * Signature: (I)Z
 */
extern "C" JNIEXPORT jboolean JNICALL
OS_NATIVE_JSV(_1isBoolean)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._isBoolean", jsRootedValue);
  return jsRootedValue->isBoolean();
}

/*
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _isInt()
 * Signature: (I)Z
 */
extern "C" JNIEXPORT jboolean JNICALL
OS_NATIVE_JSV(_1isInt)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._isBoolean", jsRootedValue);
  return jsRootedValue->isInt();
}

/*
 * Checks if a JS object is a JavaScript object.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _isJavaScriptObject
 * Signature: (I)Z
 */
extern "C" JNIEXPORT jboolean JNICALL
OS_NATIVE_JSV(_1isJavaScriptObject)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._isJavaScriptObject", jsRootedValue);
  jsval val = jsRootedValue->getValue();
  bool returnValue = false;
  if(JSVAL_IS_OBJECT(val)) {
    JSObject* jsObject = JSVAL_TO_OBJECT(val);
    returnValue = !JS_InstanceOf(JsRootedValue::currentContext(), jsObject,
        &gwt_nativewrapper_class, 0);
    tracer.log("jsobject=%p, isJSObject=%s", jsObject,
        returnValue ? "true" : "false");
  } else {
    tracer.log("not an object");
  }
  return returnValue;
} 

/*
 * Checks if a JS object is a JavaScript String object.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _isJavaScriptString
 * Signature: (I)Z
 */
extern "C" JNIEXPORT jboolean JNICALL
OS_NATIVE_JSV(_1isJavaScriptString)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._isJavaScriptString", jsRootedValue);
  bool returnValue = jsRootedValue->isJavaScriptStringObject();
  tracer.log("value=%s", returnValue ? "true" : "false");
  return returnValue;
} 

/*
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _isNull()
 * Signature: (I)Z
 */
extern "C" JNIEXPORT jboolean JNICALL
OS_NATIVE_JSV(_1isNull)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._isNull", jsRootedValue);
  return jsRootedValue->isNull();
}

/*
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _isNumber()
 * Signature: (I)Z
 */
extern "C" JNIEXPORT jboolean JNICALL
OS_NATIVE_JSV(_1isNumber)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._isNumber", jsRootedValue);
  return jsRootedValue->isNumber();
}

/*
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _isString()
 * Signature: (I)Z
 * 
 * Handles the case of JavaScript String objects as well
 */
extern "C" JNIEXPORT jboolean JNICALL
OS_NATIVE_JSV(_1isString)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._isString", jsRootedValue);
  return jsRootedValue->isString();
}

/*
 * Checks if a JS object is undefined (void)
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _isUndefined
 * Signature: (I)Z
 */
extern "C" JNIEXPORT jboolean JNICALL
OS_NATIVE_JSV(_1isUndefined)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._isUndefined", jsRootedValue);
  return jsRootedValue->isUndefined();
} 

/*
 * Checks if a JS object is a wrapped Java object.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _isWrappedJavaObject
 * Signature: (I)Z
 */
extern "C" JNIEXPORT jboolean JNICALL
OS_NATIVE_JSV(_1isWrappedJavaObject)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._isWrappedJavaObject", jsRootedValue);
  jsval val = jsRootedValue->getValue();
  bool returnValue = false;
  if(JSVAL_IS_OBJECT(val)) {
    JSObject* jsObject = JSVAL_TO_OBJECT(val);
    returnValue = JS_InstanceOf(JsRootedValue::currentContext(), jsObject,
        &gwt_nativewrapper_class, 0);
    tracer.log("jsobject=%p, wrappedJava=%s", jsObject,
        returnValue ? "true" : "false");
  } else {
    tracer.log("not an object");
  }
  return returnValue;
} 

/*
 * Set the JavaScript value to be a boolean of the supplied value.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _setBoolean()
 * Signature: (IZ)V
 */
extern "C" JNIEXPORT void JNICALL
OS_NATIVE_JSV(_1setBoolean)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt, jboolean val)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._setBoolean", jsRootedValue);
  jsRootedValue->setBoolean(val == JNI_TRUE);
  return;
}

/*
 * Set the JavaScript value to be a double of the supplied value.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _setDouble()
 * Signature: (ID)V
 */
extern "C" JNIEXPORT void JNICALL 
OS_NATIVE_JSV(_1setDouble)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt, jdouble val)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._setDouble", jsRootedValue);
  if(!jsRootedValue->setDouble(val)) {
    tracer.throwHostedModeException(jniEnv, "Unable to allocate JS double");
    return;
  }
}

/*
 * Set the Javascript value to be an integer.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _setInt()
 * Signature: (II)V
 */
extern "C" JNIEXPORT void JNICALL
OS_NATIVE_JSV(_1setInt)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt, jint val)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._setInt", jsRootedValue);
  tracer.log("val=%d", val);
  jsRootedValue->setInt(val);
}

/*
 * Set the Javascript value to be another JsRootedValue's value.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _setJsRootedValue()
 * Signature: (II)V
 */
extern "C" JNIEXPORT void JNICALL
OS_NATIVE_JSV(_1setJsRootedValue)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt, JHANDLE jsOtherRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  JsRootedValue* jsOtherRootedValue = reinterpret_cast<JsRootedValue*>
      (jsOtherRootedValueInt);
  Tracer tracer("JsValueMoz._setJsRootedValue", jsRootedValue);
  jsRootedValue->setValue(jsOtherRootedValue->getValue());
}

/*
 * Set the Javascript value to a specific jsval.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _setJsval()
 * Signature: (II)V
 */
extern "C" JNIEXPORT void JNICALL
OS_NATIVE_JSV(_1setJsval)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt, JHANDLE jsvalue)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._setJsval", jsRootedValue);
  jsRootedValue->setValue(jsvalue);
}

/*
 * Set the JavaScript value to be null.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _setNull()
 * Signature: (I)V
 */
extern "C" JNIEXPORT void JNICALL
OS_NATIVE_JSV(_1setNull)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._setNull", jsRootedValue);
  jsRootedValue->setNull();
}

/*
 * Set the JavaScript value to be a string of the supplied value.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _setString()
 * Signature: (ILjava/lang/String;)V
 */
extern "C" JNIEXPORT void JNICALL
OS_NATIVE_JSV(_1setString)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt, jstring val)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._setString", jsRootedValue);
  JStringWrap strVal(jniEnv, val);
  const jchar* stringUTF16 = strVal.jstr();
  if(!stringUTF16) {
    tracer.throwHostedModeException(jniEnv, "Unable to retrieve Java string");
    return;
  }
  tracer.log("string=%s", strVal.str());
  if(!jsRootedValue->setString(reinterpret_cast<const wchar_t*>(stringUTF16),
      strVal.length())) {
    tracer.throwHostedModeException(jniEnv, "Unable to allocate JS string");
    return;
  }
}

/*
 * Set the JavaScript value to be undefined (void).
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _setUndefined()
 * Signature: (I)V
 */
extern "C" JNIEXPORT void JNICALL
OS_NATIVE_JSV(_1setUndefined)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._setUndefined", jsRootedValue);
  jsRootedValue->setUndefined();
}

/*
 * Wraps a Java object in a JS object.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _setWrappedJavaObject
 * Signature: (ILjava/lang/Object)V
 */
extern "C" JNIEXPORT void JNICALL
OS_NATIVE_JSV(_1setWrappedJavaObject)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt, jobject obj, jclass dispClass)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._setWrappedJavaObject", jsRootedValue);
  JSContext* cx = JsRootedValue::currentContext();
  JSObject* scriptWindow = JS_GetGlobalObject(cx);
  JSObject* newObj = JS_NewObject(cx, &gwt_nativewrapper_class, 0,
      scriptWindow);
  if (!newObj) {
    tracer.throwHostedModeException(jniEnv,
        "Unable to allocate JS object to wrap Java object");
    return;
  }
  // Save in output value so it won't get GCed.
  jsRootedValue->setObject(newObj); 
  tracer.log("jsobject=%p", newObj);
  
  // This is collected when the gwt_nativewrapper_class destructor runs.
  jobject dispObjRef = jniEnv->NewGlobalRef(obj);
  if (!dispObjRef || ExceptionCheck(jniEnv)) {
    tracer.throwHostedModeException(jniEnv,
        "Unable to allocate global reference for JS wrapper");
    return;
  } 
  if (!JS_SetPrivate(cx, newObj, dispObjRef)) {
    jniEnv->DeleteGlobalRef(dispObjRef);
    tracer.throwHostedModeException(jniEnv,
        "Unable to allocate global reference for JS wrapper");
    return;
  } 
  // forcibly setup a "toString" method to override the default
//  jclass dispClass = GetObjectClass(obj);
  if (ExceptionCheck(jniEnv)) {
    tracer.throwHostedModeException(jniEnv, "Can't get object class");
    return;
  } 
  jmethodID getFieldMeth = jniEnv->GetMethodID(dispClass, "getField", GET_FIELD_SIGNATURE);
  if (!getFieldMeth || ExceptionCheck(jniEnv)) {
    tracer.throwHostedModeException(jniEnv, "Can't get getField method");
    return;
  } 
  jstring ident = jniEnv->NewStringUTF("@java.lang.Object::toString()");
  if (!ident || ExceptionCheck(jniEnv)) {
    tracer.throwHostedModeException(jniEnv,
        "Can't create Java string for toString method name");
    return;
  }
  // allocate a new root to hold the result of the getField call
  JsRootedValue* toStringFunc = new JsRootedValue(); 
  jniEnv->CallVoidMethod(obj, getFieldMeth, ident,
      reinterpret_cast<JHANDLE>(toStringFunc));
  if (toStringFunc->isUndefined() || ExceptionCheck(jniEnv)) {
    tracer.throwHostedModeException(jniEnv, "getField(toString) failed");
    return;
  } 
  if (!JS_DefineProperty(cx, newObj, "toString", toStringFunc->getValue(),
      JS_PropertyStub, JS_PropertyStub, JSPROP_READONLY | JSPROP_PERMANENT)) {
    tracer.throwHostedModeException(jniEnv, "Can't define JS toString method");
    return;
  }
} 

/*
 * Wraps a Java function in a JS object.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _setWrappedFunction
 * Signature: (ILjava/lang/String;Lcom/google/gwt/dev/shell/moz/DispatchMethod;)V
 */
extern "C" JNIEXPORT void JNICALL
OS_NATIVE_JSV(_1setWrappedFunction)
    (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt, jstring methodName,
     jobject dispatchMethod)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._setWrappedFunction", jsRootedValue);
  JSContext* cx = JsRootedValue::currentContext();
  JSObject* scriptWindow = JS_GetGlobalObject(cx);
  JStringWrap nameStr(jniEnv, methodName);
  if (!nameStr.str()) {
    tracer.throwHostedModeException(jniEnv,
       "null method name passed to setWrappedFunction");
    return;
  }
  tracer.log("JsRootedValue=%p, method=%s, obj=%p", jsRootedValueInt,
      nameStr.str(), dispatchMethod);
  JSFunction* function = JS_NewFunction(cx, invokeJavaMethod, 0,
      JSFUN_LAMBDA, 0, nameStr.str());
  if (!function) {
    tracer.throwHostedModeException(jniEnv, "JS_NewFunction failed");
    return;
  }
  JSObject* funObj = JS_GetFunctionObject(function);
  if (!funObj) {
    tracer.throwHostedModeException(jniEnv, "JS_GetFunctionObject failed");
    return;
  }
  // Save in output value so it won't get GCed.
  jsRootedValue->setObject(funObj); 

  // Create a cleanup object to hold and clean up dispMeth
  JSObject* cleanupObj = JS_NewObject(cx, &gwt_functionwrapper_class, 0,
      scriptWindow);
  if (!cleanupObj) {
    tracer.throwHostedModeException(jniEnv, "JS_NewObject failed");
    return;
  }
  tracer.log("funObj=%p, cleanupObj=%p", funObj, cleanupObj);
  // Store the cleanup object in funObj's reserved slot; now GC protected.
  if(!JS_SetReservedSlot(cx, funObj, 0, OBJECT_TO_JSVAL(cleanupObj))) {
    tracer.throwHostedModeException(jniEnv, "JS_SetReservedSlot failed");
    return;
  }
  jobject dispMethRef = jniEnv->NewGlobalRef(dispatchMethod);
  if (!dispMethRef || ExceptionCheck(jniEnv)) {
    tracer.throwHostedModeException(jniEnv,
        "NewGlobalRef(dispatchMethod) failed");
    return;
  }
  // Store our global ref in the wrapper object
  if (!JS_SetPrivate(cx, cleanupObj, dispMethRef)) {
    jniEnv->DeleteGlobalRef(dispMethRef);
    tracer.throwHostedModeException(jniEnv, "JS_SetPrivate(cleanupObj) failed");
    return;
  }
}      

/*
 * Returns a JavaScript value as a string.
 * 
 * Class:     com_google_gwt_dev_shell_moz_JsValueMoz
 * Method:    _toString
 * Signature: (I)Ljava/lang/String;
 */
extern "C" JNIEXPORT jstring JNICALL
OS_NATIVE_JSV(_1toString)
  (JNIEnv* jniEnv, jclass, JHANDLE jsRootedValueInt)
{
  JsRootedValue* jsRootedValue = reinterpret_cast<JsRootedValue*>
      (jsRootedValueInt);
  Tracer tracer("JsValueMoz._toString", jsRootedValue);
  jsval val = jsRootedValue->getValue();
  JSContext* cx = JsRootedValue::currentContext();

  // if it is a JavaScript object that has a toString member function
  // call that, otherwise call JS_ValueToString
  if(JSVAL_IS_OBJECT(val)) {
    JSObject* jsObject = JSVAL_TO_OBJECT(val);
    jsval fval;
    jsval rval;
    if (!JS_InstanceOf(cx, jsObject, &gwt_nativewrapper_class, 0)
        && JS_GetProperty(cx, jsObject, "toString", &fval)
        && JS_ValueToFunction(cx, fval)
        && JS_CallFunctionValue(cx, jsObject, fval, 0, 0, &rval)) {
      // all the steps succeeded, so use the result of toString() instead
      // of the value for JS_ValueToString below
      val = rval;
    }
  }
  JSString* str = JS_ValueToString(cx, val);
  if (!str) {
    return 0;
  }
  int len = JS_GetStringLength(str);
  jstring javaStr =jniEnv->NewString(reinterpret_cast<const jchar*>(
      JS_GetStringChars(str)), len);
  return javaStr;
} 
