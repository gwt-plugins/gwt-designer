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

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.model.widgets.IUIObjectSizeSupport;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import java.util.Map;

/**
 * Helper for support width/height properties in {@link UIObjectInfo}.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public class UIObjectSizeSupport implements IUIObjectSizeSupport {
  private final UIObjectInfo m_object;
  private final PropertyEditor m_sizePropertyEditor = new Size_PropertyEditor();
  private final Property m_sizeProperty = new Size_Property();
  private final Property[] m_sizeElementProperties = new Property[]{
      new SizeWidth_Propety(),
      new SizeHeight_Property()};

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UIObjectSizeSupport(UIObjectInfo object) throws Exception {
    m_object = object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setSize(int width, int height) throws Exception {
    setSize(new Dimension(width, height));
  }

  public void setSize(Dimension size) throws Exception {
    if (size != null) {
      int clientWidth = size.width;
      int clientHeight = size.height;
      // exclude decorations
      if (m_object.getObject() != null) {
        if (executeBooleanScript("setSize.excludeMargin")) {
          Insets margins = m_object.getMargins();
          clientWidth -= margins.getWidth();
          clientHeight -= margins.getHeight();
        }
        if (executeBooleanScript("setSize.excludeBorder")) {
          Insets borders = m_object.getBorders();
          clientWidth -= borders.getWidth();
          clientHeight -= borders.getHeight();
        }
        if (executeBooleanScript("setSize.excludePadding")) {
          Insets paddings = m_object.getPaddings();
          clientWidth -= paddings.getWidth();
          clientHeight -= paddings.getHeight();
        }
      }
      // set size in pixels
      String width = clientWidth + "px";
      String height = clientHeight + "px";
      setSize(width, height);
    } else {
      setSize(NO_SIZE, NO_SIZE);
    }
  }

  public void setSize(final String width, final String height) throws Exception {
    if (width != null || height != null) {
      ExecutionUtils.run(m_object, new RunnableEx() {
        public void run() throws Exception {
          setSize0(width, height);
        }
      });
    }
  }

  /**
   * @return {@link Boolean} result of executing script.
   */
  private boolean executeBooleanScript(String scriptName) throws Exception {
    Map<String, Object> variables = Maps.newTreeMap();
    variables.put("model", m_object);
    variables.put("object", m_object.getObject());
    String script = XmlObjectUtils.getParameter(m_object, scriptName);
    return (Boolean) m_object.getUIObjectUtils().executeScript(script, variables);
  }

  /**
   * Implementation for {@link #setSize(String, String)}.
   */
  private void setSize0(String width, String height) throws Exception {
    // XXX
    if (m_object instanceof IsWidgetWrappedInfo) {
      return;
    }
    if (NO_SIZE.equals(width)) {
      m_object.removeAttribute("width");
    } else if (width != null) {
      m_object.setAttribute("width", width);
    }
    if (NO_SIZE.equals(height)) {
      m_object.removeAttribute("height");
    } else if (height != null) {
      m_object.setAttribute("height", height);
    }
  }

  /**
   * @return the complex "Size" property.
   */
  public Property getSizeProperty() {
    return m_sizeProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SizeWidth_Property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link Property} for "width" of size.
   */
  private final class SizeWidth_Propety extends Property {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SizeWidth_Propety() {
      super(StringPropertyEditor.INSTANCE);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getTitle() {
      return "width";
    }

    @Override
    public boolean isModified() throws Exception {
      return getValue() != null;
    }

    @Override
    public Object getValue() throws Exception {
      return m_object.getAttribute("width");
    }

    @Override
    public void setValue(Object value) throws Exception {
      if (value instanceof String) {
        setSize((String) value, null);
      } else if (value == Property.UNKNOWN_VALUE) {
        setSize(NO_SIZE, null);
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // SizeHeight_Property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link Property} for "height" of size.
   */
  private final class SizeHeight_Property extends Property {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SizeHeight_Property() {
      super(StringPropertyEditor.INSTANCE);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getTitle() {
      return "height";
    }

    @Override
    public boolean isModified() throws Exception {
      return getValue() != null;
    }

    @Override
    public Object getValue() throws Exception {
      return m_object.getAttribute("height");
    }

    @Override
    public void setValue(Object value) throws Exception {
      if (value instanceof String) {
        setSize(null, (String) value);
      } else if (value == Property.UNKNOWN_VALUE) {
        setSize(null, NO_SIZE);
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Size_Property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Property to set size of {@link UIObjectInfo}.
   * 
   * @author scheglov_ke
   * @coverage gwt.property
   */
  private final class Size_Property extends Property {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public Size_Property() {
      super(m_sizePropertyEditor);
      setCategory(PropertyCategory.system(8));
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getTitle() {
      return "Size";
    }

    @Override
    public Object getValue() throws Exception {
      return null;
    }

    @Override
    public boolean isModified() throws Exception {
      return m_sizeElementProperties[0].isModified() || m_sizeElementProperties[1].isModified();
    }

    @Override
    public void setValue(Object value) throws Exception {
      if (value == Property.UNKNOWN_VALUE) {
        setSize(NO_SIZE, NO_SIZE);
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Size_PropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link PropertyEditor} for complex "Size" property.
   */
  private final class Size_PropertyEditor extends TextDisplayPropertyEditor
      implements
        IComplexPropertyEditor {
    ////////////////////////////////////////////////////////////////////////////
    //
    // TextDisplayPropertyEditor
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String getText(Property property) throws Exception {
      Rectangle bounds = m_object.getBounds();
      String width;
      String height;
      {
        Object widthObject = m_sizeElementProperties[0].getValue();
        if (widthObject instanceof String) {
          width = (String) widthObject;
        } else {
          width = Integer.toString(bounds.width);
        }
      }
      {
        Object heightObject = m_sizeElementProperties[1].getValue();
        if (heightObject instanceof String) {
          height = (String) heightObject;
        } else {
          height = Integer.toString(bounds.height);
        }
      }
      return "(" + width + ", " + height + ")";
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IComplexPropertyEditor
    //
    ////////////////////////////////////////////////////////////////////////////
    public Property[] getProperties(Property property) throws Exception {
      return m_sizeElementProperties;
    }
  }
}
