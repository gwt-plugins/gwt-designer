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
package com.google.gdt.eclipse.designer.ie;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.internal.ole.win32.COM;
import org.eclipse.swt.internal.ole.win32.IDispatch;
import org.eclipse.swt.ole.win32.Variant;

import com.google.gdt.eclipse.designer.hosted.HostedModeException;
import com.google.gdt.eclipse.designer.ie.jsni.IDispatchImpl;
import com.google.gdt.eclipse.designer.ie.jsni.SwtOleGlue;

/**
 * Performs Browser initializing procedures (gets 'window' script object).
 * 
 * @author mitin_aa
 */
final class IEInitializer {
  IDispatch m_window;
  private final External m_external;
  private final Browser m_browser;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public IEInitializer(Browser browser) {
    m_browser = browser;
    m_external = new External();
    // Expose a 'window.external' object. This object's onLoad() method will
    // be called when a hosted mode application's wrapper HTML is done loading.
    //
    SwtOleGlue.injectBrowserScriptExternalObject(m_browser, m_external);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void wait(int timeout, Runnable messageProcessor) {
    // wait for browser to fire progress event
    long startTime = System.currentTimeMillis();
    // wait for browser to init no more than timeout
    while (m_window == null && System.currentTimeMillis() - startTime < timeout) {
      messageProcessor.run();
    }
    if (m_window == null) {
      throw new HostedModeException(HostedModeException.GWT_INIT_TIMEOUT);
    }
  }

  public void dispose() {
    SwtOleGlue.ejectBrowserScriptExternalObject(m_browser);
    m_external.Release();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // External
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * IDispatch implementation of the window.external object.
   */
  public class External extends IDispatchImpl {
    /**
     * Called by the loaded HTML page to activate a new module.
     * 
     * @param frameWnd
     *          a reference to the IFRAME in which the module's injected JavaScript will live
     */
    public boolean gwtOnLoad(IDispatch frameWnd) {
      m_window = frameWnd;
      return true;
    }

    @Override
    protected void getIDsOfNames(String[] names, int[] ids) throws HResultException {
      if (names.length >= 2) {
        throw new HResultException(DISP_E_UNKNOWNNAME);
      }
      String name = names[0].toLowerCase();
      if (name.equals("gwtonload")) {
        ids[0] = 1;
        return;
      }
      throw new HResultException(DISP_E_UNKNOWNNAME);
    }

    @Override
    protected Variant invoke(int dispId, int flags, Variant[] params) throws HResultException,
        InvocationTargetException {
      if (dispId == 0 && (flags & COM.DISPATCH_PROPERTYGET) != 0) {
        // MAGIC: this is the default property, let's just do toString()
        return new Variant(toString());
      } else if (dispId == 1) {
        if ((flags & COM.DISPATCH_METHOD) != 0) {
          try {
            IDispatch frameWnd =
                params[0].getType() == COM.VT_DISPATCH ? params[0].getDispatch() : null;
            boolean success = gwtOnLoad(frameWnd);
            // boolean return type
            return new Variant(success);
          } catch (SWTException e) {
            throw new HResultException(COM.E_INVALIDARG);
          }
        } else if ((flags & COM.DISPATCH_PROPERTYGET) != 0) {
          // property get on the method itself
          try {
            IDispatchImpl funcObj = new IDispatchImpl() {
              // do nothing, just return some IDispatch
              @Override
              protected Variant invoke(int dispId, int flags, Variant[] params)
                  throws HResultException, InstantiationException, InvocationTargetException {
                return new Variant();
              }

              @Override
              protected void getIDsOfNames(String[] names, int[] ids) throws HResultException {
              }
            };
            IDispatch disp = new IDispatch(funcObj.getAddress());
            disp.AddRef();
            return new Variant(disp);
          } catch (Exception e) {
            // just return VT_EMPTY
            return new Variant();
          }
        }
        throw new HResultException(COM.E_NOTSUPPORTED);
      }
      // The specified member id is out of range.
      throw new HResultException(COM.DISP_E_MEMBERNOTFOUND);
    }
  }
}
