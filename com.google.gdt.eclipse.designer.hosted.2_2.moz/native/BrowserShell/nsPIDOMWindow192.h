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


#ifndef nsPIDOMWindow192_h__
#define nsPIDOMWindow192_h__

#include "nsISupports.h"
#include "nsIDOMLocation.h"
#include "nsIDOMElement.h"
#include "nsIDOMWindowInternal.h"
#include "nsIDOMDocument.h"
#include "nsCOMPtr.h"
#include "nsEvent.h"

//#define DOM_WINDOW_DESTROYED_TOPIC "dom-window-destroyed"

//class nsIPrincipal;

// Popup control state enum. The values in this enum must go from most
// permissive to least permissive so that it's safe to push state in
// all situations. Pushing popup state onto the stack never makes the
// current popup state less permissive (see
// nsGlobalWindow::PushPopupControlState()).
enum PopupControlState192 {
  openAllowed192 = 0,  // open that window without worries
  openControlled192,   // it's a popup, but allow it
  openAbused192,       // it's a popup. disallow it, but allow domain override.
  openOverridden192    // disallow window open
};

class nsIDocShell;

#define NS_PIDOMWINDOW_192_IID \
{ 0x70c9f57f, 0xf7b3, 0x4a37, \
  { 0xbe, 0x36, 0xbb, 0xb2, 0xd7, 0xe9, 0x40, 0x13 } }

class nsPIDOMWindow192 : public nsIDOMWindowInternal
{
public:
//  NS_DECLARE_STATIC_IID_ACCESSOR(NS_PIDOMWINDOW_IID)
  static const nsIID& GetIID() {static const nsIID iid = NS_PIDOMWINDOW_192_IID; return iid;}

  virtual nsPIDOMWindow192* GetPrivateRoot() = 0;

  virtual void ActivateOrDeactivate(PRBool aActivate) = 0;

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

  virtual PopupControlState192 PushPopupControlState(PopupControlState192 aState,
                                                  PRBool aForce) const = 0;
  virtual void PopPopupControlState(PopupControlState192 state) const = 0;
  virtual PopupControlState192 GetPopupControlState() const = 0;

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
  virtual nsresult SetTimeoutOrInterval(nsISupports *aHandler,
                                        PRInt32 interval,
                                        PRBool aIsInterval, PRInt32 *aReturn) = 0;

  // Clear a timeout from this window.
  virtual nsresult ClearTimeoutOrInterval(PRInt32 aTimerID) = 0;

  nsPIDOMWindow192 *GetOuterWindow();

  nsPIDOMWindow192 *GetCurrentInnerWindow() const
  {
    return mInnerWindow;
  }

  nsPIDOMWindow192 *EnsureInnerWindow();

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
   * Initialize window.java and window.Packages.
   */
  virtual void InitJavaProperties() = 0;

  virtual void* GetCachedXBLPrototypeHandler(nsISupports* aKey) = 0;
  virtual void CacheXBLPrototypeHandler(nsISupports* aKey,
                                        nsISupports& aHandler) = 0;

  /*
   * Get and set the currently focused element within the document. If
   * aNeedsFocus is true, then set mNeedsFocus to true to indicate that a
   * document focus event is needed.
   *
   * DO NOT CALL EITHER OF THESE METHODS DIRECTLY. USE THE FOCUS MANAGER
   * INSTEAD.
   */
  virtual nsISupports* GetFocusedNode() = 0;
  virtual void SetFocusedNode(nsISupports* aNode,
                              PRUint32 aFocusMethod = 0,
                              PRBool aNeedsFocus = PR_FALSE) = 0;

  /**
   * Retrieves the method that was used to focus the current node.
   */
  virtual PRUint32 GetFocusMethod() = 0;

  /*
   * Tells the window that it now has focus or has lost focus, based on the
   * state of aFocus. If this method returns true, then the document loaded
   * in the window has never received a focus event and expects to receive
   * one. If false is returned, the document has received a focus event before
   * and should only receive one if the window is being focused.
   *
   * aFocusMethod may be set to one of the focus method constants in
   * nsIFocusManager to indicate how focus was set.
   */
  virtual PRBool TakeFocus(PRBool aFocus, PRUint32 aFocusMethod) = 0;

