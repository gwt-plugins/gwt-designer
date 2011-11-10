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
package com.google.gdt.eclipse.designer.webkit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import com.google.gdt.eclipse.designer.hosted.HostedModeException;
import com.google.gdt.eclipse.designer.hosted.tdt.BrowserShell;
import com.google.gdt.eclipse.designer.webkit.jsni.LowLevelWebKit;
import com.google.gdt.eclipse.designer.webkit.jsni.ModuleSpaceWebKit;
import com.google.gwt.dev.shell.designtime.JsValue;
import com.google.gwt.dev.shell.designtime.ModuleSpace;

/**
 * BrowserShell implementation which uses WebKit as native system window.
 * 
 * @author mitin_aa
 */
public final class BrowserShellWebKit<H extends Number> extends BrowserShell {
  static {
    String libname = "wbp-gwt-webkit";
    try {
    	try {
    		System.loadLibrary(libname);
    	} catch (Throwable ex1) {
    		if (EnvironmentUtils.IS_LINUX) {
    			// attempt to load lib linked against older webkit shared lib name
    			System.loadLibrary(libname + "0");
    		} else {
    			throw ex1;
    		}
    	}
      LowLevelWebKit.init();
    } catch (Throwable e) {
      throw new HostedModeException(HostedModeException.NATIVE_LIBS_LOADING_ERROR,
        e,
        new String[]{libname});
    }
  }
  private final IBrowserShellWebKitImpl impl;
  private WebKitInitializer<H> helper;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor 
  //
  ////////////////////////////////////////////////////////////////////////////
  public BrowserShellWebKit(IBrowserShellWebKitImpl impl) {
    this.impl = impl;
    helper = new WebKitInitializer<H>();
    this.impl.create(helper);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // BrowserShell 
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public void setUrl(String url, String moduleName, int timeout, Runnable messageProcessor)
      throws Exception {
    checkWidget();
    // wait for browser to prepare
    this.impl.setUrl(url);
    helper.wait(timeout, messageProcessor);
    this.moduleSpace = new ModuleSpaceWebKit<H>(getHost(),
      helper.getScriptObject(),
      (H) LowLevelWebKit.getCurrentJsContext(),
      moduleName);
    // no need more, let GC to possibly release native resources
    helper = null;
    getModuleSpace().onLoad();
  }

  @Override
  public void dispose() {
    // force disposing any pending JsValue instances
    JsValue.mainThreadCleanup();
    try {
      ModuleSpace space = getModuleSpace();
      if (space != null) {
        space.dispose();
      }
    } catch (Throwable e) {
      ReflectionUtils.propagate(e);
    }
    super.dispose();
    impl.dispose();
  }

  public boolean isDisposed() {
    return impl.isDisposed();
  }

  private void checkWidget() {
    if (isDisposed()) {
      SWT.error(SWT.ERROR_WIDGET_DISPOSED);
    }
  }

  public void setVisible(boolean visible) {
    // FIXME: Not used?
  }

  public Rectangle computeTrim(int x, int y, int width, int height) {
    checkWidget();
    return impl.computeTrim(x, y, width, height);
  }

  public void setBounds(Rectangle bounds) {
    checkWidget();
    impl.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
  }

  public Rectangle getBounds() {
    checkWidget();
    return impl.getBounds();
  }

  public void setSize(int width, int height) {
    Rectangle bounds = getBounds();
    bounds.width = width;
    bounds.height = height;
    setBounds(bounds);
  }

  public void setLocation(int x, int y) {
    Rectangle bounds = getBounds();
    bounds.x = x;
    bounds.y = y;
    setBounds(bounds);
  }

  public void showAsPreview() {
    impl.showAsPreview();
  }

  public void prepare() {
    impl.prepare();
  }

  public Image createBrowserScreenshot() throws Exception {
    return impl.makeShot();
  }

  public String getUserAgentString() {
    return "safari";
  }
}
