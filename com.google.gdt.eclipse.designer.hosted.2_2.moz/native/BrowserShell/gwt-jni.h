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
#ifndef JNI_LINUX_GWT_JNI_H_
#define JNI_LINUX_GWT_JNI_H_

#ifdef WBP_ARCH64 
#define JHANDLE jlong 
#define OS_HANDLE long
#define SET_FIELD_SIGNATURE "(Ljava/lang/String;J)V"
#define GET_FIELD_SIGNATURE "(Ljava/lang/String;J)V"
#define INVOKE_SIGNATURE "(J[JJ)V"
#define OS_NATIVE_LL(func) Java_com_google_gdt_eclipse_designer_moz_jsni_LowLevelMoz64_##func 
#define OS_NATIVE_JSV(func) Java_com_google_gdt_eclipse_designer_moz_jsni_JsValueMoz64_##func 
#else 
#define SET_FIELD_SIGNATURE "(Ljava/lang/String;I)V"
#define GET_FIELD_SIGNATURE "(Ljava/lang/String;I)V"
#define INVOKE_SIGNATURE "(I[II)V"
#define JHANDLE jint 
#define OS_HANDLE int
#define OS_NATIVE_LL(func) Java_com_google_gdt_eclipse_designer_moz_jsni_LowLevelMoz32_##func 
#define OS_NATIVE_JSV(func) Java_com_google_gdt_eclipse_designer_moz_jsni_JsValueMoz32_##func 
#endif


#include <jni.h>

extern JNIEnv* savedJNIEnv;
extern jclass lowLevelMozClass;

#endif /*JNI_LINUX_GWT_JNI_H_*/
