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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.css.semantics.AbstractSidedProperty;
import org.eclipse.wb.internal.css.semantics.Semantics;

/**
 * Property for {@link AbstractSidedProperty};
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
abstract class StyleAbstractSidedProperty extends Property {
  protected final RuleAccessor m_accessor;
  protected final ObjectInfo m_object;
  protected final String m_styleName;
  protected final String m_valueObjectPath;
  protected final String m_title;
  protected final Property[] m_properties;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyleAbstractSidedProperty(PropertyEditor editor,
      RuleAccessor accessor,
      String styleName,
      String valueObjectPath,
      String title,
      Property[] properties) {
    super(editor);
    m_accessor = accessor;
    m_object = accessor.getObject();
    m_styleName = styleName;
    m_valueObjectPath = valueObjectPath;
    m_title = title;
    m_properties = properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String getTitle() {
    return m_title;
  }

  @Override
  public final boolean isModified() throws Exception {
    for (Property property : m_properties) {
      if (property.isModified()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public final Object getValue() throws Exception {
    return getSemanticProperty().get();
  }

  @Override
  public final void setValue(final Object value) throws Exception {
    ExecutionUtils.run(m_object, new RunnableEx() {
      public void run() throws Exception {
        if (value instanceof String) {
          getSemanticProperty().set(null, (String) value);
        } else {
          getSemanticProperty().clear();
        }
        m_accessor.applySemantics(m_styleName);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the underlying semantic {@link AbstractSidedProperty}.
   */
  protected final AbstractSidedProperty getSemanticProperty() throws Exception {
    Semantics semantics = m_accessor.getSemantics(m_styleName);
    return (AbstractSidedProperty) ScriptUtils.evaluate(m_valueObjectPath, semantics);
  }
}
