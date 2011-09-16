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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.association.InvocationSecondaryAssociation;
import org.eclipse.wb.core.model.broadcast.EvaluationEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.JavaInfoChildBeforeAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;
import java.util.Map;

/**
 * Model for <code>com.google.gwt.user.client.ui.StackLayoutPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class StackLayoutPanelInfo extends ComplexPanelInfo
    implements
      IStackLayoutPanelInfo<WidgetInfo> {
  private final StackContainerSupport<WidgetInfo> m_stackContainer =
      new StackContainerSupport<WidgetInfo>(this) {
        @Override
        protected List<WidgetInfo> getChildren() {
          return getChildrenWidgets();
        }
      };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StackLayoutPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    ensureNotEmpty_beforeAssociation();
    useHeaderWidgetProperty_asHeaderText();
    addBroadcastListener(new JavaInfoChildBeforeAssociation(this));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  private void ensureNotEmpty_beforeAssociation() {
    addBroadcastListener(new EvaluationEventListener() {
      @Override
      public void evaluateBefore(EvaluationContext context, ASTNode node) throws Exception {
        if (isPossibleAssociation(node)) {
          ensureNotEmpty();
        }
      }

      private boolean isPossibleAssociation(ASTNode node) {
        if (node instanceof MethodInvocation) {
          MethodInvocation invocation = (MethodInvocation) node;
          for (Expression argument : DomGenerics.arguments(invocation)) {
            if (isRepresentedBy(argument)) {
              return true;
            }
          }
        }
        return false;
      }
    });
  }

  /**
   * Runs script that ensures that this panel has at least one child.
   */
  public void ensureNotEmpty() throws Exception {
    JavaInfoUtils.executeScriptParameter(this, "refresh_beforeAssociation");
  }

  /**
   * Adds copy of "text" property of "header" widget as "HeaderText" property.
   */
  private void useHeaderWidgetProperty_asHeaderText() {
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        WidgetInfo header = getHeader(javaInfo);
        if (header != null) {
          for (Property property : header.getProperties()) {
            if (isTextProperty(property)) {
              GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
              Property copy = new GenericPropertyImpl(genericProperty, "HeaderText");
              copy.setCategory(PropertyCategory.system(7));
              properties.add(copy);
              return;
            }
          }
        }
      }

      private WidgetInfo getHeader(JavaInfo javaInfo) {
        if (javaInfo instanceof WidgetInfo && javaInfo.getParent() == StackLayoutPanelInfo.this) {
          WidgetInfo widget = (WidgetInfo) javaInfo;
          for (WidgetInfo header : widget.getChildren(WidgetInfo.class)) {
            if (header.getAssociation() instanceof InvocationSecondaryAssociation) {
              return header;
            }
          }
        }
        return null;
      }

      private boolean isTextProperty(Property property) {
        if (property instanceof GenericPropertyImpl) {
          GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
          GenericPropertyDescription description = genericProperty.getDescription();
          return description != null && description.hasTrueTag("isText");
        }
        return false;
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new RootLayoutPanelTopBoundsSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ensures that given {@link WidgetInfo} become visible on design canvas, may performs refresh().
   */
  private void showWidget(WidgetInfo widget) {
    m_stackContainer.setActive(widget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    showActiveWidget();
  }

  private void showActiveWidget() throws Exception {
    WidgetInfo active = m_stackContainer.getActive();
    if (active != null) {
      int index =
          (Integer) ReflectionUtils.invokeMethod(
              getObject(),
              "getWidgetIndex(com.google.gwt.user.client.ui.Widget)",
              active.getObject());
      // call showWidget()
      try {
        // GWT 2.1
        ReflectionUtils.invokeMethod(getObject(), "showWidget(int,int,boolean)", index, 0, false);
      } catch (Throwable e) {
        // GWT < 2.1
        ReflectionUtils.invokeMethod(getObject(), "showWidget(int,int)", index, 0);
      }
    }
  }

  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    prepareWidgetHandles();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void command_CREATE2(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
    super.command_CREATE2(component, nextComponent);
    addModelHTML(component);
  }

  @Override
  public void command_MOVE2(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
    boolean isReparent = component.getParent() != this;
    super.command_MOVE2(component, nextComponent);
    if (isReparent) {
      addModelHTML(component);
    }
  }

  /**
   * When we add new {@link WidgetInfo} child, we know that <code>new HTML()</code> is used as
   * header. So, we should add model for this <code>HTML</code> as child of added {@link WidgetInfo}
   * .
   */
  private void addModelHTML(WidgetInfo component) throws Exception {
    InvocationChildAssociation association =
        (InvocationChildAssociation) component.getAssociation();
    MethodInvocation invocation = association.getInvocation();
    ClassInstanceCreation headerExpression =
        (ClassInstanceCreation) DomGenerics.arguments(invocation).get(1);
    WidgetInfo header =
        (WidgetInfo) JavaInfoUtils.createJavaInfo(
            getEditor(),
            "com.google.gwt.user.client.ui.HTML",
            new ConstructorCreationSupport(headerExpression));
    header.bindToExpression(headerExpression);
    header.addRelatedNode(headerExpression);
    header.setVariableSupport(new EmptyVariableSupport(header, headerExpression));
    header.setAssociation(new InvocationSecondaryAssociation(invocation));
    component.addChild(header);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
    final Object headerSize = PropertyUtils.getByPath(widget, "Association/headerSize").getValue();
    final String headerText = (String) PropertyUtils.getByPath(widget, "HeaderText").getValue();
    commands.add(new PanelClipboardCommand<StackLayoutPanelInfo>(widget) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(StackLayoutPanelInfo panel, WidgetInfo widget) throws Exception {
        panel.command_CREATE2(widget, null);
        PropertyUtils.getByPath(widget, "Association/headerSize").setValue(headerSize);
        PropertyUtils.getByPath(widget, "HeaderText").setValue(headerText);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Header
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Object that describes header for widgets on {@link StackPanelInfo}.
   */
  public class WidgetHandle extends AbstractWidgetHandle<WidgetInfo> {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public WidgetHandle(WidgetInfo widget) {
      super(widget);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public Rectangle getBounds() {
      return m_widgetToHandleBounds.get(m_widget);
    }

    @Override
    public void show() {
      showWidget(m_widget);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WidgetHandle objects
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<WidgetHandle> m_widgetHandles = Lists.newArrayList();
  private final Map<WidgetInfo, Rectangle> m_widgetToHandleBounds = Maps.newHashMap();

  /**
   * Return list of all {@link WidgetHandle}'s.
   */
  public List<WidgetHandle> getWidgetHandles() {
    return m_widgetHandles;
  }

  /**
   * Fills {@link #m_widgetHandles}.
   */
  private void prepareWidgetHandles() throws Exception {
    GwtState state = getState();
    m_widgetHandles.clear();
    //
    List<?> layoutDataList = (List<?>) ReflectionUtils.getFieldObject(getObject(), "layoutData");
    List<WidgetInfo> widgets = getChildrenWidgets();
    for (int index = 0; index < widgets.size(); index++) {
      WidgetInfo widget = widgets.get(index);
      Object headerWidget = ReflectionUtils.getFieldObject(layoutDataList.get(index), "header");
      Object headerElement = state.getUIObjectUtils().getElement(headerWidget);
      // prepare bounds
      Rectangle headerBounds = state.getAbsoluteBounds(headerElement);
      absoluteToRelative(headerBounds);
      // add handle object
      m_widgetHandles.add(new WidgetHandle(widget));
      m_widgetToHandleBounds.put(widget, headerBounds);
    }
  }
}
