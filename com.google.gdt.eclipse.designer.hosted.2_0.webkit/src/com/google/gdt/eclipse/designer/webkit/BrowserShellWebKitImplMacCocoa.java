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

import com.google.gdt.eclipse.designer.hosted.HostedModeException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;


public abstract class BrowserShellWebKitImplMacCocoa<H extends Number> implements IBrowserShellWebKitImpl {
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
	private static void init() {
		if (!m_initialized) {
			String libname = "wbp-gwt-webkit-bs-cocoa";
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
	public boolean isDisposed() {
		return m_handle == null;
	}
	public void setUrl(String url) {
		_setUrl(m_handle, url);
	}
	public void prepare() {
	}
	public void showAsPreview() {
		Display display = Display.getCurrent();
		// center shell
		Rectangle clientArea = display.getClientArea();
		Rectangle shellBounds = getBounds();
		int x = clientArea.x + (clientArea.width - shellBounds.width) / 2;
		int y = clientArea.y + (clientArea.height - shellBounds.height) / 2;
		setBounds(x, y, shellBounds.width, shellBounds.height);
		// set visible
		_setVisible(m_handle, true);
		while (_isVisible(m_handle)) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Throwable e) {
			}
		}
		if (!display.isDisposed()) {
			display.update();
		}
	}
	public Image makeShot() throws Exception {
		Object nsImage = createNSImageFromHandle(_makeShot(m_handle));
		if (nsImage != null) {
			// Create a temporary image using the captured image's handle
			Class<?> NSImageClass = Class.forName("org.eclipse.swt.internal.cocoa.NSImage");
			Method method =
					Image.class.getDeclaredMethod("cocoa_new", new Class[]{
							Device.class,
							int.class,
							NSImageClass});
			method.setAccessible(true);
			Image tempImage =
					(Image) method.invoke(null, new Object[]{
							Display.getCurrent(),
							new Integer(SWT.BITMAP),
							nsImage});
			// Create the result image
			Rectangle shellBounds = getBounds();
			Image image = new Image(Display.getCurrent(), shellBounds.width, shellBounds.height);
			// Manually copy because the image's data handle isn't available
			GC gc = new GC(tempImage);
			gc.copyArea(image, 0, 0);
			gc.dispose();
			// Dispose of the temporary image allocated in the native call
			tempImage.dispose();
			return image;
		}
		// prevent failing
		return new Image(Display.getCurrent(), 1, 1);
	}
	public Rectangle computeTrim(int x, int y, int width, int height) {
		short bounds[] = new short[]{(short) x, (short) y, (short) width, (short) height};
		_computeTrim(m_handle, bounds);
		return new Rectangle(bounds[0], bounds[1] += height - bounds[3], bounds[2], bounds[3]);
	}
	public void setBounds(int x, int y, int width, int height) {
		_setBounds(m_handle, x, y, width, height);
	}
	public Rectangle getBounds() {
		short[] bounds = new short[4];
		_getBounds(m_handle, bounds);
		return new Rectangle(bounds[0], bounds[1], bounds[2], bounds[3]);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	//	Abstract methods 
	//
	////////////////////////////////////////////////////////////////////////////
	protected abstract Object createNSImageFromHandle(H handle) throws Exception;
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
	private static native <H extends Number> H _makeShot(H handle);
	private static native <H extends Number> void _setVisible(H handle, boolean visible);
	private static native <H extends Number> boolean _isVisible(H handle);
	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation 
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * 32-bit Cocoa implementation of the WebKit-driven BrowserShell.
	 */
	public static IBrowserShellWebKitImpl newImpl32() {
		return new BrowserShellWebKitImplMacCocoa<Integer>() {
			@Override
			protected Object createNSImageFromHandle(Integer handle) throws Exception {
				if (handle != null) {
					Class<?> nsImageClass = Class.forName("org.eclipse.swt.internal.cocoa.NSImage");
					Constructor<?> constructor = nsImageClass.getConstructor(int.class);
					return constructor.newInstance(handle);
				}
				return null;
			}
		};
	}
	/**
	 * 64-bit Cocoa implementation of the WebKit-driven BrowserShell.
	 */
	public static IBrowserShellWebKitImpl newImpl64() {
		return new BrowserShellWebKitImplMacCocoa<Long>() {
			@Override
			protected Object createNSImageFromHandle(Long handle) throws Exception {
				if (handle != null) {
					Class<?> nsImageClass = Class.forName("org.eclipse.swt.internal.cocoa.NSImage");
					Constructor<?> constructor = nsImageClass.getConstructor(long.class);
					return constructor.newInstance(handle);
				}
				return null;
			}
		};
	}
}
