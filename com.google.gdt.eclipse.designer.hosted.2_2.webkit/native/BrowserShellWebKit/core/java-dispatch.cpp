/*
 * Copyright 2008 Google Inc.
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

#include <string>
#include <sstream>
#include "utils.h"
#include "gwt-jni.h"
#include "java-dispatch.h"
#include "trace.h"
#include "JStringWrap.h"

void add_to_List(JNIEnv *env, jobject jlist, jobject jwhat) {
	static jmethodID addMethod = NULL;
	jclass clazz = env->GetObjectClass(jlist);
	if (addMethod == NULL) {
		addMethod = env->GetMethodID(clazz, "add", "(Ljava/lang/Object;)Z");
	}
	env->CallBooleanMethod(jlist, addMethod, jwhat);
	env->DeleteLocalRef(clazz);
}
const void* get_from_List_unwrapped(JNIEnv *env, jobject jlist, jint idx) {
	static jmethodID getMethod = NULL;
	jclass clazz = env->GetObjectClass(jlist);
	if (getMethod == NULL) {
		getMethod = env->GetMethodID(clazz, "get", "(I)Ljava/lang/Object;");
	}
	jobject jptr = env->CallObjectMethod(jlist, getMethod, idx);
	const void* result = unwrap_pointer(env, jptr);
	env->DeleteLocalRef(jptr);
	env->DeleteLocalRef(clazz);
	return result;
}
jboolean is_List_empty(JNIEnv *env, jobject jlist) {
	static jmethodID isEmptyMethod = NULL;
	jclass clazz = env->GetObjectClass(jlist);
	if (isEmptyMethod == NULL) {
		isEmptyMethod = env->GetMethodID(clazz, "isEmpty", "()Z");
	}
	jboolean result = env->CallBooleanMethod(jlist, isEmptyMethod);
	env->DeleteLocalRef(clazz);
	return result;
}
jobject new_List(JNIEnv* env, jint size) {
  // prepare list class
  jclass listClass = env->FindClass("java/util/ArrayList");
  static jmethodID listCtor = NULL;
  if (listCtor == NULL) {
    listCtor = env->GetMethodID(listClass, "<init>", "(I)V");
  }
  jobject jlist = env->NewObject(listClass, listCtor, size);
  env->DeleteLocalRef(listClass);
  return jlist;
}

namespace gwt {

  /*
   * Declarations for private functions.
   */
  JSClassRef DispatchObjectClassCreate();

  JSClassRef DispatchMethodClassCreate();

  JSValueRef DispatchObjectGetProperty(JSContextRef, JSObjectRef, JSStringRef,
                                       JSValueRef*);

  JSValueRef DispatchObjectToString(JSContextRef, JSObjectRef, JSObjectRef,
                                    size_t, const JSValueRef*, JSValueRef*);

  bool DispatchObjectSetProperty(JSContextRef, JSObjectRef, JSStringRef,
                                 JSValueRef, JSValueRef*);

  void DispatchObjectFinalize(JSObjectRef);

  JSValueRef DispatchMethodCallAsFunction(JSContextRef, JSObjectRef,
                                          JSObjectRef, size_t,
                                          const JSValueRef*, JSValueRef*);

  JSValueRef DispatchMethodGetToString(JSContextRef, JSObjectRef, JSStringRef,
                                       JSValueRef*);

  JSValueRef DispatchMethodToString(JSContextRef, JSObjectRef, JSObjectRef,
                                    size_t, const JSValueRef*, JSValueRef*);

  void DispatchMethodFinalize(JSObjectRef);

  /*
   * Call this when an underlying Java Object should be freed.
   */
  void ReleaseJavaObject(jobject jObject);
  void ReleaseJavaObject(jobject jObject, bool notifyJava);


  /*
   * The class definition stuct for DispatchObjects.
   */
  static JSClassDefinition _dispatchObjectClassDef = { 0,
      kJSClassAttributeNone, "DispatchObject", 0, 0, 0, 0,
      DispatchObjectFinalize, 0, DispatchObjectGetProperty,
      DispatchObjectSetProperty, 0, 0, 0, 0, 0, 0 };

  /*
   * The class definition structs for DispatchMethods.
   */
  static JSStaticValue _dispatchMethodStaticValues[] = {
    { "toString", DispatchMethodGetToString, 0, kJSPropertyAttributeNone },
    { 0, 0, 0, 0 }
  };
  static JSClassDefinition _dispatchMethodClassDef = { 0,
      kJSClassAttributeNoAutomaticPrototype, "DispatchMethod", 0,
      _dispatchMethodStaticValues, 0, 0, DispatchMethodFinalize, 0, 0, 0, 0,
      0, DispatchMethodCallAsFunction, 0, 0, 0 };

  /*
   * The classes used to create DispatchObjects and DispatchMethods.
   */
  static JSClassRef _dispatchObjectClass = DispatchObjectClassCreate();
  static JSClassRef _dispatchMethodClass = DispatchMethodClassCreate();

  /*
   * Java class and method references needed to do delegation.
   */
  
  /*
   * The main JVM, used by foreign threads to attach.
   */
  static JavaVM* _javaVM;

  /*
   * Only valid for the main thread!  WebKit can finalized on a foreign thread.
   */
  static JNIEnv* _javaEnv;

  static jclass _javaDispatchObjectClass;
  static jclass _javaDispatchMethodClass;
  static jclass _lowLevelWebKitClass;
  static jmethodID _javaDispatchObjectSetFieldMethod;
  static jmethodID _javaDispatchObjectGetFieldMethod;
  static jmethodID _javaDispatchMethodInvokeMethod;
  static jmethodID _javaDispatchObjectToStringMethod;
  static jmethodID _lowLevelWebKitRegisterWrapper;
  static jmethodID _lowLevelWebKitReleaseWrapper;

  /*
   * Structure to hold DispatchMethod private data.
   *
   * NOTE: utf8Name is defensively copied.
   */
  class DispatchMethodData {
   public:
    DispatchMethodData(jobject jObject, std::string& utf8Name)
        : _jObject(jObject), _utf8Name(utf8Name) { }
    ~DispatchMethodData() {
      ReleaseJavaObject(_jObject);
    }
    jobject _jObject;
    std::string _utf8Name;
  };

