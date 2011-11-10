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

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.DoublePropertyEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.clipboard.IClipboardObjectProperty;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * {@link Property} for "barHeight" attribute of {@link TabLayoutPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class TabLayoutPanelBarHeightProperty extends XmlProperty
    implements
      IClipboardObjectProperty {
  private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("0.#",
      new DecimalFormatSymbols(Locale.ENGLISH));

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TabLayoutPanelBarHeightProperty(XmlObjectInfo object) {
    super(object, "barHeight", PropertyCategory.system(7), DoublePropertyEditor.INSTANCE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isModified() throws Exception {
    return true;
  }

  @Override
  public Object getValue() throws Exception {
    return m_object.getAttributeValue("barHeight");
  }

  @Override
  protected void setValueEx(Object value) throws Exception {
    if (value instanceof Number) {
      m_object.setAttribute("barHeight", SIZE_FORMAT.format(value));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardObjectProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getClipboardObject() throws Exception {
    return getValue();
  }

  public void setClipboardObject(Object value) throws Exception {
    setValue(value);
  }
}
