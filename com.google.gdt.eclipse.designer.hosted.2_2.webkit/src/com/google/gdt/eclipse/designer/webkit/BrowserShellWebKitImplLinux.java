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

import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.internal.core.DesignerPlugin;

/**
 * Abstract Linux implementation of the WebKit-driven BrowserShell.
 * 
 * @author mitin_aa
 */
public abstract class BrowserShellWebKitImplLinux<H extends Number> implements IBrowserShellWebKitImpl {
	private H m_handle;
	////////////////////////////////////////////////////////////////////////////
	//
	// Initializing
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean m_available = false;
	private static boolean m_initialized = false;
	/**
	 * Loads shared lib and does some initializing.
	 */
	private static void init() {
		if (!m_initialized) {
			String libname = "wbp-gwt-webkit-bs";
			try {
		    	try {
		    		System.loadLibrary(libname);
		    	} catch (Throwable ex1) {
	    			// attempt to load lib linked against older webkit shared lib name
	    			System.loadLibrary(libname + "0");
		    	}
				m_available = _isAvailable();
				if (m_available) {
					_init(WebKitInitializer.class);
				}
			} catch (Throwable e) {
				// ignore exception, fall-back to Mozilla
			}
			m_initialized = true;
		}
	}
	/**
	 * @return <code>true</code> if WebKit could be used.
	 */
	public static boolean isAvailable() {
		init();
		return m_available;
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
		getEclipseShell().setEnabled(false);
		try {
			Display display = Display.getCurrent();
			// 'showAsPreview' does widget reparenting, so save current bounds
			Rectangle bounds = getBounds();
			H newWindow = _showAsPreview(m_handle);
			while (_isVisible(newWindow)) {
				try {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				} catch (Throwable e) {
				}
			}
			// restore bounds
			setBounds(0, 0, bounds.width, bounds.height);
			if (!display.isDisposed()) {
				display.update();
				// proceed outstanding events
				while(display.readAndDispatch());
			}
		} finally {
			getEclipseShell().setEnabled(true);
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
		Rectangle bounds = getBounds();
		if (bounds.width != width || bounds.height != height) {
			_setBounds(m_handle, x, y, width, height);
		}
	}
	public Rectangle getBounds() {
		short[] bounds = new short[4];
		_getBounds(m_handle, bounds);
		return new Rectangle(bounds[0], bounds[1], bounds[2], bounds[3]);
	}
	public void prepare() {
		/**
		 * WebKit/GTK 1.8.0 draws itself from backing store and does not do layout on expose events.
		 * I wasn't able to find a way to get notified when new layout is done.
		 * So, just wait in event loop.
		 * See http://code.google.com/p/google-web-toolkit/issues/detail?id=7232  
		 */
		for (int i = 0; i < 10; i++) {
			DesignerPlugin.getStandardDisplay().readAndDispatch();
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	//	Abstract methods 
	//
	////////////////////////////////////////////////////////////////////////////
	protected abstract Image createImageFromHandle(H handle) throws Exception;
	////////////////////////////////////////////////////////////////////////////
	//
	//	Native methods 
	//
	////////////////////////////////////////////////////////////////////////////
	private static native void _init(Class<?> callbackClass);
	private static native <H extends Number> H _create(Object callback);
	private static native <H extends Number> void _release(H handle);
	private static native <H extends Number> void _setVisible(H handle, boolean visible);
	private static native <H extends Number> void _setUrl(H handle, String url);
	private static native <H extends Number> void _setBounds(H handle, int x, int y, int width, int height);
	private static native <H extends Number> void _getBounds(H handle, short[] bounds);
	private static native <H extends Number> void _computeTrim(H handle, short[] bounds);
	private static native <H extends Number> boolean _isVisible(H handle);
	private static native <H extends Number> H _makeShot(H handle);
	private static native <H extends Number> H _showAsPreview(H handle);
	private static native boolean _isAvailable();
	private static native <H extends Number> void _g_object_unref(H handle);
	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation 
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * 32-bit Linux implementation of the WebKit-driven BrowserShell.
	 */
	public static IBrowserShellWebKitImpl newImpl32() {
		return new BrowserShellWebKitImplLinux<Integer>() {
			@Override
			public Image createImageFromHandle(Integer handle) throws Exception {
				if (handle != null) {
					// use reflection
					Method gtk_new = Image.class.getDeclaredMethod("gtk_new", new Class[] { Device.class, int.class, int.class, int.class });
					return (Image) gtk_new.invoke(null, new Object[] { null, SWT.BITMAP, handle, 0 });
				}
				// fallback
				return new Image(null, 1, 1);
			}
		};
	}
	/**
	 * 64-bit Linux implementation of the WebKit-driven BrowserShell.
	 */
	public static IBrowserShellWebKitImpl newImpl64() {
		return new BrowserShellWebKitImplLinux<Long>() {
			@Override
			public Image createImageFromHandle(Long handle) throws Exception {
				if (handle != null) {
					// use reflection
					Method gtk_new = Image.class.getDeclaredMethod("gtk_new", new Class[] { Device.class, int.class, long.class, long.class });
					return (Image) gtk_new.invoke(null, new Object[] { null, SWT.BITMAP, handle, 0 });
				}
				// fallback
				return new Image(null, 1, 1);
			}
		};
	}
}
