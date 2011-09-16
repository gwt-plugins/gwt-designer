/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gdt.eclipse.designer.webkit.jsni;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.google.gdt.eclipse.designer.webkit.jsni.LowLevelWebKit.DispatchMethod;
import com.google.gwt.dev.shell.designtime.DispatchIdOracle;
import com.google.gwt.dev.shell.designtime.JsValue;
import com.google.gwt.dev.shell.designtime.JsValueGlue;
import com.google.gwt.dev.shell.designtime.MethodAdaptor;
import com.google.gwt.dev.shell.designtime.ModuleSpace;

/**
 * Wraps an arbitrary Java Method as a Dispatchable component. The class was motivated by the need
 * to expose Java objects into JavaScript.
 */
class MethodDispatch<H extends Number> implements DispatchMethod<H> {
  private final WeakReference<ClassLoader> classLoaderRef;
  private final WeakReference<DispatchIdOracle> dispatchIdOracleRef;
  private final MethodAdaptor method;

  public MethodDispatch(ClassLoader classLoader, DispatchIdOracle dispIdOracle, MethodAdaptor method) {
    this.classLoaderRef = new WeakReference<ClassLoader>(classLoader);
    this.dispatchIdOracleRef = new WeakReference<DispatchIdOracle>(dispIdOracle);
    this.method = method;
  }

  public H invoke(H jsContext, H jsthisInt, List<H> jsargsInt, List<H> exception) {
    ClassLoader classLoader = classLoaderRef.get();
    if (classLoader == null) {
      throw new RuntimeException("Invalid class loader.");
    }
    DispatchIdOracle dispatchIdOracle = dispatchIdOracleRef.get();
    if (dispatchIdOracle == null) {
      throw new RuntimeException("Invalid dispatch oracle.");
    }
    LowLevelWebKit.pushJsContext(jsContext);
    JsValue jsthis = new JsValueWebKit<H>(jsthisInt);
    JsValue jsargs[] = new JsValue[jsargsInt.size()];
    for (int i = 0; i < jsargsInt.size(); ++i) {
      jsargs[i] = new JsValueWebKit<H>(jsargsInt.get(i));
    }
    JsValueWebKit<H> returnValue = new JsValueWebKit<H>();
    try {
      Class<?>[] paramTypes = method.getParameterTypes();
      int argc = paramTypes.length;
      Object args[] = new Object[argc];
      // too many arguments are ok: the extra will be silently ignored
      if (jsargs.length < argc) {
        throw new RuntimeException("Not enough arguments to " + method);
      }
      Object jthis = null;
      if (method.needsThis()) {
        jthis = JsValueGlue.get(jsthis, classLoader, method.getDeclaringClass(), "invoke this");
      }
      for (int i = 0; i < argc; ++i) {
        args[i] = JsValueGlue.get(jsargs[i], classLoader, paramTypes[i], "invoke args");
      }
      try {
        Object result;
        try {
          result = method.invoke(jthis, args);
        } catch (IllegalAccessException e) {
          // should never, ever happen
          e.printStackTrace();
          throw new RuntimeException(e);
        }
        JsValueGlue.set(returnValue, classLoader, dispatchIdOracle, method.getReturnType(), result);
        H jsResult = returnValue.getJsValue();
        // Native code will eat an extra ref.
        LowLevelWebKit.gcProtect(jsContext, jsResult);
        return jsResult;
      } catch (InstantiationException e) {
        // If we get here, it means an exception is being thrown from
        // Java back into JavaScript
        ModuleSpace.setThrownJavaException(e.getCause());
        // Native code eats the same ref it gave us.
        exception.add(LowLevelWebKit.getJsNull(jsContext));
        // Native code eats the same ref it gave us.
        return LowLevelWebKit.getJsUndefined(jsContext);
      } catch (InvocationTargetException e) {
        // If we get here, it means an exception is being thrown from
        // Java back into JavaScript
        Throwable t = e.getTargetException();
        ModuleSpace.setThrownJavaException(t);
        // Native code eats the same ref it gave us.
        exception.add(LowLevelWebKit.getJsNull(jsContext));
        // Native code eats the same ref it gave us.
        return LowLevelWebKit.getJsUndefined(jsContext);
      }
    } finally {
      LowLevelWebKit.popJsContext(jsContext);
    }
  }
}
