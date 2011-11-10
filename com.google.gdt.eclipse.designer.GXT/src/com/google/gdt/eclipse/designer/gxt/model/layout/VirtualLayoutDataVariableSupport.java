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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.association.InvocationSecondaryAssociation;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.variable.AbstractNoNameVariableSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * {@link VariableSupport} for virtual {@link LayoutDataInfo}.
 * 
 * "Virtual" is state when there are no layout data at all.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public final class VirtualLayoutDataVariableSupport extends AbstractNoNameVariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VirtualLayoutDataVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  public String getTitle() throws Exception {
    return "(virtual layout data)";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getReferenceExpression(NodeTarget target) throws Exception {
    return materialize().getReferenceExpression(target);
  }

  @Override
  public String getAccessExpression(NodeTarget target) throws Exception {
    return getReferenceExpression(target) + ".";
  }

  /**
   * Ensures that this {@link LayoutDataInfo} has {@link LocalUniqueVariableSupport}.
   */
  VariableSupport materialize() throws Exception {
    WidgetInfo widget = (WidgetInfo) m_javaInfo.getParent();
    // prepare add() invocation
    InvocationChildAssociation association = (InvocationChildAssociation) widget.getAssociation();
    MethodInvocation invocation = association.getInvocation();
    // ensure LayoutData expression
    Expression expression;
    {
      List<Expression> arguments = DomGenerics.arguments(invocation);
      String source = m_javaInfo.getDescription().getCreation(null).getSource();
      if (arguments.size() == 2) {
        expression = m_javaInfo.getEditor().replaceExpression(arguments.get(1), source);
      } else {
        expression = m_javaInfo.getEditor().addInvocationArgument(invocation, 1, source);
      }
    }
    // set CreationSupport
    {
      m_javaInfo.setCreationSupport(new ConstructorCreationSupport((ClassInstanceCreation) expression));
      m_javaInfo.bindToExpression(expression);
      m_javaInfo.addRelatedNode(expression);
    }
    // set Association
    m_javaInfo.setAssociation(new InvocationSecondaryAssociation(invocation));
    // set VariableSupport
    VariableSupport variableSupport = new EmptyVariableSupport(m_javaInfo, expression);
    m_javaInfo.setVariableSupport(variableSupport);
    return variableSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public StatementTarget getStatementTarget() throws Exception {
    throw new IllegalStateException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "virtual-layout-data";
  }
}