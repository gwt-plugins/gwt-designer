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
#ifndef __UTILS_H_
#define __UTILS_H_

#include "gwt-jni.h"

#ifdef __cplusplus
extern "C" const
#endif
void* unwrap_pointer(JNIEnv *env, jobject jptr);

#ifdef __cplusplus
extern "C" 
#endif
jobject wrap_pointer(JNIEnv *env, const void* ptr);

#ifdef __cplusplus
extern "C" 
#endif
void checkNoProxy();

#endif // __UTILS_H_