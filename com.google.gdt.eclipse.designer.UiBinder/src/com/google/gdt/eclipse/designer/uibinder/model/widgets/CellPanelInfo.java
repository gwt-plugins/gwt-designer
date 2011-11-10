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

import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.xml.model.property.editor.StaticFieldPropertyEditor;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for <code>com.google.gwt.user.client.ui.CellPanel</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public class CellPanelInfo extends ComplexPanelInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CellPanelInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    contributeCellProperties();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Cell" complex property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contributes <code>"Cell"</code> complex property for each {@link WidgetInfo} child of this
   * {@link CellPanelInfo}.
   */
  private void contributeCellProperties() {
    final CellPanelInfo panel = this;
    addBroadcastListener(new XmlObjectAddProperties() {
      public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
        if (object instanceof WidgetInfo && object.getParent() == panel) {
          WidgetInfo widget = (WidgetInfo) object;
          // prepare "Cell" property
          Property cellProperty = (Property) widget.getArbitraryValue(this);
          if (cellProperty == null) {
            cellProperty = getCellComplexProperty(widget);
            widget.putArbitraryValue(this, cellProperty);
          }
          // add "Cell" property
          properties.add(cellProperty);
        }
      }

      private Property getCellComplexProperty(WidgetInfo widget) throws Exception {
        ClassLoader editorLoader = getContext().getClassLoader();
        String namespace = StringUtils.substringBefore(getElement().getTag(), ":") + ":";
        // "width"
        Property widthProperty;
        {
          ExpressionAccessor expressionAccessor = new CellExpressionAccessor(namespace, "width");
          GenericPropertyDescription propertyDescription =
              new GenericPropertyDescription(null, "width", String.class, expressionAccessor);
          propertyDescription.setEditor(StringPropertyEditor.INSTANCE);
          propertyDescription.setConverter(StringConverter.INSTANCE);
          widthProperty = new GenericPropertyImpl(widget, propertyDescription);
        }
        // "height"
        Property heightProperty;
        {
          ExpressionAccessor expressionAccessor = new CellExpressionAccessor(namespace, "height");
          GenericPropertyDescription propertyDescription =
              new GenericPropertyDescription(null, "height", String.class, expressionAccessor);
          propertyDescription.setEditor(StringPropertyEditor.INSTANCE);
          propertyDescription.setConverter(StringConverter.INSTANCE);
          heightProperty = new GenericPropertyImpl(widget, propertyDescription);
        }
        // "horizontalAlignment"
        Property horizontalAlignmentProperty;
        {
          StaticFieldPropertyEditor propertyEditor = new StaticFieldPropertyEditor();
          Class<?> hasHorizontalAlignmentClass =
              editorLoader.loadClass("com.google.gwt.user.client.ui.HasHorizontalAlignment");
          propertyEditor.configure(hasHorizontalAlignmentClass, new String[]{
              "ALIGN_LEFT",
              "ALIGN_CENTER",
              "ALIGN_RIGHT"});
          Object defaultValue =
              ReflectionUtils.getFieldObject(hasHorizontalAlignmentClass, "ALIGN_LEFT");
          // create property
          ExpressionAccessor expressionAccessor =
              new CellExpressionAccessor(namespace, "horizontalAlignment");
          GenericPropertyDescription propertyDescription =
              new GenericPropertyDescription(null,
                  "horizontalAlignment",
                  String.class,
                  expressionAccessor);
          propertyDescription.setEditor(propertyEditor);
          propertyDescription.setDefaultValue(defaultValue);
          horizontalAlignmentProperty = new GenericPropertyImpl(widget, propertyDescription);
        }
        // "verticalAlignment"
        Property verticalAlignmentProperty;
        {
          StaticFieldPropertyEditor propertyEditor = new StaticFieldPropertyEditor();
          Class<?> hasVerticalAlignmentClass =
              editorLoader.loadClass("com.google.gwt.user.client.ui.HasVerticalAlignment");
          propertyEditor.configure(hasVerticalAlignmentClass, new String[]{
              "ALIGN_TOP",
              "ALIGN_MIDDLE",
              "ALIGN_BOTTOM"});
          Object defaultValue =
              ReflectionUtils.getFieldObject(hasVerticalAlignmentClass, "ALIGN_TOP");
          // create property
          ExpressionAccessor expressionAccessor =
              new CellExpressionAccessor(namespace, "verticalAlignment");
          GenericPropertyDescription propertyDescription =
              new GenericPropertyDescription(null,
                  "verticalAlignment",
                  String.class,
                  expressionAccessor);
          propertyDescription.setEditor(propertyEditor);
          propertyDescription.setDefaultValue(defaultValue);
          verticalAlignmentProperty = new GenericPropertyImpl(widget, propertyDescription);
        }
        // create complex "Cell" property
        ComplexProperty cellProperty = new ComplexProperty("Cell", "(cell properties)");
        cellProperty.setCategory(PropertyCategory.system(7));
        cellProperty.setProperties(new Property[]{
            widthProperty,
            heightProperty,
            horizontalAlignmentProperty,
            verticalAlignmentProperty});
        return cellProperty;
      }
    });
  }
}
