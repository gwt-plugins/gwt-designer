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

import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Carbon implementation for MacOSX native WebKit window support (32-bit only).
 * 
 * @author mitin_aa
 */
public class BrowserShellMacImplCarbon implements IBrowserShellMacImpl {
	static {
		System.loadLibrary("wbp-gwt-carbon");
		_init(BrowserShellMac.class);
	}
	public Rectangle computeTrim(long handle, Rectangle trim) {
		short rect[] = new short[4];
		// values returned in top,left,bottom,right order
		_computeTrim((int) handle, rect);
		Rectangle trimmer = new Rectangle(rect[1], rect[0], rect[1] + rect[3], rect[0] + rect[2]);
		trim.x -= trimmer.x;
		trim.y -= trimmer.y;
		trim.width += trimmer.width;
		trim.height += trimmer.height;
		return trim;
	}
	public long create(Object callback) {
		return _create(callback);
	}
	public Rectangle getBounds(long handle) {
		short[] bounds = new short[4];
		_getBounds((int) handle, bounds);
		return new Rectangle(bounds[1], bounds[0], bounds[3] - bounds[1], bounds[2] - bounds[0]);
	}
	public void release(long handle) {
		_release((int) handle);
	}
	public void setBounds(long handle, Rectangle bounds) {
		_setBounds((int) handle, bounds.x, bounds.x + bounds.width, bounds.y, bounds.y + bounds.height);
	}
	public void setUrl(long handle, String url) {
		_setUrl((int) handle, url);
	}
	public void setVisible(long handle, boolean visible) {
		_setVisible((int) handle, visible);
	}
	public void showAsPreview(long handle) {
		setVisible(handle, true);
		_selectWindow((int) handle);
		Display display = Display.getCurrent();
		while (_isVisible((int) handle)) {
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
		float[] bounds = new float[4];
		_getWebViewBounds((int) handle, bounds);
		int imageHandle = _makeShot((int) handle);
		Image image = createImageFromHandle(imageHandle, (int)bounds[2], (int)bounds[3]);
		return image;
	}
	private static Image createImageFromHandle(int imageHandle, int width, int height) throws Exception {
		if (imageHandle != 0) {
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
							new Integer(imageHandle),
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
	////////////////////////////////////////////////////////////////////////////
	//
	// Native
	//
	////////////////////////////////////////////////////////////////////////////
	private static native boolean _init(Class<?> callbackClass);
	private static native int _create(Object callback);
	private static native void _release(int handle);
	private static native void _setVisible(int handle, boolean visible);
	private static native void _selectWindow(int handle);
	private static native void _setUrl(int handle, String url);
	private static native void _setBounds(int handle, int left, int right, int top, int bottom);
	private static native void _getBounds(int handle, short[] bounds);
	private static native void _getWebViewBounds(int handle, float[]bounds);
	private static native void _computeTrim(int handle, short[] bounds);
	private static native boolean _isVisible(int handle);
	//
	private static native int _makeShot(int handle);
}
