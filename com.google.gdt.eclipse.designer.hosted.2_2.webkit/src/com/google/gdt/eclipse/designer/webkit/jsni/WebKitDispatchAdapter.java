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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import com.google.gdt.eclipse.designer.webkit.jsni.LowLevelWebKit.DispatchMethod;
import com.google.gdt.eclipse.designer.webkit.jsni.LowLevelWebKit.DispatchObject;
import com.google.gwt.dev.shell.designtime.DispatchIdOracle;
import com.google.gwt.dev.shell.designtime.JavaDispatch;
import com.google.gwt.dev.shell.designtime.JavaDispatchImpl;
import com.google.gwt.dev.shell.designtime.JsValue;
import com.google.gwt.dev.shell.designtime.JsValueGlue;
import com.google.gwt.dev.shell.designtime.MethodAdaptor;
import com.google.gwt.dev.shell.designtime.WrappersCache;

/**
 * Wraps an arbitrary Java Object as a Dispatch component. The class was motivated by the need to
 * expose Java objects into JavaScript.
 * 
 * An instance of this class with no target is used to globally access all static methods or fields.
 */
class WebKitDispatchAdapter<H extends Number> implements DispatchObject<H> {
  final WeakReference<DispatchIdOracle> dispIdOracleRef;
  final WeakReference<ClassLoader> classLoaderRef;
  JavaDispatch javaDispatch;

  /**
   * This constructor initializes as the static dispatcher, which handles only static method calls
   * and field references.
   * 
   * @param cl
   *          this class's classLoader
   */
  WebKitDispatchAdapter(ClassLoader cl, DispatchIdOracle ora) {
    this.javaDispatch = new JavaDispatchImpl(ora);
    this.classLoaderRef = new WeakReference<ClassLoader>(cl);
    this.dispIdOracleRef = new WeakReference<DispatchIdOracle>(ora);
  }

  /**
   * This constructor initializes a dispatcher, around a particular instance.
   * 
   * @param cl
   *          this class's classLoader
   * @param target
   *          the object being wrapped as an IDispatch
   */
  WebKitDispatchAdapter(ClassLoader cl, DispatchIdOracle ora, Object target) {
    this.javaDispatch = new JavaDispatchImpl(ora, target);
    this.classLoaderRef = new WeakReference<ClassLoader>(cl);
    this.dispIdOracleRef = new WeakReference<DispatchIdOracle>(ora);
  }

  public H getField(H jsContext, String name) {
    LowLevelWebKit.pushJsContext(jsContext);
    ClassLoader classLoader = classLoaderRef.get();
    if (classLoader == null) {
      return LowLevelWebKit.getJsUndefined(jsContext);
    }
    DispatchIdOracle dispIdOracle = dispIdOracleRef.get();
    if (dispIdOracle == null) {
      return LowLevelWebKit.getJsUndefined(jsContext);
    }
    try {
      int dispId = getDispId(name);
      if (dispId < 0) {
        return LowLevelWebKit.getJsUndefined(jsContext);
      }
      if (javaDispatch.isField(dispId)) {
        Field field = javaDispatch.getField(dispId);
        JsValueWebKit<H> jsValue = new JsValueWebKit<H>();
        JsValueGlue.set(
          jsValue,
          classLoader,
          dispIdOracle,
          field.getType(),
          javaDispatch.getFieldValue(dispId));
        H jsval = jsValue.getJsValue();
        // Native code will eat an extra ref.
        LowLevelWebKit.gcProtect(jsContext, jsval);
        return jsval;
      } else {
        MethodAdaptor method = javaDispatch.getMethod(dispId);
        AccessibleObject obj = method.getUnderlyingObject();
        DispatchMethod<?> dispMethod =
            (DispatchMethod<?>) WrappersCache.getWrapperForObject(classLoader, obj);
        if (dispMethod == null) {
          dispMethod = new MethodDispatch<H>(classLoader, dispIdOracle, method);
          WrappersCache.putWrapperForObject(classLoader, obj, dispMethod);
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
    ClassLoader classLoader = classLoaderRef.get();
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

  @Override
  public String toString() {
    return getTarget().toString();
  }
}
