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
import java.lang.reflect.Method;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.mozilla.XPCOM;
import org.eclipse.swt.internal.mozilla.XPCOMObject;
import org.eclipse.swt.internal.mozilla.nsIWebBrowser;
import org.eclipse.swt.internal.mozilla.nsIWebProgressListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import com.google.gdt.eclipse.designer.hosted.HostedModeException;
import com.google.gdt.eclipse.designer.moz.jsni.JsValueMoz64;
import com.google.gdt.eclipse.designer.moz.jsni.LowLevelMoz64;
import com.google.gdt.eclipse.designer.moz.jsni.ModuleSpaceMoz64;
import com.google.gdt.eclipse.designer.moz.jsni.LowLevelMoz64.DispatchMethod64;
import com.google.gdt.eclipse.designer.moz.jsni.LowLevelMoz64.DispatchObject64;
import com.google.gwt.dev.shell.JsValue;
import com.google.gwt.dev.shell.ModuleSpace;
import com.google.gwt.dev.shell.ModuleSpaceHost;

public class BrowserShellLinux64 extends BrowserShellLinux {
	private long m_window;
	private long m_windowPrivate;
	@Override
	public void dispose() {
		super.dispose();
		LowLevelMoz64.releaseScriptObjectProxy(m_window);
	}
	@Override
	protected boolean hasWindow() {
		return m_window != 0;
	}
	@Override
	protected void fetchWindow() throws Exception {
		// looks like FireFox 1.5 fires this event every time when DOM changed.
		// We need the first-time event only
		if (m_window != 0 || m_windowPrivate != 0) {
			return;
		}
		nsIWebBrowser webBrowser;
		if (m_isBrowser33) {
			// 3.3M5+
			Field webBrowserField = m_webBrowser.getClass().getDeclaredField("webBrowser");
			webBrowserField.setAccessible(true);
			webBrowser = (nsIWebBrowser) webBrowserField.get(m_webBrowser);
		} else {
			// previous versions
			webBrowser = (nsIWebBrowser) m_webBrowser;
		}
		long[] aContentDOMWindow = new long[1];
		// use reflection for to be compiled under 32bit environment
		// webBrowser.GetContentDOMWindow(aContentDOMWindow);
		Method webBrowser_GetContentDOMWindow =
				webBrowser.getClass().getMethod("GetContentDOMWindow", new Class[]{long[].class});
		webBrowser_GetContentDOMWindow.invoke(webBrowser, new Object[]{aContentDOMWindow});
		final long window = LowLevelMoz64.getScriptObjectProxy(aContentDOMWindow[0]);
		if (window == 0) {
			m_unsupportedBrowserVersion = true;
			return;
		}
		System.out.println("window = " + window);
		m_windowPrivate = window;
		// add body.onLoad execution watcher
		// do it async because we can't manipulating with JS here.
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				try {
					JsValueMoz64 externalJSObject = new JsValueMoz64();
					externalJSObject.setWrappedJavaObject(new GwtOnLoadDispatchObject64());
					createAndInvoke(
						"__defineExternal",
						new String[]{"__arg0"},
						"window.__wbp_geckoExternal = __arg0;",
						new long[]{externalJSObject.getJsRootedValue()});
				} catch (Throwable e) {
					throw new HostedModeException(HostedModeException.LINUX_HOSTED_MODE_INIT_ERROR, e);
				}
			}
		});
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				try {
					JsValue returnVal =
							createAndInvoke(
								"__isInitializersReady",
								new String[0],
								"return (window.__wbp_geckoExternal != undefined) && (window.__wbp_geckoExternal.gwtOnLoad != undefined);",
								new long[0]);
					if (!returnVal.isBoolean() || !returnVal.getBoolean()) {
						throw new HostedModeException(HostedModeException.LINUX_HOSTED_MODE_INIT_ERROR);
					}
				} catch (Throwable e) {
					m_exception[0] = e;
				}
			}
		});
	}
	protected final JsValue createAndInvoke(String name, String[] jsargs, String body, Object args) {
		String newScript = ModuleSpace.createNativeMethodInjector(name, jsargs, body);
		LowLevelMoz64.executeScriptWithInfo(m_windowPrivate, newScript, "", 0);
		JsValueMoz64 jsthis = new JsValueMoz64();
		jsthis.setNull();
		JsValueMoz64 returnVal = new JsValueMoz64();
		LowLevelMoz64.invoke(
			m_windowPrivate,
			name,
			jsthis.getJsRootedValue(),
			(long[]) args,
			returnVal.getJsRootedValue());
		return returnVal;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Screenshot
	//
	////////////////////////////////////////////////////////////////////////////
	public Image createBrowserScreenshot() throws Exception {
		try {
			beginShot(m_browser);
			return makeShot(m_browser);
		} finally {
			endShot(m_browser);
		}
	}
	// TODO: rip this out when moved to D2
	@Override
	protected void beginShot(Control control) {
		super.beginShot(control);
		Shell shell = control.getShell();
		if (isWorkaroundsDisabled()) {
			shell.setLocation(10000, 10000);
			shell.setVisible(true);
			// to prevent 'black screen' on gtk-2.18+
			// there is no way to ensure that the Browser rendered fully, so just pump loop
			for (int i = 0; i < 10; ++i) {
				while (Display.getCurrent().readAndDispatch()) {
				}
			}
		} else {
			LowLevelMoz64._begin_shot(getShellHandle(shell));
			try {
				// Bug/feature is SWT: since the widget is already shown, the Shell.setVisible() invocation 
				// has no effect, so we've end up with wrong shell trimming.
				// The workaround is to call adjustTrim() explicitly.
				Method adjustTrimMethod = shell.getClass().getDeclaredMethod("adjustTrim", new Class[0]);
				adjustTrimMethod.setAccessible(true);
				adjustTrimMethod.invoke(shell, new Object[0]);
			} catch (Throwable e) {
				// ignore
			}
		}
	}
	@Override
	protected void endShot(Control control) {
		Shell shell = control.getShell();
		if (isWorkaroundsDisabled()) {
			shell.setVisible(false);
		} else {
			LowLevelMoz64._end_shot(getShellHandle(shell));
		}
		super.endShot(control);
	}
	/**
	 * @return the handle value of the {@link Shell} using reflection.
	 */
	private long getShellHandle(Shell shell) {
		long widgetHandle = getFieldLong(shell, Control.class, "fixedHandle");
		if (widgetHandle == 0) {
			// may be null, roll back to "shellHandle"
			widgetHandle = getFieldLong(shell, shell.getClass(), "shellHandle");
		}
		return widgetHandle;
	}
	private Image makeShot(Control control) {
		Shell shell = control.getShell();
		//
		fixMozilla(shell);
		//
		// get the handle for the control, first try "fixedHandle"
		//		shell.setLocation(10000, 10000);
		Rectangle controlBounds = control.getBounds();
		Image shotImage = new Image(null, controlBounds);
		try {
			long widgetHandle = getFieldLong(shell, Control.class, "fixedHandle");
			if (widgetHandle == 0) {
				// may be null, roll back to "handle"
				widgetHandle = getFieldLong(shell, Widget.class, "handle");
			}
			// apply shot magic
			long pixmap = LowLevelMoz64._makeShot(widgetHandle);
			setPixmapField(shotImage, pixmap);
		} catch (Throwable e) {
			return null;
		} finally {
			shell.setVisible(false);
		}
		return shotImage;
	}
	/**
	 * Replaces the pixmap field in given {@link Image}. Disposes the old pixmap field.
	 */
	private void setPixmapField(Image image, long pixmap) throws Exception {
		long oldPixmap = getFieldLong(image, image.getClass(), "pixmap");
		LowLevelMoz64._g_object_unref/*64*/(oldPixmap);
		setField(image, "pixmap", new Long(pixmap));
	}
	private void setField(Object object, String fieldName, Object value) {
		try {
			Field field = object.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	private long getFieldLong(Object object, Class<?> objectClass, String fieldName) {
		try {
			Field field = objectClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (Long) field.get(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// State change hack
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void hackOnStateChange() {
		if (!m_isBrowser33) {
			return;
		}
		Class mozClass = m_webBrowser.getClass();
		final Method m_Mozilla_unhookDOMListeners;
		try {
			m_Mozilla_unhookDOMListeners = mozClass.getDeclaredMethod("unhookDOMListeners", new Class[0]);
			m_Mozilla_unhookDOMListeners.setAccessible(true);
		} catch (Throwable e) {
			// this method may not exist in early 3.3 milestones
			// so no hack is needed
			return;
		}
		try {
			// get old web progress listener instance and dispose
			Field f_Mozilla_webProgressListener = mozClass.getDeclaredField("webProgressListener");
			f_Mozilla_webProgressListener.setAccessible(true);
			final XPCOMObject oldWebProgressListener =
					(XPCOMObject) f_Mozilla_webProgressListener.get(m_webBrowser);
			oldWebProgressListener.dispose();
			// get methods which needed to delegate to
			final Method m_Mozilla_QueryInterface =
					mozClass.getDeclaredMethod("QueryInterface", new Class[]{long.class, long.class});
			final Method m_Mozilla_AddRef = mozClass.getDeclaredMethod("AddRef", new Class[0]);
			final Method m_Mozilla_Release = mozClass.getDeclaredMethod("Release", new Class[0]);
			final Method m_Mozilla_OnLocationChange;
			final Method[] m_Mozilla_OnStateChange = new Method[1];
			final Method[] m_Mozilla_OnProgressChange = new Method[1];
			final Method[] m_Mozilla_OnStatusChange = new Method[1];
			final Method[] m_Mozilla_OnSecurityChange = new Method[1];
			try {
				// eclipse 3.3
				m_Mozilla_OnStateChange[0] =
						mozClass.getDeclaredMethod("OnStateChange", new Class[]{
								long.class,
								long.class,
								long.class,
								long.class});
			} catch (NoSuchMethodException nsme) {
				// eclipse 3.4
				m_Mozilla_OnStateChange[0] =
						mozClass.getDeclaredMethod("OnStateChange", new Class[]{
								long.class,
								long.class,
								int.class,
								int.class});
			}
			try {
				m_Mozilla_OnProgressChange[0] =
						mozClass.getDeclaredMethod("OnProgressChange", new Class[]{
								long.class,
								long.class,
								long.class,
								long.class,
								long.class,
								long.class});
			} catch (NoSuchMethodException nsme) {
				m_Mozilla_OnProgressChange[0] =
						mozClass.getDeclaredMethod("OnProgressChange", new Class[]{
								long.class,
								long.class,
								int.class,
								int.class,
								int.class,
								int.class});
			}
			m_Mozilla_OnLocationChange =
					mozClass.getDeclaredMethod("OnLocationChange", new Class[]{
							long.class,
							long.class,
							long.class});
			try {
				m_Mozilla_OnStatusChange[0] =
						mozClass.getDeclaredMethod("OnStatusChange", new Class[]{
								long.class,
								long.class,
								long.class,
								long.class});
			} catch (NoSuchMethodException nsme) {
				m_Mozilla_OnStatusChange[0] =
						mozClass.getDeclaredMethod("OnStatusChange", new Class[]{
								long.class,
								long.class,
								int.class,
								long.class});
			}
			try {
				m_Mozilla_OnSecurityChange[0] =
						mozClass.getDeclaredMethod("OnSecurityChange", new Class[]{
								long.class,
								long.class,
								long.class});
			} catch (NoSuchMethodException nsme) {
				m_Mozilla_OnSecurityChange[0] =
						mozClass.getDeclaredMethod("OnSecurityChange", new Class[]{
								long.class,
								long.class,
								int.class});
			}
			// get them accessible
			m_Mozilla_QueryInterface.setAccessible(true);
			m_Mozilla_AddRef.setAccessible(true);
			m_Mozilla_Release.setAccessible(true);
			m_Mozilla_OnLocationChange.setAccessible(true);
			m_Mozilla_OnStateChange[0].setAccessible(true);
			m_Mozilla_OnProgressChange[0].setAccessible(true);
			m_Mozilla_OnStatusChange[0].setAccessible(true);
			m_Mozilla_OnSecurityChange[0].setAccessible(true);
			// create own web progress listener instance
			XPCOMObject webProgressListener = new XPCOMObject(new int[]{2, 0, 0, 4, 6, 3, 4, 3}) {
				private Object[] prepareArgs(Method method, long[] args) {
					Class<?>[] parameterTypes = method.getParameterTypes();
					int count = parameterTypes.length;
					Object[] result = new Object[count];
					for (int i = 0; i < count; ++i) {
						if (parameterTypes[i] == long.class) {
							result[i] = new Long(args[i]);
						} else if (parameterTypes[i] == int.class) {
							result[i] = new Integer((int) args[i]);
						}
					}
					return result;
				}
				private long invokeLong(Method method, long[] args) {
					try {
						Number result = (Number) method.invoke(m_webBrowser, prepareArgs(method, args));
						return result.longValue();
					} catch (Throwable e) {
						m_exception[0] = e;
					}
					return XPCOM.NS_ERROR_FAILURE;
				}
				public long method0(long[] args) {
					// return QueryInterface(args[0], args[1]);
					return invokeLong(m_Mozilla_QueryInterface, args);
				}
				public long method1(long[] args) {
					// return AddRef();
					return invokeLong(m_Mozilla_AddRef, args);
				}
				public long method2(long[] args) {
					// return Release();
					return invokeLong(m_Mozilla_Release, args);
				}
				public long method3(long[] args) {
					// return OnStateChange(args[0], args[1], args[2], args[3]);
					try {
						long result = invokeLong(m_Mozilla_OnStateChange[0], args);
						if (result != XPCOM.NS_OK) {
							return result;
						}
						// unhook events which are hooked at STATE_STOP
						if ((args[2] /*aStateFlags*/& nsIWebProgressListener.STATE_STOP) != 0) {
							m_Mozilla_unhookDOMListeners.invoke(m_webBrowser, new Object[0]);
						}
						return result;
					} catch (Throwable e) {
						return XPCOM.NS_ERROR_FAILURE;
					}
				}
				public long method4(long[] args) {
					// return OnProgressChange(args[0], args[1], args[2], args[3], args[4], args[5]);
					return invokeLong(m_Mozilla_OnProgressChange[0], args);
				}
				public long method5(long[] args) {
					// return OnLocationChange(args[0], args[1], args[2]);
					return invokeLong(m_Mozilla_OnLocationChange, args);
				}
				public long method6(long[] args) {
					// return OnStatusChange(args[0], args[1], args[2], args[3]);
					return invokeLong(m_Mozilla_OnStatusChange[0], args);
				}
				public long method7(long[] args) {
					// return OnSecurityChange(args[0], args[1], args[2]);
					return invokeLong(m_Mozilla_OnSecurityChange[0], args);
				}
			};
			// replace with ours
			f_Mozilla_webProgressListener.set(m_webBrowser, webProgressListener);
		} catch (Throwable e) {
			// can't hack into it, skip
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// GwtOnLoad
	//
	////////////////////////////////////////////////////////////////////////////
	private static DispatchMethod64 EMPTY_DISP_METHOD = new DispatchMethod64() {
		public void invoke(long jsthis, long[] jsargs, long returnValue) {
		}
	};
	/**
	 * This used as dispatch object for 'window.external' field. It should provide the only 'toString' and
	 * 'gwtOnLoad' methods. When 'gwtOnLoad' invoked from JS this means that the browser is ready to work.
	 * 
	 * @author mitin_aa
	 */
	private final class GwtOnLoadDispatchObject64 implements DispatchObject64 {
		public void setField(String name, long value) {
		}
		public Object getTarget() {
			return null;
		}
		public void getField(String name, long jsRootedValue) {
			if (name.indexOf("toString") != -1) {
				JsValueMoz64 jsValue = new JsValueMoz64(jsRootedValue);
				jsValue.setWrappedFunction(name, EMPTY_DISP_METHOD);
			} else if (name.indexOf("gwtOnLoad") != -1) {
				JsValueMoz64 jsValue = new JsValueMoz64(jsRootedValue);
				jsValue.setWrappedFunction(name, new DispatchMethod64() {
					public void invoke(long jsthis, long[] jsargs, long returnValue) {
						try {
							System.out.println("Browser ready.");
							// Attach a new ModuleSpace to make it programmable.
							ModuleSpaceHost msh = getHost().createModuleSpaceHost(m_moduleName);
							ModuleSpace moduleSpace =
									new ModuleSpaceMoz64(msh, m_windowPrivate, m_moduleName);
							attachModuleSpace(moduleSpace);
							m_window = m_windowPrivate;
						} catch (Throwable e) {
							m_exception[0] = e;
						}
					}
				});
			}
		}
	}
}
