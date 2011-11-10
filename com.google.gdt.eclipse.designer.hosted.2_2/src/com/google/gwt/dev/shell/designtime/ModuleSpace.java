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
package com.google.gwt.dev.shell.designtime;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import com.google.gdt.eclipse.designer.hosted.tdt.IBrowserShellHost;

/**
 * The interface to the low-level browser, this class serves as a 'domain' for a module, loading all
 * of its classes in a separate, isolated class loader. This allows us to run multiple modules, both
 * in succession and simultaneously.
 */
public abstract class ModuleSpace {
  /**
   * Logger is thread local.
   */
  private static ThreadLocal<Object> threadLocalLogger = new ThreadLocal<Object>();
  private static Class<?> delegatingModuleSpaceClass;

  public static void setDelegatingModuleSpaceClass(Class<?> moduleSpaceClass) {
    delegatingModuleSpaceClass = moduleSpaceClass;
  }

  public static void setThrownJavaException(Throwable t) {
    try {
      ReflectionUtils.invokeMethod(
        delegatingModuleSpaceClass,
        "setThrownJavaException(java.lang.Throwable)",
        t);
    } catch (Throwable e) {
      ReflectionUtils.propagate(e);
    }
  }

  protected Object msHost;
  private Object delegate;
  private final IBrowserShellHost bsHost;

  protected ModuleSpace(IBrowserShellHost bsHost, String moduleName) throws Exception {
    this.bsHost = bsHost;
    this.msHost = bsHost.createModuleSpaceHost(moduleName);
    this.delegate = bsHost.createModuleSpace(moduleName, msHost, this);
  }

  public void dispose() throws Exception {
    ClassLoader classLoader = getIsolatedClassLoader();
    WrappersCache.clear(classLoader);
    // Clear our class loader.
    ReflectionUtils.invokeMethod2(delegate, "dispose");
    msHost = null;
  }

  public static void setLogger(Object logger) {
    threadLocalLogger.set(logger);
  }

  protected abstract void doCreateNativeMethods(String jsni);

