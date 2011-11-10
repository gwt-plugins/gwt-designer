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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.google.gwt.dev.shell.CompilingClassLoader;

/**
 * Implements all native / low-level functions for Mac/Safari hosted mode.
 * 
 * TODO (knorton): Consider changing the APIs to not have to take a jsContext; instead the context
 * could always be pulled from top-of-stack in the wrapper functions and passed into the native
 * functions.
 */
public class LowLevelWebKit {
  /**
   * Interface by which the native code interacts with a Java Method.
   */
  public interface DispatchMethod<H extends Number> {
    H invoke(H jsContext, H jsthis, List<H> jsargs, List<H> exception);
  }
  /**
   * Interface by which the native code interacts with a Java Object.
   * 
   * TODO (knorton): Add additional argument for an exception array (like in
   * {@link DispatchMethod#invoke(int, int, int[], int[])}). An example of where this would be
   * immediately helpful is in {@link BrowserWidgetSaf}.
   */
  public interface DispatchObject<H extends Number> {
    H getField(H jsContext, String name);

    Object getTarget();

    void setField(H jsContext, String name, H value);
  }

  /**
   * Stores a map from DispatchObject/DispatchMethod to the live underlying jsval. This is used to
   * both preserve identity for the same Java Object and also prevent GC.
   * 
   * Access must be synchronized because WebKit can finalize on a foreign thread.
   */
  // mitin_aa: this cache mechanic is wrong because the stored js-value can be GC'ed by WebKit 
  // but in Java code we know nothing about this fact because the cache entry would be 
  // removed by finalizing of js-object but until now stored js-value could be used in some 
  // java-js operation. For example, invoking wrapped java method as js-function.
  // Not using any cache won't get the hosted mode slow and won't produce leaks.
  // static Map<Object, Number> sObjectToJsval = new IdentityHashMap<Object, Number>();
  private static boolean initialized = false;
  private static final ThreadLocal<Stack<Number>> jsContextStack = new ThreadLocal<Stack<Number>>();
  private static boolean jsValueProtectionCheckingEnabled;

  public static <H extends Number> H executeScript(H jsContext, String script) {
    final List<H> rval = new ArrayList<H>(1);
    if (!executeScriptWithInfoImpl(jsContext, script, null, 0, rval)) {
      throw new RuntimeException("Failed to execute script: " + script);
    }
    return rval.get(0);
  }

  public static <H extends Number> H executeScriptWithInfo(H jsContext,
      String script,
      String url,
      int line) {
    final List<H> rval = new ArrayList<H>(1);
    if (!executeScriptWithInfoImpl(jsContext, script, url, line, rval)) {
      throw new RuntimeException(url + "(" + line + "): Failed to execute script: " + script);
    }
    return rval.get(0);
  }

  public static native <H extends Number> void gcProtect(H jsContext, H jsValue);

  public static native <H extends Number> void gcUnprotect(H jsContext, H jsValue);

  @SuppressWarnings("unchecked")
  public static <H extends Number> H getCurrentJsContext() {
    Stack<Number> stack = jsContextStack.get();
    if (stack == null) {
      throw new RuntimeException("No JSContext stack on this thread.");
    }
    return (H) stack.peek();
  }

  public static <H extends Number> H getGlobalJsObject(H jsContext) {
    final List<H> rval = new ArrayList<H>(1);
    if (!getGlobalJsObjectImpl(jsContext, rval)) {
      throw new RuntimeException("Unable to get JavaScript global object.");
    }
    return rval.get(0);
  }

  public static native <H extends Number> H getJsNull(H jsContext);

  public static native <H extends Number> H getJsUndefined(H jsContext);

  public static native <H extends Number> String getTypeString(H jsContext, H jsValue);

  public static synchronized void init() {
    if (initialized) {
      return;
    }
    if (!initImpl(DispatchObject.class, DispatchMethod.class, LowLevelWebKit.class)) {
      throw new RuntimeException("Unable to initialize LowLevelWebKit");
    }
    jsValueProtectionCheckingEnabled = isJsValueProtectionCheckingEnabledImpl();
    initialized = true;
  }

