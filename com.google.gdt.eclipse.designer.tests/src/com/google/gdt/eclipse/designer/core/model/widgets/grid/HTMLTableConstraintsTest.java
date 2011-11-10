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

import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.CellConstraintsSupport;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.ColumnInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.RowInfo;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import org.apache.commons.lang.StringUtils;

/**
 * Test for {@link HTMLTableInfo} and {@link CellConstraintsSupport}.
 * 
 * @author scheglov_ke
 */
public class HTMLTableConstraintsTest extends GwtModelTest {
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
  // getConstraints()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getCC_newComponent() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(2, 3);",
            "    rootPanel.add(panel);",
            "    panel.setWidget(1, 2, new Button());",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    // prepare new Button, associate it with "panel"
    WidgetInfo newButton = createButton();
    panel.addChild(newButton);
    // check CellConstraintsSupport
    CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(newButton);
    assertEquals(-1, constraints.getX());
    assertEquals(-1, constraints.getY());
    assertEquals(1, constraints.getWidth());
    assertEquals(1, constraints.getHeight());
  }

  /**
   * Test for {@link CellConstraintsSupport}, location.
   */
  public void test_getCC_location_noSpan() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Grid panel = new Grid(2, 3);",
            "    rootPanel.add(panel);",
            "    panel.setWidget(1, 2, new Button());",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    // check CellConstraintsSupport
    CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(button);
    assertEquals(2, constraints.getX());
    assertEquals(1, constraints.getY());
    assertEquals(1, constraints.getWidth());
    assertEquals(1, constraints.getHeight());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getHorizontalAlignment()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getHorizontalAlignment_LEFT() throws Exception {
    check_getHorizontalAlignment_setH("ALIGN_LEFT", ColumnInfo.Alignment.LEFT);
  }

  public void test_getHorizontalAlignment_CENTER() throws Exception {
    check_getHorizontalAlignment_setH("ALIGN_CENTER", ColumnInfo.Alignment.CENTER);
  }

  public void test_getHorizontalAlignment_RIGHT() throws Exception {
    check_getHorizontalAlignment_setH("ALIGN_RIGHT", ColumnInfo.Alignment.RIGHT);
  }

  public void test_getHorizontalAlignment_UNKNOWN() throws Exception {
    check_getHorizontalAlignment_setH(null, ColumnInfo.Alignment.UNKNOWN);
  }

  /**
   * Test for {@link CellConstraintsSupport#getHorizontalAlignment()}, when alignment is set using
   * <code>CellFormatter.setHorizontalAlignment()</code>.
   */
  private void check_getHorizontalAlignment_setH(String gwtAlignString,
      ColumnInfo.Alignment expectedAlignment) throws Exception {
    String setAlignmentLine;
    if (gwtAlignString != null) {
      setAlignmentLine =
          "    panel.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment."
              + gwtAlignString
              + ");";
    } else {
      setAlignmentLine = "";
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
            setAlignmentLine,
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(button);
    assertSame(expectedAlignment, constraints.getHorizontalAlignment());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setHorizontalAlignment()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setHorizontalAlignment_LEFT() throws Exception {
    check_setHorizontalAlignment("ALIGN_LEFT", ColumnInfo.Alignment.LEFT);
  }

  public void test_setHorizontalAlignment_CENTER() throws Exception {
    check_setHorizontalAlignment("ALIGN_CENTER", ColumnInfo.Alignment.CENTER);
  }

  public void test_setHorizontalAlignment_RIGHT() throws Exception {
    check_setHorizontalAlignment("ALIGN_RIGHT", ColumnInfo.Alignment.RIGHT);
  }

  public void test_setHorizontalAlignment_UNKNOWN() throws Exception {
    check_setHorizontalAlignment(null, ColumnInfo.Alignment.UNKNOWN);
  }

  /**
   * Test for {@link CellConstraintsSupport#setHorizontalAlignment(ColumnInfo.Alignment)}.
   */
  private void check_setHorizontalAlignment(String expectedAlignString,
      final ColumnInfo.Alignment alignment) throws Exception {
    String setAlignmentLine;
    if (expectedAlignString != null) {
      setAlignmentLine =
          "    panel.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment."
              + expectedAlignString
              + ");";
    } else {
      setAlignmentLine = "// no line";
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
            "    panel.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo button = panel.getChildrenWidgets().get(0);
        CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(button);
        constraints.setHorizontalAlignment(alignment);
      }
    });
    //
    String expectedSource =
        getTestSource(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            setAlignmentLine,
            "  }",
            "}");
    expectedSource = StringUtils.replace(expectedSource, "// no line\n", "");
    assertEditor(expectedSource, m_lastEditor);
  }

  /**
   * Test for {@link CellConstraintsSupport#setHorizontalAlignment(ColumnInfo.Alignment)}.
   */
  public void test_setHorizontalAlignment_VH() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo button = panel.getChildrenWidgets().get(0);
        CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(button);
        constraints.setHorizontalAlignment(ColumnInfo.Alignment.RIGHT);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);",
        "    panel.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getVerticalAlignment()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getVerticalAlignment_TOP() throws Exception {
    check_getVerticalAlignment_setV("ALIGN_TOP", RowInfo.Alignment.TOP);
  }

  public void test_getVerticalAlignment_MIDDLE() throws Exception {
    check_getVerticalAlignment_setV("ALIGN_MIDDLE", RowInfo.Alignment.MIDDLE);
  }

  public void test_getVerticalAlignment_BOTTOM() throws Exception {
    check_getVerticalAlignment_setV("ALIGN_BOTTOM", RowInfo.Alignment.BOTTOM);
  }

  public void test_getVerticalAlignment_UNKNOWN() throws Exception {
    check_getVerticalAlignment_setV(null, RowInfo.Alignment.UNKNOWN);
  }

  /**
   * Test for {@link CellConstraintsSupport#getVerticalAlignment()}, when alignment is set using
   * <code>CellFormatter.setVerticalAlignment()</code>.
   */
  private void check_getVerticalAlignment_setV(String gwtAlignString,
      RowInfo.Alignment expectedAlignment) throws Exception {
    String setAlignmentLine;
    if (gwtAlignString != null) {
      setAlignmentLine =
          "    panel.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment."
              + gwtAlignString
              + ");";
    } else {
      setAlignmentLine = "";
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
            setAlignmentLine,
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(button);
    assertSame(expectedAlignment, constraints.getVerticalAlignment());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Complex horizontal/vertical alignments
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CellConstraintsSupport#getHorizontalAlignment()}, when alignment is set using
   * <code>CellFormatter.setAlignment()</code>.
   */
  public void test_getAlignment_setHV() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(button);
    assertSame(ColumnInfo.Alignment.LEFT, constraints.getHorizontalAlignment());
    assertSame(RowInfo.Alignment.TOP, constraints.getVerticalAlignment());
  }

  /**
   * Test for {@link ColumnInfo.Alignment#FILL}.
   */
  public void test_horizontalAlignment_fill() throws Exception {
    HTMLTableInfo panel =
        parseJavaInfo(
            "public class Test extends FlexTable {",
            "  public Test() {",
            "    Button button = new Button('A');",
            "    setWidget(0, 0, button);",
            "    button.setWidth('100%');",
            "  }",
            "}");
    refresh();
    //
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(button);
    // FILL initially
    {
      ColumnInfo.Alignment alignment = constraints.getHorizontalAlignment();
      assertSame(ColumnInfo.Alignment.FILL, alignment);
      // check properties
      assertNotNull(alignment.getSmallImage());
      assertNotNull(alignment.getMenuImage());
      assertEquals("N/A", alignment.getAlignmentField());
      assertEquals("N/A", alignment.getAlignmentString());
    }
    // set RIGHT, removes "width=100%"
    setHorizontalAlignment(button, ColumnInfo.Alignment.RIGHT);
    assertEditor(
        "public class Test extends FlexTable {",
        "  public Test() {",
        "    Button button = new Button('A');",
        "    setWidget(0, 0, button);",
        "    getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);",
        "  }",
        "}");
    // set "width=150px" and LEFT, keep "width=150px"
    button.getSizeSupport().setSize("150px", null);
    setHorizontalAlignment(button, ColumnInfo.Alignment.LEFT);
    assertEditor(
        "public class Test extends FlexTable {",
        "  public Test() {",
        "    Button button = new Button('A');",
        "    setWidget(0, 0, button);",
        "    button.setWidth('150px');",
        "    getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);",
        "  }",
        "}");
    // set FILL
    setHorizontalAlignment(button, ColumnInfo.Alignment.FILL);
    assertSame(ColumnInfo.Alignment.FILL, constraints.getHorizontalAlignment());
    assertEditor(
        "public class Test extends FlexTable {",
        "  public Test() {",
        "    Button button = new Button('A');",
        "    setWidget(0, 0, button);",
        "    button.setWidth('100%');",
        "  }",
        "}");
  }

  /**
   * Test for {@link RowInfo.Alignment#FILL}.
   */
  public void test_verticalAlignment_fill() throws Exception {
    HTMLTableInfo panel =
        parseJavaInfo(
            "public class Test extends FlexTable {",
            "  public Test() {",
            "    Button button = new Button('A');",
            "    setWidget(0, 0, button);",
            "    button.setHeight('100%');",
            "  }",
            "}");
    refresh();
    //
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(button);
    // FILL initially
    {
      RowInfo.Alignment alignment = constraints.getVerticalAlignment();
      assertSame(RowInfo.Alignment.FILL, alignment);
      // check properties
      assertNotNull(alignment.getSmallImage());
      assertNotNull(alignment.getMenuImage());
      assertEquals("N/A", alignment.getAlignmentField());
      assertEquals("N/A", alignment.getAlignmentString());
    }
    // set BOTTOM, removes "height=100%"
    setVerticalAlignment(button, RowInfo.Alignment.BOTTOM);
    assertEditor(
        "public class Test extends FlexTable {",
        "  public Test() {",
        "    Button button = new Button('A');",
        "    setWidget(0, 0, button);",
        "    getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_BOTTOM);",
        "  }",
        "}");
    // set "height=150px" and TOP, keep "height=150px"
    button.getSizeSupport().setSize(null, "150px");
    setVerticalAlignment(button, RowInfo.Alignment.TOP);
    assertEditor(
        "public class Test extends FlexTable {",
        "  public Test() {",
        "    Button button = new Button('A');",
        "    setWidget(0, 0, button);",
        "    button.setHeight('150px');",
        "    getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);",
        "  }",
        "}");
    // set FILL
    setVerticalAlignment(button, RowInfo.Alignment.FILL);
    assertSame(RowInfo.Alignment.FILL, constraints.getVerticalAlignment());
    assertEditor(
        "public class Test extends FlexTable {",
        "  public Test() {",
        "    Button button = new Button('A');",
        "    setWidget(0, 0, button);",
        "    button.setHeight('100%');",
        "  }",
        "}");
  }

  /**
   * Calls {@link CellConstraintsSupport#setHorizontalAlignment(ColumnInfo.Alignment)} in edit
   * operation.
   */
  private static void setHorizontalAlignment(final WidgetInfo widget,
      final ColumnInfo.Alignment alignment) {
    ExecutionUtils.run(widget, new RunnableEx() {
      @Override
      public void run() throws Exception {
        CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(widget);
        constraints.setHorizontalAlignment(alignment);
      }
    });
  }

  /**
   * Calls {@link CellConstraintsSupport#setVerticalAlignment(ColumnInfo.Alignment)} in edit
   * operation.
   */
  private static void setVerticalAlignment(final WidgetInfo widget,
      final RowInfo.Alignment alignment) {
    ExecutionUtils.run(widget, new RunnableEx() {
      @Override
      public void run() throws Exception {
        CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(widget);
        constraints.setVerticalAlignment(alignment);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setVerticalAlignment()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setVerticalAlignment_TOP() throws Exception {
    check_setVerticalAlignment("ALIGN_TOP", RowInfo.Alignment.TOP);
  }

  public void test_setVerticalAlignment_MIDDLE() throws Exception {
    check_setVerticalAlignment("ALIGN_MIDDLE", RowInfo.Alignment.MIDDLE);
  }

  public void test_setVerticalAlignment_BOTTOM() throws Exception {
    check_setVerticalAlignment("ALIGN_BOTTOM", RowInfo.Alignment.BOTTOM);
  }

  public void test_setVerticalAlignment_UNKNOWN() throws Exception {
    check_setVerticalAlignment(null, RowInfo.Alignment.UNKNOWN);
  }

  /**
   * Test for {@link CellConstraintsSupport#setVerticalAlignment(RowInfo.Alignment)}.
   */
  private void check_setVerticalAlignment(String expectedAlignString,
      final RowInfo.Alignment alignment) throws Exception {
    String setAlignmentLine;
    if (expectedAlignString != null) {
      setAlignmentLine =
          "    panel.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment."
              + expectedAlignString
              + ");";
    } else {
      setAlignmentLine = "// no line";
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
            "    panel.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo button = panel.getChildrenWidgets().get(0);
        CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(button);
        constraints.setVerticalAlignment(alignment);
      }
    });
    //
    String expectedSource =
        getTestSource(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            setAlignmentLine,
            "  }",
            "}");
    expectedSource = StringUtils.replace(expectedSource, "// no line\n", "");
    assertEditor(expectedSource, m_lastEditor);
  }

  /**
   * Test for {@link CellConstraintsSupport#setVerticalAlignment(MigVerticalInfo.Alignment)}.
   */
  public void test_setVerticalAlignment_VH() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo button = panel.getChildrenWidgets().get(0);
        CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(button);
        constraints.setVerticalAlignment(RowInfo.Alignment.BOTTOM);
      }
    });
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button('A'));",
        "    panel.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);",
        "    panel.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_BOTTOM);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_alignmentContextMenuHorizontal_LEFT() throws Exception {
    check_alignmentContextMenuHorizontal("Left", ColumnInfo.Alignment.LEFT);
  }

  public void test_alignmentContextMenuHorizontal_CENTER() throws Exception {
    check_alignmentContextMenuHorizontal("Center", ColumnInfo.Alignment.CENTER);
  }

  public void test_alignmentContextMenuHorizontal_RIGHT() throws Exception {
    check_alignmentContextMenuHorizontal("Right", ColumnInfo.Alignment.RIGHT);
  }

  public void test_alignmentContextMenuVertical_TOP() throws Exception {
    check_alignmentContextMenuVertical("Top", RowInfo.Alignment.TOP);
  }

  public void test_alignmentContextMenuVertical_MIDDLE() throws Exception {
    check_alignmentContextMenuVertical("Middle", RowInfo.Alignment.MIDDLE);
  }

  public void test_alignmentContextMenuVertical_BOTTOM() throws Exception {
    check_alignmentContextMenuVertical("Bottom", RowInfo.Alignment.BOTTOM);
  }

  /**
   * Test for horizontal alignment actions.
   */
  private void check_alignmentContextMenuHorizontal(String actionText,
      ColumnInfo.Alignment expectedHorizontalAlignment) throws Exception {
    check_alignmentContextMenu(
        "Horizontal alignment",
        actionText,
        expectedHorizontalAlignment,
        RowInfo.Alignment.UNKNOWN);
  }

  /**
   * Test for vertical alignment actions.
   */
  private void check_alignmentContextMenuVertical(String actionText,
      RowInfo.Alignment expectedVerticalAlignment) throws Exception {
    check_alignmentContextMenu(
        "Vertical alignment",
        actionText,
        ColumnInfo.Alignment.UNKNOWN,
        expectedVerticalAlignment);
  }

  /**
   * Test for alignment actions.
   */
  private void check_alignmentContextMenu(final String managerText,
      final String actionText,
      final ColumnInfo.Alignment expectedHorizontalAlignment,
      final RowInfo.Alignment expectedVerticalAlignment) throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    final WidgetInfo button = panel.getChildrenWidgets().get(0);
    //
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        // prepare alignment manager
        final IMenuManager alignmentManager;
        {
          MenuManager contextMenu = getDesignerMenuManager();
          panel.getBroadcastObject().addContextMenu(ImmutableList.of(button), button, contextMenu);
          alignmentManager = findChildMenuManager(contextMenu, managerText);
          assertNotNull(alignmentManager);
        }
        // set alignment
        IAction alignmentAction = findChildAction(alignmentManager, actionText);
        assertNotNull(actionText, alignmentAction);
        alignmentAction.setChecked(true);
        alignmentAction.run();
      }
    });
    // check result
    {
      CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(button);
      assertSame(expectedHorizontalAlignment, constraints.getHorizontalAlignment());
      assertSame(expectedVerticalAlignment, constraints.getVerticalAlignment());
    }
  }
}