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
import com.google.gdt.eclipse.designer.model.widgets.panels.StackLayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.TabLayoutPanelInfo;
import com.google.gdt.eclipse.designer.parser.ParseFactory;

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;

/**
 * Test for {@link TabLayoutPanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class TabLayoutPanelGefTest extends GwtGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void test_tearDown() throws Exception {
    super.test_tearDown();
    ParseFactory.disposeSharedGWTState();
  }

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
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_activeWidget() throws Exception {
    TabLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'AAAAAA', false);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'BBBBBB', false);",
            "    }",
            "  }",
            "}");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    // "button_1" is active, so it should be selected on click
    assertSame(button_1, panel.getActiveWidget());
    canvas.click(panel, 100, 100);
    canvas.assertPrimarySelected(button_1);
    // select "button_2"
    panel.getWidgetHandles().get(1).show();
    canvas.deselectAll();
    canvas.click(panel, 100, 100);
    canvas.assertPrimarySelected(button_2);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_canvas_empty() throws Exception {
    TabLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "  }",
            "}");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, 10, 10).click();
    assertEditor(
        "public class Test extends TabLayoutPanel {",
        "  public Test() {",
        "    super(1.5, Unit.EM);",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New Widget', false);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_canvas_before() throws Exception {
    TabLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "    {",
            "      Button button = new Button();",
            "      add(button, 'AAA', false);",
            "    }",
            "  }",
            "}");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, 1, 10).click();
    assertEditor(
        "public class Test extends TabLayoutPanel {",
        "  public Test() {",
        "    super(1.5, Unit.EM);",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New Widget', false);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'AAA', false);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_canvas_after() throws Exception {
    TabLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "    {",
            "      Button button = new Button();",
            "      add(button, 'AAA', false);",
            "    }",
            "  }",
            "}");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, 0.9, 10).click();
    assertEditor(
        "public class Test extends TabLayoutPanel {",
        "  public Test() {",
        "    super(1.5, Unit.EM);",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'AAA', false);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New Widget', false);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_tree() throws Exception {
    TabLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "  }",
            "}");
    //
    loadButton();
    tree.moveOn(panel).click();
    assertEditor(
        "public class Test extends TabLayoutPanel {",
        "  public Test() {",
        "    super(1.5, Unit.EM);",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New Widget', false);",
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
    TabLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "    {",
            "      Button myButton = new Button();",
            "      add(myButton, 'header', false);",
            "    }",
            "  }",
            "}");
    //
    {
      WidgetInfo button = getJavaInfoByName("myButton");
      doCopyPaste(button);
    }
    canvas.moveTo(panel, 5, 10).click();
    assertEditor(
        "public class Test extends TabLayoutPanel {",
        "  public Test() {",
        "    super(1.5, Unit.EM);",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New Widget', false);",
        "    }",
        "    {",
        "      Button myButton = new Button();",
        "      add(myButton, 'header', false);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.TabLayoutPanel} {this} {/add(myButton, 'header', false)/ /add(button, 'New Widget', false)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /add(button, 'New Widget', false)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: myButton} {/new Button()/ /add(myButton, 'header', false)/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_widget() throws Exception {
    TabLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'AAAAAA', false);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'BBBBBB', false);",
            "    }",
            "  }",
            "}");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    // drag "button_2" itself
    tree.select(button_2);
    canvas.beginDrag(button_2).dragTo(panel, 5, 10).endDrag();
    assertEditor(
        "public class Test extends TabLayoutPanel {",
        "  public Test() {",
        "    super(1.5, Unit.EM);",
        "    {",
        "      Button button_2 = new Button();",
        "      add(button_2, 'BBBBBB', false);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      add(button_1, 'AAAAAA', false);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_header_before() throws Exception {
    TabLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'AAAAAA', false);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'BBBBBB', false);",
            "    }",
            "  }",
            "}");
    // drag header of "button_2"
    {
      Rectangle bounds = panel.getWidgetHandles().get(1).getBounds();
      canvas.moveTo(panel, bounds.x, bounds.y);
      canvas.beginDrag().dragTo(panel, 5, 10).endDrag();
    }
    assertEditor(
        "public class Test extends TabLayoutPanel {",
        "  public Test() {",
        "    super(1.5, Unit.EM);",
        "    {",
        "      Button button_2 = new Button();",
        "      add(button_2, 'BBBBBB', false);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      add(button_1, 'AAAAAA', false);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_header_after() throws Exception {
    TabLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'AAAAAA', false);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'BBBBBB', false);",
            "    }",
            "    {",
            "      Button button_3 = new Button();",
            "      add(button_3, 'CCCCCC', false);",
            "    }",
            "  }",
            "}");
    // drag header of "button_1"
    {
      Rectangle bounds = panel.getWidgetHandles().get(0).getBounds();
      canvas.moveTo(panel, bounds.x, bounds.y);
      canvas.beginDrag().dragTo(panel, -5, 10).endDrag();
    }
    assertEditor(
        "public class Test extends TabLayoutPanel {",
        "  public Test() {",
        "    super(1.5, Unit.EM);",
        "    {",
        "      Button button_2 = new Button();",
        "      add(button_2, 'BBBBBB', false);",
        "    }",
        "    {",
        "      Button button_3 = new Button();",
        "      add(button_3, 'CCCCCC', false);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      add(button_1, 'AAAAAA', false);",
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
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      StackLayoutPanel panel = new StackLayoutPanel(Unit.CM);",
        "      rootPanel.add(panel);",
        "      rootPanel.setWidgetLeftRight(panel, 10, Unit.PX, 10, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(panel, 10, Unit.PX, 200, Unit.PX);",
        "    }",
        "    {",
        "      Button button = new Button('button');",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 10, Unit.PX, 150, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(button, 250, Unit.PX, 50, Unit.PX);",
        "    }",
        "  }",
        "}");
    StackLayoutPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 0.5, 5).endDrag();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      StackLayoutPanel panel = new StackLayoutPanel(Unit.CM);",
        "      {",
        "        Button button = new Button('button');",
        "        panel.add(button, new HTML('New Widget'), 2.0);",
        "      }",
        "      rootPanel.add(panel);",
        "      rootPanel.setWidgetLeftRight(panel, 10, Unit.PX, 10, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(panel, 10, Unit.PX, 200, Unit.PX);",
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
    TabLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'AAAAAA', false);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'BBBBBB', false);",
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
    TabLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'AAA', false);",
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
        "public class Test extends TabLayoutPanel {",
        "  public Test() {",
        "    super(1.5, Unit.EM);",
        "    {",
        "      Button button_1 = new Button();",
        "      add(button_1, '123', false);",
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