  public static <H extends Number> H invoke(H jsContext,
      H jsScriptObject,
      String methodName,
      H thisObj,
      List<H> args) {
    final List<H> rval = new ArrayList<H>(1);
    if (!invokeImpl(jsContext, jsScriptObject, methodName, thisObj, args, args.size(), rval)) {
      throw new RuntimeException("Failed to invoke native method: "
        + methodName
        + " with "
        + args.size()
        + " arguments.");
    }
    return rval.get(0);
  }

  public static <H extends Number> boolean isDispatchObject(H jsContext, H jsValue) {
    final boolean[] rval = new boolean[1];
    if (!isDispatchObjectImpl(jsContext, jsValue, rval)) {
      throw new RuntimeException("Failed isDispatchObject.");
    }
    return rval[0];
  }

  public static native <H extends Number> boolean isJsBoolean(H jsContext, H jsValue);

  public static native <H extends Number> boolean isJsNull(H jsContext, H jsValue);

  public static native <H extends Number> boolean isJsNumber(H jsContext, H jsValue);

  public static native <H extends Number> boolean isJsObject(H jsContext, H jsValue);

  public static <H extends Number> boolean isJsString(H jsContext, H jsValue) {
    final boolean rval[] = new boolean[1];
    if (!isJsStringImpl(jsContext, jsValue, rval)) {
      throw new RuntimeException("Failed isJsString.");
    }
    return rval[0];
  }

  public static native <H extends Number> boolean isJsUndefined(H jsContext, H jsValue);

  public static <H extends Number> void popJsContext(H expectedJsContext) {
    final Stack<Number> stack = jsContextStack.get();
    if (stack == null) {
      throw new RuntimeException("No JSContext stack on this thread.");
    }
    if (!stack.pop().equals(expectedJsContext)) {
      throw new RuntimeException("Popping JSContext returned an unxpected value.");
    }
  }

  public static <H extends Number> void pushJsContext(H jsContext) {
    Stack<Number> stack = jsContextStack.get();
    if (stack == null) {
      stack = new Stack<Number>();
      jsContextStack.set(stack);
    }
    stack.push(jsContext);
  }

  public static native <H extends Number> void releaseJsGlobalContext(H jsContext);

  public static native <H extends Number> void retainJsGlobalContext(H jsContext);

  public static <H extends Number> boolean toBoolean(H jsContext, H jsValue) {
    boolean[] rval = new boolean[1];
    if (!toBooleanImpl(jsContext, jsValue, rval)) {
      throw new RuntimeException("Failed to coerce to boolean value.");
    }
    return rval[0];
  }

  public static <H extends Number> byte toByte(H jsContext, H jsValue) {
    return (byte) toNumber(jsContext, jsValue, "byte");
  }

  public static <H extends Number> char toChar(H jsContext, H jsValue) {
    return (char) toNumber(jsContext, jsValue, "char");
  }

  public static <H extends Number> double toDouble(H jsContext, H jsValue) {
    return toNumber(jsContext, jsValue, "double");
  }

  public static <H extends Number> float toFloat(H jsContext, H jsValue) {
    return (float) toNumber(jsContext, jsValue, "float");
  }

  public static <H extends Number> int toInt(H jsContext, H jsValue) {
    return (int) toNumber(jsContext, jsValue, "int");
  }

  public static <H extends Number> H toJsBoolean(H jsContext, boolean value) {
    final List<H> rval = new ArrayList<H>(1);
    if (!toJsBooleanImpl(jsContext, value, rval)) {
      throw new RuntimeException("Failed to convert Boolean value: " + String.valueOf(value));
    }
    return rval.get(0);
  }

  public static <H extends Number> H toJsNumber(H jsContext, double value) {
    final List<H> rval = new ArrayList<H>(1);
    if (!toJsNumberImpl(jsContext, value, rval)) {
      throw new RuntimeException("Failed to convert Double value: " + String.valueOf(value));
    }
    return rval.get(0);
  }

