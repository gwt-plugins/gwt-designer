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

import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.google.gdt.eclipse.designer.hosted.HostedModeException;

public class BrowserShellWebKitImplMacCarbon implements IBrowserShellWebKitImpl {
	private Integer m_handle;
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
			String libname = "wbp-gwt-webkit-bs-carbon";
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
		_setVisible(m_handle, true);
		_selectWindow(m_handle);
		Display display = Display.getCurrent();
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
		float[] bounds = new float[4];
		_getWebViewBounds(m_handle, bounds);
		Integer imageHandle = _makeShot(m_handle);
		Image image = createImageFromHandle(imageHandle, (int) bounds[2], (int) bounds[3]);
		return image;
	}
	private Image createImageFromHandle(Integer imageHandle, int width, int height) throws Exception {
		if (imageHandle != null) {
			// Create a temporary image using the captured image's handle
			Method method =
					Image.class.getDeclaredMethod("carbon_new", new Class[]{
							Device.class,
							int.class,
							int.class,
							int.class});
			method.setAccessible(true);
			Image tempImage =
					(Image) method.invoke(null, new Object[]{
							Display.getCurrent(),
							new Integer(SWT.BITMAP),
							imageHandle.intValue(),
							new Integer(0)});
			// Create the result image
			Image image = new Image(Display.getCurrent(), width, height);
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
		Rectangle trim = new Rectangle(x, y, width, height);
		short bounds[] = new short[4];
		_computeTrim(m_handle, bounds);
		// values returned in top,left,bottom,right order (actually as insets)
		Rectangle trimmer = new Rectangle(bounds[1], bounds[0], bounds[1] + bounds[3], bounds[0] + bounds[2]);
		trim.x -= trimmer.x;
		trim.y -= trimmer.y;
		trim.width += trimmer.width;
		trim.height += trimmer.height;
		return trim;
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
	//	Native methods 
	//
	////////////////////////////////////////////////////////////////////////////
	private static native void _init(Class<?> callbackClass);
	private static native Integer _create(Object callback);
	private static native void _release(Integer handle);
	private static native void _setUrl(Integer handle, String url);
	private static native void _setBounds(Integer handle, int x, int y, int width, int height);
	private static native void _getBounds(Integer handle, short[] bounds);
	private static native void _computeTrim(Integer handle, short[] bounds);
	private static native Integer _makeShot(Integer handle);
	private static native void _setVisible(Integer handle, boolean visible);
	private static native boolean _isVisible(Integer handle);
	private static native void _selectWindow(Integer handle);
	private static native void _getWebViewBounds(Integer handle, float[] bounds);
}
