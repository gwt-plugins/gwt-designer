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
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.ColumnInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.ColumnInfo.Alignment;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

/**
 * Test for {@link ColumnInfo}.
 * 
 * @author scheglov_ke
 */
public class DimensionColumnTest extends GwtModelTest {
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
  /**
   * Test for {@link ColumnInfo#getIndex()}.
   */
  public void test_getIndex() throws Exception {
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
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    assertThat(panel.getColumns()).hasSize(2);
    assertEquals(0, panel.getColumns().get(0).getIndex());
    assertEquals(1, panel.getColumns().get(1).getIndex());
  }

  /**
   * Test for {@link ColumnInfo#isLast()}.
   */
  public void test_isLast() throws Exception {
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
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    assertThat(panel.getColumns()).hasSize(2);
    assertEquals(false, panel.getColumns().get(0).isLast());
    assertEquals(true, panel.getColumns().get(1).isLast());
  }

  /**
   * Test for {@link ColumnInfo#isEmpty()}.
   */
  public void test_isEmpty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(0, 2, new Button('B'));",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    assertThat(panel.getColumns()).hasSize(3);
    assertFalse(panel.getColumns().get(0).isEmpty());
    assertTrue(panel.getColumns().get(1).isEmpty());
    assertFalse(panel.getColumns().get(2).isEmpty());
  }

  /**
   * Test for {@link Alignment#getSmallImage()}.
   */
  public void test_Alignment_getSmallImage() throws Exception {
    assertNotNull(Alignment.UNKNOWN.getSmallImage());
    assertNotNull(Alignment.LEFT.getSmallImage());
    assertNotNull(Alignment.CENTER.getSmallImage());
    assertNotNull(Alignment.RIGHT.getSmallImage());
  }

  /**
   * Test for {@link Alignment#getMenuImage()}.
   */
  public void test_Alignment_getMenuImage() throws Exception {
    assertNotNull(Alignment.UNKNOWN.getMenuImage());
    assertNotNull(Alignment.LEFT.getMenuImage());
    assertNotNull(Alignment.CENTER.getMenuImage());
    assertNotNull(Alignment.RIGHT.getMenuImage());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getAlignment()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ColumnInfo#getAlignment()}, <code>LEFT</code> alignment.
   */
  public void test_getAlignment_setH_LEFT() throws Exception {
    check_getAlignment_setH("ALIGN_LEFT", ColumnInfo.Alignment.LEFT);
  }

  /**
   * Test for {@link ColumnInfo#getAlignment()}, <code>CENTER</code> alignment.
   */
  public void test_getAlignment_setH_CENTER() throws Exception {
    check_getAlignment_setH("ALIGN_CENTER", ColumnInfo.Alignment.CENTER);
  }

  /**
   * Test for {@link ColumnInfo#getAlignment()}, <code>RIGHT</code> alignment.
   */
  public void test_getAlignment_setH_RIGHT() throws Exception {
    check_getAlignment_setH("ALIGN_RIGHT", ColumnInfo.Alignment.RIGHT);
  }

  /**
   * Test for {@link ColumnInfo#getAlignment()}, <code>UNKNOWN</code> alignment, because no
   * alignment set.
   */
  public void test_getAlignment_setH_UNKNOWN_0() throws Exception {
    check_getAlignment_setH(null, ColumnInfo.Alignment.UNKNOWN);
  }

  /**
   * Test for {@link ColumnInfo#getAlignment()}, <code>UNKNOWN</code> alignment, because different
   * alignments for rows are set.
   */
  public void test_getAlignment_setH_UNKNOWN_2() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 0, new Button('B'));",
            "    panel.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);",
            "    panel.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    assertThat(panel.getColumns()).hasSize(1);
    assertEquals(ColumnInfo.Alignment.UNKNOWN, panel.getColumns().get(0).getAlignment());
  }

  /**
   * Test for {@link ColumnInfo#getAlignment()}, for two different columns.
   */
  public void test_getAlignment_setH_2() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(0, 1, new Button('B'));",
            "    panel.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);",
            "    panel.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    assertThat(panel.getColumns()).hasSize(2);
    assertEquals(ColumnInfo.Alignment.LEFT, panel.getColumns().get(0).getAlignment());
    assertEquals(ColumnInfo.Alignment.RIGHT, panel.getColumns().get(1).getAlignment());
  }

  /**
   * Test for {@link ColumnInfo#getAlignment()}, <code>LEFT</code> alignment.
   */
  public void test_getAlignment_setHV_LEFT() throws Exception {
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
    assertThat(panel.getColumns()).hasSize(1);
    assertEquals(ColumnInfo.Alignment.LEFT, panel.getColumns().get(0).getAlignment());
  }

  /**
   * Test for {@link ColumnInfo#getAlignment()}, when alignment is set using
   * <code>CellFormatter.setHorizontalAlignment()</code>.
   */
  private void check_getAlignment_setH(String gwtAlignString, Alignment expectedAlignment)
      throws Exception {
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
    assertThat(panel.getColumns()).hasSize(1);
    assertEquals(expectedAlignment, panel.getColumns().get(0).getAlignment());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setAlignment
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setAlignment_UNKNOWN() throws Exception {
    check_setAlignment(null, ColumnInfo.Alignment.UNKNOWN);
  }

  public void test_setAlignment_LEFT() throws Exception {
    check_setAlignment("ALIGN_LEFT", ColumnInfo.Alignment.LEFT);
  }

  public void test_setAlignment_CENTER() throws Exception {
    check_setAlignment("ALIGN_CENTER", ColumnInfo.Alignment.CENTER);
  }

  public void test_setAlignment_RIGHT() throws Exception {
    check_setAlignment("ALIGN_RIGHT", ColumnInfo.Alignment.RIGHT);
  }

  /**
   * Test for {@link ColumnInfo#setAlignment(Alignment)}.
   */
  private void check_setAlignment(String expectedAlignString, final Alignment alignment)
      throws Exception {
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
            "    panel.setWidget(0, 1, new Button('B'));",
            "    panel.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);",
            "    panel.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    assertThat(panel.getColumns()).hasSize(2);
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.getColumns().get(0).setAlignment(alignment);
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
            "    panel.setWidget(0, 1, new Button('B'));",
            "    panel.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);",
            setAlignmentLine,
            "  }",
            "}");
    expectedSource = StringUtils.replace(expectedSource, "// no line\n", "");
    assertEditor(expectedSource, m_lastEditor);
  }

  /**
   * Test for {@link ColumnInfo#setAlignment(Alignment)}.<br>
   * When <code>CellFormatter.setAlignment()</code> used, it should be replaced with
   * <code>CellFormatter.setVerticalAlignment()</code>.
   */
  public void test_setAlignment_setBothAlignments() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 0, new Button('B'));",
            "    panel.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    assertThat(panel.getColumns()).hasSize(1);
    assertThat(panel.getRows()).hasSize(2);
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.getColumns().get(0).setAlignment(Alignment.RIGHT);
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
        "    panel.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);",
        "    panel.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);",
        "    panel.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);",
        "  }",
        "}");
  }

  /**
   * Test for {@link ColumnInfo#setAlignment(Alignment)}.<br>
   * Skip spanned rows.
   */
  public void test_setAlignment_setRowSpan() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 1, new Button('B'));",
            "    panel.setWidget(2, 0, new Button('C'));",
            "    panel.getFlexCellFormatter().setRowSpan(0, 0, 2);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    assertThat(panel.getColumns()).hasSize(2);
    assertThat(panel.getRows()).hasSize(3);
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.getColumns().get(0).setAlignment(Alignment.RIGHT);
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
        "    panel.setWidget(2, 0, new Button('C'));",
        "    panel.getFlexCellFormatter().setRowSpan(0, 0, 2);",
        "    panel.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);",
        "    panel.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_RIGHT);",
        "  }",
        "}");
  }
}