//
//  DispatchObjectWrapper.m
//  BrowserShell
//
//  Created by Alexander Mitin on 26.05.07.
//  Copyright 2007 Instantiations, Inc. All rights reserved.
//

#include "wbp-gwt.h"
#include "JStringWrapper.h"
#import "DispatchObjectWrapper.h"


static JNIEnv* getJNIEnv() {
	JNIEnv* env = NULL;
	gJavaVM->AttachCurrentThreadAsDaemon(reinterpret_cast<void**>(&env), NULL);
	return env;
}

#define GET_JNIENV(a, b) \
  JNIEnv *env = getJNIEnv();\
  if (env == NULL) {\
    TRACE_ERR((b));\
    TRACE_ERR("JNIEnv == NULL");\
    return (a);\
  }

#define GET_JNIENV_VOID(b) \
  JNIEnv *env = getJNIEnv();\
  if (env == NULL) {\
    TRACE_ERR((b));\
    TRACE_ERR("JNIEnv == NULL");\
    return;\
  }

#define GET_DISPOBJ(a, b) \
  jlong value__ = convertPointer(self);\
  jobject dispObject = env->CallStaticObjectMethod(c_JsValueSaf, m_JsValueSaf_getDispatchObjectRef, value__);\
  if (dispObject == NULL) {\
	TRACE_ERR((b));\
	TRACE_ERR("NULL dispObject");\
	return (a);\
  }
#define GET_DISPOBJ_VOID(b) \
  jlong value__ = convertPointer(self);\
  jobject dispObject = env->CallStaticObjectMethod(c_JsValueSaf, m_JsValueSaf_getDispatchObjectRef, value__);\
  if (dispObject == NULL) {\
    TRACE_ERR((b));\
    TRACE_ERR("NULL dispObject");\
    return;\
  }

@implementation DispatchObjectWrapper

- initWithObject:(jobject)obj 
{
	[super init];
	GET_JNIENV(self, "initWithObject:");
	// store ref in java
	jlong value = convertPointer(self);
	env->CallStaticVoidMethod(c_JsValueSaf, m_JsValueSaf_putDispatchObjectRef, value, obj);
	return self;
}

/*- (id)retain
{
	NSString *message = [NSString stringWithFormat:@" retain: %p", self];
	TRACE_ERR([message UTF8String]);
	return [super retain];
}

- (void)release
{
	NSString *message = [NSString stringWithFormat:@"release: %p: %d", self, [self retainCount] - 1];
	TRACE_ERR([message UTF8String]);
	[super release];
}*/
- (void)dealloc
{
	JNIEnv *env = getJNIEnv();
	if (env) {
		jlong value = convertPointer(self);
		env->CallStaticVoidMethod(c_JsValueSaf, m_JsValueSaf_removeDispatchObjectRef, value);
	}
	[super dealloc];
}
                                                                                                                        
+ (BOOL)isSelectorExcludedFromWebScript:(SEL)aSelector                                                                       
{  
	return YES;                                                                                                           
}                                                                                                                            
                                                                                                                             
+ (BOOL)isKeyExcludedFromWebScript:(const char *)name                                                                        
{       
	/*jobjectArray result = (jobjectArray)gEnv->CallObjectMethod([self object], m_DispatchObject_getFields);
	jsize len = gEnv->GetArrayLength(result);
	BOOL excluded = YES;
	for (int i = 0; i < len; ++i) {
		JStringWrapper str(gEnv, (jstring)gEnv->GetObjectArrayElement(result, i));
		if (strcmp(str.c_str(), name) == 0) {
			excluded = NO;
			break;
			//return NO;
		}
	}    
	                                                                                                             
	BOOL res = ![[self attributeKeys] containsObject:[NSString stringWithUTF8String:name]];
	*/
	/*NSString *message = [NSString stringWithFormat:@"isKeyExcludedFromWebScript: \"%s\"\n", name];
	TRACE_ERR([message UTF8String]);
	return excluded;*/
	return NO;
}                                                                                                                            
- (NSArray *)attributeKeys
{
	GET_JNIENV([super attributeKeys], "attributeKeys:");
	GET_DISPOBJ([super attributeKeys], "attributeKeys:");
	jobjectArray result = (jobjectArray)env->CallObjectMethod(dispObject, m_DispatchObject_getFields);
	jsize len = env->GetArrayLength(result);
	NSMutableArray *res = [NSMutableArray arrayWithArray:[super attributeKeys]];
	for (int i = 0; i < len; ++i) {
		JStringWrapper str(env, (jstring)env->GetObjectArrayElement(result, i));
		NSString *s = [NSString stringWithUTF8String:str.c_str()];
		[res addObject:s];
	}
	return res;
}

