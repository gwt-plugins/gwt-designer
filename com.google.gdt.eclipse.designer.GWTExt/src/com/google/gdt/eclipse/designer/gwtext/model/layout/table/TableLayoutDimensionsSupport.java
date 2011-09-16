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
package com.google.gdt.eclipse.designer.gwtext.model.layout.table;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Initializer for {@link TableLayoutDataInfo}.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model.layout
 */
final class TableLayoutDimensionsSupport {
  private final TableLayoutInfo layout;
  private final boolean initializeData;
  private final int columnCount;
  private final List<boolean[]> rows = Lists.newArrayList();
  private int row = 0;
  private int column = 0;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableLayoutDimensionsSupport(TableLayoutInfo layout, boolean initializeData)
      throws Exception {
    this.layout = layout;
    this.initializeData = initializeData;
    columnCount = (Integer) ReflectionUtils.invokeMethod(layout.getObject(), "getColumns()");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getColumnCount() {
    return rows.isEmpty() ? 0 : columnCount;
  }

  public int getRowCount() {
    return rows.size();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  public void initialize() throws Exception {
    for (WidgetInfo widget : layout.getContainer().getChildrenWidgets()) {
      TableLayoutDataInfo data = TableLayoutInfo.getTableData(widget);
      if (initializeData) {
        data.initializeSpans();
      }
      // initialize data
      findEmptyCell();
      if (initializeData) {
        data.setColumn(column);
        data.setRow(row);
      }
      // fill cells
      ensureRows(data);
      fillCells(data);
    }
  }

  private void findEmptyCell() {
    if (rows.size() <= row) {
      addRow();
    }
    // check for empty cell
    if (!rows.get(row)[column]) {
      return;
    }
    // next column
    column++;
    // if end of row, go next row
    if (column == columnCount) {
      row++;
      column = 0;
    }
    // recurse
    findEmptyCell();
  }

  private void ensureRows(TableLayoutDataInfo data) {
    while (rows.size() < row + data.getRowSpan()) {
      addRow();
    }
  }

  private void fillCells(TableLayoutDataInfo data) {
    for (int r = row; r < row + data.getRowSpan(); r++) {
      for (int c = column; c < column + data.getColSpan(); c++) {
        rows.get(r)[c] = true;
      }
    }
  }

  private void addRow() {
    rows.add(new boolean[columnCount]);
  }
}