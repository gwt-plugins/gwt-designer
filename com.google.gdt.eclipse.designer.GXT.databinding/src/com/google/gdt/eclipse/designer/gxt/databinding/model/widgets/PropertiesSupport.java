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
package com.google.gdt.eclipse.designer.gxt.databinding.model.widgets;

import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.gxt.databinding.ui.providers.TypeImageProvider;

import org.eclipse.wb.internal.core.databinding.model.presentation.SimpleObservePresentation;

import java.util.Collections;
import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public final class PropertiesSupport {
  private final Class<?> m_SimpleComboBoxClass;
  private final Class<?> m_TimeFieldClass;
  private final Class<?> m_FieldClass;
  private final Class<?> m_FormPanelClass;
  private final Class<?> m_GridClass;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertiesSupport(ClassLoader classLoader) throws Exception {
    m_SimpleComboBoxClass =
        classLoader.loadClass("com.extjs.gxt.ui.client.widget.form.SimpleComboBox");
    m_TimeFieldClass = classLoader.loadClass("com.extjs.gxt.ui.client.widget.form.TimeField");
    m_FieldClass = classLoader.loadClass("com.extjs.gxt.ui.client.widget.form.Field");
    m_FormPanelClass = classLoader.loadClass("com.extjs.gxt.ui.client.widget.form.FormPanel");
    m_GridClass = classLoader.loadClass("com.extjs.gxt.ui.client.widget.grid.Grid");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Class<?> getFieldClass() {
    return m_FieldClass;
  }

  public Class<?> getSimpleComboBoxClass() {
    return m_SimpleComboBoxClass;
  }

  public Class<?> getTimeFieldClass() {
    return m_TimeFieldClass;
  }

  public Class<?> getFormPanelClass() {
    return m_FormPanelClass;
  }

  public Class<?> getGridClass() {
    return m_GridClass;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<WidgetPropertyObserveInfo> getProperties(Class<?> widgetType) throws Exception {
    if (m_SimpleComboBoxClass.isAssignableFrom(widgetType)
        || m_TimeFieldClass.isAssignableFrom(widgetType)
        || m_FieldClass.isAssignableFrom(widgetType)
        || m_FormPanelClass.isAssignableFrom(widgetType)
        || widgetType == getClass()) {
      SimpleObservePresentation presentation =
          new SimpleObservePresentation("<Self Object>",
              "",
              TypeImageProvider.SELF_OBJECT_PROPERTY_IMAGE);
      return ImmutableList.of(new WidgetPropertyObserveInfo(presentation));
    }
    if (m_GridClass.isAssignableFrom(widgetType)) {
      SimpleObservePresentation presentation =
          new SimpleObservePresentation("selection", "selection", TypeImageProvider.OBJECT_IMAGE);
      return ImmutableList.of(new WidgetPropertyObserveInfo(presentation));
    }
    //
    return Collections.emptyList();
  }
}