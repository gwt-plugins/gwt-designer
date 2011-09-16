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
import org.eclipse.wb.internal.css.semantics.AbstractValue;
import org.eclipse.wb.internal.css.semantics.Semantics;

/**
 * Property for {@link AbstractValue};
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
abstract class StyleAbstractValueProperty extends Property {
  protected final RuleAccessor m_accessor;
  protected final ObjectInfo m_object;
  protected final String m_styleName;
  protected final String m_valueObjectPath;
  protected final String m_title;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyleAbstractValueProperty(RuleAccessor accessor,
      String styleName,
      String valueObjectPath,
      String title,
      PropertyEditor editor) {
    super(editor);
    m_accessor = accessor;
    m_valueObjectPath = valueObjectPath;
    m_object = accessor.getObject();
    m_styleName = styleName;
    m_title = title;
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Semantics} of corresponding CSS rule.
   */
  protected final Semantics getSemantics() throws Exception {
    return m_accessor.getSemantics(m_styleName);
  }
}
