/* -*- Mode: C++; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set ts=2 sw=2 et tw=80: */
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

#ifndef nsIScriptGlobalObject192_h__
#define nsIScriptGlobalObject192_h__

#include "nsISupports.h"
#include "nsEvent.h"
//#include "nsIProgrammingLanguage.h"

class nsIScriptContext192;
class nsIDOMDocument;
class nsIDOMEvent;
class nsPresContext;
class nsIScriptGlobalObjectOwner;
class nsIArray;
class nsScriptErrorEvent;
class nsIScriptGlobalObject192;
struct JSObject; // until we finally remove GetGlobalJSObject...

// Some helpers for working with integer "script type IDs", and specifically
// for working with arrays of such objects. For example, it is common for
// implementations supporting multiple script languages to keep each
// language's nsIScriptContext in an array indexed by the language ID.

// Implementation note: We always ignore nsIProgrammingLanguage::UNKNOWN and
// nsIProgrammingLanguage::CPLUSPLUS - this gives javascript slot 0.  An
// attempted micro-optimization tried to avoid us going all the way to 
// nsIProgrammingLanguage::MAX; however:
// * Someone is reportedly working on a PHP impl - that has value 9
// * nsGenericElement therefore allows 4 bits for the value.
// So there is no good reason for us to be more restrictive again...

//#define NS_STID_FIRST nsIProgrammingLanguage::JAVASCRIPT
// like nsGenericElement, only 4 bits worth is valid...
//#define NS_STID_LAST (nsIProgrammingLanguage::MAX > 0x000FU ? \
//                      0x000FU : nsIProgrammingLanguage::MAX)

// Use to declare the array size
//#define NS_STID_ARRAY_UBOUND (NS_STID_LAST-NS_STID_FIRST+1)

// Is a language ID valid?
//#define NS_STID_VALID(langID) (langID >= NS_STID_FIRST && langID <= NS_STID_LAST)

// Return an index for a given ID.
//#define NS_STID_INDEX(langID) (langID-NS_STID_FIRST)

// Create a 'for' loop iterating over all possible language IDs (*not* indexes)
//#define NS_STID_FOR_ID(varName) \
//         for (varName=NS_STID_FIRST;varName<=NS_STID_LAST;varName++)

// Create a 'for' loop iterating over all indexes (when you don't need to know
// what language it is)
//#define NS_STID_FOR_INDEX(varName) \
//          for (varName=0;varName<=NS_STID_INDEX(NS_STID_LAST);varName++)

// A helper function for nsIScriptGlobalObject implementations to use
// when handling a script error.  Generally called by the global when a context
// notifies it of an error via nsIScriptGlobalObject::HandleScriptError.
// Returns PR_TRUE if HandleDOMEvent was actually called, in which case
// aStatus will be filled in with the status.
PRBool
NS_HandleScriptError(nsIScriptGlobalObject192 *aScriptGlobal,
                     nsScriptErrorEvent *aErrorEvent,
                     nsEventStatus *aStatus);


#define NS_ISCRIPTGLOBALOBJECT_192_IID \
{ 0xe9f3f2c1, 0x2d94, 0x4722, \
  { 0xbb, 0xd4, 0x2b, 0xf6, 0xfd, 0xf4, 0x2f, 0x48 } }

/**
  * The global object which keeps a script context for each supported script
  * language. This often used to store per-window global state.
 */

class nsIScriptGlobalObject192 : public nsISupports
{
public:
//  NS_DECLARE_STATIC_IID_ACCESSOR(NS_ISCRIPTGLOBALOBJECT_IID)
   static const nsIID& GetIID() {static const nsIID iid = NS_ISCRIPTGLOBALOBJECT_192_IID; return iid;}
  /**
   * Ensure that the script global object is initialized for working with the
   * specified script language ID.  This will set up the nsIScriptContext
   * and 'script global' for that language, allowing these to be fetched
   * and manipulated.
   * @return NS_OK if successful; error conditions include that the language
   * has not been registered, as well as 'normal' errors, such as
   * out-of-memory
   */
  virtual nsresult EnsureScriptEnvironment(PRUint32 aLangID) = 0;
  /**
   * Get a script context (WITHOUT added reference) for the specified language.
   */
  virtual nsIScriptContext192 *GetScriptContext(PRUint32 lang) = 0;
  
  /**
   * Get the opaque "global" object for the specified lang.
   */
  virtual void *GetScriptGlobal(PRUint32 lang) = 0;

  // Set/GetContext deprecated methods - use GetScriptContext/Global
  virtual JSObject *GetGlobalJSObject() {
        return (JSObject *)GetScriptGlobal(2/*nsIProgrammingLanguage::JAVASCRIPT*/);
  }

  virtual nsIScriptContext192 *GetContext() {
        return GetScriptContext(2/*nsIProgrammingLanguage::JAVASCRIPT*/);
  }

  /**
   * Set a new language context for this global.  The native global for the
   * context is created by the context's GetNativeGlobal() method.
   */

  virtual nsresult SetScriptContext(PRUint32 lang, nsIScriptContext192 *aContext) = 0;

  /**
   * Called when the global script for a language is finalized, typically as
   * part of its GC process.  By the time this call is made, the
   * nsIScriptContext for the language has probably already been removed.
   * After this call, the passed object is dead - which should generally be the
   * same object the global is using for a global for that language.
   */

  virtual void OnFinalize(PRUint32 aLangID, void *aScriptGlobal) = 0;

  /**
   * Called to enable/disable scripts.
   */
  virtual void SetScriptsEnabled(PRBool aEnabled, PRBool aFireTimeouts) = 0;

  /**
   * Handle a script error.  Generally called by a script context.
   */
  virtual nsresult HandleScriptError(nsScriptErrorEvent *aErrorEvent,
                                     nsEventStatus *aEventStatus) {
    return NS_HandleScriptError(this, aErrorEvent, aEventStatus);
  }
};

//NS_DEFINE_STATIC_IID_ACCESSOR(nsIScriptGlobalObject,
//                              NS_ISCRIPTGLOBALOBJECT_IID)

#endif
