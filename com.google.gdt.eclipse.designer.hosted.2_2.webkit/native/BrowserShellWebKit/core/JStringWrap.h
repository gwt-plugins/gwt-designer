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
#ifndef JSTRINGWRAP_H
#define JSTRINGWRAP_H

#include <jni.h>

/*
 * Wrap a Java String and automatically clean up temporary storage allocated
 * for accessing its contents.
 */
struct JStringWrap
{
  JStringWrap(JNIEnv* env, jstring str): env(env), s(str), p(0), jp(0) { }
  ~JStringWrap() {
  	if (p) env->ReleaseStringUTFChars(s, p);
  	if (jp) env->ReleaseStringChars(s, jp);
  }
  const char* str() { if (!p) p = env->GetStringUTFChars(s, 0); return p; }
  const jchar* jstr() { if (!jp) jp = env->GetStringChars(s, 0); return jp; }
  jsize length() { return env->GetStringLength(s); }
private:
  JNIEnv* env;
  jstring s;
  const char* p;
  const jchar* jp;
};

#endif
