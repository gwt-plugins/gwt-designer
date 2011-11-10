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
#ifndef __BROWSER_SHELL_H
#define __BROWSER_SHELL_H

#include <jni.h>

extern JNIEnv* gEnv;
extern JavaVM* gJavaVM;
extern jclass c_DispatchObject;
extern jclass c_DispatchMethod;
extern jclass c_JsValueSaf;

extern jmethodID m_DispatchObject_getField; 
extern jmethodID m_DispatchObject_getFields;
extern jmethodID m_DispatchObject_setField;
extern jmethodID m_DispatchObject_getWrappedMethod; 
extern jmethodID m_DispatchObject_toString; 
extern jmethodID m_DispatchMethod_invoke;
extern jmethodID m_JsValueSaf_getDispatchObjectRef;
extern jmethodID m_JsValueSaf_putDispatchObjectRef;
extern jmethodID m_JsValueSaf_removeDispatchObjectRef;


jstring createJavaString(JNIEnv* env, NSString* nsString);
jlong convertPointer(void *ptr);

// #define FILETRACE
// #define JAVATRACE
// #define JAVATRACE_ERR

#if defined(FILETRACE) && defined(JAVATRACE)
#define TRACE(s) filetrace(s),javatrace(s)
#elif defined(FILETRACE)
#define TRACE(s) filetrace(s)
#elif defined(JAVATRACE)
#define TRACE(s) javatrace(s)
#else
#define TRACE(s) ((void)0)
#endif

#if defined(JAVATRACE_ERR)
#define TRACE_ERR(s) javatrace(s)
#else
#define TRACE_ERR(s) ((void)0)
#endif

#ifdef FILETRACE
extern FILE* gout;
void filetrace(const char* s);
#endif // FILETRACE

#if defined(JAVATRACE) || defined(JAVATRACE_ERR)
extern jmethodID m_BrowserShell_trace;
void javatrace(const char* s);
#endif // JAVATRACE

#define OS_NATIVE(func) Java_com_instantiations_designer_gwt_mac_BrowserShellMac_##func

#ifdef __LP64__                                                                                                                                                                                                                 
#define JHANDLE jlong                                                                                                                                                                                                           
#else                                                                                                                                                                                                                           
#define JHANDLE jint                                                                                                                                                                                                            
#endif


#endif
