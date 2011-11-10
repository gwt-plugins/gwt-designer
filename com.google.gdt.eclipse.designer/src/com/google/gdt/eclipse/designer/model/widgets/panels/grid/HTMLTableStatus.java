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

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Container with low-level information about <code>HTMLTable</code>.
 * <p>
 * {@link HTMLTableStatus} is created only one time for each {@link HTMLTableInfo} instance, during
 * first refresh cycle. It fetches all information from <code>TABLE</code> element and then
 * {@link HTMLTableInfo} gets all information from {@link HTMLTableStatus}, and updates it during
 * operations. So, we reduce number of low level access operations and can also have actual
 * information even without refresh.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public abstract class HTMLTableStatus {
  private final HTMLTableInfo m_panel;
  protected int m_rowCount;
  protected int m_columnCount;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public HTMLTableStatus(HTMLTableInfo panel) throws Exception {
    m_panel = panel;
    m_rowCount = (Integer) ReflectionUtils.invokeMethod(panel.getObject(), "getRowCount()");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row/column count
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the count of rows in this <code>HTMLTable</code>.
   */
  public final int getRowCount() {
    return m_rowCount;
  }

  /**
   * Sets the count of rows in this <code>HTMLTable</code>.
   */
  public void setRowCount(int rowCount) throws Exception {
    m_rowCount = rowCount;
  }

  /**
   * @return the count of columns in this <code>HTMLTable</code>.
   */
  public final int getColumnCount() {
    return m_columnCount;
  }

  /**
   * Sets the count of columns in this <code>HTMLTable</code>.
   */
  public void setColumnCount(int columnCount) throws Exception {
    m_columnCount = columnCount;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Notifies {@link HTMLTableStatus} that column with given index was inserted in model.
   */
  public void insertColumn(int index) throws Exception {
    m_columnCount++;
  }

  /**
   * Notifies {@link HTMLTableStatus} that column with given index was deleted in model.
   */
  public void deleteColumn(int index) throws Exception {
    m_columnCount--;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Notifies {@link HTMLTableStatus} that row with given index was inserted in model.
   */
  public void insertRow(int index) throws Exception {
    m_rowCount++;
  }

  /**
   * Notifies {@link HTMLTableStatus} that row with given index was deleted in model.
   */
  public void deleteRow(int index) throws Exception {
    m_rowCount--;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row/column span
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the <code>rowSpan</code> attribute of given cell.
   */
  public abstract int getRowSpan(int row, int cell);

  /**
   * @return the <code>colSpan</code> attribute of given cell.
   */
  public abstract int getColSpan(int row, int cell);

  /**
   * @return the cell on row that corresponds given column.
   */
  public abstract int getCellOfColumn(int row, int column);

  /**
   * @return the column that corresponds to the cell on row.
   */
  public abstract int getColumnOfCell(int row, int cell) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row span
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * After <code>FlexTableHelper.fixRowSpan()</code> some cells in next rows may not exist (because
   * our <code>FlexTableHelper</code> removed them). So, we have to check, if they exist before
   * attempting to access.
   */
  public abstract boolean isExistingCell(int row, int cell) throws Exception;

  /**
   * After <code>FlexTableHelper.fixRowSpan()</code> cells in rows may be shifted back, to we have
   * to update cell index to make it valid for underlying <code>TABLE</code> element.
   * 
   * @return fixed cell in given row.
   */
  public abstract int fixCellAfterRowSpan(int row, int cell) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Element
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>TD</code> element at given row/cell, may throw {@link Exception} if no such
   *         element.
   */
  public final Object getElement(int row, int cell) throws Exception {
    Object cellFormatter = m_panel.getCellFormatter().getObject();
    return ReflectionUtils.invokeMethod(cellFormatter, "getElement(int,int)", row, cell);
  }
}
