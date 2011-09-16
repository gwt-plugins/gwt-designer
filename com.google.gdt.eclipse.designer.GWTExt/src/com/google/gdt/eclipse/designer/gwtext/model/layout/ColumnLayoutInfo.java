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
package com.google.gdt.eclipse.designer.gwtext.model.layout;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Model for <code>com.gwtext.client.widgets.layout.ColumnLayout</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model.layout
 */
public final class ColumnLayoutInfo extends LayoutInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Components/constraints
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Object getDefaultVirtualDataObject(WidgetInfo widget) throws Exception {
    ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
    Class<?> dataClass = classLoader.loadClass("com.gwtext.client.widgets.layout.ColumnLayoutData");
    return ReflectionUtils.getConstructor(dataClass, double.class).newInstance(0.0d);
  }

  /**
   * @return {@link ColumnLayoutDataInfo} association with given {@link WidgetInfo}.
   */
  public static ColumnLayoutDataInfo getColumnData(WidgetInfo widget) {
    return (ColumnLayoutDataInfo) getLayoutData(widget);
  }
}