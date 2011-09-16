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
package com.google.gdt.eclipse.designer.ie.jsni;

import java.lang.ref.WeakReference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.internal.ole.win32.COM;
import org.eclipse.swt.internal.ole.win32.IDispatch;
import org.eclipse.swt.ole.win32.Variant;

import com.google.gwt.dev.shell.designtime.DispatchIdOracle;
import com.google.gwt.dev.shell.designtime.JsValueGlue;
import com.google.gwt.dev.shell.designtime.MethodAdaptor;
import com.google.gwt.dev.shell.designtime.WrappersCache;

/**
 * Wraps an arbitrary Java Method as an Automation-compatible server. The class was motivated by the
 * need to expose Java objects into JavaScript.
 * 
 * <p>
 * <b>Features</b>
 * </p>
 * <ul>
 * <li>Implements the <code>IDispatch</code> interface for you</li>
 * <li>If the COM client keeps a reference to this object, this object is prevented from being
 * garbage collected</li>
 * </ul>
 * 
 * <p>
 * <b>Limitations</b>
 * </p>
 * <ul>
 * <li>Only late-bound dispatch is supported</li>
 * <li>Named arguments are not supported (see {@link #GetIDsOfNames})).</li>
 * </ul>
 */
public class MethodDispatch extends IDispatchImpl {
  private final WeakReference<ClassLoader> classLoaderRef;
  private final WeakReference<DispatchIdOracle> dispIdOracleRef;
  private final MethodAdaptor method;

  public MethodDispatch(ClassLoader cl, DispatchIdOracle ora, MethodAdaptor method) {
    this.classLoaderRef = new WeakReference<ClassLoader>(cl);
    this.dispIdOracleRef = new WeakReference<DispatchIdOracle>(ora);
    this.method = method;
  }

  @Override
  public String toString() {
    return "\nfunction  " + method.toString() + "(){\n    [native code]\n}\n";
  }

  /**
   * ID 0 is magic. It can either mean toString or invoke, depending on the flags. So we start with
   * ID 1 for toString. {@link IDispatchProxy} and {@link BrowserWidgetIE6.External} should be fixed
   * to do the same.
   */
  @Override
  protected void getIDsOfNames(String[] names, int[] ids) throws HResultException {
    if (names[0].equalsIgnoreCase("toString")) {
      ids[0] = 1;
    } else if (names[0].equalsIgnoreCase("call")) {
      ids[0] = 2;
    } else if (names[0].equalsIgnoreCase("apply")) {
      ids[0] = 3;
    } else {
      throw new HResultException(IDispatchImpl.DISP_E_UNKNOWNNAME);
    }
  }

  /*
   * Handles all the things the browser can do to a function object.
   */
  @Override
  protected Variant invoke(int id, int flags, Variant[] params) throws HResultException,
      InstantiationException, InvocationTargetException {
    ClassLoader classLoader = classLoaderRef.get();
    if (classLoader == null) {
      throw new RuntimeException("Invalid class loader.");
    }
    DispatchIdOracle dispIdOracle = dispIdOracleRef.get();
    if (dispIdOracle == null) {
      throw new RuntimeException("Invalid dispatch oracle.");
    }
    switch (id) {
      case 0 :
        // An implicit access.
        if ((flags & COM.DISPATCH_METHOD) != 0) {
          // implicit call -- "m()"
          return callMethod(classLoader, dispIdOracle, null, params, method);
        } else if ((flags & COM.DISPATCH_PROPERTYGET) != 0) {
          // implicit toString -- "'foo' + m"
          return new Variant(toString());
        }
        break;
      case 1 :
        // toString
        if ((flags & COM.DISPATCH_METHOD) != 0) {
          // "m.toString()"
          return new Variant(toString());
        } else if ((flags & COM.DISPATCH_PROPERTYGET) != 0) {
          // "m.toString"
          MethodAdaptor toStringMethod;
          try {
            toStringMethod = new MethodAdaptor(Object.class.getDeclaredMethod("toString"));
          } catch (Throwable e) {
            throw new RuntimeException("Failed to get Object.toString() method", e);
          }
          AccessibleObject obj = toStringMethod.getUnderlyingObject();
          IDispatchImpl dispMethod =
              (IDispatchImpl) WrappersCache.getWrapperForObject(classLoader, obj);
          if (dispMethod == null || dispMethod.refCount < 1) {
            dispMethod = new MethodDispatch(classLoader, dispIdOracle, toStringMethod);
            WrappersCache.putWrapperForObject(classLoader, obj, dispMethod);
          }
          IDispatch disp = new IDispatch(dispMethod.getAddress());
          disp.AddRef();
          return new Variant(disp);
        }
        break;
      case 2 :
        // call
        if ((flags & COM.DISPATCH_METHOD) != 0) {
          // "m.call(thisObj, arg)"
          /*
           * First param must be a this object of the correct type (for instance
           * methods). If method is static, it can be null.
           */
          Object jthis =
              JsValueGlue.get(
                new JsValueIE6(params[0]),
                classLoader,
                method.getDeclaringClass(),
                "this");
          Variant[] otherParams = new Variant[params.length - 1];
          System.arraycopy(params, 1, otherParams, 0, otherParams.length);
          return callMethod(classLoader, dispIdOracle, jthis, otherParams, method);
        } else if ((flags & COM.DISPATCH_PROPERTYGET) != 0) {
          // "m.call"
          // TODO: not supported
        }
        break;
      case 3 :
        // apply
        // TODO: not supported
        break;
      case IDispatchProxy.DISPID_MAGIC_GETGLOBALREF :
        // We are NOT in fact a "wrapped Java Object", but we don't want to
        // throw an exception for being asked.
        return new Variant(0);
      default :
        // The specified member id is out of range.
        throw new HResultException(COM.DISP_E_MEMBERNOTFOUND);
    }
    throw new HResultException(COM.E_NOTSUPPORTED);
  }
}
