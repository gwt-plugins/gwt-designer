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

import java.lang.ref.WeakReference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import com.google.gdt.eclipse.designer.moz.jsni.LowLevelMoz64.DispatchMethod64;
import com.google.gdt.eclipse.designer.moz.jsni.LowLevelMoz64.DispatchObject64;
import com.google.gwt.dev.shell.designtime.DispatchIdOracle;
import com.google.gwt.dev.shell.designtime.JavaDispatch;
import com.google.gwt.dev.shell.designtime.JavaDispatchImpl;
import com.google.gwt.dev.shell.designtime.JsValue;
import com.google.gwt.dev.shell.designtime.JsValueGlue;
import com.google.gwt.dev.shell.designtime.MethodAdaptor;
import com.google.gwt.dev.shell.designtime.WrappersCache;

/**
 * Wraps an arbitrary Java Object as a Dispatch component. The class was
 * motivated by the need to expose Java objects into JavaScript.
 * 
 * An instance of this class with no target is used to globally access all
 * static methods or fields.
 */
public class GeckoDispatchAdapter64 implements DispatchObject64 {

  final WeakReference<DispatchIdOracle> dispIdOracleRef;
  final WeakReference<ClassLoader> classLoaderRef;
  JavaDispatch javaDispatch;
  /**
   * This constructor initializes as the static dispatcher, which handles only
   * static method calls and field references.
   * 
   * @param cl this class's classLoader
   */
  GeckoDispatchAdapter64(ClassLoader cl, DispatchIdOracle ora) {
    this.javaDispatch = new JavaDispatchImpl(ora);
    this.classLoaderRef = new WeakReference<ClassLoader>(cl);
    this.dispIdOracleRef = new WeakReference<DispatchIdOracle>(ora);
  }

  /**
   * This constructor initializes a dispatcher, around a particular instance.
   * 
   * @param cl this class's classLoader
   * @param target the object being wrapped as an IDispatch
   */
  GeckoDispatchAdapter64(ClassLoader cl, DispatchIdOracle ora, Object target) {
    this.javaDispatch = new JavaDispatchImpl(ora, target);
    this.classLoaderRef = new WeakReference<ClassLoader>(cl);
    this.dispIdOracleRef = new WeakReference<DispatchIdOracle>(ora);
  }

  /**
   * Retrieve a field and store in the passed JsValue. This function is called
   * exclusively from native code.
   * 
   * @param name name of the field to retrieve
   * @param jsValue a reference to the JsValue object to receive the value of
   *          the field
   */
  public void getField(String member, long jsRootedValue) {
    JsValueMoz64 jsValue = new JsValueMoz64(jsRootedValue);
    ClassLoader classLoader = classLoaderRef.get();
    if (classLoader == null) {
        jsValue.setUndefined();
        return;
    }
    DispatchIdOracle dispIdOracle = dispIdOracleRef.get();
    if (dispIdOracle == null) {
        jsValue.setUndefined();
        return;
    }
    int dispId = getDispId(member);
    if (dispId < 0) {
      // no field by that name, return undefined
      jsValue.setUndefined();
      return;
    }
    if (javaDispatch.isField(dispId)) {
      Field field = javaDispatch.getField(dispId);
      JsValueGlue.set(jsValue, classLoader, dispIdOracle, field.getType(),
          javaDispatch.getFieldValue(dispId));
      return;
    } else {
      MethodAdaptor method = javaDispatch.getMethod(dispId);
      AccessibleObject obj = method.getUnderlyingObject();
      DispatchMethod64 dispMethod = (DispatchMethod64) WrappersCache.getWrapperForObject(classLoader, obj);
      if (dispMethod == null) {
        dispMethod = new MethodDispatch64(classLoader, dispIdOracle, method);
        WrappersCache.putWrapperForObject(classLoader, obj, dispMethod);
      }
      jsValue.setWrappedFunction(method.toString(), dispMethod);
    }
  }

  public Object getTarget() {
    return javaDispatch.getTarget();
  }

  public void setField(String member, long jsRootedValue) {
    ClassLoader classLoader = classLoaderRef.get();
    if (classLoader == null) {
      throw new RuntimeException("Invalid class loader.");
    }
    JsValue jsValue = new JsValueMoz64(jsRootedValue);
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
      DispatchIdOracle dispIdOracle = dispIdOracleRef.get();
      if (dispIdOracle == null) {
        throw new RuntimeException("Invalid dispatch oracle.");
      }
      return dispIdOracle.getDispId(member);
    }
  }
}
