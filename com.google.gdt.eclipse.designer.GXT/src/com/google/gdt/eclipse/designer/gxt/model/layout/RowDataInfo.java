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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Model for <code>RowData</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public final class RowDataInfo extends LayoutDataInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowDataInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public double getWidth() {
    return (Double) ReflectionUtils.invokeMethodEx(getObject(), "getWidth()");
  }

  public double getHeight() {
    return (Double) ReflectionUtils.invokeMethodEx(getObject(), "getHeight()");
  }

  public void setWidth(double width) throws Exception {
    setSizePropertyValue("width", width);
  }

  public void setHeight(double height) throws Exception {
    setSizePropertyValue("height", height);
  }

  private void setSizePropertyValue(String name, double value) throws Exception {
    Property property = getPropertyByTitle(name);
    if (value == -1) {
      property.setValue(Property.UNKNOWN_VALUE);
    } else {
      property.setValue(value);
    }
  }
}
