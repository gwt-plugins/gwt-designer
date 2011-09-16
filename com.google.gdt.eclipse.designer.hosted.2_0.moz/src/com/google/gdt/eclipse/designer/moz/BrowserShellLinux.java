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
package com.google.gdt.eclipse.designer.moz;
import java.lang.reflect.Field;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.google.gdt.eclipse.designer.hosted.HostedModeException;
import com.google.gdt.eclipse.designer.hosted.tdz.BrowserShell;
import com.google.gwt.dev.shell.JsValue;
/**
 * Browser shell implementation for Linux/Mozilla
 * 
 * @author mitin_aa
 */
public abstract class BrowserShellLinux extends BrowserShell {
	protected final Shell m_shell;
	protected final Browser m_browser;
	protected boolean m_isBrowser33;
	protected Object m_webBrowser;
	protected boolean m_unsupportedBrowserVersion; // set to true if unsupported version of Mozilla detected
	protected final Throwable[] m_exception = new Throwable[1];
	protected String m_moduleName;
	private String m_oldShellText;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BrowserShellLinux() {
		m_shell = new Shell();
		m_shell.setLayout(new FillLayout());
		m_shell.setLocation(-10000, -10000);
		// prevent close, hide instead
		m_shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				e.doit = false;
				Shell shell = (Shell) e.widget;
				shell.setVisible(false);
			}
		});
		try {
			m_browser = new Browser(m_shell, SWT.MOZILLA);
		} catch (Throwable e) {
			throw new HostedModeException(HostedModeException.LINUX_BROWSER_ERROR, e);
		}
		try {
			Field webBrowserField = m_browser.getClass().getDeclaredField("webBrowser");
			webBrowserField.setAccessible(true);
			m_webBrowser = webBrowserField.get(m_browser);
			m_isBrowser33 = m_webBrowser.getClass().getName().indexOf("Mozilla") != -1;
			hackOnStateChange();
		} catch (Throwable e) {
			throw new HostedModeException(HostedModeException.LINUX_GENERAL_INIT_ERROR, e);
		}
		String libname = "gwt-ll";
		try {
			System.loadLibrary(libname);
		} catch (Throwable e) {
			throw new HostedModeException(HostedModeException.NATIVE_LIBS_LOADING_ERROR,
				e,
				new String[]{libname});
		}
		m_browser.addProgressListener(new ProgressListener() {
			public void completed(ProgressEvent event) {
				try {
					fetchWindow();
				} catch (Throwable e) {
					m_exception[0] = e;
				}
			}
			public void changed(ProgressEvent event) {
			}
		});
		m_shell.layout();
	}
	/**
	 * There is incompatibility (bug?) between SWT 3.3 Browser and GWT 1.4 ('iframe'-based widgets/panels).
	 * SWT Browser installs some hooks into 'iframe' and these hooks may fired when 'iframe' instance 
	 * already deleted from DOM tree (DOM.removeChild). So this causes a crash.
	 * The workaround it to unhook these hooks just immediately after they hooked ;-) because we don't need any events.
	 * This is done by installing own XPCOM object instance.
	 * @throws Exception
	 */
	protected abstract void hackOnStateChange() throws Exception;
	////////////////////////////////////////////////////////////////////////////
	//
	// IBrowserShell
	//
	////////////////////////////////////////////////////////////////////////////
	public Rectangle computeTrim(int x, int y, int width, int height) {
		return m_shell.computeTrim(x, y, width, height);
	}
	@Override
	public void dispose() {
		// force disposing any pending JsValue instances
		JsValue.mainThreadCleanup();
		System.runFinalization();
		System.gc();
		System.runFinalization();
		System.gc();
		m_shell.dispose();
		super.dispose();
	}
	public boolean isDisposed() {
		return m_shell == null || m_shell.isDisposed();
	}
	public void setBounds(Rectangle bounds) {
		m_shell.setBounds(bounds);
	}
	public void setLocation(int x, int y) {
		m_shell.setLocation(x, y);
	}
	public void setSize(int width, int height) {
		m_shell.setSize(width, height);
	}
	public void setUrl(String url, String moduleName, int timeout, Runnable messageProcessor) throws Exception {
		m_moduleName = moduleName;
		m_browser.setUrl(url);
		long startTime = System.currentTimeMillis();
		// wait for browser to init no more than timeout
		while (!hasWindow() && !m_unsupportedBrowserVersion && System.currentTimeMillis() - startTime < timeout && m_exception[0] == null) {
			messageProcessor.run();
		}
		if (m_unsupportedBrowserVersion) {
			throw new HostedModeException(HostedModeException.LINUX_WRONG_MOZILLA_VER);
		}
		if (m_exception[0] != null) {
			if (m_exception[0] instanceof HostedModeException) {
				throw (HostedModeException) m_exception[0];
			}
			throw new HostedModeException(HostedModeException.MODULE_LOADING_ERROR, m_exception[0]);
		}
		if (!hasWindow()) {
			throw new HostedModeException(HostedModeException.GWT_INIT_TIMEOUT);
		}
	}
	public void setVisible(boolean visible) {
		try {
			m_shell.setVisible(visible);
		} catch (SWTException e) {
			// ignore low level SWT errors
		} catch (IllegalArgumentException e) {
			// ignore low level SWT errors
		}
	}
	public void showAsPreview() {
		getEclipseShell().setEnabled(false);
		try {
		try {
				Rectangle containerBounds = m_shell.getDisplay()
						.getClientArea();
			Rectangle shellBounds = m_shell.getBounds();
				int x = Math.max(0, containerBounds.x
						+ (containerBounds.width - shellBounds.width) / 2);
				int y = Math.max(0, containerBounds.y
						+ (containerBounds.height - shellBounds.height) / 3);
			m_shell.setSize(shellBounds.width, shellBounds.height);
			m_shell.setLocation(x, y);
		} catch (SWTException e) {
			// ignore low level SWT errors
		} catch (IllegalArgumentException e) {
			// ignore low level SWT errors
		}
			m_shell.setVisible(true);
			m_shell.setActive();
			Display display = Display.getCurrent();
			while (m_shell.isVisible()) {
				try {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				} catch (Throwable e) {
				}
			}
			if (!display.isDisposed()) {
				display.update();
				// proceed outstanding events
				while (display.readAndDispatch());
			}
		} finally {
			getEclipseShell().setEnabled(true);
		}
	}
	private Shell getEclipseShell() {
	  return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
	public void prepare() {
	}
	public String getUserAgentString() {
		return "gecko1_8";
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Abstract methods
	//
	////////////////////////////////////////////////////////////////////////////
	protected abstract boolean hasWindow();
	protected abstract void fetchWindow() throws Exception;
	////////////////////////////////////////////////////////////////////////////
	//
	// Other
	//
	////////////////////////////////////////////////////////////////////////////
	protected final void fixMozilla(Shell shell) {
		Rectangle bounds = shell.getBounds();
		shell.setSize(bounds.width - 1, bounds.height - 1);
		shell.redraw();
		shell.update();
		shell.setSize(bounds.width, bounds.height);
		shell.redraw();
		shell.update();
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Screenshot
	//
	////////////////////////////////////////////////////////////////////////////
	// TODO: rip this out when moved to D2
	protected void beginShot(Control control) {
		Shell shell = control.getShell();
		// setup key title to be used by compiz WM (if enabled)
		m_oldShellText = shell.getText();
		shell.setText("__wbp_preview_window");
	}
	protected void endShot(Control control) {
		Shell shell = control.getShell();
		shell.setText(m_oldShellText);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Troubleshooting
	//
	////////////////////////////////////////////////////////////////////////////
	protected final boolean isWorkaroundsDisabled() {
		return Boolean.parseBoolean(System.getProperty("__wbp.linux.disableScreenshotWorkarounds"));
	}
}
