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
package com.google.gdt.eclipse.designer.moz.jsni;

import com.google.gdt.eclipse.designer.hosted.tdt.IBrowserShellHost;
import com.google.gwt.dev.shell.designtime.DispatchIdOracle;
import com.google.gwt.dev.shell.designtime.JsValue;
import com.google.gwt.dev.shell.designtime.JsValueGlue;
import com.google.gwt.dev.shell.designtime.ModuleSpace;

/**
 * An implementation of {@link com.google.gwt.dev.shell.ModuleSpace} for
 * Mozilla.
 */
public class ModuleSpaceMoz32 extends ModuleSpace {

  private final int /*long*/window;

  /**
   * Constructs a browser interface for use with a Mozilla global window object.
   */
  public ModuleSpaceMoz32(IBrowserShellHost bsHost, int scriptGlobalObject,
	      String moduleName) throws Exception {
    super(bsHost, moduleName);

    // Hang on to the parent window.
    //
    window = scriptGlobalObject;
  }

  @Override
  protected void doCreateNativeMethods(String jsni) {
    LowLevelMoz32/*64*/.executeScriptWithInfo(window, jsni, "", 0);
  }
  @Override
  protected void createStaticDispatcher() {
	String newScript = createNativeMethodInjector("__defineStatic", new String[] {"__arg0"}, "window.__static = __arg0;");
    LowLevelMoz32/*64*/.executeScriptWithInfo(window, newScript, "", 0);
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
      Object[] args) throws Exception {
    ClassLoader isolatedClassLoader = getIsolatedClassLoader();
    DispatchIdOracle dispatchIdOracle = getDispatchIdOracle();

    JsValueMoz32/*64*/ jsthis = new JsValueMoz32/*64*/();
    Class<?> jthisType = (jthis == null) ? Object.class : jthis.getClass();
    JsValueGlue.set(jsthis, isolatedClassLoader, dispatchIdOracle, jthisType, jthis);

    int argc = args.length;
    JsValueMoz32/*64*/ argv[] = new JsValueMoz32/*64*/[argc];
    int /*long*/[] jsArgsInt = new int /*long*/[argc];
    for (int i = 0; i < argc; ++i) {
      argv[i] = new JsValueMoz32/*64*/();
      JsValueGlue.set(argv[i], isolatedClassLoader, dispatchIdOracle, types[i], args[i]);
      jsArgsInt[i] = argv[i].getJsRootedValue();
    }
    JsValueMoz32/*64*/ returnVal = new JsValueMoz32/*64*/();
    LowLevelMoz32/*64*/.invoke(window, name, jsthis.getJsRootedValue(), jsArgsInt,
        returnVal.getJsRootedValue());
    return returnVal;
  }

  @Override
  protected Object getStaticDispatcher() throws Exception {
    return new GeckoDispatchAdapter32/*64*/(getIsolatedClassLoader(), getDispatchIdOracle());
  }
}
