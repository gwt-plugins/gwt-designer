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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.gxt.Activator;
import com.google.gdt.eclipse.designer.gxt.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.gxt.model.layout.assistant.TableLayoutAssistant;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildGraphical;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildTree;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.variable.EmptyInvocationVariableSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import java.util.List;
import java.util.Set;

/**
 * Model for <code>com.extjs.gxt.ui.client.widget.layout.TableLayout</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public class TableLayoutInfo extends LayoutInfo {
  public static final int E_WIDTH = 25;
  public static final int E_HEIGHT = 25;
  static final String E_WIDTH_PX = E_WIDTH + "px";
  static final String E_HEIGHT_PX = E_HEIGHT + "px";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    dontShowFillers();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initializeLayoutAssistant() {
    new TableLayoutAssistant(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasts
  //
  ////////////////////////////////////////////////////////////////////////////
  private void dontShowFillers() {
    addBroadcastListener(new ObjectInfoChildTree() {
      public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
        handlePresentationChild(object, visible);
      }
    });
    addBroadcastListener(new ObjectInfoChildGraphical() {
      public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
        handlePresentationChild(object, visible);
      }
    });
  }

  private void handlePresentationChild(ObjectInfo object, boolean[] visible) {
    if (object instanceof WidgetInfo && object.getParent() == getContainer()) {
      visible[0] &= !isFiller((WidgetInfo) object);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Components/constraints
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_replaceWithFillers = true;
  private final boolean m_removeEmptyColumnsRows = true;
  private TableDataInfo m_removingTableData;

  @Override
  protected void onWidgetRemoveBefore(WidgetInfo widget) throws Exception {
    // remember TableLayoutData_Info for using later in "remove after"
    m_removingTableData = getTableData(widget);
    // continue
    super.onWidgetRemoveBefore(widget);
  }

  @Override
  protected void onWidgetRemoveAfter(WidgetInfo widget) throws Exception {
    // ignore, no need to update layout, it will be deleted in any case
    if (getContainer().isDeleting()) {
      return;
    }
    // replace widget with fillers
    if (m_replaceWithFillers && !isFiller(widget)) {
      // replace with fillers
      {
        TableDataInfo tableData = m_removingTableData;
        for (int x = tableData.x; x < tableData.x + tableData.width; x++) {
          for (int y = tableData.y; y < tableData.y + tableData.height; y++) {
            addFiller(x, y);
          }
        }
      }
      // delete empty columns/rows
      if (m_removeEmptyColumnsRows) {
        deleteEmptyColumnsRows(m_removingTableData);
      }
      m_removingTableData = null;
    }
    // continue
    super.onWidgetRemoveAfter(widget);
  }

  /**
   * @return {@link TableDataInfo} association with given {@link WidgetInfo}.
   */
  public static TableDataInfo getTableData(WidgetInfo widget) {
    return (TableDataInfo) getLayoutData(widget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void onDelete() throws Exception {
    // delete filler's
    for (WidgetInfo widget : getWidgets()) {
      if (isFiller(widget)) {
        widget.delete();
      }
    }
    // delete other
    super.onDelete();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    m_gridInfo = null;
    super.refresh_dispose();
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    if (!GlobalState.isParsing()) {
      fetchDimensions();
      createColumnsRows();
      ensureReasonableSizeForEmptyDimensions();
    }
  }

  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
  }

  private void ensureReasonableSizeForEmptyDimensions() throws Exception {
    List<WidgetInfo> widgets = getWidgets();
    int columnCount = (Integer) getPropertyByTitle("columns").getValue();
    int rowCount = (int) Math.ceil((double) widgets.size() / columnCount);
    for (int column = 0; column < columnCount; column++) {
      if (isEmptyColumn(column)) {
        Object widgetObject;
        if (column < widgets.size()) {
          widgetObject = widgets.get(column).getObject();
        } else {
          widgetObject =
              JavaInfoUtils.executeScript(this, CodeUtils.getSource(
                  "filler = new com.extjs.gxt.ui.client.widget.Text();",
                  "model.container.object.add(filler);",
                  "return filler;"));
        }
        ReflectionUtils.invokeMethod(widgetObject, "setWidth(java.lang.String)", E_WIDTH_PX);
      }
    }
    for (int row = 0; row < rowCount; row++) {
      if (isEmptyRow(row)) {
        WidgetInfo widget = widgets.get(row * columnCount);
        ReflectionUtils.invokeMethod(widget.getObject(), "setHeight(java.lang.String)", E_HEIGHT_PX);
      }
    }
    ReflectionUtils.invokeMethod(getContainer().getObject(), "doLayout()");
    getContainer().getState().runMessagesLoop();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dimensions operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Removes empty (only with fillers) columns/rows.
   */
  void deleteEmptyColumnsRows(TableDataInfo removingData) throws Exception {
    deleteEmptyColumns(removingData);
    deleteEmptyRows(removingData);
  }

  /**
   * Deletes empty (only with fillers) columns.
   */
  private void deleteEmptyColumns(TableDataInfo removingData) throws Exception {
    WidgetInfo[][] grid = getWidgetsGrid();
    boolean deleteOnlyIfIsRemovingColumn = false;
    for (int column = grid[0].length - 1; column >= 0; column--) {
      boolean isRemovingColumn =
          removingData != null
              && removingData.x <= column
              && column < removingData.x + removingData.width;
      // check if empty
      boolean isEmpty = true;
      for (int row = 0; row < grid.length; row++) {
        WidgetInfo widget = grid[row][column];
        isEmpty &= isFiller(widget);
      }
      // delete if empty
      if (isEmpty && (!deleteOnlyIfIsRemovingColumn || isRemovingColumn)) {
        command_deleteColumn(column, false);
      } else {
        deleteOnlyIfIsRemovingColumn = true;
      }
    }
  }

  /**
   * Deletes empty (only with fillers) rows.
   */
  private void deleteEmptyRows(TableDataInfo removingData) throws Exception {
    WidgetInfo[][] grid = getWidgetsGrid();
    boolean deleteOnlyIfIsRemovingRow = false;
    for (int row = grid.length - 1; row >= 0; row--) {
      boolean isRemovingRow =
          removingData != null
              && removingData.y <= row
              && row < removingData.y + removingData.height;
      // check if empty
      boolean isEmpty = true;
      for (int column = 0; column < grid[row].length; column++) {
        WidgetInfo widget = grid[row][column];
        isEmpty &= isFiller(widget);
      }
      // delete if empty
      if (isEmpty && (!deleteOnlyIfIsRemovingRow || isRemovingRow)) {
        command_deleteRow(row, false);
      } else {
        deleteOnlyIfIsRemovingRow = true;
      }
    }
  }

  /**
   * Deletes column with given index and all widgets that located in this column.
   */
  public void command_deleteColumn(int column, boolean deleteEmptyRows) throws Exception {
    int columnCount = getWidgetsGridSize().width;
    // update TableData, delete widgets
    m_replaceWithFillers = false;
    try {
      for (WidgetInfo widget : getWidgets()) {
        TableDataInfo tableData = getTableData(widget);
        //
        if (tableData.x == column) {
          widget.delete();
        } else if (tableData.x > column) {
          tableData.x--;
        } else if (tableData.x + tableData.width > column) {
          tableData.setColSpan(tableData.width - 1);
        }
      }
    } finally {
      m_replaceWithFillers = true;
    }
    // update count
    if (columnCount >= 2) {
      getPropertyByTitle("columns").setValue(columnCount - 1);
    }
    // it is possible, that we have now empty rows, so delete them too
    if (deleteEmptyRows) {
      deleteEmptyRows(null);
    }
  }

  /**
   * Deletes row with given index and all widgets that located in this row.
   */
  public void command_deleteRow(int row, boolean deleteEmptyColumn) throws Exception {
    // update TableData, delete widgets
    m_replaceWithFillers = false;
    try {
      for (WidgetInfo widget : getWidgets()) {
        TableDataInfo tableData = getTableData(widget);
        //
        if (tableData.y == row) {
          widget.delete();
        } else if (tableData.y > row) {
          tableData.y--;
        } else if (tableData.y + tableData.height > row) {
          tableData.setRowSpan(tableData.height - 1);
        }
      }
    } finally {
      m_replaceWithFillers = true;
    }
    // it is possible, that we have now empty columns, so delete them too
    if (deleteEmptyColumn) {
      deleteEmptyColumns(null);
    }
  }

  /**
   * Moves column from/to given index.
   */
  public void command_MOVE_COLUMN(int fromIndex, int toIndex) throws Exception {
    fixGrid();
    // move column in columns list
    {
      getColumns(); // kick to initialize columns
      ColumnInfo column = m_columns.remove(fromIndex);
      if (fromIndex < toIndex) {
        m_columns.add(toIndex - 1, column);
      } else {
        m_columns.add(toIndex, column);
      }
    }
    // prepare new column
    prepareCell(toIndex, true, -1, false);
    if (toIndex < fromIndex) {
      fromIndex++;
    }
    // move children
    for (WidgetInfo widget : getWidgets()) {
      if (!isFiller(widget)) {
        TableDataInfo tableData = getTableData(widget);
        if (tableData.x == fromIndex) {
          command_setCells(widget, new Rectangle(toIndex, tableData.y, 1, tableData.height), true);
        }
      }
    }
    // delete old column
    command_deleteColumn(fromIndex, false);
    deleteEmptyColumnsRows(null);
  }

  /**
   * Moves row from/to given index.
   */
  public void command_MOVE_ROW(int fromIndex, int toIndex) throws Exception {
    fixGrid();
    // move row in rows list
    {
      getRows(); // kick to initialize rows
      RowInfo row = m_rows.remove(fromIndex);
      if (fromIndex < toIndex) {
        m_rows.add(toIndex - 1, row);
      } else {
        m_rows.add(toIndex, row);
      }
    }
    // prepare new row
    prepareCell(-1, false, toIndex, true);
    if (toIndex < fromIndex) {
      fromIndex++;
    }
    // move children
    for (WidgetInfo widget : getWidgets()) {
      if (!isFiller(widget)) {
        TableDataInfo tableData = getTableData(widget);
        if (tableData.y == fromIndex) {
          command_setCells(widget, new Rectangle(tableData.x, toIndex, tableData.width, 1), true);
        }
      }
    }
    // delete old row
    command_deleteRow(fromIndex, false);
    deleteEmptyColumnsRows(null);
  }

  /**
   * If there are components that span multiple columns/rows, and no other "real" components in
   * these columns/rows, then removes these excess columns/rows.
   */
  public void command_normalizeSpanning() throws Exception {
    Dimension gridSize = getWidgetsGridSize();
    boolean[] filledColumns = new boolean[gridSize.width];
    boolean[] filledRows = new boolean[gridSize.height];
    for (WidgetInfo widget : getWidgets()) {
      if (!isFiller(widget)) {
        TableDataInfo tableData = getTableData(widget);
        filledColumns[tableData.x] = true;
        filledRows[tableData.y] = true;
      }
    }
    // remove empty columns
    for (int column = filledColumns.length - 1; column >= 0; column--) {
      if (!filledColumns[column]) {
        command_deleteColumn(column, false);
      }
    }
    // remove empty rows
    for (int row = filledRows.length - 1; row >= 0; row--) {
      if (!filledRows[row]) {
        command_deleteRow(row, false);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dimensions
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_columnCount;
  private int m_rowCount;
  private final List<ColumnInfo> m_columns = Lists.newArrayList();
  private final List<RowInfo> m_rows = Lists.newArrayList();

  private void fetchDimensions() throws Exception {
    TableLayoutDimensionsSupport support = new TableLayoutDimensionsSupport(this, true);
    support.initialize();
    m_columnCount = support.getColumnCount();
    m_rowCount = support.getRowCount();
  }

  /**
   * Fills {@link #m_columns} and {@link #m_rows} collections.
   */
  private void createColumnsRows() {
    m_columns.clear();
    m_rows.clear();
    // add columns
    for (int i = 0; i < m_columnCount; i++) {
      m_columns.add(new ColumnInfo(this));
    }
    // add rows
    for (int i = 0; i < m_rowCount; i++) {
      m_rows.add(new RowInfo(this));
    }
  }

  /**
   * @return the list of all {@link ColumnInfo}'s.
   */
  public final List<ColumnInfo> getColumns() {
    return m_columns;
  }

  /**
   * @return the list of all {@link RowInfo}'s.
   */
  public final List<RowInfo> getRows() {
    return m_rows;
  }

  /**
   * @return <code>true</code> if given column is empty, i.e. has no components that start in it.
   */
  boolean isEmptyColumn(final int column) {
    final boolean[] isEmpty = new boolean[]{true};
    visitWidgets(new WidgetVisitor() {
      public void visit(WidgetInfo component, TableDataInfo cell) throws Exception {
        if (cell.x == column) {
          isEmpty[0] &= isFiller(component);
        }
      }
    });
    return isEmpty[0];
  }

  /**
   * @return <code>true</code> if given row is empty, i.e. has no components that start in it.
   */
  boolean isEmptyRow(final int row) {
    final boolean[] isEmpty = new boolean[]{true};
    visitWidgets(new WidgetVisitor() {
      public void visit(WidgetInfo component, TableDataInfo cell) throws Exception {
        if (cell.y == row) {
          isEmpty[0] &= isFiller(component);
        }
      }
    });
    return isEmpty[0];
  }

  /**
   * @return <code>true</code> if given {@link WidgetInfo} is filler.
   */
  public static boolean isFiller(WidgetInfo widget) {
    if (widget.getVariableSupport() instanceof EmptyVariableSupport
        && widget.getCreationSupport() instanceof ConstructorCreationSupport) {
      return AstNodeUtils.isCreation(
          widget.getCreationSupport().getNode(),
          "com.extjs.gxt.ui.client.widget.Text",
          "<init>()");
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link WidgetInfo} in given cell.
   * 
   * @param newWidget
   *          the new {@link WidgetInfo} to create.
   * @param column
   *          the column (0 based).
   * @param row
   *          the row (0 based).
   */
  public void command_CREATE(WidgetInfo newWidget,
      int column,
      boolean insertColumn,
      int row,
      boolean insertRow) throws Exception {
    startEdit();
    try {
      command_CREATE(newWidget, null);
      // move to required cell
      prepareCell(column, insertColumn, row, insertRow);
      command_setCells(newWidget, new Rectangle(column, row, 1, 1), false);
    } finally {
      endEdit();
    }
  }

  /**
   * Moves existing {@link WidgetInfo} into new cell.
   */
  public void command_MOVE(WidgetInfo widget,
      int column,
      boolean insertColumn,
      int row,
      boolean insertRow) throws Exception {
    startEdit();
    try {
      prepareCell(column, insertColumn, row, insertRow);
      command_setCells(widget, new Rectangle(column, row, 1, 1), true);
      deleteEmptyColumnsRows(null);
    } finally {
      endEdit();
    }
  }

  /**
   * Adds {@link WidgetInfo} from other parent into cell.
   */
  public void command_ADD(WidgetInfo widget,
      int column,
      boolean insertColumn,
      int row,
      boolean insertRow) throws Exception {
    startEdit();
    try {
      command_MOVE(widget, null);
      // move to required cell
      prepareCell(column, insertColumn, row, insertRow);
      command_setCells(widget, new Rectangle(column, row, 1, 1), false);
    } finally {
      endEdit();
    }
  }

  /**
   * Prepares cell with given column/row - inserts/appends columns/rows if necessary.
   */
  void prepareCell(int column, boolean insertColumn, int row, boolean insertRow) throws Exception {
    // prepare count of columns/rows
    int columnCount;
    int rowCount;
    {
      Dimension gridSize = getWidgetsGridSize();
      columnCount = gridSize.width;
      rowCount = gridSize.height;
    }
    // append
    {
      int newColumnCount = Math.max(columnCount, 1 + column);
      int newRowCount = Math.max(rowCount, 1 + row);
      // append rows
      for (int newRow = rowCount; newRow <= row; newRow++) {
        for (int columnIndex = 0; columnIndex < newColumnCount; columnIndex++) {
          addFiller(columnIndex, newRow);
        }
      }
      // append columns
      getPropertyByTitle("columns").setValue(newColumnCount);
      for (int newColumn = columnCount; newColumn <= column; newColumn++) {
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
          addFiller(newColumn, rowIndex);
        }
      }
      // set new count of columns/rows
      columnCount = newColumnCount;
      rowCount = newRowCount;
    }
    // insert row
    if (insertRow) {
      rowCount++;
      // update TableData for all widgets
      boolean[] columnsToIgnore = new boolean[columnCount];
      for (WidgetInfo widget : getWidgets()) {
        TableDataInfo tableData = getTableData(widget);
        //
        if (tableData.y >= row) {
          tableData.y++;
        } else if (tableData.y + tableData.height > row) {
          tableData.setRowSpan(tableData.height + 1);
          for (int i = tableData.x; i < tableData.x + tableData.width; i++) {
            columnsToIgnore[i] = true;
          }
        }
      }
      // add fillers for new row
      for (int i = 0; i < columnCount; i++) {
        if (!columnsToIgnore[i]) {
          addFiller(i, row);
        }
      }
    }
    // insert column
    if (insertColumn) {
      // update TableData for all widgets
      boolean[] rowsToIgnore = new boolean[rowCount];
      for (WidgetInfo widget : getWidgets()) {
        TableDataInfo tableData = getTableData(widget);
        //
        if (tableData.x >= column) {
          tableData.x++;
        } else if (tableData.x + tableData.width > column) {
          tableData.setColSpan(tableData.width + 1);
          for (int i = tableData.y; i < tableData.y + tableData.height; i++) {
            rowsToIgnore[i] = true;
          }
        }
      }
      // insert fillers for new column
      getPropertyByTitle("columns").setValue(columnCount + 1);
      for (int i = 0; i < rowCount; i++) {
        if (!rowsToIgnore[i]) {
          addFiller(column, i);
        }
      }
    }
  }

  /**
   * Sets the cells occupied by given {@link WidgetInfo}.
   * 
   * @param forMove
   *          is <code>true</code> if we move widget and <code>false</code> if we set cells for
   *          newly added widget.
   */
  public void command_setCells(WidgetInfo widget, Rectangle cells, boolean forMove)
      throws Exception {
    TableDataInfo tableData = getTableData(widget);
    // prepare grid
    fixGrid();
    WidgetInfo[][] grid = getWidgetsGrid();
    Set<Point> cellsToAddFillers = Sets.newHashSet();
    Set<Point> cellsToRemoveFillers = Sets.newHashSet();
    // replace widget with fillers
    if (forMove) {
      for (int x = tableData.x; x < tableData.x + tableData.width; x++) {
        for (int y = tableData.y; y < tableData.y + tableData.height; y++) {
          Point cell = new Point(x, y);
          cellsToAddFillers.add(cell);
        }
      }
    }
    // remove fillers from occupied cells
    for (int x = cells.x; x < cells.right(); x++) {
      for (int y = cells.y; y < cells.bottom(); y++) {
        Point cell = new Point(x, y);
        cellsToAddFillers.remove(cell);
        if (isFiller(grid[y][x])) {
          cellsToRemoveFillers.add(cell);
        }
      }
    }
    // do edit operations
    startEdit();
    try {
      // move
      if (tableData.x != cells.x || tableData.y != cells.y) {
        // update TableData
        {
          tableData.x = cells.x;
          tableData.y = cells.y;
        }
        // move model
        {
          WidgetInfo reference = getReferenceWidget(cells.y, cells.x, widget);
          command_MOVE(widget, reference);
        }
      }
      // set span
      {
        tableData.setColSpan(cells.width);
        tableData.setRowSpan(cells.height);
      }
      // remove fillers
      for (Point cell : cellsToRemoveFillers) {
        WidgetInfo filler = grid[cell.y][cell.x];
        filler.delete();
      }
      // add fillers
      for (Point cell : cellsToAddFillers) {
        addFiller(cell.x, cell.y);
      }
    } finally {
      endEdit();
    }
  }

  /**
   * @return the {@link Point} with size of widgets grid.
   */
  private Dimension getWidgetsGridSize() {
    int columnCount = 0;
    int rowCount = 0;
    for (WidgetInfo widget : getWidgets()) {
      TableDataInfo tableData = getTableData(widget);
      //
      columnCount = Math.max(columnCount, tableData.x + tableData.width);
      rowCount = Math.max(rowCount, tableData.y + tableData.height);
    }
    // OK, we have grid
    return new Dimension(columnCount, rowCount);
  }

  /**
   * @return the double array of {@link WidgetInfo} where each element contains {@link WidgetInfo}
   *         that occupies this cell.
   */
  private WidgetInfo[][] getWidgetsGrid() throws Exception {
    Dimension gridSize = getWidgetsGridSize();
    // prepare empty grid
    WidgetInfo[][] grid;
    {
      grid = new WidgetInfo[gridSize.height][];
      for (int rowIndex = 0; rowIndex < grid.length; rowIndex++) {
        grid[rowIndex] = new WidgetInfo[gridSize.width];
      }
    }
    // fill grid
    for (WidgetInfo widget : getWidgets()) {
      // prepare cells
      Rectangle cells;
      {
        TableDataInfo tableData = getTableData(widget);
        cells = new Rectangle(tableData.x, tableData.y, tableData.width, tableData.height);
      }
      // fill grid cells
      for (int x = cells.x; x < cells.right(); x++) {
        for (int y = cells.y; y < cells.bottom(); y++) {
          // ignore newly added widgets without real cell
          if (x != -1 && y != -1) {
            grid[y][x] = widget;
          }
        }
      }
    }
    // OK, we have grid
    return grid;
  }

  /**
   * "Fixes" grid, i.e. ensures that all cells are filled (at least with fillers), even if this is
   * not strongly required by layout itself for final cells. We do this to avoid checks for
   * <code>null</code> in many places.
   */
  void fixGrid() throws Exception {
    WidgetInfo[][] grid = getWidgetsGrid();
    for (int row = 0; row < grid.length; row++) {
      for (int column = 0; column < grid[row].length; column++) {
        if (grid[row][column] == null) {
          addFiller(column, row);
        }
      }
    }
  }

  /**
   * @return the {@link WidgetInfo} that should be used as reference of adding into specified cell.
   * 
   * @param exclude
   *          the {@link WidgetInfo} that should not be checked, for example because we move it now
   */
  private WidgetInfo getReferenceWidget(int row, int column, WidgetInfo exclude) throws Exception {
    for (WidgetInfo widget : getWidgets()) {
      if (widget != exclude) {
        TableDataInfo tableData = getTableData(widget);
        if (tableData.y > row || tableData.y == row && tableData.x >= column) {
          return widget;
        }
      }
    }
    // no reference
    return null;
  }

  /**
   * Adds filler {@link WidgetInfo} into given cell.
   */
  private void addFiller(int column, int row) throws Exception {
    // prepare creation support
    ConstructorCreationSupport creationSupport = new ConstructorCreationSupport("empty", false);
    // prepare filler
    WidgetInfo filler =
        (WidgetInfo) JavaInfoUtils.createJavaInfo(
            getEditor(),
            "com.extjs.gxt.ui.client.widget.Text",
            creationSupport);
    // add filler
    WidgetInfo reference = getReferenceWidget(row, column, null);
    JavaInfoUtils.add(
        filler,
        new EmptyInvocationVariableSupport(filler, "%parent%.add(%child%)", 0),
        PureFlatStatementGenerator.INSTANCE,
        AssociationObjects.invocationChildNull(),
        getContainer(),
        reference);
    // set x/y for new filler
    TableDataInfo tableData = getTableData(filler);
    tableData.x = column;
    tableData.y = row;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Visitor for {@link WidgetInfo} and their {@link TableDataInfo}.
   */
  interface WidgetVisitor {
    void visit(WidgetInfo component, TableDataInfo constraints) throws Exception;
  }

  /**
   * Visits grid {@link WidgetInfo}'s of this {@link ContainerInfo}.
   */
  void visitWidgets(final WidgetVisitor visitor) {
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        for (WidgetInfo component : getWidgets()) {
          TableDataInfo cell = getTableData(component);
          visitor.visit(component, cell);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGridInfo support
  //
  ////////////////////////////////////////////////////////////////////////////
  private Interval[] m_columnIntervals;
  private Interval[] m_rowIntervals;
  private IGridInfo m_gridInfo;

  /**
   * @return the {@link IGridInfo} that describes this layout.
   */
  public IGridInfo getGridInfo() {
    if (m_gridInfo == null) {
      ExecutionUtils.runRethrow(new RunnableEx() {
        public void run() throws Exception {
          createGridInfo();
        }
      });
    }
    return m_gridInfo;
  }

  /**
   * Initializes {@link #m_gridInfo}.
   */
  private void createGridInfo() throws Exception {
    {
      TableLayoutIntervalsSupport intervalsSupport = new TableLayoutIntervalsSupport(this);
      intervalsSupport.fetch();
      m_columnIntervals = intervalsSupport.getColumnIntervals();
      m_rowIntervals = intervalsSupport.getRowIntervals();
    }
    m_gridInfo = new IGridInfo() {
      ////////////////////////////////////////////////////////////////////////////
      //
      // Dimensions
      //
      ////////////////////////////////////////////////////////////////////////////
      public int getColumnCount() {
        return m_columnIntervals.length;
      }

      public int getRowCount() {
        return m_rowIntervals.length;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Intervals
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
      // Cells
      //
      ////////////////////////////////////////////////////////////////////////////
      public Rectangle getComponentCells(IAbstractComponentInfo component) {
        Assert.instanceOf(WidgetInfo.class, component);
        TableDataInfo data = getTableData((WidgetInfo) component);
        return new Rectangle(data.getColumn(), data.getRow(), data.getColSpan(), data.getRowSpan());
      }

      public Rectangle getCellsRectangle(Rectangle cells) {
        int x = m_columnIntervals[cells.x].begin;
        int y = m_rowIntervals[cells.y].begin;
        int w = m_columnIntervals[cells.right() - 1].end() - x;
        int h = m_rowIntervals[cells.bottom() - 1].end() - y;
        return new Rectangle(x, y, w, h);
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Feedback
      //
      ////////////////////////////////////////////////////////////////////////////
      public boolean isRTL() {
        return false;
      }

      public Insets getInsets() {
        return new Insets();
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Virtual columns
      //
      ////////////////////////////////////////////////////////////////////////////
      public boolean hasVirtualColumns() {
        return true;
      }

      public int getVirtualColumnSize() {
        return E_WIDTH;
      }

      public int getVirtualColumnGap() {
        return 5;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Virtual columns
      //
      ////////////////////////////////////////////////////////////////////////////
      public boolean hasVirtualRows() {
        return true;
      }

      public int getVirtualRowSize() {
        return E_HEIGHT;
      }

      public int getVirtualRowGap() {
        return 5;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Checks
      //
      ////////////////////////////////////////////////////////////////////////////
      public IAbstractComponentInfo getOccupied(int column, int row) {
        for (WidgetInfo widget : getWidgets()) {
          TableDataInfo data = getTableData(widget);
          if (data.getColumn() <= column
              && column < data.getColumn() + data.getColSpan()
              && data.getRow() <= row
              && row < data.getRow() + data.getRowSpan()) {
            return isFiller(widget) ? null : widget;
          }
        }
        return null;
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Image} for {@link TableLayoutInfo}.
   */
  public static Image getImage(String path) {
    return Activator.getImage("info/layout/TableLayout/" + path);
  }

  /**
   * @return the {@link ImageDescriptor} for {@link TableLayoutInfo}.
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return Activator.getImageDescriptor("info/layout/TableLayout/" + path);
  }
}