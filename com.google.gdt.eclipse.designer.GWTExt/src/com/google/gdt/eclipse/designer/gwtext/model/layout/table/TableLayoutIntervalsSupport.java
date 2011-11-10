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
package com.google.gdt.eclipse.designer.gwtext.model.layout.table;

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.ContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.DOMUtils;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;

import java.util.Map;

/**
 * Intervals/bounds fetcher for {@link TableLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model.layout
 */
final class TableLayoutIntervalsSupport {
  private final ContainerInfo container;
  private final GwtState state;
  private final DOMUtils dom;
  private final Interval[] columnIntervals;
  private final Interval[] rowIntervals;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableLayoutIntervalsSupport(TableLayoutInfo layout) throws Exception {
    container = layout.getContainer();
    state = container.getState();
    dom = container.getDOMUtils();
    //
    rowIntervals = new Interval[layout.getRows().size()];
    columnIntervals = new Interval[layout.getColumns().size()];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Interval[] getColumnIntervals() {
    return columnIntervals;
  }

  public Interval[] getRowIntervals() {
    return rowIntervals;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void fetch() throws Exception {
    Map<Interval, Interval> spannedColumnIntervals = Maps.newHashMap();
    Map<Interval, Interval> spannedRowIntervals = Maps.newHashMap();
    for (WidgetInfo widget : container.getChildrenWidgets()) {
      TableLayoutDataInfo data = TableLayoutInfo.getTableData(widget);
      int column = data.getColumn();
      int row = data.getRow();
      int colSpan = data.getColSpan();
      int rowSpan = data.getRowSpan();
      Object widgetElement = widget.getElement();
      Object td = dom.getParent(widgetElement);
      // prepare row interval
      if (rowIntervals[row] == null) {
        Object trElement = dom.getParent(td);
        Rectangle trBounds = state.getAbsoluteBounds(trElement);
        container.absoluteToRelative(trBounds);
        Interval trInterval = new Interval(trBounds.y, trBounds.height);
        if (rowSpan == 1) {
          rowIntervals[row] = trInterval;
        } else {
          Rectangle tdBounds = state.getAbsoluteBounds(td);
          Interval spannedInterval = new Interval(trBounds.y, tdBounds.height);
          spannedRowIntervals.put(new Interval(row, rowSpan), spannedInterval);
        }
      }
      // prepare column interval
      if (columnIntervals[column] == null) {
        Rectangle tdBounds = state.getAbsoluteBounds(td);
        container.absoluteToRelative(tdBounds);
        Interval columnInterval = new Interval(tdBounds.x, tdBounds.width);
        if (colSpan == 1) {
          columnIntervals[column] = columnInterval;
        } else {
          spannedColumnIntervals.put(new Interval(column, colSpan), columnInterval);
        }
      }
      //Object widgetElement = dom.getElementById(Container_Info.getID(widget));
    }
    // fix spanned columns/rows
    fetchCells_fixSpannedColumns(spannedColumnIntervals);
    fetchCells_fixSpannedRows(spannedRowIntervals);
    // if no rows, fill empty column intervals
    /*if (m_rowIntervals.length == 0) {
    	for (int i = 0; i < m_columnIntervals.length; i++) {
    		m_columnIntervals[i] = new Interval();
    	}
    }*/
    //
    /*System.out.println("rows: " + ArrayUtils.toString(m_rowIntervals));
    System.out.println("columns: " + ArrayUtils.toString(m_columnIntervals));
    System.out.println("spannedRowIntervals: " + spannedRowIntervals);
    System.out.println("spannedColumnIntervals: " + spannedColumnIntervals);*/
  }

  /**
   * It is possible that some columns don't have individual widgets in not spanned cells, so we can
   * not get exact columns intervals and have to approximate it.
   */
  private void fetchCells_fixSpannedColumns(Map<Interval, Interval> spannedColumnIntervals) {
    for (int column = 0; column < columnIntervals.length; column++) {
      Interval rowInterval = columnIntervals[column];
      if (rowInterval == null) {
        for (Map.Entry<Interval, Interval> spanEntry : spannedColumnIntervals.entrySet()) {
          if (spanEntry.getKey().contains(column)) {
            int x = spanEntry.getValue().begin;
            int width = spanEntry.getValue().length / spanEntry.getKey().length;
            for (int _column = 0; _column < spanEntry.getKey().length; _column++) {
              columnIntervals[column + _column] = new Interval(x, width);
              x += width;
            }
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
    for (int row = 0; row < rowIntervals.length; row++) {
      Interval rowInterval = rowIntervals[row];
      if (rowInterval == null) {
        for (Map.Entry<Interval, Interval> spanEntry : spannedRowIntervals.entrySet()) {
          if (spanEntry.getKey().contains(row)) {
            int y = spanEntry.getValue().begin;
            int height = spanEntry.getValue().length / spanEntry.getKey().length;
            for (int _row = 0; _row < spanEntry.getKey().length; _row++) {
              rowIntervals[row + _row] = new Interval(y, height);
              y += height;
            }
          }
        }
      }
    }
  }
}