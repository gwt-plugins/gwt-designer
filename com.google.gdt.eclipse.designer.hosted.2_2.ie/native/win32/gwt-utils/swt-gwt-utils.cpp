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
#include <stdio.h>
#include <jvmpi.h>
#include <stdlib.h>
#include <windows.h>
#include <Wininet.h>

#ifdef _WIN64
	#define JHANDLE jlong
#else
	#define JHANDLE jint
#endif
#define OS_NATIVE_UTILS(func) Java_com_google_gdt_eclipse_designer_ie_util_Utils_##func 


extern "C" { 
	JNIEXPORT jint JNICALL OS_NATIVE_UTILS(_1newGlobalRefInt)(JNIEnv *env, jclass that, jobject obj) {
		return (jint) env->NewGlobalRef( obj );
	}
	JNIEXPORT void JNICALL OS_NATIVE_UTILS(_1deleteGlobalRefInt)(JNIEnv *env, jclass that, jobject globalRef) {
		env->DeleteGlobalRef( globalRef );
	}
	JNIEXPORT jobject JNICALL OS_NATIVE_UTILS(_1objFromGlobalRefInt)(JNIEnv *env, jclass that, jobject globalRef) {
		return globalRef;
	}

	JNIEXPORT void JNICALL OS_NATIVE_UTILS(_1ensureProxyBypassLocal)(JNIEnv *env, jclass that) {
		INTERNET_PER_CONN_OPTION_LIST	List; 
		INTERNET_PER_CONN_OPTION        Option; 
		
		unsigned long                   nSize = sizeof(INTERNET_PER_CONN_OPTION_LIST); 
		
		Option.dwOption = INTERNET_PER_CONN_PROXY_BYPASS; 

		List.dwSize = sizeof(INTERNET_PER_CONN_OPTION_LIST); 
		List.pszConnection = NULL; 
		List.dwOptionCount = 1; 
		List.dwOptionError = 0; 
		List.pOptions = &Option; 

		char buffer[4096];
		ZeroMemory(buffer, sizeof(buffer));

		if (InternetQueryOption(NULL, INTERNET_OPTION_PER_CONNECTION_OPTION, &List, &nSize)) {
   			if (Option.Value.pszValue != NULL) {
				strncpy(buffer, Option.Value.pszValue, sizeof(buffer));
   				GlobalFree(Option.Value.pszValue);
   				if (strstr(buffer, "<local>") == NULL) {
   					strncat(buffer, ";<local>", sizeof(buffer) - strlen(buffer));
   				} else {
   					// already bypassing local addresses
   					return;
   				}
   			} else {
 				strncpy(buffer, "<local>", sizeof(buffer));
 			}
			Option.Value.pszValue = buffer; 

			InternetSetOption(NULL, INTERNET_OPTION_PER_CONNECTION_OPTION, &List, nSize);
			InternetSetOption(NULL, INTERNET_OPTION_REFRESH, NULL, NULL); 
   		}
	}
}
