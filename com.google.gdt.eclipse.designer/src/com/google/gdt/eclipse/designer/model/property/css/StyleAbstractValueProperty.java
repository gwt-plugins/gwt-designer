/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
