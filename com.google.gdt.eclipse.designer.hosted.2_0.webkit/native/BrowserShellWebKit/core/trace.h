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

#ifndef TRACE_H__
#define TRACE_H__
#include <iostream>
#include <map>
#include "gwt-jni.h"

// Uncomment to trace enter, exit and fail for all native calls.
// #define ENABLE_CALL_TRACE

// Uncomment to double check that JSValueRef's are protected and
// unprotected properly.
// #define ENABLE_JSVALUE_PROTECTION_CHECKING

#ifdef ENABLE_CALL_TRACE
#define TR_ENTER() std::cout << "ENTER " << __PRETTY_FUNCTION__ << std::endl
#define TR_LEAVE() std::cout << "LEAVE " << __PRETTY_FUNCTION__ << std::endl
#define TR_FAIL() std::cout << "FAIL " << __FILE__ << "@" << __LINE__ \
    << std::endl;
#else
#define TR_ENTER()
#define TR_LEAVE()
#define TR_FAIL()
#endif // ENABLE_CALL_TRACE

void JSValueUnprotectChecked(JSContextRef, JSValueRef);
void JSValueProtectChecked(JSContextRef, JSValueRef);
bool JSValueIsProtected(JSValueRef jsValue);
bool JSValueProtectCheckingIsEnabled();

#endif // TRACE_H__