  public boolean invokeNativeBoolean(String name, Object jthis, Class<?>[] types, Object[] args)
      throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a boolean");
    Boolean value = JsValueGlue.get(result, getIsolatedClassLoader(), boolean.class, msgPrefix);
    if (value == null) {
      throw new RuntimeException(msgPrefix + ": return value null received, expected a boolean");
    }
    return value.booleanValue();
  }

  public byte invokeNativeByte(String name, Object jthis, Class<?>[] types, Object[] args)
      throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a byte");
    Byte value = JsValueGlue.get(result, null, Byte.TYPE, msgPrefix);
    if (value == null) {
      throw new RuntimeException(msgPrefix + ": return value null received, expected a byte");
    }
    return value.byteValue();
  }

  public char invokeNativeChar(String name, Object jthis, Class<?>[] types, Object[] args)
      throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a char");
    Character value = JsValueGlue.get(result, null, Character.TYPE, msgPrefix);
    if (value == null) {
      throw new RuntimeException(msgPrefix + ": return value null received, expected a char");
    }
    return value.charValue();
  }

  public double invokeNativeDouble(String name, Object jthis, Class<?>[] types, Object[] args)
      throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a double");
    Double value = JsValueGlue.get(result, null, Double.TYPE, msgPrefix);
    if (value == null) {
      throw new RuntimeException(msgPrefix + ": return value null received, expected a double");
    }
    return value.doubleValue();
  }

  public float invokeNativeFloat(String name, Object jthis, Class<?>[] types, Object[] args)
      throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a float");
    Float value = JsValueGlue.get(result, null, Float.TYPE, msgPrefix);
    if (value == null) {
      throw new RuntimeException(msgPrefix + ": return value null received, expected a float");
    }
    return value.floatValue();
  }

  public int invokeNativeInt(String name, Object jthis, Class<?>[] types, Object[] args)
      throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "an int");
    Integer value = JsValueGlue.get(result, null, Integer.TYPE, msgPrefix);
    if (value == null) {
      throw new RuntimeException(msgPrefix + ": return value null received, expected an int");
    }
    return value.intValue();
  }

  public long invokeNativeLong(String name, Object jthis, Class<?>[] types, Object[] args)
      throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a long");
    Long value = JsValueGlue.get(result, null, Long.TYPE, msgPrefix);
    if (value == null) {
      throw new RuntimeException(msgPrefix + ": return value null received, expected a long");
    }
    return value.longValue();
  }

  public Object invokeNativeObject(String name, Object jthis, Class<?>[] types, Object[] args)
      throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a Java object");
    return JsValueGlue.get(result, getIsolatedClassLoader(), Object.class, msgPrefix);
  }

  public short invokeNativeShort(String name, Object jthis, Class<?>[] types, Object[] args)
      throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    String msgPrefix = composeResultErrorMsgPrefix(name, "a short");
    Short value = JsValueGlue.get(result, null, Short.TYPE, msgPrefix);
    if (value == null) {
      throw new RuntimeException(msgPrefix + ": return value null received, expected a short");
    }
    return value.shortValue();
  }

  public void invokeNativeVoid(String name, Object jthis, Class<?>[] types, Object[] args)
      throws Throwable {
    JsValue result = invokeNative(name, jthis, types, args);
    if (!result.isUndefined()) {
      /*FIXME: logger.log(
        TreeLogger.WARN,
        "JSNI method '"
          + name
          + "' returned a value of type "
          + result.getTypeString()
          + " but was declared void; it should not have returned a value at all",
        null);*/
    }
  }

  /**
   * Runs the module's user startup code.
   */
  public final void onLoad() throws Exception {
    // Tell the host we're ready for business.
    //
    onModuleReady();
    // Make sure we can resolve JSNI references to static Java names.
    //
    try {
      createStaticDispatcher();
      initializeStaticDispatcher();
    } catch (Throwable e) {
      throw new RuntimeException("Unable to initialize static dispatcher", e);
    }
    try {
      // Set up GWT-entry code
      Class<?> implClass = loadClassFromSourceName("com.google.gwt.core.client.impl.Impl");
      Method registerEntry = implClass.getDeclaredMethod("registerEntry");
      registerEntry.setAccessible(true);
      registerEntry.invoke(null);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  protected void initializeStaticDispatcher() throws Throwable {
    Object staticDispatch = getStaticDispatcher();
    invokeNativeVoid(
      "__defineStatic",
      null,
      new Class[]{Object.class},
      new Object[]{staticDispatch});
  }

  // GWT.create(Binder.class) generates units dynamically, and when they compiled, we need to
  // see standard GWT classes. So, we need to set project ClassLoader, it is used by TypeOracleMediator.
  public <T> T rebindAndCreate(String requestedClassName) throws Exception {
    ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(bsHost.getDevClassLoader());
    try {
      return rebindAndCreate0(requestedClassName);
    } finally {
      Thread.currentThread().setContextClassLoader(oldContextClassLoader);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T rebindAndCreate0(String requestedClassName) throws Exception {
    return (T) ReflectionUtils.invokeMethod(
      delegate,
      "rebindAndCreate0(java.lang.String)",
      requestedClassName);
  }

  public static String createNativeMethodInjector(String jsniSignature,
      String[] paramNames,
      String js) {
    String newScript = "window[\"" + jsniSignature + "\"] = function(";
    for (int i = 0; i < paramNames.length; ++i) {
      if (i > 0) {
        newScript += ", ";
      }
      newScript += paramNames[i];
    }
    newScript += ") { " + js + " };\n";
    return newScript;
  }

  /**
   * Create the __defineStatic method.
   */
  protected abstract void createStaticDispatcher();

  /**
   * Invokes a native JavaScript function.
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
  protected abstract JsValue doInvoke(String name, Object jthis, Class<?>[] types, Object[] args)
      throws Throwable;

  protected final ClassLoader getIsolatedClassLoader() throws Exception {
    return (ClassLoader) ReflectionUtils.invokeMethod2(delegate, "getIsolatedClassLoader");
  }

  protected final DispatchIdOracle getDispatchIdOracle() throws Exception {
    return bsHost.getDispatchIdOracle(delegate);
  }

  /**
   * Injects the magic needed to resolve JSNI references from module-space.
   */
  protected abstract Object getStaticDispatcher() throws Exception;

  /**
   * Invokes a native JavaScript function.
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
  protected final JsValue invokeNative(String name, Object jthis, Class<?>[] types, Object[] args)
      throws Throwable {
    // Whenever a native method is invoked, release any enqueued cleanup objects
    JsValue.mainThreadCleanup();
    JsValue result = doInvoke(name, jthis, types, args);
    // Is an exception active?
    Throwable thrown = activeException();
    if (thrown == null) {
      return result;
    }
    resetActiveException();
    scrubStackTrace(thrown);
    throw thrown;
  }

  private Throwable activeException() throws Exception {
    return (Throwable) ReflectionUtils.invokeMethod2(delegate, "activeException");
  }

  public void resetActiveException() throws Exception {
    ReflectionUtils.invokeMethod2(delegate, "resetActiveException");
  }

  /**
   * @param original
   *          the thrown exception
   * @param exception
   *          the caught exception
   */
  protected boolean isExceptionSame(Throwable original, Object exception) {
    // For most platforms, the null exception means we threw it.
    // IE overrides this.
    return exception == null;
  }

  private String composeResultErrorMsgPrefix(String name, String typePhrase) {
    return "Something other than " + typePhrase + " was returned from JSNI method '" + name + "'";
  }

  private boolean isUserFrame(StackTraceElement element) throws Exception {
    try {
      ClassLoader cl = getIsolatedClassLoader();
      String className = element.getClassName();
      Class<?> clazz = Class.forName(className, false, cl);
      if (clazz.getClassLoader() == cl) {
        // Lives in user classLoader.
        return true;
      }
      // At this point, it must be a JRE class to qualify.
      if (clazz.getClassLoader() != null || !className.startsWith("java.")) {
        return false;
      }
      if (className.startsWith("java.lang.reflect.")) {
        return false;
      }
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  /**
   * Handles loading a class that might be nested given a source type name.
   */
  private Class<?> loadClassFromSourceName(String sourceName) throws Exception {
    String toTry = sourceName;
    while (true) {
      try {
        return Class.forName(toTry, true, getIsolatedClassLoader());
      } catch (ClassNotFoundException e) {
        // Assume that the last '.' should be '$' and try again.
        //
        int i = toTry.lastIndexOf('.');
        if (i == -1) {
          throw e;
        }
        toTry = toTry.substring(0, i) + "$" + toTry.substring(i + 1);
      }
    }
  }

  /**
   * Clean up the stack trace by removing our hosting frames. But don't do this if our own frames
   * are at the top of the stack, because we may be the real cause of the exception.
   */
  private void scrubStackTrace(Throwable thrown) throws Exception {
    List<StackTraceElement> trace =
        new ArrayList<StackTraceElement>(Arrays.asList(thrown.getStackTrace()));
    boolean seenUserFrame = false;
    for (ListIterator<StackTraceElement> it = trace.listIterator(); it.hasNext();) {
      StackTraceElement element = it.next();
      if (!isUserFrame(element)) {
        if (seenUserFrame) {
          it.remove();
        }
        continue;
      }
      seenUserFrame = true;
      // Remove a JavaScriptHost.invokeNative*() frame.
      if (element.getClassName().equals("com.google.gwt.dev.shell.JavaScriptHost")) {
        if (element.getMethodName().equals("exceptionCaught")) {
          it.remove();
        } else if (element.getMethodName().startsWith("invokeNative")) {
          it.remove();
          // Also try to convert the next frame to a true native.
          if (it.hasNext()) {
            StackTraceElement next = it.next();
            if (next.getLineNumber() == -1) {
              next =
                  new StackTraceElement(next.getClassName(),
                    next.getMethodName(),
                    next.getFileName(),
                    -2);
              it.set(next);
            }
          }
        }
      }
    }
    thrown.setStackTrace(trace.toArray(new StackTraceElement[trace.size()]));
  }

  private void onModuleReady() throws Exception {
    ReflectionUtils.invokeMethod(
      msHost,
      "onModuleReady(com.google.gwt.dev.shell.ModuleSpace)",
      delegate);
  }

  public void invalidateRebind(String typeName) throws Exception {
    ReflectionUtils.invokeMethod(delegate, "invalidateRebind(java.lang.String)", typeName);
  }
}
