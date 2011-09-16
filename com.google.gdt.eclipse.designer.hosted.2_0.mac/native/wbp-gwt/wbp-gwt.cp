/*
 *  BrowserShell.c
 *  CarbonWeb
 *
 *  Created by Alexander Mitin on 21.05.07.
 *  Copyright 2007 Instantiations, Inc. All rights reserved.
 *
 */
#include <jni.h>
#include "wbp-gwt.h"
#include "JStringWrapper.h"

#import "DispatchObjectWrapper.h"

JNIEnv* gEnv;
JavaVM* gJavaVM;
jclass c_DispatchObject;
jclass c_DispatchMethod;
jclass c_JsValueSaf;

jmethodID m_DispatchObject_getWrappedMethod;
jmethodID m_DispatchObject_getField; 
jmethodID m_DispatchObject_getFields;
jmethodID m_DispatchObject_setField;
jmethodID m_DispatchObject_toString;
jmethodID m_DispatchMethod_invoke;
jmethodID m_JsValueSaf_getDispatchObjectRef;
jmethodID m_JsValueSaf_putDispatchObjectRef;
jmethodID m_JsValueSaf_removeDispatchObjectRef;

static BOOL isNull(id object) {
	return [object isMemberOfClass: [NSNull class]];
}
static BOOL isUndefined(id object) {
	return [object isMemberOfClass: [WebUndefined class]];
}
static BOOL isNumber(id object) {
	return [object isKindOfClass:[NSNumber class]];
}

jstring createJavaString(JNIEnv* env, NSString* nsString) {
	unichar *buffer = (unichar*)malloc([nsString length] * sizeof(unichar));
	[nsString getCharacters:buffer];
	jstring result = env->NewString((const jchar*)buffer, [nsString length]);
	free(buffer);
	return result;
}

jlong convertPointer(void *ptr) {
	jlong result;
	memset(&result, 0, sizeof(jlong));
	memcpy(&result, &ptr, sizeof(void*));
	return result;
}