/*
 * The following takes the prototype from the Function constructor, this allows
 * us to easily support call and apply on our objects that support CallAsFunction.
 *
 * NOTE: The return value is not protected.
 */
JSValueRef GetFunctionPrototype(JSContextRef jsContext, JSValueRef* exception) {
  TR_ENTER();
  JSObjectRef globalObject = JSContextGetGlobalObject(jsContext);
  JSStringRef fnPropName= JSStringCreateWithUTF8CString("Function");
  JSValueRef fnCtorValue = JSObjectGetProperty(jsContext, globalObject,
      fnPropName, exception);
  JSStringRelease(fnPropName);
  if (!fnCtorValue) {
    return JSValueMakeUndefined(jsContext);
  }

  JSObjectRef fnCtorObject = JSValueToObject(jsContext, fnCtorValue, exception);
  if (!fnCtorObject) {
    return JSValueMakeUndefined(jsContext);
  }

  JSStringRef protoPropName = JSStringCreateWithUTF8CString("prototype");
  JSValueRef fnPrototype = JSObjectGetProperty(jsContext, fnCtorObject,
      protoPropName, exception);
  JSStringRelease(protoPropName);
  if (!fnPrototype) {
    return JSValueMakeUndefined(jsContext);
  }

  TR_LEAVE();
  return fnPrototype;
}

/*
 *
 */
JSClassRef GetDispatchObjectClass() {
  TR_ENTER();
  TR_LEAVE();
  return _dispatchObjectClass;
}

/*
 *
 */
JSClassRef GetDispatchMethodClass() {
  TR_ENTER();
  TR_LEAVE();
  return _dispatchMethodClass;
}

/*
 *
 */
