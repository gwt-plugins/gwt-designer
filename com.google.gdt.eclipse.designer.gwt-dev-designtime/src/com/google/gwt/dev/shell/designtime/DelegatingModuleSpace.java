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
package com.google.gwt.dev.shell.designtime;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.javac.JsniMethod;
import com.google.gwt.dev.shell.CompilingClassLoader;
import com.google.gwt.dev.shell.DispatchIdOracle;
import com.google.gwt.dev.shell.JsValue;
import com.google.gwt.dev.shell.Jsni;
import com.google.gwt.dev.shell.ModuleSpace;
import com.google.gwt.dev.shell.ModuleSpaceHost;
import com.google.gwt.dev.shell.ShellModuleSpaceHost;
import com.google.gwt.dev.util.Name.BinaryName;
import com.google.gwt.dev.util.collect.HashMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public final class DelegatingModuleSpace extends ModuleSpace {

  private Object delegate;
  private final Map<String, Method> invokeNativeMethods = new HashMap<String, Method>();

  DelegatingModuleSpace(TreeLogger logger, ModuleSpaceHost host,
      String moduleName, Object delegate) {
    super(logger, host, moduleName);
    this.delegate = delegate;
  }

  public void createNativeMethods(TreeLogger logger,
      List<JsniMethod> jsniMethods, DispatchIdOracle dispatchIdOracle) {
    if (jsniMethods.isEmpty()) {
      return;
    }
    StringBuilder jsni = new StringBuilder();
    for (JsniMethod jsniMethod : jsniMethods) {
      String body = getJavaScriptForHostedMode(dispatchIdOracle, jsniMethod);
      if (body == null) {
        // The error has been logged; just ignore it for now.
        continue;
      }
      jsni.append("// " + jsniMethod.location() + ":" + jsniMethod.line()
          + "\n");
      jsni.append("this[\"" + jsniMethod.name() + "\"] = function(");
      String[] paramNames = jsniMethod.paramNames();
      for (int i = 0; i < paramNames.length; ++i) {
        if (i > 0) {
          jsni.append(", ");
        }
        jsni.append(paramNames[i]);
      }
      jsni.append(") {\n");
      jsni.append(body);
      jsni.append("};\n\n");
    }
    doCreateNativeMethods(jsni.toString());
  }

  /**
   * HACK: use reflection to support both GWT 2.2 & GWT trunk, as 2.2+ has
   * com.google.gwt.dev.shell.Jsni.getJavaScriptForHostedMode() signature
   * changed.
   */
  private static Method gjsfhmMethod = null;
  private static boolean is22p = false;
  private static String getJavaScriptForHostedMode(
      DispatchIdOracle dispatchIdOracle, JsniMethod jsniMethod) {
    try {
      // get method lazily
      if (gjsfhmMethod == null) {
        Class<?> jsniClass = Jsni.class;
        // try 2.2
        try {
          gjsfhmMethod = jsniClass.getMethod("getJavaScriptForHostedMode",
              new Class[]{
                  TreeLogger.class, DispatchIdOracle.class, JsniMethod.class});
        } catch (NoSuchMethodException e) {
          // try 2.2+
          try {
            gjsfhmMethod = jsniClass.getMethod("getJavaScriptForHostedMode",
                new Class[]{DispatchIdOracle.class, JsniMethod.class});
            is22p = true;
          } catch (NoSuchMethodException e1) {
            // ignore and allow fail later
          }
        }
      }
      if (is22p) {
        return (String) gjsfhmMethod.invoke(null, new Object[]{dispatchIdOracle, jsniMethod});
      } else {
        return (String) gjsfhmMethod.invoke(null, new Object[]{TreeLogger.NULL, dispatchIdOracle, jsniMethod});
      }
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private void doCreateNativeMethods(String jsni) {
    invokeOnDelegate("doCreateNativeMethods", new Class[]{String.class},
        new Object[]{jsni});
  }

  @Override
  protected void createStaticDispatcher(TreeLogger logger) {
    // do nothing, we don't invoke onLoad()
    throw new RuntimeException("Should not invoke this method on this class.");
  }

  @Override
  protected JsValue doInvoke(String name, Object jthis, Class<?>[] types,
      Object[] args) throws Throwable {
    // all invocations should be passed into delegate
    throw new RuntimeException("Should not invoke this method on this class.");
  }

  @Override
  protected Object getStaticDispatcher() {
    // do nothing, we don't invoke onLoad()
    throw new RuntimeException("Should not invoke this method on this class.");
  }

  public void invalidateRebind(String typeName) {
    ShellModuleSpaceHost sHost = (ShellModuleSpaceHost) host;
    String sourceName = BinaryName.toSourceName(typeName);
    sHost.invalidateRebind(sourceName);
  }

  public boolean invokeNativeBoolean(String name, Object jthis,
      java.lang.Class<?>[] types, Object[] args) throws Throwable {
    return (Boolean) invokeNativeOnDelegate("invokeNativeBoolean", name, jthis,
        types, args);
  }

  public byte invokeNativeByte(String name, Object jthis,
      java.lang.Class<?>[] types, Object[] args) throws Throwable {
    return (Byte) invokeNativeOnDelegate("invokeNativeByte", name, jthis,
        types, args);
  }

  public char invokeNativeChar(String name, Object jthis,
      java.lang.Class<?>[] types, Object[] args) throws Throwable {
    return (Character) invokeNativeOnDelegate("invokeNativeChar", name, jthis,
        types, args);
  }

  public double invokeNativeDouble(String name, Object jthis,
      java.lang.Class<?>[] types, Object[] args) throws Throwable {
    return (Double) invokeNativeOnDelegate("invokeNativeDouble", name, jthis,
        types, args);
  }

  public float invokeNativeFloat(String name, Object jthis,
      java.lang.Class<?>[] types, Object[] args) throws Throwable {
    return (Float) invokeNativeOnDelegate("invokeNativeFloat", name, jthis,
        types, args);
  }

  public int invokeNativeInt(String name, Object jthis,
      java.lang.Class<?>[] types, Object[] args) throws Throwable {
    return (Integer) invokeNativeOnDelegate("invokeNativeInt", name, jthis,
        types, args);
  }

  public long invokeNativeLong(String name, Object jthis,
      java.lang.Class<?>[] types, Object[] args) throws Throwable {
    return (Long) invokeNativeOnDelegate("invokeNativeLong", name, jthis,
        types, args);
  }

  public Object invokeNativeObject(String name, Object jthis,
      java.lang.Class<?>[] types, Object[] args) throws Throwable {
    return invokeNativeOnDelegate("invokeNativeObject", name, jthis, types,
        args);
  }

  public short invokeNativeShort(String name, Object jthis,
      java.lang.Class<?>[] types, Object[] args) throws Throwable {
    return (Short) invokeNativeOnDelegate("invokeNativeShort", name, jthis,
        types, args);
  }

  public void invokeNativeVoid(String name, Object jthis,
      java.lang.Class<?>[] types, Object[] args) throws Throwable {
    invokeNativeOnDelegate("invokeNativeVoid", name, jthis, types, args);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T rebindAndCreate(String requestedClassName)
      throws UnableToCompleteException {
    // delegate to setup class loader
    return (T) invokeOnDelegate("rebindAndCreate", new Class[]{String.class},
        new Object[]{requestedClassName});
  }

  public <T> T rebindAndCreate0(String requestedClassName)
      throws UnableToCompleteException {
    // callback from delegate: pass to super, it will go with our class loader
    return super.rebindAndCreate(requestedClassName);
  }

  public DispatchIdOracle getDispatchIdOracle() throws Exception {
    CompilingClassLoader classLoader = getIsolatedClassLoader();
    return classLoader;
  }

  public void dispose() {
    super.dispose();
    delegate = null;
  }

  public static Throwable activeException() throws Exception {
    ThreadLocal<Throwable> caughtFieldLocal = getExceptionFieldValue("sCaughtJavaExceptionObject");
    return caughtFieldLocal.get();
  }

  public static void resetActiveException() throws Exception {
    ThreadLocal<Throwable> caughtFieldLocal = getExceptionFieldValue("sCaughtJavaExceptionObject");
    caughtFieldLocal.set(null);
  }

  private static ThreadLocal<Throwable> getExceptionFieldValue(String fieldName)
      throws Exception {
    Field caughtField = DelegatingModuleSpace.class.getSuperclass().getDeclaredField(
        fieldName);
    caughtField.setAccessible(true);
    @SuppressWarnings("unchecked")
    ThreadLocal<Throwable> caughtFieldLocal = (ThreadLocal<Throwable>) caughtField.get(null);
    return caughtFieldLocal;
  }

  private Object invokeNativeOnDelegate(String methodName, String name,
      Object jthis, Class<?>[] types, Object[] args) {
    // do some caching
    Method method = invokeNativeMethods.get(methodName);
    if (method == null) {
      try {
        method = getDelegateMethod(methodName, new Class[]{
            String.class, Object.class, Class[].class, Object[].class});
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
      invokeNativeMethods.put(methodName, method);
    }
    return invokeOnDelegate(method, new Object[]{name, jthis, types, args});
  }

  private Object invokeOnDelegate(String methodName, Class<?>[] argTypes,
      Object[] args) {
    try {
      Method method = getDelegateMethod(methodName, argTypes);
      return invokeOnDelegate(method, args);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private Object invokeOnDelegate(Method method, Object[] args) {
    try {
      return method.invoke(delegate, args);
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private Method getDelegateMethod(String methodName, Class<?>[] argTypes)
      throws NoSuchMethodException {
    Class<? extends Object> delegateClass = delegate.getClass();
    Method method;
    try {
      method = delegateClass.getMethod(methodName, argTypes);
    } catch (NoSuchMethodException e) {
      // no public, try declared
      method = delegateClass.getDeclaredMethod(methodName, argTypes);
    }
    method.setAccessible(true);
    return method;
  }

}
