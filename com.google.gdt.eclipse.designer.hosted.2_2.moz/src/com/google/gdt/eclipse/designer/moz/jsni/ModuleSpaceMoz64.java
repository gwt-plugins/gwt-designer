/*
 * Copyright 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
public class ModuleSpaceMoz64 extends ModuleSpace {

  private final long window;

  /**
   * Constructs a browser interface for use with a Mozilla global window object.
   */
  public ModuleSpaceMoz64(IBrowserShellHost bsHost, long scriptGlobalObject,
      String moduleName) throws Exception {
    super(bsHost, moduleName);

    // Hang on to the parent window.
    //
    window = scriptGlobalObject;
  }

  @Override
  protected void doCreateNativeMethods(String jsni) {
    LowLevelMoz64.executeScriptWithInfo(window, jsni, "", 0);
  }
  @Override
  protected void createStaticDispatcher() {
	String newScript = createNativeMethodInjector("__defineStatic", new String[] {"__arg0"}, "window.__static = __arg0;");
    LowLevelMoz64.executeScriptWithInfo(window, newScript, "", 0);
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

    JsValueMoz64 jsthis = new JsValueMoz64();
    Class<?> jthisType = (jthis == null) ? Object.class : jthis.getClass();
    JsValueGlue.set(jsthis, isolatedClassLoader, dispatchIdOracle, jthisType, jthis);

    int argc = args.length;
    JsValueMoz64 argv[] = new JsValueMoz64[argc];
    long [] jsArgsInt = new long [argc];
    for (int i = 0; i < argc; ++i) {
      argv[i] = new JsValueMoz64();
      JsValueGlue.set(argv[i], isolatedClassLoader, dispatchIdOracle, types[i], args[i]);
      jsArgsInt[i] = argv[i].getJsRootedValue();
    }
    JsValueMoz64 returnVal = new JsValueMoz64();
    LowLevelMoz64.invoke(window, name, jsthis.getJsRootedValue(), jsArgsInt,
        returnVal.getJsRootedValue());
    return returnVal;
  }

  @Override
  protected Object getStaticDispatcher() throws Exception {
    return new GeckoDispatchAdapter64(getIsolatedClassLoader(), getDispatchIdOracle());
  }
}