  public static <H extends Number> H toJsString(H jsContext, String value) {
    final List<H> rval = new ArrayList<H>(1);
    if (!toJsStringImpl(jsContext, value, rval)) {
      throw new RuntimeException("Failed to convert String value: " + String.valueOf(value));
    }
    return rval.get(0);
  }

  public static <H extends Number> long toLong(H jsContext, H jsValue) {
    return (long) toNumber(jsContext, jsValue, "long");
  }

  public static <H extends Number> short toShort(H jsContext, H jsValue) {
    return (short) toNumber(jsContext, jsValue, "short");
  }

  public static <H extends Number> String toString(H jsContext, H jsValue) {
    final String[] rval = new String[1];
    if (!toStringImpl(jsContext, jsValue, rval)) {
      throw new RuntimeException("Failed to coerce to String value");
    }
    return rval[0];
  }

  public static <H extends Number> DispatchObject<?> unwrapDispatchObject(H jsContext, H jsValue) {
    final List<DispatchObject<?>> rval = new ArrayList<DispatchObject<?>>(1);
    if (!unwrapDispatchObjectImpl(jsContext, jsValue, rval)) {
      throw new RuntimeException("Failed to unwrap DispatchObject.");
    }
    return rval.get(0);
  }

  public static <H extends Number> H wrapDispatchMethod(H jsContext,
      String name,
      DispatchMethod<?> dispatch) {
    final List<H> rval = new ArrayList<H>(1);
    if (!wrapDispatchMethodImpl(jsContext, name, dispatch, rval)) {
      throw new RuntimeException("Failed to wrap DispatchMethod.");
    }
    return rval.get(0);
  }

  public static <H extends Number> H wrapDispatchObject(H jsContext, DispatchObject<?> dispatcher) {
    final List<H> rval = new ArrayList<H>(1);
    if (!wrapDispatchObjectImpl(jsContext, dispatcher, rval)) {
      throw new RuntimeException("Failed to wrap DispatchObject.");
    }
    return rval.get(0);
  }

  static native <H extends Number> boolean isGcProtected(H jsValue);

  /**
   * Enables checking of JSValueRef protect/unprotect calls to ensure calls are properly matched.
   * See ENABLE_JSVALUE_PROTECTION_CHECKING in trace.h to enable this feature.
   * 
   * @return whether JSValue protection checking is enabled
   */
  static boolean isJsValueProtectionCheckingEnabled() {
    return jsValueProtectionCheckingEnabled;
  }

  private static native <H extends Number> boolean executeScriptWithInfoImpl(H jsContext,
      String script,
      String url,
      int line,
      List<H> rval);

  private static native <H extends Number> boolean getGlobalJsObjectImpl(H jsContext, List<H> rval);

  private static native <H extends Number> boolean initImpl(Class<?> dispatchObjectClass,
      Class<?> dispatchMethodClass,
      Class<LowLevelWebKit> lowLevelSafClass);

  private static native <H extends Number> boolean invokeImpl(H jsContext,
      H jsScriptObject,
      String methodName,
      H thisObj,
      List<H> args,
      int argsLength,
      List<H> rval);

  private static native <H extends Number> boolean isDispatchObjectImpl(H jsContext,
      H jsValue,
      boolean[] rval);

  private static native <H extends Number> boolean isJsStringImpl(H jsContext,
      H jsValue,
      boolean[] rval);

  private static native boolean isJsValueProtectionCheckingEnabledImpl();

  private static native <H extends Number> boolean toBooleanImpl(H jsContext,
      H jsValue,
      boolean[] rval);

  private static native <H extends Number> boolean toDoubleImpl(H jsContext,
      H jsValue,
      double[] rval);

  private static native <H extends Number> boolean toJsBooleanImpl(H jsContext,
      boolean value,
      List<H> rval);

  private static native <H extends Number> boolean toJsNumberImpl(H jsContext,
      double value,
      List<H> rval);

  private static native <H extends Number> boolean toJsStringImpl(H jsContext,
      String value,
      List<H> rval);

