/* 
 * Copyright 2006 Google Inc.
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
#ifndef __JSTRINGWRAPPER_H
#define __JSTRINGWRAPPER_H

#include <jni.h>

struct JStringWrapper
{
    JStringWrapper(JNIEnv* env, jstring str): env(env), s(str), p(0), jp(0), ns(0) { }
    ~JStringWrapper() { 
		if (ns) {
			[ns release];
		}
		if (p) { 
			env->ReleaseStringUTFChars(s, p); 
		}
		if (jp) {
			env->ReleaseStringChars(s, jp); 
		}
	}
    const char* c_str() { 
		if (!p) {
			p = env->GetStringUTFChars(s, 0);
		} 
		return p; 
	}
    const jchar* j_str() { 
		if (!jp) { 
			jp = env->GetStringChars(s, 0); 
		}
		return jp; 
	}
	const NSString* ns_str() {
		if (!ns) {
			ns = [[NSString alloc] initWithUTF8String:c_str()];
		}
		return ns;
	}
	jsize length() { 
		return env->GetStringLength(s); 
	}
private:
    JNIEnv* env;
    jstring s;
	const NSString *ns;
    const char* p;
    const jchar* jp;
};

#endif
