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

import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo.TaggedParameterVisitor;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Implementation of {@link HTMLTableStatus} for <code>Grid</code> widget.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
class GridStatus extends HTMLTableStatus {
  private final GridInfo m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridStatus(GridInfo panel) throws Exception {
    super(panel);
    m_panel = panel;
    m_columnCount = (Integer) ReflectionUtils.invokeMethod(panel.getObject(), "getColumnCount()");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row/column count
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setRowCount(int rowCount) throws Exception {
    super.setRowCount(rowCount);
    low_setRowColumnCount("Grid.rows", rowCount);
  }

  @Override
  public void setColumnCount(int columnCount) throws Exception {
    super.setColumnCount(columnCount);
    low_setRowColumnCount("Grid.columns", columnCount);
  }

  /**
   * Sets rows/columns number, low level method.
   */
  private void low_setRowColumnCount(String tagName, final int count) throws Exception {
    m_panel.visitTaggedParameters(tagName, new TaggedParameterVisitor() {
      public void visit(Expression argument) throws Exception {
        m_panel.setIntExpression(argument, count);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void insertColumn(int index) throws Exception {
    super.insertColumn(index);
    setColumnCount(m_panel.getColumns().size());
  }

  @Override
  public void deleteColumn(int index) throws Exception {
    super.deleteColumn(index);
    setColumnCount(m_panel.getColumns().size());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void insertRow(int index) throws Exception {
    super.insertRow(index);
    setRowCount(m_panel.getRows().size());
  }

  @Override
  public void deleteRow(int index) throws Exception {
    super.deleteRow(index);
    setRowCount(m_panel.getRows().size());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row/column span
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int getRowSpan(int row, int cell) {
    return 1;
  }

  @Override
  public int getColSpan(int row, int cell) {
    return 1;
  }

  @Override
  public int getCellOfColumn(int row, int column) {
    return column;
  }

  @Override
  public int getColumnOfCell(int row, int cell) throws Exception {
    return cell;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row span
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isExistingCell(int row, int cell) throws Exception {
    return true;
  }

  @Override
  public int fixCellAfterRowSpan(int row, int cell) throws Exception {
    return cell;
  }
}