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
import com.google.gdt.eclipse.designer.moz.jsni.JsValueMoz32;
import com.google.gdt.eclipse.designer.moz.jsni.LowLevelMoz32;
import com.google.gdt.eclipse.designer.moz.jsni.ModuleSpaceMoz32;
import com.google.gdt.eclipse.designer.moz.jsni.LowLevelMoz32.DispatchMethod32;
import com.google.gdt.eclipse.designer.moz.jsni.LowLevelMoz32.DispatchObject32;
import com.google.gwt.dev.shell.JsValue;
import com.google.gwt.dev.shell.ModuleSpace;
import com.google.gwt.dev.shell.ModuleSpaceHost;

public class BrowserShellLinux32 extends BrowserShellLinux {
	private int /*long*/m_window;
	private int /*long*/m_windowPrivate;
	@Override
	public void dispose() {
		super.dispose();
		LowLevelMoz32/*64*/.releaseScriptObjectProxy(m_window);
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
		int /*long*/[] aContentDOMWindow = new int /*long*/[1];
		webBrowser.GetContentDOMWindow(aContentDOMWindow);
		final int /*long*/window = LowLevelMoz32/*64*/.getScriptObjectProxy(aContentDOMWindow[0]);
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
					JsValueMoz32/*64*/externalJSObject = new JsValueMoz32/*64*/();
					externalJSObject.setWrappedJavaObject(new GwtOnLoadDispatchObject32/*64*/());
					createAndInvoke(
						"__defineExternal",
						new String[]{"__arg0"},
						"window.__wbp_geckoExternal = __arg0;",
						new int /*long*/[]{externalJSObject.getJsRootedValue()});
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
								new int[0]);
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
		LowLevelMoz32/*64*/.executeScriptWithInfo(m_windowPrivate, newScript, "", 0);
		JsValueMoz32/*64*/jsthis = new JsValueMoz32/*64*/();
		jsthis.setNull();
		JsValueMoz32/*64*/returnVal = new JsValueMoz32/*64*/();
		LowLevelMoz32/*64*/.invoke(
			m_windowPrivate,
			name,
			jsthis.getJsRootedValue(),
			(int /*long*/[]) args,
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
			// makeShot() should be called immediately after beginShot() else browser returns empty image.
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
			LowLevelMoz32/*64*/._begin_shot(getShellHandle(shell));
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
			LowLevelMoz32/*64*/._end_shot(getShellHandle(shell));
		}
		super.endShot(control);
	}
	/**
	 * @return the handle value of the {@link Shell} using reflection.
	 */
	private int /*long*/getShellHandle(Shell shell) {
		int /*long*/widgetHandle = getFieldInt /*getFieldLong*/(shell, Control.class, "fixedHandle");
		if (widgetHandle == 0) {
			// may be null, roll back to "shellHandle"
			widgetHandle = getFieldInt /*getFieldLong*/(shell, shell.getClass(), "shellHandle");
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
			int /*long*/widgetHandle = getFieldInt /*getFieldLong*/(shell, Control.class, "fixedHandle");
			if (widgetHandle == 0) {
				// may be null, roll back to "handle"
				widgetHandle = getFieldInt /*getFieldLong*/(shell, Widget.class, "handle");
			}
			// apply shot magic
			int /*long*/pixmap = LowLevelMoz32/*64*/._makeShot(widgetHandle);
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
	private void setPixmapField(Image image, int /*long*/pixmap) throws Exception {
		int /*long*/oldPixmap = getFieldInt /*getFieldLong*/(image, image.getClass(), "pixmap");
		LowLevelMoz32/*64*/._g_object_unref/*64*/(oldPixmap);
		setField(image, "pixmap", new Integer /*Long*/(pixmap));
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
	private int /*long*/getFieldInt/*getFieldLong*/(Object object, Class<?> objectClass, String fieldName) {
		try {
			Field field = objectClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (Integer /*Long*/) field.get(object);
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
	protected void hackOnStateChange() throws Exception {
		if (!m_isBrowser33) {
			return;
		}
		Class<?> mozClass = m_webBrowser.getClass();
		final Method m_Mozilla_unhookDOMListeners;
		try {
			m_Mozilla_unhookDOMListeners = mozClass.getDeclaredMethod("unhookDOMListeners", new Class[0]);
			m_Mozilla_unhookDOMListeners.setAccessible(true);
		} catch (Throwable e) {
			// this method may not exist in early 3.3 milestones
			// so no hack is needed
			return;
		}
		// get old web progress listener instance and dispose
		Field f_Mozilla_webProgressListener = mozClass.getDeclaredField("webProgressListener");
		f_Mozilla_webProgressListener.setAccessible(true);
		final XPCOMObject oldWebProgressListener =
				(XPCOMObject) f_Mozilla_webProgressListener.get(m_webBrowser);
		oldWebProgressListener.dispose();
		// get methods which needed to delegate to
		final Method m_Mozilla_QueryInterface =
				mozClass.getDeclaredMethod("QueryInterface", new Class[]{int.class, int.class});
		final Method m_Mozilla_AddRef = mozClass.getDeclaredMethod("AddRef", new Class[0]);
		final Method m_Mozilla_Release = mozClass.getDeclaredMethod("Release", new Class[0]);
		final Method m_Mozilla_OnStateChange =
				mozClass.getDeclaredMethod("OnStateChange", new Class[]{
						int.class,
						int.class,
						int.class,
						int.class});
		final Method m_Mozilla_OnProgressChange =
				mozClass.getDeclaredMethod("OnProgressChange", new Class[]{
						int.class,
						int.class,
						int.class,
						int.class,
						int.class,
						int.class});
		final Method m_Mozilla_OnLocationChange =
				mozClass.getDeclaredMethod("OnLocationChange", new Class[]{int.class, int.class, int.class});
		final Method m_Mozilla_OnStatusChange =
				mozClass.getDeclaredMethod("OnStatusChange", new Class[]{
						int.class,
						int.class,
						int.class,
						int.class});
		final Method m_Mozilla_OnSecurityChange =
				mozClass.getDeclaredMethod("OnSecurityChange", new Class[]{int.class, int.class, int.class});
		// get them accessible
		m_Mozilla_QueryInterface.setAccessible(true);
		m_Mozilla_AddRef.setAccessible(true);
		m_Mozilla_Release.setAccessible(true);
		m_Mozilla_OnStateChange.setAccessible(true);
		m_Mozilla_OnProgressChange.setAccessible(true);
		m_Mozilla_OnLocationChange.setAccessible(true);
		m_Mozilla_OnStatusChange.setAccessible(true);
		m_Mozilla_OnSecurityChange.setAccessible(true);
		// create own web progress listener instance
		XPCOMObject webProgressListener = new XPCOMObject(new int[]{2, 0, 0, 4, 6, 3, 4, 3}) {
			private Integer[] prepareArgs(int[] args, int count) {
				Integer[] result = new Integer[count];
				for (int i = 0; i < count; ++i) {
					result[i] = new Integer(args[i]);
				}
				return result;
			}
			private int invokeInt(Method method, Integer[] args) {
				try {
					Integer result = (Integer) method.invoke(m_webBrowser, args);
					return result.intValue();
				} catch (Throwable e) {
					// do nothing: the exception should be already recorded in m_exception[] field
				}
				return XPCOM.NS_ERROR_FAILURE;
			}
			@Override
			public int /*long*/method0(int /*long*/[] args) {
				// return QueryInterface(args[0], args[1]);
				return invokeInt(m_Mozilla_QueryInterface, prepareArgs(args, 2));
			}
			@Override
			public int /*long*/method1(int /*long*/[] args) {
				// return AddRef();
				return invokeInt(m_Mozilla_AddRef, prepareArgs(args, 0));
			}
			@Override
			public int /*long*/method2(int /*long*/[] args) {
				// return Release();
				return invokeInt(m_Mozilla_Release, prepareArgs(args, 0));
			}
			@Override
			public int /*long*/method3(int /*long*/[] args) {
				// return OnStateChange(args[0], args[1], args[2], args[3]);
				try {
					int result = invokeInt(m_Mozilla_OnStateChange, prepareArgs(args, 4));
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
			@Override
			public int /*long*/method4(int /*long*/[] args) {
				// return OnProgressChange(args[0], args[1], args[2], args[3], args[4], args[5]);
				return invokeInt(m_Mozilla_OnProgressChange, prepareArgs(args, 6));
			}
			@Override
			public int /*long*/method5(int /*long*/[] args) {
				// return OnLocationChange(args[0], args[1], args[2]);
				return invokeInt(m_Mozilla_OnLocationChange, prepareArgs(args, 3));
			}
			@Override
			public int /*long*/method6(int /*long*/[] args) {
				// return OnStatusChange(args[0], args[1], args[2], args[3]);
				return invokeInt(m_Mozilla_OnStatusChange, prepareArgs(args, 4));
			}
			@Override
			public int /*long*/method7(int /*long*/[] args) {
				// return OnSecurityChange(args[0], args[1], args[2]);
				return invokeInt(m_Mozilla_OnSecurityChange, prepareArgs(args, 3));
			}
		};
		// replace with ours
		f_Mozilla_webProgressListener.set(m_webBrowser, webProgressListener);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// GwtOnLoad
	//
	////////////////////////////////////////////////////////////////////////////
	private static DispatchMethod32/*64*/EMPTY_DISP_METHOD = new DispatchMethod32/*64*/() {
		public void invoke(int /*long*/jsthis, int /*long*/[] jsargs, int /*long*/returnValue) {
		}
	};
	/**
	 * This used as dispatch object for 'window.external' field. It should provide the only 'toString' and
	 * 'gwtOnLoad' methods. When 'gwtOnLoad' invoked from JS this means that the browser is ready to work.
	 * 
	 * @author mitin_aa
	 */
	private final class GwtOnLoadDispatchObject32/*64*/implements DispatchObject32/*64*/{
		public void setField(String name, int /*long*/value) {
		}
		public Object getTarget() {
			return null;
		}
		public void getField(String name, int /*long*/jsRootedValue) {
			if (name.indexOf("toString") != -1) {
				JsValueMoz32/*64*/jsValue = new JsValueMoz32/*64*/(jsRootedValue);
				jsValue.setWrappedFunction(name, EMPTY_DISP_METHOD);
			} else if (name.indexOf("gwtOnLoad") != -1) {
				JsValueMoz32/*64*/jsValue = new JsValueMoz32/*64*/(jsRootedValue);
				jsValue.setWrappedFunction(name, new DispatchMethod32/*64*/() {
					public void invoke(int /*long*/jsthis, int /*long*/[] jsargs, int /*long*/returnValue) {
						try {
							System.out.println("Browser ready.");
							// Attach a new ModuleSpace to make it programmable.
							ModuleSpaceHost msh = getHost().createModuleSpaceHost(m_moduleName);
							ModuleSpace moduleSpace =
									new ModuleSpaceMoz32/*64*/(msh, m_windowPrivate, m_moduleName);
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