  /**
   * Indicates that the window may now accept a document focus event. This
   * should be called once a document has been loaded into the window.
   */
  virtual void SetReadyForFocus() = 0;

  /**
   * Indicates that the page in the window has been hidden. This is used to
   * reset the focus state.
   */
  virtual void PageHidden() = 0;

  /**
   * Instructs this window to asynchronously dispatch a hashchange event.  This
   * method must be called on an inner window.
   */
  virtual nsresult DispatchAsyncHashchange() = 0;


  /**
   * Tell this window that there is an observer for orientation changes
   */
  virtual void SetHasOrientationEventListener() = 0;

  /**
   * Set a arguments for this window. This will be set on the window
   * right away (if there's an existing document) and it will also be
   * installed on the window when the next document is loaded. Each
   * language impl is responsible for converting to an array of args
   * as appropriate for that language.
   */
  virtual nsresult SetArguments(nsIArray *aArguments, nsISupports *aOrigin) = 0;

protected:
  // The nsPIDOMWindow constructor. The aOuterWindow argument should
  // be null if and only if the created window itself is an outer
  // window. In all other cases aOuterWindow should be the outer
  // window for the inner window that is being created.
  nsPIDOMWindow192(nsPIDOMWindow192 *aOuterWindow);

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
  nsISupports             *mRunningTimeout;

  PRUint32               mMutationBits;

  PRPackedBool           mIsDocumentLoaded;
  PRPackedBool           mIsHandlingResizeEvent;
  PRPackedBool           mIsInnerWindow;
  PRPackedBool           mMayHavePaintEventListener;

  // This variable is used on both inner and outer windows (and they
  // should match).
  PRPackedBool           mIsModalContentWindow;

  // And these are the references between inner and outer windows.
  nsPIDOMWindow192         *mInnerWindow;
  nsPIDOMWindow192         *mOuterWindow;
};


//NS_DEFINE_STATIC_IID_ACCESSOR(nsPIDOMWindow, NS_PIDOMWINDOW_IID)

//#ifdef _IMPL_NS_LAYOUT
//PopupControlState
//PushPopupControlState(PopupControlState aState, PRBool aForce);
//
//void
//PopPopupControlState(PopupControlState aState);
//
//#define NS_AUTO_POPUP_STATE_PUSHER nsAutoPopupStatePusherInternal
//#else
//#define NS_AUTO_POPUP_STATE_PUSHER nsAutoPopupStatePusherExternal
//#endif
//
// Helper class that helps with pushing and popping popup control
// state. Note that this class looks different from within code that's
// part of the layout library than it does in code outside the layout
// library.  We give the two object layouts different names so the symbols
// don't conflict, but code should always use the name
// |nsAutoPopupStatePusher|.
//class NS_AUTO_POPUP_STATE_PUSHER
//{
//public:
//#ifdef _IMPL_NS_LAYOUT
//  NS_AUTO_POPUP_STATE_PUSHER(PopupControlState aState, PRBool aForce = PR_FALSE)
//    : mOldState(::PushPopupControlState(aState, aForce))
//  {
//  }
//
//  ~NS_AUTO_POPUP_STATE_PUSHER()
//  {
//    PopPopupControlState(mOldState);
//  }
//#else
//  NS_AUTO_POPUP_STATE_PUSHER(nsPIDOMWindow *aWindow, PopupControlState aState)
//    : mWindow(aWindow), mOldState(openAbused)
//  {
//    if (aWindow) {
//      mOldState = aWindow->PushPopupControlState(aState, PR_FALSE);
//    }
//  }
//
//  ~NS_AUTO_POPUP_STATE_PUSHER()
//  {
//    if (mWindow) {
//      mWindow->PopPopupControlState(mOldState);
//    }
//  }
//#endif
//
//protected:
//#ifndef _IMPL_NS_LAYOUT
//  nsCOMPtr<nsPIDOMWindow> mWindow;
//#endif
//  PopupControlState mOldState;
//
//private:
//  // Hide so that this class can only be stack-allocated
//  static void* operator new(size_t /*size*/) CPP_THROW_NEW { return nsnull; }
//  static void operator delete(void* /*memory*/) {}
//};

// #define nsAutoPopupStatePusher NS_AUTO_POPUP_STATE_PUSHER

#endif // nsPIDOMWindow_h__
