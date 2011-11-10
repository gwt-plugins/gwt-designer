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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdt.eclipse.designer.hosted.HostedModeException;
import com.google.gdt.eclipse.designer.webkit.jsni.LowLevelWebKit;
import com.google.gdt.eclipse.designer.webkit.jsni.LowLevelWebKit.DispatchMethod;
import com.google.gdt.eclipse.designer.webkit.jsni.LowLevelWebKit.DispatchObject;

/**
 * Performs Browser initializing procedures (mainly gets 'window' script object and initializes JS context).
 * 
 * @author mitin_aa
 */
final class WebKitInitializer<H extends Number> {
	private H m_scriptObject;
	private final Map<H, H> m_globalContexts = new HashMap<H, H>();
	private int m_code;
	private String m_description;
	private boolean m_doneLoading;
	private Throwable m_exception;
	/**
	 * Waiting for 'window' script object available.
	 * 
	 * @param timeout
	 * @param messageProcessor
	 */
	public void wait(int timeout, Runnable messageProcessor) {
		long startTime = System.currentTimeMillis();
		while (true) {
			messageProcessor.run();
			if (m_doneLoading && m_code != 0) {
				throw new HostedModeException(HostedModeException.OSX_BROWSER_ERROR, new String[]{
						"" + m_code,
						m_description});
			}
			boolean exit1 = m_scriptObject != null && m_doneLoading;
			boolean exit2 = System.currentTimeMillis() - startTime >= timeout;
			if (exit1 || exit2 || m_exception != null) {
				break;
			}
		}
		if (m_scriptObject == null && m_exception == null) {
			throw new HostedModeException(HostedModeException.GWT_INIT_TIMEOUT);
		}
		if (!m_doneLoading) {
			if (m_exception != null) {
				throw new HostedModeException(HostedModeException.OSX_BROWSER_INIT_ERROR, m_exception);
			} else {
				throw new HostedModeException(HostedModeException.OSX_UNKNOWN_BROWSER_ERROR);
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
	/**
	 * Called from native code by 'alert()' function invocation.
	 */
	protected final void scriptAlert(String alertMessage) {
		System.out.println("alert(): " + alertMessage);
	}
	/**
	 * Called from native code when 'window' script object is available for loaded frame
	 */
	public final void windowScriptObjectAvailable(H jsGlobalContext) {
		if (m_scriptObject == null) {
			/*
			 * When GwtOnLoad fires we may not be able to get to the JSGlobalContext
			 * that corresponds to our module frame (since the call to GwtOnLoad
			 * could originate in the main page. So as each frame fires a
			 * windowScriptObjectAvailable event, we must store all window,
			 * globalContext pairs in a HashMap so we can later look up the global
			 * context by window object when GwtOnLoad is called.
			 */
			H jsGlobalObject = LowLevelWebKit.getGlobalJsObject(jsGlobalContext);
			LowLevelWebKit.pushJsContext(jsGlobalContext);
			try {
				m_globalContexts.put(jsGlobalObject, jsGlobalContext);
				H external = LowLevelWebKit.wrapDispatchObject(jsGlobalContext, new ExternalObject());
				LowLevelWebKit.executeScript(jsGlobalContext, "function __defineExternal(x) {"
					+ "  window.__wbp_external = x;"
					+ "}");
				List<H> listext = new ArrayList<H>(1);
				listext.add(external);
				H ignoredResult =
						LowLevelWebKit.invoke(
							jsGlobalContext,
							jsGlobalObject,
							"__defineExternal",
							jsGlobalObject,
							listext);
				LowLevelWebKit.gcUnprotect(jsGlobalContext, ignoredResult);
			} catch (Throwable e) {
				m_exception = e;
			} finally {
				LowLevelWebKit.popJsContext(jsGlobalContext);
			}
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	//  Access 
	//
	////////////////////////////////////////////////////////////////////////////
	public H getScriptObject() {
		return m_scriptObject;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	//  'External' object 
	//
	////////////////////////////////////////////////////////////////////////////
	private class ExternalObject implements DispatchObject<H> {
		public H getField(H jsContext, String name) {
			if ("gwtonload".equalsIgnoreCase(name)) {
				return LowLevelWebKit.wrapDispatchMethod(jsContext, "gwtOnload", new DispatchMethod<H>() {
					public H invoke(H jsContext, H jsthis, List<H> jsargs, List<H> exception) {
						H jsFalse = LowLevelWebKit.toJsBoolean(jsContext, false);
						LowLevelWebKit.pushJsContext(jsContext);
						try {
							if (!LowLevelWebKit.isDispatchObject(jsContext, jsthis)) {
								return jsFalse;
							}
							Object thisObj = LowLevelWebKit.unwrapDispatchObject(jsContext, jsthis);
							if (!(thisObj instanceof WebKitInitializer.ExternalObject)) {
								return jsFalse;
							}
							if (!LowLevelWebKit.isJsObject(jsContext, jsargs.get(0))) {
								return jsFalse;
							}
							m_scriptObject = jsargs.get(0);
							/*
							 * The global context for each window object is recorded during the
							 * windowScriptObjectAvailable event. Now that we know which window
							 * belongs to this module, we can resolve the correct global context.
							 */
							final H globalContext = m_globalContexts.get(m_scriptObject);
							// set global context on the top of the stack
							LowLevelWebKit.popJsContext(jsContext);
							LowLevelWebKit.pushJsContext(globalContext);
							LowLevelWebKit.pushJsContext(jsContext);
							//
							// Native code eats the same ref it gave us.
							return LowLevelWebKit.toJsBoolean(jsContext, true);
						} catch (Throwable e) {
							m_exception = e;
							return jsFalse;
						} finally {
							for (H jsarg : jsargs) {
								LowLevelWebKit.gcUnprotect(jsContext, jsarg);
							}
							LowLevelWebKit.gcUnprotect(jsContext, jsthis);
							LowLevelWebKit.popJsContext(jsContext);
						}
					}
				});
			}
			// Native code eats the same ref it gave us.
			return LowLevelWebKit.getJsUndefined(jsContext);
		}
		public Object getTarget() {
			return this;
		}
		public void setField(H jsContext, String name, H value) {
			try {
				// TODO (knorton): This should produce an error. The SetProperty
				// callback on the native side should be changed to pass an exception
				// array.
			} finally {
				LowLevelWebKit.gcUnprotect(jsContext, value);
			}
		}
	}
}