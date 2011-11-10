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
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.model.widgets.support.DOMUtils;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import static org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils.invokeMethod;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link HTMLTableStatus} for <code>FlexTable</code> widget.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
class FlexTableStatus extends HTMLTableStatus {
  private final FlexTableInfo m_panel;
  private final DOMUtils m_dom;
  private final List<Integer> m_rowCellCount = Lists.newArrayList();
  private final Map<Point, Dimension> m_cellToSpan = Maps.newHashMap();
  private boolean m_isRowSpanFixed = false;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FlexTableStatus(FlexTableInfo panel) throws Exception {
    super(panel);
    m_panel = panel;
    m_dom = m_panel.getState().getDomUtils();
    // get information about existing rows/cells
    List<Integer> rowCellCount = Lists.newArrayList();
    Map<Point, Dimension> cellToSpan = Maps.newHashMap();
    {
      Object object = m_panel.getObject();
      Object flex = m_panel.getFlexCellFormatter().getObject();
      for (int row = 0; row < m_rowCount; row++) {
        int cellsInRow = (Integer) invokeMethod(object, "getCellCount(int)", row);
        rowCellCount.add(cellsInRow);
        for (int cell = 0; cell < cellsInRow; cell++) {
          int rowSpan = (Integer) invokeMethod(flex, "getRowSpan(int,int)", row, cell);
          int colSpan = (Integer) invokeMethod(flex, "getColSpan(int,int)", row, cell);
          cellToSpan.put(new Point(cell, row), new Dimension(colSpan, rowSpan));
        }
      }
    }
    // prepare m_columnCount as maximum count of columns in each row
    for (int row = 0; row < m_rowCount; row++) {
      int columnsInRow = 0;
      int cellsInRow = rowCellCount.get(row);
      for (int cell = 0; cell < cellsInRow; cell++) {
        int colSpan = cellToSpan.get(new Point(cell, row)).width;
        columnsInRow += colSpan;
      }
      m_columnCount = Math.max(m_columnCount, columnsInRow);
    }
    // last step: fill final m_rowCellCount and m_cellToSpan
    for (int row = 0; row < m_rowCount; row++) {
      // iterate over existing cells
      int column = 0;
      int cell = 0;
      {
        int cellsInRow = rowCellCount.get(row);
        for (; cell < cellsInRow; cell++) {
          Point key = new Point(cell, row);
          // remember existing span
          Dimension span = cellToSpan.get(key);
          m_cellToSpan.put(key, span);
          // move column index
          column += span.width;
        }
      }
      // if cells are over, but we still did not reach required number of columns...
      while (column < m_columnCount) {
        Point key = new Point(cell, row);
        // remember default span
        Dimension span = new Dimension(1, 1);
        m_cellToSpan.put(key, span);
        // next column/cell
        column++;
        cell++;
      }
      // set count of cells in row
      m_rowCellCount.add(cell);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(m_columnCount);
    builder.append(" ");
    builder.append(m_rowCount);
    builder.append(" ");
    builder.append(m_rowCellCount);
    builder.append(" ");
    // append short presentation of m_cellToSpan
    {
      builder.append("{");
      if (!m_cellToSpan.isEmpty()) {
        List<Map.Entry<Point, Dimension>> entries = Lists.newArrayList();
        entries.addAll(m_cellToSpan.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<Point, Dimension>>() {
          public int compare(Map.Entry<Point, Dimension> o1, Map.Entry<Point, Dimension> o2) {
            if (o1.getKey().y < o2.getKey().y) {
              return -1;
            }
            if (o1.getKey().y > o2.getKey().y) {
              return +1;
            }
            return o1.getKey().x - o2.getKey().x;
          }
        });
        for (Map.Entry<Point, Dimension> entry : entries) {
          builder.append("(");
          {
            builder.append(entry.getKey().x);
            builder.append(",");
            builder.append(entry.getKey().y);
          }
          builder.append(")=(");
          {
            builder.append(entry.getValue().width);
            builder.append(",");
            builder.append(entry.getValue().height);
          }
          builder.append(") ");
        }
        builder.setLength(builder.length() - 1);
      }
      builder.append("}");
    }
    // final result
    return builder.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Specifies if this {@link FlexTableInfo} was fixed using <code>FlexTableHelper</code>.
   */
  public void setRowSpanFixed(boolean isRowSpanFixed) {
    m_isRowSpanFixed = isRowSpanFixed;
  }

  /**
   * @return the count of cells in given row, from {@link #m_rowCellCount}.
   */
  private int getCellCount(int row) {
    return m_rowCellCount.get(row);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Low-level filling
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String EMPTY_SIZE = "20px";

  /**
   * Ensures that all <code>FlexTable</code> cells are filled, so have reasonable size.
   */
  void fillAllCells() throws Exception {
    // prepare filled (without any span) rows/columns
    Set<Point> filledCells = Sets.newHashSet();
    Set<Integer> filledRows = Sets.newHashSet();
    Set<Integer> filledColumns = Sets.newHashSet();
    for (int row = 0; row < getRowCount(); row++) {
      for (int cell = 0, column = 0; cell < getCellCount(row); cell++) {
        int colSpan = getColSpan(row, cell);
        int rowSpan = getRowSpan(row, cell);
        if (low_isFilledCell(row, cell)) {
          // mark all cell as filled
          filledCells.add(new Point(cell, row));
          // row/column is filled only if it is not spanned
          if (rowSpan == 1) {
            filledRows.add(row);
          }
          if (colSpan == 1) {
            filledColumns.add(column);
          }
        }
        column += colSpan;
      }
    }
    /*System.out.println("filledCells: " + filledCells);
    System.out.println("filledRows: " + filledRows);
    System.out.println("filledColumns: " + filledColumns);*/
    for (int row = 0; row < getRowCount(); row++) {
      for (int cell = 0, column = 0; column < getColumnCount(); cell++) {
        Object td = getElementSafe(row, cell);
        // prepare colSpan
        int colSpan = 1;
        if (td != null) {
          colSpan = getColSpan(row, cell);
        }
        // fill cell to fill column
        if (!filledCells.contains(new Point(cell, row))) {
          //System.out.println("\tfill (row,cell,column): " + row + " " + cell + " " + column);
          low_fillCell(filledRows, filledColumns, row, cell, column);
        }
        // next column
        column += colSpan;
      }
    }
  }

  private void low_fillCell(Set<Integer> filledRows,
      Set<Integer> filledColumns,
      int row,
      int cell,
      int column) throws Exception {
    Object object = m_panel.getObject();
    GwtState state = m_panel.getState();
    // do fill
    {
      Object label = state.getUIObjectUtils().createLabel();
      String labelWidth = filledColumns.contains(column) ? "1px" : EMPTY_SIZE;
      String labelHeight = filledRows.contains(row) ? "1px" : EMPTY_SIZE;
      invokeMethod(label, "setSize(java.lang.String,java.lang.String)", labelWidth, labelHeight);
      invokeMethod(
          object,
          "setWidget(int,int,com.google.gwt.user.client.ui.Widget)",
          row,
          cell,
          label);
    }
  }

  private boolean low_isFilledCell(int row, int cell) throws Exception {
    Object td = getElementSafe(row, cell);
    return low_isFilledCell(td);
  }

  private boolean low_isFilledCell(Object td) throws Exception {
    if (td != null) {
      return m_dom.getChildCount(td) != 0 || m_dom.getInnerText(td).length() != 0;
    }
    return false;
  }

  /**
   * @return <code>TD</code> element at given row/cell, or <code>null</code> if no such element.
   */
  private Object getElementSafe(int row, int cell) throws Exception {
    int cellsInRow = (Integer) invokeMethod(m_panel.getObject(), "getCellCount(int)", row);
    if (cell < cellsInRow) {
      return getElement(row, cell);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void insertColumn(int index) throws Exception {
    super.insertColumn(index);
    for (int row = 0; row < getRowCount(); row++) {
      int cell = getCellOfColumn(row, index);
      if (cell < getCellCount(row)) {
        int colSpan = getColSpan(row, cell);
        if (colSpan != 1) {
          m_cellToSpan.get(new Point(cell, row)).width++;
        } else {
          for (Iterator<Point> I = m_cellToSpan.keySet().iterator(); I.hasNext();) {
            Point key = I.next();
            if (key.y == row) {
              if (key.x >= cell) {
                key.x++;
              }
            }
          }
          m_cellToSpan.put(new Point(cell, row), new Dimension(1, 1));
          updateCellCount(row, +1);
        }
      } else {
        m_cellToSpan.put(new Point(cell, row), new Dimension(1, 1));
        updateCellCount(row, +1);
      }
    }
    rehashMap(m_cellToSpan);
  }

  @Override
  public void deleteColumn(int index) throws Exception {
    super.deleteColumn(index);
    for (int row = 0; row < getRowCount(); row++) {
      int cell = getCellOfColumn(row, index);
      int colSpan = getColSpan(row, cell);
      if (colSpan != 1) {
        m_cellToSpan.get(new Point(cell, row)).width--;
      } else {
        for (Iterator<Point> I = m_cellToSpan.keySet().iterator(); I.hasNext();) {
          Point key = I.next();
          if (key.y == row) {
            if (key.x == cell) {
              I.remove();
            } else if (key.x > cell) {
              key.x--;
            }
          }
        }
        updateCellCount(row, -1);
      }
    }
    rehashMap(m_cellToSpan);
  }

  /**
   * Recreates {@link Map} after updating its key.
   */
  private static <K, V> void rehashMap(Map<K, V> map) {
    Map<K, V> newMap = Maps.newHashMap();
    newMap.putAll(map);
    map.clear();
    map.putAll(newMap);
  }

  /**
   * Updates <code>Integer</code> value of {@link #m_rowCellCount}.
   */
  private void updateCellCount(int row, int delta) {
    m_rowCellCount.set(row, m_rowCellCount.get(row) + delta);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void insertRow(int index) throws Exception {
    super.insertRow(index);
    // update information about existing rows
    {
      for (Iterator<Map.Entry<Point, Dimension>> I = m_cellToSpan.entrySet().iterator(); I.hasNext();) {
        Map.Entry<Point, Dimension> entry = I.next();
        Point key = entry.getKey();
        if (key.y < index && index < key.y + entry.getValue().height) {
          entry.getValue().height++;
        } else if (key.y >= index) {
          key.y++;
        }
      }
      rehashMap(m_cellToSpan);
    }
    // add new row
    {
      m_rowCellCount.add(index, getColumnCount());
      for (int column = 0; column < getColumnCount(); column++) {
        m_cellToSpan.put(new Point(column, index), new Dimension(1, 1));
      }
    }
  }

  @Override
  public void deleteRow(int index) throws Exception {
    // update information about rows
    {
      for (Iterator<Map.Entry<Point, Dimension>> I = m_cellToSpan.entrySet().iterator(); I.hasNext();) {
        Map.Entry<Point, Dimension> entry = I.next();
        Point key = entry.getKey();
        if (key.y < index && index < key.y + entry.getValue().height) {
          entry.getValue().height--;
        } else if (key.y == index) {
          I.remove();
        } else if (key.y >= index) {
          key.y--;
        }
      }
      rehashMap(m_cellToSpan);
    }
    // remove row
    m_rowCellCount.remove(index);
    super.deleteRow(index);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row/column span
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int getRowSpan(final int row, final int cell) {
    return ExecutionUtils.runObject(new RunnableObjectEx<Integer>() {
      public Integer runObject() throws Exception {
        return m_cellToSpan.get(new Point(cell, row)).height;
      }
    }, "Can not find cell (cell,row) (%d, %d) in %s %s.", cell, row, m_panel, this);
  }

  /**
   * Sets the <code>rowSpan</code> attribute of given cell.
   */
  public void setRowSpan(int row, int cell, int newSpan) {
    // set new span for cell
    Dimension span = m_cellToSpan.get(new Point(cell, row));
    span.height = newSpan;
  }

  @Override
  public int getColSpan(final int row, final int cell) {
    return ExecutionUtils.runObject(new RunnableObjectEx<Integer>() {
      public Integer runObject() throws Exception {
        return m_cellToSpan.get(new Point(cell, row)).width;
      }
    }, "Can not find cell (cell,row) (%d, %d) in %s %s.", cell, row, m_panel, this);
  }

  /**
   * Sets the <code>colSpan</code> attribute of given cell.
   */
  public void setColSpan(int row, int cell, int newSpan) {
    // update cell count
    int oldSpan;
    {
      Dimension span = m_cellToSpan.get(new Point(cell, row));
      oldSpan = span.width;
      // we can change span: 1 -> some value; or some value -> 1
      Assert.isTrue(oldSpan == 1 || newSpan == 1);
      // set new span for cell
      span.width = newSpan;
    }
    // update count of cells (they will be filled by fillAllCells() method)
    updateCellCount(row, oldSpan - newSpan);
    // update span table
    {
      int delta = oldSpan - newSpan;
      if (oldSpan == 1) {
        // remove cells that will be filled
        for (int i = cell + 1; i < cell + newSpan; i++) {
          m_cellToSpan.remove(new Point(i, row));
        }
        // update cells after current on same row
        for (Point key : m_cellToSpan.keySet()) {
          if (key.y == row && key.x > cell) {
            key.x += delta;
          }
        }
      } else {
        // update cells after current on same row
        for (Point key : m_cellToSpan.keySet()) {
          if (key.y == row && key.x > cell) {
            key.x += delta;
          }
        }
        // add cells that are not filled anymore
        for (int i = cell + 1; i < cell + oldSpan; i++) {
          m_cellToSpan.put(new Point(i, row), new Dimension(1, 1));
        }
      }
      // rehash
      rehashMap(m_cellToSpan);
    }
  }

  @Override
  public int getColumnOfCell(int row, int cell) throws Exception {
    int column = 0;
    for (int _cell = 0; _cell < cell; _cell++) {
      int colSpan = getColSpan(row, _cell);
      column += colSpan;
    }
    return column;
  }

  @Override
  public int getCellOfColumn(final int row, final int column) {
    int cell = 0;
    int currentColumn = 0;
    // check existing cells
    int cellCount = getCellCount(row);
    for (; cell < cellCount; cell++) {
      int colSpan = getColSpan(row, cell);
      // check for column inside of cell (may be spanned)
      if (currentColumn <= column && column < currentColumn + colSpan) {
        return cell;
      }
      // next cell, skip its columns
      currentColumn += colSpan;
    }
    // last chance: column is directly after last cell
    if (currentColumn == column) {
      return cell;
    }
    // out of bounds
    throw new IllegalArgumentException(String.format(
        "Can not find column %d in row %d in %s.",
        column,
        row,
        m_panel));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row span
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isExistingCell(int row, int cell) throws Exception {
    if (m_isRowSpanFixed) {
      int column = getColumnOfCell(row, cell);
      for (Map.Entry<Point, Dimension> entry : m_cellToSpan.entrySet()) {
        Point location = entry.getKey();
        Dimension span = entry.getValue();
        int _row = location.y;
        int _cell = location.x;
        int _rowSpan = span.height;
        int _colSpan = span.width;
        if (_row < row && row < _row + _rowSpan) {
          int _column = getColumnOfCell(_row, _cell);
          if (_column <= column && column < _column + _colSpan) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public int fixCellAfterRowSpan(int row, int cell) throws Exception {
    int result = cell;
    if (m_isRowSpanFixed) {
      for (int _row = 0; _row < getRowCount(); _row++) {
        for (int _cell = 0; _cell < getCellCount(_row); _cell++) {
          if (_row < row) {
            int rowSpan = getRowSpan(_row, _cell);
            if (row < _row + rowSpan && _cell < cell) {
              int colSpan = getColSpan(_row, _cell);
              result -= colSpan;
            }
          }
        }
      }
    }
    return result;
  }
}