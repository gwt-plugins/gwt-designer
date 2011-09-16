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
package com.google.gdt.eclipse.designer.smart.model.support;

import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.SectionStackSectionInfo;
import com.google.gdt.eclipse.designer.smart.model.TabInfo;
import com.google.gdt.eclipse.designer.smart.model.form.CanvasItemInfo;
import com.google.gdt.eclipse.designer.smart.model.form.FormItemInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Utilities for manipulation of SmartGWT <code>Canvas</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.support
 */
public final class SmartClientUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private SmartClientUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns absolute bounds for given <code>Canvas</code>. Meaning of "absolute" is following:
   * relative to point (0,0) of screen shot that we show on design canvas.
   */
  public static Rectangle getAbsoluteBounds(Object canvas) {
    int left = (Integer) ReflectionUtils.invokeMethodEx(canvas, "getAbsoluteLeft()");
    int top = (Integer) ReflectionUtils.invokeMethodEx(canvas, "getAbsoluteTop()");
    int width = (Integer) ReflectionUtils.invokeMethodEx(canvas, "getVisibleWidth()");
    int height = (Integer) ReflectionUtils.invokeMethodEx(canvas, "getVisibleHeight()");
    return new Rectangle(left, top, width, height);
  }

  /**
   * @return <code>true</code> if {@link JavaInfo} represents SmartGWT widget.
   */
  public static boolean isWidgetSC(JavaInfo widget) {
    return widget instanceof CanvasInfo
        || widget instanceof SectionStackSectionInfo
        || widget instanceof CanvasItemInfo
        || widget instanceof FormItemInfo
        || widget instanceof TabInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life-cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  public static boolean isCanvasCreated(Object canvas) {
    if (canvas != null) {
      return (Boolean) ReflectionUtils.invokeMethodEx(canvas, "isCreated()");
    } else {
      return false;
    }
  }

  public static void destroyCanvas(Object canvas) {
    if (isCanvasCreated(canvas)) {
      ReflectionUtils.invokeMethodEx(canvas, "destroy()");
    }
  }

  public static boolean isDataSourceCreated(Object dataSource) {
    if (dataSource != null) {
      return (Boolean) ReflectionUtils.invokeMethodEx(dataSource, "isCreated()");
    } else {
      return false;
    }
  }

  public static void destroyDataSource(Object dataSource) {
    if (isDataSourceCreated(dataSource)) {
      ReflectionUtils.invokeMethodEx(dataSource, "destroy()");
    }
  }

  public static boolean isJsObjectCreated(Object jsObject) {
    if (jsObject != null) {
      return (Boolean) ReflectionUtils.invokeMethodEx(jsObject, "isCreated()");
    } else {
      return false;
    }
  }
}
