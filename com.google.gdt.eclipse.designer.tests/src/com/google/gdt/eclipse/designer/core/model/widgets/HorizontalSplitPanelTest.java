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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.HorizontalSplitPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.SplitPanelInfo;

import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link HorizontalSplitPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class HorizontalSplitPanelTest extends GwtModelTest {
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
  // getLeftWidget() and getRightWidget()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link HorizontalSplitPanelInfo#getLeftWidget()}.
   */
  public void test_getLeftWidget_noWidget() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "  }",
        "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    assertSame(null, panel.getLeftWidget());
  }

  /**
   * Test for {@link HorizontalSplitPanelInfo#getLeftWidget()}.
   */
  public void test_getLeftWidget_setLeftWidget() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setLeftWidget(button);",
        "    }",
        "  }",
        "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    assertSame(button, panel.getLeftWidget());
  }

  /**
   * Test for {@link HorizontalSplitPanelInfo#getLeftWidget()}.
   */
  public void test_getLeftWidget_setStartOfLineWidget() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setStartOfLineWidget(button);",
        "    }",
        "  }",
        "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    assertSame(button, panel.getLeftWidget());
  }

  /**
   * Test for {@link HorizontalSplitPanelInfo#getRightWidget()}.
   */
  public void test_getRightWidget_noWidget() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "  }",
        "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    assertSame(null, panel.getRightWidget());
  }

  /**
   * Test for {@link HorizontalSplitPanelInfo#getRightWidget()}.
   */
  public void test_getRightWidget_setRightWidget() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setRightWidget(button);",
        "    }",
        "  }",
        "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    assertSame(button, panel.getRightWidget());
  }

  /**
   * Test for {@link HorizontalSplitPanelInfo#getRightWidget()}.
   */
  public void test_getRightWidget_setEndOfLineWidget() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setEndOfLineWidget(button);",
        "    }",
        "  }",
        "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    assertSame(button, panel.getRightWidget());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Region
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getEmptyRegion_noWidgets() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "  }",
        "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    assertEquals("left", panel.getEmptyRegion());
  }

  public void test_getEmptyRegion_hasLeft() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setLeftWidget(button);",
        "    }",
        "  }",
        "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    assertEquals("right", panel.getEmptyRegion());
  }

  public void test_getEmptyRegion_hasRight() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setRightWidget(button);",
        "    }",
        "  }",
        "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    assertEquals("left", panel.getEmptyRegion());
  }

  public void test_getEmptyRegion_hasLeft_hasRight() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setLeftWidget(button);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      panel.setRightWidget(button);",
        "    }",
        "  }",
        "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    assertEquals(null, panel.getEmptyRegion());
  }

  /**
   * Region should be reflected in child {@link WidgetInfo} title.
   */
  public void test_regionInTitle() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.setLeftWidget(button_1);",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.setRightWidget(button_2);",
        "    }",
        "  }",
        "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    // check title decorations
    {
      String title = ObjectsLabelProvider.INSTANCE.getText(button_1);
      assertThat(title).startsWith("left - ");
    }
    {
      String title = ObjectsLabelProvider.INSTANCE.getText(button_2);
      assertThat(title).startsWith("right - ");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link HorizontalSplitPanelInfo#command_CREATE(WidgetInfo, String)}
   */
  public void test_CREATE() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "  }",
        "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo newButton = createButton();
    panel.command_CREATE(newButton, "left");
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setLeftWidget(button);",
        "      button.setSize('100%', '100%');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link HorizontalSplitPanelInfo#command_MOVE(WidgetInfo, String)}
   */
  public void test_MOVE_setRegion() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setLeftWidget(button);",
        "    }",
        "  }",
        "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    // initial state
    WidgetInfo button = panel.getLeftWidget();
    assertNull(panel.getRightWidget());
    // do move
    panel.command_MOVE(button, "right");
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setRightWidget(button);",
        "    }",
        "  }",
        "}");
    assertNull(panel.getLeftWidget());
    assertSame(button, panel.getRightWidget());
  }

  /**
   * Test for {@link SplitPanelInfo#command_MOVE(WidgetInfo, WidgetInfo)}.
   */
  public void test_MOVE_reorder() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button leftButton = new Button();",
        "      panel.setLeftWidget(leftButton);",
        "    }",
        "    {",
        "      Button rightButton = new Button();",
        "      panel.setRightWidget(rightButton);",
        "    }",
        "  }",
        "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo leftButton = getJavaInfoByName("leftButton");
    WidgetInfo rightButton = getJavaInfoByName("rightButton");
    //
    panel.command_MOVE(rightButton, leftButton);
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button rightButton = new Button();",
        "      panel.setRightWidget(rightButton);",
        "    }",
        "    {",
        "      Button leftButton = new Button();",
        "      panel.setLeftWidget(leftButton);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link HorizontalSplitPanelInfo#command_MOVE(WidgetInfo, String)}
   */
  public void test_MOVE_reparent() throws Exception {
    ComplexPanelInfo frame =
        parseJavaInfo(
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
            "    add(panel);",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    refresh();
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    // initial state
    WidgetInfo button = frame.getChildrenWidgets().get(1);
    assertNull(panel.getLeftWidget());
    assertNull(panel.getRightWidget());
    // do move
    panel.command_MOVE(button, "right");
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setRightWidget(button);",
        "      button.setSize('100%', '100%');",
        "    }",
        "  }",
        "}");
    assertNull(panel.getLeftWidget());
    assertSame(button, panel.getRightWidget());
  }

  /**
   * Test for copy/paste {@link HorizontalSplitPanelInfo} with its children.
   */
  public void test_clipboard() throws Exception {
    final ComplexPanelInfo frame =
        parseJavaInfo(
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "    {",
            "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
            "      add(panel);",
            "      {",
            "        Button button = new Button();",
            "        panel.setRightWidget(button);",
            "        button.setSize('100%', '100%');",
            "      }",
            "    }",
            "  }",
            "}");
    refresh();
    // do copy/paste
    {
      HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
      doCopyPaste(panel, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          flowContainer_CREATE(frame, copy, null);
        }
      });
    }
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      add(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.setRightWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "    {",
        "      HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();",
        "      add(horizontalSplitPanel);",
        "      {",
        "        Button button = new Button();",
        "        horizontalSplitPanel.setRightWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
  }
}