JSClassRef DispatchObjectClassCreate() {
  TR_ENTER();
  JSClassRef dispClass = JSClassCreate(&_dispatchObjectClassDef);
  JSClassRetain(dispClass);
  TR_LEAVE();
  return dispClass;
}

/*
 *
 */
JSClassRef DispatchMethodClassCreate() {
  TR_ENTER();
  JSClassRef dispClass = JSClassCreate(&_dispatchMethodClassDef);
  JSClassRetain(dispClass);
  TR_LEAVE();
  return dispClass;
}

/*
 * NOTE: The object returned from this function is not protected.
 */
JSObjectRef DispatchObjectCreate(JSContextRef jsContext, jobject jObject) {
  TR_ENTER();
  
  jobject jNewObjectRef = _javaEnv->NewGlobalRef(jObject);
  JSObjectRef dispInst = JSObjectMake(jsContext, _dispatchObjectClass, jNewObjectRef);
  // register wrapper
  _javaEnv->CallStaticVoidMethod(_lowLevelWebKitClass, _lowLevelWebKitRegisterWrapper, jNewObjectRef);
  if (_javaEnv->ExceptionCheck()) {
	  _javaEnv->ExceptionClear();
  }

  TR_LEAVE();
  return dispInst;
}

/*
 * NOTE: The object returned from this function is not protected.
 */
JSObjectRef DispatchMethodCreate(JSContextRef jsContext, std::string& name,
    jobject jObject) {
  TR_ENTER();
 
  JSObjectRef dispInst = JSObjectMake(jsContext, _dispatchMethodClass,
      new DispatchMethodData(_javaEnv->NewGlobalRef(jObject), name));

  // This could only be cached relative to jsContext.
  JSValueRef fnProtoValue = GetFunctionPrototype(jsContext, NULL);
  JSObjectSetPrototype(jsContext, dispInst, fnProtoValue);
  TR_LEAVE();
  return dispInst;
}

/*
 * NOTE: The value returned from this function is not protected, but all
 * JSValues that are passed into Java are protected before the invocation.
 */
JSValueRef DispatchObjectGetProperty(JSContextRef jsContext,
    JSObjectRef jsObject, JSStringRef jsPropertyName,
    JSValueRef* jsException) {
  TR_ENTER();

  // If you call toString on a DispatchObject, you should get the results
  // of the java object's toString invcation.
  if (JSStringIsEqualToUTF8CString(jsPropertyName, "toString")) {
    JSObjectRef jsFunction = JSObjectMakeFunctionWithCallback(jsContext,
        jsPropertyName, DispatchObjectToString);
    return jsFunction;
  }

  // The class check is omitted because it should not be possible to tear off
  // a getter.
  jobject jObject = reinterpret_cast<jobject>(JSObjectGetPrivate(jsObject));

  jstring jPropertyName = _javaEnv->NewString(
#ifdef _WIN32
      reinterpret_cast
#else
      static_cast
#endif
      <const jchar*>(JSStringGetCharactersPtr(jsPropertyName)),
      static_cast<jsize>(JSStringGetLength(jsPropertyName)));
  if (!jObject || !jPropertyName || _javaEnv->ExceptionCheck()) {
    TR_FAIL();
    _javaEnv->ExceptionClear();
    return JSValueMakeUndefined(jsContext);
  }

  jobject jContext = wrap_pointer(_javaEnv, jsContext);
  jobject jResult = _javaEnv->CallObjectMethod(jObject, _javaDispatchObjectGetFieldMethod, jContext, jPropertyName);
  JSValueRef jsResult = reinterpret_cast<JSValueRef>(unwrap_pointer(_javaEnv, jResult));
  _javaEnv->DeleteLocalRef(jResult);
  _javaEnv->DeleteLocalRef(jContext);

  if (!jsResult || _javaEnv->ExceptionCheck()) {
    TR_FAIL();
    _javaEnv->ExceptionClear();
    return JSValueMakeUndefined(jsContext);
  }

  // Java left us an extra reference to eat.
  JSValueUnprotectChecked(jsContext, jsResult);
  TR_LEAVE();
  return jsResult;
}

