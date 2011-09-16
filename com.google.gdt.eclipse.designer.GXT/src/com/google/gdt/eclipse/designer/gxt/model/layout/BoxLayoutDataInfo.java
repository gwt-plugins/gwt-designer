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
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Model for <code>HBoxLayoutData</code> and <code>VBoxLayoutData</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public abstract class BoxLayoutDataInfo extends MarginDataInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BoxLayoutDataInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the value of "flex" property.
   */
  public final int getFlex() {
    return ((Double) ReflectionUtils.invokeMethodEx(getObject(), "getFlex()")).intValue();
  }

  /**
   * Sets the value of "flex" property.
   */
  public final void setFlex(int flex) throws Exception {
    getPropertyByTitle("flex").setValue((double) flex);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Margins
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void setMarginAll(Object value) throws Exception {
    if (value == Property.UNKNOWN_VALUE) {
      value = 0;
    }
    setMarginAll0(value);
  }

  /**
   * No constructor with single margin argument, so set four margins.
   */
  private void setMarginAll0(final Object value) throws Exception {
    ExecutionUtils.run(this, new RunnableEx() {
      public void run() throws Exception {
        setMarginTop(value);
        setMarginRight(value);
        setMarginBottom(value);
        setMarginLeft(value);
      }
    });
  }
}
