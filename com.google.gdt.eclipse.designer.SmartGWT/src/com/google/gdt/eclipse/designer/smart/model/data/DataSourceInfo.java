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
package com.google.gdt.eclipse.designer.smart.model.data;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.widgets.UIObjectInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.model.widgets.support.IGwtStateProvider;
import com.google.gdt.eclipse.designer.smart.model.CanvasAfterAttach;
import com.google.gdt.eclipse.designer.smart.model.support.SmartClientUtils;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanInfo;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupportUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.data.DataSource</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class DataSourceInfo extends AbstractComponentInfo implements IGwtStateProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DataSourceInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    installListeners();
    // attach as NVO
    new NonVisualBeanInfo() {
      @Override
      public void moveLocation(Point moveDelta) throws Exception {
      }
    }.setJavaInfo(this);
  }

  private void installListeners() {
    // add root attach listener
    addBroadcastListener(new CanvasAfterAttach() {
      public void invoke() throws Exception {
        processObjectReady();
      }
    });
    // child before pseudo-association
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void target_isTerminalStatement(JavaInfo parent,
          JavaInfo child,
          Statement statement,
          boolean[] terminal) {
        // check for any invocation with this DataSource as argument
        if (parent == DataSourceInfo.this && statement instanceof ExpressionStatement) {
          Expression expression = ((ExpressionStatement) statement).getExpression();
          if (expression instanceof MethodInvocation) {
            MethodInvocation invocation = (MethodInvocation) expression;
            for (Expression argument : DomGenerics.arguments(invocation)) {
              if (isRepresentedBy(argument)) {
                terminal[0] = true;
                return;
              }
            }
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<DataSourceFieldInfo> getFields() {
    return getChildren(DataSourceFieldInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGWTStateProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public GwtState getState() {
    return (GwtState) getEditor().getGlobalValue(UIObjectInfo.STATE_KEY);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setObject(Object object) throws Exception {
    super.setObject(object);
    // tweaks for correct render...
    if (object != null && getFields().size() == 0 && !isPlaceholder()) {
      // at least one field required for some parent widgets
      ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
      Class<?> clazz =
          classLoader.loadClass("com.smartgwt.client.data.fields.DataSourceIntegerField");
      Object field = clazz.newInstance();
      ReflectionUtils.invokeMethod(
          object,
          "addField(com.smartgwt.client.data.DataSourceField)",
          field);
    }
  }

  @Override
  public void refresh_dispose() throws Exception {
    // remember Object & State
    Object object = getObject();
    GwtState state = getState();
    // dispose children
    super.refresh_dispose();
    // destroy object after all disposed
    if (object != null && !isPlaceholder() && state != null && !state.isDisposed()) {
      SmartClientUtils.destroyDataSource(object);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepareForAssignTo(JavaInfo component) throws Exception {
    // ensure that value component is accessible
    NodeTarget nodeTarget = JavaInfoUtils.getNodeTarget_afterCreation(component);
    if (JavaInfoUtils.isCreatedAtTarget(this, nodeTarget)) {
      return;
    }
    // try convert to "lazy"
    if (LazyVariableSupportUtils.canConvert(this)) {
      LazyVariableSupportUtils.convert(this);
      return;
    }
    // fail, can't be assigned
    throw new IllegalStateException("Selected argument can't be assigned (typically, it is not accessable at "
        + nodeTarget
        + ").");
  }

  public StatementTarget calculateStatementTarget(JavaInfo component, List<ASTNode> afterNodes)
      throws Exception {
    // prepare
    prepareForAssignTo(component);
    //
    LazyVariableSupport lazyVariableSupport = null;
    if (getVariableSupport() instanceof LazyVariableSupport) {
      lazyVariableSupport = (LazyVariableSupport) getVariableSupport();
    }
    // collect nodes
    List<ASTNode> nodes = Lists.newArrayList(afterNodes);
    for (ASTNode node : getRelatedNodes()) {
      if (lazyVariableSupport != null
          && lazyVariableSupport.m_accessor.equals(AstNodeUtils.getEnclosingMethod(node))) {
        // skip nodes in "lazy" method
        continue;
      }
      // all "dataSource.setX()" invocations must be before
      ASTNode parentNode = node.getParent();
      if (parentNode instanceof MethodInvocation) {
        MethodInvocation methodInvocation = (MethodInvocation) parentNode;
        if (isRepresentedBy(methodInvocation.getExpression())
            && methodInvocation.getName().getIdentifier().startsWith("set")) {
          nodes.add(node);
          continue;
        }
      }
      // all "dataSource.x = X" fields assignment must be before
      if (parentNode instanceof FieldAccess) {
        FieldAccess fieldAccess = (FieldAccess) parentNode;
        if (isRepresentedBy(fieldAccess.getExpression())
            && fieldAccess.getParent() instanceof Assignment) {
          nodes.add(node);
          continue;
        }
      }
    }
    // process nodes
    if (!nodes.isEmpty()) {
      nodes.add(getCreationSupport().getNode());
      nodes.add(component.getCreationSupport().getNode());
      // sort nodes
      ExecutionFlowDescription flowDescription =
          JavaInfoUtils.getState(getRootJava()).getFlowDescription();
      JavaInfoUtils.sortNodesByFlow(flowDescription, false, nodes);
      ASTNode targetNode = nodes.get(nodes.size() - 1);
      return new StatementTarget(targetNode, false);
    }
    return null;
  }

  public StatementTarget calculateStatementTarget(JavaInfo component) throws Exception {
    return calculateStatementTarget(component, Lists.<ASTNode>newArrayList());
  }
}
