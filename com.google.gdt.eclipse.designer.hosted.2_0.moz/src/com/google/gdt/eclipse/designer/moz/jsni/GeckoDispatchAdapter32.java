/*
 * Copyright 2008 Google Inc.
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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import com.google.gdt.eclipse.designer.moz.jsni.LowLevelMoz32.DispatchMethod32;
import com.google.gdt.eclipse.designer.moz.jsni.LowLevelMoz32.DispatchObject32;
import com.google.gwt.dev.shell.CompilingClassLoader;
import com.google.gwt.dev.shell.JavaDispatch;
import com.google.gwt.dev.shell.JavaDispatchImpl;
import com.google.gwt.dev.shell.JsValue;
import com.google.gwt.dev.shell.JsValueGlue;
import com.google.gwt.dev.shell.MethodAdaptor;

/**
 * Wraps an arbitrary Java Object as a Dispatch component. The class was
 * motivated by the need to expose Java objects into JavaScript.
 * 
 * An instance of this class with no target is used to globally access all
 * static methods or fields.
 */
public class GeckoDispatchAdapter32 implements DispatchObject32/*64*/ {

  private final CompilingClassLoader classLoader;

  private final JavaDispatch javaDispatch;

  /**
   * This constructor initializes as the static dispatcher, which handles only
   * static method calls and field references.
   * 
   * @param cl this class's classLoader
   */
  GeckoDispatchAdapter32(CompilingClassLoader cl) {
    javaDispatch = new JavaDispatchImpl(cl);
    classLoader = cl;
  }

  /**
   * This constructor initializes a dispatcher, around a particular instance.
   * 
   * @param cl this class's classLoader
   * @param target the object being wrapped as an IDispatch
   */
  GeckoDispatchAdapter32(CompilingClassLoader cl, Object target) {
    javaDispatch = new JavaDispatchImpl(cl, target);
    classLoader = cl;
  }

  /**
   * Retrieve a field and store in the passed JsValue. This function is called
   * exclusively from native code.
   * 
   * @param name name of the field to retrieve
   * @param jsValue a reference to the JsValue object to receive the value of
   *          the field
   */
  public void getField(String member, int /*long*/jsRootedValue) {
    JsValueMoz32/*64*/ jsValue = new JsValueMoz32/*64*/(jsRootedValue);
    int dispId = getDispId(member);
    if (dispId < 0) {
      // no field by that name, return undefined
      jsValue.setUndefined();
      return;
    }
    if (javaDispatch.isField(dispId)) {
      Field field = javaDispatch.getField(dispId);
      JsValueGlue.set(jsValue, classLoader, field.getType(),
          javaDispatch.getFieldValue(dispId));
      return;
    } else {
      MethodAdaptor method = javaDispatch.getMethod(dispId);
      AccessibleObject obj = method.getUnderlyingObject();
      DispatchMethod32/*64*/ dispMethod = (DispatchMethod32/*64*/) classLoader.getWrapperForObject(obj);
      if (dispMethod == null) {
        dispMethod = new MethodDispatch32/*64*/(classLoader, method);
        classLoader.putWrapperForObject(obj, dispMethod);
      }
      jsValue.setWrappedFunction(method.toString(), dispMethod);
    }
  }

  public Object getTarget() {
    return javaDispatch.getTarget();
  }

  public void setField(String member, int /*long*/jsRootedValue) {
    JsValue jsValue = new JsValueMoz32/*64*/(jsRootedValue);
    int dispId = getDispId(member);
    if (dispId < 0) {
      // no field by that name
      // TODO: expandos?
      throw new RuntimeException("No such field " + member);
    }
    if (javaDispatch.isMethod(dispId)) {
      throw new RuntimeException("Cannot reassign method " + member);
    }
    Field field = javaDispatch.getField(dispId);
    Object val = JsValueGlue.get(jsValue, classLoader, field.getType(),
        "setField");
    javaDispatch.setFieldValue(dispId, val);
  }
  private int getDispId(String member) {
    if (Character.isDigit(member.charAt(0))) {
      return Integer.valueOf(member);
    } else {
      return classLoader.getDispId(member);
	  }
  }
}
