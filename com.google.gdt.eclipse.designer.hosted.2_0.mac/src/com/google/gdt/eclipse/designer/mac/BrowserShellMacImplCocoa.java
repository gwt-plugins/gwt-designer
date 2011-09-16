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
package com.google.gdt.eclipse.designer.mac;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Cocoa implementation for MacOSX native WebKit window support (both 32 and 64-bit).
 * 
 * @author mitin_aa
 */
public class BrowserShellMacImplCocoa implements IBrowserShellMacImpl {
	static {
		System.loadLibrary("wbp-gwt-cocoa");
		_init(BrowserShellMac.class);
	}
	public Rectangle computeTrim(long handle, Rectangle trim) {
		short bounds[] = new short[]{(short) trim.x, (short) trim.y, (short) trim.width, (short) trim.height};
		_computeTrim(handle, bounds);
		return new Rectangle(bounds[0], bounds[1] += trim.height - bounds[3], bounds[2], bounds[3]);
	}
	public long create(Object callback) {
		return _create(callback);
	}
	public Rectangle getBounds(long handle) {
		short[] bounds = new short[4];
		_getBounds(handle, bounds);
		return new Rectangle(bounds[0], bounds[1], bounds[2], bounds[3]);
	}
	public void release(long handle) {
		_release(handle);
	}
	public void setBounds(long handle, Rectangle bounds) {
		_setBounds(handle, bounds.x, bounds.y, bounds.width, bounds.height);
	}
	public void setUrl(long handle, String url) {
		_setUrl(handle, url);
	}
	public void setVisible(long handle, boolean visible) {
		_setVisible(handle, visible);
	}
	public void showAsPreview(long handle) {
		setVisible(handle, true);
		Display display = Display.getCurrent();
		while (_isVisible(handle)) {
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
	////////////////////////////////////////////////////////////////////////////
	//
	// TODO: remove when completely switching to D2
	//
	////////////////////////////////////////////////////////////////////////////
	public Image createBrowserScreenshot(long handle) throws Exception {
		Rectangle bounds = getBounds(handle);
		long imageHandle = _makeWindowShot(handle);
		Image image = createImageFromHandle(imageHandle, bounds.width, bounds.height);
		return image;
	}
	private static Image createImageFromHandle(long imageHandle, int width, int height) throws Exception {
		if (imageHandle != 0) {
			Class<?> nsImageClass = Class.forName("org.eclipse.swt.internal.cocoa.NSImage");
			Object handleObject;
			Class<?> handleClass;
			if (SystemUtils.OS_ARCH.indexOf("64") != -1) {
				handleClass = long.class;
				handleObject = new Long(imageHandle);
			} else {
				handleClass = int.class;
				handleObject = new Integer((int) imageHandle);
			}
			Constructor<?> constructor = nsImageClass.getConstructor(handleClass);
			Object nsImage = constructor.newInstance(handleObject);
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
	////////////////////////////////////////////////////////////////////////////
	//
	// Native
	//
	////////////////////////////////////////////////////////////////////////////
	private static native boolean _init(Class<?> callbackClass);
	private static native long _create(Object callback);
	private static native void _release(long handle);
	private static native void _setVisible(long handle, boolean visible);
	private static native void _setUrl(long handle, String url);
	private static native void _setBounds(long handle, int x, int y, int width, int height);
	private static native void _getBounds(long handle, short[] bounds);
	private static native void _computeTrim(long handle, short[] bounds);
	private static native boolean _isVisible(long handle);
	//
	private static native long _makeWindowShot(long shellHandle);
}
