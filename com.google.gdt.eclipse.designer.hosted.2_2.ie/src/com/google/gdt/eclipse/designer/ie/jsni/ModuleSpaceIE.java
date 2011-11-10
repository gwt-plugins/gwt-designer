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
package com.google.gdt.eclipse.designer.ie.jsni;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.internal.ole.win32.IDispatch;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.Variant;

import com.google.gdt.eclipse.designer.hosted.tdt.GWTEnvironmentUtils;
import com.google.gdt.eclipse.designer.hosted.tdt.IBrowserShellHost;
import com.google.gdt.eclipse.designer.ie.jsni.IDispatchImpl.HResultException;
import com.google.gdt.eclipse.designer.ie.util.Utils;
import com.google.gwt.dev.shell.designtime.DispatchIdOracle;
import com.google.gwt.dev.shell.designtime.JsValue;
import com.google.gwt.dev.shell.designtime.ModuleSpace;

/**
 * An implementation of {@link com.google.gwt.dev.shell.ModuleSpace} for Internet Explorer 6.
 */
public class ModuleSpaceIE extends ModuleSpace {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Invocation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<String, NativeFunctionInfo> m_nativeFunctions =
      new HashMap<String, NativeFunctionInfo>();

  private static class NativeFunctionInfo {
    private final OleAutomation m_function;
    private final int m_callId;

    public NativeFunctionInfo(OleAutomation function, int callId) {
      m_function = function;
      m_callId = callId;
    }

    public void dispose() {
      m_function.dispose();
    }
  }

  /**
   * Invoke a JavaScript function. This is instance method that caches COM things for speed.
   * 
   * @param name
   *          the name of the function
   * @param vArgs
   *          the array of arguments. vArgs[0] is the this parameter supplied to the function, which
   *          must be null if it is static.
   * @return the return value of the JavaScript function
   */
  private Variant doInvokeOnWindow(String name, Variant args[]) {
    // ensure function information object
    NativeFunctionInfo functionInfo = m_nativeFunctions.get(name);
    if (functionInfo == null) {
      // prepare id of function
      int ids[] = window.getIDsOfNames(new String[]{name});
      if (ids == null) {
        throw new RuntimeException("Could not find a native method with the signature '"
          + name
          + "'");
      }
      int functionId = ids[0];
      // prepare function and "call" property
      Variant functionVariant = window.getProperty(functionId);
      OleAutomation function = functionVariant.getAutomation();
      int callId = function.getIDsOfNames(new String[]{"call"})[0];
      // dispose function variant (we have automation) and fill information object
      functionVariant.dispose();
      functionInfo = new NativeFunctionInfo(function, callId);
      m_nativeFunctions.put(name, functionInfo);
    }
    // invoke function
    return functionInfo.m_function.invoke(functionInfo.m_callId, args);
  }

