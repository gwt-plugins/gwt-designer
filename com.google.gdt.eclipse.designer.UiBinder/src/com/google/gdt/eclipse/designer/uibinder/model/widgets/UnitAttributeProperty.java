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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import org.eclipse.wb.internal.core.model.property.ITypedProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.clipboard.IClipboardObjectProperty;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;
import org.eclipse.wb.internal.core.xml.model.property.editor.EnumPropertyEditor;

/**
 * {@link Property} for "unit" attribute.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public class UnitAttributeProperty extends XmlProperty
    implements
      ITypedProperty,
      IClipboardObjectProperty {
  private final String m_attribute;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UnitAttributeProperty(XmlObjectInfo object) {
    this(object, "Unit", "unit");
  }

  public UnitAttributeProperty(XmlObjectInfo object, String title, String attribute) {
    super(object, title, PropertyCategory.system(7), EnumPropertyEditor.INSTANCE);
    m_attribute = attribute;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ITypedProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  public Class<?> getType() {
    return ExecutionUtils.runObject(new RunnableObjectEx<Class<?>>() {
      public Class<?> runObject() throws Exception {
        ClassLoader classLoader = m_object.getContext().getClassLoader();
        return classLoader.loadClass("com.google.gwt.dom.client.Style$Unit");
      }
    });
  }

  /**
   * @return the <code>Unit</code>, may be converted from {@link String}.
   */
  protected Object toUnit(Object unit) {
    if (unit instanceof String) {
      unit = ReflectionUtils.getFieldObject(getType(), (String) unit);
    }
    return unit;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModified() throws Exception {
    return m_object.getAttributeValue(m_attribute) != UNKNOWN_VALUE;
  }

  @Override
  public Object getValue() throws Exception {
    Object value = m_object.getAttributeValue(m_attribute);
    if (value == UNKNOWN_VALUE) {
      value = m_object.getAttributeValue(m_attribute + ".default");
    }
    return value;
  }

  @Override
  protected void setValueEx(Object value) throws Exception {
    if (value instanceof Enum<?>) {
      Enum<?> enumValue = (Enum<?>) value;
      value = enumValue.name();
    }
    if (value instanceof String) {
      m_object.setAttribute(m_attribute, ((String) value));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardObjectProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getClipboardObject() throws Exception {
    Object value = getValue();
    return ((Enum<?>) value).name();
  }

  public void setClipboardObject(Object value) throws Exception {
    setValue(value);
  }
}
