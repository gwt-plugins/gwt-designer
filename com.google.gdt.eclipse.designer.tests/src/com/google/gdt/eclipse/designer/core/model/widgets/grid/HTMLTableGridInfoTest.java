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
package com.google.gdt.eclipse.designer.core.model.widgets.grid;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.FlexTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.GridInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.IntValue;

import org.apache.commons.lang.NotImplementedException;

import java.util.List;

/**
 * Tests for {@link HTMLTableInfo#getGridInfo()} implementation.
 * 
 * @author scheglov_ke
 */
public class HTMLTableGridInfoTest extends GwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constants
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Spacing between rows.
   */
  private static final int RS = 2;
  /**
   * Spacing between columns.
   */
  private static final int CS = 2;
  /**
   * Height of row with single "A" button.
   */
  private static final int RA = Expectations.get(24, new IntValue[]{
      new IntValue("kosta-home", 32),
      new IntValue("scheglov-win", 24),
      new IntValue("flanker-windows", 24)});
  /**
   * Width of column with single "A" button.
   */
  private static final int CA = Expectations.get(17, new IntValue[]{
      new IntValue("kosta-home", 21),
      new IntValue("scheglov-win", 31),
      new IntValue("flanker-windows", 17)});
  /**
   * Height of empty row.
   * <p>
   * NOTE: we set "20px" for TD content, but it seems that IE6 adds 3 more pixels (padding?).
   * <p>
   * Exact value in strict mode.
   */
  private static final int RE = 20;
  /**
   * Width of empty column.
   * <p>
   * NOTE: we set "20px" for TD content, but it seems that IE6 adds 2 more pixels (padding?).
   * <p>
   * Exact value in strict mode.
   */
  private static final int CE = 20;

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
  // General
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * <code>HTMLTable</code> always filled with rows/columns, so no virtual intervals.
   */
  public void test_noVirtualIntervals() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(1, 2);",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    // check IGridInfo
    IGridInfo gridInfo = panel.getGridInfo();
    assertFalse(gridInfo.hasVirtualColumns());
    assertFalse(gridInfo.hasVirtualRows());
    try {
      gridInfo.getVirtualRowSize();
      fail();
    } catch (NotImplementedException e) {
    }
    try {
      gridInfo.getVirtualRowGap();
      fail();
    } catch (NotImplementedException e) {
    }
    try {
      gridInfo.getVirtualColumnSize();
      fail();
    } catch (NotImplementedException e) {
    }
    try {
      gridInfo.getVirtualColumnGap();
      fail();
    } catch (NotImplementedException e) {
    }
  }

  /**
   * Test for {@link IGridInfo#getCellsRectangle(Rectangle)}.
   */
  public void test_cellsRectangle() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(2, 2);",
            "    rootPanel.add(panel);",
            "    panel.getColumnFormatter().setWidth(0, '100px');",
            "    panel.getColumnFormatter().setWidth(1, '200px');",
            "    panel.getCellFormatter().setHeight(0, 0, '100px');",
            "    panel.getCellFormatter().setHeight(1, 0, '200px');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    // check IGridInfo
    IGridInfo gridInfo = panel.getGridInfo();
    assertEquals(2, gridInfo.getRowCount());
    assertEquals(2, gridInfo.getColumnCount());
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertEquals(2, intervals.length);
      assertEquals(new Interval(2, 100), intervals[0]);
      assertEquals(new Interval(2 + 100 + 2, 200), intervals[1]);
    }
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertEquals(2, intervals.length);
      assertEquals(new Interval(2, 100), intervals[0]);
      assertEquals(new Interval(2 + 100 + 2, 200), intervals[1]);
    }
    // cells rectangle
    {
      {
        Rectangle cells = new Rectangle(0, 0, 1, 1);
        Rectangle expected = new Rectangle(2, 2, 100, 100).getResized(1, 1);
        assertEquals(expected, gridInfo.getCellsRectangle(cells));
      }
      {
        Rectangle cells = new Rectangle(0, 0, 2, 1);
        Rectangle expected = new Rectangle(2, 2, 100 + 2 + 200, 100).getResized(1, 1);
        assertEquals(expected, gridInfo.getCellsRectangle(cells));
      }
      {
        Rectangle cells = new Rectangle(0, 0, 1, 2);
        Rectangle expected = new Rectangle(2, 2, 100, 100 + 2 + 200).getResized(1, 1);
        assertEquals(expected, gridInfo.getCellsRectangle(cells));
      }
    }
  }

  /**
   * We should have workaround for case when user copy/pasted code and has two widgets in one cell.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47257
   */
  public void test_twoWidgets_inSameCell() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    Grid panel = new Grid(2, 2);",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.setWidget(0, 0, button_1);",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.setWidget(0, 0, button_2);",
        "    }",
        "  }",
        "}");
    refresh();
    HTMLTableInfo panel = getJavaInfoByName("panel");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    // check IGridInfo
    IGridInfo gridInfo = panel.getGridInfo();
    {
      Rectangle cells = gridInfo.getComponentCells(button_2);
      assertEquals(new Rectangle(0, 0, 1, 1), cells);
    }
    {
      Rectangle cells = gridInfo.getComponentCells(button_1);
      assertEquals(new Rectangle(0, 0, 0, 0), cells);
      Rectangle cellsRectangle = gridInfo.getCellsRectangle(cells);
      assertEquals(new Rectangle(2, 2, 0, 0), cellsRectangle);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Grid
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Grid, no spans.
   */
  public void test_Grid_columnIntervals_0() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(1, 2);",
            "    rootPanel.add(panel);",
            "    panel.getColumnFormatter().setWidth(0, '100px');",
            "    panel.getColumnFormatter().setWidth(1, '200px');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    // check IGridInfo
    IGridInfo gridInfo = panel.getGridInfo();
    assertEquals(2, gridInfo.getColumnCount());
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertEquals(2, intervals.length);
      assertEquals(new Interval(2, 100), intervals[0]);
      assertEquals(new Interval(2 + 100 + 2, 200), intervals[1]);
    }
    // low_getCellOfColumn()
    {
      assertEquals(0, panel.getStatus().getCellOfColumn(0, 0));
      assertEquals(1, panel.getStatus().getCellOfColumn(0, 1));
    }
  }

  /**
   * Grid, no spans.
   */
  public void test_Grid_rowIntervals_0() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(2, 1);",
            "    rootPanel.add(panel);",
            "    panel.getCellFormatter().setHeight(0, 0, '100px');",
            "    panel.getCellFormatter().setHeight(1, 0, '200px');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    // check IGridInfo
    IGridInfo gridInfo = panel.getGridInfo();
    assertEquals(2, gridInfo.getRowCount());
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertEquals(2, intervals.length);
      assertEquals(new Interval(2, 100), intervals[0]);
      assertEquals(new Interval(2 + 100 + 2, 200), intervals[1]);
    }
  }

  public void test_Grid_cells() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(3, 2);",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('1'));",
            "    panel.setWidget(2, 1, new Button('2'));",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    // check hierarchy
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(panel)/}",
        "  {new: com.google.gwt.user.client.ui.Grid} {local-unique: panel} {/new Grid(3, 2)/ /rootPanel.add(panel)/ /panel.setWidget(0, 0, new Button('1'))/ /panel.setWidget(2, 1, new Button('2'))/}",
        "    {method: public com.google.gwt.user.client.ui.HTMLTable$RowFormatter com.google.gwt.user.client.ui.HTMLTable.getRowFormatter()} {property} {}",
        "    {method: public com.google.gwt.user.client.ui.HTMLTable$ColumnFormatter com.google.gwt.user.client.ui.HTMLTable.getColumnFormatter()} {property} {}",
        "    {method: public com.google.gwt.user.client.ui.HTMLTable$CellFormatter com.google.gwt.user.client.ui.HTMLTable.getCellFormatter()} {property} {}",
        "    {new: com.google.gwt.user.client.ui.Button} {empty} {/panel.setWidget(0, 0, new Button('1'))/}",
        "    {new: com.google.gwt.user.client.ui.Button} {empty} {/panel.setWidget(2, 1, new Button('2'))/}");
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    // check IGridInfo cells
    IGridInfo gridInfo = panel.getGridInfo();
    assertEquals(new Rectangle(0, 0, 1, 1), gridInfo.getComponentCells(button_1));
    assertEquals(new Rectangle(1, 2, 1, 1), gridInfo.getComponentCells(button_2));
    // getOccupied()
    assertSame(button_1, gridInfo.getOccupied(0, 0));
    assertSame(button_2, gridInfo.getOccupied(1, 2));
    assertNull(gridInfo.getOccupied(1, 0));
    assertNull(gridInfo.getOccupied(1, 1));
    assertNull(gridInfo.getOccupied(2, 1));
    assertNull(gridInfo.getOccupied(3, 1));
    assertNull(gridInfo.getOccupied(0, 2));
    // we don't support insets
    assertTrue(gridInfo.getInsets().isEmpty());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FlexTable
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * FlexTable, no spans, no skips.
   */
  public void test_FlexTable_intervals_0() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('A'));",
            "    panel.setWidget(2, 2, new Button('A'));",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    FlexTableInfo panel = (FlexTableInfo) frame.getChildrenWidgets().get(0);
    // check IGridInfo
    IGridInfo gridInfo = panel.getGridInfo();
    assertEquals(3, gridInfo.getRowCount());
    assertEquals(3, gridInfo.getColumnCount());
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertEquals(3, intervals.length);
      assertEquals(new Interval(RS, RA), intervals[0]);
      assertEquals(new Interval(RS + RA + RS, RA), intervals[1]);
      assertEquals(new Interval(RS + RA + RS + RA + RS, RA), intervals[2]);
    }
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertEquals(3, intervals.length);
      assertEquals(new Interval(CS, CA), intervals[0]);
      assertEquals(new Interval(CS + CA + CS, CA), intervals[1]);
      assertEquals(new Interval(CS + CA + CS + CA + CS, CA), intervals[2]);
    }
    // cells
    {
      List<WidgetInfo> widgets = panel.getChildrenWidgets();
      assertEquals(new Rectangle(0, 0, 1, 1), gridInfo.getComponentCells(widgets.get(0)));
      assertEquals(new Rectangle(1, 1, 1, 1), gridInfo.getComponentCells(widgets.get(1)));
      assertEquals(new Rectangle(2, 2, 1, 1), gridInfo.getComponentCells(widgets.get(2)));
    }
  }

  /**
   * FlexTable, no spans, skip single row/column.
   */
  public void test_FlexTable_intervals_1() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(2, 2, new Button('A'));",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    FlexTableInfo panel = (FlexTableInfo) frame.getChildrenWidgets().get(0);
    // check IGridInfo
    IGridInfo gridInfo = panel.getGridInfo();
    assertEquals(3, gridInfo.getRowCount());
    assertEquals(3, gridInfo.getColumnCount());
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertEquals(3, intervals.length);
      assertEquals(new Interval(RS, RA), intervals[0]);
      assertEquals(new Interval(RS + RA + RS, RE), intervals[1]);
      assertEquals(new Interval(RS + RA + RS + RE + RS, RA), intervals[2]);
    }
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertEquals(3, intervals.length);
      assertEquals(new Interval(CS, CA), intervals[0]);
      assertEquals(new Interval(CS + CA + CS, CE), intervals[1]);
      assertEquals(new Interval(CS + CA + CS + CE + CS, CA), intervals[2]);
    }
    // cells
    {
      List<WidgetInfo> widgets = panel.getChildrenWidgets();
      assertEquals(new Rectangle(0, 0, 1, 1), gridInfo.getComponentCells(widgets.get(0)));
      assertEquals(new Rectangle(2, 2, 1, 1), gridInfo.getComponentCells(widgets.get(1)));
    }
  }

  /**
   * FlexTable, column spanning, spanned cell is in first row.
   */
  public void test_FlexTable_intervals_2() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('A'));",
            "    panel.setWidget(1, 2, new Button('A'));",
            "    panel.getFlexCellFormatter().setColSpan(0, 0, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    FlexTableInfo panel = (FlexTableInfo) frame.getChildrenWidgets().get(0);
    IGridInfo gridInfo = panel.getGridInfo();
    // check intervals
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertEquals(2, intervals.length);
      assertEquals(new Interval(RS, RA), intervals[0]);
      assertEquals(new Interval(RS + RA + RS, RA), intervals[1]);
    }
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertEquals(3, intervals.length);
      assertEquals(new Interval(CS, CE), intervals[0]);
      assertEquals(new Interval(CS + CE + CS, CA), intervals[1]);
      assertEquals(new Interval(CS + CE + CS + CA + CS, CA), intervals[2]);
    }
    // low_getCellOfColumn()
    {
      assertEquals(0, panel.getStatus().getCellOfColumn(0, 0));
      assertEquals(1, panel.getStatus().getCellOfColumn(0, 2));
      assertEquals(0, panel.getStatus().getCellOfColumn(1, 0));
      assertEquals(1, panel.getStatus().getCellOfColumn(1, 1));
      assertEquals(2, panel.getStatus().getCellOfColumn(1, 2));
      // column in the middle of spanned cell
      assertEquals(0, panel.getStatus().getCellOfColumn(0, 1));
      // column directly after last cell
      assertEquals(2, panel.getStatus().getCellOfColumn(0, 3));
      assertEquals(3, panel.getStatus().getCellOfColumn(1, 3));
      // out of bounds
      try {
        panel.getStatus().getCellOfColumn(0, 100);
        fail();
      } catch (Throwable e) {
      }
    }
  }

  /**
   * FlexTable, column spanning, spanned cell is in second row.
   */
  public void test_FlexTable_intervals_3() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 1, new Button('A'));",
            "    panel.setWidget(0, 2, new Button('A'));",
            "    panel.setWidget(1, 0, new Button('A'));",
            "    panel.getFlexCellFormatter().setColSpan(1, 0, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    FlexTableInfo panel = (FlexTableInfo) frame.getChildrenWidgets().get(0);
    IGridInfo gridInfo = panel.getGridInfo();
    // check intervals
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertEquals(2, intervals.length);
      assertEquals(new Interval(RS, RA), intervals[0]);
      assertEquals(new Interval(RS + RA + RS, RA), intervals[1]);
    }
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertEquals(3, intervals.length);
      assertEquals(new Interval(CS, CE), intervals[0]);
      assertEquals(new Interval(CS + CE + CS, CA), intervals[1]);
      assertEquals(new Interval(CS + CE + CS + CA + CS, CA), intervals[2]);
    }
  }

  /**
   * No widget in 1-th column, because of spanning.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?41872
   */
  public void test_FlexTable_intervals_noWidgetInMiddleColumn() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('A'));",
            "    panel.setWidget(2, 2, new Button('A'));",
            "    panel.getFlexCellFormatter().setColSpan(0, 0, 3);",
            "    panel.getFlexCellFormatter().setColSpan(1, 1, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    FlexTableInfo panel = (FlexTableInfo) frame.getChildrenWidgets().get(0);
    IGridInfo gridInfo = panel.getGridInfo();
    // check intervals
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertEquals(3, intervals.length);
      assertEquals(new Interval(RS, RA), intervals[0]);
      assertEquals(new Interval(RS + RA + RS, RA), intervals[1]);
      assertEquals(new Interval(RS + RA + RS + RA + RS, RA), intervals[2]);
    }
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertEquals(3, intervals.length);
      assertEquals(new Interval(CS, CA), intervals[0]);
      assertEquals(new Interval(CS + CA + CS, CE), intervals[1]);
      assertEquals(new Interval(CS + CA + CS + CE + CS, CA), intervals[2]);
    }
  }

  /**
   * No widget in 2-th column, because of spanning.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42224
   */
  public void test_FlexTable_intervals_noWidgetInLastColumn() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(0, 1, new Button('A'));",
            "    panel.getFlexCellFormatter().setColSpan(0, 1, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    FlexTableInfo panel = (FlexTableInfo) frame.getChildrenWidgets().get(0);
    IGridInfo gridInfo = panel.getGridInfo();
    // check intervals
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertEquals(3, intervals.length);
      assertEquals(new Interval(CS, CA), intervals[0]);
      assertEquals(new Interval(CS + CA + CS, CA / 2), intervals[1]);
      assertEquals(new Interval(CS + CA + CS + CA / 2, CA / 2), intervals[2]);
    }
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertEquals(1, intervals.length);
      assertEquals(new Interval(RS, RA), intervals[0]);
    }
  }

  /**
   * FlexTable, one of the components spanned on two rows/columns.
   */
  public void test_FlexTable_intervals_4() throws Exception {
    dontUseSharedGWTState();
    FlexTableInfo.ensureFlexTableHelper(m_testProject.getSourceFolder().getPackageFragment(
        "test.client"));
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(2, 1, new Button('A'));",
            "    panel.getFlexCellFormatter().setRowSpan(0, 0, 2);",
            "    panel.getFlexCellFormatter().setColSpan(0, 0, 2);",
            "    FlexTableHelper.fixRowSpan(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    FlexTableInfo panel = (FlexTableInfo) frame.getChildrenWidgets().get(0);
    IGridInfo gridInfo = panel.getGridInfo();
    assertEquals(
        "2 3 [1, 2, 2] {(0,0)=(2,2) (0,1)=(1,1) (1,1)=(1,1) (0,2)=(1,1) (1,2)=(1,1)}",
        panel.getStatus().toString());
    // check intervals
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertEquals(3, intervals.length);
      int spanHeight = RA / 2;
      assertEquals(new Interval(RS, spanHeight), intervals[0]);
      assertEquals(new Interval(RS + spanHeight, spanHeight), intervals[1]);
      assertEquals(new Interval(RS + spanHeight + spanHeight, RA), intervals[2]);
    }
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertEquals(2, intervals.length);
      assertEquals(new Interval(CS, CE), intervals[0]);
      assertEquals(new Interval(CS + CE + CS, CA), intervals[1]);
    }
    // check TABLE element
    {
      assertEquals(3, ReflectionUtils.invokeMethod(panel.getObject(), "getRowCount()"));
      assertEquals(1, ReflectionUtils.invokeMethod(panel.getObject(), "getCellCount(int)", 0));
      assertEquals(0, ReflectionUtils.invokeMethod(panel.getObject(), "getCellCount(int)", 1));
      assertEquals(2, ReflectionUtils.invokeMethod(panel.getObject(), "getCellCount(int)", 2));
    }
  }

  /**
   * FlexTable, one of the components spanned on two columns.
   */
  public void test_FlexTable_intervals_5() throws Exception {
    dontUseSharedGWTState();
    FlexTableInfo.ensureFlexTableHelper(m_testProject.getSourceFolder().getPackageFragment(
        "test.client"));
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.getFlexCellFormatter().setColSpan(0, 0, 2);",
            "    FlexTableHelper.fixRowSpan(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    FlexTableInfo panel = (FlexTableInfo) frame.getChildrenWidgets().get(0);
    IGridInfo gridInfo = panel.getGridInfo();
    assertEquals(2, panel.getColumns().size());
    assertEquals(1, panel.getRows().size());
    assertEquals("2 1 [1] {(0,0)=(2,1)}", panel.getStatus().toString());
    // check intervals
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertEquals(1, intervals.length);
      assertEquals(new Interval(RS, RA), intervals[0]);
    }
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertEquals(2, intervals.length);
      assertEquals(new Interval(CS, CA / 2), intervals[0]);
      assertEquals(new Interval(CS + CA / 2, CA / 2), intervals[1]);
    }
    // check TABLE element
    {
      assertEquals(1, ReflectionUtils.invokeMethod(panel.getObject(), "getRowCount()"));
      assertEquals(1, ReflectionUtils.invokeMethod(panel.getObject(), "getCellCount(int)", 0));
    }
  }

  /**
   * FlexTable cells, column spanning.
   */
  public void test_FlexTable_cells_1() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('A'));",
            "    panel.setWidget(1, 2, new Button('A'));",
            "    panel.getFlexCellFormatter().setColSpan(0, 0, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    FlexTableInfo panel = (FlexTableInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    WidgetInfo button_3 = panel.getChildrenWidgets().get(2);
    // check IGridInfo cells
    IGridInfo gridInfo = panel.getGridInfo();
    assertEquals(new Rectangle(0, 0, 2, 1), gridInfo.getComponentCells(button_1));
    assertEquals(new Rectangle(1, 1, 1, 1), gridInfo.getComponentCells(button_2));
    assertEquals(new Rectangle(2, 1, 1, 1), gridInfo.getComponentCells(button_3));
    // getOccupied()
    assertSame(button_1, gridInfo.getOccupied(0, 0));
    assertSame(button_1, gridInfo.getOccupied(1, 0));
    assertSame(button_2, gridInfo.getOccupied(1, 1));
    assertSame(button_3, gridInfo.getOccupied(2, 1));
    assertNull(gridInfo.getOccupied(2, 0));
    assertNull(gridInfo.getOccupied(0, 1));
  }

  /**
   * FlexTable, one of the components spanned on two columns, next component should have correct
   * cell.
   */
  public void test_FlexTable_cells_2() throws Exception {
    dontUseSharedGWTState();
    FlexTableInfo.ensureFlexTableHelper(m_testProject.getSourceFolder().getPackageFragment(
        "test.client"));
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(0, 1, new Button('B'));",
            "    panel.getFlexCellFormatter().setColSpan(0, 0, 2);",
            "    FlexTableHelper.fixRowSpan(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    FlexTableInfo panel = (FlexTableInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo buttonA = panel.getChildrenWidgets().get(0);
    WidgetInfo buttonB = panel.getChildrenWidgets().get(1);
    assertEquals(3, panel.getColumns().size());
    assertEquals(1, panel.getRows().size());
    assertEquals("3 1 [2] {(0,0)=(2,1) (1,0)=(1,1)}", panel.getStatus().toString());
    //
    IGridInfo gridInfo = panel.getGridInfo();
    assertEquals(new Rectangle(0, 0, 2, 1), gridInfo.getComponentCells(buttonA));
    assertEquals(new Rectangle(2, 0, 1, 1), gridInfo.getComponentCells(buttonB));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // No rows/columns
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Even if there are not rows, we should generate non <code>null</code> intervals.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?41941
   */
  public void test_Grid_intervals_noRows() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(0, 2);",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    GridInfo panel = (GridInfo) frame.getChildrenWidgets().get(0);
    IGridInfo gridInfo = panel.getGridInfo();
    // check intervals
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertEquals(0, intervals.length);
    }
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertEquals(2, intervals.length);
      assertEquals(new Interval(0, 0), intervals[0]);
      assertEquals(new Interval(0, 0), intervals[1]);
    }
  }

  /**
   * Even if there are not columns, we should generate non <code>null</code> intervals.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?41941
   */
  public void test_Grid_intervals_noColumns() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(2, 0);",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    GridInfo panel = (GridInfo) frame.getChildrenWidgets().get(0);
    IGridInfo gridInfo = panel.getGridInfo();
    // check intervals
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertEquals(2, intervals.length);
      assertEquals(new Interval(0, 0), intervals[0]);
      assertEquals(new Interval(0, 0), intervals[1]);
    }
    {
      Interval[] intervals = gridInfo.getColumnIntervals();
      assertEquals(0, intervals.length);
    }
  }
}