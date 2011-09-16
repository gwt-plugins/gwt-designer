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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import com.google.gdt.eclipse.designer.webkit.jsni.LowLevelWebKit.DispatchMethod;
import com.google.gdt.eclipse.designer.webkit.jsni.LowLevelWebKit.DispatchObject;
import com.google.gwt.dev.shell.CompilingClassLoader;
import com.google.gwt.dev.shell.JavaDispatch;
import com.google.gwt.dev.shell.JavaDispatchImpl;
import com.google.gwt.dev.shell.JsValue;
import com.google.gwt.dev.shell.JsValueGlue;
import com.google.gwt.dev.shell.MethodAdaptor;

/**
 * Wraps an arbitrary Java Object as a Dispatch component. The class was motivated by the need to
 * expose Java objects into JavaScript.
 * 
 * An instance of this class with no target is used to globally access all static methods or fields.
 */
class WebKitDispatchAdapter<H extends Number> implements DispatchObject<H> {
  WeakReference<CompilingClassLoader> classLoaderRef;
  JavaDispatch javaDispatch;

  /**
   * This constructor initializes as the static dispatcher, which handles only static method calls
   * and field references.
   * 
   * @param cl
   *          this class's classLoader
   */
  WebKitDispatchAdapter(CompilingClassLoader cl) {
    javaDispatch = new JavaDispatchImpl(cl);
    this.classLoaderRef = new WeakReference<CompilingClassLoader>(cl);
  }

  /**
   * This constructor initializes a dispatcher, around a particular instance.
   * 
   * @param cl
   *          this class's classLoader
   * @param target
   *          the object being wrapped as an IDispatch
   */
  WebKitDispatchAdapter(CompilingClassLoader cl, Object target) {
    javaDispatch = new JavaDispatchImpl(cl, target);
    this.classLoaderRef = new WeakReference<CompilingClassLoader>(cl);
  }

  public H getField(H jsContext, String name) {
    LowLevelWebKit.pushJsContext(jsContext);
    CompilingClassLoader classLoader = classLoaderRef.get();
    if (classLoader == null) {
      throw new RuntimeException("Invalid class loader.");
    }
    try {
      int dispId = getDispId(name);
      if (dispId < 0) {
        return LowLevelWebKit.getJsUndefined(jsContext);
      }
      if (javaDispatch.isField(dispId)) {
        Field field = javaDispatch.getField(dispId);
        JsValueWebKit<H> jsValue = new JsValueWebKit<H>();
        JsValueGlue.set(jsValue, classLoader, field.getType(), javaDispatch.getFieldValue(dispId));
        H jsval = jsValue.getJsValue();
        // Native code will eat an extra ref.
        LowLevelWebKit.gcProtect(jsContext, jsval);
        return jsval;
      } else {
        MethodAdaptor method = javaDispatch.getMethod(dispId);
        AccessibleObject obj = method.getUnderlyingObject();
        DispatchMethod<?> dispMethod = (DispatchMethod<?>) classLoader.getWrapperForObject(obj);
        if (dispMethod == null) {
          dispMethod = new MethodDispatch<H>(classLoader, method);
          classLoader.putWrapperForObject(obj, dispMethod);
        }
        // Native code eats the same ref it gave us.
        return LowLevelWebKit.wrapDispatchMethod(jsContext, method.toString(), dispMethod);
      }
    } finally {
      LowLevelWebKit.popJsContext(jsContext);
    }
  }

  public Object getTarget() {
    return javaDispatch.getTarget();
  }

  public void setField(H jsContext, String name, H value) {
    CompilingClassLoader classLoader = classLoaderRef.get();
    if (classLoader == null) {
      throw new RuntimeException("Invalid class loader.");
    }
    LowLevelWebKit.pushJsContext(jsContext);
    try {
      JsValue jsValue = new JsValueWebKit<H>(value);
      int dispId = getDispId(name);
      if (dispId < 0) {
        // TODO (knorton): We could allow expandos, but should we?
        throw new RuntimeException("No such field " + name);
      }
      if (javaDispatch.isMethod(dispId)) {
        throw new RuntimeException("Cannot reassign method " + name);
      }
      Field field = javaDispatch.getField(dispId);
      Object val = JsValueGlue.get(jsValue, classLoader, field.getType(), "setField");
      javaDispatch.setFieldValue(dispId, val);
    } finally {
      LowLevelWebKit.popJsContext(jsContext);
    }
  }

  private int getDispId(String member) {
    try {
      return Integer.valueOf(member);
    } catch (Throwable e) {
      CompilingClassLoader classLoader = classLoaderRef.get();
      if (classLoader == null) {
        throw new RuntimeException("Invalid class loader.");
      }
      return classLoader.getDispId(member);
    }
  }

  @Override
  public String toString() {
    return getTarget().toString();
  }
}
