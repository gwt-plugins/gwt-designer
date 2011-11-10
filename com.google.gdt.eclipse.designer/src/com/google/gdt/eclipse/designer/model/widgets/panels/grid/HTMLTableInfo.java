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
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.PanelClipboardCommand;
import com.google.gdt.eclipse.designer.model.widgets.support.DOMUtils;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ExposedPropertyCreationSupport;
import org.eclipse.wb.internal.core.model.description.AbstractInvocationDescription;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.NotImplementedException;

import java.util.List;
import java.util.Map;

/**
 * Model for GWT <code>HTMLTable</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public abstract class HTMLTableInfo extends ComplexPanelInfo {
  private JavaInfo m_rowFormatter;
  private JavaInfo m_cellFormatter;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public HTMLTableInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    cleanUpAfterChildRemove();
    addContextMenuActions();
    new CellPropertySupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Image} for {@link HTMLTableInfo}.
   */
  public static Image getImage(String name) {
    return Activator.getImage("info/HTMLTable/" + name);
  }

  /**
   * @return the {@link ImageDescriptor} for {@link HTMLTableInfo}.
   */
  public static ImageDescriptor getImageDescriptor(String name) {
    return Activator.getImageDescriptor("info/HTMLTable/" + name);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link WidgetInfo} is removed from {@link HTMLTableInfo} we should remove its
   * {@link CellConstraintsSupport} and empty trailing columns/rows.
   */
  private void cleanUpAfterChildRemove() {
    addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (child instanceof WidgetInfo && parent == HTMLTableInfo.this) {
          cleanUpWidget(child);
        }
      }
    });
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
          throws Exception {
        if (child instanceof WidgetInfo
            && oldParent == HTMLTableInfo.this
            && newParent != oldParent) {
          cleanUpWidget(child);
        }
      }
    });
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void childRemoveAfter(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (child instanceof WidgetInfo && parent == HTMLTableInfo.this) {
          // remove setX() invocations for widget cell
          {
            CellConstraintsSupport constraints = m_constraints.get(child);
            removeCellInvocations(new Rectangle(constraints.getX(), constraints.getY(), 1, 1));
          }
          // we don't need constraints anymore
          m_constraints.remove(child);
          // delete trailing dimensions
          deleteTrailingEmptyColumns();
          deleteTrailingEmptyRows();
        }
      }
    });
  }

  private void cleanUpWidget(ObjectInfo child) throws Exception {
    WidgetInfo widget = (WidgetInfo) child;
    setComponentColSpan(widget, 1);
    setComponentRowSpan(widget, 1);
  }

  /**
   * Adds {@link HTMLTableInfo} actions into context menu.
   */
  private void addContextMenuActions() {
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object instanceof WidgetInfo && object.getParent() == HTMLTableInfo.this) {
          WidgetInfo component = (WidgetInfo) object;
          CellConstraintsSupport support = getConstraints(component);
          support.addContextMenu(manager);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exposed objects
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createExposedChildren() throws Exception {
    super.createExposedChildren();
    if (m_rowFormatter == null) {
      m_rowFormatter = JavaInfoUtils.addChildExposedByMethod(this, "getRowFormatter");
    }
    JavaInfoUtils.addChildExposedByMethod(this, "getColumnFormatter");
    if (m_cellFormatter == null) {
      m_cellFormatter = JavaInfoUtils.addChildExposedByMethod(this, "getCellFormatter");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    m_gridInfo = null;
    super.refresh_dispose();
  }

  @Override
  protected void refresh_afterCreate0() throws Exception {
    if (!isPlaceholder()) {
      initializeTableStatus();
    }
    super.refresh_afterCreate0();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    // ask constraints for all components now,
    // to avoid their lazy initialization later (may be at time when we temporary inconsistent)
    if (!isPlaceholder()) {
      for (WidgetInfo component : getChildrenWidgets()) {
        getConstraints(component);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the <code>rowFormatter</code> exposed object.
   */
  JavaInfo getRowFormatter() {
    return m_rowFormatter;
  }

  /**
   * @return the <code>cellFormatter</code> exposed object.
   */
  JavaInfo getCellFormatter() {
    return m_cellFormatter;
  }

  /**
   * @return the {@link HTMLTableStatus} to access/update low-level information.
   */
  public HTMLTableStatus getStatus() {
    return m_status;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Invocations visitor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Visits <code>setX(row, cell, value)</code> invocations.
   */
  interface CellInvocationsVisitor {
    void visit(MethodDescription methodDescription,
        MethodInvocation invocation,
        Expression rowArgument,
        Expression cellArgument,
        int row,
        int cell) throws Exception;
  }

  /**
   * Visits <code>setX(row, cell, value)</code> invocations of given this {@link HTMLTableInfo} and
   * its formatters.
   */
  protected final void visitCellInvocations(CellInvocationsVisitor visitor) throws Exception {
    visitCellInvocations(this, visitor);
    for (JavaInfo child : getChildrenJava()) {
      if (child.getCreationSupport() instanceof ExposedPropertyCreationSupport) {
        visitCellInvocations(child, visitor);
      }
    }
  }

  /**
   * Visits <code>setX(row, cell, value)</code> invocations of given this {@link HTMLTableInfo} and
   * its formatters.
   */
  protected final void removeCellInvocations(final Rectangle cells) throws Exception {
    visitCellInvocations(new CellInvocationsVisitor() {
      public void visit(MethodDescription methodDescription,
          MethodInvocation invocation,
          Expression rowArgument,
          Expression cellArgument,
          int row,
          int cell) throws Exception {
        int column = getStatus().getColumnOfCell(row, cell);
        if (cells.y <= row && row < cells.bottom() && cells.x <= column && column < cells.right()) {
          getEditor().removeEnclosingStatement(invocation);
        }
      }
    });
  }

  /**
   * Removes <code>setX(row, cell, value)</code> invocations in given cell with given signature.
   */
  protected final void removeCellInvocations(final int sourceRow,
      final int sourceCell,
      final String sourceSignature) throws Exception {
    visitCellInvocations(new CellInvocationsVisitor() {
      public void visit(MethodDescription methodDescription,
          MethodInvocation invocation,
          Expression rowArgument,
          Expression cellArgument,
          int row,
          int cell) throws Exception {
        if (row == sourceRow
            && cell == sourceCell
            && AstNodeUtils.getMethodSignature(invocation).equals(sourceSignature)) {
          getEditor().removeEnclosingStatement(invocation);
        }
      }
    });
  }

  /**
   * Visits <code>setX(row, cell, value)</code> invocations of given {@link JavaInfo}.
   */
  private static void visitCellInvocations(JavaInfo javaInfo, CellInvocationsVisitor visitor)
      throws Exception {
    for (ASTNode node : javaInfo.getRelatedNodes()) {
      MethodInvocation invocation = javaInfo.getMethodInvocation(node);
      if (invocation != null) {
        String methodSignature = AstNodeUtils.getMethodSignature(invocation);
        MethodDescription methodDescription = javaInfo.getDescription().getMethod(methodSignature);
        if (methodDescription != null
            && methodDescription.getName().startsWith("set")
            && methodDescription.getParameters().size() > 2
            && methodDescription.getParameter(0).getType() == int.class
            && methodDescription.getParameter(1).getType() == int.class
            && methodDescription.getParameter(0).getTags().containsKey("HTMLTable.row")
            && methodDescription.getParameter(1).getTags().containsKey("HTMLTable.cell")) {
          List<Expression> arguments = DomGenerics.arguments(invocation);
          Expression rowExpression = arguments.get(0);
          Expression cellExpression = arguments.get(1);
          int row = (Integer) JavaInfoEvaluationHelper.getValue(rowExpression);
          int cell = (Integer) JavaInfoEvaluationHelper.getValue(cellExpression);
          visitor.visit(methodDescription, invocation, rowExpression, cellExpression, row, cell);
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AST utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Visits arguments for {@link MethodInvocation} parameters marked with some tag.
   */
  interface TaggedParameterVisitor {
    void visit(Expression argument) throws Exception;
  }
  /**
   * Visits arguments for {@link MethodInvocation} parameters marked with some tag.
   */
  interface TaggedParameterVisitorEx {
    void visit(AbstractInvocationDescription methodDescription,
        List<Expression> arguments,
        ParameterDescription parameter,
        Expression argument) throws Exception;
  }

  /**
   * Visits arguments of {@link MethodInvocation} which {@link ParameterDescription} is marked with
   * required tag. The {@link MethodInvocation}'s of this {@link HTMLTableInfo} and its exposed
   * formatters are considered.
   */
  void visitTaggedParameters(String tagName, final TaggedParameterVisitor visitor) throws Exception {
    TaggedParameterVisitorEx _visitor = new TaggedParameterVisitorEx() {
      public void visit(AbstractInvocationDescription methodDescription,
          List<Expression> arguments,
          ParameterDescription parameter,
          Expression argument) throws Exception {
        visitor.visit(argument);
      }
    };
    visitTaggedParameters(tagName, _visitor);
  }

  /**
   * Visits arguments of {@link MethodInvocation} which {@link ParameterDescription} is marked with
   * required tag. The {@link MethodInvocation}'s of this {@link HTMLTableInfo} and its exposed
   * formatters are considered.
   */
  void visitTaggedParameters(String tagName, TaggedParameterVisitorEx visitor) throws Exception {
    visitTaggedParameters(this, visitor, tagName);
    for (JavaInfo child : getChildrenJava()) {
      if (child.getCreationSupport() instanceof ExposedPropertyCreationSupport) {
        visitTaggedParameters(child, visitor, tagName);
      }
    }
  }

  /**
   * Visits arguments of {@link MethodInvocation} of given {@link JavaInfo} which
   * {@link ParameterDescription} is marked with required tag.
   */
  private static void visitTaggedParameters(JavaInfo javaInfo,
      TaggedParameterVisitorEx visitor,
      String tagName) throws Exception {
    // visit constructor
    if (javaInfo.getCreationSupport() instanceof ConstructorCreationSupport) {
      ConstructorCreationSupport creationSupport =
          (ConstructorCreationSupport) javaInfo.getCreationSupport();
      ClassInstanceCreation creation = creationSupport.getCreation();
      ConstructorDescription constructorDescription = creationSupport.getDescription();
      List<Expression> arguments = DomGenerics.arguments(creation);
      for (ParameterDescription parameter : constructorDescription.getParameters()) {
        if (parameter.hasTrueTag(tagName)) {
          visitTaggedParameter(visitor, constructorDescription, arguments, parameter);
        }
      }
    }
    // visit methods
    for (MethodInvocation invocation : javaInfo.getMethodInvocations()) {
      String methodSignature = AstNodeUtils.getMethodSignature(invocation);
      MethodDescription methodDescription = javaInfo.getDescription().getMethod(methodSignature);
      if (methodDescription != null) {
        List<Expression> arguments = DomGenerics.arguments(invocation);
        for (ParameterDescription parameter : methodDescription.getParameters()) {
          if (parameter.hasTrueTag(tagName)) {
            visitTaggedParameter(visitor, methodDescription, arguments, parameter);
          }
        }
      }
    }
  }

  private static void visitTaggedParameter(TaggedParameterVisitorEx visitor,
      AbstractInvocationDescription description,
      List<Expression> arguments,
      ParameterDescription parameter) throws Exception {
    int index = parameter.getIndex();
    Expression argument = arguments.get(index);
    visitor.visit(description, arguments, parameter, argument);
  }

  /**
   * Replaces {@link Expression} with integer value.
   */
  void setIntExpression(Expression expression, int value) throws Exception {
    String source = IntegerConverter.INSTANCE.toJavaSource(this, value);
    Expression newExpression = getEditor().replaceExpression(expression, source);
    JavaInfoEvaluationHelper.setValue(newExpression, value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Table status
  //
  ////////////////////////////////////////////////////////////////////////////
  protected HTMLTableStatus m_status;

  /**
   * Initializes {@link HTMLTableStatus} about underlying <code>TABLE</code> in this model.
   */
  private void initializeTableStatus() throws Exception {
    if (m_status == null) {
      m_status = createTableStatus();
      initializeDimensions();
    }
  }

  /**
   * Creates {@link HTMLTableStatus} that corresponds to the type of {@link HTMLTableInfo}.
   */
  protected abstract HTMLTableStatus createTableStatus() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // CellConstraintsSupport access
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<WidgetInfo, CellConstraintsSupport> m_constraints = Maps.newHashMap();

  /**
   * @return the {@link CellConstraintsSupport} for given {@link ComponentInfo}.
   */
  public static CellConstraintsSupport getConstraints(final WidgetInfo component) {
    return ExecutionUtils.runObject(new RunnableObjectEx<CellConstraintsSupport>() {
      public CellConstraintsSupport runObject() throws Exception {
        HTMLTableInfo panel = (HTMLTableInfo) component.getParent();
        Assert.isTrue(panel.getChildrenWidgets().contains(component));
        //
        CellConstraintsSupport cell = panel.m_constraints.get(component);
        if (cell == null) {
          cell = new CellConstraintsSupport(panel, component);
          panel.m_constraints.put(component, cell);
        }
        return cell;
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dimensions
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_deleteDimensionLevel = 0;
  private final List<ColumnInfo> m_columns = Lists.newArrayList();
  private final List<RowInfo> m_rows = Lists.newArrayList();

  /**
   * Fills {@link #m_columns} and {@link #m_rows} collections.
   */
  private void initializeDimensions() {
    // add columns
    for (int i = 0; i < m_status.getColumnCount(); i++) {
      m_columns.add(new ColumnInfo(this));
    }
    // add rows
    for (int i = 0; i < m_status.getRowCount(); i++) {
      m_rows.add(new RowInfo(this));
    }
  }

  /**
   * @return the list of all {@link ColumnInfo}'s.
   */
  public final List<ColumnInfo> getColumns() {
    return m_columns;
  }

  /**
   * @return the list of all {@link RowInfo}'s.
   */
  public final List<RowInfo> getRows() {
    return m_rows;
  }

  /**
   * @return <code>true</code> if given column is empty, i.e. has no components that start in it.
   */
  boolean isEmptyColumn(final int column) {
    final boolean[] isEmpty = new boolean[]{true};
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        visitTaggedParameters("HTMLTable.cell", new TaggedParameterVisitorEx() {
          public void visit(AbstractInvocationDescription methodDescription,
              List<Expression> arguments,
              ParameterDescription parameter,
              Expression argument) throws Exception {
            int parameterIndex = parameter.getIndex();
            Expression rowExpression = arguments.get(parameterIndex - 1);
            Expression cellExpression = arguments.get(parameterIndex);
            int row = (Integer) JavaInfoEvaluationHelper.getValue(rowExpression);
            int cell = (Integer) JavaInfoEvaluationHelper.getValue(cellExpression);
            int cellColumn = getStatus().getColumnOfCell(row, cell);
            isEmpty[0] &= cellColumn != column;
          }
        });
      }
    });
    return isEmpty[0];
  }

  /**
   * @return <code>true</code> if given row is empty, i.e. has no components that start in it.
   */
  boolean isEmptyRow(final int row) {
    final boolean[] isEmpty = new boolean[]{true};
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        visitTaggedParameters("HTMLTable.row", new TaggedParameterVisitorEx() {
          public void visit(AbstractInvocationDescription methodDescription,
              List<Expression> arguments,
              ParameterDescription parameter,
              Expression argument) throws Exception {
            int invocationRow = (Integer) JavaInfoEvaluationHelper.getValue(argument);
            isEmpty[0] &= invocationRow != row;
          }
        });
      }
    });
    return isEmpty[0];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Inserts new {@link ColumnInfo} into target index.
   */
  public final void insertColumn(final int index) throws Exception {
    m_columns.add(index, new ColumnInfo(this));
    m_status.insertColumn(index);
    // update constraints
    visitGridComponents(new MigComponentVisitor() {
      public void visit(WidgetInfo component, CellConstraintsSupport cell) throws Exception {
        if (cell.getX() >= index) {
          cell.updateX(1);
        } else if (cell.getX() + cell.getWidth() > index) {
          cell.updateWidth(1);
        }
      }
    });
    // update invocations
    visitTaggedParameters("HTMLTable.column", new TaggedParameterVisitor() {
      public void visit(Expression argument) throws Exception {
        int value = (Integer) JavaInfoEvaluationHelper.getValue(argument);
        if (value >= index) {
          setIntExpression(argument, value + 1);
        }
      }
    });
    visitTaggedParameters("HTMLTable.cell", new TaggedParameterVisitorEx() {
      public void visit(AbstractInvocationDescription methodDescription,
          List<Expression> arguments,
          ParameterDescription parameter,
          Expression argument) throws Exception {
        int parameterIndex = parameter.getIndex();
        int row = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(parameterIndex - 1));
        int cell = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(parameterIndex));
        int cellColumn = getStatus().getColumnOfCell(row, cell);
        if (cellColumn >= index) {
          setIntExpression(argument, cell + 1);
        } else if (methodDescription.getSignature().equals("setColSpan(int,int,int)")
            && cellColumn + getStatus().getColSpan(row, cell) > index) {
          int colSpan = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(2));
          setIntExpression(arguments.get(2), colSpan + 1);
        }
      }
    });
  }

  /**
   * Deletes the {@link ColumnInfo} with given index.
   */
  public final void deleteColumn(final int index) throws Exception {
    try {
      m_deleteDimensionLevel++;
      deleteColumn0(index);
    } finally {
      m_deleteDimensionLevel--;
    }
    deleteTrailingEmptyRows();
  }

  /**
   * Implementation of {@link #deleteColumn(int)}.
   */
  private void deleteColumn0(final int index) throws Exception {
    // update constraints
    visitGridComponents(new MigComponentVisitor() {
      public void visit(WidgetInfo component, CellConstraintsSupport cell) throws Exception {
        if (cell.getX() == index) {
          component.delete();
        } else if (cell.getX() >= index) {
          cell.updateX(-1);
        } else if (cell.getX() + cell.getWidth() > index) {
          cell.updateWidth(-1);
        }
      }
    });
    // update invocations
    visitTaggedParameters("HTMLTable.column", new TaggedParameterVisitor() {
      public void visit(Expression argument) throws Exception {
        int value = (Integer) JavaInfoEvaluationHelper.getValue(argument);
        if (value == index) {
          getEditor().removeEnclosingStatement(argument);
        } else if (value >= index) {
          setIntExpression(argument, value - 1);
        }
      }
    });
    visitTaggedParameters("HTMLTable.cell", new TaggedParameterVisitorEx() {
      public void visit(AbstractInvocationDescription methodDescription,
          List<Expression> arguments,
          ParameterDescription parameter,
          Expression argument) throws Exception {
        int parameterIndex = parameter.getIndex();
        int row = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(parameterIndex - 1));
        int cell = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(parameterIndex));
        int cellColumn = getStatus().getColumnOfCell(row, cell);
        if (cellColumn == index) {
          getEditor().removeEnclosingStatement(argument);
        } else if (cellColumn >= index) {
          setIntExpression(argument, cell - 1);
        } else if (methodDescription.getSignature().equals("setColSpan(int,int,int)")
            && cellColumn + getStatus().getColSpan(row, cell) > index) {
          int colSpan = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(2));
          setIntExpression(arguments.get(2), colSpan - 1);
        }
      }
    });
    // update status
    m_columns.remove(index);
    m_status.deleteColumn(index);
    // remove empty rows at the end
    deleteTrailingEmptyRows();
  }

  /**
   * Deletes the {@link ComponentInfo}'s that located in {@link ColumnInfo} with given index.
   */
  public final void clearColumn(final int index) throws Exception {
    // delete components
    visitGridComponents(new MigComponentVisitor() {
      public void visit(WidgetInfo component, CellConstraintsSupport cell) throws Exception {
        if (cell.getX() == index) {
          component.delete();
        }
      }
    });
    // delete invocations
    removeCellInvocations(new Rectangle(index, 0, 1, m_status.getRowCount()));
    // delete trailing dimensions
    deleteTrailingEmptyColumns();
    deleteTrailingEmptyRows();
  }

  /**
   * Deletes empty columns at the end.
   */
  private void deleteTrailingEmptyColumns() throws Exception {
    if (m_deleteDimensionLevel != 0) {
      return;
    }
    while (!getColumns().isEmpty()) {
      int column = getColumns().size() - 1;
      if (isEmptyColumn(column)) {
        deleteColumn(column);
      } else {
        break;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Inserts new {@link RowInfo} into target index.
   */
  public final void insertRow(final int index) throws Exception {
    m_rows.add(index, new RowInfo(this));
    m_status.insertRow(index);
    // update constraints
    visitGridComponents(new MigComponentVisitor() {
      public void visit(WidgetInfo component, CellConstraintsSupport cell) throws Exception {
        if (cell.getY() >= index) {
          cell.updateY(1);
        } else if (cell.getY() + cell.getHeight() > index) {
          cell.updateHeight(1);
        }
      }
    });
    // update invocations
    visitTaggedParameters("HTMLTable.row", new TaggedParameterVisitorEx() {
      public void visit(AbstractInvocationDescription methodDescription,
          List<Expression> arguments,
          ParameterDescription parameter,
          Expression argument) throws Exception {
        int row = (Integer) JavaInfoEvaluationHelper.getValue(argument);
        if (row >= index) {
          setIntExpression(argument, row + 1);
        } else if (methodDescription.getSignature().equals("setRowSpan(int,int,int)")) {
          int cell = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(1));
          if (row + getStatus().getRowSpan(row, cell) > index) {
            int colSpan = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(2));
            setIntExpression(arguments.get(2), colSpan + 1);
          }
        }
      }
    });
  }

  /**
   * Deletes the {@link RowInfo} with given index.
   */
  public final void deleteRow(final int index) throws Exception {
    try {
      m_deleteDimensionLevel++;
      deleteRow0(index);
    } finally {
      m_deleteDimensionLevel--;
    }
    deleteTrailingEmptyColumns();
  }

  /**
   * Implementation of {@link #deleteRow(int)}.
   */
  private void deleteRow0(final int index) throws Exception {
    // update constraints
    visitGridComponents(new MigComponentVisitor() {
      public void visit(WidgetInfo component, CellConstraintsSupport cell) throws Exception {
        if (cell.getY() == index) {
          component.delete();
        } else if (cell.getY() >= index) {
          cell.updateY(-1);
        } else if (cell.getY() + cell.getHeight() > index) {
          cell.updateHeight(-1);
        }
      }
    });
    // update invocations
    visitTaggedParameters("HTMLTable.row", new TaggedParameterVisitorEx() {
      public void visit(AbstractInvocationDescription methodDescription,
          List<Expression> arguments,
          ParameterDescription parameter,
          Expression argument) throws Exception {
        int row = (Integer) JavaInfoEvaluationHelper.getValue(argument);
        if (row == index) {
          getEditor().removeEnclosingStatement(argument);
        } else if (row > index) {
          setIntExpression(argument, row - 1);
        } else if (methodDescription.getSignature().equals("setRowSpan(int,int,int)")) {
          int cell = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(1));
          if (row + getStatus().getRowSpan(row, cell) > index) {
            int rowSpan = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(2));
            setIntExpression(arguments.get(2), rowSpan - 1);
          }
        }
      }
    });
    // update status
    m_rows.remove(index);
    m_status.deleteRow(index);
  }

  /**
   * Deletes the {@link ComponentInfo}'s that located in {@link RowInfo} with given index.
   */
  public final void clearRow(final int index) throws Exception {
    // delete components
    visitGridComponents(new MigComponentVisitor() {
      public void visit(WidgetInfo component, CellConstraintsSupport cell) throws Exception {
        if (cell.getY() == index) {
          component.delete();
        }
      }
    });
    // delete invocations
    removeCellInvocations(new Rectangle(0, index, m_status.getColumnCount(), 1));
    // delete trailing dimensions
    deleteTrailingEmptyColumns();
    deleteTrailingEmptyRows();
  }

  /**
   * Deletes empty rows at the end.
   */
  private void deleteTrailingEmptyRows() throws Exception {
    if (m_deleteDimensionLevel != 0) {
      return;
    }
    while (!getRows().isEmpty()) {
      int row = getRows().size() - 1;
      if (isEmptyRow(row)) {
        deleteRow(row);
      } else {
        break;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets location of component.
   */
  public final void setComponentLocation(WidgetInfo component, int newColumn, final int newRow)
      throws Exception {
    CellConstraintsSupport constraints = getConstraints(component);
    int column = constraints.getX();
    final int row = constraints.getY();
    if (column == newColumn && row == newRow) {
      return;
    }
    // set location in constraints
    constraints.setX(newColumn);
    constraints.setY(newRow);
    // update setXXX() invocations
    final int newCell = getStatus().getCellOfColumn(newRow, newColumn);
    boolean isExistingCell = row != -1 && column != -1;
    if (isExistingCell) {
      boolean isSameCell = newColumn == column && newRow == row;
      if (!isSameCell) {
        final int cell = getStatus().getCellOfColumn(row, column);
        visitCellInvocations(new CellInvocationsVisitor() {
          public void visit(MethodDescription methodDescription,
              MethodInvocation invocation,
              Expression rowArgument,
              Expression cellArgument,
              int row_,
              int cell_) throws Exception {
            // remove invocations for target cell
            if (row_ == newRow && cell_ == newCell) {
              getEditor().removeEnclosingStatement(invocation);
            }
            // move invocations from source to target cell
            if (row_ == row && cell_ == cell) {
              setIntExpression(rowArgument, newRow);
              setIntExpression(cellArgument, newCell);
            }
          }
        });
      }
    } else {
      constraints.setAssociationArgumentInt("HTMLTable.row", newRow);
      constraints.setAssociationArgumentInt("HTMLTable.cell", newCell);
    }
    // clean up
    deleteTrailingEmptyRows();
    deleteTrailingEmptyColumns();
  }

  /**
   * Sets number of columns filled by component.
   */
  public void setComponentColSpan(WidgetInfo component, int newSpan) throws Exception {
  }

  /**
   * Sets number of rows filled by component.
   */
  public void setComponentRowSpan(WidgetInfo component, int newSpan) throws Exception {
  }

  /**
   * Sets cells occupied by given {@link WidgetInfo}.
   */
  public final void command_CELLS(WidgetInfo component, Rectangle cells) throws Exception {
    setComponentColSpan(component, 1);
    setComponentRowSpan(component, 1);
    setComponentLocation(component, cells.x, cells.y);
    setComponentColSpan(component, cells.width);
    setComponentRowSpan(component, cells.height);
  }

  /**
   * Creates new {@link WidgetInfo} in given cell.
   * 
   * @param newComponent
   *          the new {@link WidgetInfo} to create.
   * @param column
   *          the column, 0 based.
   * @param row
   *          the row, 0 based.
   */
  public final void command_CREATE(WidgetInfo newComponent,
      int column,
      boolean insertColumn,
      int row,
      boolean insertRow) throws Exception {
    prepareCell(column, insertColumn, row, insertRow);
    // add in components
    {
      WidgetInfo nextComponent = getReference(column, row, null);
      JavaInfoUtils.add(
          newComponent,
          AssociationObjects.invocationChild("%parent%.setWidget(0, 0, %child%)", true),
          this,
          nextComponent);
    }
    // move in grid
    setComponentLocation(newComponent, column, row);
  }

  /**
   * Moves existing {@link WidgetInfo} into new cell.
   */
  public final void command_MOVE(WidgetInfo component,
      int column,
      boolean insertColumn,
      int row,
      boolean insertRow) throws Exception {
    prepareCell(column, insertColumn, row, insertRow);
    // if internal move, clear span
    if (component.getParent() == this) {
      setComponentColSpan(component, 1);
      setComponentRowSpan(component, 1);
    }
    // move in components
    {
      WidgetInfo nextComponent = getReference(column, row, component);
      JavaInfoUtils.move(
          component,
          AssociationObjects.invocationChild("%parent%.setWidget(0, 0, %child%)", false),
          this,
          nextComponent);
    }
    // move in grid
    setComponentLocation(component, column, row);
  }

  /**
   * @return the {@link WidgetInfo} that should be used as reference for adding into given cell.
   * 
   * @param exclude
   *          the {@link ComponentInfo} that should not be checked, for example because we move it
   *          now.
   */
  private WidgetInfo getReference(int column, int row, WidgetInfo exclude) throws Exception {
    for (WidgetInfo component : getChildrenWidgets()) {
      if (component != exclude) {
        CellConstraintsSupport constraints = getConstraints(component);
        if (constraints.getY() > row || constraints.getY() == row && constraints.getX() >= column) {
          return component;
        }
      }
    }
    // no reference
    return null;
  }

  /**
   * Prepares cell with given column/row - inserts columns/rows if necessary.
   */
  protected final void prepareCell(int column, boolean insertColumn, int row, boolean insertRow)
      throws Exception {
    Assert.isTrue(column >= 0, "Invalid column %d", column);
    Assert.isTrue(row >= 0, "Invalid row %d", row);
    // insert column/row
    if (insertColumn) {
      insertColumn(column);
    }
    if (insertRow) {
      insertRow(row);
    }
    // append columns/rows
    while (m_columns.size() <= column) {
      insertColumn(m_columns.size());
    }
    while (m_rows.size() <= row) {
      insertRow(m_rows.size());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
    final Rectangle cells = getGridInfo().getComponentCells(widget);
    commands.add(new PanelClipboardCommand<HTMLTableInfo>(widget) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(HTMLTableInfo panel, WidgetInfo widget) throws Exception {
        panel.m_deleteDimensionLevel++;
        try {
          panel.command_CREATE(widget, cells.x, false, cells.y, false);
          panel.command_CELLS(widget, cells);
        } finally {
          panel.m_deleteDimensionLevel--;
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Visitor for {@link WidgetInfo} and their {@link CellConstraintsSupport}.
   */
  interface MigComponentVisitor {
    void visit(WidgetInfo component, CellConstraintsSupport constraints) throws Exception;
  }

  /**
   * Visits grid {@link ComponentInfo}'s of this {@link ContainerInfo}.
   */
  void visitGridComponents(final MigComponentVisitor visitor) {
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        for (WidgetInfo component : getChildrenWidgets()) {
          CellConstraintsSupport cell = getConstraints(component);
          visitor.visit(component, cell);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGridInfo support
  //
  ////////////////////////////////////////////////////////////////////////////
  private IGridInfo m_gridInfo;

  /**
   * @return the {@link IGridInfo} that describes this layout.
   */
  public IGridInfo getGridInfo() {
    if (m_gridInfo == null) {
      ExecutionUtils.runRethrow(new RunnableEx() {
        public void run() throws Exception {
          createGridInfo();
        }
      });
    }
    return m_gridInfo;
  }

  /**
   * Initializes {@link #m_gridInfo}.
   */
  private void createGridInfo() throws Exception {
    fetchCells();
    m_gridInfo = new IGridInfo() {
      ////////////////////////////////////////////////////////////////////////////
      //
      // Dimensions
      //
      ////////////////////////////////////////////////////////////////////////////
      public int getColumnCount() {
        return m_columnIntervals.length;
      }

      public int getRowCount() {
        return m_rowIntervals.length;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Intervals
      //
      ////////////////////////////////////////////////////////////////////////////
      public Interval[] getColumnIntervals() {
        return m_columnIntervals;
      }

      public Interval[] getRowIntervals() {
        return m_rowIntervals;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Cells
      //
      ////////////////////////////////////////////////////////////////////////////
      public Rectangle getComponentCells(IAbstractComponentInfo component) {
        Assert.instanceOf(WidgetInfo.class, component);
        return m_widgetToCells.get(component);
      }

      public Rectangle getCellsRectangle(Rectangle cells) {
        int x = m_columnIntervals[cells.x].begin;
        int y = m_rowIntervals[cells.y].begin;
        if (cells.isEmpty()) {
          return new Rectangle(x, y, 0, 0);
        } else {
          int w = m_columnIntervals[cells.right() - 1].end() - x;
          int h = m_rowIntervals[cells.bottom() - 1].end() - y;
          return new Rectangle(x, y, w + 1, h + 1);
        }
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Feedback
      //
      ////////////////////////////////////////////////////////////////////////////
      public boolean isRTL() {
        return false;
      }

      public Insets getInsets() {
        return new Insets();
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Virtual columns
      //
      ////////////////////////////////////////////////////////////////////////////
      public boolean hasVirtualColumns() {
        return false;
      }

      public int getVirtualColumnSize() {
        throw new NotImplementedException("HTMLTable does not supports virtual intervals.");
      }

      public int getVirtualColumnGap() {
        throw new NotImplementedException("HTMLTable does not supports virtual intervals.");
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Virtual rows
      //
      ////////////////////////////////////////////////////////////////////////////
      public boolean hasVirtualRows() {
        return false;
      }

      public int getVirtualRowSize() {
        throw new NotImplementedException("HTMLTable does not supports virtual intervals.");
      }

      public int getVirtualRowGap() {
        throw new NotImplementedException("HTMLTable does not supports virtual intervals.");
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Checks
      //
      ////////////////////////////////////////////////////////////////////////////
      public AbstractComponentInfo getOccupied(int column, int row) {
        for (Map.Entry<WidgetInfo, Rectangle> entry : m_widgetToCells.entrySet()) {
          if (entry.getValue().contains(column, row)) {
            return entry.getKey();
          }
        }
        return null;
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cells information
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<WidgetInfo, Rectangle> m_widgetToCells = Maps.newHashMap();
  private Interval[] m_columnIntervals;
  private Interval[] m_rowIntervals;

  /**
   * Fetch cells information.
   */
  private void fetchCells() throws Exception {
    GwtState state = getState();
    DOMUtils dom = getDOMUtils();
    m_widgetToCells.clear();
    // prepare map: Element -> Widget_Info
    Map<Object, WidgetInfo> elementToWidgetMap = Maps.newHashMap();
    for (WidgetInfo widget : getChildrenWidgets()) {
      Object widgetObject = widget.getObject();
      Object widgetElement = state.getUIObjectUtils().getElement(widgetObject);
      elementToWidgetMap.put(widgetElement, widget);
    }
    // prepare cells used by each Widget_Info
    m_columnIntervals = new Interval[m_status.getColumnCount()];
    m_rowIntervals = new Interval[m_status.getRowCount()];
    Map<Interval, Interval> spannedColumnIntervals = Maps.newHashMap();
    Map<Interval, Interval> spannedRowIntervals = Maps.newHashMap();
    for (int row = 0; row < m_status.getRowCount(); row++) {
      for (int cell = 0, column = 0; column < m_status.getColumnCount(); cell++) {
        // FlexTableHelper may remove cells in row below spanned
        if (!m_status.isExistingCell(row, cell)) {
          column++;
          continue;
        }
        // prepare cell information
        int fixedCell = m_status.fixCellAfterRowSpan(row, cell);
        Object td = m_status.getElement(row, fixedCell);
        int colSpan = m_status.getColSpan(row, cell);
        int rowSpan = m_status.getRowSpan(row, cell);
        // remember widget cells
        if (dom.getChildCount(td) == 1) {
          Object tdChild = dom.getChild(td, 0);
          tdChild = dom.unwrapElement(tdChild);
          WidgetInfo widget = elementToWidgetMap.get(tdChild);
          if (widget != null) {
            m_widgetToCells.put(widget, new Rectangle(column, row, colSpan, rowSpan));
          }
        }
        // prepare row interval
        if (m_rowIntervals[row] == null) {
          Object trElement = dom.getParent(td);
          Rectangle trBounds = state.getAbsoluteBounds(trElement);
          absoluteToRelative(trBounds);
          Interval trInterval = new Interval(trBounds.y, trBounds.height);
          if (rowSpan == 1) {
            m_rowIntervals[row] = trInterval;
          } else {
            Rectangle tdBounds = state.getAbsoluteBounds(td);
            Interval spannedInterval = new Interval(trBounds.y, tdBounds.height);
            spannedRowIntervals.put(new Interval(row, rowSpan), spannedInterval);
          }
        }
        // prepare column interval
        if (m_columnIntervals[column] == null) {
          Rectangle tdBounds = state.getAbsoluteBounds(td);
          absoluteToRelative(tdBounds);
          Interval columnInterval = new Interval(tdBounds.x, tdBounds.width);
          if (colSpan == 1) {
            m_columnIntervals[column] = columnInterval;
          } else {
            spannedColumnIntervals.put(new Interval(column, colSpan), columnInterval);
          }
        }
        // next column
        column += colSpan;
      }
    }
    // fix spanned columns/rows
    fetchCells_fixSpannedColumns(spannedColumnIntervals);
    fetchCells_fixSpannedRows(spannedRowIntervals);
    // if no rows, fill column intervals
    if (m_rowIntervals.length == 0) {
      for (int i = 0; i < m_columnIntervals.length; i++) {
        m_columnIntervals[i] = new Interval();
      }
    }
    // if no columns, fill row intervals
    if (m_columnIntervals.length == 0) {
      for (int i = 0; i < m_rowIntervals.length; i++) {
        m_rowIntervals[i] = new Interval();
      }
    }
    // ensure that widget has at least some cells
    for (WidgetInfo widget : getChildrenWidgets()) {
      Rectangle cells = m_widgetToCells.get(widget);
      if (cells == null) {
        m_widgetToCells.put(widget, new Rectangle(0, 0, 0, 0));
      }
    }
    //
    /*System.out.println(getStatus());
    System.out.println("rows: " + ArrayUtils.toString(m_rowIntervals));
    System.out.println("columns: " + ArrayUtils.toString(m_columnIntervals));
    System.out.println("spannedRowIntervals: " + spannedRowIntervals);
    System.out.println("spannedColumnIntervals: " + spannedColumnIntervals);
    System.out.println(StringUtils.join(m_widgetToCells.entrySet().iterator(), "\n\t"));*/
  }

  /**
   * It is possible that some columns don't have individual widgets in not spanned cells, so we can
   * not get exact columns intervals and have to approximate it.
   */
  private void fetchCells_fixSpannedColumns(Map<Interval, Interval> spannedColumnIntervals) {
    for (int column = 0; column < m_columnIntervals.length; column++) {
      Interval columnInterval = m_columnIntervals[column];
      if (columnInterval == null) {
        for (Map.Entry<Interval, Interval> spanEntry : spannedColumnIntervals.entrySet()) {
          Interval spanRange = spanEntry.getKey();
          if (spanRange.contains(column)) {
            int x = spanEntry.getValue().begin;
            int width = spanEntry.getValue().length / spanRange.length;
            for (int _column = spanRange.begin(); _column < spanRange.end(); _column++) {
              if (_column == column) {
                m_columnIntervals[_column] = new Interval(x, width);
              }
              x += width;
            }
            break;
          }
        }
      }
    }
  }

  /**
   * It is possible that some rows don't have individual widgets in not spanned cells, so we can not
   * get exact row intervals and have to approximate it.
   */
  private void fetchCells_fixSpannedRows(Map<Interval, Interval> spannedRowIntervals) {
    for (int row = 0; row < m_rowIntervals.length; row++) {
      Interval rowInterval = m_rowIntervals[row];
      if (rowInterval == null) {
        for (Map.Entry<Interval, Interval> spanEntry : spannedRowIntervals.entrySet()) {
          Interval spanRange = spanEntry.getKey();
          if (spanRange.contains(row)) {
            int y = spanEntry.getValue().begin;
            int height = spanEntry.getValue().length / spanRange.length;
            for (int _row = spanRange.begin(); _row < spanRange.end(); _row++) {
              if (_row == row) {
                m_rowIntervals[_row] = new Interval(y, height);
              }
              y += height;
            }
            break;
          }
        }
      }
    }
  }
}