/*
 *
 */
bool DispatchObjectSetProperty(JSContextRef jsContext, JSObjectRef jsObject,
    JSStringRef jsPropertyName, JSValueRef jsValue, JSValueRef* jsException) {
  TR_ENTER();

  // The class check is omitted because it should not be possible to tear off
  // a getter.
  jobject jObject = reinterpret_cast<jobject>(JSObjectGetPrivate(jsObject));

  jstring jPropertyName = _javaEnv->NewString(
#ifdef _WIN32
      reinterpret_cast
#else
      static_cast
#endif
      <const jchar*>(JSStringGetCharactersPtr(jsPropertyName)),
      static_cast<jsize>(JSStringGetLength(jsPropertyName)));
  if (!jObject || !jPropertyName || _javaEnv->ExceptionCheck()) {
    _javaEnv->ExceptionClear();
    return false;
  }

  JSValueProtectChecked(jsContext, jsValue);

  jobject jContext = wrap_pointer(_javaEnv, jsContext);
  jobject jValue = wrap_pointer(_javaEnv, jsValue);
  _javaEnv->CallVoidMethod(jObject, _javaDispatchObjectSetFieldMethod, jContext, jPropertyName, jValue);
  _javaEnv->DeleteLocalRef(jValue);
  _javaEnv->DeleteLocalRef(jContext);

  if (_javaEnv->ExceptionCheck()) {
    _javaEnv->ExceptionClear();
    return false;
  }

  TR_LEAVE();
  return true;
}

/*
 *
 */
void DispatchObjectFinalize(JSObjectRef jsObject) {
  TR_ENTER();
  jobject jObject = reinterpret_cast<jobject>(JSObjectGetPrivate(jsObject));
  ReleaseJavaObject(jObject, true);
  TR_LEAVE();
}

/*
 *
 */
void DispatchMethodFinalize(JSObjectRef jsObject) {
  TR_ENTER();
  DispatchMethodData* data = reinterpret_cast<DispatchMethodData*>(
      JSObjectGetPrivate(jsObject));
  delete data;
  TR_LEAVE();
}

/*
 * NOTE: The value returned from this function is not protected.
 */
JSValueRef DispatchObjectToString(JSContextRef jsContext, JSObjectRef,
    JSObjectRef jsThis, size_t, const JSValueRef*, JSValueRef*) {
  TR_ENTER();

  // This function cannot be torn off and applied to any JSValue. If this does
  // not reference a DispatchObject, return undefined.
  if (!JSValueIsObjectOfClass(jsContext, jsThis, GetDispatchObjectClass())) {
    return JSValueMakeUndefined(jsContext);
  }

  jobject jObject = reinterpret_cast<jobject>(JSObjectGetPrivate(jsThis));
  jstring jResult = reinterpret_cast<jstring>(
      _javaEnv->CallObjectMethod(jObject, _javaDispatchObjectToStringMethod));
  if (_javaEnv->ExceptionCheck()) {
    return JSValueMakeUndefined(jsContext);
  } else if (!jResult) {
    return JSValueMakeNull(jsContext);
  } else {
    JStringWrap result(_javaEnv, jResult);
    JSStringRef resultString = JSStringCreateWithCharacters(
#ifdef _WIN32
      reinterpret_cast
#else
      static_cast
#endif
      <const JSChar*>(result.jstr()),
      static_cast<size_t>(result.length()));
    JSValueRef jsResultString = JSValueMakeString(jsContext, resultString);
    JSStringRelease(resultString);
    return jsResultString;
  }
  TR_LEAVE();
}

/*
 *
 */
