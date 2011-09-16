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
package com.google.gdt.eclipse.designer.model.property.css;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractTextPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.css.semantics.LengthValue;
import org.eclipse.wb.internal.css.semantics.Semantics;

import org.apache.commons.lang.ObjectUtils;

/**
 * Property for {@link LengthValue};
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
class StyleLengthValueProperty extends StyleAbstractValueProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyleLengthValueProperty(RuleAccessor accessor,
      String styleName,
      String valueObjectPath,
      String title) {
    this(accessor, styleName, valueObjectPath, title, LengthValuePropertyEditor.INSTANCE);
  }

  public StyleLengthValueProperty(RuleAccessor accessor,
      String styleName,
      String valueObjectPath,
      String title,
      PropertyEditor propertyEditor) {
    super(accessor, styleName, valueObjectPath, title, propertyEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModified() throws Exception {
    return getValueObject().hasValue();
  }

  @Override
  public Object getValue() throws Exception {
    return getValueObject().get();
  }

  @Override
  public void setValue(Object value) throws Exception {
    if (value == UNKNOWN_VALUE) {
      value = "";
    }
    if (value instanceof String) {
      final String stringValue = (String) value;
      if (!ObjectUtils.equals(getValue(), stringValue)) {
        ExecutionUtils.run(m_object, new RunnableEx() {
          public void run() throws Exception {
            getValueObject().set(stringValue);
            m_accessor.applySemantics(m_styleName);
          }
        });
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link LengthValue} to edit.
   */
  protected final LengthValue getValueObject() throws Exception {
    Semantics semantics = getSemantics();
    return (LengthValue) ScriptUtils.evaluate(m_valueObjectPath, semantics);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LenghValuePropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  static class LengthValuePropertyEditor extends AbstractTextPropertyEditor {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Instance
    //
    ////////////////////////////////////////////////////////////////////////////
    public static final PropertyEditor INSTANCE = new LengthValuePropertyEditor();

    private LengthValuePropertyEditor() {
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Text
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the text for current color value.
     */
    @Override
    public String getText(Property property) throws Exception {
      return (String) property.getValue();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Editing
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String getEditorText(Property property) throws Exception {
      return getText(property);
    }

    @Override
    protected boolean setEditorText(Property property, String text) throws Exception {
      property.setValue(text);
      return true;
    }
  }
}