  /**
   * Invoke a JavaScript function. The static function exists to allow platform-dependent code to
   * make JavaScript calls without having a ModuleSpaceIE6 (and all that entails) if it is not
   * required.
   * 
   * @param window
   *          the window containing the function
   * @param name
   *          the name of the function
   * @param vArgs
   *          the array of arguments. vArgs[0] is the this parameter supplied to the function, which
   *          must be null if it is static.
   * @return the return value of the JavaScript function
   */
  protected Variant doInvokeOnWindow2(OleAutomation window, String name, Variant[] vArgs) {
    OleAutomation funcObj = null;
    Variant funcObjVar = null;
    try {
      // Get the function object and its 'call' method.
      //
      int[] ids = window.getIDsOfNames(new String[]{name});
      if (ids == null) {
        throw new RuntimeException("Could not find a native method with the signature '"
          + name
          + "'");
      }
      int functionId = ids[0];
      funcObjVar = window.getProperty(functionId);
      funcObj = funcObjVar.getAutomation();
      int callDispId = funcObj.getIDsOfNames(new String[]{"call"})[0];
      // Invoke it and return the result.
      //
      return funcObj.invoke(callDispId, vArgs);
    } finally {
      if (funcObjVar != null) {
        funcObjVar.dispose();
      }
      if (funcObj != null) {
        funcObj.dispose();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance 
  //
  ////////////////////////////////////////////////////////////////////////////
  private final OleAutomation window;

  /**
   * Constructs a browser interface for use with an IE6 'window' automation object.
   * 
   * @param moduleName
   */
  public ModuleSpaceIE(IBrowserShellHost bsHost, IDispatch scriptFrameWindow, String moduleName)
      throws Exception {
    super(bsHost, moduleName);
    window = Utils.newOleAutomation(scriptFrameWindow);
  }

  @Override
  public void dispose() throws Exception {
    for (NativeFunctionInfo function : m_nativeFunctions.values()) {
      function.dispose();
    }
    // Dispose everything else.
    if (window != null) {
      window.dispose();
    }
    super.dispose();
    IDispatchProxy.clearIDispatchProxyRefs(getIsolatedClassLoader());
    for (int i = 0; i < 2; i++) {
      if (!GWTEnvironmentUtils.DEVELOPERS_HOST) {
        System.gc();
      }
      System.runFinalization();
      JsValue.mainThreadCleanup();
    }
  }

  /**
   * Invokes a native javascript function.
   * 
   * @param name
   *          the name of the function to invoke
   * @param jthis
   *          the function's 'this' context
   * @param types
   *          the type of each argument
   * @param args
   *          the arguments to be passed
   * @return the return value as a Variant.
   */
  @Override
  protected JsValue doInvoke(String name, Object jthis, Class<?>[] types, Object[] args)
      throws Throwable {
    Variant[] vArgs = null;
    try {
      ClassLoader isolatedClassLoader = getIsolatedClassLoader();
      DispatchIdOracle dispatchIdOracle = getDispatchIdOracle();
      // Build the argument list, including 'jthis'.
      //
      int len = args.length;
      vArgs = new Variant[len + 1];
      Class<?> jthisType = jthis == null ? Object.class : jthis.getClass();
      vArgs[0] =
          SwtOleGlue.convertObjectToVariant(isolatedClassLoader, dispatchIdOracle, jthisType, jthis);
      for (int i = 0; i < len; ++i) {
        vArgs[i + 1] =
            SwtOleGlue.convertObjectToVariant(
              isolatedClassLoader,
              dispatchIdOracle,
              types[i],
              args[i]);
      }
      Variant result = doInvokeOnWindow(name, vArgs);
      try {
        return new JsValueIE6(result);
      } finally {
        if (result != null) {
          result.dispose();
        }
      }
    } finally {
      // We allocated variants for all arguments, so we must dispose them all.
      //
      for (int i = 0; i < vArgs.length; ++i) {
        if (vArgs[i] != null) {
          vArgs[i].dispose();
        }
      }
    }
  }

  @Override
  protected void doCreateNativeMethods(String jsni) {
    checkedExecute(jsni);
  }

  private void checkedExecute(String jsni) {
    try {
      Variant result = execute(jsni);
      if (result != null) {
        result.dispose();
      }
    } catch (RuntimeException e) {
      throw new RuntimeException("Failed to create JSNI methods", e);
    }
  }

  @Override
  protected void createStaticDispatcher() {
    checkedExecute("function __defineStatic(__arg0) { window.__static = __arg0; }");
  }

  @Override
  protected Object getStaticDispatcher() throws Exception {
    return new IDispatchProxy(getIsolatedClassLoader(), getDispatchIdOracle());
  }

  /**
   * On IE6, we currently have no way of throwing arbitrary exception objects into JavaScript. What
   * we throw in exception cases is an exception not under our exact control, so the best we can do
   * is match descriptions to indicate a match. In practice this works well.
   */
  @Override
  protected boolean isExceptionSame(Throwable original, Object exception) {
    Throwable caught;
    try {
      HResultException hre = new HResultException(original);
      RuntimeException jse = createJavaScriptException(getIsolatedClassLoader(), exception);
      Method method = jse.getClass().getMethod("getDescription");
      String description = (String) method.invoke(jse);
      return hre.getMessage().equals(description);
    } catch (SecurityException e) {
      caught = e;
    } catch (NoSuchMethodException e) {
      caught = e;
    } catch (IllegalArgumentException e) {
      caught = e;
    } catch (IllegalAccessException e) {
      caught = e;
    } catch (InvocationTargetException e) {
      caught = e;
    } catch (Exception e) {
      caught = e;
    }
    throw new RuntimeException("Failed to invoke JavaScriptException.getDescription()", caught);
  }

  private Variant execute(String code) {
    int[] dispIds = window.getIDsOfNames(new String[]{"execScript", "code"});
    Variant[] vArgs = new Variant[1];
    vArgs[0] = new Variant(code);
    int[] namedArgs = new int[1];
    namedArgs[0] = dispIds[1];
    Variant result = window.invoke(dispIds[0], vArgs, namedArgs);
    vArgs[0].dispose();
    if (result == null) {
      String lastError = window.getLastError();
      throw new RuntimeException("Error (" + lastError + ") executing JavaScript:\n" + code);
    }
    return result;
  }

  /**
   * Create a JavaScriptException object. This must be done reflectively, since this class will have
   * been loaded from a ClassLoader other than the session's thread.
   */
  private static RuntimeException createJavaScriptException(ClassLoader cl, Object exception) {
    Exception caught;
    try {
      Class<?> javaScriptExceptionClass =
          Class.forName("com.google.gwt.core.client.JavaScriptException", true, cl);
      Constructor<?> ctor = javaScriptExceptionClass.getDeclaredConstructor(Object.class);
      return (RuntimeException) ctor.newInstance(new Object[]{exception});
    } catch (InstantiationException e) {
      caught = e;
    } catch (IllegalAccessException e) {
      caught = e;
    } catch (SecurityException e) {
      caught = e;
    } catch (ClassNotFoundException e) {
      caught = e;
    } catch (NoSuchMethodException e) {
      caught = e;
    } catch (IllegalArgumentException e) {
      caught = e;
    } catch (InvocationTargetException e) {
      caught = e;
    }
    throw new RuntimeException("Error creating JavaScriptException", caught);
  }
}
