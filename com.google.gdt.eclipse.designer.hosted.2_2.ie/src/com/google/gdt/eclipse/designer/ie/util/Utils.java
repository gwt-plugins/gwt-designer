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
package com.google.gdt.eclipse.designer.ie.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.ole.win32.IDispatch;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Utility classes for Windows support of GWT 2.0.
 * 
 * @author mitin_aa
 */
public class Utils {
  static {
    System.loadLibrary("swt-gwt-utils");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private Utils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Private fields access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Reflection utility: sets the field value of given object using target class.
   */
  public static void setFieldObjectValue(Class<?> targetClass,
      Object target,
      String fieldName,
      Object value) {
    try {
      Field field = targetClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (Throwable e) {
      throw new RuntimeException("Unable to get field '"
        + fieldName
        + "' from class "
        + targetClass.getName(), e);
    }
  }

  /**
   * Reflection utility: sets the field value of given object.
   */
  public static void setFieldObjectValue(Object target, String fieldName, Object value) {
    if (target != null) {
      setFieldObjectValue(target.getClass(), target, fieldName, value);
    } else {
      throw new NullPointerException("target must not be null");
    }
  }

  /**
   * Reflection utility: gets the field value of given object using source class.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getFieldObjectValue(Class<?> sourceClass, Object source, String fieldName) {
    try {
      Field field = sourceClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      return (T) field.get(source);
    } catch (Throwable e) {
      throw new RuntimeException("Unable to get field '"
        + fieldName
        + "' from class "
        + sourceClass.getName(), e);
    }
  }

  /**
   * Reflection utility: gets the field value of given object.
   */
  public static Object getFieldObjectValue(Object source, String fieldName) {
    if (source != null) {
      return getFieldObjectValue(source.getClass(), source, fieldName);
    } else {
      throw new NullPointerException("target must not be null");
    }
  }

  /**
   * Creates the {@link OleAutomation} instance with {@link IDispatch} parameter using reflection.
   */
  public static OleAutomation newOleAutomation(IDispatch disp) {
    try {
      Constructor<OleAutomation> constructor =
          OleAutomation.class.getDeclaredConstructor(new Class[]{IDispatch.class});
      constructor.setAccessible(true);
      return constructor.newInstance(new Object[]{disp});
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Method copied from 3.3+ Variant class.
   */
  public static Variant win32_new(int varArgAddr) {
    try {
      Variant variant = new Variant();
      Method setDataMethod =
          variant.getClass().getDeclaredMethod("setData", new Class[]{int.class});
      setDataMethod.setAccessible(true);
      setDataMethod.invoke(variant, new Object[]{new Integer(varArgAddr)});
      return variant;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Method copied from 3.3+ Variant class.
   */
  public static void win32_copy(int pVarDest, Variant varSrc) {
    try {
      Method getDataMethod = varSrc.getClass().getDeclaredMethod("getData", new Class[]{int.class});
      getDataMethod.setAccessible(true);
      getDataMethod.invoke(varSrc, new Object[]{new Integer(pVarDest)});
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Screen shot for browser
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Uses OleDraw (instead of IViewObject::Draw) and pass OleDraw the IHTMLDocument2 interface.
   * <p>
   * See <a href="http://www.nathanm.com/ihtmlelementrender-opacity">About OleDraw</a>.
   */
  public static Image makeShot(Control browser) throws Exception {
    Rectangle bounds = browser.getBounds();
    Image image = new Image(Display.getCurrent(), bounds.width, bounds.height);
    GC gc = new GC(image);
    try {
      Object webBrowser = ReflectionUtils.getFieldObject(browser, "webBrowser");
      Object oleClientSite = ReflectionUtils.getFieldObject(webBrowser, "site");
      // force size, else some default 200x150 or so will be used
      ReflectionUtils.invokeMethod(oleClientSite, "setExtent(int,int)", bounds.width, bounds.height);
      // force STATE_RUNNING, during Popup displaying it can be in STATE_UIACTIVE
      ReflectionUtils.setField(oleClientSite, "state", 1);
      // ask for painting
      {
        Event event = new Event();
        event.gc = gc;
        ReflectionUtils.invokeMethod(oleClientSite, "onPaint(org.eclipse.swt.widgets.Event)", event);
      }
    } finally {
      gc.dispose();
    }
    return image;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Global references
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Deletes the native global ref.
   */
  private static native void _deleteGlobalRefInt(int i);

  /**
   * Creates the native global ref.
   */
  private static native int _newGlobalRefInt(Object obj);

  /**
   * Converts native pointer pointing to {@link Object} instance to {@link Object} reference.
   */
  private static native Object _objFromGlobalRefInt(int i);

  /**
   * Forcibly enables the option 'Do not use proxy for local addresses' in IE proxy settings.
   */
  private static native void _ensureProxyBypassLocal();

  /**
   * Forcibly enables the option 'Do not use proxy for local addresses' in IE proxy settings.
   */
  public static void ensureProxyBypassLocal() {
    _ensureProxyBypassLocal();
  }

  /**
   * Deletes the native global ref.
   */
  public static void deleteGlobalRefInt(int globalRef) {
    _deleteGlobalRefInt(globalRef);
  }

  /**
   * Creates the native global ref.
   */
  public static int newGlobalRefInt(Object o) {
    return _newGlobalRefInt(o);
  }

  /**
   * Converts native pointer pointing to {@link Object} instance to {@link Object} reference.
   */
  public static Object objFromGlobalRefInt(int globalRef) {
    return _objFromGlobalRefInt(globalRef);
  }
}