JSValueRef DispatchMethodCallAsFunction(JSContextRef jsContext,
    JSObjectRef jsFunction, JSObjectRef jsThis, size_t argumentCount,
    const JSValueRef arguments[], JSValueRef* exception) {
  TR_ENTER();

  // We don't need to check the class here because we take the private
  // data from jsFunction and not jsThis.
  DispatchMethodData* data = reinterpret_cast<DispatchMethodData*>(
      JSObjectGetPrivate(jsFunction));

  if (data == NULL) {
    return JSValueMakeUndefined(jsContext);
  }

  jobject jObject = data->_jObject;
 
  jobject jArgumentsList = new_List(_javaEnv, argumentCount);
  if (!jArgumentsList || _javaEnv->ExceptionCheck()) {
    return JSValueMakeUndefined(jsContext);
  }

  // This single element int array will be passed into the java call to allow the
  // called java method to raise an exception. We will check for a non-null value
  // after the call is dispatched.
  jobject jExceptionList = new_List(_javaEnv, 1);
  if (!jExceptionList || _javaEnv->ExceptionCheck()) {
    return JSValueMakeUndefined(jsContext);
  }

  for (size_t i = 0; i < argumentCount; ++i) {
    JSValueRef arg = arguments[i];
    // Java will take ownership of the arguments.
    JSValueProtectChecked(jsContext, arg);
    add_to_List(_javaEnv, jArgumentsList, wrap_pointer(_javaEnv, arg));
    if (_javaEnv->ExceptionCheck()) {
      return JSValueMakeUndefined(jsContext);
    }
  }

  // Java will take ownership of this.
  JSValueProtectChecked(jsContext, jsThis);

  jobject jContext = wrap_pointer(_javaEnv, jsContext);
  jobject jThis = wrap_pointer(_javaEnv, jsThis);
  jobject jResult = _javaEnv->CallObjectMethod(jObject, _javaDispatchMethodInvokeMethod, jContext, jThis, jArgumentsList, jExceptionList);
  JSValueRef jsResult = reinterpret_cast<JSValueRef>(unwrap_pointer(_javaEnv, jResult));
  _javaEnv->DeleteLocalRef(jResult);
  _javaEnv->DeleteLocalRef(jThis);
  _javaEnv->DeleteLocalRef(jContext);

  if (_javaEnv->ExceptionCheck()) {
    return JSValueMakeUndefined(jsContext);
  }


  JSValueRef jsException = NULL;
  if (is_List_empty(_javaEnv, jExceptionList) == JNI_FALSE) {
  	jsException = reinterpret_cast<const JSValueRef>(get_from_List_unwrapped(_javaEnv, jExceptionList, 0));
  }
  if (!_javaEnv->ExceptionCheck() && jsException) {
    // If the java dispatch set an exception, then we pass it back to our caller.
	if (exception) {
      *exception = jsException;
	}
    // Java left us an extra reference to eat.
    JSValueUnprotectChecked(jsContext, jsException);
  }
  _javaEnv->DeleteLocalRef(jArgumentsList);
  _javaEnv->DeleteLocalRef(jExceptionList);

  // Java left us an extra reference to eat.
  JSValueUnprotectChecked(jsContext, jsResult);
  TR_LEAVE();
  return jsResult;
}

/*
 * NOTE: The object returned from this function is not protected.
 */
JSValueRef DispatchMethodToString(JSContextRef jsContext, JSObjectRef,
    JSObjectRef thisObject, size_t, const JSValueRef*, JSValueRef*) {
  TR_ENTER();
  
  // This function cannot be torn off and applied to any JSValue. If this does
  // not reference a DispatchMethod, return undefined.
  if (!JSValueIsObjectOfClass(jsContext, thisObject, GetDispatchMethodClass())) {
    return JSValueMakeUndefined(jsContext);
  }

  std::ostringstream ss;
  DispatchMethodData* data = reinterpret_cast<DispatchMethodData*>(
      JSObjectGetPrivate(thisObject));
  ss << "function " << data->_utf8Name << "() {\n    [native code]\n}\n";
  JSStringRef stringRep = JSStringCreateWithUTF8CString(ss.str().c_str());
  JSValueRef jsStringRep = JSValueMakeString(jsContext, stringRep);
  JSStringRelease(stringRep);
  TR_LEAVE();
  return jsStringRep;
}

/*
 * NOTE: The object returned from this function is not protected.
 */
