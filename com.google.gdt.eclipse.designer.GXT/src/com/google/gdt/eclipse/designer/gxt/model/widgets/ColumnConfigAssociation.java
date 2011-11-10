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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationUtils;
import org.eclipse.wb.core.model.association.InvocationAssociation;
import org.eclipse.wb.internal.core.model.generation.statement.AbstractInsideStatementGenerator;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

/**
 * {@link Association} between {@link ColumnConfigInfo} and {@link GridInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public final class ColumnConfigAssociation extends InvocationAssociation {
  private final SimpleName m_columns;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnConfigAssociation(SimpleName columns) {
    m_columns = columns;
  }

  public ColumnConfigAssociation(SimpleName columns, MethodInvocation invocation) {
    m_columns = columns;
    m_invocation = invocation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void add(JavaInfo javaInfo, StatementTarget target, String[] leadingComments)
      throws Exception {
    // initialize MethodInvocation instance
    String m_source = m_columns.getIdentifier() + ".add(%child%)";
    {
      // add new statement
      String source = AssociationUtils.replaceTemplates(javaInfo, m_source, target);
      List<String> lines = GenericsUtils.asList(leadingComments, source + ";");
      ExpressionStatement statement =
          (ExpressionStatement) javaInfo.getEditor().addStatement(lines, target);
      m_invocation = (MethodInvocation) statement.getExpression();
    }
    // add related nodes
    AbstractInsideStatementGenerator.addRelatedNodes(javaInfo, m_invocation);
    // set association
    setInModelNoCompound(javaInfo);
  }

  @Override
  public void move(StatementTarget target) throws Exception {
    Statement statement = getStatement();
    m_editor.moveStatement(statement, target);
  }

  @Override
  public boolean remove() throws Exception {
    Statement statement = getStatement();
    m_editor.removeEnclosingStatement(statement);
    // continue
    return super.remove();
  }
}
