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

import com.google.gdt.eclipse.designer.hosted.HostedModeException;
import com.google.gdt.eclipse.designer.hosted.tdz.BrowserShell;
import com.google.gdt.eclipse.designer.webkit.jsni.LowLevelWebKit;
import com.google.gdt.eclipse.designer.webkit.jsni.ModuleSpaceWebKit;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.shell.JsValue;
import com.google.gwt.dev.shell.ModuleSpace;
import com.google.gwt.dev.shell.ModuleSpaceHost;

/**
 * BrowserShell implementation which uses WebKit as native system window.
 * 
 * @author mitin_aa
 */
public final class BrowserShellWebKit<H extends Number> extends BrowserShell {
	static {
		String libname = "wbp-gwt-webkit";
		try {
			System.loadLibrary(libname);
			LowLevelWebKit.init();
		} catch (Throwable e) {
			throw new HostedModeException(HostedModeException.NATIVE_LIBS_LOADING_ERROR,
				e,
				new String[]{libname});
		}
	}
	private final IBrowserShellWebKitImpl m_impl;
	private String m_moduleName;
	private WebKitInitializer<H> m_helper;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor 
	//
	////////////////////////////////////////////////////////////////////////////
	public BrowserShellWebKit(IBrowserShellWebKitImpl impl) {
		m_impl = impl;
		m_helper = new WebKitInitializer<H>();
		m_impl.create(m_helper);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// BrowserShell 
	//
	////////////////////////////////////////////////////////////////////////////
	public void setUrl(String url, String moduleName, int timeout, Runnable messageProcessor)
			throws Exception {
		m_moduleName = moduleName;
		checkWidget();
		// wait for browser to prepare
		m_impl.setUrl(url);
		m_helper.wait(timeout, messageProcessor);
		TreeLogger logger = getHost().getLogger();
		// load module
		ModuleSpaceHost msh = getHost().createModuleSpaceHost(m_moduleName);
		@SuppressWarnings("unchecked")
		ModuleSpace moduleSpace =
				new ModuleSpaceWebKit<H>(logger,
					msh,
					m_helper.getScriptObject(),
					(H) LowLevelWebKit.getCurrentJsContext(),
					m_moduleName);
		attachModuleSpace(moduleSpace);
		// no need more, let GC to possibly release native resources
		m_helper = null;
	}
	@Override
	public void dispose() {
		// force disposing any pending JsValue instances
		JsValue.mainThreadCleanup();
		System.runFinalization();
		System.gc();
		System.runFinalization();
		System.gc();
		super.dispose();
		m_impl.dispose();
	}
	public boolean isDisposed() {
		return m_impl.isDisposed();
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
		return m_impl.computeTrim(x, y, width, height);
	}
	public void setBounds(Rectangle bounds) {
		checkWidget();
		m_impl.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	public Rectangle getBounds() {
		checkWidget();
		return m_impl.getBounds();
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
		m_impl.showAsPreview();
	}
	public void prepare() {
		m_impl.prepare();
	}
	public Image createBrowserScreenshot() throws Exception {
		return m_impl.makeShot();
	}
	public String getUserAgentString() {
		return "safari";
	}
}
