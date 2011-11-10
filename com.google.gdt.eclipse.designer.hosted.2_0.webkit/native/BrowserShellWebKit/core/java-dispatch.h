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

#ifndef JAVA_DISPATCHER_H__
#define JAVA_DISPATCHER_H__
#include <iostream>
#include "gwt-jni.h"

void add_to_List(JNIEnv *env, jobject jlist, jobject jwhat);
const void* get_from_List_unwrapped(JNIEnv *env, jobject jlist, jint idx);

namespace gwt {

/*
 * Initializes static members needed by DispatchObject, DispatchMethod,
 * and LowLevelSaf.  This should be called before before calling anything
 * else.
 */
bool Initialize(JNIEnv*, jclass, jclass, jclass);

/*
 * Returns a shared reference to the DispatchObject class
 */
JSClassRef GetDispatchObjectClass();

/*
 * Constructs a new DispatchObject.
 *
 * jContext - the JavaScript context
 * jObject - the java instance of DispatchObject to which
 *   this instance will dispatch calls
 */
JSObjectRef DispatchObjectCreate(JSContextRef jContext, jobject jObject);

/*
 * Returns a shared reference to the DispatchMethod class
 */
JSClassRef GetDispatchMethodClass();

/*
 * Constructs a new DispatchMethod object.
 *
 * jsContext - the JavaScript context
 * name - the name of the method (used in toString)
 * jObject - the java instance of DispatchMethod to which this object will
 *   delegate calls.
 */
JSObjectRef DispatchMethodCreate(JSContextRef jsContext, std::string& name,
                                 jobject jObject);


} // namespace gwt

#endif // JAVA_DISPATCHER_H__
