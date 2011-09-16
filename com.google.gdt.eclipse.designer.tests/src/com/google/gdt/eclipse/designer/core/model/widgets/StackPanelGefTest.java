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
import com.google.gdt.eclipse.designer.model.widgets.panels.StackPanelInfo;

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;

/**
 * Test for {@link StackPanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class StackPanelGefTest extends GwtGefTest {
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
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_canvas_empty() throws Exception {
    StackPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends StackPanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, 0.5, 5).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends StackPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New widget', false);",
        "      button.setSize('100%', '100%');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_canvas_before() throws Exception {
    StackPanelInfo panel =
        openJavaInfo(
            "public class Test extends StackPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      add(button, 'A', false);",
            "      button.setSize('100%', '100%');",
            "    }",
            "  }",
            "}");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, 0.5, 5).click();
    assertEditor(
        "public class Test extends StackPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New widget', false);",
        "      button.setSize('100%', '100%');",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'A', false);",
        "      button.setSize('100%', '100%');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_canvas_after() throws Exception {
    StackPanelInfo panel =
        openJavaInfo(
            "public class Test extends StackPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      add(button, 'A', false);",
            "      button.setSize('100%', '100%');",
            "    }",
            "  }",
            "}");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, 0.5, 50).click();
    assertEditor(
        "public class Test extends StackPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'A', false);",
        "      button.setSize('100%', '100%');",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New widget', false);",
        "      button.setSize('100%', '100%');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_tree() throws Exception {
    StackPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends StackPanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    loadButton();
    tree.moveOn(panel).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends StackPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New widget', false);",
        "      button.setSize('100%', '100%');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_PASTE() throws Exception {
    StackPanelInfo panel =
        openJavaInfo(
            "public class Test extends StackPanel {",
            "  public Test() {",
            "    {",
            "      Button existing = new Button();",
            "      add(existing, 'A', false);",
            "      existing.setSize('100%', '100%');",
            "    }",
            "  }",
            "}");
    WidgetInfo existing = getJavaInfoByName("existing");
    // do copy/paste
    doCopyPaste(existing);
    canvas.moveTo(panel, 0.5, 5).click();
    assertEditor(
        "public class Test extends StackPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New widget', false);",
        "      button.setSize('100%', '100%');",
        "    }",
        "    {",
        "      Button existing = new Button();",
        "      add(existing, 'A', false);",
        "      existing.setSize('100%', '100%');",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.StackPanel} {this} {/add(existing, 'A', false)/ /add(button, 'New widget', false)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /add(button, 'New widget', false)/ /button.setSize('100%', '100%')/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: existing} {/new Button()/ /add(existing, 'A', false)/ /existing.setSize('100%', '100%')/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_widget() throws Exception {
    StackPanelInfo panel =
        openJavaInfo(
            "public class Test extends StackPanel {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'A', false);",
            "      button_1.setSize('100%', '100%');",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'B', false);",
            "      button_2.setSize('100%', '100%');",
            "    }",
            "  }",
            "}");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    // drag "button_2" itself
    tree.select(button_2);
    canvas.beginDrag(button_2).dragTo(panel, 0.5, 5).endDrag();
    assertEditor(
        "public class Test extends StackPanel {",
        "  public Test() {",
        "    {",
        "      Button button_2 = new Button();",
        "      add(button_2, 'B', false);",
        "      button_2.setSize('100%', '100%');",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      add(button_1, 'A', false);",
        "      button_1.setSize('100%', '100%');",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_header() throws Exception {
    StackPanelInfo panel =
        openJavaInfo(
            "public class Test extends StackPanel {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'A', false);",
            "      button_1.setSize('100%', '100%');",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'B', false);",
            "      button_2.setSize('100%', '100%');",
            "    }",
            "  }",
            "}");
    // drag header of "button_2"
    {
      Rectangle bounds = panel.getWidgetHandles().get(1).getBounds();
      Point handleCenter = bounds.getCenter();
      canvas.moveTo(panel, handleCenter.x, handleCenter.y);
      canvas.beginDrag().dragTo(panel, 5, 0).endDrag();
    }
    assertEditor(
        "public class Test extends StackPanel {",
        "  public Test() {",
        "    {",
        "      Button button_2 = new Button();",
        "      add(button_2, 'B', false);",
        "      button_2.setSize('100%', '100%');",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      add(button_1, 'A', false);",
        "      button_1.setSize('100%', '100%');",
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
    openJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      StackPanel panel = new StackPanel();",
        "      add(panel);",
        "    }",
        "    {",
        "      Button button = new Button('button');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    StackPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 0.5, 5).endDrag();
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      StackPanel panel = new StackPanel();",
        "      add(panel);",
        "      {",
        "        Button button = new Button('button');",
        "        panel.add(button, 'New widget', false);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_doubleClickHandle() throws Exception {
    StackPanelInfo panel =
        openJavaInfo(
            "public class Test extends StackPanel {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'A', false);",
            "      button_1.setSize('100%', '100%');",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'B', false);",
            "      button_2.setSize('100%', '100%');",
            "    }",
            "  }",
            "}");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    // initially "button_1" is visible
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // double click handle of "button_1"
    {
      Rectangle bounds = panel.getWidgetHandles().get(1).getBounds();
      Point handleCenter = bounds.getCenter();
      canvas.moveTo(panel, handleCenter.x, handleCenter.y).doubleClick();
    }
    // now "button_2" is visible
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  public void test_canvas_directEditHandle() throws Exception {
    StackPanelInfo panel =
        openJavaInfo(
            "public class Test extends StackPanel {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'AAA', false);",
            "      button_1.setSize('100%', '100%');",
            "    }",
            "  }",
            "}");
    // select header
    {
      Rectangle bounds = panel.getWidgetHandles().get(0).getBounds();
      Point handleCenter = bounds.getCenter();
      canvas.moveTo(panel, handleCenter.x, handleCenter.y).click();
    }
    // do direct edit
    canvas.performDirectEdit("123");
    assertEditor(
        "public class Test extends StackPanel {",
        "  public Test() {",
        "    {",
        "      Button button_1 = new Button();",
        "      add(button_1, '123', false);",
        "      button_1.setSize('100%', '100%');",
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
   * @return <code>true</code> if object of given {@link WidgetInfo} is visible.
   */
  private static boolean isVisible(WidgetInfo widget) throws Exception {
    return widget.getBounds().height != 0;
  }
}
