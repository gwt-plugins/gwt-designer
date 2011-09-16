/* -*- Mode: C++; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* vim: set ts=2 sw=2 et tw=80: */
/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is mozilla.org code.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1998
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */


#ifndef nsPIDOMWindow191_h__
#define nsPIDOMWindow191_h__

#include "nsISupports.h"
#include "nsIDOMLocation.h"
#include "nsIDOMElement.h"
#include "nsIDOMDocument.h"
#include "nsIDOMWindowInternal.h"
#include "nsCOMPtr.h"
#include "nsEvent.h"


// Popup control state enum. The values in this enum must go from most
// permissive to least permissive so that it's safe to push state in
// all situations. Pushing popup state onto the stack never makes the
// current popup state less permissive (see
// nsGlobalWindow::PushPopupControlState()).
enum PopupControlState191 {
  openAllowed191 = 0,  // open that window without worries
  openControlled191,   // it's a popup, but allow it
  openAbused191,       // it's a popup. disallow it, but allow domain override.
  openOverridden191    // disallow window open
};

class nsIDocShell;

#define NS_PIDOMWINDOW_191_IID \
{ 0x80dd53b6, 0x8c61, 0x4dd6, \
  { 0xb4, 0x51, 0xf7, 0xd7, 0x5c, 0xfc, 0x51, 0x96 } }

class nsPIDOMWindow191 : public nsIDOMWindowInternal
{
public:
//  NS_DECLARE_STATIC_IID_ACCESSOR(NS_PIDOMWINDOW_IID)
  static const nsIID& GetIID() {static const nsIID iid = NS_PIDOMWINDOW_191_IID; return iid;} 

  virtual nsPIDOMWindow191* GetPrivateRoot() = 0;

  // This is private because activate/deactivate events are not part
  // of the DOM spec.
  virtual nsresult Activate() = 0;
  virtual nsresult Deactivate() = 0;

  nsISupports* GetChromeEventHandler() const;

  virtual void SetChromeEventHandler(nsISupports* aChromeEventHandler) = 0;

  PRBool HasMutationListeners(PRUint32 aMutationEventType) const;

  void SetMutationListeners(PRUint32 aType);

  virtual nsISupports* GetRootFocusController() = 0;

  // GetExtantDocument provides a backdoor to the DOM GetDocument accessor
  nsIDOMDocument* GetExtantDocument() const;

  // Internal getter/setter for the frame element, this version of the
  // getter crosses chrome boundaries whereas the public scriptable
  // one doesn't for security reasons.
  nsIDOMElement* GetFrameElementInternal() const;

  void SetFrameElementInternal(nsIDOMElement *aFrameElement);

  PRBool IsLoadingOrRunningTimeout() const;

  // Check whether a document is currently loading
  PRBool IsLoading() const;

  PRBool IsHandlingResizeEvent() const;

  // Tell this window who opened it.  This only has an effect if there is
  // either no document currently in the window or if the document is the
  // original document this window came with (an about:blank document either
  // preloaded into it when it was created, or created by
  // CreateAboutBlankContentViewer()).
  virtual void SetOpenerScriptPrincipal(nsISupports* aPrincipal) = 0;
  // Ask this window who opened it.
  virtual nsISupports* GetOpenerScriptPrincipal() = 0;

  virtual PopupControlState191 PushPopupControlState(PopupControlState191 aState,
                                                  PRBool aForce) const = 0;
  virtual void PopPopupControlState(PopupControlState191 state) const = 0;
  virtual PopupControlState191 GetPopupControlState() const = 0;

  // Returns an object containing the window's state.  This also suspends
  // all running timeouts in the window.
  virtual nsresult SaveWindowState(nsISupports **aState) = 0;

  // Restore the window state from aState.
  virtual nsresult RestoreWindowState(nsISupports *aState) = 0;

  // Suspend timeouts in this window and in child windows.
  virtual void SuspendTimeouts(PRUint32 aIncrease = 1,
                               PRBool aFreezeChildren = PR_TRUE) = 0;

  // Resume suspended timeouts in this window and in child windows.
  virtual nsresult ResumeTimeouts(PRBool aThawChildren = PR_TRUE) = 0;

  virtual PRUint32 TimeoutSuspendCount() = 0;

  // Fire any DOM notification events related to things that happened while
  // the window was frozen.
  virtual nsresult FireDelayedDOMEvents() = 0;

  virtual PRBool IsFrozen() const = 0;

  // Add a timeout to this window.
  virtual nsresult SetTimeoutOrInterval(nsISupports* aHandler,
                                        PRInt32 interval,
                                        PRBool aIsInterval, PRInt32 *aReturn) = 0;

