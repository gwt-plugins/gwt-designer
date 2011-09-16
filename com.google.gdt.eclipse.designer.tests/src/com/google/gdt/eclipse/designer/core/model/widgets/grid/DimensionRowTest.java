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
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.RowInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.RowInfo.Alignment;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

/**
 * Test for {@link RowInfo}.
 * 
 * @author scheglov_ke
 */
public class DimensionRowTest extends GwtModelTest {
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
   * Test for {@link RowInfo#getIndex()}.
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
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(0, panel.getRows().get(0).getIndex());
    assertEquals(1, panel.getRows().get(1).getIndex());
  }

  /**
   * Test for {@link RowInfo#isLast()}.
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
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(false, panel.getRows().get(0).isLast());
    assertEquals(true, panel.getRows().get(1).isLast());
  }

  /**
   * Test for {@link RowInfo#isEmpty()}.
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
            "    panel.setWidget(2, 0, new Button('B'));",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    assertThat(panel.getRows()).hasSize(3);
    assertFalse(panel.getRows().get(0).isEmpty());
    assertTrue(panel.getRows().get(1).isEmpty());
    assertFalse(panel.getRows().get(2).isEmpty());
  }

  /**
   * Test for {@link Alignment#getSmallImage()}.
   */
  public void test_Alignment_getSmallImage() throws Exception {
    assertNotNull(Alignment.UNKNOWN.getSmallImage());
    assertNotNull(Alignment.TOP.getSmallImage());
    assertNotNull(Alignment.MIDDLE.getSmallImage());
    assertNotNull(Alignment.BOTTOM.getSmallImage());
  }

  /**
   * Test for {@link Alignment#getMenuImage()}.
   */
  public void test_Alignment_getMenuImage() throws Exception {
    assertNotNull(Alignment.UNKNOWN.getMenuImage());
    assertNotNull(Alignment.TOP.getMenuImage());
    assertNotNull(Alignment.MIDDLE.getMenuImage());
    assertNotNull(Alignment.BOTTOM.getMenuImage());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getAlignment()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link RowInfo#getAlignment()}, <code>TOP</code> alignment.
   */
  public void test_getAlignment_setV_TOP() throws Exception {
    check_getAlignment_setV("ALIGN_TOP", RowInfo.Alignment.TOP);
  }

  /**
   * Test for {@link RowInfo#getAlignment()}, <code>CENTER</code> alignment.
   */
  public void test_getAlignment_setV_MIDDLE() throws Exception {
    check_getAlignment_setV("ALIGN_MIDDLE", RowInfo.Alignment.MIDDLE);
  }

  /**
   * Test for {@link RowInfo#getAlignment()}, <code>BOTTOM</code> alignment.
   */
  public void test_getAlignment_setV_BOTTOM() throws Exception {
    check_getAlignment_setV("ALIGN_BOTTOM", RowInfo.Alignment.BOTTOM);
  }

  /**
   * Test for {@link RowInfo#getAlignment()}, <code>UNKNOWN</code> alignment, because no alignment
   * set.
   */
  public void test_getAlignment_setH_UNKNOWN_0() throws Exception {
    check_getAlignment_setV(null, RowInfo.Alignment.UNKNOWN);
  }

  /**
   * Test for {@link RowInfo#getAlignment()}, for two different rows.
   */
  public void test_getAlignment_setV_2() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.setWidget(1, 0, new Button('B'));",
            "    panel.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);",
            "    panel.getRowFormatter().setVerticalAlign(1, HasVerticalAlignment.ALIGN_BOTTOM);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    assertThat(panel.getRows()).hasSize(2);
    assertEquals(RowInfo.Alignment.TOP, panel.getRows().get(0).getAlignment());
    assertEquals(RowInfo.Alignment.BOTTOM, panel.getRows().get(1).getAlignment());
  }

  /**
   * Test for {@link RowInfo#getAlignment()}, when alignment is set using
   * <code>CellFormatter.setVerticalAlignment()</code>.
   */
  private void check_getAlignment_setV(String gwtAlignString, Alignment expectedAlignment)
      throws Exception {
    String setAlignmentLine;
    if (gwtAlignString != null) {
      setAlignmentLine =
          "    panel.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment."
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
    assertThat(panel.getRows()).hasSize(1);
    assertEquals(expectedAlignment, panel.getRows().get(0).getAlignment());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setAlignment
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setAlignment_UNKNOWN() throws Exception {
    check_setAlignment(null, RowInfo.Alignment.UNKNOWN);
  }

  public void test_setAlignment_TOP() throws Exception {
    check_setAlignment("ALIGN_TOP", RowInfo.Alignment.TOP);
  }

  public void test_setAlignment_MIDDLE() throws Exception {
    check_setAlignment("ALIGN_MIDDLE", RowInfo.Alignment.MIDDLE);
  }

  public void test_setAlignment_BOTTOM() throws Exception {
    check_setAlignment("ALIGN_BOTTOM", RowInfo.Alignment.BOTTOM);
  }

  /**
   * Test for {@link RowInfo#setAlignment(Alignment)}, replace existing alignment.
   */
  private void check_setAlignment(String expectedAlignString, final Alignment alignment)
      throws Exception {
    String setAlignmentLine;
    if (expectedAlignString != null) {
      setAlignmentLine =
          "    panel.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment."
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
            "    panel.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_BOTTOM);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    final HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    //
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.getRows().get(0).setAlignment(alignment);
      }
    });
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
   * Test for {@link RowInfo#setAlignment(Alignment)}, add new
   * <code>RowFormatter.setVerticalAlign()</code>.
   */
  public void test_setAlignment() throws Exception {
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
    //
    ExecutionUtils.run(frame, new RunnableEx() {
      public void run() throws Exception {
        panel.getRows().get(0).setAlignment(RowInfo.Alignment.TOP);
      }
    });
    String expectedSource =
        getTestSource(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button('A'));",
            "    panel.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);",
            "  }",
            "}");
    expectedSource = StringUtils.replace(expectedSource, "// no line\n", "");
    assertEditor(expectedSource, m_lastEditor);
  }
}