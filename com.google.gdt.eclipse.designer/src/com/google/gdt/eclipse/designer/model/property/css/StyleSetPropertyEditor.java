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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * {@link PropertyEditor} for set of {@link String} items.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
public class StyleSetPropertyEditor extends TextDisplayPropertyEditor
    implements
      IComplexPropertyEditor {
  private final String[] m_items;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyleSetPropertyEditor(String... items) {
    m_items = items;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getText(Property property) throws Exception {
    return (String) property.getValue();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IComplexPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Property[] getProperties(Property property) throws Exception {
    Property[] properties = new Property[m_items.length];
    for (int i = 0; i < m_items.length; i++) {
      String item = m_items[i];
      properties[i] = new ItemProperty(property, item);
    }
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // 
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ItemProperty extends Property {
    private final Property m_property;
    private final String m_item;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ItemProperty(Property property, String item) {
      super(BooleanPropertyEditor.INSTANCE);
      m_property = property;
      m_item = item;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getTitle() {
      return m_item;
    }

    @Override
    public boolean isModified() throws Exception {
      return (Boolean) getValue();
    }

    @Override
    public Object getValue() throws Exception {
      List<String> items = getPropertyItems();
      return items.contains(m_item);
    }

    @Override
    public void setValue(Object value) throws Exception {
      List<String> items = getPropertyItems();
      if (value instanceof Boolean && (Boolean) value) {
        if (!items.contains(m_item)) {
          items.add(m_item);
        }
      } else {
        items.remove(m_item);
      }
      m_property.setValue(StringUtils.join(items, " "));
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Utils
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the {@link String}s value of {@link #m_property}.
     */
    private List<String> getPropertyItems() throws Exception {
      String s = (String) m_property.getValue();
      s = StringUtils.trimToEmpty(s);
      return Lists.newArrayList(StringUtils.split(s));
    }
  }
}
