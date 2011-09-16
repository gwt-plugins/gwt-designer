/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.gxt.model.layout.table;

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ContainerInfo;
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
 * @coverage ExtGWT.model.layout
 */
final class TableLayoutIntervalsSupport {
  private final TableLayoutInfo layout;
  private final ContainerInfo container;
  private final GwtState state;
  private final DOMUtils dom;
  private final Interval[] m_columnIntervals;
  private final Interval[] m_rowIntervals;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableLayoutIntervalsSupport(TableLayoutInfo layout) throws Exception {
    this.layout = layout;
    container = layout.getContainer();
    state = container.getState();
    dom = container.getDOMUtils();
    //
    m_rowIntervals = new Interval[layout.getRows().size()];
    m_columnIntervals = new Interval[layout.getColumns().size()];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Interval[] getColumnIntervals() {
    return m_columnIntervals;
  }

  public Interval[] getRowIntervals() {
    return m_rowIntervals;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void fetch() throws Exception {
    Map<Interval, Interval> spannedColumnIntervals = Maps.newHashMap();
    Map<Interval, Interval> spannedRowIntervals = Maps.newHashMap();
    for (WidgetInfo widget : layout.getWidgets()) {
      TableDataInfo data = TableLayoutInfo.getTableData(widget);
      int column = data.getColumn();
      int row = data.getRow();
      int colSpan = data.getColSpan();
      int rowSpan = data.getRowSpan();
      Object widgetElement = widget.getElement();
      Object td = dom.getParent(widgetElement);
      // prepare row interval
      if (m_rowIntervals[row] == null) {
        Object trElement = dom.getParent(td);
        Rectangle trBounds = state.getAbsoluteBounds(trElement);
        container.absoluteToModel(trBounds);
        Interval trInterval = new Interval(trBounds.y, trBounds.height);
        if (rowSpan == 1) {
          m_rowIntervals[row] = trInterval;
        } else {
          Rectangle tdBounds = state.getAbsoluteBounds(td);
          Interval spannedInterval = new Interval(trBounds.y, tdBounds.height);
          spannedRowIntervals.put(new Interval(row, rowSpan), spannedInterval);
        }
      }
      // prepare column interval
      if (m_columnIntervals[column] == null) {
        Rectangle tdBounds = state.getAbsoluteBounds(td);
        container.absoluteToModel(tdBounds);
        Interval columnInterval = new Interval(tdBounds.x, tdBounds.width);
        if (colSpan == 1) {
          m_columnIntervals[column] = columnInterval;
        } else {
          spannedColumnIntervals.put(new Interval(column, colSpan), columnInterval);
        }
      }
      //Object widgetElement = dom.getElementById(Container_Info.getID(widget));
    }
    // fix spanned columns/rows
    fetchCells_fixSpannedColumns(spannedColumnIntervals);
    fetchCells_fixSpannedRows(spannedRowIntervals);
    // fill empty columns
    {
      int lastIntervalEnd = 0;
      for (int i = 0; i < m_columnIntervals.length; i++) {
        if (m_columnIntervals[i] != null) {
          lastIntervalEnd = m_columnIntervals[i].end();
        } else {
          Interval in = new Interval(lastIntervalEnd, TableLayoutInfo.E_WIDTH);
          m_columnIntervals[i] = in;
          lastIntervalEnd = in.end();
        }
      }
    }
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
    for (int column = 0; column < m_columnIntervals.length; column++) {
      Interval rowInterval = m_columnIntervals[column];
      if (rowInterval == null) {
        for (Map.Entry<Interval, Interval> spanEntry : spannedColumnIntervals.entrySet()) {
          if (spanEntry.getKey().contains(column)) {
            int x = spanEntry.getValue().begin;
            int width = spanEntry.getValue().length / spanEntry.getKey().length;
            for (int _column = 0; _column < spanEntry.getKey().length; _column++) {
              m_columnIntervals[column + _column] = new Interval(x, width);
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
    for (int row = 0; row < m_rowIntervals.length; row++) {
      Interval rowInterval = m_rowIntervals[row];
      if (rowInterval == null) {
        for (Map.Entry<Interval, Interval> spanEntry : spannedRowIntervals.entrySet()) {
          if (spanEntry.getKey().contains(row)) {
            int y = spanEntry.getValue().begin;
            int height = spanEntry.getValue().length / spanEntry.getKey().length;
            for (int _row = 0; _row < spanEntry.getKey().length; _row++) {
              m_rowIntervals[row + _row] = new Interval(y, height);
              y += height;
            }
          }
        }
      }
    }
  }
}