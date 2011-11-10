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

import com.google.common.collect.ImmutableMap;

import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.broadcast.EvaluationEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;
import java.util.Map;

/**
 * Model for <code>TreeGrid</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public class TreeGridInfo extends GridInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeGridInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    ensureTreeColumn_directlyBeforeAssociation();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Required models
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Each time when we try to associate this <code>TreeGrid</code> with some parent (during parsing
   * and refresh), we ensure that we have <code>ColumnConfig</code> with
   * <code>TreeGridCellRenderer</code>.
   */
  private void ensureTreeColumn_directlyBeforeAssociation() {
    addBroadcastListener(new EvaluationEventListener() {
      @Override
      public void evaluateBefore(EvaluationContext context, ASTNode node) throws Exception {
        if (isPossibleAssociation(node)) {
          ensureTreeColumn();
        }
      }

      /**
       * Rough approximation for association checking, may fail sometimes.
       */
      private boolean isPossibleAssociation(ASTNode node) {
        if (node instanceof MethodInvocation) {
          MethodInvocation invocation = (MethodInvocation) node;
          List<Expression> arguments = DomGenerics.arguments(invocation);
          return !arguments.isEmpty() && isRepresentedBy(arguments.get(0));
        }
        return false;
      }
    });
  }

  private void ensureTreeColumn() throws Exception {
    ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
    Map<String, Object> variables = ImmutableMap.of("grid", getObject());
    ScriptUtils.evaluate(classLoader, CodeUtils.getSource(
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "import com.extjs.gxt.ui.client.widget.treegrid.*;",
        "cm = grid.getColumnModel();",
        "",
        "// may be has ColumnConfig with TreeGridCellRenderer",
        "foreach (column : cm.getColumns()) {",
        "  if (column.getRenderer() is TreeGridCellRenderer) {",
        "    return;",
        "  }",
        "}",
        "",
        "// make first ColumnConfig with TreeGridCellRenderer",
        "foreach (column : cm.getColumns()) {",
        "  column.setRenderer(new TreeGridCellRenderer());",
        "  return;",
        "}",
        "",
        "// create ColumnConfig with TreeGridCellRenderer",
        "treeColumn = new ColumnConfig('treeColumn', 'Tree column', 200);",
        "treeColumn.setRenderer(new TreeGridCellRenderer());",
        "cm.getColumns().add(treeColumn);"), variables);
  }
}
