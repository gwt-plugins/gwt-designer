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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAdd;
import org.eclipse.wb.internal.core.xml.model.clipboard.IClipboardObjectProperty;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;

import java.util.List;

/**
 * Model for <code>com.google.gwt.user.client.ui.DisclosurePanel</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public class DisclosurePanelInfo extends CompositeInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DisclosurePanelInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    setDefaultHeader_onCreate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasts
  //
  ////////////////////////////////////////////////////////////////////////////
  private void setDefaultHeader_onCreate() {
    addBroadcastListener(new XmlObjectAdd() {
      @Override
      public void after(ObjectInfo parent, XmlObjectInfo child) throws Exception {
        if (child == DisclosurePanelInfo.this) {
          m_headerProperty.setValue("New DisclosurePanel");
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    properties.add(m_headerProperty);
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Header
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Property m_headerProperty = new HeaderProperty(this,
      "Header",
      PropertyCategory.system(7),
      StringPropertyEditor.INSTANCE);

  private final class HeaderProperty extends XmlProperty implements IClipboardObjectProperty {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    private HeaderProperty(XmlObjectInfo object,
        String title,
        PropertyCategory category,
        PropertyEditor propertyEditor) {
      super(object, title, category, propertyEditor);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isModified() throws Exception {
      return getValue() != UNKNOWN_VALUE;
    }

    @Override
    public Object getValue() throws Exception {
      DocumentElement headerElement = getHeaderElement(false);
      if (headerElement == null) {
        return UNKNOWN_VALUE;
      }
      return headerElement.getTextNode().getText();
    }

    @Override
    protected void setValueEx(Object value) throws Exception {
      DocumentElement headerElement = getHeaderElement(true);
      if (value instanceof String) {
        String text = (String) value;
        headerElement.setText(text, false);
      } else {
        headerElement.remove();
      }
    }

    private DocumentElement getHeaderElement(boolean ensure) {
      DocumentElement element = getElement();
      String tag = element.getTagNS() + "header";
      DocumentElement headerElement = element.getChild(tag, false);
      if (headerElement == null && ensure) {
        headerElement = new DocumentElement(tag);
        element.addChild(headerElement, 0);
      }
      return headerElement;
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
}
