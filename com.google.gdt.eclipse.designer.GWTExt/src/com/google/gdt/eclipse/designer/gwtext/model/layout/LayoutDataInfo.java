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
package com.google.gdt.eclipse.designer.gwtext.model.layout;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.model.widgets.support.IGwtStateProvider;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.util.List;

/**
 * Model for layout data object.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model.layout
 */
public abstract class LayoutDataInfo extends JavaInfo implements IGwtStateProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutDataInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addLayoutDataPropertySupport();
    addMaterializeSupport();
    new LayoutDataNameSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGWTStateProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public GwtState getState() {
    return getWidget().getState();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link WidgetInfo} that uses this {@link LayoutDataInfo}.
   */
  public final WidgetInfo getWidget() {
    return (WidgetInfo) getParent();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcast events
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When we set value of property for virtual {@link LayoutDataInfo}, often we want to set this
   * value in constructor. But constructor does not exists yet, because our {@link LayoutDataInfo}
   * is virtual. So virtual {@link CreationSupport} adds no any new {@link ExpressionAccessor}. This
   * method adds the listener which forces the {@link LayoutDataInfo} materialization, so
   * {@link VariableSupport} has a chance to replace the {@link CreationSupport} instance for
   * underlying {@link JavaInfo}.
   */
  private void addMaterializeSupport() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void setPropertyExpression(GenericPropertyImpl property,
          String[] source,
          Object[] value,
          boolean[] shouldSet) throws Exception {
        if (property.getJavaInfo() == LayoutDataInfo.this) {
          materialize();
        }
      }
    });
  }

  protected final void materialize() throws Exception {
    if (getVariableSupport() instanceof VirtualLayoutDataVariableSupport) {
      ((VirtualLayoutDataVariableSupport) getVariableSupport()).materialize();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "LayoutData" property
  //
  ////////////////////////////////////////////////////////////////////////////
  private ComplexProperty m_complexProperty;

  /**
   * Contributes <code>"LayoutData"</code> property to host {@link WidgetInfo}.
   */
  private void addLayoutDataPropertySupport() {
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (isActiveForComponent(javaInfo)) {
          addLayoutDataProperty(properties);
        }
      }

      private boolean isActiveForComponent(JavaInfo component) {
        return component.getChildren().contains(LayoutDataInfo.this);
      }
    });
  }

  /**
   * Adds <code>"LayoutData"</code> property of this {@link LayoutDataInfo} to properties of host
   * {@link WidgetInfo}.
   */
  private void addLayoutDataProperty(List<Property> properties) throws Exception {
    // prepare complex property
    if (m_complexProperty == null) {
      String text;
      {
        Class<?> componentClass = getDescription().getComponentClass();
        text = "(" + componentClass.getName() + ")";
      }
      // prepare ComplexProperty
      m_complexProperty = new ComplexProperty("LayoutData", text) {
        @Override
        public boolean isModified() throws Exception {
          return true;
        }

        @Override
        public void setValue(Object value) throws Exception {
          if (value == UNKNOWN_VALUE) {
            delete();
          }
        }
      };
      m_complexProperty.setCategory(PropertyCategory.system(5));
    }
    // set sub-properties
    List<Property> subProperties =
        PropertyUtils.getProperties_excludeByParameter(this, "layoutData.exclude-properties");
    m_complexProperty.setProperties(subProperties);
    // add property
    if (!subProperties.isEmpty()) {
      properties.add(m_complexProperty);
    }
  }
}