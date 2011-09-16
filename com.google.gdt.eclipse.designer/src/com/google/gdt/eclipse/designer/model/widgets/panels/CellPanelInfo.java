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
package com.google.gdt.eclipse.designer.model.widgets.panels;

import com.google.gdt.eclipse.designer.model.property.accessor.CellExpressionAccessor;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.StaticFieldPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Model for GWT <code>CellPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public abstract class CellPanelInfo extends ComplexPanelInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CellPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
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
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (javaInfo instanceof WidgetInfo && javaInfo.getParent() == panel) {
          WidgetInfo component = (WidgetInfo) javaInfo;
          // prepare "Cell" property
          Property cellProperty = (Property) component.getArbitraryValue(this);
          if (cellProperty == null) {
            cellProperty = getCellComplexProperty(component);
            component.putArbitraryValue(this, cellProperty);
          }
          // add "Cell" property
          properties.add(cellProperty);
        }
      }

      private Property getCellComplexProperty(WidgetInfo component) throws Exception {
        ClassLoader editorLoader = JavaInfoUtils.getClassLoader(panel);
        // "width"
        GenericPropertyImpl widthProperty;
        {
          ExpressionAccessor expressionAccessor =
              new CellExpressionAccessor(panel, "setCellWidth", "java.lang.String");
          widthProperty =
              new GenericPropertyImpl(component,
                  "width",
                  new ExpressionAccessor[]{expressionAccessor},
                  "",
                  StringConverter.INSTANCE,
                  StringPropertyEditor.INSTANCE);
        }
        // "height"
        GenericPropertyImpl heightProperty;
        {
          ExpressionAccessor expressionAccessor =
              new CellExpressionAccessor(panel, "setCellHeight", "java.lang.String");
          heightProperty =
              new GenericPropertyImpl(component,
                  "height",
                  new ExpressionAccessor[]{expressionAccessor},
                  "",
                  StringConverter.INSTANCE,
                  StringPropertyEditor.INSTANCE);
        }
        // "horizontalAlignment"
        GenericPropertyImpl horizontalAlignmentProperty;
        {
          StaticFieldPropertyEditor propertyEditor = new StaticFieldPropertyEditor();
          Class<?> hasHorizontalAlignmentClass =
              editorLoader.loadClass("com.google.gwt.user.client.ui.HasHorizontalAlignment");
          ExpressionAccessor expressionAccessor =
              new CellExpressionAccessor(panel,
                  "setCellHorizontalAlignment",
                  "com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant");
          propertyEditor.configure(hasHorizontalAlignmentClass, new String[]{
              "ALIGN_LEFT",
              "ALIGN_CENTER",
              "ALIGN_RIGHT",
              "ALIGN_DEFAULT"});
          Object defaultValue =
              ReflectionUtils.getFieldObject(hasHorizontalAlignmentClass, "ALIGN_DEFAULT");
          horizontalAlignmentProperty =
              new GenericPropertyImpl(component,
                  "horizontalAlignment",
                  new ExpressionAccessor[]{expressionAccessor},
                  defaultValue,
                  null,
                  propertyEditor);
        }
        // "verticalAlignment"
        GenericPropertyImpl verticalAlignmentProperty;
        {
          StaticFieldPropertyEditor propertyEditor = new StaticFieldPropertyEditor();
          Class<?> hasVerticalAlignmentClass =
              editorLoader.loadClass("com.google.gwt.user.client.ui.HasVerticalAlignment");
          propertyEditor.configure(hasVerticalAlignmentClass, new String[]{
              "ALIGN_TOP",
              "ALIGN_MIDDLE",
              "ALIGN_BOTTOM"});
          ExpressionAccessor expressionAccessor =
              new CellExpressionAccessor(panel,
                  "setCellVerticalAlignment",
                  "com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant");
          Object defaultValue =
              ReflectionUtils.getFieldObject(hasVerticalAlignmentClass, "ALIGN_TOP");
          verticalAlignmentProperty =
              new GenericPropertyImpl(component,
                  "verticalAlignment",
                  new ExpressionAccessor[]{expressionAccessor},
                  defaultValue,
                  null,
                  propertyEditor);
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
