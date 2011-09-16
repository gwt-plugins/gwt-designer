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

import com.google.gdt.eclipse.designer.gwtext.model.layout.assistant.FormLayoutAssistant;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.FieldInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;

/**
 * Model for <code>com.gwtext.client.widgets.layout.FormLayout</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model.layout
 */
public final class FormLayoutInfo extends AnchorLayoutInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initializeLayoutAssistant() {
    new FormLayoutAssistant(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Components/constraints
  //
  ////////////////////////////////////////////////////////////////////////////
  private Class<?> m_formLayoutDataClass;

  @Override
  protected Object getDefaultVirtualDataObject(WidgetInfo widget) throws Exception {
    if (widget instanceof FieldInfo) {
      return getFormLayoutDataClass().newInstance();
    }
    return super.getDefaultVirtualDataObject(widget);
  }

  @Override
  protected Class<?> getLayoutDataClass(WidgetInfo widget) throws Exception {
    if (widget instanceof FieldInfo) {
      return getFormLayoutDataClass();
    }
    return super.getLayoutDataClass(widget);
  }

  private Class<?> getFormLayoutDataClass() throws Exception {
    if (m_formLayoutDataClass == null) {
      // extract class name
      String layoutDataClassName = "com.gwtext.client.widgets.layout.FormLayoutData";
      Assert.isNotNull(layoutDataClassName);
      Assert.isTrue(layoutDataClassName.length() != 0);
      // load class
      m_formLayoutDataClass = JavaInfoUtils.getClassLoader(this).loadClass(layoutDataClassName);
    }
    return m_formLayoutDataClass;
  }

  /**
   * @return {@link FormLayoutDataInfo} association with given {@link WidgetInfo}.
   */
  public static FormLayoutDataInfo getFormData(WidgetInfo widget) {
    return (FormLayoutDataInfo) getLayoutData(widget);
  }
}