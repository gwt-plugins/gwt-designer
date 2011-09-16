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
package com.google.gdt.eclipse.designer.core.model.widgets.grid;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.GridInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;

import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.model.util.MorphingSupport;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link GridInfo}.
 * 
 * @author scheglov_ke
 */
public class GridTest extends GwtModelTest {
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
  // Low level rows/columns access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link HTMLTableInfo#low_setRowCount(int)}.<br>
   */
  public void test_setRowCount() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(0, 0);",
            "    rootPanel.add(panel);",
            "    panel.resize(0, 0);",
            "    panel.resizeRows(0);",
            "    panel.resizeColumns(0);",
            "  }",
            "}");
    frame.refresh();
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    panel.getStatus().setRowCount(2);
    assertEquals(2, panel.getStatus().getRowCount());
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    Grid panel = new Grid(2, 0);",
        "    rootPanel.add(panel);",
        "    panel.resize(2, 0);",
        "    panel.resizeRows(2);",
        "    panel.resizeColumns(0);",
        "  }",
        "}");
  }

  /**
   * Test for {@link HTMLTableInfo#low_setColumnCount(int)}.<br>
   */
  public void test_setColumnCount() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(0, 0);",
            "    rootPanel.add(panel);",
            "    panel.resize(0, 0);",
            "    panel.resizeRows(0);",
            "    panel.resizeColumns(0);",
            "  }",
            "}");
    frame.refresh();
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    panel.getStatus().setColumnCount(2);
    assertEquals(2, panel.getStatus().getColumnCount());
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    Grid panel = new Grid(0, 2);",
        "    rootPanel.add(panel);",
        "    panel.resize(0, 2);",
        "    panel.resizeRows(0);",
        "    panel.resizeColumns(2);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link HTMLTableInfo#deleteColumn(int)}.
   */
  public void test_COLUMN_delete() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(2, 2);",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('B'));",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    // do operation
    panel.deleteColumn(0);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    Grid panel = new Grid(2, 1);",
        "    rootPanel.add(panel);",
        "    panel.setWidget(1, 0, new Button('B'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(2);
  }

  /**
   * Test for {@link HTMLTableInfo#clearColumn(int)}.
   */
  public void test_COLUMN_clear() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(2, 2);",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('B'));",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
    // do operation
    panel.clearColumn(0);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    Grid panel = new Grid(2, 2);",
        "    rootPanel.add(panel);",
        "    panel.setWidget(1, 1, new Button('B'));",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(2);
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
  public void test_CREATE_0() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(2, 3);",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = getJavaInfoByName("panel");
    // do CREATE
    final WidgetInfo newButton = createButton();
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        panel.command_CREATE(newButton, 2, false, 1, false);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    Grid panel = new Grid(2, 3);",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setWidget(1, 2, button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link HTMLTableInfo#command_CREATE(WidgetInfo, int, boolean, int, boolean)}.<br>
   * Has reference, with inserts.
   */
  public void test_CREATE_insertMove() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    Grid panel = new Grid(2, 3);",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.setWidget(1, 2, new Button('B'));",
        "  }",
        "}");
    refresh();
    final HTMLTableInfo panel = getJavaInfoByName("panel");
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(2);
    // do CREATE
    final WidgetInfo newButton = createButton();
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        panel.command_CREATE(newButton, 1, true, 1, true);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    Grid panel = new Grid(3, 4);",
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
            "    Grid panel = new Grid(1, 1);",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(1);
    // do CREATE
    WidgetInfo newButton = createButton();
    panel.command_CREATE(newButton, 2, false, 1, false);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    Grid panel = new Grid(2, 3);",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setWidget(1, 2, button);",
        "    }",
        "  }",
        "}");
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(2);
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
            "    Grid panel = new Grid(2, 3);",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button_1 = new Button();",
            "      panel.setWidget(0, 1, button_1);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      panel.setWidget(1, 2, button_2);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getColumns()).hasSize(3);
    assertThat(panel.getRows()).hasSize(2);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    // do MOVE
    panel.command_MOVE(button_2, 0, false, 0, false);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    Grid panel = new Grid(1, 2);",
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
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for copy/paste {@link GridInfo} with its children.
   */
  public void test_clipboard() throws Exception {
    final RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(2, 2);",
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
      GridInfo panel = getJavaInfoByName("panel");
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
        "    Grid panel = new Grid(2, 2);",
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
        "      Grid grid = new Grid(2, 2);",
        "      rootPanel.add(grid);",
        "      {",
        "        Button button = new Button();",
        "        grid.setWidget(0, 0, button);",
        "      }",
        "      {",
        "        Button button = new Button();",
        "        grid.setWidget(1, 1, button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Morphing
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_morphInto_Grid() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      FlexTable panel = new FlexTable();",
            "      rootPanel.add(panel);",
            "      panel.setWidget(0, 0, new Button('A'));",
            "      panel.setWidget(1, 2, new Button('B'));",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    Class<?> classGrid = m_lastLoader.loadClass("com.google.gwt.user.client.ui.Grid");
    MorphingTargetDescription target = new MorphingTargetDescription(classGrid, null);
    MorphingSupport.morph("com.google.gwt.user.client.ui.Widget", panel, target);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Grid panel = new Grid();",
        "      panel.resize(2, 3);",
        "      rootPanel.add(panel);",
        "      panel.setWidget(0, 0, new Button('A'));",
        "      panel.setWidget(1, 2, new Button('B'));",
        "    }",
        "  }",
        "}");
  }
}