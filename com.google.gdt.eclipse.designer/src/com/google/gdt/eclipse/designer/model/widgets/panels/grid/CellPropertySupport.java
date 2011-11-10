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
package com.google.gdt.eclipse.designer.model.widgets.panels.grid;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.property.css.StylePropertyEditor;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.JavaProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.converter.BooleanConverter;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.EnumCustomPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.IntegerPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import java.util.List;

/**
 * Support for "Cell" property of {@link HTMLTableInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class CellPropertySupport {
  private final HTMLTableInfo m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CellPropertySupport(HTMLTableInfo panel) {
    m_panel = panel;
    contributeCellProperties();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Cell" complex property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contributes <code>"Cell"</code> complex property for each {@link WidgetInfo} child of this
   * {@link CellPropertySupport}.
   */
  private void contributeCellProperties() {
    m_panel.addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (javaInfo instanceof WidgetInfo && javaInfo.getParent() == m_panel) {
          WidgetInfo widget = (WidgetInfo) javaInfo;
          // prepare "Cell" property
          Property cellProperty = (Property) widget.getArbitraryValue(this);
          if (cellProperty == null) {
            cellProperty = createCellComplexProperty(widget);
            widget.putArbitraryValue(this, cellProperty);
          }
          // add "Cell" property
          properties.add(cellProperty);
        }
      }
    });
  }

  private Property createCellComplexProperty(final WidgetInfo widget) throws Exception {
    GenericPropertyImpl widthProperty = createStringProperty(widget, "setWidth", "width");
    GenericPropertyImpl heightProperty = createStringProperty(widget, "setHeight", "height");
    GenericPropertyImpl styleNameProperty =
        createStyleProperty(widget, "setStyleName", "styleName");
    GenericPropertyImpl stylePrimaryNameProperty =
        createStyleProperty(widget, "setStylePrimaryName", "stylePrimaryName");
    GenericPropertyImpl visibleProperty =
        createBooleanProperty(widget, "setVisible", "visible", true);
    GenericPropertyImpl wordWrapProperty =
        createBooleanProperty(widget, "setWordWrap", "wordWrap", false);
    // "horizontalAlignment"
    Property horizontalAlignmentProperty;
    {
      horizontalAlignmentProperty =
          new CellAlignmentProperty<ColumnInfo.Alignment>(widget, "horizontalAlignment",
              ColumnInfo.Alignment.class, ColumnInfo.Alignment.UNKNOWN) {
            @Override
            protected ColumnInfo.Alignment getAlignmentEx(CellConstraintsSupport constraints) {
              return constraints.getHorizontalAlignment();
            }

            @Override
            protected void setAlignmentEx(CellConstraintsSupport constraints,
                ColumnInfo.Alignment alignment) throws Exception {
              constraints.setHorizontalAlignment(alignment);
            }
          };
    }
    // "verticalAlignment"
    Property verticalAlignmentProperty;
    {
      verticalAlignmentProperty =
          new CellAlignmentProperty<RowInfo.Alignment>(widget, "verticalAlignment",
              RowInfo.Alignment.class, RowInfo.Alignment.UNKNOWN) {
            @Override
            protected RowInfo.Alignment getAlignmentEx(CellConstraintsSupport constraints) {
              return constraints.getVerticalAlignment();
            }

            @Override
            protected void setAlignmentEx(CellConstraintsSupport constraints,
                RowInfo.Alignment alignment) throws Exception {
              constraints.setVerticalAlignment(alignment);
            }
          };
    }
    // required properties
    List<Property> properties =
        Lists.newArrayList(
            horizontalAlignmentProperty,
            verticalAlignmentProperty,
            widthProperty,
            heightProperty,
            styleNameProperty,
            stylePrimaryNameProperty,
            visibleProperty,
            wordWrapProperty);
    // optional FlexTable properties
    if (m_panel instanceof FlexTableInfo) {
      addSpanProperties(widget, properties);
    }
    // create complex "Cell" property
    ComplexProperty cellProperty = new ComplexProperty("Cell", "(cell properties)");
    cellProperty.setCategory(PropertyCategory.system(7));
    cellProperty.setProperties(properties);
    return cellProperty;
  }

  private void addSpanProperties(final WidgetInfo widget, List<Property> properties) {
    Property colSpanProperty = new JavaProperty(widget, "colSpan", IntegerPropertyEditor.INSTANCE) {
      @Override
      public boolean isModified() throws Exception {
        return (Integer) getValue() != 1;
      }

      @Override
      public Object getValue() throws Exception {
        return HTMLTableInfo.getConstraints(widget).getWidth();
      }

      @Override
      public void setValue(Object value) throws Exception {
        final int newSpan = value instanceof Integer ? (Integer) value : 1;
        ExecutionUtils.run(widget, new RunnableEx() {
          public void run() throws Exception {
            m_panel.setComponentColSpan(widget, 1);
            m_panel.setComponentColSpan(widget, newSpan);
          }
        });
      }
    };
    Property rowSpanProperty = new JavaProperty(widget, "rowSpan", IntegerPropertyEditor.INSTANCE) {
      @Override
      public boolean isModified() throws Exception {
        return (Integer) getValue() != 1;
      }

      @Override
      public Object getValue() throws Exception {
        return HTMLTableInfo.getConstraints(widget).getHeight();
      }

      @Override
      public void setValue(Object value) throws Exception {
        final int newSpan = value instanceof Integer ? (Integer) value : 1;
        ExecutionUtils.run(widget, new RunnableEx() {
          public void run() throws Exception {
            m_panel.setComponentRowSpan(widget, 1);
            m_panel.setComponentRowSpan(widget, newSpan);
          }
        });
      }
    };
    properties.add(colSpanProperty);
    properties.add(rowSpanProperty);
  }

  private static GenericPropertyImpl createStringProperty(WidgetInfo widget,
      String methodName,
      String title) {
    PropertyEditor propertyEditor = StringPropertyEditor.INSTANCE;
    return createStringConverterProperty(widget, methodName, title, propertyEditor);
  }

  private static GenericPropertyImpl createStyleProperty(WidgetInfo widget,
      String methodName,
      String title) {
    PropertyEditor propertyEditor = StylePropertyEditor.INSTANCE;
    return createStringConverterProperty(widget, methodName, title, propertyEditor);
  }

  private static GenericPropertyImpl createStringConverterProperty(WidgetInfo widget,
      String methodName,
      String title,
      PropertyEditor propertyEditor) {
    ExpressionConverter converter = StringConverter.INSTANCE;
    return createProperty(widget, methodName, "java.lang.String", title, converter, propertyEditor);
  }

  private static GenericPropertyImpl createBooleanProperty(WidgetInfo widget,
      String methodName,
      String title,
      boolean defaultValue) {
    ExpressionConverter converter = BooleanConverter.INSTANCE;
    PropertyEditor propertyEditor = BooleanPropertyEditor.INSTANCE;
    return createProperty(
        widget,
        methodName,
        "boolean",
        title,
        defaultValue,
        converter,
        propertyEditor);
  }

  private static GenericPropertyImpl createProperty(WidgetInfo widget,
      String methodName,
      String typeName,
      String title,
      ExpressionConverter converter,
      PropertyEditor propertyEditor) {
    return createProperty(
        widget,
        methodName,
        typeName,
        title,
        Property.UNKNOWN_VALUE,
        converter,
        propertyEditor);
  }

  private static GenericPropertyImpl createProperty(WidgetInfo widget,
      String methodName,
      String typeName,
      String title,
      Object defaultValue,
      ExpressionConverter converter,
      PropertyEditor propertyEditor) {
    ExpressionAccessor expressionAccessor =
        new CellFormatterExpressionAccessor(methodName, typeName);
    return new GenericPropertyImpl(widget,
        title,
        new ExpressionAccessor[]{expressionAccessor},
        defaultValue,
        converter,
        propertyEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Alignment" property
  //
  ////////////////////////////////////////////////////////////////////////////
  private abstract class CellAlignmentProperty<T extends Enum<?>> extends JavaProperty {
    private final WidgetInfo m_widget;
    private final T m_defaultValue;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public CellAlignmentProperty(WidgetInfo widget, String title, Class<T> enumClass, T defaultValue) {
      super(widget, title, new EnumCustomPropertyEditor());
      ((EnumCustomPropertyEditor) getEditor()).configure(enumClass);
      m_widget = widget;
      m_defaultValue = defaultValue;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isModified() throws Exception {
      return getValue() != m_defaultValue;
    }

    @Override
    public Object getValue() throws Exception {
      CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(m_widget);
      return getAlignmentEx(constraints);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setValue(Object value) throws Exception {
      T alignment = value == Property.UNKNOWN_VALUE ? m_defaultValue : (T) value;
      setAlignment(alignment);
    }

    private void setAlignment(final T alignment) {
      ExecutionUtils.run(m_widget, new RunnableEx() {
        public void run() throws Exception {
          CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(m_widget);
          setAlignmentEx(constraints, alignment);
        }
      });
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Abstract
    //
    ////////////////////////////////////////////////////////////////////////////
    protected abstract T getAlignmentEx(CellConstraintsSupport constraints);

    protected abstract void setAlignmentEx(CellConstraintsSupport constraints, T alignment)
        throws Exception;
  }
}
