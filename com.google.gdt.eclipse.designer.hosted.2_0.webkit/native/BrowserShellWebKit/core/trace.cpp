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

#include <iostream>
#include <map>
#include "trace.h"

#ifdef ENABLE_JSVALUE_PROTECTION_CHECKING
static std::map<JSValueRef, int> _protectMap;
#endif

/*
 *
 */
void JSValueProtectChecked(JSContextRef jsContext, JSValueRef jsValue) {
  JSValueProtect(jsContext, jsValue);
#ifdef ENABLE_JSVALUE_PROTECTION_CHECKING
  _protectMap[jsValue]++;
#endif
}

/*
 *
 */
void JSValueUnprotectChecked(JSContextRef jsContext, JSValueRef jsValue) {
#ifdef ENABLE_JSVALUE_PROTECTION_CHECKING
  // Unrecord in a hash_map
  unsigned count = _protectMap[jsValue];
  if (count == 0) {
    std::cerr << "[WARNING] JSValueUnprotect called on unprotected JSValueRef (0x"
        << std::hex << ((unsigned)jsValue) << ")" << std::endl;
    return;
  }
  _protectMap[jsValue] = count - 1;
#else
  JSValueUnprotect(jsContext, jsValue);
#endif
}

bool JSValueIsProtected(JSValueRef jsValue) {
#ifdef ENABLE_JSVALUE_PROTECTION_CHECKING
  return _protectMap[jsValue] > 0;
#else
  return true;
#endif
}

bool JSValueProtectCheckingIsEnabled() {
#ifdef ENABLE_JSVALUE_PROTECTION_CHECKING
  return true;
#else
  return false;
#endif
}
