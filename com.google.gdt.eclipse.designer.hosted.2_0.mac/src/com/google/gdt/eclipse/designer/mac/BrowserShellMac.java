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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.google.gdt.eclipse.designer.hosted.HostedModeException;
import com.google.gwt.dev.shell.ModuleSpace;
import com.google.gwt.dev.shell.ModuleSpaceHost;
import com.google.gwt.dev.shell.mac.JsValueSaf;
import com.google.gwt.dev.shell.mac.ModuleSpaceSaf;

/**
 * Wrapping class for native window with WebKit browser
 * 
 * @author mitin_aa
 */
public class BrowserShellMac extends com.google.gdt.eclipse.designer.hosted.tdz.BrowserShell {
	static {
		System.loadLibrary("wbp-gwt");
		init();
		m_impl =
				"cocoa".equals(SWT.getPlatform())
						? new BrowserShellMacImplCocoa()
						: new BrowserShellMacImplCarbon();
	}
	private static IBrowserShellMacImpl m_impl;
	private long m_handle;
	private long m_windowScriptObject; // WebScriptObject instance
	private boolean m_doneLoading; // fired from native code when the frame is loaded
	private boolean m_doneInitializing; // fired when body.onLoad called
	private final Throwable[] m_exception = new Throwable[1];
	private int m_code;
	private String m_description;
	private String m_moduleName;
	//
	public BrowserShellMac() {
		m_handle = impl_create(this);
	}
	@Override
	public void dispose() {
		if (m_handle != 0) {
			impl_release(m_handle);
			m_handle = 0;
		}
		super.dispose();
	}
	public boolean isDisposed() {
		return m_handle == 0;
	}
	public void setVisible(boolean visible) {
		checkWidget();
		impl_setVisible(m_handle, visible);
	}
	/**
	 * Called from native code when 'window' script object is available for loaded frame
	 * 
	 * @param wso
	 *            WebScriptObject instance passed as long (native pointer type)
	 */
	protected final void windowScriptObjectAvailable(long wso) {
		if (m_windowScriptObject == 0) {
			// window script object is available for non-root frame also. But we not need it. 
			m_windowScriptObject = wso;
			// add body.onLoad execution watcher
			try {
				String newScript =
						ModuleSpace.createNativeMethodInjector(
							"__defineExternal",
							new String[]{"__arg0"},
							"window.__wbp_external = __arg0;");
				executeScript(wso, newScript);
				long external = wrapDispatch(new ExternalObject());
				invoke(wso, "__defineExternal", wso, new long[]{external});
				// let the WebKit to be the owner of the object
				objcRelease(external);
			} catch (Throwable e) {
				m_exception[0] = new HostedModeException(HostedModeException.OSX_BROWSER_INIT_ERROR, e);
			}
		}
	}
	/**
	 * Called from native code when frame loading is done
	 */
	protected final void doneLoading(int code, String description) {
		m_code = code;
		m_description = description;
		m_doneLoading = true;
	}
	public void setUrl(String url, String moduleName, int timeout, Runnable messageProcessor)
			throws Exception {
		// paranoid clearing the error
		m_exception[0] = null;
		m_moduleName = moduleName;
		checkWidget();
		impl_setUrl(m_handle, url);
		long startTime = System.currentTimeMillis();
		// wait for load
		while (true) {
			messageProcessor.run();
			if (m_doneLoading && m_code != 0) {
				throw new HostedModeException(HostedModeException.OSX_BROWSER_ERROR, new String[]{
						"" + m_code,
						m_description});
			}
			boolean exit1 = m_windowScriptObject != 0 && m_doneInitializing;
			boolean exit2 = System.currentTimeMillis() - startTime >= timeout;
			if (exit1 || exit2 || m_exception[0] != null) {
				break;
			}
		}
		if (m_windowScriptObject == 0) {
			throw new HostedModeException(HostedModeException.GWT_INIT_TIMEOUT, m_exception[0]);
		}
		if (!m_doneLoading || !m_doneInitializing) {
			if (m_exception[0] != null) {
				if (m_exception[0] instanceof HostedModeException) {
					throw (HostedModeException) m_exception[0];
				}
				throw new HostedModeException(HostedModeException.MODULE_LOADING_ERROR, m_exception[0]);
			} else {
				throw new HostedModeException(HostedModeException.OSX_UNKNOWN_BROWSER_ERROR);
			}
		}
	}
	public void setBounds(Rectangle bounds) {
		checkWidget();
		impl_setBounds(m_handle, bounds);
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
	public Rectangle computeTrim(int x, int y, int width, int height) {
		checkWidget();
		return impl_computeTrim(m_handle, new Rectangle(x, y, width, height));
	}
	public void showAsPreview() {
		checkWidget();
		Rectangle shellBounds = getBounds();
		Rectangle screenBounds = Display.getCurrent().getClientArea();
		int x = Math.max(0, screenBounds.x + (screenBounds.width - shellBounds.width) / 2);
		int y =
				Math.max(20 /*main menu app. height*/, screenBounds.y
					+ (screenBounds.height - shellBounds.height)
					/ 3);
		// bug in webkit, it doesn't redraw it's view sometimes. 
		// the workaround is to pull it's size a pixel down and up again :) 
		Rectangle bounds = getBounds();
		bounds.x = x;
		bounds.y = y;
		bounds.width -= 1;
		setBounds(bounds);
		bounds.width += 1;
		setBounds(bounds);
		//
		showAsPreview0();
	}
	public void prepare() {
		// nothing to do on MacOSX
	}
	public String getUserAgentString() {
		return "safari";
	}
	public Image createBrowserScreenshot() throws Exception {
		checkWidget();
		return impl_createBrowserScreenshot(m_handle);
	}
	private void showAsPreview0() {
		MacOSXActivator.getShell().setEnabled(false);
		try {
			checkWidget();
			impl_showAsPreview(m_handle);
		} finally {
			MacOSXActivator.getShell().setEnabled(true);
		}
	}
	public Rectangle getBounds() {
		checkWidget();
		return impl_getBounds(m_handle);
	}
	private void checkWidget() {
		if (isDisposed()) {
			SWT.error(SWT.ERROR_WIDGET_DISPOSED);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// External object, used to wait for body.onLoad to execute 
	//
	////////////////////////////////////////////////////////////////////////////
	private final class ExternalObject implements DispatchObject {
		public long getField(String name) {
			// not used
			return jsUndefined();
		}
		public Object getTarget() {
			// not used
			return null;
		}
		public void setField(String name, long value) {
			// not used
		}
		public String[] getFields() {
			// we have only one 'gwtonload' virtual javascript field
			return new String[]{"gwtonload"};
		}
		public Object getWrappedMethod(String name) {
			// the only 'gwtonload' can be invoked
			Object obj = new DispatchMethod() {
				public long invoke(long jsthis, long[] jsargs) {
					try {
						Long key = new Long(m_windowScriptObject);
						ModuleSpaceHost msh = getHost().createModuleSpaceHost(m_moduleName);
						/*
						 * The global context for each window object is recorded during the
						 * windowScriptObjectAvailable event. Now that we know which window
						 * belongs to this module, we can resolve the correct global context.
						 */
						ModuleSpace moduleSpace =
								new ModuleSpaceSaf(msh, m_windowScriptObject, m_moduleName, key);
						BrowserShellMac.this.attachModuleSpace(moduleSpace);
						//
						BrowserShellMac.this.m_doneInitializing = true;
						return BrowserShellMac.convertBoolean(true);
					} catch (Throwable e) {
						m_exception[0] = e;
						return BrowserShellMac.convertBoolean(false);
					}
				}
			};
			return obj;
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// obj-c <-> java bridge
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Provides interface for methods to be exposed on javascript side.
	 */
	public interface DispatchMethod {
		/**
		 * Invoked inside of JS via obj-c bridge.
		 * 
		 * @param jsthis
		 *            A JS object (may be wrapped at obj-c level DispatchObject instance).
		 * @param jsargs
		 *            An array of parameters
		 * @return the result of invocation wrapped as obj-c value
		 */
		long invoke(long jsthis, long[] jsargs);
	}
	/**
	 * Wrapper class. Provides interface for java objects to be exposed on javascript side.
	 */
	public interface DispatchObject {
		/**
		 * Target instance which DispatchObject wrapped around
		 */
		Object getTarget();
		/**
		 * Returns an instance of DispatchMethod, a wrapper around class method
		 * 
		 * @param name
		 *            A full 'google' name of method i.e. 'at'com.google.client.ui.Button::getText();
		 * @return an instance of DispatchMethod
		 */
		Object getWrappedMethod(String name);
		/**
		 * Returns field value wrapped as obj-c value
		 * 
		 * @param name
		 *            A full 'google' name of field
		 * @return field value wrapped as obj-c value
		 */
		long getField(String name);
		/**
		 * Sets field value. A passed <code>value</code> is obj-c value, which may be unwrapped before set to
		 * field
		 * 
		 * @param name
		 *            A full 'google' name of field
		 * @param value
		 *            A wrapped as obj-c value
		 */
		void setField(String name, long value);
		/**
		 * Enumerates fields of wrapping target class.
		 * 
		 * @return An array of string of full 'google' names of target class fields.
		 */
		String[] getFields();
	}
	public static boolean coerceToBoolean(long jsval) {
		boolean[] rval = new boolean[1];
		if (!_coerceToBoolean(jsval, rval)) {
			throw new RuntimeException("Failed to coerce to boolean value.");
		}
		return rval[0];
	}
	public static byte coerceToByte(long jsval) {
		double[] rval = new double[1];
		if (!_coerceToDouble(jsval, rval)) {
			throw new RuntimeException("Failed to coerce to byte value");
		}
		return (byte) rval[0];
	}
	public static char coerceToChar(long jsval) {
		double[] rval = new double[1];
		if (!_coerceToDouble(jsval, rval)) {
			throw new RuntimeException("Failed to coerce to char value");
		}
		return (char) rval[0];
	}
	public static double coerceToDouble(long jsval) {
		double[] rval = new double[1];
		if (!_coerceToDouble(jsval, rval)) {
			throw new RuntimeException("Failed to coerce to double value");
		}
		return rval[0];
	}
	public static float coerceToFloat(long jsval) {
		double[] rval = new double[1];
		if (!_coerceToDouble(jsval, rval)) {
			throw new RuntimeException("Failed to coerce to double value");
		}
		return (float) rval[0];
	}
	public static int coerceToInt(long jsval) {
		double[] rval = new double[1];
		if (!_coerceToDouble(jsval, rval)) {
			throw new RuntimeException("Failed to coerce to int value");
		}
		return (int) rval[0];
	}
	public static long coerceToLong(long jsval) {
		double[] rval = new double[1];
		if (!_coerceToDouble(jsval, rval)) {
			throw new RuntimeException("Failed to coerce to long value");
		}
		return (long) rval[0];
	}
	public static short coerceToShort(long jsval) {
		double[] rval = new double[1];
		if (!_coerceToDouble(jsval, rval)) {
			throw new RuntimeException("Failed to coerce to short value");
		}
		return (short) rval[0];
	}
	public static String coerceToString(long jsval) {
		String[] rval = new String[1];
		if (!_coerceToString(jsval, rval)) {
			throw new RuntimeException("Failed to coerce to String value");
		}
		return rval[0];
	}
	public static long convertBoolean(boolean v) {
		long[] rval = new long[1];
		if (!_convertBoolean(v, rval)) {
			throw new RuntimeException("Failed to convert Boolean value: " + String.valueOf(v));
		}
		return rval[0];
	}
	public static long convertDouble(double v) {
		long[] rval = new long[1];
		if (!_convertDouble(v, rval)) {
			throw new RuntimeException("Failed to convert Double value: " + String.valueOf(v));
		}
		return rval[0];
	}
	public static long convertString(String v) {
		long[] rval = new long[1];
		if (!_convertString(v, rval)) {
			throw new RuntimeException("Failed to convert String value: " + String.valueOf(v));
		}
		return rval[0];
	}
	/**
	 * Executes JavaScript code.
	 * 
	 * @param window
	 *            An opaque handle to the browser shell window instance
	 * @param code
	 *            The JavaScript code to execute
	 */
	public static void executeScript(long window, String code) {
		if (!_executeScript(window, code)) {
			throw new RuntimeException("Failed to execute script: " + code);
		}
	}
	public static void objcRetain(long jsval) {
		_objcRetain(jsval);
	}
	public static void objcRelease(long jsval) {
		_objcRelease(jsval);
	}
	public static synchronized void init() {
		if (!_init(DispatchObject.class, DispatchMethod.class, JsValueSaf.class)) {
			throw new RuntimeException("Unable to initialize low-level routines. Please check installation.");
		}
	}
	/**
	 * Invokes a method implemented in JavaScript.
	 * 
	 * @param window
	 *            An opaque handle to the browser shell window instance (TWindow instance)
	 * @param methodName
	 *            the method name on jsthis to call
	 * @param jsthis
	 *            a wrapped java object as a obj-c value
	 * @param jsargs
	 *            the arguments to pass to the method
	 * @return the result of the invocation
	 */
	public static long invoke(long window, String methodName, long jsthis, long[] jsargs) {
		long[] rval = new long[1];
		if (!_invoke(window, methodName, jsthis, jsargs.length, jsargs, rval)) {
			throw new RuntimeException("Failed to invoke native method: "
				+ methodName
				+ " with "
				+ jsargs.length
				+ " arguments.");
		}
		return rval[0];
	}
	/**
	 * @param jsval
	 *            the obj-c value in question
	 * @return <code>true</code> if the value is the null value
	 */
	public static boolean isNull(long jsval) {
		return _isNull(jsval);
	}
	/**
	 * Is the jsval a web script object?
	 * 
	 * @param jsval
	 *            the value
	 * @return true if jsval is a web script object
	 */
	public static boolean isObject(long jsval) {
		return _isObject(jsval);
	}
	public static boolean isBoolean(long jsval) {
		return _isBoolean(jsval);
	}
	public static boolean isNumber(long jsval) {
		return _isNumber(jsval);
	}
	/**
	 * Is the jsval a string primitive?
	 * 
	 * @param jsval
	 *            the value
	 * @return true if the jsval is a string primitive
	 */
	public static boolean isString(long jsval) {
		return _isString(jsval);
	}
	public static String getTypeString(long jsval) {
		return _getTypeString(jsval);
	}
	/**
	 * @param jsval
	 *            the obj-c value in question
	 * @return <code>true</code> if the value is the undefined value
	 */
	public static boolean isUndefined(long jsval) {
		return _isUndefined(jsval);
	}
	/**
	 * Is the jsval obj-c a wrapped DispatchObject? (see obj-c class DispatchObjectWrapper)
	 * 
	 * @param jsval
	 *            the value
	 * @return true if the jsval is a wrapped DispatchObject
	 */
	public static boolean isWrappedDispatch(long jsval) {
		boolean[] rval = new boolean[1];
		if (!_isWrappedDispatch(jsval, rval)) {
			throw new RuntimeException("Failed isWrappedDispatch.");
		}
		return rval[0];
	}
	/**
	 * @return the obj-c null value (NSNull)
	 */
	public static long jsNull() {
		return _jsNull();
	}
	/**
	 * @return the obj-c undefined value (WebUndefined)
	 */
	public static long jsUndefined() {
		return _jsUndefined();
	}
	/**
	 * Call this to raise an exception in JavaScript before returning control. mitin_aa: raising JS exceptions
	 * are not available via public API, this method provided for convenience
	 */
	public static void raiseJavaScriptException(long execState, long jsval) {
		if (!_raiseJavaScriptException(execState, jsval)) {
			throw new RuntimeException("Failed to raise Java Exception into JavaScript.");
		}
	}
	/**
	 * Unwraps a wrapped DispatchObject. DispatchObject wrapped by obj-c DispatchObjectWrapper class instance.
	 * 
	 * @param jsval
	 *            a value previously returned from wrapDispatch (a pointer DispatchObjectWrapper instance)
	 * @return the original DispatchObject
	 */
	public static DispatchObject unwrapDispatch(long jsval) {
		DispatchObject[] rval = new DispatchObject[1];
		if (!_unwrapDispatch(jsval, rval)) {
			throw new RuntimeException("Failed to unwrapDispatch.");
		}
		return rval[0];
	}
	/**
	 * Wraps DispatchObject by obj-c DispatchObjectWrapper
	 * 
	 * @param dispObj
	 *            the DispatchObject to wrap
	 * @return the wrapped object as pointer to obj-c DispatchObjectWrapper instance
	 */
	public static long wrapDispatch(DispatchObject dispObj) {
		long[] rval = new long[1];
		if (!_wrapDispatch(dispObj, rval)) {
			throw new RuntimeException("Failed to wrapDispatch.");
		}
		return rval[0];
	}
	/**
	 * Called from native code to do tracing.
	 * 
	 * @param s
	 *            the string to trace
	 */
	protected static void trace(String s) {
		System.out.println(s);
		System.out.flush();
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Native
	//
	////////////////////////////////////////////////////////////////////////////
	private static native boolean _init(Class<?> dispObjClass,
			Class<?> dispMethClass,
			Class<?> jsValueSafClass);
	//
	private static native boolean _coerceToBoolean(long jsval, boolean[] rval);
	private static native boolean _coerceToDouble(long jsval, double[] rval);
	private static native boolean _coerceToString(long jsval, String[] rval);
	private static native boolean _convertBoolean(boolean v, long[] rval);
	private static native boolean _convertDouble(double v, long[] rval);
	private static native boolean _convertString(String v, long[] rval);
	//
	private static native boolean _isBoolean(long jsval);
	private static native boolean _isNumber(long jsval);
	private static native boolean _isObject(long jsval);
	private static native boolean _isString(long jsval);
	private static native boolean _isWrappedDispatch(long jsval, boolean[] rval);
	private static native boolean _isNull(long jsval);
	private static native boolean _isUndefined(long jsval);
	//
	private static native boolean _raiseJavaScriptException(long execState, long jsval);
	private static native boolean _unwrapDispatch(long jsval, DispatchObject[] rval);
	private static native boolean _wrapDispatch(DispatchObject dispObj, long[] rval);
	//
	private static native boolean _executeScript(long window, String code);
	private static native boolean _invoke(long window,
			String methodName,
			long jsthis,
			int jsargCount,
			long[] jsargs,
			long[] rval);
	//
	private static native void _objcRetain(long jsval);
	private static native void _objcRelease(long jsval);
	//
	private static native long _jsNull();
	private static native long _jsUndefined();
	private static native String _getTypeString(long jsval);
	////////////////////////////////////////////////////////////////////////////
	//
	// Visual data  methods
	//
	////////////////////////////////////////////////////////////////////////////
	private static long impl_create(Object callback) {
		return m_impl.create(callback);
	}
	private static void impl_release(long handle) {
		m_impl.release(handle);
	}
	private static void impl_setVisible(long handle, boolean visible) {
		m_impl.setVisible(handle, visible);
	}
	private static void impl_setUrl(long handle, String url) {
		m_impl.setUrl(handle, url);
	}
	private static void impl_setBounds(long handle, Rectangle bounds) {
		m_impl.setBounds(handle, bounds);
	}
	private static Rectangle impl_getBounds(long handle) {
		return m_impl.getBounds(handle);
	}
	private static Rectangle impl_computeTrim(long handle, Rectangle trim) {
		return m_impl.computeTrim(handle, trim);
	}
	private static void impl_showAsPreview(long handle) {
		m_impl.showAsPreview(handle);
	}
	private static Image impl_createBrowserScreenshot(long handle) throws Exception {
		return m_impl.createBrowserScreenshot(handle);
	}
}