  // Clear a timeout from this window.
  virtual nsresult ClearTimeoutOrInterval(PRInt32 aTimerID) = 0;

  nsPIDOMWindow191 *GetOuterWindow();

  nsPIDOMWindow191 *GetCurrentInnerWindow() const
  {
    return mInnerWindow;
  }

  nsPIDOMWindow191 *EnsureInnerWindow();

  PRBool IsInnerWindow() const;

  PRBool IsOuterWindow() const;

  virtual PRBool WouldReuseInnerWindow(nsISupports *aNewDocument) = 0;

  /**
   * Get the docshell in this window.
   */
  nsIDocShell *GetDocShell();

  /**
   * Set or unset the docshell in the window.
   */
  virtual void SetDocShell(nsIDocShell *aDocShell) = 0;

  /**
   * Set a new document in the window. Calling this method will in
   * most cases create a new inner window. If this method is called on
   * an inner window the call will be forewarded to the outer window,
   * if the inner window is not the current inner window an
   * NS_ERROR_NOT_AVAILABLE error code will be returned. This may be
   * called with a pointer to the current document, in that case the
   * document remains unchanged, but a new inner window will be
   * created.
   */
  virtual nsresult SetNewDocument(nsISupports *aDocument,
                                  nsISupports *aState,
                                  PRBool aClearScope) = 0;

  /**
   * Set the opener window.  aOriginalOpener is true if and only if this is the
   * original opener for the window.  That is, it can only be true at most once
   * during the life cycle of a window, and then only the first time
   * SetOpenerWindow is called.  It might never be true, of course, if the
   * window does not have an opener when it's created.
   */
  virtual void SetOpenerWindow(nsISupports *aOpener,
                               PRBool aOriginalOpener) = 0;

  virtual void EnsureSizeUpToDate() = 0;

  /**
   * Callback for notifying a window about a modal dialog being
   * opened/closed with the window as a parent.
   */
  virtual void EnterModalState() = 0;
  virtual void LeaveModalState() = 0;

  void SetModalContentWindow(PRBool aIsModalContentWindow);

  PRBool IsModalContentWindow() const;

  /**
   * Call this to indicate that some node (this window, its document,
   * or content in that document) has a paint event listener.
   */
  void SetHasPaintEventListeners();

  /**
   * Call this to check whether some node (this window, its document,
   * or content in that document) has a paint event listener.
   */
  PRBool HasPaintEventListeners();
  
  /**
   * Initialize window.java and window.Packages, and start LiveConnect
   * if we're running with a non-NPRuntime enabled Java plugin.
   */
  virtual void InitJavaProperties() = 0;

  virtual void* GetCachedXBLPrototypeHandler(nsISupports* aKey) = 0;
  virtual void CacheXBLPrototypeHandler(nsISupports* aKey,
                                        nsISupports& aHandler) = 0;

protected:
  // The nsPIDOMWindow constructor. The aOuterWindow argument should
  // be null if and only if the created window itself is an outer
  // window. In all other cases aOuterWindow should be the outer
  // window for the inner window that is being created.
  nsPIDOMWindow191(nsPIDOMWindow191 *aOuterWindow);

  void SetChromeEventHandlerInternal(nsISupports* aChromeEventHandler);

  // These two variables are special in that they're set to the same
  // value on both the outer window and the current inner window. Make
  // sure you keep them in sync!
  nsCOMPtr<nsISupports> mChromeEventHandler; // strong
  nsCOMPtr<nsIDOMDocument> mDocument; // strong

  // These members are only used on outer windows.
  nsCOMPtr<nsIDOMElement> mFrameElement;
  nsIDocShell           *mDocShell;  // Weak Reference

  PRUint32               mModalStateDepth;

  // These variables are only used on inner windows.
  nsISupports            *mRunningTimeout;

  PRUint32               mMutationBits;

  PRPackedBool           mIsDocumentLoaded;
  PRPackedBool           mIsHandlingResizeEvent;
  PRPackedBool           mIsInnerWindow;
  PRPackedBool           mMayHavePaintEventListener;

  // This variable is used on both inner and outer windows (and they
  // should match).
  PRPackedBool           mIsModalContentWindow;

  // And these are the references between inner and outer windows.
  nsPIDOMWindow191         *mInnerWindow;
  nsPIDOMWindow191         *mOuterWindow;
};


/*NS_DEFINE_STATIC_IID_ACCESSOR(nsPIDOMWindow, NS_PIDOMWINDOW_IID)*/

#endif // nsPIDOMWindow_h__
