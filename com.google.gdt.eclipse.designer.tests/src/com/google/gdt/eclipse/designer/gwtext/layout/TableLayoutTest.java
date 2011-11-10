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
package com.google.gdt.eclipse.designer.gwtext.layout;

import com.google.gdt.eclipse.designer.gwtext.GwtExtModelTest;
import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.table.ColumnInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.table.RowInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.table.TableLayoutDataInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.table.TableLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
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
public class TableLayoutTest extends GwtExtModelTest {
  private static final int A_HEIGHT = Expectations.get(21, new IntValue[]{
      new IntValue("Flanker-Windows", 21),
      new IntValue("kosta-home", 32),
      new IntValue("scheglov-win", 21)});
  private static final int A_WIDTH = Expectations.get(11, new IntValue[]{
      new IntValue("kosta-home", 18),
      new IntValue("scheglov-win", 11)});

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
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    assertInstanceOf(TableLayoutInfo.class, panel.getLayout());
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new TableLayout(1))/ /add(label)/}",
        "  {new: com.gwtext.client.widgets.layout.TableLayout} {empty} {/setLayout(new TableLayout(1))/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.TableLayoutData} {virtual-layout-data} {}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    TableLayoutDataInfo layoutData = TableLayoutInfo.getTableData(label);
    assertNotNull(layoutData);
  }

  public void test_parseEmpty() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
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

  /**
   * Fillers should be filtered out from presentation children.
   */
  public void test_excludeFillersFromPresentationChildren() throws Exception {
    PanelInfo shell =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "    add(new Label());",
            "  }",
            "}");
    shell.refresh();
    assertThat(shell.getChildrenWidgets()).hasSize(2);
    WidgetInfo button = shell.getChildrenWidgets().get(0);
    WidgetInfo filler = shell.getChildrenWidgets().get(1);
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
   * Test for initializing {@link TableLayoutDataInfo}.
   */
  public void test_initializeTable_noSpans() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Label label_1 = new Label();",
            "      add(label_1);",
            "    }",
            "    {",
            "      Label label_2 = new Label();",
            "      add(label_2);",
            "    }",
            "    {",
            "      Label label_3 = new Label();",
            "      add(label_3);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertCells(panel.getChildrenWidgets().get(0), 0, 0, 1, 1);
    assertCells(panel.getChildrenWidgets().get(1), 1, 0, 1, 1);
    assertCells(panel.getChildrenWidgets().get(2), 0, 1, 1, 1);
    // refresh() second time, just to covert double initialization, no changes expected
    panel.refresh();
    assertCells(panel.getChildrenWidgets().get(0), 0, 0, 1, 1);
    assertCells(panel.getChildrenWidgets().get(1), 1, 0, 1, 1);
    assertCells(panel.getChildrenWidgets().get(2), 0, 1, 1, 1);
  }

  /**
   * Test for initializing {@link TableLayoutDataInfo}.
   */
  public void test_initializeTable_colSpan() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Label label_1 = new Label();",
            "      add(label_1, new TableLayoutData(2));",
            "    }",
            "    {",
            "      Label label_2 = new Label();",
            "      add(label_2);",
            "    }",
            "    {",
            "      Label label_3 = new Label();",
            "      add(label_3);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertCells(panel.getChildrenWidgets().get(0), 0, 0, 2, 1);
    assertCells(panel.getChildrenWidgets().get(1), 0, 1, 1, 1);
    assertCells(panel.getChildrenWidgets().get(2), 1, 1, 1, 1);
  }

  /**
   * Test for initializing {@link TableLayoutDataInfo}.
   */
  public void test_initializeTable_rowSpan() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Label label_1 = new Label();",
            "      TableLayoutData tableLayoutData = new TableLayoutData(1);",
            "      tableLayoutData.setRowspan(2);",
            "      add(label_1, tableLayoutData);",
            "    }",
            "    {",
            "      Label label_2 = new Label();",
            "      add(label_2);",
            "    }",
            "    {",
            "      Label label_3 = new Label();",
            "      add(label_3);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertCells(panel.getChildrenWidgets().get(0), 0, 0, 1, 2);
    assertCells(panel.getChildrenWidgets().get(1), 1, 0, 1, 1);
    assertCells(panel.getChildrenWidgets().get(2), 1, 1, 1, 1);
  }

  private static void assertCells(WidgetInfo widget, int column, int row, int colSpan, int rowSpan) {
    TableLayoutDataInfo data = TableLayoutInfo.getTableData(widget);
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
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Label label_1 = new Label('A');",
            "      add(label_1);",
            "    }",
            "    {",
            "      Label label_2 = new Label('AA');",
            "      add(label_2);",
            "    }",
            "    {",
            "      Label label_3 = new Label('A');",
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
      assertEquals(1, intervals[0].begin);
      assertEquals(A_WIDTH, intervals[0].length);
      assertEquals(1 + A_WIDTH, intervals[1].begin);
      assertEquals(A_WIDTH * 2, intervals[1].length);
    }
    // getRowIntervals()
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertThat(intervals).hasSize(2);
      assertEquals(1, intervals[0].begin);
      assertEquals(A_HEIGHT, intervals[0].length);
      assertEquals(1 + A_HEIGHT, intervals[1].begin);
      assertEquals(A_HEIGHT, intervals[1].length);
    }
    // getCellsRectangle()
    {
      {
        Rectangle cells = new Rectangle(0, 0, 1, 1);
        Rectangle expected = new Rectangle(1, 1, A_WIDTH, A_HEIGHT);
        assertEquals(expected, gridInfo.getCellsRectangle(cells));
      }
      {
        Rectangle cells = new Rectangle(0, 0, 2, 1);
        Rectangle expected = new Rectangle(1, 1, A_WIDTH + 2 * A_WIDTH, A_HEIGHT);
        assertEquals(expected, gridInfo.getCellsRectangle(cells));
      }
      {
        Rectangle cells = new Rectangle(0, 0, 1, 2);
        Rectangle expected = new Rectangle(1, 1, A_WIDTH, A_HEIGHT + A_HEIGHT);
        assertEquals(expected, gridInfo.getCellsRectangle(cells));
      }
    }
    // getComponentCells()
    {
      assertEquals(
          new Rectangle(0, 0, 1, 1),
          gridInfo.getComponentCells(panel.getChildrenWidgets().get(0)));
      assertEquals(
          new Rectangle(1, 0, 1, 1),
          gridInfo.getComponentCells(panel.getChildrenWidgets().get(1)));
      assertEquals(
          new Rectangle(0, 1, 1, 1),
          gridInfo.getComponentCells(panel.getChildrenWidgets().get(2)));
    }
    // getOccupied()
    {
      assertSame(panel.getChildrenWidgets().get(0), gridInfo.getOccupied(0, 0));
      assertSame(panel.getChildrenWidgets().get(1), gridInfo.getOccupied(1, 0));
      assertSame(panel.getChildrenWidgets().get(2), gridInfo.getOccupied(0, 1));
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
      assertEquals(25, gridInfo.getVirtualColumnSize());
    }
    // virtual rows
    {
      assertTrue(gridInfo.hasVirtualRows());
      assertEquals(5, gridInfo.getVirtualRowGap());
      assertEquals(25, gridInfo.getVirtualRowSize());
    }
  }

  public void test_IGridInfo_3columns_2rows() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(3));",
            "    {",
            "      Label label_1 = new Label('A');",
            "      add(label_1);",
            "    }",
            "    {",
            "      Label label_2 = new Label('AA');",
            "      add(label_2);",
            "    }",
            "    {",
            "      Label label_3 = new Label('A');",
            "      add(label_3);",
            "    }",
            "    {",
            "      Label label_4 = new Label('A');",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "    add(new Label());",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    IGridInfo gridInfo = layout.getGridInfo();
    WidgetInfo button = panel.getChildrenWidgets().get(0);
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
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    add(new Label());",
            "    add(new Label());",
            "    add(new Label());",
            "    {",
            "      Label label = new Label('A');",
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
      assertEquals(1, intervals[0].begin);
      assertEquals(25, intervals[0].length);
      assertEquals(1 + 25, intervals[1].begin);
      assertEquals(A_WIDTH, intervals[1].length);
    }
    // getRowIntervals()
    {
      Interval[] intervals = gridInfo.getRowIntervals();
      assertThat(intervals).hasSize(2);
      assertEquals(1, intervals[0].begin);
      assertThat(intervals[0].length).isGreaterThanOrEqualTo(25);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setCells()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setCells_horizontalSpan() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label());",
            "    add(new Label());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    TableLayoutDataInfo tableData = TableLayoutInfo.getTableData(button);
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button, new TableLayoutData(2));",
        "    }",
        "    add(new Label());",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button, new TableLayoutData(2));",
            "    }",
            "    add(new Label());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    TableLayoutDataInfo tableData = TableLayoutInfo.getTableData(button);
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button, new TableLayoutData(1));",
        "    }",
        "    add(new Label());",
        "    add(new Label());",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label());",
            "    add(new Label());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    TableLayoutDataInfo tableData = TableLayoutInfo.getTableData(button);
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      TableLayoutData tableLayoutData = new TableLayoutData(1);",
        "      tableLayoutData.setRowspan(2);",
        "      add(button, tableLayoutData);",
        "    }",
        "    add(new Label());",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      TableLayoutData tableLayoutData = new TableLayoutData(1);",
            "      tableLayoutData.setRowspan(2);",
            "      add(button, tableLayoutData);",
            "    }",
            "    add(new Label());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    TableLayoutDataInfo tableData = TableLayoutInfo.getTableData(button);
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      TableLayoutData tableLayoutData = new TableLayoutData(1);",
        "      add(button, tableLayoutData);",
        "    }",
        "    add(new Label());",
        "    add(new Label());",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label());",
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
    WidgetInfo button = panel.getChildrenWidgets().get(2);
    //
    layout.command_setCells(button, new Rectangle(1, 0, 1, 1), true);
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
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
        "    add(new Label());",
        "    {",
        "      Button button = new Button('2');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // check x/y for new filler
    {
      WidgetInfo filler = panel.getChildrenWidgets().get(2);
      TableLayoutDataInfo tableData = TableLayoutInfo.getTableData(filler);
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label());",
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
    WidgetInfo button = panel.getChildrenWidgets().get(2);
    //
    button.delete();
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Label());",
        "    add(new Label());",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    //
    button.delete();
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "  }",
        "}");
  }

  public void test_delete_removeEmptyDimensions() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label());",
            "    add(new Label());",
            "    add(new Label());",
            "    add(new Label());",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = panel.getChildrenWidgets().get(5);
    // check initial location
    {
      TableLayoutDataInfo tableData = TableLayoutInfo.getTableData(button);
      assertEquals(1, getInt(tableData, "x"));
      assertEquals(2, getInt(tableData, "y"));
    }
    // delete "button"
    button.delete();
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
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
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_inEmptyCell() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label());",
            "    add(new Label());",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
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
        "    add(new Label());",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_refreshIntervals() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Label());",
        "    add(new Label());",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label('A'));",
            "    add(new Label('B'));",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Label('A'));",
        "    add(new Label());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "    add(new Label('B'));",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertColumn() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label('A'));",
            "    add(new Label('B'));",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
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
        "    add(new Label('A'));",
        "    add(new Label('B'));",
        "    add(new Label());",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertColumnRow() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "    add(new Label());",
        "    add(new Label());",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // delete - should return in initial state
    newButton.delete();
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Label());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_appendColumn() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(3));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Label());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_appendColumnRow() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Label());",
        "    add(new Label());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_insertColumnHorizontalSpan() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button, new TableLayoutData(2));",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(3));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button, new TableLayoutData(3));",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      TableLayoutData tableLayoutData = new TableLayoutData(1);",
            "      tableLayoutData.setRowspan(2);",
            "      add(button, tableLayoutData);",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      TableLayoutData tableLayoutData = new TableLayoutData(1);",
        "      tableLayoutData.setRowspan(3);",
        "      add(button, tableLayoutData);",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    add(new Label());",
            "    add(new Label());",
            "    add(new Label());",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    WidgetInfo newButton = createButton();
    layout.command_CREATE(newButton, 1, false, 1, false);
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Label());",
        "    add(new Label());",
        "    add(new Label());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_noReference() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Label());",
        "    add(new Label());",
        "    add(new Label());",
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
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Label label_1 = new Label('A');",
            "      add(label_1);",
            "    }",
            "    {",
            "      Label label_2 = new Label('A');",
            "      add(label_2);",
            "    }",
            "    {",
            "      Label label_3 = new Label('A');",
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
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "    add(new Label());",
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
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Label label_1 = new Label('A');",
            "      add(label_1);",
            "    }",
            "    {",
            "      Label label_2 = new Label('A');",
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
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "    add(new Label());",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(3));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button, new TableLayoutData(3));",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button, new TableLayoutData(2));",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label());",
            "    add(new Label());",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(3));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button, new TableLayoutData(3));",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button, new TableLayoutData(2));",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      TableLayoutData tableLayoutData = new TableLayoutData(1);",
            "      tableLayoutData.setRowspan(3);",
            "      add(button, tableLayoutData);",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      TableLayoutData tableLayoutData = new TableLayoutData(1);",
        "      tableLayoutData.setRowspan(2);",
        "      add(button, tableLayoutData);",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label());",
            "    add(new Label());",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label());",
            "    add(new Label());",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Label());",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    add(new Label());",
        "  }",
        "}");
  }

  public void test_MOVE_COLUMN_after() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label());",
            "    add(new Label());",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Label());",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    add(new Label());",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE ROW
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_ROW_before() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label());",
            "    add(new Label());",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Label());",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Label());",
        "  }",
        "}");
  }

  public void test_MOVE_ROW_after() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label());",
            "    add(new Label());",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Label());",
        "    {",
        "      Button button = new Button('1');",
        "      add(button);",
        "    }",
        "    {",
        "      Button button = new Button('0');",
        "      add(button);",
        "    }",
        "    add(new Label());",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button, new TableLayoutData(2));",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    layout.command_normalizeSpanning();
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button, new TableLayoutData(1));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link TableLayoutInfo#command_normalizeSpanning()}.<br>
   * Single control spanned on two rows.
   */
  public void test_normalizeSpanning_2() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button('0');",
            "      TableLayoutData tableLayoutData = new TableLayoutData(1);",
            "      tableLayoutData.setRowspan(2);",
            "      add(button, tableLayoutData);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    layout.command_normalizeSpanning();
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button('0');",
        "      TableLayoutData tableLayoutData = new TableLayoutData(1);",
        "      add(button, tableLayoutData);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link TableLayoutInfo#command_normalizeSpanning()}.<br>
   * No normalize: each column/row has control.
   */
  public void test_normalizeSpanning_3() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button, new TableLayoutData(2));",
            "    }",
            "    add(new Label());",
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
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button, new TableLayoutData(2));",
        "    }",
        "    add(new Label());",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button, new TableLayoutData(2));",
            "    }",
            "    {",
            "      Button button = new Button('1');",
            "      add(button);",
            "    }",
            "    add(new Label());",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    //
    layout.command_normalizeSpanning();
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button('0');",
        "      add(button, new TableLayoutData(1));",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(2));",
            "    {",
            "      Button button = new Button('0');",
            "      add(button);",
            "    }",
            "    add(new Label());",
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
    WidgetInfo button = panel.getChildrenWidgets().get(2);
    //
    layout.command_MOVE(button, 1, false, 0, false);
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
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
        "    add(new Label());",
        "    {",
        "      Button button = new Button('2');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_out() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Panel composite = new Panel();",
            "      add(composite);",
            "    }",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    PanelInfo composite = (PanelInfo) panel.getChildrenWidgets().get(0);
    LayoutInfo layout = composite.getLayout();
    WidgetInfo button = panel.getChildrenWidgets().get(1);
    //
    layout.command_MOVE(button, null);
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Panel composite = new Panel();",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(4));",
            "    add(new Label());",
            "    add(new Label());",
            "    add(new Label());",
            "    add(new Label());",
            "    add(new Label());",
            "    add(new Label());",
            "    add(new Label());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getChildrenWidgets().get(7);
    //
    layout.command_MOVE(button, 1, false, 0, false);
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Label());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_error_2() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(3));",
            "    add(new Label());",
            "    add(new Label());",
            "    add(new Label());",
            "    add(new Label());",
            "    add(new Label());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    WidgetInfo button = panel.getChildrenWidgets().get(5);
    //
    layout.command_MOVE(button, 0, false, 0, false);
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Panel composite = new Panel();",
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
    PanelInfo composite = (PanelInfo) panel.getChildrenWidgets().get(0);
    WidgetInfo button = composite.getChildrenWidgets().get(0);
    //
    layout.command_ADD(button, 0, false, 1, false);
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Panel composite = new Panel();",
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
    PanelInfo panel =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.Button;",
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    add(new Label());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TableLayoutInfo layout = (TableLayoutInfo) panel.getLayout();
    // initially 2 controls - filler and Button
    assertThat(panel.getChildrenWidgets()).hasSize(2);
    // after delete - only Button
    layout.delete();
    assertThat(panel.getChildrenWidgets()).hasSize(1);
    assertEditor(
        "import com.google.gwt.user.client.ui.Button;",
        "public class Test extends Panel {",
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
  private static int getInt(TableLayoutDataInfo tableData, String fieldName) throws Exception {
    return ReflectionUtils.getFieldInt(tableData, fieldName);
  }
}