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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;
import org.eclipse.wb.internal.core.model.layout.absolute.OrderingSupport;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.model.util.AbsoluteLayoutCreationFlowSupport;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for GWT <code>AbsolutePanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public class AbsolutePanelInfo extends ComplexPanelInfo implements IAbsolutePanelInfo<WidgetInfo> {
  private final AbsolutePanelInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsolutePanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    contributeWidgetContextMenu();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  private void contributeWidgetContextMenu() {
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object instanceof WidgetInfo && object.getParent() == m_this) {
          WidgetInfo component = (WidgetInfo) object;
          contributeWidgetContextMenu(manager, component);
        }
      }
    });
  }

  /**
   * Contributes {@link Action}'s into {@link WidgetInfo} context menu.
   */
  private void contributeWidgetContextMenu(IMenuManager manager, final WidgetInfo widget) {
    // order
    {
      List<WidgetInfo> widgets = getChildrenWidgets();
      new OrderingSupport(widgets, widget).contributeActions(manager);
    }
    // auto-size
    {
      IAction action =
          new ObjectInfoAction(widget, "Autosize widget",
              DesignerPlugin.getImageDescriptor("info/layout/absolute/fit_to_size.png")) {
            @Override
            protected void runEx() throws Exception {
              widget.getSizeSupport().setSize(null);
            }
          };
      manager.appendToGroup(IContextMenuConstants.GROUP_CONSTRAINTS, action);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_BOUNDS(WidgetInfo widget, Point location, Dimension size) throws Exception {
    Assert.isTrue(getChildren().contains(widget), "%s is not child of %s.", widget, this);
    if (size != null) {
      widget.getSizeSupport().setSize(size);
    }
    if (location != null) {
      setLocation(widget, location);
    }
    // check creation flow
    if (location != null
        && (widget.getModelBounds() != null || EnvironmentUtils.isTestingTime())
        && useCreationFlow()) {
      AbsoluteLayoutCreationFlowSupport.apply(this, getChildrenWidgets(), widget, location, size);
    }
  }

  /**
   * Modifies location of {@link WidgetInfo}.
   * 
   * @param location
   *          new location, not <code>null</code>.
   */
  private void setLocation(WidgetInfo widget, Point location) throws Exception {
    Assert.isNotNull(location);
    String xString = IntegerConverter.INSTANCE.toJavaSource(this, location.x);
    String yString = IntegerConverter.INSTANCE.toJavaSource(this, location.y);
    String xyString = ", " + xString + ", " + yString;
    boolean locationSet = false;
    boolean force_setWidgetPosition =
        JavaInfoUtils.hasTrueParameter(widget, "GWT.AbsolutePanel: force setWidgetPosition");
    // add(%widget%,int,int)
    for (MethodInvocation invocation : getInvocations("add(%widget%,int,int)", widget)) {
      setExpression(invocation, 1, xString);
      setExpression(invocation, 2, yString);
      locationSet = true;
    }
    // setWidgetLocation(%widget%,int,int)
    for (MethodInvocation invocation : getInvocations("setWidgetPosition(%widget%,int,int)", widget)) {
      setExpression(invocation, 1, xString);
      setExpression(invocation, 2, yString);
      locationSet = true;
    }
    // if no location set yet, try to update add()
    if (!locationSet && !force_setWidgetPosition) {
      for (MethodInvocation invocation : getInvocations("add(%widget%)", widget)) {
        AstEditor editor = getEditor();
        String argsSource = editor.getSource(DomGenerics.arguments(invocation).get(0));
        argsSource += xyString;
        editor.replaceInvocationArguments(invocation, ImmutableList.of(argsSource));
        locationSet = true;
        // related nodes
        addRelatedNodes(invocation);
        widget.addRelatedNodes(invocation);
      }
    }
    // if no location set yet, add setWidgetPosition()
    if (!locationSet) {
      // prepare target (after association)
      StatementTarget target;
      {
        Statement associationStatement = widget.getAssociation().getStatement();
        target = new StatementTarget(associationStatement, false);
      }
      // add invocation
      String source = TemplateUtils.format("{0}.setWidgetPosition({1}{2})", this, widget, xyString);
      Expression expression = widget.addExpressionStatement(target, source);
      addRelatedNodes(expression);
    }
  }

  private boolean useCreationFlow() {
    return getToolkit().getPreferences().getBoolean(IPreferenceConstants.P_CREATION_FLOW);
  }

  private ToolkitDescription getToolkit() {
    return getDescription().getToolkit();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link MethodInvocation}'s of this {@link JavaInfo}, where one of the arguments is
   *         given {@link WidgetInfo}.
   * 
   * @param signature
   *          the signature of method, with <code>%widget%</code> at place where {@link WidgetInfo}
   *          argument is expected.
   */
  private List<MethodInvocation> getInvocations(String signature, WidgetInfo widget) {
    // analyze/update signature
    int widgetIndex;
    {
      int widgetPatternIndex = signature.indexOf("%widget%");
      Assert.isTrue(widgetPatternIndex != -1, "No %%widget%% in %s.", signature);
      widgetIndex = StringUtils.countMatches(signature.substring(0, widgetPatternIndex), ",");
      signature =
          StringUtils.replace(signature, "%widget%", "com.google.gwt.user.client.ui.Widget");
    }
    // filter MethodInvocation's
    List<MethodInvocation> invocations = Lists.newArrayList();
    for (MethodInvocation invocation : getMethodInvocations(signature)) {
      Expression widgetExpression = DomGenerics.arguments(invocation).get(widgetIndex);
      if (widget.isRepresentedBy(widgetExpression)) {
        invocations.add(invocation);
      }
    }
    return invocations;
  }

  /**
   * Set the argument of {@link MethodInvocation} to given source.
   */
  private void setExpression(MethodInvocation invocation, int index, String arg) throws Exception {
    getEditor().replaceExpression(DomGenerics.arguments(invocation).get(index), arg);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
    final Rectangle modelBounds = widget.getModelBounds();
    commands.add(new PanelClipboardCommand<AbsolutePanelInfo>(widget) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(AbsolutePanelInfo panel, WidgetInfo widget) throws Exception {
        panel.command_CREATE2(widget, null);
        panel.command_BOUNDS(widget, modelBounds.getLocation(), null);
      }
    });
  }
}
