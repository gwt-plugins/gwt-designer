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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.InvocationChildEllipsisAssociation;
import org.eclipse.wb.core.model.broadcast.JavaInfoTreeAlmostComplete;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.nonvisual.EllipsisObjectInfo;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import java.text.MessageFormat;
import java.util.List;

/**
 * Model for <code>Grid</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public class GridInfo extends BoxComponentInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    m_columnBinder = createColumnAsListBinder();
    // schedule columns binding
    addBroadcastListener(new JavaInfoTreeAlmostComplete() {
      public void invoke(JavaInfo root, List<JavaInfo> components) throws Exception {
        bindColumns(components);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setObject(Object object) throws Exception {
    super.setObject(object);
    // ignore if placeholder
    if (isPlaceholder()) {
      return;
    }
    // force rendering
    JavaInfoUtils.executeScript(this, "object.setLazyRowRender(0);");
    // bind ColumnConfig models
    if (m_columnBinder != null) {
      m_columnBinder.bindColumnObjects();
    }
  }

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
  private abstract class ColumnsBinder {
    abstract void bindColumnObjects() throws Exception;

    abstract void create(ColumnConfigInfo column, ColumnConfigInfo nextColumn) throws Exception;

    abstract void move(ColumnConfigInfo column, ColumnConfigInfo nextColumn) throws Exception;
  };

  private ColumnsBinder m_columnBinder;

  /**
   * @return the {@link List} of {@link ColumnConfigInfo} children.
   */
  public List<ColumnConfigInfo> getColumns() {
    return getChildren(ColumnConfigInfo.class);
  }

  /**
   * @return the {@link ClassInstanceCreation} for "ColumnModel" of this grid.
   */
  private ClassInstanceCreation getColumnModelCreation(boolean ensure) throws Exception {
    if (!(getCreationSupport() instanceof ConstructorCreationSupport)) {
      return null;
    }
    ASTNode gridCreation = getCreationSupport().getNode();
    List<Expression> arguments = DomGenerics.arguments(gridCreation);
    for (Expression argument : arguments) {
      if (AstNodeUtils.isSuccessorOf(argument, "com.extjs.gxt.ui.client.widget.grid.ColumnModel")) {
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
                "new com.extjs.gxt.ui.client.widget.grid.ColumnModel(null)");
          }
          return null;
        }
      }
    }
    return null;
  }

  /**
   * Binds {@link ColumnConfigInfo}-s to this grid.
   */
  private void bindColumns(List<JavaInfo> javaInfoList) throws Exception {
    ClassInstanceCreation columnModelCreation = getColumnModelCreation(false);
    if (columnModelCreation != null) {
      // exists ColumnModel creation
      List<Expression> columnModelArguments = DomGenerics.arguments(columnModelCreation);
      if (columnModelArguments.size() == 1) {
        Expression columnsList = columnModelArguments.get(0);
        // process supported association models...
        if (columnsList instanceof SimpleName) {
          // simple List variable
          bindColumnsAsList((SimpleName) columnsList, javaInfoList);
        } else if (columnsList instanceof MethodInvocation) {
          MethodInvocation invocation = (MethodInvocation) columnsList;
          IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(invocation);
          if (methodBinding != null) {
            String methodQualifiedSignature =
                methodBinding.getDeclaringClass().getQualifiedName()
                    + "."
                    + AstNodeUtils.getMethodSignature(methodBinding);
            // inline Array to List conversion
            if ("java.util.Arrays.asList(com.extjs.gxt.ui.client.widget.grid.ColumnConfig[])".equals(methodQualifiedSignature)) {
              bindColumnsAsArray(invocation, javaInfoList);
            }
          }
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Columns: List
  //
  ////////////////////////////////////////////////////////////////////////////
  private void bindColumnsAsList(SimpleName columnsList, List<JavaInfo> javaInfoList)
      throws Exception {
    // check ColumnConfig-s
    for (JavaInfo javaInfo : javaInfoList) {
      if (javaInfo instanceof ColumnConfigInfo && javaInfo.getParent() == null) {
        ColumnConfigInfo column = (ColumnConfigInfo) javaInfo;
        MethodInvocation invocation = getInvocationOfAssociationWithThisGrid(columnsList, column);
        if (invocation != null) {
          addChild(column);
          column.setAssociation(new ColumnConfigAssociation(columnsList, invocation));
        }
      }
    }
  }

  /**
   * @return {@link MethodInvocation} used for associating given "column" with {@link List} of model
   *         columns.
   */
  private MethodInvocation getInvocationOfAssociationWithThisGrid(SimpleName columnsList,
      ColumnConfigInfo column) {
    for (ASTNode node : column.getRelatedNodes()) {
      if (node.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
        MethodInvocation invocation = (MethodInvocation) node.getParent();
        if (invocation.getName().getIdentifier().equals("add")
            && isSameColumnsList(columnsList, invocation.getExpression())) {
          return invocation;
        }
      }
    }
    return null;
  }

  /**
   * @return <code>true</code> if "actual" and "possible" represent same {@link List} of columns.
   */
  private boolean isSameColumnsList(SimpleName columnsList, Expression possibleExpression) {
    // "add" should be into variable
    if (!(possibleExpression instanceof SimpleName)) {
      return false;
    }
    // variable should be same as "columnsList"
    SimpleName possibleName = (SimpleName) possibleExpression;
    if (!columnsList.getIdentifier().equals(possibleName.getIdentifier())) {
      return false;
    }
    // there should be no re-assignment of "columnsList"
    ExecutionFlowDescription flowDescription = JavaInfoUtils.getState(this).getFlowDescription();
    ASTNode columnsAssignment = ExecutionFlowUtils.getLastAssignment(flowDescription, columnsList);
    ASTNode possibleAssignment =
        ExecutionFlowUtils.getLastAssignment(flowDescription, possibleName);
    return columnsAssignment == possibleAssignment;
  }

  private ColumnsBinder createColumnAsListBinder() {
    return new ColumnsBinder() {
      @Override
      void bindColumnObjects() throws Exception {
        // Adds <code>ColumnConfig</code> objects into <code>ColumnModel</code>.
        Object grid = getObject();
        Object model = ReflectionUtils.getFieldObject(grid, "cm");
        Object modelColumns = ReflectionUtils.getFieldObject(model, "configs");
        for (ColumnConfigInfo column : getColumns()) {
          ReflectionUtils.invokeMethod(modelColumns, "add(java.lang.Object)", column.getObject());
        }
      }

      @Override
      void create(ColumnConfigInfo column, ColumnConfigInfo nextColumn) throws Exception {
        SimpleName columnsList = ensureColumnsList();
        ColumnConfigAssociation association = new ColumnConfigAssociation(columnsList);
        AssociationObject associationObject = new AssociationObject(association, true);
        if (nextColumn == null) {
          Statement columnsListUsageStatement = AstNodeUtils.getEnclosingStatement(columnsList);
          StatementTarget target = new StatementTarget(columnsListUsageStatement, true);
          JavaInfoUtils.addTarget(column, associationObject, GridInfo.this, target);
        } else {
          JavaInfoUtils.add(column, associationObject, GridInfo.this, nextColumn);
        }
      }

      @Override
      void move(ColumnConfigInfo column, ColumnConfigInfo nextColumn) throws Exception {
        SimpleName columnsList = ensureColumnsList();
        ColumnConfigAssociation association = new ColumnConfigAssociation(columnsList);
        AssociationObject associationObject = new AssociationObject(association, true);
        if (nextColumn == null) {
          Statement columnsListUsageStatement = AstNodeUtils.getEnclosingStatement(columnsList);
          StatementTarget target = new StatementTarget(columnsListUsageStatement, true);
          JavaInfoUtils.moveTarget(column, associationObject, GridInfo.this, null, target);
        } else {
          JavaInfoUtils.move(column, associationObject, GridInfo.this, nextColumn);
        }
      }

      SimpleName ensureColumnsList() throws Exception {
        ClassInstanceCreation columnModelCreation = getColumnModelCreation(true);
        // "columns" List usage in "ColumnModel" creation
        List<Expression> columnModelArguments = DomGenerics.arguments(columnModelCreation);
        Expression columnsList = columnModelArguments.get(0);
        if (columnsList instanceof SimpleName) {
          return (SimpleName) columnsList;
        }
        // if no columns then generate new ArrayList
        Statement columnModelStatement = AstNodeUtils.getEnclosingStatement(columnModelCreation);
        StatementTarget target = new StatementTarget(columnModelStatement, true);
        String configsName =
            getEditor().getUniqueVariableName(
                columnModelStatement.getStartPosition(),
                "configs",
                null);
        getEditor().addStatement(
            MessageFormat.format(
                "java.util.List<{0}> {1} = new java.util.ArrayList<{0}>();",
                "com.extjs.gxt.ui.client.widget.grid.ColumnConfig",
                configsName),
            target);
        return (SimpleName) getEditor().replaceExpression(columnsList, configsName);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Columns: Arrays.asList
  //
  ////////////////////////////////////////////////////////////////////////////
  private void bindColumnsAsArray(MethodInvocation invocation, List<JavaInfo> javaInfoList)
      throws Exception {
    List<Expression> asListArguments = DomGenerics.arguments(invocation);
    // create info
    final EllipsisObjectInfo arrayInfo =
        new EllipsisObjectInfo(getEditor(),
            "Arrays.asList",
            EditorState.get(getEditor()).getEditorLoader().loadClass(
                "com.extjs.gxt.ui.client.widget.grid.ColumnConfig"),
            invocation,
            0);
    addChild(arrayInfo);
    arrayInfo.setRemoveOnEmpty(false);
    arrayInfo.setHideInTree(true);
    arrayInfo.setOnEmptySource("java.util.Collections.<com.extjs.gxt.ui.client.widget.grid.ColumnConfig>emptyList()");
    // process infos
    for (Expression asListArgument : asListArguments) {
      for (JavaInfo javaInfo : javaInfoList) {
        if (javaInfo instanceof ColumnConfigInfo && javaInfo.getParent() == null) {
          ColumnConfigInfo column = (ColumnConfigInfo) javaInfo;
          if (column.isRepresentedBy(asListArgument)) {
            addChild(column);
            arrayInfo.addItem(column);
            column.setAssociation(new InvocationChildEllipsisAssociation(invocation, arrayInfo));
          }
        }
      }
    }
    m_columnBinder = new ColumnsBinder() {
      @Override
      void bindColumnObjects() {
        // do nothing
      }

      @Override
      void create(ColumnConfigInfo column, ColumnConfigInfo nextColumn) throws Exception {
        ensureColumnsAsArray();
        arrayInfo.command_CREATE(column, nextColumn);
      }

      @Override
      void move(ColumnConfigInfo column, ColumnConfigInfo nextColumn) throws Exception {
        ensureColumnsAsArray();
        arrayInfo.command_MOVE(column, nextColumn);
      }

      void ensureColumnsAsArray() throws Exception {
        ClassInstanceCreation columnModelCreation = getColumnModelCreation(true);
        // "columns" List usage in "ColumnModel" creation
        List<Expression> columnModelArguments = DomGenerics.arguments(columnModelCreation);
        Expression columnsList = columnModelArguments.get(0);
        if (columnsList instanceof MethodInvocation) {
          MethodInvocation invocation = (MethodInvocation) columnsList;
          if (invocation == arrayInfo.getInvocation()) {
            return;
          }
        }
        // if no columns then generate new Array.asList()
        MethodInvocation invocation =
            (MethodInvocation) getEditor().replaceExpression(
                columnsList,
                "java.util.Arrays.asList()");
        arrayInfo.setInvocation(invocation);
      }
    };
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
    m_columnBinder.create(column, nextColumn);
  }

  /**
   * Moves existing {@link ColumnConfigInfo}.
   */
  public void command_MOVE(ColumnConfigInfo column, ColumnConfigInfo nextColumn) throws Exception {
    m_columnBinder.move(column, nextColumn);
  }
}
