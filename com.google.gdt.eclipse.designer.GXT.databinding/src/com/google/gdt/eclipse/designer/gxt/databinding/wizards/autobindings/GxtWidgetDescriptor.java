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
package com.google.gdt.eclipse.designer.gxt.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.AbstractDescriptor;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author lobas_av
 * 
 */
public final class GxtWidgetDescriptor extends AbstractDescriptor {
  private String[] m_propertyClasses;
  private String m_widgetClass;
  private String m_bindingClass = "com.extjs.gxt.ui.client.binding.FieldBinding";
  private boolean m_generic;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getWidgetClass() {
    return m_widgetClass;
  }

  public void setWidgetClass(String widgetClass) {
    m_widgetClass = widgetClass;
  }

  public String getBindingClass() {
    return m_bindingClass;
  }

  public void setBindingClass(String bindingClass) {
    m_bindingClass = bindingClass;
  }

  public boolean isGeneric() {
    return m_generic;
  }

  public void setGeneric() {
    m_generic = true;
  }

  public String getPropertyClass() {
    return m_propertyClasses[m_propertyClasses.length - 1];
  }

  public void setPropertyClass(String classes) {
    m_propertyClasses = StringUtils.split(classes);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isDefault(Object property) {
    PropertyAdapter propertyAdapter = (PropertyAdapter) property;
    Class<?> propertyType = propertyAdapter.getType();
    if (propertyType != null) {
      return ArrayUtils.contains(m_propertyClasses, propertyType.getName());
    }
    return false;
  }
}