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
package com.google.gdt.eclipse.designer.uibinder.model.util;

import com.google.gdt.eclipse.designer.uibinder.model.widgets.UIObjectInfo;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.AbstractTextPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;

/**
 * {@link Property} for editing name of "@UiField".
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class NameProperty extends XmlProperty {
  private static final String TITLE_TOOLTIP =
      "Optional name of @UiField, used to manipulate widget programatically.";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NameProperty(UIObjectInfo object) {
    super(object, "UiField", NamePropertyEditor.INSTANCE);
    setCategory(PropertyCategory.system(0));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getValue() throws Exception {
    String name = NameSupport.getName(m_object);
    return name != null ? name : UNKNOWN_VALUE;
  }

  @Override
  public boolean isModified() throws Exception {
    return getValue() != UNKNOWN_VALUE;
  }

  @Override
  protected void setValueEx(Object value) throws Exception {
    if (value == UNKNOWN_VALUE) {
      NameSupport.removeName(m_object);
    }
    if (value instanceof String) {
      String name = ((String) value).trim();
      NameSupport.setName(m_object, name);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adapter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public <T> T getAdapter(Class<T> adapter) {
    if (adapter == PropertyTooltipProvider.class) {
      return adapter.cast(new PropertyTooltipTextProvider() {
        @Override
        protected String getText(Property property) throws Exception {
          return TITLE_TOOLTIP;
        }
      });
    }
    return super.getAdapter(adapter);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // NamePropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class NamePropertyEditor extends AbstractTextPropertyEditor {
    private static final PropertyEditor INSTANCE = new NamePropertyEditor();

    ////////////////////////////////////////////////////////////////////////////
    //
    // Text
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String getText(Property property) throws Exception {
      Object value = property.getValue();
      if (value instanceof String) {
        return (String) value;
      }
      return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // AbstractTextPropertyEditor
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String getEditorText(Property property) throws Exception {
      return getText(property);
    }

    @Override
    protected boolean setEditorText(Property property, String text) throws Exception {
      String name = text.trim();
      // validate name
      {
        XmlObjectInfo object = ((NameProperty) property).getObject();
        String errorMessage = NameSupport.validateName(object, name);
        if (errorMessage != null) {
          UiUtils.openWarning(DesignerPlugin.getShell(), property.getTitle(), errorMessage);
          return false;
        }
      }
      // OK
      property.setValue(name);
      return true;
    }
  }
}
