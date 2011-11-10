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
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.CellConstraintsSupport;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.ColumnInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.FlexTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.RowInfo;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.IType;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link FlexTableInfo}.
 * 
 * @author scheglov_ke
 */
public class FlexTableTest extends GwtModelTest {
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
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We ask for "formatters" as exposed {@link JavaInfo}. But in case of "this", we do this several
   * time. Unfortunately, when we ask second time and it was already exposed, then <code>null</code>
   * returned.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44300
   */
  public void test_parse_this() throws Exception {
    FlexTableInfo panel =
        parseJavaInfo(
            "public class Test extends FlexTable {",
            "  public void onModuleLoad() {",
            "    setWidget(0, 0, new Button('A'));",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setText() and setHTML()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We should execute <code>HTMLTable.setText(row,col,text)</code> and understand that TD with text
   * is not empty.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44967
   */
  public void test_parse_setText() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      FlexTable panel = new FlexTable();",
            "      rootPanel.add(panel);",
            "      panel.setText(0, 0, 'Some very very long text');",
            "    }",
            "  }",
            "}");
    frame.refresh();
    FlexTableInfo panel = getJavaInfoByName("panel");
    // if not replaced with "filler", then "panel" has big width
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.width).isGreaterThan(150);
    }
    // column is not empty
    {
      List<ColumnInfo> columns = panel.getColumns();
      assertThat(columns).hasSize(1);
      ColumnInfo column = columns.get(0);
      assertFalse(column.isEmpty());
    }
    // row is not empty
    {
      List<RowInfo> rows = panel.getRows();
      assertThat(rows).hasSize(1);
      RowInfo row = rows.get(0);
      assertFalse(row.isEmpty());
    }
  }

  /**
   * We should execute <code>HTMLTable.setHTML(row,col,text)</code> and understand that TD with text
   * is not empty.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44967
   */
  public void test_parse_setHTML() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      FlexTable panel = new FlexTable();",
            "      rootPanel.add(panel);",
            "      panel.setHTML(0, 0, 'Some very very long text');",
            "    }",
            "  }",
            "}");
    frame.refresh();
    FlexTableInfo panel = getJavaInfoByName("panel");
    // if not replaced with "filler", then "panel" has big width
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.width).isGreaterThan(150);
    }
    // column is not empty
    {
      List<ColumnInfo> columns = panel.getColumns();
      assertThat(columns).hasSize(1);
      ColumnInfo column = columns.get(0);
      assertFalse(column.isEmpty());
    }
    // row is not empty
    {
      List<RowInfo> rows = panel.getRows();
      assertThat(rows).hasSize(1);
      RowInfo row = rows.get(0);
      assertFalse(row.isEmpty());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FlexTableHelper
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_FlexTableHelper_noFix() throws Exception {
    do_projectCreate();
    setUp();
    dontUseSharedGWTState();
    // remove FlexTableHelper
    {
      IType helperType = m_testProject.getJavaProject().findType("test.client.FlexTableHelper");
      if (helperType != null) {
        helperType.getCompilationUnit().getUnderlyingResource().delete(true, null);
        waitForAutoBuild();
      }
    }
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('B'));",
            "    panel.getFlexCellFormatter().setRowSpan(0, 0, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    // no fix, so each row has 2 cells, however for row "0" this means 3 (!) visual cells
    assertEquals(2, ReflectionUtils.invokeMethod(panel.getObject(), "getCellCount(int)", 0));
    assertEquals(2, ReflectionUtils.invokeMethod(panel.getObject(), "getCellCount(int)", 1));
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,2) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
  }

  public void test_FlexTableHelper_addFix() throws Exception {
    dontUseSharedGWTState();
    {
      RootPanelInfo frame =
          parseJavaInfo(
              "public class Test implements EntryPoint {",
              "  public void onModuleLoad() {",
              "    RootPanel rootPanel = RootPanel.get();",
              "    FlexTable panel = new FlexTable();",
              "    rootPanel.add(panel);",
              "    panel.setWidget(0, 0, new Button('A'));",
              "    panel.setWidget(1, 1, new Button('B'));",
              "    panel.getFlexCellFormatter().setRowSpan(0, 0, 2);",
              "  }",
              "}");
      frame.refresh();
      assertNoErrors(frame);
      FlexTableInfo panel = (FlexTableInfo) frame.getChildrenWidgets().get(0);
      // no FlexTableHelper initially
      assertNull(m_testProject.getJavaProject().findType("test.client.FlexTableHelper"));
      // ensure FlexTableHelper
      {
        panel.ensureFlexTableHelper();
        assertNotNull(m_testProject.getJavaProject().findType("test.client.FlexTableHelper"));
        assertEditor(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('B'));",
            "    panel.getFlexCellFormatter().setRowSpan(0, 0, 2);",
            "    FlexTableHelper.fixRowSpan(panel);",
            "  }",
            "}");
      }
    }
    //
    RootPanelInfo frame;
    {
      String source = m_lastEditor.getSource();
      tearDown();
      setUp();
      dontUseSharedGWTState();
      frame = (RootPanelInfo) parseSource("test.client", "Test.java", source);
      frame.refresh();
      assertNoErrors(frame);
    }
    FlexTableInfo panel = (FlexTableInfo) frame.getChildrenWidgets().get(0);
    // with fix each row should have 2 visual cells, but row "1" has only 1 TD element
    assertEquals(2, ReflectionUtils.invokeMethod(panel.getObject(), "getCellCount(int)", 0));
    assertEquals(1, ReflectionUtils.invokeMethod(panel.getObject(), "getCellCount(int)", 1));
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,2) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
  }

  /**
   * Invocation of <code>FlexTableHelper.fixRowSpan()</code> should be last statement for
   * {@link FlexTableInfo}.
   */
  public void test_FlexTableHelper_fixReference() throws Exception {
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
            "    FlexTableHelper.fixRowSpan(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    FlexTableInfo panel = (FlexTableInfo) frame.getChildrenWidgets().get(0);
    // add new statement, should be added before fixRowSpan()
    {
      StatementTarget target = JavaInfoUtils.getTarget(panel, null);
      panel.addExpressionStatement(target, "System.out.println()");
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    System.out.println();",
        "    FlexTableHelper.fixRowSpan(panel);",
        "  }",
        "}");
  }

  /**
   * If <code>FlexTableHelper.fixRowSpan()</code> already done for this instance of
   * {@link FlexTableInfo}, it should not be added again.
   */
  public void test_FlexTableHelper_fix2() throws Exception {
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
            "    FlexTableHelper.fixRowSpan(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    FlexTableInfo panel = (FlexTableInfo) frame.getChildrenWidgets().get(0);
    // ask "ensure" again, no change expected
    String expectedSource = m_lastEditor.getSource();
    panel.ensureFlexTableHelper();
    assertEditor(expectedSource, m_lastEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column INSERT
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link HTMLTableInfo#insertColumn(int)}. <br>
   * Append column to empty table.
   */
  public void test_insertColumn_appendToEmpty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(0);
    assertThat(panel.getRows()).hasSize(0);
    assertEquals("0 0 [] {}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo newButton = createButton();
        panel.command_CREATE(newButton, 0, false, 0, false);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setWidget(0, 0, button);",
        "    }",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#insertColumn(int)}. <br>
   * Append column to existing table.
   */
  public void test_insertColumn_append() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(1, 0, new Button('A'));",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals("1 2 [1, 1] {(0,0)=(1,1) (0,1)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo newButton = createButton();
        panel.command_CREATE(newButton, 1, false, 0, false);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setWidget(0, 1, button);",
        "    }",
        "    panel.setWidget(1, 0, new Button('A'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,1) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#insertColumn(int)}. <br>
   * Insert column to existing table, invocations should be updated.
   */
  public void test_insertColumn_insertNoSpans() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(0, 1, new Button('B'));",
            "    panel.getCellFormatter().setWidth(0, 0, '10px');",
            "    panel.getCellFormatter().setWidth(0, 1, '20px');",
            "    panel.getFlexCellFormatter().setColSpan(0, 0, 1);",
            "    panel.getFlexCellFormatter().setColSpan(0, 1, 1);",
            "    panel.getColumnFormatter().setWidth(0, '100px');",
            "    panel.getColumnFormatter().setWidth(1, '200px');",
            "    panel.getRowFormatter().setStyleName(0, 'A');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("2 1 [2] {(0,0)=(1,1) (1,0)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.insertColumn(1);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setWidget(0, 2, new Button('B'));",
        "    panel.getCellFormatter().setWidth(0, 0, '10px');",
        "    panel.getCellFormatter().setWidth(0, 2, '20px');",
        "    panel.getFlexCellFormatter().setColSpan(0, 0, 1);",
        "    panel.getFlexCellFormatter().setColSpan(0, 2, 1);",
        "    panel.getColumnFormatter().setWidth(0, '100px');",
        "    panel.getColumnFormatter().setWidth(2, '200px');",
        "    panel.getRowFormatter().setStyleName(0, 'A');",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("3 1 [3] {(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#insertColumn(int)}. <br>
   * Insert column to existing table, spans should be updated.
   */
  public void test_insertColumn_insertWithSpans() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('B'));",
            "    panel.setWidget(2, 2, new Button('C'));",
            "    panel.getFlexCellFormatter().setColSpan(1, 1, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(3);
    assertEquals("3 3 [3, 2, 3] {"
        + "(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1) "
        + "(0,1)=(1,1) (1,1)=(2,1) "
        + "(0,2)=(1,1) (1,2)=(1,1) (2,2)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.insertColumn(2);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setWidget(1, 1, new Button('B'));",
        "    panel.setWidget(2, 3, new Button('C'));",
        "    panel.getFlexCellFormatter().setColSpan(1, 1, 3);",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(4);
    assertThat(panel.getRows()).hasSize(3);
    assertEquals("4 3 [4, 2, 4] {"
        + "(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1) (3,0)=(1,1) "
        + "(0,1)=(1,1) (1,1)=(3,1) "
        + "(0,2)=(1,1) (1,2)=(1,1) (2,2)=(1,1) (3,2)=(1,1)}", panel.getStatus().toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column DELETE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link HTMLTableInfo#deleteColumn(int)}. <br>
   * Delete single column, empty rows also should be deleted.
   */
  public void test_deleteColumn_lastOnly() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.setWidget(0, 0, button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.deleteColumn(0);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(0);
    assertThat(panel.getRows()).hasSize(0);
    assertEquals("0 0 [] {}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#deleteColumn(int)}. <br>
   * Delete last column in filled table.
   */
  public void test_deleteColumn_last() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.setWidget(0, 1, button);",
            "    }",
            "    panel.setWidget(1, 0, new Button('A'));",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,1) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.deleteColumn(1);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(1, 0, new Button('A'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals("1 2 [1, 1] {(0,0)=(1,1) (0,1)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#deleteColumn(int)}. <br>
   * Delete last column in filled table.
   */
  public void test_deleteColumn_last_noWidget() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.getCellFormatter().setWidth(0, 1, '100px');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("2 1 [2] {(0,0)=(1,1) (1,0)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.deleteColumn(1);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#deleteColumn(int)}. <br>
   * Delete column from the middle, invocations should be updated.
   */
  public void test_deleteColumn_middleNoSpans() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(0, 1, new Button('B'));",
            "    panel.setWidget(0, 2, new Button('C'));",
            "    panel.getCellFormatter().setWidth(0, 0, '10px');",
            "    panel.getCellFormatter().setWidth(0, 1, '20px');",
            "    panel.getCellFormatter().setWidth(0, 2, '30px');",
            "    panel.getFlexCellFormatter().setColSpan(0, 0, 1);",
            "    panel.getFlexCellFormatter().setColSpan(0, 1, 1);",
            "    panel.getFlexCellFormatter().setColSpan(0, 2, 1);",
            "    panel.getColumnFormatter().setWidth(0, '100px');",
            "    panel.getColumnFormatter().setWidth(1, '200px');",
            "    panel.getColumnFormatter().setWidth(2, '300px');",
            "    panel.getRowFormatter().setStyleName(0, 'A');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("3 1 [3] {(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.deleteColumn(1);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setWidget(0, 1, new Button('C'));",
        "    panel.getCellFormatter().setWidth(0, 0, '10px');",
        "    panel.getCellFormatter().setWidth(0, 1, '30px');",
        "    panel.getFlexCellFormatter().setColSpan(0, 0, 1);",
        "    panel.getFlexCellFormatter().setColSpan(0, 1, 1);",
        "    panel.getColumnFormatter().setWidth(0, '100px');",
        "    panel.getColumnFormatter().setWidth(1, '300px');",
        "    panel.getRowFormatter().setStyleName(0, 'A');",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("2 1 [2] {(0,0)=(1,1) (1,0)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#insertColumn(int)}. <br>
   * Delete column from the middle, spans should be updated.
   */
  public void test_deleteColumn_insertWithSpans() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('B'));",
            "    panel.setWidget(2, 3, new Button('C'));",
            "    panel.getFlexCellFormatter().setColSpan(1, 1, 3);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(4);
    assertThat(panel.getRows()).hasSize(3);
    assertEquals("4 3 [4, 2, 4] {"
        + "(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1) (3,0)=(1,1) "
        + "(0,1)=(1,1) (1,1)=(3,1) "
        + "(0,2)=(1,1) (1,2)=(1,1) (2,2)=(1,1) (3,2)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.deleteColumn(2);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setWidget(1, 1, new Button('B'));",
        "    panel.setWidget(2, 2, new Button('C'));",
        "    panel.getFlexCellFormatter().setColSpan(1, 1, 2);",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(3);
    assertEquals("3 3 [3, 2, 3] {"
        + "(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1) "
        + "(0,1)=(1,1) (1,1)=(2,1) "
        + "(0,2)=(1,1) (1,2)=(1,1) (2,2)=(1,1)}", panel.getStatus().toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clear column
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link HTMLTableInfo#clearColumn(int)}.<br>
   * Clear inner column.
   */
  public void test_clearColumn_inner() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(0, 1, new Button('B'));",
            "    panel.getCellFormatter().setWidth(0, 0, '100px');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("2 1 [2] {(0,0)=(1,1) (1,0)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.clearColumn(0);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 1, new Button('B'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("2 1 [2] {(0,0)=(1,1) (1,0)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#clearColumn(int)}.<br>
   * Clear last column, it should be removed.
   */
  public void test_clearColumn_last() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(0, 1, new Button('B'));",
            "    panel.getCellFormatter().setWidth(0, 1, '100px');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("2 1 [2] {(0,0)=(1,1) (1,0)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.clearColumn(1);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#clearColumn(int)}.<br>
   * Column was created without widget, just because of some <code>Element</code> access.
   */
  public void test_clearColumn_noWidget() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.getCellFormatter().setWidth(0, 1, '100px');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("2 1 [2] {(0,0)=(1,1) (1,0)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.clearColumn(1);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row INSERT
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link HTMLTableInfo#insertRow(int)}. <br>
   * Append row to empty table.
   */
  public void test_insertRow_appendToEmpty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(0);
    assertThat(panel.getRows()).hasSize(0);
    assertEquals("0 0 [] {}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo newButton = createButton();
        panel.command_CREATE(newButton, 0, false, 0, false);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setWidget(0, 0, button);",
        "    }",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#insertRow(int)}. <br>
   * Append row to existing table.
   */
  public void test_insertRow_append() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 1, new Button('A'));",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("2 1 [2] {(0,0)=(1,1) (1,0)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo newButton = createButton();
        panel.command_CREATE(newButton, 0, false, 1, false);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 1, new Button('A'));",
        "    {",
        "      Button button = new Button();",
        "      panel.setWidget(1, 0, button);",
        "    }",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,1) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#insertRow(int)}. <br>
   * Insert row into existing table, invocations should be updated.
   */
  public void test_insertRow_insertNoSpans() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 0, new Button('B'));",
            "    panel.getCellFormatter().setWidth(0, 0, '10px');",
            "    panel.getCellFormatter().setWidth(1, 0, '20px');",
            "    panel.getFlexCellFormatter().setColSpan(0, 0, 1);",
            "    panel.getFlexCellFormatter().setColSpan(1, 0, 1);",
            "    panel.getColumnFormatter().setWidth(0, '100px');",
            "    panel.getRowFormatter().setStyleName(0, 'A');",
            "    panel.getRowFormatter().setStyleName(1, 'B');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals("1 2 [1, 1] {(0,0)=(1,1) (0,1)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.insertRow(1);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setWidget(2, 0, new Button('B'));",
        "    panel.getCellFormatter().setWidth(0, 0, '10px');",
        "    panel.getCellFormatter().setWidth(2, 0, '20px');",
        "    panel.getFlexCellFormatter().setColSpan(0, 0, 1);",
        "    panel.getFlexCellFormatter().setColSpan(2, 0, 1);",
        "    panel.getColumnFormatter().setWidth(0, '100px');",
        "    panel.getRowFormatter().setStyleName(0, 'A');",
        "    panel.getRowFormatter().setStyleName(2, 'B');",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(3);
    assertEquals(
        "1 3 [1, 1, 1] {(0,0)=(1,1) (0,1)=(1,1) (0,2)=(1,1)}",
        panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#insertRow(int)}. <br>
   * Insert row into existing table, spans should be updated.
   */
  public void test_insertRow_insertWithSpans() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('B'));",
            "    panel.setWidget(2, 2, new Button('C'));",
            "    panel.getFlexCellFormatter().setRowSpan(1, 1, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(3);
    assertEquals("3 3 [3, 3, 3] {"
        + "(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1) "
        + "(0,1)=(1,1) (1,1)=(1,2) (2,1)=(1,1) "
        + "(0,2)=(1,1) (1,2)=(1,1) (2,2)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.insertRow(2);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setWidget(1, 1, new Button('B'));",
        "    panel.setWidget(3, 2, new Button('C'));",
        "    panel.getFlexCellFormatter().setRowSpan(1, 1, 3);",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(4);
    assertEquals("3 4 [3, 3, 3, 3] {"
        + "(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1) "
        + "(0,1)=(1,1) (1,1)=(1,3) (2,1)=(1,1) "
        + "(0,2)=(1,1) (1,2)=(1,1) (2,2)=(1,1) "
        + "(0,3)=(1,1) (1,3)=(1,1) (2,3)=(1,1)}", panel.getStatus().toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Row INSERT
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link HTMLTableInfo#deleteRow(int)}. <br>
   * Delete last/only row.
   */
  public void test_deleteRow_lastOnly() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.setWidget(0, 0, button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.deleteRow(0);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(0);
    assertThat(panel.getRows()).hasSize(0);
    assertEquals("0 0 [] {}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#deleteRow(int)}. <br>
   * Delete last row.
   */
  public void test_deleteRow_last() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 0, new Button('B'));",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals("1 2 [1, 1] {(0,0)=(1,1) (0,1)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.deleteRow(1);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#deleteRow(int)}. <br>
   * Delete row from the middle table, invocations should be updated.
   */
  public void test_deleteRow_middleNoSpans() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(2, 0, new Button('B'));",
            "    panel.getCellFormatter().setWidth(0, 0, '10px');",
            "    panel.getCellFormatter().setWidth(2, 0, '20px');",
            "    panel.getFlexCellFormatter().setColSpan(0, 0, 1);",
            "    panel.getFlexCellFormatter().setColSpan(2, 0, 1);",
            "    panel.getColumnFormatter().setWidth(0, '100px');",
            "    panel.getRowFormatter().setStyleName(0, 'A');",
            "    panel.getRowFormatter().setStyleName(2, 'B');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(3);
    assertEquals(
        "1 3 [1, 1, 1] {(0,0)=(1,1) (0,1)=(1,1) (0,2)=(1,1)}",
        panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.deleteRow(1);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setWidget(1, 0, new Button('B'));",
        "    panel.getCellFormatter().setWidth(0, 0, '10px');",
        "    panel.getCellFormatter().setWidth(1, 0, '20px');",
        "    panel.getFlexCellFormatter().setColSpan(0, 0, 1);",
        "    panel.getFlexCellFormatter().setColSpan(1, 0, 1);",
        "    panel.getColumnFormatter().setWidth(0, '100px');",
        "    panel.getRowFormatter().setStyleName(0, 'A');",
        "    panel.getRowFormatter().setStyleName(1, 'B');",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals("1 2 [1, 1] {(0,0)=(1,1) (0,1)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#deleteRow(int)}. <br>
   * Delete row from the middle table, spans should be updated.
   */
  public void test_deleteRow_insertWithSpans() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('B'));",
            "    panel.setWidget(3, 2, new Button('C'));",
            "    panel.getFlexCellFormatter().setRowSpan(1, 1, 3);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(4);
    assertEquals("3 4 [3, 3, 3, 3] {"
        + "(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1) "
        + "(0,1)=(1,1) (1,1)=(1,3) (2,1)=(1,1) "
        + "(0,2)=(1,1) (1,2)=(1,1) (2,2)=(1,1) "
        + "(0,3)=(1,1) (1,3)=(1,1) (2,3)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.deleteRow(2);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setWidget(1, 1, new Button('B'));",
        "    panel.setWidget(2, 2, new Button('C'));",
        "    panel.getFlexCellFormatter().setRowSpan(1, 1, 2);",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(3);
    assertEquals("3 3 [3, 3, 3] {"
        + "(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1) "
        + "(0,1)=(1,1) (1,1)=(1,2) (2,1)=(1,1) "
        + "(0,2)=(1,1) (1,2)=(1,1) (2,2)=(1,1)}", panel.getStatus().toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clear row
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link HTMLTableInfo#clearRow(int)}.<br>
   * Clear inner row.
   */
  public void test_clearRow_inner() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 0, new Button('B'));",
            "    panel.getCellFormatter().setWidth(0, 0, '100px');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals("1 2 [1, 1] {(0,0)=(1,1) (0,1)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.clearRow(0);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(1, 0, new Button('B'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals("1 2 [1, 1] {(0,0)=(1,1) (0,1)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#clearRow(int)}.<br>
   * Clear last row, it should be removed.
   */
  public void test_clearRow_last() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 0, new Button('B'));",
            "    panel.getCellFormatter().setWidth(1, 0, '100px');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals("1 2 [1, 1] {(0,0)=(1,1) (0,1)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.clearRow(1);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#clearRow(int)}.<br>
   * Row was created without widget, just because of some <code>Element</code> access.
   */
  public void test_clearRow_noWidget() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.getCellFormatter().setWidth(1, 0, '100px');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals("1 2 [1, 1] {(0,0)=(1,1) (0,1)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.clearRow(1);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // command_CELLS
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link HTMLTableInfo#setComponentColSpan(WidgetInfo, int)}.<br>
   * 1 -> 2
   */
  public void test_setComponentColSpan_plus() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('B'));",
            "    panel.setText(0, 1, 'To remove');",
            "    panel.setText(1, 0, 'To keep');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,1) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo buttonA = panel.getChildrenWidgets().get(0);
        assertEquals("0 0 1 1", HTMLTableInfo.getConstraints(buttonA).toString());
        panel.command_CELLS(buttonA, new Rectangle(0, 0, 2, 1));
        assertEquals("0 0 2 1", HTMLTableInfo.getConstraints(buttonA).toString());
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setWidget(1, 1, new Button('B'));",
        "    panel.setText(1, 0, 'To keep');",
        "    panel.getFlexCellFormatter().setColSpan(0, 0, 2);",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals("2 2 [1, 2] {(0,0)=(2,1) (0,1)=(1,1) (1,1)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#setComponentColSpan(WidgetInfo, int)}.<br>
   * 1 -> 3
   */
  public void test_setComponentColSpan_plus2() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setText(0, 2, 'Text');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("3 1 [3] {(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo buttonA = panel.getChildrenWidgets().get(0);
        assertEquals("0 0 1 1", HTMLTableInfo.getConstraints(buttonA).toString());
        panel.command_CELLS(buttonA, new Rectangle(0, 0, 2, 1));
        assertEquals("0 0 2 1", HTMLTableInfo.getConstraints(buttonA).toString());
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setText(0, 1, 'Text');",
        "    panel.getFlexCellFormatter().setColSpan(0, 0, 2);",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("3 1 [2] {(0,0)=(2,1) (1,0)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#setComponentColSpan(WidgetInfo, int)}.<br>
   * 2 -> 1
   */
  public void test_setComponentColSpan_minus() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('B'));",
            "    panel.setText(1, 0, 'To keep');",
            "    panel.getFlexCellFormatter().setColSpan(0, 0, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals("2 2 [1, 2] {(0,0)=(2,1) (0,1)=(1,1) (1,1)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo buttonA = panel.getChildrenWidgets().get(0);
        assertEquals("0 0 2 1", HTMLTableInfo.getConstraints(buttonA).toString());
        panel.command_CELLS(buttonA, new Rectangle(0, 0, 1, 1));
        assertEquals("0 0 1 1", HTMLTableInfo.getConstraints(buttonA).toString());
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setWidget(1, 1, new Button('B'));",
        "    panel.setText(1, 0, 'To keep');",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,1) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#setComponentColSpan(WidgetInfo, int)}.<br>
   * 3 -> 1
   */
  public void test_setComponentColSpan_minus2() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setText(0, 1, 'Text');",
            "    panel.getFlexCellFormatter().setColSpan(0, 0, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("3 1 [2] {(0,0)=(2,1) (1,0)=(1,1)}", panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo buttonA = panel.getChildrenWidgets().get(0);
        assertEquals("0 0 2 1", HTMLTableInfo.getConstraints(buttonA).toString());
        panel.command_CELLS(buttonA, new Rectangle(0, 0, 1, 1));
        assertEquals("0 0 1 1", HTMLTableInfo.getConstraints(buttonA).toString());
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setText(0, 2, 'Text');",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("3 1 [3] {(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#setComponentRowSpan(WidgetInfo, int)}.<br>
   * 1 -> 2
   */
  public void test_setComponentRowSpan_plus() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('B'));",
            "    panel.setText(1, 0, 'To remove');",
            "    panel.setText(0, 1, 'To keep');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,1) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo buttonA = panel.getChildrenWidgets().get(0);
        assertEquals("0 0 1 1", HTMLTableInfo.getConstraints(buttonA).toString());
        panel.command_CELLS(buttonA, new Rectangle(0, 0, 1, 2));
        assertEquals("0 0 1 2", HTMLTableInfo.getConstraints(buttonA).toString());
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setWidget(1, 1, new Button('B'));",
        "    panel.setText(0, 1, 'To keep');",
        "    panel.getFlexCellFormatter().setRowSpan(0, 0, 2);",
        "    FlexTableHelper.fixRowSpan(panel);",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,2) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#setComponentRowSpan(WidgetInfo, int)}.<br>
   * 2 -> 1
   */
  public void test_setComponentRowSpan_minus() throws Exception {
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
            "    panel.setWidget(1, 1, new Button('B'));",
            "    panel.setText(0, 1, 'To keep');",
            "    panel.getFlexCellFormatter().setRowSpan(0, 0, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,2) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo buttonA = panel.getChildrenWidgets().get(0);
        assertEquals("0 0 1 2", HTMLTableInfo.getConstraints(buttonA).toString());
        panel.command_CELLS(buttonA, new Rectangle(0, 0, 1, 1));
        assertEquals("0 0 1 1", HTMLTableInfo.getConstraints(buttonA).toString());
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setWidget(1, 1, new Button('B'));",
        "    panel.setText(0, 1, 'To keep');",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,1) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cell/colSpan and Cell/rowSpan
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for updating property "Cell/colSpan".<br>
   * 1 -> 2
   */
  public void test_setProperty_colSpan() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button());",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
    // do operation
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    PropertyUtils.getByPath(button, "Cell/colSpan").setValue(2);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button());",
        "    panel.getFlexCellFormatter().setColSpan(0, 0, 2);",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("2 1 [1] {(0,0)=(2,1)}", panel.getStatus().toString());
    // check constraints
    CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(button);
    assertEquals(0, constraints.getX());
    assertEquals(0, constraints.getY());
    assertEquals(2, constraints.getWidth());
    assertEquals(1, constraints.getHeight());
    // checks grid
    IGridInfo gridInfo = panel.getGridInfo();
    assertEquals(new Rectangle(0, 0, 2, 1), gridInfo.getComponentCells(button));
  }

  /**
   * Test for updating property "Cell/rowSpan".<br>
   * 1 -> 2
   */
  public void test_setProperty_rowSpan() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button());",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
    // try to set number of rows
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    PropertyUtils.getByPath(button, "Cell/rowSpan").setValue(2);
    // no such row, so no changes performed
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button());",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setLocation()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link HTMLTableInfo#setComponentLocation(WidgetInfo, int, int)}.
   */
  public void test_setComponentLocation() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 1, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('B'));",
            "    panel.getCellFormatter().setWidth(0, 0, '1cm');",
            "    panel.getCellFormatter().setWidth(1, 1, '2cm');",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,1) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
    //
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo buttonB = panel.getChildrenWidgets().get(1);
        panel.setComponentLocation(buttonB, 0, 0);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 1, new Button('A'));",
        "    panel.setWidget(0, 0, new Button('B'));",
        "    panel.getCellFormatter().setWidth(0, 0, '2cm');",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("2 1 [2] {(0,0)=(1,1) (1,0)=(1,1)}", panel.getStatus().toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link HTMLTableInfo#command_CREATE(WidgetInfo, int, boolean, int, boolean)}.<br>
   * Empty grid, not reference, no inserts.
   */
  public void test_CREATE_empty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(0);
    assertThat(panel.getRows()).hasSize(0);
    assertEquals("0 0 [] {}", panel.getStatus().toString());
    // do CREATE
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo newButton = createButton();
        panel.command_CREATE(newButton, 2, false, 1, false);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setWidget(1, 2, button);",
        "    }",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "3 2 [3, 3] {(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1) (2,1)=(1,1)}",
        panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#command_CREATE(WidgetInfo, int, boolean, int, boolean)}.<br>
   * Has reference, with inserts.
   */
  public void test_CREATE_insertMove() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 2, new Button('B'));",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals("3 2 [3, 3] {"
        + "(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1) "
        + "(0,1)=(1,1) (1,1)=(1,1) (2,1)=(1,1)}", panel.getStatus().toString());
    // do CREATE
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo newButton = createButton();
        panel.command_CREATE(newButton, 1, true, 1, true);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    {",
        "      Button button = new Button();",
        "      panel.setWidget(1, 1, button);",
        "    }",
        "    panel.setWidget(2, 3, new Button('B'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(4);
    assertThat(panel.getRows()).hasSize(3);
    assertEquals("4 3 [4, 4, 4] {"
        + "(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1) (3,0)=(1,1) "
        + "(0,1)=(1,1) (1,1)=(1,1) (2,1)=(1,1) (3,1)=(1,1) "
        + "(0,2)=(1,1) (1,2)=(1,1) (2,2)=(1,1) (3,2)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#command_CREATE(WidgetInfo, int, boolean, int, boolean)}.<br>
   * No reference, append columns/rows.
   */
  public void test_CREATE_appendDimensions() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(0);
    assertThat(panel.getRows()).hasSize(0);
    assertEquals("0 0 [] {}", panel.getStatus().toString());
    // do CREATE
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo newButton = createButton();
        panel.command_CREATE(newButton, 2, false, 1, false);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setWidget(1, 2, button);",
        "    }",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "3 2 [3, 3] {(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1) (2,1)=(1,1)}",
        panel.getStatus().toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link HTMLTableInfo#command_MOVE(WidgetInfo, int, boolean, int, boolean)}.<br>
   * Move widget internally into empty cell.
   */
  public void test_MOVE_internalNoInsert() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button_1 = new Button();",
            "      panel.setWidget(0, 1, button_1);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      panel.setWidget(1, 0, button_2);",
            "      panel.getFlexCellFormatter().setColSpan(1, 0, 2);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals("2 2 [2, 1] {(0,0)=(1,1) (1,0)=(1,1) (0,1)=(2,1)}", panel.getStatus().toString());
    // do MOVE
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
        panel.command_MOVE(button_2, 0, false, 0, false);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.setWidget(0, 0, button_2);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.setWidget(0, 1, button_1);",
        "    }",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("2 1 [2] {(0,0)=(1,1) (1,0)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test for {@link HTMLTableInfo#command_MOVE(WidgetInfo, int, boolean, int, boolean)}.<br>
   * Move widget from <code>RootPanel</code> into empty cell of <code>FlexTable</code>.
   */
  public void test_MOVE_externalNoInsert() throws Exception {
    final RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button_1 = new Button();",
            "      panel.setWidget(0, 1, button_1);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      rootPanel.add(button_2);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("2 1 [2] {(0,0)=(1,1) (1,0)=(1,1)}", panel.getStatus().toString());
    // do MOVE
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo button_2 = frame.getChildrenWidgets().get(1);
        panel.command_MOVE(button_2, 0, false, 0, false);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.setWidget(0, 0, button_2);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.setWidget(0, 1, button_1);",
        "    }",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("2 1 [2] {(0,0)=(1,1) (1,0)=(1,1)}", panel.getStatus().toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Remove child widget
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that when {@link WidgetInfo} is deleted, we clean up {@link HTMLTableInfo}.
   */
  public void test_delete_removeEmptyDimensions() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('B'));",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,1) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo buttonB = panel.getChildrenWidgets().get(1);
        buttonB.delete();
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * Test that when {@link WidgetInfo} is deleted, we clean up {@link HTMLTableInfo}.
   */
  public void test_delete_removeCellInvocations() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(0, 1, new Button('B'));",
            "    panel.setWidget(1, 1, new Button('C'));",
            "    panel.getFlexCellFormatter().setRowSpan(0, 0, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,2) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo buttonA = panel.getChildrenWidgets().get(0);
        buttonA.delete();
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 1, new Button('B'));",
        "    panel.setWidget(1, 1, new Button('C'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,1) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
  }

  /**
   * Test that when {@link WidgetInfo} is moved out of {@link HTMLTableInfo}, we clean up it.
   */
  public void test_moveOut_removeEmptyDimensions() throws Exception {
    final RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    {",
            "      Button button = new Button('B');",
            "      panel.setWidget(1, 1, button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "2 2 [2, 2] {(0,0)=(1,1) (1,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1)}",
        panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo buttonB = panel.getChildrenWidgets().get(1);
        frame.command_MOVE2(buttonB, null);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    {",
        "      Button button = new Button('B');",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    assertEquals("1 1 [1] {(0,0)=(1,1)}", panel.getStatus().toString());
  }

  /**
   * When {@link WidgetInfo} is moved out of {@link HTMLTableInfo}, we should remove
   * <code>setX()</code> invocations for this cell, including cleaning span in status.
   */
  public void test_moveOut_removeInvocations() throws Exception {
    final RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(1, 0, new Button('A'));",
            "    {",
            "      Button button = new Button('B');",
            "      panel.setWidget(0, 1, button);",
            "    }",
            "    panel.setWidget(1, 2, new Button('C'));",
            "    panel.getFlexCellFormatter().setRowSpan(0, 1, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "3 2 [3, 3] {(0,0)=(1,1) (1,0)=(1,2) (2,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1) (2,1)=(1,1)}",
        panel.getStatus().toString());
    // do operation
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo buttonB = panel.getChildrenWidgets().get(1);
        frame.command_MOVE2(buttonB, null);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(1, 0, new Button('A'));",
        "    panel.setWidget(1, 2, new Button('C'));",
        "    {",
        "      Button button = new Button('B');",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(
        "3 2 [3, 3] {(0,0)=(1,1) (1,0)=(1,1) (2,0)=(1,1) (0,1)=(1,1) (1,1)=(1,1) (2,1)=(1,1)}",
        panel.getStatus().toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for copy/paste {@link FlexTableInfo} with its children.
   */
  public void test_clipboard() throws Exception {
    final RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button_2 = new Button();",
            "      panel.setWidget(0, 0, button_2);",
            "    }",
            "    {",
            "      Button button_1 = new Button();",
            "      panel.setWidget(1, 1, button_1);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    {
      FlexTableInfo panel = getJavaInfoByName("panel");
      doCopyPaste(panel, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          frame.command_CREATE2(copy, null);
        }
      });
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.setWidget(0, 0, button_2);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.setWidget(1, 1, button_1);",
        "    }",
        "    {",
        "      FlexTable flexTable = new FlexTable();",
        "      rootPanel.add(flexTable);",
        "      {",
        "        Button button = new Button();",
        "        flexTable.setWidget(0, 0, button);",
        "      }",
        "      {",
        "        Button button = new Button();",
        "        flexTable.setWidget(1, 1, button);",
        "      }",
        "    }",
        "  }",
        "}");
  }
}