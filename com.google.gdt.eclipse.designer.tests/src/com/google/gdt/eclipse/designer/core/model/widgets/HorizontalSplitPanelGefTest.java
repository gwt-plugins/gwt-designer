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

import com.google.gdt.eclipse.designer.core.model.widgets.generic.GwtGefTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.HorizontalSplitPanelInfo;

/**
 * Test for {@link HorizontalSplitPanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class HorizontalSplitPanelGefTest extends GwtGefTest {
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
  // Canvas.CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE_left() throws Exception {
    check_canvas_CREATE(0.1, 0.5, "setLeftWidget");
  }

  public void test_canvas_CREATE_right() throws Exception {
    check_canvas_CREATE(0.9, 0.5, "setRightWidget");
  }

  private void check_canvas_CREATE(double x, double y, String method) throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "    }",
        "  }",
        "}");
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    loadButton();
    canvas.moveTo(panel, x, y).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel." + method + "(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Only one "left" allowed.
   */
  public void test_canvas_CREATE_usedLeft() throws Exception {
    check_canvas_CREATE_used(0.1, 0.5, "setLeftWidget");
  }

  /**
   * Only one "right" allowed.
   */
  public void test_canvas_CREATE_usedRight() throws Exception {
    check_canvas_CREATE_used(0.9, 0.5, "setRightWidget");
  }

  private void check_canvas_CREATE_used(double x, double y, String method) throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel." + method + "(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    loadButton();
    canvas.moveTo(panel, x, y);
    canvas.assertCommandNull();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Canvas.PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_PASTE_onIt() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button existing = new Button();",
        "        panel.setLeftWidget(existing);",
        "        existing.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo existing = getJavaInfoByName("existing");
    //
    doCopyPaste(existing);
    canvas.moveTo(panel, 0.9, 0.5).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button existing = new Button();",
        "        panel.setLeftWidget(existing);",
        "        existing.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button button = new Button();",
        "        panel.setRightWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_PASTE_it() throws Exception {
    ComplexPanelInfo flowPanel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "    {",
            "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
            "      add(panel);",
            "      panel.setSize('150px', '100px');",
            "      {",
            "        Button button = new Button('left');",
            "        panel.setLeftWidget(button);",
            "        button.setSize('100%', '100%');",
            "      }",
            "      {",
            "        Button button = new Button('right');",
            "        panel.setRightWidget(button);",
            "        button.setSize('100%', '100%');",
            "      }",
            "    }",
            "  }",
            "}");
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    // do copy/paste
    doCopyPaste(panel);
    canvas.moveTo(flowPanel, 0.9, 0.1).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      add(panel);",
        "      panel.setSize('150px', '100px');",
        "      {",
        "        Button button = new Button('left');",
        "        panel.setLeftWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button button = new Button('right');",
        "        panel.setRightWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "    {",
        "      HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();",
        "      add(horizontalSplitPanel);",
        "      horizontalSplitPanel.setSize('150px', '100px');",
        "      {",
        "        Button button = new Button('left');",
        "        horizontalSplitPanel.setLeftWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button button = new Button('right');",
        "        horizontalSplitPanel.setRightWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_MOVE() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.setLeftWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 0.9, 0.5).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.setRightWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_ADD() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      add(panel);",
        "      panel.setSize('150px', '100px');",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 0.9, 0.5).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      add(panel);",
        "      panel.setSize('150px', '100px');",
        "      {",
        "        Button button = new Button();",
        "        panel.setRightWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE_hasEmptyLeft() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "    }",
        "  }",
        "}");
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo newButton = loadButton();
    tree.moveOn(panel).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.setLeftWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    tree.assertPrimarySelected(newButton);
  }

  public void test_tree_CREATE_hasEmptyRight() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button leftButton = new Button();",
        "        panel.setLeftWidget(leftButton);",
        "        leftButton.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo newButton = loadButton();
    tree.moveOn(panel).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button leftButton = new Button();",
        "        panel.setLeftWidget(leftButton);",
        "        leftButton.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button button = new Button();",
        "        panel.setRightWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    tree.assertPrimarySelected(newButton);
  }

  public void test_tree_CREATE_noEmpty() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button leftButton = new Button();",
        "        panel.setLeftWidget(leftButton);",
        "        leftButton.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button rightButton = new Button();",
        "        panel.setRightWidget(rightButton);",
        "        rightButton.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    loadButton();
    tree.moveOn(panel);
    tree.assertCommandNull();
  }

  public void test_tree_PASTE() throws Exception {
    openJavaInfo(
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button existing = new Button();",
        "        panel.setLeftWidget(existing);",
        "        existing.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo existingButton = getJavaInfoByName("existing");
    //
    doCopyPaste(existingButton);
    tree.moveOn(panel).click();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button existing = new Button();",
        "        panel.setLeftWidget(existing);",
        "        existing.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button button = new Button();",
        "        panel.setRightWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_MOVE() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.setLeftWidget(button_1);",
        "        button_1.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button button_2 = new Button();",
        "        panel.setRightWidget(button_2);",
        "        button_2.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    //
    tree.startDrag(button_2).dragBefore(button_1).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button button_2 = new Button();",
        "        panel.setRightWidget(button_2);",
        "        button_2.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.setLeftWidget(button_1);",
        "        button_1.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_ADD() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      add(panel);",
        "      panel.setSize('150px', '100px');",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    HorizontalSplitPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button");
    //
    tree.startDrag(button).dragOn(panel).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      HorizontalSplitPanel panel = new HorizontalSplitPanel();",
        "      add(panel);",
        "      panel.setSize('150px', '100px');",
        "      {",
        "        Button button = new Button();",
        "        panel.setLeftWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
  }
}
