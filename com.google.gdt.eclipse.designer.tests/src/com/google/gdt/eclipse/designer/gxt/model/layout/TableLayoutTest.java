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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.layout.table.ColumnInfo;
import com.google.gdt.eclipse.designer.gxt.model.layout.table.RowInfo;
import com.google.gdt.eclipse.designer.gxt.model.layout.table.TableDataInfo;
import com.google.gdt.eclipse.designer.gxt.model.layout.table.TableLayoutInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.IntValue;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link TableLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class TableLayoutTest extends GxtModelTest {
  private static final int A_HEIGHT = Expectations.get(21, new IntValue[]{
      new IntValue("flanker-win", 18),
      new IntValue("kosta-home", 32),
      new IntValue("scheglov-win", 21)});
  private static final int A_WIDTH = Expectations.get(11, new IntValue[]{
      new IntValue("kosta-home", 18),
      new IntValue("scheglov-win", 11)});
  private static final int E_WIDTH = TableLayoutInfo.E_WIDTH;
  private static final int E_HEIGHT = TableLayoutInfo.E_HEIGHT;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_parse() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Text label = new Text();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    assertInstanceOf(TableLayoutInfo.class, container.getLayout());
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new TableLayout(1))/ /add(label)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.TableLayout} {empty} {/setLayout(new TableLayout(1))/}",
        "  {new: com.extjs.gxt.ui.client.widget.Text} {local-unique: label} {/new Text()/ /add(label)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.TableData} {virtual-layout-data} {}");
    WidgetInfo label = container.getWidgets().get(0);
    TableDataInfo layoutData = TableLayoutInfo.getTableData(label);
    assertNotNull(layoutData);
  }

  public void test_parseEmpty() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "  }",
            "}");
    panel.refresh();
    //
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    IGridInfo gridInfo = layout.getGridInfo();
    assertThat(gridInfo.getRowCount()).isZero();
    assertThat(gridInfo.getRowIntervals()).isEmpty();
    assertThat(gridInfo.getColumnCount()).isZero();
    assertThat(gridInfo.getColumnIntervals()).isEmpty();
  }

  public void test_parseEmpty2() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "  }",
            "}");
    panel.refresh();
    //
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    IGridInfo gridInfo = layout.getGridInfo();
    assertThat(gridInfo.getRowCount()).isZero();
    assertThat(gridInfo.getRowIntervals()).isEmpty();
    assertThat(gridInfo.getColumnCount()).isZero();
    assertThat(gridInfo.getColumnIntervals()).isEmpty();
  }

  /**
   * Test for {@link TableLayoutInfo#isFiller(WidgetInfo)}.
   */
  public void test_isFiller() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    add(new Text());",
            "    add(new Text('test'));",
            "    {",
            "      Text text = new Text();",
            "      add(text);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    List<WidgetInfo> widgets = panel.getWidgets();
    // no variable, no text -> filler
    {
      WidgetInfo widget = widgets.get(0);
      assertTrue(TableLayoutInfo.isFiller(widget));
    }
    // no variable, has text -> not filler
    {
      WidgetInfo widget = widgets.get(1);
      assertFalse(TableLayoutInfo.isFiller(widget));
    }
    // has variable -> not filler
    {
      WidgetInfo widget = widgets.get(2);
      assertFalse(TableLayoutInfo.isFiller(widget));
    }
  }

  /**
   * Fillers should be filtered out from presentation children.
   */
  public void test_excludeFillersFromPresentationChildren() throws Exception {
    LayoutContainerInfo shell =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "  }",
            "}");
    shell.refresh();
    assertThat(shell.getWidgets()).hasSize(2);
    WidgetInfo button = shell.getWidgets().get(0);
    WidgetInfo filler = shell.getWidgets().get(1);
    //
    IObjectPresentation presentation = shell.getPresentation();
    {
      List<ObjectInfo> presentationChildren = presentation.getChildrenTree();
      assertTrue(presentationChildren.contains(button));
      assertFalse(presentationChildren.contains(filler));
    }
    {
      List<ObjectInfo> presentationChildren = presentation.getChildrenGraphical();
      assertTrue(presentationChildren.contains(button));
      assertFalse(presentationChildren.contains(filler));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for initializing {@link TableDataInfo}.
   */
  public void test_initializeTable_noSpans() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Text label_1 = new Text();",
            "      add(label_1);",
            "    }",
            "    {",
            "      Text label_2 = new Text();",
            "      add(label_2);",
            "    }",
            "    {",
            "      Text label_3 = new Text();",
            "      add(label_3);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertCells(panel.getWidgets().get(0), 0, 0, 1, 1);
    assertCells(panel.getWidgets().get(1), 1, 0, 1, 1);
    assertCells(panel.getWidgets().get(2), 0, 1, 1, 1);
    // refresh() second time, just to covert double initialization, no changes expected
    panel.refresh();
    assertCells(panel.getWidgets().get(0), 0, 0, 1, 1);
    assertCells(panel.getWidgets().get(1), 1, 0, 1, 1);
    assertCells(panel.getWidgets().get(2), 0, 1, 1, 1);
  }

  /**
   * Test for initializing {@link TableDataInfo}.
   */
  public void test_initializeTable_colSpan() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Text label_1 = new Text();",
            "      TableData tableData = new TableData();",
            "      tableData.setColspan(2);",
            "      add(label_1, tableData);",
            "    }",
            "    {",
            "      Text label_2 = new Text();",
            "      add(label_2);",
            "    }",
            "    {",
            "      Text label_3 = new Text();",
            "      add(label_3);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertCells(panel.getWidgets().get(0), 0, 0, 2, 1);
    assertCells(panel.getWidgets().get(1), 0, 1, 1, 1);
    assertCells(panel.getWidgets().get(2), 1, 1, 1, 1);
  }

  /**
   * Test for initializing {@link TableDataInfo}.
   */
  public void test_initializeTable_rowSpan() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Text label_1 = new Text();",
            "      TableData tableData = new TableData();",
            "      tableData.setRowspan(2);",
            "      add(label_1, tableData);",
            "    }",
            "    {",
            "      Text label_2 = new Text();",
            "      add(label_2);",
            "    }",
            "    {",
            "      Text label_3 = new Text();",
            "      add(label_3);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertCells(panel.getWidgets().get(0), 0, 0, 1, 2);
    assertCells(panel.getWidgets().get(1), 1, 0, 1, 1);
    assertCells(panel.getWidgets().get(2), 1, 1, 1, 1);
  }

  private static void assertCells(WidgetInfo widget, int column, int row, int colSpan, int rowSpan) {
    TableDataInfo data = TableLayoutInfo.getTableData(widget);
    assertEquals(column, data.getColumn());
    assertEquals(row, data.getRow());
    assertEquals(colSpan, data.getColSpan());
    assertEquals(rowSpan, data.getRowSpan());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGridInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_IGridInfo_noSpans() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Text label_1 = new Text('A');",
            "      add(label_1);",
            "    }",
            "    {",
            "      Text label_2 = new Text('AA');",
            "      add(label_2);",
            "    }",
            "    {",
            "      Text label_3 = new Text('A');",
            "      add(label_3);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    IGridInfo gridInfo = layout.getGridInfo();
    //
    assertEquals(2, gridInfo.getColumnCount());
    assertEquals(2, gridInfo.getRowCount());
    // getColumnIntervals()
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertThat(intervals).hasSize(2);
      assertEquals(0, intervals[0].begin);
      assertEquals(A_WIDTH, intervals[0].length);
      assertEquals(A_WIDTH, intervals[1].begin);
      assertEquals(A_WIDTH * 2, intervals[1].length);
    }
    // getRowIntervals()
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertThat(intervals).hasSize(2);
      assertEquals(0, intervals[0].begin);
      assertEquals(A_HEIGHT, intervals[0].length);
      assertEquals(A_HEIGHT, intervals[1].begin);
      assertEquals(A_HEIGHT, intervals[1].length);
    }
    // getCellsRectangle()
    {
      {
        Rectangle cells = new Rectangle(0, 0, 1, 1);
        Rectangle expected = new Rectangle(0, 0, A_WIDTH, A_HEIGHT);
        assertEquals(expected, gridInfo.getCellsRectangle(cells));
      }
      {
        Rectangle cells = new Rectangle(0, 0, 2, 1);
        Rectangle expected = new Rectangle(0, 0, A_WIDTH + 2 * A_WIDTH, A_HEIGHT);
        assertEquals(expected, gridInfo.getCellsRectangle(cells));
      }
      {
        Rectangle cells = new Rectangle(0, 0, 1, 2);
        Rectangle expected = new Rectangle(0, 0, A_WIDTH, A_HEIGHT + A_HEIGHT);
        assertEquals(expected, gridInfo.getCellsRectangle(cells));
      }
    }
    // getComponentCells()
    {
      assertEquals(new Rectangle(0, 0, 1, 1), gridInfo.getComponentCells(panel.getWidgets().get(0)));
      assertEquals(new Rectangle(1, 0, 1, 1), gridInfo.getComponentCells(panel.getWidgets().get(1)));
      assertEquals(new Rectangle(0, 1, 1, 1), gridInfo.getComponentCells(panel.getWidgets().get(2)));
    }
    // getOccupied()
    {
      assertSame(panel.getWidgets().get(0), gridInfo.getOccupied(0, 0));
      assertSame(panel.getWidgets().get(1), gridInfo.getOccupied(1, 0));
      assertSame(panel.getWidgets().get(2), gridInfo.getOccupied(0, 1));
      assertSame(null, gridInfo.getOccupied(1, 1));
    }
    // getInsets()
    {
      Insets insets = gridInfo.getInsets();
      assertEquals(new Insets(), insets);
    }
    // virtual columns
    {
      assertTrue(gridInfo.hasVirtualColumns());
      assertEquals(5, gridInfo.getVirtualColumnGap());
      assertEquals(E_WIDTH, gridInfo.getVirtualColumnSize());
    }
    // virtual rows
    {
      assertTrue(gridInfo.hasVirtualRows());
      assertEquals(5, gridInfo.getVirtualRowGap());
      assertEquals(E_HEIGHT, gridInfo.getVirtualRowSize());
    }
  }

  /**
   * If <code>TableLayout</code> has more columns than widgets, empty column intervals still should
   * has reasonable width.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43657
   */
  public void test_IGridInfo_onlyOneColumnFilled() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Text text = new Text('A');",
            "      add(text);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    IGridInfo gridInfo = layout.getGridInfo();
    //
    assertEquals(2, gridInfo.getColumnCount());
    assertEquals(1, gridInfo.getRowCount());
    // getColumnIntervals()
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertThat(intervals).hasSize(2);
      assertEquals(0, intervals[0].begin);
      assertEquals(A_WIDTH, intervals[0].length);
      assertEquals(A_WIDTH, intervals[1].begin);
      assertEquals(E_WIDTH, intervals[1].length);
    }
    // getRowIntervals()
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertThat(intervals).hasSize(1);
      assertEquals(0, intervals[0].begin);
      assertEquals(A_HEIGHT, intervals[0].length);
    }
  }

  /**
   * Column/row intervals should use "model bounds", i.e. exclude "client area insets" of container.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43657
   */
  public void test_IGridInfo_useModelBounds_forIntervals() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends ContentPanel {",
            "  public Test() {",
            "    setHeading('Some title');",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Text text = new Text('A');",
            "      add(text);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    IGridInfo gridInfo = layout.getGridInfo();
    //
    assertEquals(1, gridInfo.getColumnCount());
    assertEquals(1, gridInfo.getRowCount());
    // getColumnIntervals()
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertThat(intervals).hasSize(1);
      assertEquals(0, intervals[0].begin);
      assertEquals(A_WIDTH, intervals[0].length);
    }
    // getRowIntervals()
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertThat(intervals).hasSize(1);
      assertEquals(0, intervals[0].begin);
      assertEquals(A_HEIGHT, intervals[0].length);
    }
  }

  public void test_IGridInfo_3columns_2rows() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(3));",
            "    {",
            "      Text label_1 = new Text('A');",
            "      add(label_1);",
            "    }",
            "    {",
            "      Text label_2 = new Text('AA');",
            "      add(label_2);",
            "    }",
            "    {",
            "      Text label_3 = new Text('A');",
            "      add(label_3);",
            "    }",
            "    {",
            "      Text label_4 = new Text('A');",
            "      add(label_4);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    IGridInfo gridInfo = layout.getGridInfo();
    //
    assertEquals(3, gridInfo.getColumnCount());
    assertEquals(2, gridInfo.getRowCount());
    // getColumnIntervals()
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertThat(intervals).hasSize(3);
    }
    // getRowIntervals()
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertThat(intervals).hasSize(2);
    }
  }

  public void test_IGridInfo_emptyCellIfFiller() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    IGridInfo gridInfo = layout.getGridInfo();
    WidgetInfo button = panel.getWidgets().get(0);
    //
    assertEquals(2, gridInfo.getColumnCount());
    assertEquals(1, gridInfo.getRowCount());
    assertSame(button, gridInfo.getOccupied(0, 0));
    assertSame(null, gridInfo.getOccupied(1, 0));
  }

  /**
   * When some columns/rows are empty, we should force them to have some reasonable size, so that
   * user will able to drop component into these empty cells.
   */
  public void test_IGridInfo_emptyColumnsRows() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    add(new Text());",
            "    add(new Text());",
            "    add(new Text());",
            "    {",
            "      Text label = new Text('A');",
            "      add(label);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    IGridInfo gridInfo = layout.getGridInfo();
    //
    assertEquals(2, gridInfo.getColumnCount());
    assertEquals(2, gridInfo.getRowCount());
    // getColumnIntervals()
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertThat(intervals).hasSize(2);
      assertEquals(0, intervals[0].begin);
      assertEquals(E_WIDTH, intervals[0].length);
      assertEquals(E_WIDTH, intervals[1].begin);
      assertEquals(A_WIDTH, intervals[1].length);
    }
    // getRowIntervals()
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertThat(intervals).hasSize(2);
      assertEquals(0, intervals[0].begin);
      assertThat(intervals[0].length).isGreaterThanOrEqualTo(25);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setCells()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setCells_horizontalSpan() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getWidgets().get(0);
    TableDataInfo tableData = TableLayoutInfo.getTableData(button);
    // check initial TableLayoutData
    {
      assertEquals(0, getInt(tableData, "x"));
      assertEquals(0, getInt(tableData, "y"));
      assertEquals(1, getInt(tableData, "width"));
      assertEquals(1, getInt(tableData, "height"));
    }
    // set horizontal span
    layout.command_setCells(button, new Rectangle(0, 0, 2, 1), true);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      TableData tableData = new TableData();",
        "      tableData.setColspan(2);",
        "      add(button, tableData);",
        "    }",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // check TableLayoutData
    {
      assertEquals(0, getInt(tableData, "x"));
      assertEquals(0, getInt(tableData, "y"));
      assertEquals(2, getInt(tableData, "width"));
      assertEquals(1, getInt(tableData, "height"));
    }
  }

  public void test_setCells_horizontalSpan2() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      TableData tableData = new TableData();",
            "      tableData.setColspan(2);",
            "      add(button, tableData);",
            "    }",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getWidgets().get(0);
    TableDataInfo tableData = TableLayoutInfo.getTableData(button);
    // check initial TableLayoutData
    {
      assertEquals(0, getInt(tableData, "x"));
      assertEquals(0, getInt(tableData, "y"));
      assertEquals(2, getInt(tableData, "width"));
      assertEquals(1, getInt(tableData, "height"));
    }
    // set horizontal span
    layout.command_setCells(button, new Rectangle(0, 0, 1, 1), true);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // check TableLayoutData
    {
      assertEquals(0, getInt(tableData, "x"));
      assertEquals(0, getInt(tableData, "y"));
      assertEquals(1, getInt(tableData, "width"));
      assertEquals(1, getInt(tableData, "height"));
    }
  }

  public void test_setCells_verticalSpan() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getWidgets().get(0);
    TableDataInfo tableData = TableLayoutInfo.getTableData(button);
    // check initial TableLayoutData
    {
      assertEquals(0, getInt(tableData, "x"));
      assertEquals(0, getInt(tableData, "y"));
      assertEquals(1, getInt(tableData, "width"));
      assertEquals(1, getInt(tableData, "height"));
    }
    // set horizontal span
    layout.command_setCells(button, new Rectangle(0, 0, 1, 2), true);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      TableData tableData = new TableData();",
        "      tableData.setRowspan(2);",
        "      add(button, tableData);",
        "    }",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // check TableLayoutData
    {
      assertEquals(0, getInt(tableData, "x"));
      assertEquals(0, getInt(tableData, "y"));
      assertEquals(1, getInt(tableData, "width"));
      assertEquals(2, getInt(tableData, "height"));
    }
  }

  public void test_setCells_verticalSpan2() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      TableData tableData = new TableData();",
            "      tableData.setRowspan(2);",
            "      add(button, tableData);",
            "    }",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getWidgets().get(0);
    TableDataInfo tableData = TableLayoutInfo.getTableData(button);
    // check initial TableLayoutData
    {
      assertEquals(0, getInt(tableData, "x"));
      assertEquals(0, getInt(tableData, "y"));
      assertEquals(1, getInt(tableData, "width"));
      assertEquals(2, getInt(tableData, "height"));
    }
    // set horizontal span
    layout.command_setCells(button, new Rectangle(0, 0, 1, 1), true);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // check TableLayoutData
    {
      assertEquals(0, getInt(tableData, "x"));
      assertEquals(0, getInt(tableData, "y"));
      assertEquals(1, getInt(tableData, "width"));
      assertEquals(1, getInt(tableData, "height"));
    }
  }

  public void test_setCells_move() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "    {",
            "      Button button = new Button('2');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getWidgets().get(2);
    //
    layout.command_setCells(button, new Rectangle(1, 0, 1, 1), true);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('2');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // check x/y for new filler
    {
      WidgetInfo filler = panel.getWidgets().get(2);
      TableDataInfo tableData = TableLayoutInfo.getTableData(filler);
      assertEquals(0, getInt(tableData, "x"));
      assertEquals(1, getInt(tableData, "y"));
      assertEquals(1, getInt(tableData, "width"));
      assertEquals(1, getInt(tableData, "height"));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When we delete {@link WidgetInfo}, it should be replaced with filler.
   */
  public void test_delete_replaceWithFillers() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "    {",
            "      Button button = new Button('2');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = panel.getWidgets().get(2);
    //
    button.delete();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('2');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * When we delete column, we should keep at least one column.
   */
  public void test_delete_keepOneColumn() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = panel.getWidgets().get(0);
    //
    button.delete();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "  }",
        "}");
  }

  public void test_delete_removeEmptyDimensions() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "    add(new Text());",
            "    add(new Text());",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = panel.getWidgets().get(5);
    // check initial location
    {
      TableDataInfo tableData = TableLayoutInfo.getTableData(button);
      assertEquals(1, getInt(tableData, "x"));
      assertEquals(2, getInt(tableData, "y"));
    }
    // delete "button"
    button.delete();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * There was problem with deleting {@link TableDataInfo}.
   */
  public void test_delete_TableData() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button();",
            "      add(button, new TableData());",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new TableLayout(1))/ /add(button, new TableData())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.TableLayout} {empty} {/setLayout(new TableLayout(1))/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button, new TableData())/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.TableData} {empty} {/add(button, new TableData())/}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getWidgets().get(0);
    // we can ask "grid", no exception
    layout.getGridInfo();
    // delete "explicit TableData"
    {
      TableDataInfo tableData = TableLayoutInfo.getTableData(button);
      assertEquals(0, tableData.getColumn());
      assertEquals(0, tableData.getRow());
      assertEquals(1, tableData.getColSpan());
      assertEquals(1, tableData.getRowSpan());
      tableData.delete();
    }
    // because of invalid values in "virtual TableData"...
    {
      TableDataInfo tableData = TableLayoutInfo.getTableData(button);
      assertEquals(0, tableData.getColumn());
      assertEquals(0, tableData.getRow());
      assertEquals(1, tableData.getColSpan());
      assertEquals(1, tableData.getRowSpan());
    }
    // ...this caused exception
    layout.getGridInfo();
  }

  /**
   * When we delete "ButtonGroup" with "Button" we should not try to update layout, because
   * "LayoutData" is already deleted, and in any case this work is useless.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43507
   */
  public void test_deleteButtonGroup() throws Exception {
    parseJavaInfo(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    ButtonGroup group = new ButtonGroup(1);",
        "    {",
        "      Button button = new Button();",
        "      group.add(button);",
        "    }",
        "    add(group);",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(group)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.button.ButtonGroup} {local-unique: group} {/new ButtonGroup(1)/ /group.add(button)/ /add(group)/}",
        "    {implicit-layout: com.extjs.gxt.ui.client.widget.layout.TableLayout} {implicit-layout} {}",
        "    {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /group.add(button)/}",
        "      {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.TableData} {virtual-layout-data} {}");
    // delete "group"
    ComponentInfo group = getJavaInfoByName("group");
    group.delete();
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {}",
        "  {implicit-layout: default} {implicit-layout} {}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_inEmptyCell() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    WidgetInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 0, false);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_refreshIntervals() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    // initial state
    {
      IGridInfo gridInfo = layout.getGridInfo();
      assertEquals(1, gridInfo.getColumnCount());
      assertEquals(1, gridInfo.getRowCount());
      assertThat(layout.getColumns()).hasSize(1);
      assertThat(layout.getRows()).hasSize(1);
    }
    // create Button
    WidgetInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, false);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "    add(new Text());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // new state
    {
      IGridInfo gridInfo = layout.getGridInfo();
      assertEquals(2, gridInfo.getColumnCount());
      assertEquals(2, gridInfo.getRowCount());
      assertThat(layout.getColumns()).hasSize(2);
      assertThat(layout.getRows()).hasSize(2);
    }
  }

  public void test_CREATE_insertRow() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text('A'));",
            "    add(new Text('B'));",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    WidgetInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, true);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Text('A'));",
        "    add(new Text());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "    add(new Text('B'));",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertColumn() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text('A'));",
            "    add(new Text('B'));",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    WidgetInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, true, 0, false);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(3));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "    add(new Text('A'));",
        "    add(new Text('B'));",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertColumnRow() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    WidgetInfo newButton = createButton();
    layout.command_CREATE(newButton, 0, true, 0, true);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // delete - should return in initial state
    newButton.delete();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_appendRow() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    WidgetInfo newButton = createButton();
    layout.command_CREATE(newButton, 0, false, 2, false);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_appendColumn() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    WidgetInfo newButton = createButton();
    layout.command_CREATE(newButton, 2, false, 0, false);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(3));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_appendColumnRow() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    WidgetInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, false);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "    add(new Text());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertColumnHorizontalSpan() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      TableData tableData = new TableData();",
            "      tableData.setColspan(2);",
            "      add(button, tableData);",
            "    }",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "    {",
            "      Button button = new Button('2');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    WidgetInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, true, 1, false);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(3));",
        "    {",
        "      Button button = new Button('0');",
        "      TableData tableData = new TableData();",
        "      tableData.setColspan(3);",
        "      add(button, tableData);",
        "    }",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('2');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertRowVerticalSpan() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      TableData tableData = new TableData();",
            "      tableData.setRowspan(2);",
            "      add(button, tableData);",
            "    }",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "    {",
            "      Button button = new Button('2');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    WidgetInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, true);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      TableData tableData = new TableData();",
        "      tableData.setRowspan(3);",
        "      add(button, tableData);",
        "    }",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('2');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for parsing "not balanced" {@link TableLayoutInfo} and adding into <code>null</code> cell.
   */
  public void test_CREATE_notBalanced() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    add(new Text());",
            "    add(new Text());",
            "    add(new Text());",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    WidgetInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, false);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Text());",
        "    add(new Text());",
        "    add(new Text());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_noReference() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    WidgetInfo newButton = createButton();
    layout.command_CREATE(newButton, 0, false, 0, false);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_1x1() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    WidgetInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, false);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Text());",
        "    add(new Text());",
        "    add(new Text());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dimension operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_columnAccess() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Text label_1 = new Text('A');",
            "      add(label_1);",
            "    }",
            "    {",
            "      Text label_2 = new Text('A');",
            "      add(label_2);",
            "    }",
            "    {",
            "      Text label_3 = new Text('A');",
            "      add(label_3);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    List<ColumnInfo> columns = layout.getColumns();
    assertThat(columns).hasSize(2);
    {
      ColumnInfo column = columns.get(0);
      assertEquals(0, column.getIndex());
      assertFalse(column.isEmpty());
    }
    {
      ColumnInfo column = columns.get(1);
      assertEquals(1, column.getIndex());
      assertFalse(column.isEmpty());
    }
  }

  public void test_columnAccess_isEmpty() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    List<ColumnInfo> columns = layout.getColumns();
    assertThat(columns).hasSize(2);
    {
      ColumnInfo column = columns.get(0);
      assertEquals(0, column.getIndex());
      assertFalse(column.isEmpty());
    }
    {
      ColumnInfo column = columns.get(1);
      assertEquals(1, column.getIndex());
      assertTrue(column.isEmpty());
    }
  }

  public void test_rowAccess() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Text label_1 = new Text('A');",
            "      add(label_1);",
            "    }",
            "    {",
            "      Text label_2 = new Text('A');",
            "      add(label_2);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    List<RowInfo> rows = layout.getRows();
    assertThat(rows).hasSize(2);
    {
      RowInfo row = rows.get(0);
      assertEquals(0, row.getIndex());
      assertFalse(row.isEmpty());
    }
    {
      RowInfo row = rows.get(1);
      assertEquals(1, row.getIndex());
      assertFalse(row.isEmpty());
    }
  }

  public void test_rowAccess_isEmpty() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    List<RowInfo> rows = layout.getRows();
    assertThat(rows).hasSize(2);
    {
      RowInfo row = rows.get(0);
      assertEquals(0, row.getIndex());
      assertFalse(row.isEmpty());
    }
    {
      RowInfo row = rows.get(1);
      assertEquals(1, row.getIndex());
      assertTrue(row.isEmpty());
    }
  }

  public void test_deleteColumn() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(3));",
            "    {",
            "      Button button = new Button('0');",
            "      TableData tableData = new TableData();",
            "      tableData.setColspan(3);",
            "      add(button, tableData);",
            "    }",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "    {",
            "      Button button = new Button('2');",
            "      add(button);",
            "    }",
            "    {",
            "      Button button = new Button('3');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    try {
      panel.startEdit();
      layout.command_deleteColumn(1, true);
    } finally {
      panel.endEdit();
    }
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      TableData tableData = new TableData();",
        "      tableData.setColspan(2);",
        "      add(button, tableData);",
        "    }",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('3');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteColumn_deleteAlsoEmptyRows() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    try {
      panel.startEdit();
      layout.command_deleteColumn(1, true);
    } finally {
      panel.endEdit();
    }
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link ColumnInfo#delete()}.
   */
  public void test_columnDelete() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(3));",
            "    {",
            "      Button button = new Button('0');",
            "      TableData tableData = new TableData();",
            "      tableData.setColspan(3);",
            "      add(button, tableData);",
            "    }",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "    {",
            "      Button button = new Button('2');",
            "      add(button);",
            "    }",
            "    {",
            "      Button button = new Button('3');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    try {
      panel.startEdit();
      layout.getColumns().get(1).delete();
    } finally {
      panel.endEdit();
    }
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      TableData tableData = new TableData();",
        "      tableData.setColspan(2);",
        "      add(button, tableData);",
        "    }",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('3');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteRow() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      TableData tableData = new TableData();",
            "      tableData.setRowspan(3);",
            "      add(button, tableData);",
            "    }",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "    {",
            "      Button button = new Button('2');",
            "      add(button);",
            "    }",
            "    {",
            "      Button button = new Button('3');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    try {
      panel.startEdit();
      layout.command_deleteRow(1, true);
    } finally {
      panel.endEdit();
    }
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      TableData tableData = new TableData();",
        "      tableData.setRowspan(2);",
        "      add(button, tableData);",
        "    }",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('3');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_deleteRow_deleteAlsoEmptyColumns() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    try {
      panel.startEdit();
      layout.command_deleteRow(1, true);
    } finally {
      panel.endEdit();
    }
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE COLUMN
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_COLUMN_before() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    try {
      layout.startEdit();
      layout.command_MOVE_COLUMN(1, 0);
    } finally {
      layout.endEdit();
    }
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "  }",
        "}");
  }

  public void test_MOVE_COLUMN_after() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    try {
      layout.startEdit();
      layout.command_MOVE_COLUMN(0, 2);
    } finally {
      layout.endEdit();
    }
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE ROW
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_ROW_before() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    try {
      layout.startEdit();
      layout.command_MOVE_ROW(1, 0);
    } finally {
      layout.endEdit();
    }
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "  }",
        "}");
  }

  public void test_MOVE_ROW_after() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    try {
      layout.startEdit();
      layout.command_MOVE_ROW(0, 2);
    } finally {
      layout.endEdit();
    }
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // command_normalizeSpanning()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link TableLayoutInfo#command_normalizeSpanning()}.<br>
   * Single control spanned on two columns.
   */
  public void test_normalizeSpanning_1() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      TableData tableData = new TableData();",
            "      tableData.setColspan(2);",
            "      add(button, tableData);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    layout.command_normalizeSpanning();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link TableLayoutInfo#command_normalizeSpanning()}.<br>
   * Single control spanned on two rows.
   */
  public void test_normalizeSpanning_2() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button('0');",
            "      TableData tableData = new TableData();",
            "      tableData.setRowspan(2);",
            "      add(button, tableData);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    layout.command_normalizeSpanning();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link TableLayoutInfo#command_normalizeSpanning()}.<br>
   * No normalize: each column/row has control.
   */
  public void test_normalizeSpanning_3() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      TableData tableData = new TableData();",
            "      tableData.setColspan(2);",
            "      add(button, tableData);",
            "    }",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    layout.command_normalizeSpanning();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      TableData tableData = new TableData();",
        "      tableData.setColspan(2);",
        "      add(button, tableData);",
        "    }",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link TableLayoutInfo#command_normalizeSpanning()}.<br>
   * Do normalize: no control for second column.
   */
  public void test_normalizeSpanning_4() throws Exception {
    final LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      TableData tableData = new TableData();",
            "      tableData.setColspan(2);",
            "      add(button, tableData);",
            "    }",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "  }",
            "}");
    panel.refresh();
    //
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
        layout.command_normalizeSpanning();
      }
    });
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Text());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "    {",
            "      Button button = new Button('2');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getWidgets().get(2);
    //
    layout.command_MOVE(button, 1, false, 0, false);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    add(new Text());",
        "    {",
        "      Button button = new Button('2');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_out() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      LayoutContainer composite = new LayoutContainer();",
            "      add(composite);",
            "    }",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    LayoutContainerInfo composite = (LayoutContainerInfo) panel.getWidgets().get(0);
    LayoutInfo layout = composite.getLayout();
    WidgetInfo button = panel.getWidgets().get(1);
    //
    layout.command_MOVE(button, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      LayoutContainer composite = new LayoutContainer();",
        "      {",
        "        Button button = new Button();",
        "        composite.add(button);",
        "      }",
        "      add(composite);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_error_1() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(4));",
            "    add(new Text());",
            "    add(new Text());",
            "    add(new Text());",
            "    add(new Text());",
            "    add(new Text());",
            "    add(new Text());",
            "    add(new Text());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getWidgets().get(7);
    //
    layout.command_MOVE(button, 1, false, 0, false);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Text());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_error_2() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(3));",
            "    add(new Text());",
            "    add(new Text());",
            "    add(new Text());",
            "    add(new Text());",
            "    add(new Text());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getWidgets().get(5);
    //
    layout.command_MOVE(button, 0, false, 0, false);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ADD() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      LayoutContainer composite = new LayoutContainer();",
            "      {",
            "        Button button = new Button();",
            "        composite.add(button);",
            "      }",
            "      add(composite);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = getJavaInfoByName("button");
    //
    layout.command_ADD(button, 0, false, 1, false);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      LayoutContainer composite = new LayoutContainer();",
        "      add(composite);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete layout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that when delete {@link TableLayoutInfo}, fillers are also removed, because there are not
   * controls that user wants.
   */
  public void test_DELETE_removeFillers() throws Exception {
    LayoutContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    add(new Text());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    // initially 2 controls - filler and Button
    assertThat(panel.getWidgets()).hasSize(2);
    // after delete - only Button
    layout.delete();
    assertThat(panel.getWidgets()).hasSize(1);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the <code>int</code> value of field with given name.
   */
  private static int getInt(TableDataInfo tableData, String fieldName) throws Exception {
    return ReflectionUtils.getFieldInt(tableData, fieldName);
  }
}