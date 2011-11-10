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
#ifndef JNI_WEBKIT_GWT_JNI_H_
#define JNI_WEBKIT_GWT_JNI_H_

#define OS_NATIVE_LL(func) Java_com_google_gdt_eclipse_designer_webkit_jsni_LowLevelWebKit_##func 

#if defined(_WBP_MACOSX) || defined(_WBP_WIN32)
#include <JavaScriptCore/JavaScriptCore.h>
#else
#include <JavaScriptCore/JavaScript.h>
#endif // _WBP_MACOSX

#include <jni.h>

#endif /*JNI_WEBKIT_GWT_JNI_H_*/
