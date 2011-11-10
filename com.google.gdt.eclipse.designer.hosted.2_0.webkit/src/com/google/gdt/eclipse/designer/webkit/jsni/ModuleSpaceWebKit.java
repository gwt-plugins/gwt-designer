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
package com.google.gdt.eclipse.designer.webkit.jsni;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.shell.CompilingClassLoader;
import com.google.gwt.dev.shell.JsValue;
import com.google.gwt.dev.shell.JsValueGlue;
import com.google.gwt.dev.shell.ModuleSpace;
import com.google.gwt.dev.shell.ModuleSpaceHost;

/**
 * An implementation of {@link com.google.gwt.dev.shell.ModuleSpace} for Safari.
 */
public class ModuleSpaceWebKit<H extends Number> extends ModuleSpace {

  private final H globalObject;

  private final H globalContext;

  /**
   * Constructs a browser interface for use with a global window object.
   * 
   * @param moduleName name of the module
   * @param key unique key for this instance of the module
   */
  public ModuleSpaceWebKit(TreeLogger logger, ModuleSpaceHost host,
      H scriptGlobalObject, H scriptGlobalContext, String moduleName) {
    super(logger, host, moduleName);

    // Hang on to the global execution state.
    //
    this.globalObject = scriptGlobalObject;
    this.globalContext = scriptGlobalContext;
    LowLevelWebKit.gcProtect(LowLevelWebKit.getCurrentJsContext(), scriptGlobalObject);
    LowLevelWebKit.retainJsGlobalContext(scriptGlobalContext);
  }

  @Override
  protected void doCreateNativeMethods(String jsni) {
    LowLevelWebKit.executeScript(globalContext, jsni);
  }
  @Override
  protected void createStaticDispatcher(TreeLogger logger) {
    String newScript = createNativeMethodInjector("__defineStatic", new String[] {"__arg0"}, "window.__static = __arg0;");
    LowLevelWebKit.executeScript(globalContext, newScript);
  }

  @Override
  public void dispose() {
    LowLevelWebKit.gcUnprotect(LowLevelWebKit.getCurrentJsContext(), globalObject);
    LowLevelWebKit.releaseJsGlobalContext(globalContext);
    super.dispose();
    LowLevelWebKit.cleanupWrappers(getIsolatedClassLoader());
  }

  /**
   * Invokes a native JavaScript function.
   * 
   * @param name the name of the function to invoke
   * @param jthis the function's 'this' context
   * @param types the type of each argument
   * @param args the arguments to be passed
   * @return the return value as a Object.
   */
  @Override
  protected JsValue doInvoke(String name, Object jthis, Class<?>[] types,
      Object[] args) {
    CompilingClassLoader isolatedClassLoader = getIsolatedClassLoader();

    JsValueWebKit<H> jsValueThis = new JsValueWebKit<H>();
    Class<?> jthisType = jthis == null ? Object.class : jthis.getClass();
    JsValueGlue.set(jsValueThis, isolatedClassLoader, jthisType, jthis);
    H jsthis = jsValueThis.getJsValue();

    int argc = args.length;
    List<H> argv = new ArrayList<H>(argc);
    for (int i = 0; i < argc; ++i) {
      JsValueWebKit<H> jsValue = new JsValueWebKit<H>();
      JsValueGlue.set(jsValue, isolatedClassLoader, types[i], args[i]);
      argv.add(jsValue.getJsValue());
    }

    final H curJsContext = LowLevelWebKit.getCurrentJsContext();

    H result = LowLevelWebKit.invoke(curJsContext, globalObject, name, jsthis,
        argv);
    return new JsValueWebKit<H>(result);
  }
  @Override
  protected Object getStaticDispatcher() {
    return new WebKitDispatchAdapter<H>(getIsolatedClassLoader());
  }
}
