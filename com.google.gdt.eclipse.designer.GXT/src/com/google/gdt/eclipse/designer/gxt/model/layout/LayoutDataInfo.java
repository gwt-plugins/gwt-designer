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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.model.widgets.support.IGwtStateProvider;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Model for <code>LayoutData</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public class LayoutDataInfo extends JavaInfo implements IGwtStateProvider {
  private final LayoutDataInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutDataInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    removeIfContainerHasNoLayout();
    contributeLayoutDataProperties_toWidget();
    addMaterializeSupport();
    turnIntoBlock_whenEnsureVariable();
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
  private void removeIfContainerHasNoLayout() {
    addBroadcastListener(new ObjectInfoTreeComplete() {
      public void invoke() throws Exception {
        removeBroadcastListener(this);
        WidgetInfo widget = getWidget();
        // if dangling LayoutData, ignore it
        if (widget == null) {
          return;
        }
        // if no parent with Layout, remove this LayoutData
        LayoutContainerInfo container = (LayoutContainerInfo) widget.getParent();
        if (container == null || !container.hasLayoutData()) {
          widget.removeChild(m_this);
        }
      }
    });
  }

  /**
   * Contribute "LayoutData" complex property to our {@link WidgetInfo}.
   */
  private void contributeLayoutDataProperties_toWidget() {
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        if (isActiveForWidget(javaInfo)) {
          addLayoutDataProperties(properties);
        }
      }

      private boolean isActiveForWidget(JavaInfo widget) {
        return widget.getChildren().contains(m_this);
      }
    });
  }

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
        if (property.getJavaInfo() == m_this) {
          on_setPropertyExpression(property);
        }
      }
    });
  }

  protected void on_setPropertyExpression(GenericPropertyImpl property) throws Exception {
    materialize();
  }

  protected final void materialize() throws Exception {
    if (isVirtual()) {
      ((VirtualLayoutDataVariableSupport) getVariableSupport()).materialize();
    }
  }

  /**
   * @return <code>true</code> if this {@link LayoutDataInfo} is virtual.
   */
  private boolean isVirtual() {
    return getVariableSupport() instanceof VirtualLayoutDataVariableSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Performs following optimizations:
   * <ul>
   * <li>When <code>LayoutData</code> has {@link LocalUniqueVariableSupport}, but it is used just to
   * call <code>add(Widget,LayoutData)</code>, then variable may be inlined.</li>
   * <li>When <code>LayoutData</code> has all default values, then we can delete it at all.</li>
   * </ul>
   */
  private void turnIntoBlock_whenEnsureVariable() {
    // no invocations/fields -> inline
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void endEdit_aboutToRefresh() throws Exception {
        if (getVariableSupport() instanceof LocalUniqueVariableSupport) {
          LocalUniqueVariableSupport variableSupport =
              (LocalUniqueVariableSupport) getVariableSupport();
          if (variableSupport.canInline()) {
            variableSupport.inline();
          }
        }
      }
    });
    // is default -> delete
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void endEdit_aboutToRefresh() throws Exception {
        if (!isDeleted()
            && getCreationSupport() instanceof ConstructorCreationSupport
            && getMethodInvocations().isEmpty()
            && getFieldAssignments().isEmpty()) {
          ConstructorCreationSupport creationSupport =
              (ConstructorCreationSupport) getCreationSupport();
          ClassInstanceCreation creation = creationSupport.getCreation();
          String signature = creationSupport.getDescription().getSignature();
          // prepare arguments
          List<Expression> arguments = DomGenerics.arguments(creation);
          if (!AstNodeUtils.areLiterals(arguments)) {
            return;
          }
          // evaluate arguments
          List<Object> argumentValues;
          {
            EditorState state = JavaInfoUtils.getState(m_this);
            EvaluationContext context =
                new EvaluationContext(state.getEditorLoader(), state.getFlowDescription());
            argumentValues = Lists.newArrayList();
            for (Expression argument : arguments) {
              Object value = AstEvaluationEngine.evaluate(context, argument);
              JavaInfoEvaluationHelper.setValue(argument, value);
              argumentValues.add(value);
            }
          }
          // delete, if default constructor arguments
          if (isDefault(signature, argumentValues)) {
            delete();
          }
          // post process
          {
            boolean creationChanged =
                postProcessConstructorCreation(signature, creation, argumentValues);
            if (creationChanged) {
              setCreationSupport(new ConstructorCreationSupport(creation));
            }
          }
        }
      }

      /**
       * @return <code>true</code> if existing <code>isDefault</code> script says that object of
       *         this {@link LayoutDataInfo} is in default state.
       */
      private boolean isDefault(String signature, List<Object> args) throws Exception {
        String script = JavaInfoUtils.getParameter(m_this, "isDefault");
        if (script != null) {
          Map<String, Object> variables = ImmutableMap.of("signature", signature, "args", args);
          return (Boolean) ScriptUtils.evaluate(
              JavaInfoUtils.getClassLoader(m_this),
              script,
              variables);
        }
        return false;
      }
    });
  }

  /**
   * This method is invoked when all edit operations are done and {@link ObjectInfo#endEdit()} is
   * going to perform refresh. Here we can analyze {@link ClassInstanceCreation} and remove default
   * arguments.
   */
  protected boolean postProcessConstructorCreation(String signature,
      ClassInstanceCreation creation,
      List<Object> argumentValues) throws Exception {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "LayoutData" property
  //
  ////////////////////////////////////////////////////////////////////////////
  private ComplexProperty m_complexProperty;

  /**
   * Adds properties of this {@link LayoutDataInfo} to the properties of its {@link WidgetInfo}.
   */
  private void addLayoutDataProperties(List<Property> properties) throws Exception {
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
      // set sub-properties
      m_complexProperty.setProperties(getFilteredProperties());
    }
    // add property
    properties.add(m_complexProperty);
  }

  /**
   * @return the {@link Property}'s to show in complex property of {@link LayoutDataInfo} parent.
   */
  private Property[] getFilteredProperties() throws Exception {
    Property[] properties = getProperties();
    // For some layout data it needs to exclude some properties, such as "Class" or "Constructor".
    // This can be done using "layoutData.exclude-properties" parameter of class description.
    String propertiesExcludeString =
        JavaInfoUtils.getParameter(this, "layoutData.exclude-properties");
    if (propertiesExcludeString != null) {
      List<Property> filteredProperties = Lists.newArrayList();
      String[] propertiesExclude = StringUtils.split(propertiesExcludeString);
      props : for (Property property : properties) {
        for (String propertyExclude : propertiesExclude) {
          if (property.getTitle().equals(propertyExclude)) {
            continue props;
          }
        }
        filteredProperties.add(property);
      }
      properties = filteredProperties.toArray(new Property[filteredProperties.size()]);
    }
    return properties;
  }
}