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
package com.google.gdt.eclipse.designer.webkit;

import com.google.gdt.eclipse.designer.hosted.HostedModeException;

import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


/**
 * Abstract Windows implementation of the WebKit-driven BrowserShell.
 * 
 * @author mitin_aa
 */
public abstract class BrowserShellWebKitImplWin32<H extends Number> implements IBrowserShellWebKitImpl {
	private H m_handle;
	////////////////////////////////////////////////////////////////////////////
	//
	// Initializing
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean m_initialized = false;
	/**
	 * Loads shared lib and does some initializing.
	 */
	static void init() {
		if (!m_initialized) {
			String libname = "wbp-gwt-webkit-bs";
			try {
				System.loadLibrary(libname);
				_init(WebKitInitializer.class);
			} catch (Throwable e) {
				throw new HostedModeException(HostedModeException.NATIVE_LIBS_LOADING_ERROR,
					e,
					new String[]{libname});
			}
			m_initialized = true;
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// IBrowserShellWebKitImpl 
	//
	////////////////////////////////////////////////////////////////////////////	
	public void create(Object callback) {
		init();
		m_handle = _create(callback);
	}
	public void dispose() {
		if (m_handle != null) {
			_release(m_handle);
			m_handle = null;
		}
	}
	public void showAsPreview() {
		Shell eclipseShell = getEclipseShell();
		eclipseShell.setEnabled(false);
		Display display = Display.getCurrent();
		try {
			_showAsPreview(m_handle, getEclipseShellHandle(eclipseShell));
			while (_isVisible(m_handle)) {
				try {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				} catch (Throwable e) {
				}
			}
		} catch (Throwable e) {
			// ignore
		} finally {
			eclipseShell.setEnabled(true);
			eclipseShell.forceActive();
		}
	}
	private Shell getEclipseShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
	public Rectangle computeTrim(int x, int y, int width, int height) {
		short[] bounds = new short[]{(short) x, (short) y, (short) width, (short) height};
		_computeTrim(m_handle, bounds);
		return new Rectangle(bounds[0], bounds[1], bounds[2], bounds[3]);
	}
	public Image makeShot() throws Exception {
		return createImageFromHandle(_makeShot(m_handle));
	}
	public void setUrl(String url) {
		_setUrl(m_handle, url);
	}
	public boolean isDisposed() {
		return m_handle == null;
	}
	public void setBounds(int x, int y, int width, int height) {
		_setBounds(m_handle, x, y, width, height);
	}
	public Rectangle getBounds() {
		short[] bounds = new short[4];
		_getBounds(m_handle, bounds);
		return new Rectangle(bounds[0], bounds[1], bounds[2], bounds[3]);
	}
	public void prepare() {
	}
	////////////////////////////////////////////////////////////////////////////
	//
	//	Abstract methods 
	//
	////////////////////////////////////////////////////////////////////////////
	protected abstract Image createImageFromHandle(H handle) throws Exception;
	protected abstract H getEclipseShellHandle(Shell shell) throws Exception;
	////////////////////////////////////////////////////////////////////////////
	//
	//	Native methods 
	//
	////////////////////////////////////////////////////////////////////////////
	private static native void _init(Class<?> callbackClass);
	private static native <H extends Number> H _create(Object callback);
	private static native <H extends Number> void _release(H handle);
	private static native <H extends Number> void _setUrl(H handle, String url);
	private static native <H extends Number> void _setBounds(H handle, int x, int y, int width, int height);
	private static native <H extends Number> void _getBounds(H handle, short[] bounds);
	private static native <H extends Number> void _computeTrim(H handle, short[] bounds);
	private static native <H extends Number> boolean _isVisible(H handle);
	private static native <H extends Number> H _makeShot(H handle);
	private static native <H extends Number> void _showAsPreview(H handle, H shellHandle);
	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation 
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * 32-bit Windows implementation of the WebKit-driven BrowserShell.
	 */
	public static IBrowserShellWebKitImpl newImpl32() {
		return new BrowserShellWebKitImplWin32<Integer>() {
			@Override
			public Image createImageFromHandle(Integer handle) throws Exception {
				/*
				 Use reflection for
				 Image image = Image.win32_new(null, SWT.BITMAP, handle.intValue());
				 */
				Method win32_newMethod = Image.class.getDeclaredMethod("win32_new", new Class[]{Device.class, int.class, int.class});
				Image image = (Image) win32_newMethod.invoke(null, new Object[]{null, SWT.BITMAP, handle.intValue()});
				return image;
			}
			@Override
			protected Integer getEclipseShellHandle(Shell shell) throws Exception {
				return shell.getClass().getField("handle").getInt(shell);
			}
		};
	}
	/**
	 * 64-bit Windows implementation of the WebKit-driven BrowserShell.
	 */
	public static IBrowserShellWebKitImpl newImpl64() {
		return new BrowserShellWebKitImplWin32<Long>() {
			@Override
			public Image createImageFromHandle(Long handle) throws Exception {
				/*
				 Use reflection for
				 Image image = Image.win32_new(null, SWT.BITMAP, handle.longValue());
				 */
				Method win32_newMethod = Image.class.getDeclaredMethod("win32_new", new Class[]{Device.class, int.class, long.class});
				Image image = (Image) win32_newMethod.invoke(null, new Object[]{null, SWT.BITMAP, handle.longValue()});
				return image;
			}
			@Override
			protected Long getEclipseShellHandle(Shell shell) throws Exception {
				return shell.getClass().getField("handle").getLong(shell);
			}
		};
	}
}
