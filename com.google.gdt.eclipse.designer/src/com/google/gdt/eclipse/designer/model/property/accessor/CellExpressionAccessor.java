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
package com.google.gdt.eclipse.designer.model.property.accessor;

import com.google.gdt.eclipse.designer.model.widgets.panels.PanelInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.InvocationAssociation;
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
 * Implementation of {@link ExpressionAccessor} for <code>setCellXXX(Widget,value)</code>
 * invocations.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class CellExpressionAccessor extends ExpressionAccessor {
  private final PanelInfo m_panel;
  private final String m_methodSignature;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CellExpressionAccessor(PanelInfo panel, String methodName, String valueTypeName) {
    m_panel = panel;
    m_methodSignature = methodName + "(com.google.gwt.user.client.ui.Widget," + valueTypeName + ")";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionAccessor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Expression getExpression(JavaInfo javaInfo) throws Exception {
    for (MethodInvocation invocation : m_panel.getMethodInvocations(m_methodSignature)) {
      List<Expression> arguments = DomGenerics.arguments(invocation);
      Expression widgetExpression = arguments.get(0);
      if (javaInfo.isRepresentedBy(widgetExpression)) {
        return arguments.get(1);
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
          if (javaInfo.getAssociation() instanceof InvocationAssociation) {
            StatementTarget target = getTarget(javaInfo);
            String arguments = TemplateUtils.format("{0}, {1}", javaInfo, source);
            m_panel.addMethodInvocation(target, m_methodSignature, arguments);
          }
        }

        private StatementTarget getTarget(JavaInfo javaInfo) {
          Statement targetStatement = javaInfo.getAssociation().getStatement();
          return new StatementTarget(targetStatement, false);
        }
      });
      return true;
    }
    // no changes
    return false;
  }
}
