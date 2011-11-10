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

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.AbstractInvocationDescription;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for GWT <code>FlexTable</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class FlexTableInfo extends HTMLTableInfo {
  private JavaInfo m_flexCellFormatter;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FlexTableInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    keepLast_FlexTableHelper();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exposed objects
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createExposedChildren() throws Exception {
    super.createExposedChildren();
    if (m_flexCellFormatter == null) {
      m_flexCellFormatter = JavaInfoUtils.addChildExposedByMethod(this, "getFlexCellFormatter");
    }
  }

  /**
   * @return the exposed <code>flexCellFormatter</code> model.
   */
  JavaInfo getFlexCellFormatter() {
    return m_flexCellFormatter;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    ((FlexTableStatus) getStatus()).fillAllCells();
    fixRowSpan();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FlexTableHelper
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds broadcast listener that keeps <code>FlexTableHelper.fixRowSpan()</code> as last statement
   * for this {@link FlexTableInfo}.
   */
  private void keepLast_FlexTableHelper() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void target_isTerminalStatement(JavaInfo parent,
          JavaInfo child,
          Statement statement,
          boolean[] terminal) {
        if (parent == FlexTableInfo.this && isFlexTableHelperInvocation(statement)) {
          terminal[0] = true;
        }
      }

      private boolean isFlexTableHelperInvocation(Statement statement) {
        return statement.toString().contains("FlexTableHelper.fixRowSpan(");
      }
    });
  }

  /**
   * Attempts to use <code>FlexTableHelper</code> in same package to fix "rowSpan" bug.
   */
  private void fixRowSpan() throws Exception {
    // prepare FlexTableHelper in same package
    final String helperClassName;
    {
      TypeDeclaration rootTypeDeclaration = JavaInfoUtils.getTypeDeclaration(this);
      String rootTypeName = AstNodeUtils.getFullyQualifiedName(rootTypeDeclaration, false);
      String packageName = CodeUtils.getPackage(rootTypeName);
      helperClassName = packageName + ".FlexTableHelper";
    }
    // if FlexTableHelper exists, use it
    ((FlexTableStatus) getStatus()).setRowSpanFixed(false);
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        Class<?> helperClass =
            JavaInfoUtils.getClassLoader(FlexTableInfo.this).loadClass(helperClassName);
        ReflectionUtils.invokeMethod(
            helperClass,
            "fixRowSpan(com.google.gwt.user.client.ui.FlexTable)",
            getObject());
        ((FlexTableStatus) getStatus()).setRowSpanFixed(true);
      }
    });
  }

  /**
   * Ensures that <code>FlexTableHelper</code> exists in same package and used for this
   * {@link FlexTableInfo}.
   */
  public void ensureFlexTableHelper() throws Exception {
    IPackageFragment packageFragment = (IPackageFragment) getEditor().getModelUnit().getParent();
    String helperClassName = ensureFlexTableHelper(packageFragment);
    // ensure fixRowSpan() invocation
    if (!hasInvocation_fixRowSpan()) {
      StatementTarget target = JavaInfoUtils.getTarget(this);
      String source = TemplateUtils.format("{0}.fixRowSpan({1})", helperClassName, this);
      addExpressionStatement(target, source);
    }
  }

  /**
   * @return <code>true</code> if this {@link FlexTableInfo} already "fixed".
   */
  private boolean hasInvocation_fixRowSpan() {
    for (ASTNode relatedNode : getRelatedNodes()) {
      if (relatedNode.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
        MethodInvocation invocation = (MethodInvocation) relatedNode.getParent();
        if (invocation.toString().contains("FlexTableHelper.fixRowSpan(")) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Ensures that <code>FlexTableHelper</code> exists in given package.
   * 
   * @return the fully qualified name of <code>FlexTableHelper</code> in given package.
   */
  public static String ensureFlexTableHelper(IPackageFragment packageFragment) throws Exception {
    String packageName = packageFragment.getElementName();
    String helperClassName = packageName + ".FlexTableHelper";
    if (packageFragment.getJavaProject().findType(helperClassName) == null) {
      String source =
          IOUtils2.readString(FlexTableInfo.class.getResourceAsStream("FlexTableHelper.jvt"));
      source = StringUtils.replace(source, "%packageName%", packageName);
      packageFragment.createCompilationUnit("FlexTableHelper.java", source, true, null);
      ProjectUtils.waitForAutoBuild();
    }
    return helperClassName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Table status
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected HTMLTableStatus createTableStatus() throws Exception {
    return new FlexTableStatus(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets number of columns filled by component.
   */
  @Override
  public void setComponentColSpan(WidgetInfo component, final int newSpan) throws Exception {
    CellConstraintsSupport constraints = getConstraints(component);
    int column = constraints.getX();
    final int row = constraints.getY();
    final int cell = getStatus().getCellOfColumn(row, column);
    final int width = constraints.getWidth();
    int height = constraints.getHeight();
    // check if already has same span
    if (width == newSpan) {
      return;
    }
    // prepare cell
    prepareCell(column + newSpan - 1, false, row, false);
    // we can change span: 1 -> some value; or some value -> 1
    Assert.isTrue(width == 1 || newSpan == 1);
    constraints.setWidth(newSpan);
    // 1 -> some value
    if (width == 1) {
      // clear target cells
      removeCellInvocations(new Rectangle(column + 1, row, newSpan - 1, height));
      // add setColSpan() invocation
      {
        String arguments = String.format("%d, %d, %d", row, cell, newSpan);
        MethodInvocation invocation =
            m_flexCellFormatter.addMethodInvocation("setColSpan(int,int,int)", arguments);
        setIntExpression(DomGenerics.arguments(invocation).get(0), row);
        setIntExpression(DomGenerics.arguments(invocation).get(1), cell);
      }
    }
    // some value -> 1
    if (newSpan == 1) {
      // remove setColSpan() invocation
      removeCellInvocations(row, cell, "setColSpan(int,int,int)");
    }
    // update cells after current in this row
    visitTaggedParameters("HTMLTable.cell", new TaggedParameterVisitorEx() {
      public void visit(AbstractInvocationDescription methodDescription,
          List<Expression> arguments,
          ParameterDescription parameter,
          Expression argument) throws Exception {
        int parameterIndex = parameter.getIndex();
        int row_ = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(parameterIndex - 1));
        int cell_ = (Integer) JavaInfoEvaluationHelper.getValue(argument);
        if (row_ == row && cell_ > cell) {
          int delta = width - newSpan;
          setIntExpression(argument, cell_ + delta);
        }
      }
    });
    // update status
    ((FlexTableStatus) getStatus()).setColSpan(row, cell, newSpan);
  }

  /**
   * Sets number of rows filled by component.
   */
  @Override
  public void setComponentRowSpan(WidgetInfo component, int newSpan) throws Exception {
    CellConstraintsSupport constraints = getConstraints(component);
    int column = constraints.getX();
    int row = constraints.getY();
    int width = constraints.getWidth();
    int height = constraints.getHeight();
    int cell = getStatus().getCellOfColumn(row, column);
    // check if already has same span
    if (height == newSpan) {
      return;
    }
    // check if has required rows
    if (row + newSpan > getStatus().getRowCount()) {
      return;
    }
    // we can change span: 1 -> some value; or some value -> 1
    Assert.isTrue(height == 1 || newSpan == 1);
    constraints.setHeight(newSpan);
    // 1 -> some value
    if (height == 1) {
      // clear target cells
      removeCellInvocations(new Rectangle(column, row + 1, width, newSpan - 1));
      // add setColSpan() invocation
      {
        String arguments = row + ", " + cell + ", " + newSpan;
        m_flexCellFormatter.addMethodInvocation("setRowSpan(int,int,int)", arguments);
      }
    }
    // some value -> 1
    if (newSpan == 1) {
      // remove setRowSpan() invocation
      removeCellInvocations(row, cell, "setRowSpan(int,int,int)");
    }
    // update status
    ((FlexTableStatus) getStatus()).setRowSpan(row, cell, newSpan);
    // if "rowSpan" is used, ensure FlexTableHelper, note that it will be used only after next reparse
    if (newSpan != 1) {
      ensureFlexTableHelper();
    }
  }
}
