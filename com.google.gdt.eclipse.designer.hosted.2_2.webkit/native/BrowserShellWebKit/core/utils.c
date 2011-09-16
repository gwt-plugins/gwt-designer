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
#include "utils.h"

#ifdef _WIN32
#include <windows.h>
#endif

#if (defined(__LP64__) && (!defined(WBP_ARCH64))) || defined(_WIN64)
#define WBP_ARCH64
#endif /*__LP64__*/  

void* unwrap_pointer(JNIEnv *env, jobject jptr) {
	jclass clazz;
#ifdef WBP_ARCH64
	jlong result;
#else
	jint result;
#endif
	static jmethodID getterMethod = NULL;
	if (jptr == NULL) {
		return NULL;
	}
	clazz = (*env)->GetObjectClass(env, jptr);
#ifdef WBP_ARCH64
	if (getterMethod == NULL) {
		getterMethod = (*env)->GetMethodID(env, clazz, "longValue", "()J");
	}
	result = (*env)->CallLongMethod(env, jptr, getterMethod);
#else
	if (getterMethod == NULL) {
		getterMethod = (*env)->GetMethodID(env, clazz, "intValue", "()I");
	}
	result = (*env)->CallIntMethod(env, jptr, getterMethod);
#endif
	(*env)->DeleteLocalRef(env, clazz);
	return (void*)result;
}
jobject wrap_pointer(JNIEnv *env, const void* ptr) {
	jclass clazz;
	jobject newObject;
	static jmethodID ctor = NULL;
	if (ptr == NULL) {
		return NULL;
	}
#ifdef WBP_ARCH64
	clazz = (*env)->FindClass(env, "java/lang/Long");
	if (ctor == NULL) {
		ctor = (*env)->GetMethodID(env, clazz, "<init>", "(J)V");
	}
	newObject = (*env)->NewObject(env, clazz, ctor, (jlong)ptr);
#else
	clazz = (*env)->FindClass(env, "java/lang/Integer");
	if (ctor == NULL) {
		ctor = (*env)->GetMethodID(env, clazz, "<init>", "(I)V");
	}
	newObject = (*env)->NewObject(env, clazz, ctor, (jint)ptr);
#endif
	(*env)->DeleteLocalRef(env, clazz);
	return newObject;
}
static char *GetEnv(const char *variable) {
#ifdef _WIN32
	char env[MAX_PATH]; /* MAX_PATH is from windef.h */
	char *temp = getenv(variable);
	env[0] = '\0';
	if(temp != NULL) {
		ExpandEnvironmentStringsA(temp, env, sizeof(env));
	}
	return (env[0] != '\0') ? strdup(env) : NULL;
#else
	char *env = getenv(variable);
	return (env && env[0]) ? strdup(env) : NULL;
#endif
}

void checkNoProxy() {
  char *app, *newNoProxy;
  size_t len;
  // try 'no_proxy' first
  char *envName = "no_proxy";
  char *noProxy = GetEnv(envName);
  if (!noProxy) {
    // try uppercase
    envName = "NO_PROXY";
    noProxy = GetEnv(envName);
  }
  if (noProxy) {
    if (strstr(noProxy, "127.0.0.1") || strstr(noProxy, "localhost")) {
      // already set
      free(noProxy);
      return;
	}
  } else {
    // no luck with uppercase too, use low-case as default
    envName = "no_proxy";
  }
  // prepare new
  app = "127.0.0.1,localhost";
  len = strlen(envName) + 1 + strlen(app) + 1; // one for '=' and one for term zero
  if (noProxy) {
    len += strlen(noProxy) + 1; // extra 1 for comma
  }
  newNoProxy = (char*)malloc(len);
  strcpy(newNoProxy, envName);
  strcat(newNoProxy, "=");
  strcat(newNoProxy, app);
  // copy existing
  if (noProxy) {
    strcat(newNoProxy, ",");
	strcat(newNoProxy, noProxy);
    free(noProxy);
  }
  // putenv & cleanup
  putenv(newNoProxy);
  free(newNoProxy);
}
