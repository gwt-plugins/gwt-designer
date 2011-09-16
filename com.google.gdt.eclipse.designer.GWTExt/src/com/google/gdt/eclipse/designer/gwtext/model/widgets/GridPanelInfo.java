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
package com.google.gdt.eclipse.designer.gwtext.model.widgets;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.ArrayAssociation;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.broadcast.JavaInfoTreeAlmostComplete;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddAfter;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddBefore;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import java.util.List;

/**
 * Model for <code>GridPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model
 */
public final class GridPanelInfo extends ContainerInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addBroadcastListener(new JavaInfoTreeAlmostComplete() {
      public void invoke(JavaInfo root, List<JavaInfo> components) throws Exception {
        bindColumns(components);
      }
    });
    ensureModels();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Required models
  //
  ////////////////////////////////////////////////////////////////////////////
  private void ensureModels() throws Exception {
    new ComponentConfiguratorBeforeAssociation(this) {
      @Override
      protected void configure() throws Exception {
        JavaInfoUtils.executeScript(GridPanelInfo.this, CodeUtils.getSource(
            "import com.gwtext.client.widgets.grid.*;",
            "if (object.getStore() == null) {",
            "  object.setStore(new com.gwtext.client.data.SimpleStore('field', {}));",
            "}",
            "if (model.getColumns().isEmpty()) {",
            "  columns = new ColumnConfig[] {",
            "    new ColumnConfig('First', 'f1', 100),",
            "    new ColumnConfig('Second', 'f2', 200),",
            "  };",
            "  object.setColumnModel(new ColumnModel(columns))",
            "}",
            ""));
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    List<ColumnConfigInfo> columns = getColumns();
    for (int i = 0; i < columns.size(); i++) {
      ColumnConfigInfo column = columns.get(i);
      Object columnElement =
          ScriptUtils.evaluate(
              "grid.getView().getHeaderCell(index)",
              "grid",
              getObject(),
              "index",
              i);
      Rectangle bounds = getState().getAbsoluteBounds(columnElement);
      absoluteToRelative(bounds);
      column.setBounds(bounds);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Columns
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link List} of {@link ColumnConfigInfo} children.
   */
  public List<ColumnConfigInfo> getColumns() {
    return getChildren(ColumnConfigInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Binds (on parse) 
  //
  ////////////////////////////////////////////////////////////////////////////
  private void bindColumns(List<JavaInfo> javaInfoList) throws Exception {
    ArrayCreation columnsArray = getColumnsArray(false);
    if (columnsArray == null) {
      return;
    }
    @SuppressWarnings("unchecked")
    List<Expression> arrayExpressions = columnsArray.getInitializer().expressions();
    // check ColumnConfig-s
    for (JavaInfo javaInfo : javaInfoList) {
      if (javaInfo instanceof ColumnConfigInfo && javaInfo.getParent() == null) {
        ColumnConfigInfo column = (ColumnConfigInfo) javaInfo;
        for (Expression arrayExpression : arrayExpressions) {
          if (column.isRepresentedBy(arrayExpression)) {
            addChild(column);
            column.setAssociation(new ArrayAssociation(columnsArray));
          }
        }
      }
    }
  }

  /**
   * @return the {@link ArrayCreation} of columns in "ColumnModel".
   */
  private ArrayCreation getColumnsArray(boolean ensure) throws Exception {
    ClassInstanceCreation columnModelCreation = getColumnModelCreation(ensure);
    if (columnModelCreation == null) {
      return null;
    }
    // "columns" Array usage in "ColumnModel" creation
    Expression columnArray = DomGenerics.arguments(columnModelCreation).get(0);
    ExecutionFlowDescription flowDescription = JavaInfoUtils.getState(this).getFlowDescription();
    while (true) {
      if (columnArray instanceof ArrayCreation) {
        return (ArrayCreation) columnArray;
      }
      if (AstNodeUtils.isVariable(columnArray)) {
        ASTNode lastAssignment = ExecutionFlowUtils.getLastAssignment(flowDescription, columnArray);
        if (lastAssignment instanceof VariableDeclaration) {
          VariableDeclaration variableDeclaration = (VariableDeclaration) lastAssignment;
          columnArray = variableDeclaration.getInitializer();
          continue;
        }
      }
      if (ensure) {
        return (ArrayCreation) getEditor().replaceExpression(
            columnArray,
            "new com.gwtext.client.widgets.grid.ColumnConfig[] {}");
      }
      return null;
    }
  }

  /**
   * @return the {@link ClassInstanceCreation} for "ColumnModel" of this grid.
   */
  private ClassInstanceCreation getColumnModelCreation(boolean ensure) throws Exception {
    // process constructor
    if (getCreationSupport() instanceof ConstructorCreationSupport) {
      ASTNode gridCreation = getCreationSupport().getNode();
      ClassInstanceCreation columnModelCreation = getColumnModelCreation0(gridCreation, ensure);
      if (columnModelCreation != null) {
        return columnModelCreation;
      }
    }
    // process setColumnModel invocation
    MethodInvocation setColumnModelInvocation =
        getMethodInvocation("setColumnModel(com.gwtext.client.widgets.grid.ColumnModel)");
    if (setColumnModelInvocation != null) {
      ClassInstanceCreation columnModelCreation =
          getColumnModelCreation0(setColumnModelInvocation, ensure);
      if (columnModelCreation != null) {
        return columnModelCreation;
      }
    } else {
      // add setColumnModel invocation
      if (ensure) {
        String noColumns = "(com.gwtext.client.widgets.grid.ColumnConfig[]) null";
        String newColumnModel = "new com.gwtext.client.widgets.grid.ColumnModel(" + noColumns + ")";
        setColumnModelInvocation =
            addMethodInvocation(
                "setColumnModel(com.gwtext.client.widgets.grid.ColumnModel)",
                newColumnModel);
        return (ClassInstanceCreation) DomGenerics.arguments(setColumnModelInvocation).get(0);
      }
    }
    return null;
  }

  private ClassInstanceCreation getColumnModelCreation0(ASTNode node, boolean ensure)
      throws Exception {
    List<Expression> arguments = DomGenerics.arguments(node);
    for (Expression argument : arguments) {
      if (AstNodeUtils.isSuccessorOf(argument, "com.gwtext.client.widgets.grid.ColumnModel")) {
        ExecutionFlowDescription flowDescription =
            JavaInfoUtils.getState(this).getFlowDescription();
        while (true) {
          if (argument instanceof ClassInstanceCreation) {
            return (ClassInstanceCreation) argument;
          }
          if (AstNodeUtils.isVariable(argument)) {
            ASTNode lastAssignment =
                ExecutionFlowUtils.getLastAssignment(flowDescription, argument);
            if (lastAssignment instanceof VariableDeclaration) {
              VariableDeclaration variableDeclaration = (VariableDeclaration) lastAssignment;
              argument = variableDeclaration.getInitializer();
              continue;
            }
          }
          if (ensure) {
            return (ClassInstanceCreation) getEditor().replaceExpression(
                argument,
                "new com.gwtext.client.widgets.grid.ColumnModel(new com.gwtext.client.widgets.grid.ColumnConfig[] {})");
          }
          return null;
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link ColumnConfigInfo}.
   */
  public void command_CREATE(ColumnConfigInfo column, ColumnConfigInfo nextColumn) throws Exception {
    ArrayCreation columnsArray = getColumnsArray(true);
    Assert.isNotNull(columnsArray);
    ArrayInitializer arrayInitializer = columnsArray.getInitializer();
    // fire before event
    getBroadcast(ObjectInfoChildAddBefore.class).invoke(this, column, new ObjectInfo[]{nextColumn});
    getBroadcastJava().addBefore(this, column);
    // setup hierarchy
    int index =
        nextColumn == null ? arrayInitializer.expressions().size() : getColumns().indexOf(
            nextColumn);
    addChild(column, nextColumn);
    // add source
    StatementTarget statementTarget =
        new StatementTarget(AstNodeUtils.getEnclosingStatement(columnsArray), true);
    String source = column.getCreationSupport().add_getSource(new NodeTarget(statementTarget));
    getCreateItemExpression(column, arrayInitializer, index, source);
    // set association
    column.setAssociation(new ArrayAssociation(columnsArray));
    // fire after event
    getBroadcastJava().addAfter(this, column);
    getBroadcast(ObjectInfoChildAddAfter.class).invoke(this, column);
  }

  private Expression getCreateItemExpression(ColumnConfigInfo column,
      ArrayInitializer arrayInitializer,
      int index,
      String source) throws Exception {
    Expression arrayElement = getEditor().addArrayElement(arrayInitializer, index, source);
    column.addRelatedNode(arrayElement);
    // set variable support
    if (column.getVariableSupport() == null) {
      column.setVariableSupport(new EmptyVariableSupport(column, arrayElement));
    }
    // set source creation
    if (arrayElement instanceof ClassInstanceCreation) {
      column.getCreationSupport().add_setSourceExpression(arrayElement);
    }
    return arrayElement;
  }

  /**
   * Moves existing {@link ColumnConfigInfo}.
   */
  public void command_MOVE(ColumnConfigInfo column, ColumnConfigInfo nextColumn) throws Exception {
    ArrayCreation columnsArray = getColumnsArray(true);
    Assert.isNotNull(columnsArray);
    ArrayInitializer arrayInitializer = columnsArray.getInitializer();
    JavaInfo oldParent = column.getParent() instanceof JavaInfo ? column.getParentJava() : null;
    int oldIndex = column.getParent().getChildren(ColumnConfigInfo.class).indexOf(column);
    int newIndex = getChildren(ColumnConfigInfo.class).indexOf(nextColumn);
    newIndex = newIndex == -1 ? arrayInitializer.expressions().size() : newIndex;
    // fire before event
    getBroadcastObject().childMoveBefore(getParent(), column, nextColumn);
    getBroadcastJava().moveBefore(column, oldParent, this);
    // move hierarchy
    if (column.getParent() == this && column.getAssociation() instanceof ArrayAssociation) {
      // move inside this grid
      moveChild(column, nextColumn);
      // exchange elements
      getEditor().moveArrayElement(arrayInitializer, arrayInitializer, oldIndex, newIndex);
    } else {
      // try optimize source
      if (column.getVariableSupport() instanceof LocalUniqueVariableSupport) {
        LocalUniqueVariableSupport localVariableSupport =
            (LocalUniqueVariableSupport) column.getVariableSupport();
        if (localVariableSupport.canInline()) {
          localVariableSupport.inline();
        }
      }
      // source
      String source = null;
      if (column.getVariableSupport() instanceof EmptyVariableSupport) {
        source =
            getEditor().getSource(
                ((EmptyVariableSupport) column.getVariableSupport()).getInitializer());
      }
      // remove from old place
      Association association = column.getAssociation();
      if (association != null) {
        if (association.remove()) {
          column.setAssociation(null);
        }
      }
      column.getParent().removeChild(column);
      // add to array
      addChild(column, nextColumn);
      if (!(column.getVariableSupport() instanceof EmptyVariableSupport)) {
        StatementTarget statementTarget =
            new StatementTarget(AstNodeUtils.getEnclosingStatement(columnsArray), true);
        column.getVariableSupport().ensureInstanceReadyAt(statementTarget);
        source =
            column.getVariableSupport().getReferenceExpression(new NodeTarget(statementTarget));
      }
      Assert.isNotNull(source, "No source found for.");
      getCreateItemExpression(column, arrayInitializer, newIndex, source);
    }
    // set association
    column.setAssociation(new ArrayAssociation(columnsArray));
    // fire after event
    getBroadcastJava().moveAfter(column, oldParent, this);
    getBroadcastObject().childMoveAfter(getParent(), column, nextColumn, oldIndex, newIndex);
  }
}