- (NSString *)stringRepresentation
{
	GET_JNIENV(@"NULL JNIEnv", "stringRepresentation");
	GET_DISPOBJ(@"NULL JNIEnv", "stringRepresentation");
	jstring jresult = (jstring)env->CallObjectMethod(dispObject, m_DispatchObject_toString);
	if (env->ExceptionCheck() || !jresult) {
		return @"Undefined";
	} else {
		JStringWrapper result(env, jresult);
		return [NSString stringWithUTF8String:result.c_str()];
	}
}
- (id)invokeDefaultMethodWithArguments:(NSArray *)arg 
{
/*	NSString *message = [NSString stringWithFormat:@"invokeDefaultMethodWithArguments: %p\n", arg];
	TRACE_ERR([message UTF8String]);*/

	return [WebUndefined undefined];
}
- (id)invokeUndefinedMethodFromWebScript:(NSString *)name withArguments:(NSArray *)args 
{
	/*NSString *message = [NSString stringWithFormat:@"invokeUndefinedMethodFromWebScript: \"%s\"\n", [name UTF8String]];
	TRACE_ERR([message UTF8String]);

	printf("%s\n", message);fflush(stdout);*/
	
	GET_JNIENV([WebUndefined undefined], "invokeUndefinedMethodFromWebScript:");
	GET_DISPOBJ([WebUndefined undefined], "invokeUndefinedMethodFromWebScript:");

	jstring jmethodName = createJavaString(env, name);
	jobject wrappedMethod = env->CallObjectMethod(dispObject, m_DispatchObject_getWrappedMethod, jmethodName);
	int argc = [args count];
    jlongArray jsargs = env->NewLongArray(argc);
    if (!jsargs || env->ExceptionCheck()) {
		return [WebUndefined undefined];
	}

	for (unsigned int i = 0; i < argc; ++i) {
		id arg = [args objectAtIndex: i];
		jlong argValue = convertPointer(arg);
		env->SetLongArrayRegion(jsargs, i, 1, &argValue);
		if (env->ExceptionCheck()) {
			return [WebUndefined undefined];
		}
	}

	jlong value = convertPointer(self);
    id result = (id)env->CallLongMethod(wrappedMethod, m_DispatchMethod_invoke, value, jsargs);
    if (env->ExceptionCheck()) {
		return [WebUndefined undefined];
	}
	return result;
}   
- (void)setValue:(id)value forKey:(NSString *)key 
{
/*	NSString *message = [NSString stringWithFormat:@"setValue: \"%s\" forKey: %s\n", object_getClassName(value), [key cString]];
	TRACE_ERR([message UTF8String]);*/
	GET_JNIENV_VOID("setValue:forKey:");
	jstring jpropertyName = createJavaString(env, key);
	if (!jpropertyName || env->ExceptionCheck()) {
		env->ExceptionClear();
		return;
	}
	GET_DISPOBJ_VOID("setValue:forKey:");
	jlong value2 = convertPointer(value);
	env->CallVoidMethod(dispObject, m_DispatchObject_setField, jpropertyName, value2);
	if (env->ExceptionCheck()) {
		env->ExceptionClear();
	}
}
- (void)setValue:(id)value forUndefinedKey:(NSString *)key 
{
/*	NSString *message = [NSString stringWithFormat:@"setValue: \"%s\" forUndefinedKey: %s\n", object_getClassName(value), [key cString]];
	TRACE_ERR([message UTF8String]);*/
}
- (id)valueForKey:(NSString *)key
{
/*	NSString *message = [NSString stringWithFormat:@"value for key: \"%s\"\n", [key cString]];
	TRACE_ERR([message UTF8String]);*/
	GET_JNIENV([WebUndefined undefined], "valueForKey:");
	GET_DISPOBJ([WebUndefined undefined], "valueForKey:");
	jstring jpropertyName = createJavaString(env, key);
	if (!jpropertyName || env->ExceptionCheck()) {
		env->ExceptionClear();
		return [WebUndefined undefined];
	}
	jlong result = env->CallLongMethod(dispObject, m_DispatchObject_getField, jpropertyName);
	if (!result || env->ExceptionCheck()) {
		env->ExceptionClear();
		return [WebUndefined undefined];
	}
	return (id)result;
}
- (id)valueForUndefinedKey:(NSString *)key 
{
/*	jstring jpropertyName = createJavaString(key);
	jint result = gEnv->CallIntMethod(m_object, m_DispatchObject_getField, jpropertyName);

	NSString *message = [NSString stringWithFormat:@"value for undefined key: \"%s\", result = %d\n", [key cString], result];
	TRACE_ERR([message UTF8String]);*/
	return @"[ GWT Designer: undefined key ]";
}                                                                                                                          
@end
