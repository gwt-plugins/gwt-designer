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
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

/**
 * Implementation of {@link ExpressionAccessor} for <code>CellFormatter.setXXX(int,int,value)</code>
 * invocations.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class CellFormatterExpressionAccessor extends ExpressionAccessor {
  private final String m_methodSignature;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CellFormatterExpressionAccessor(String methodName, String valueTypeName) {
    m_methodSignature = methodName + "(int,int," + valueTypeName + ")";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionAccessor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Expression getExpression(JavaInfo javaInfo) throws Exception {
    Point location = getLocation(javaInfo);
    JavaInfo formatter = getFormatter(javaInfo);
    for (MethodInvocation invocation : formatter.getMethodInvocations(m_methodSignature)) {
      List<Expression> arguments = DomGenerics.arguments(invocation);
      int row = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(0));
      int cell = (Integer) JavaInfoEvaluationHelper.getValue(arguments.get(1));
      if (row == location.y && cell == location.x) {
        return arguments.get(2);
      }
    }
    return null;
  }

  @Override
  public boolean setExpression(final JavaInfo javaInfo, final String source) throws Exception {
    final AstEditor editor = javaInfo.getEditor();
    // check for existing invocation
    {
      final Expression expression = getExpression(javaInfo);
      if (expression != null) {
        if (source != null) {
          ExecutionUtils.run(javaInfo, new RunnableEx() {
            public void run() throws Exception {
              editor.replaceExpression(expression, source);
            }
          });
        } else {
          ExecutionUtils.run(javaInfo, new RunnableEx() {
            public void run() throws Exception {
              editor.removeEnclosingStatement(expression);
            }
          });
        }
        return true;
      }
    }
    // add new invocation
    if (source != null) {
      ExecutionUtils.run(javaInfo, new RunnableEx() {
        public void run() throws Exception {
          StatementTarget target = getTarget();
          Point location = getLocation(javaInfo);
          String argumentsSource =
              TemplateUtils.format("{0}, {1}, {2}", location.y, location.x, source);
          // add invocation
          JavaInfo formatter = getFormatter(javaInfo);
          MethodInvocation invocation =
              formatter.addMethodInvocation(target, m_methodSignature, argumentsSource);
          // remember values because they will be asked later because of
          // GenericPropertyImpl.rememberValueIntoExpression(Object)
          // TODO ask Mitin and may be remove "remember"
          {
            List<Expression> arguments = DomGenerics.arguments(invocation);
            JavaInfoEvaluationHelper.setValue(arguments.get(0), location.y);
            JavaInfoEvaluationHelper.setValue(arguments.get(1), location.x);
          }
        }

        private StatementTarget getTarget() {
          Statement targetStatement = javaInfo.getAssociation().getStatement();
          return new StatementTarget(targetStatement, false);
        }
      });
      return true;
    }
    // no changes
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the (cell,row) location of {@link WidgetInfo}.
   */
  private Point getLocation(JavaInfo widget) {
    CellConstraintsSupport constraints = HTMLTableInfo.getConstraints((WidgetInfo) widget);
    int row = constraints.getY();
    int column = constraints.getX();
    int cell = getPanel(widget).getStatus().getCellOfColumn(row, column);
    return new Point(cell, row);
  }

  private static JavaInfo getFormatter(JavaInfo widget) {
    return getPanel(widget).getCellFormatter();
  }

  private static HTMLTableInfo getPanel(JavaInfo widget) {
    return (HTMLTableInfo) widget.getParent();
  }
}