extern "C" { 
/////////////////////////////////////////////////////////////////////////////////////
//
// GWT API
//
/////////////////////////////////////////////////////////////////////////////////////
JNIEXPORT jboolean JNICALL OS_NATIVE(_1isNull)
	(JNIEnv *env, jclass, jlong jsval)
{
	if (jsval && isNull((id)jsval)) {
		return JNI_TRUE;
	}
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1isUndefined)
	(JNIEnv *env, jclass, jlong jsval)
{
	if (jsval && isUndefined((id)jsval)) {
		return JNI_TRUE;
	}
	return JNI_FALSE;
}

JNIEXPORT jlong JNICALL OS_NATIVE(_1jsNull)
	(JNIEnv *, jclass)
{
	return convertPointer([NSNull null]);
}

JNIEXPORT jlong JNICALL OS_NATIVE(_1jsUndefined)
	(JNIEnv *env, jclass)
{
	return convertPointer([WebUndefined undefined]);
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1coerceToBoolean)
	(JNIEnv * env, jclass, jlong jsval, jbooleanArray rval)
{
	id objectValue = (id)jsval;
	if (objectValue) {
		jboolean result = JNI_FALSE;
		if ([objectValue isKindOfClass:[NSNumber class]]) {
			NSNumber *number = (NSNumber*) objectValue;
			double value = [number doubleValue];
			result = (value < 0.0 || value > 0.0) ? JNI_TRUE : JNI_FALSE; // 0 and NaN means "false"
		} else if ([objectValue isKindOfClass:[NSString class]]) {
			NSString *str = (NSString *) objectValue;
			result = [str length] > 0 ? JNI_TRUE : JNI_FALSE;
		} else {
			result = ([objectValue respondsToSelector:@selector(invokeUndefinedMethodFromWebScript:withArguments:)]) ? JNI_TRUE : JNI_FALSE;
		}
		env->SetBooleanArrayRegion(rval, 0, 1, &result);
		if (env->ExceptionCheck()) {
			return JNI_FALSE;
		}
		return JNI_TRUE;
	}
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1coerceToDouble)
	(JNIEnv *env, jclass, jlong jsval, jdoubleArray rval)
{
	id objectValue = (id)jsval;
	if (objectValue) {
		jdouble result = 0;
		if ([objectValue isKindOfClass:[NSNumber class]]) {
			NSNumber *number = (NSNumber*) objectValue;
			result = (jdouble) [number doubleValue];
		} else if ([objectValue isKindOfClass:[NSString class]]) {
			NSString *str = (NSString *) objectValue;
			result = (jdouble) [str doubleValue];
		} 
		env->SetDoubleArrayRegion(rval, 0, 1, &result);
		if (env->ExceptionCheck()) {
			return JNI_FALSE;
		}
		return JNI_TRUE;
	}
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1coerceToString)
	(JNIEnv *env, jclass, jlong jsval, jobjectArray rval)
{
	id objectValue = (id)jsval;
	if (objectValue) { 
		/* 
		 * Convert all objects to their string representation, EXCEPT
		 * null and undefined which will be returned as a true NULL.
		 */
		jstring result = NULL;
		NSString *nsResult = NULL; 
		if (!isNull(objectValue) && !isUndefined(objectValue)) {
			if ([objectValue isKindOfClass:[NSNumber class]]) {
				NSNumber *number = (NSNumber*) objectValue;
				nsResult = [number stringValue];
			} else if ([objectValue isKindOfClass:[NSString class]]) {
				nsResult = (NSString *) objectValue;
			} else if ([objectValue isKindOfClass:[WebScriptObject class]]) {
				nsResult = [(WebScriptObject*)objectValue stringRepresentation];
			} else {
				nsResult = [NSString stringWithFormat:@"[ %s object ]", object_getClassName(objectValue)];
			}
			// convert into java unicode string
			result = createJavaString(env, nsResult);
			if (env->ExceptionCheck()) {
				return JNI_FALSE;
			}
		}
		env->SetObjectArrayElement(rval, 0, result);
		if (env->ExceptionCheck()) {
			return JNI_FALSE;
		}
		return JNI_TRUE;
	}
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1convertBoolean)
	(JNIEnv *env, jclass, jboolean jval, jlongArray rval)
{
	// to be more compartible: using macros
	NSNumber *result = [[NSNumber alloc] initWithBool:(jval == JNI_TRUE ? YES : NO)];
/*NSString *message = [NSString stringWithFormat:@"mine:     %p class: %s", result, object_getClassName(result)];
TRACE_ERR([message UTF8String]);*/	
    if (result) {
		jlong value = convertPointer(result);
		env->SetLongArrayRegion(rval, 0, 1, &value);
		if (env->ExceptionCheck()) {
			return JNI_FALSE;
		}
	}
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1convertDouble)
	(JNIEnv *env, jclass, jdouble jval, jlongArray rval)
{
	NSNumber *result = [[NSNumber alloc] initWithDouble:(double)jval];
/*NSString *message = [NSString stringWithFormat:@"mine:     %p class: %s", result, object_getClassName(result)];
TRACE_ERR([message UTF8String]);*/	
    if (result) {
		jlong value = convertPointer(result);
		env->SetLongArrayRegion(rval, 0, 1, &value);
		if (env->ExceptionCheck()) {
			return JNI_FALSE;
		}
	}
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1convertString)
	(JNIEnv *env, jclass, jstring jval, jlongArray rval)
{
	JStringWrapper str(env, jval);
	NSString *result = [[NSString alloc] initWithUTF8String:str.c_str()];
/*NSString *message = [NSString stringWithFormat:@"mine:     %p class: %s", result, object_getClassName(result)];
TRACE_ERR([message UTF8String]);*/
    if (result) {
		jlong value = convertPointer(result);
		env->SetLongArrayRegion(rval, 0, 1, &value);
		if (env->ExceptionCheck()) {
			return JNI_FALSE;
		}
	}
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1executeScript)
	(JNIEnv* env, jclass, jlong jwnd, jstring jcode)
{
	WebScriptObject* window = reinterpret_cast<WebScriptObject*>(jwnd);
	JStringWrapper code(env, jcode);
	id result = [window evaluateWebScript:code.ns_str()];
	if ([result isKindOfClass:[WebUndefined class]]) {
		return JNI_FALSE;
	}
/*	DEBUG
	char* toString;
	if ([result respondsToSelector:@selector(stringRepresentation)]) {
		toString = const_cast<char*>([[result stringRepresentation] UTF8String]);
	} else {
		toString  = "[ undefined/unknown ]";
	}
NSString *message = [NSString stringWithFormat:@"ScriptObject: %p eval result: %s: [ %s ]", window, object_getClassName(result), toString];
TRACE_ERR([message UTF8String]);*/
	return JNI_TRUE;
}

JNIEXPORT void JNICALL OS_NATIVE(_1objcRetain)
	(JNIEnv *, jclass, jlong jsval)
{
	if (jsval) {
		id objectValue = (id) jsval;
/*NSString *message = [NSString stringWithFormat:@"gcLock:   %p class: %s", objectValue, object_getClassName(objectValue)];
TRACE_ERR([message UTF8String]);*/
		[objectValue retain];
	}
}

JNIEXPORT void JNICALL OS_NATIVE(_1objcRelease)
	(JNIEnv *, jclass, jlong jsval)
{
	if (jsval) {
		id objectValue = (id) jsval;
/*NSString *message = [NSString stringWithFormat:@"gcUnlock: %p class: %s", objectValue, object_getClassName(objectValue)];
TRACE_ERR([message UTF8String]);*/
		[objectValue release];
	}
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1init)
	(JNIEnv* env, jclass llClass, jclass dispObjCls, jclass dispMethCls, jclass jsValueSafCls)
{
	gEnv = env;
	env->GetJavaVM(&gJavaVM);
	//
	c_DispatchObject = static_cast<jclass>(env->NewGlobalRef(dispObjCls));
	c_DispatchMethod = static_cast<jclass>(env->NewGlobalRef(dispMethCls));
	c_JsValueSaf = static_cast<jclass>(env->NewGlobalRef(jsValueSafCls));
	if (!gJavaVM || !c_DispatchObject || !c_DispatchMethod || !c_JsValueSaf || env->ExceptionCheck()) {
		return JNI_FALSE;
	}

	m_DispatchObject_getField = env->GetMethodID(c_DispatchObject, "getField", "(Ljava/lang/String;)J");
	m_DispatchObject_setField = env->GetMethodID(c_DispatchObject, "setField", "(Ljava/lang/String;J)V");
	m_DispatchObject_toString = env->GetMethodID(c_DispatchObject, "toString", "()Ljava/lang/String;");
	m_DispatchObject_getFields = env->GetMethodID(c_DispatchObject, "getFields", "()[Ljava/lang/String;");
	m_DispatchObject_getWrappedMethod = env->GetMethodID(c_DispatchObject, "getWrappedMethod", "(Ljava/lang/String;)Ljava/lang/Object;");
	m_DispatchMethod_invoke = env->GetMethodID(c_DispatchMethod, "invoke", "(J[J)J");
	m_JsValueSaf_getDispatchObjectRef = env->GetStaticMethodID(c_JsValueSaf, "getDispatchObjectRef", "(J)Ljava/lang/Object;");
	m_JsValueSaf_putDispatchObjectRef = env->GetStaticMethodID(c_JsValueSaf, "putDispatchObjectRef", "(JLjava/lang/Object;)V");
	m_JsValueSaf_removeDispatchObjectRef = env->GetStaticMethodID(c_JsValueSaf, "removeDispatchObjectRef", "(J)V");
	if (!m_DispatchObject_getField || 
		!m_DispatchObject_setField ||
		!m_DispatchObject_getFields || 
		!m_DispatchObject_toString || 
		!m_DispatchMethod_invoke ||
		!m_JsValueSaf_getDispatchObjectRef ||
		!m_JsValueSaf_putDispatchObjectRef ||
		!m_JsValueSaf_removeDispatchObjectRef ||
		env->ExceptionCheck()) {
		return JNI_FALSE;
	}

#ifdef FILETRACE
    gout = fopen("BrowserShell.log", "w");
    filetrace("LOG STARTED");
#endif //FILETRACE

#if defined(JAVATRACE) || defined(JAVATRACE_ERR)
    m_BrowserShell_trace = env->GetStaticMethodID(c_BrowserShell, "trace", "(Ljava/lang/String;)V");
    if (!m_BrowserShell_trace || env->ExceptionCheck()) {
        return JNI_FALSE;
	}
#endif //JAVATRACE
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1invoke)
	(JNIEnv* env, jclass, jlong jwnd, jstring jmethodName, jlong jsthis, jint argc, jlongArray argv, jlongArray rval)
{
	// prepare args: copy 'this' as first arg
	NSMutableArray* args = [[NSMutableArray alloc] init];
	if (jsthis == 0 || isNull((id)jsthis)) {
		[args addObject:[NSNull null]];
	} else {
		NSObject *thisObject = (NSObject *)jsthis;
		[args addObject:thisObject];
	}
	for (int i = 0; i < argc; ++i) {
		jlong argi;
		env->GetLongArrayRegion(argv, i, 1, &argi);
		if (env->ExceptionCheck()) {
			return JNI_FALSE;
		}
		if (argi) {
			[args addObject:(id)argi];
		} else {
			[args addObject:[NSNull null]];
		}
	}
	//[[windowObject valueForKey:@"myFunction"] callWebScriptMethod:@"call" withArguments:[NSArray arrayWithObjects:myThisObjectWrapper, arg1, arg2, nil]];
	//
	WebScriptObject* scriptObject = reinterpret_cast<WebScriptObject*>(jwnd);
	if (scriptObject == NULL) {
		TRACE("*** scriptObject == NULL");
		return JNI_FALSE;
	}
	// get object representing JS function
	JStringWrapper methodName(env, jmethodName);
/*NSString *message = [NSString stringWithFormat:@"%p: %s", scriptObject, methodName.c_str()];
TRACE_ERR([message UTF8String]);*/
	id functionObject = [scriptObject valueForKey:methodName.ns_str()];
	if (isUndefined(functionObject)) {
		NSString *message = [NSString stringWithFormat:@"*** Undefined function: %s", methodName.c_str()];
		TRACE_ERR([message UTF8String]);
		return JNI_FALSE;
	}
	// do invoke the JS function
	id result = [functionObject callWebScriptMethod:@"call" withArguments:args];
	if (result == nil) {
		result = [NSNull null];
	}

	[args release];
	jlong value = convertPointer(result);
    env->SetLongArrayRegion(rval, 0, 1, &value);
    if (env->ExceptionCheck()) {
        return JNI_FALSE;
	}

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1isNumber)
	(JNIEnv *, jclass, jlong jsval)
{
	id objectValue = (id) jsval;
	if (isNumber(objectValue)) { 
		return JNI_TRUE;
	}
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1isBoolean)
(JNIEnv *, jclass, jlong jsval)
{
	id objectValue = (id) jsval;
	// FIXME: I can't determine is it boolean or not, so just use isNumber
	if (isNumber(objectValue)) { 
		return JNI_TRUE;
	}
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1isObject)
	(JNIEnv *, jclass, jlong jsval)
{
	id objectValue = (id) jsval;
	// FIXME: I don't know what is "object": is it WebScriptObject or dispatch wrapper object
	// but I know that it is not a "null", "undefined", "string", "number", "array"
	if (isNull(objectValue) ||
		isUndefined(objectValue) ||
		[objectValue isKindOfClass:[NSString class]] ||
		[objectValue isKindOfClass:[NSNumber class]] ||
		[objectValue isKindOfClass:[NSArray class]]
	  ) {
		return JNI_FALSE;
	}
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1isString)
	(JNIEnv *, jclass, jlong jsval)
{
	if (jsval && [((id)jsval) isKindOfClass:[NSString class]]) {
		return JNI_TRUE;
	}
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1isWrappedDispatch)
	(JNIEnv* env, jclass, jlong jsval, jbooleanArray rval)
{
	if (!jsval) {
		return JNI_FALSE;
	}
	NSObject *value = (NSObject*)jsval;
	jboolean result = [value isMemberOfClass:[DispatchObjectWrapper class]] ? JNI_TRUE : JNI_FALSE;  //isKindOfClass?
	env->SetBooleanArrayRegion(rval, 0, 1, &result);
	if (env->ExceptionCheck()) {
		return JNI_FALSE;
	}
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1raiseJavaScriptException)
	(JNIEnv *env, jclass, jlong execState, jlong jsval) 
{
	TRACE("Not Implemented: raiseJavaScriptException");
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1unwrapDispatch)
	(JNIEnv* env, jclass, jlong jsval, jobjectArray rval)
{
	// TODO: maybe move this method into java?
	if (!jsval) {
		return JNI_FALSE;
	}
	NSObject *value = (NSObject*)jsval;
	if ([value isMemberOfClass:[DispatchObjectWrapper class]]) { // isKindOfClass?
		jobject dispObj = env->CallStaticObjectMethod(c_JsValueSaf, m_JsValueSaf_getDispatchObjectRef, jsval);
		if (dispObj == NULL) {
			return JNI_FALSE;
		}
		env->SetObjectArrayElement(rval, 0, dispObj);
		if (env->ExceptionCheck()) {
			return JNI_FALSE;
		}
		return JNI_TRUE;
	}
	return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1wrapDispatch)
	(JNIEnv* env, jclass, jobject jdispatchObject, jlongArray rval)
{
	DispatchObjectWrapper *wrapper = [[DispatchObjectWrapper alloc] initWithObject:jdispatchObject];
	jlong value = convertPointer(wrapper);
    env->SetLongArrayRegion(rval, 0, 1, &value);
    if (env->ExceptionCheck()) {
        return JNI_FALSE;
	}

    return JNI_TRUE;
}

JNIEXPORT jstring JNICALL OS_NATIVE(_1getTypeString)
	(JNIEnv* env, jclass, jlong jsval)
{
	id objectValue = (id) jsval;
	return createJavaString(env, [NSString stringWithUTF8String:object_getClassName(objectValue)]);
}

} // extern "C"
#ifdef FILETRACE
FILE* gout = 0;
void filetrace(const char* s) {
    fprintf(gout, s);
    fprintf(gout, "\n");
    fflush(gout);
}
#endif //FILETRACE

#if defined (JAVATRACE) || defined (JAVATRACE_ERR)
jmethodID m_BrowserShell_trace = 0;
void javatrace(const char* s) {
    if (!gEnv->ExceptionCheck()) {
        jstring out = gEnv->NewStringUTF(s);
        if (!gEnv->ExceptionCheck()) {
            gEnv->CallStaticVoidMethod(c_BrowserShell, m_BrowserShell_trace, out);
        } else {
            gEnv->ExceptionClear();
		}
    }
}
#endif //JAVATRACE
