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
// include all of the necessary Mozilla headers.

#include "mozilla-config.h"

#include "nsStringAPI.h"
#include "nsXPCOM.h"
#include "nsIServiceManager.h"
#include "nsIDOMWindow.h"

#include "nsServiceManagerUtils.h"

#include "nsCOMPtr.h"
#include "nsAutoPtr.h"

#include "nsEvent.h"
#include "jsapi.h"

#include "nsIJSContextStack.h"

#ifdef JSVAL_VOID 
  #undef JSVAL_VOID
#endif

#ifdef JSVAL_IS_VOID
  #undef JSVAL_IS_VOID
#endif

#ifdef JSVAL_IS_NUMBER
  #undef JSVAL_IS_NUMBER
#endif



