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

import org.eclipse.wb.core.controls.CComboBox;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractComboBoxPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

/**
 * {@link PropertyEditor} for selecting from list of items, or entering new value.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
public class StringComboBoxPropertyEditor extends AbstractComboBoxPropertyEditor {
  private final String[] m_items;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StringComboBoxPropertyEditor(String... items) {
    m_items = items;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Items
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

  @Override
  protected void addItems(Property property, CComboBox combo) throws Exception {
    for (int i = 0; i < m_items.length; i++) {
      String item = m_items[i];
      combo.addItem(item);
    }
  }

  @Override
  protected void toPropertyEx(Property property, CComboBox combo) throws Exception {
    property.setValue(combo.getEditText());
  }
}