  private static <H extends Number> double toNumber(H jsContext, H jsValue, String typeName) {
    double[] rval = new double[1];
    if (!toDoubleImpl(jsContext, jsValue, rval)) {
      throw new RuntimeException("Failed to coerce to " + typeName + " value");
    }
    return rval[0];
  }

  private static native <H extends Number> boolean toStringImpl(H jsContext,
      H jsValue,
      String[] rval);

  private static native <H extends Number> boolean unwrapDispatchObjectImpl(H jsContext,
      H jsValue,
      List<?/*DispatchObject<?>*/> rval);

  private static native <H extends Number> boolean wrapDispatchMethodImpl(H jsContext,
      String name,
      DispatchMethod<?> dispatch,
      List<H> rval);

  private static native <H extends Number> boolean wrapDispatchObjectImpl(H jsContext,
      DispatchObject<?> obj,
      List<H> rval);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tracking/Cleanup
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * GWT keeps references to {@link WebKitDispatchAdapter} in native code in JS-objects. In turn,
   * JS-objects are GC-protected in Java and released using JsValueWebKit.finalize() which
   * unprotects JS-object and when it GCed by JS GC it deletes the global reference to
   * {@link WebKitDispatchAdapter}. But sometimes JS-object won't GC-ed in time, so we've got memory
   * leak because {@link WebKitDispatchAdapter} has a reference to {@link CompilingClassLoader}
   * which could be huge. The workaround is to track references to {@link WebKitDispatchAdapter}s
   * and break {@link CompilingClassLoader} references. See also a Cleanup section in
   * IDispatchProxy.
   */
  private static CountedSet<DispatchObject<?>> m_trackingWrappers =
      new CountedSet<DispatchObject<?>>();

  static void registerWrapper(DispatchObject<?> wrapper) {
    synchronized (m_trackingWrappers) {
      if (wrapper instanceof WebKitDispatchAdapter) {
        m_trackingWrappers.add(wrapper);
      }
    }
  }

  static void releaseWrapper(DispatchObject<?> wrapper) {
    synchronized (m_trackingWrappers) {
      m_trackingWrappers.remove(wrapper);
    }
  }

  public static void cleanupWrappers(CompilingClassLoader cl) {
    synchronized (m_trackingWrappers) {
      List<DispatchObject<?>> removingRefs = new ArrayList<DispatchObject<?>>();
      for (DispatchObject<?> refObj : m_trackingWrappers) {
        if (refObj instanceof WebKitDispatchAdapter) {
          WebKitDispatchAdapter<?> adapter = (WebKitDispatchAdapter<?>) refObj;
          WeakReference<CompilingClassLoader> classLoaderRef = adapter.classLoaderRef;
          if (classLoaderRef == null || classLoaderRef.get() == null || classLoaderRef.get() == cl) {
            // [mitin_aa] I'm not sure why WKDA with null classLoaderRef are still in this set. 
            adapter.classLoaderRef = null;
            adapter.javaDispatch = null;
            removingRefs.add(adapter);
          }
        }
      }
      for (DispatchObject<?> refObj : removingRefs) {
        m_trackingWrappers.remove(refObj);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner classes
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Set with entries counting (aka Bag aka Multiset).
   */
  private static class CountedSet<E> implements Iterable<E> {
    private final Map<E, Integer> m_set;

    public CountedSet() {
      m_set = new HashMap<E, Integer>();
    }

    public boolean add(E e) {
      Integer count = m_set.get(e);
      if (count == null) {
        m_set.put(e, 1);
        return true;
      } else {
        m_set.put(e, Integer.valueOf(count.intValue() + 1));
        return false;
      }
    }

    public boolean remove(E o) {
      if (!m_set.containsKey(o)) {
        return true;
      }
      Integer count = m_set.get(o);
      int descreased = count.intValue() - 1;
      if (descreased == 0) {
        m_set.remove(o);
        return true;
      } else {
        m_set.put(o, Integer.valueOf(descreased));
      }
      return false;
    }

    public Iterator<E> iterator() {
      return m_set.keySet().iterator();
    }
  }
}