JSValueRef DispatchMethodGetToString(JSContextRef jsContext,
    JSObjectRef jsObject, JSStringRef jsPropertyName, JSValueRef* jsException) {
  TR_ENTER();
  JSObjectRef toStringFn = JSObjectMakeFunctionWithCallback(jsContext,
      jsPropertyName, DispatchMethodToString);
  TR_LEAVE();
  return toStringFn;
}

/*
 *
 */
bool Initialize(JNIEnv* javaEnv, jclass javaDispatchObjectClass,
    jclass javaDispatchMethodClass, jclass lowLevelWebKitClass) {
  TR_ENTER();
  if (!javaEnv || !javaDispatchObjectClass || !javaDispatchMethodClass
      || !lowLevelWebKitClass) {
    return false;
  }

  _javaVM = 0;
  javaEnv->GetJavaVM(&_javaVM);

  _javaEnv = javaEnv;
  _javaDispatchObjectClass = static_cast<jclass>(
      javaEnv->NewGlobalRef(javaDispatchObjectClass));
  _javaDispatchMethodClass = static_cast<jclass>(
      javaEnv->NewGlobalRef(javaDispatchMethodClass));
  _lowLevelWebKitClass = static_cast<jclass>(
      javaEnv->NewGlobalRef(lowLevelWebKitClass));
  _javaDispatchObjectSetFieldMethod = javaEnv->GetMethodID(
      javaDispatchObjectClass, "setField", "(Ljava/lang/Number;Ljava/lang/String;Ljava/lang/Number;)V");
  _javaDispatchObjectGetFieldMethod = javaEnv->GetMethodID(
      javaDispatchObjectClass, "getField", "(Ljava/lang/Number;Ljava/lang/String;)Ljava/lang/Number;");
  _javaDispatchMethodInvokeMethod = javaEnv->GetMethodID(
      javaDispatchMethodClass, "invoke", "(Ljava/lang/Number;Ljava/lang/Number;Ljava/util/List;Ljava/util/List;)Ljava/lang/Number;");
  _javaDispatchObjectToStringMethod = javaEnv->GetMethodID(
      javaDispatchObjectClass, "toString", "()Ljava/lang/String;");
  _lowLevelWebKitRegisterWrapper = javaEnv->GetStaticMethodID(
      lowLevelWebKitClass, "registerWrapper", "(Lcom/google/gdt/eclipse/designer/webkit/jsni/LowLevelWebKit$DispatchObject;)V");
  _lowLevelWebKitReleaseWrapper = javaEnv->GetStaticMethodID(
      lowLevelWebKitClass, "releaseWrapper", "(Lcom/google/gdt/eclipse/designer/webkit/jsni/LowLevelWebKit$DispatchObject;)V");

  if (!_javaVM
      || !_javaDispatchObjectSetFieldMethod || !_javaDispatchObjectGetFieldMethod
      || !_javaDispatchMethodInvokeMethod || !_javaDispatchObjectToStringMethod
	  || !_lowLevelWebKitRegisterWrapper || !_lowLevelWebKitReleaseWrapper || javaEnv->ExceptionCheck()) {
    return false;
  }

  TR_LEAVE();
  return true;
}

void ReleaseJavaObject(jobject jObject) {
	ReleaseJavaObject(jObject, false);
}
void ReleaseJavaObject(jobject jObject, bool notifyJava) {
  // Tricky: this call may be on a foreign thread.
  JNIEnv* javaEnv = 0;
  if ((_javaVM->AttachCurrentThreadAsDaemon(reinterpret_cast<void**>(&javaEnv),
      NULL) < 0) || !javaEnv) {
    TR_FAIL();
    return;
  }
  if (notifyJava) {
	// Tell the Java code we're done with this object.
    javaEnv->CallStaticVoidMethod(_lowLevelWebKitClass, _lowLevelWebKitReleaseWrapper, jObject);
    if (javaEnv->ExceptionCheck()) {
      javaEnv->ExceptionClear();
    }
  }
  javaEnv->DeleteGlobalRef(jObject);
}

} // namespace gwt
