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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.widgets.generic.GwtGefTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.DockLayoutPanelInfo;

import org.eclipse.wb.draw2d.IPositionConstants;

/**
 * Test for {@link DockLayoutPanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class DockLayoutPanelGefTest extends GwtGefTest {
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
  public void test_canvas_CREATE_WEST() throws Exception {
    DockLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "  }",
            "}");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, 10, 100).click();
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 1.0);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_CREATE_EAST() throws Exception {
    DockLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "  }",
            "}");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, -10, 100).click();
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button = new Button();",
        "      addEast(button, 1.0);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_CREATE_CENTER() throws Exception {
    DockLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "  }",
            "}");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, 0.5, 0.5).click();
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_PASTE_onIt() throws Exception {
    DockLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 1.1);",
            "    }",
            "  }",
            "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    doCopyPaste(button);
    canvas.moveTo(panel, 0.9, 0.5).click();
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 1.1);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      addEast(button, 1.0);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_MOVE() throws Exception {
    DockLayoutPanelInfo panel =
        openJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 2.0);",
            "    }",
            "  }",
            "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 0.5, -10).endDrag();
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button = new Button();",
        "      addSouth(button, 2.0);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_ADD() throws Exception {
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      DockLayoutPanel panel = new DockLayoutPanel(Unit.CM);",
        "      rootPanel.add(panel);",
        "      rootPanel.setWidgetLeftRight(panel, 10, Unit.PX, 10, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(panel, 10, Unit.PX, 200, Unit.PX);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 10, Unit.PX, 150, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(button, 250, Unit.PX, 50, Unit.PX);",
        "    }",
        "  }",
        "}");
    ComplexPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 10, 0.5).endDrag();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      DockLayoutPanel panel = new DockLayoutPanel(Unit.CM);",
        "      rootPanel.add(panel);",
        "      rootPanel.setWidgetLeftRight(panel, 10, Unit.PX, 10, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(panel, 10, Unit.PX, 200, Unit.PX);",
        "      {",
        "        Button button = new Button();",
        "        panel.addWest(button, 1.0);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // RESIZE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_resize_WEST() throws Exception {
    openJavaInfo(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.PX);",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 150.0);",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.EAST);
    canvas.dragOn(50, 0).endDrag();
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.PX);",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 200.0);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_resize_EAST() throws Exception {
    openJavaInfo(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.PX);",
        "    {",
        "      Button button = new Button();",
        "      addEast(button, 150.0);",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.WEST);
    canvas.dragOn(-50, 0).endDrag();
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.PX);",
        "    {",
        "      Button button = new Button();",
        "      addEast(button, 200.0);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_resize_NORTH() throws Exception {
    openJavaInfo(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.PX);",
        "    {",
        "      Button button = new Button();",
        "      addNorth(button, 150.0);",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.SOUTH);
    canvas.dragOn(0, 50).endDrag();
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.PX);",
        "    {",
        "      Button button = new Button();",
        "      addNorth(button, 200.0);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_resize_SOUTH() throws Exception {
    openJavaInfo(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.PX);",
        "    {",
        "      Button button = new Button();",
        "      addSouth(button, 150.0);",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.NORTH);
    canvas.dragOn(0, 50).endDrag();
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.PX);",
        "    {",
        "      Button button = new Button();",
        "      addSouth(button, 100.0);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE() throws Exception {
    DockLayoutPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "  }",
            "}");
    //
    WidgetInfo newButton = loadButton();
    tree.moveOn(panel).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 1.0);",
        "    }",
        "  }",
        "}");
    tree.assertPrimarySelected(newButton);
  }

  public void test_tree_PASTE() throws Exception {
    openJavaInfo(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button existing = new Button();",
        "      addWest(existing, 2.0);",
        "    }",
        "  }",
        "}");
    WidgetInfo existingButton = getJavaInfoByName("existing");
    //
    doCopyPaste(existingButton);
    tree.moveBefore(existingButton).click();
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 1.0);",
        "    }",
        "    {",
        "      Button existing = new Button();",
        "      addWest(existing, 2.0);",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_MOVE() throws Exception {
    openJavaInfo(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button_1 = new Button();",
        "      addWest(button_1, 1.1);",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      addEast(button_2, 1.2);",
        "    }",
        "  }",
        "}");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    //
    tree.startDrag(button_2).dragBefore(button_1).endDrag();
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button_2 = new Button();",
        "      addEast(button_2, 1.2);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      addWest(button_1, 1.1);",
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
        "      DockLayoutPanel panel = new DockLayoutPanel(Unit.CM);",
        "      add(panel);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    DockLayoutPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button");
    //
    tree.startDrag(button).dragOn(panel).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      DockLayoutPanel panel = new DockLayoutPanel(Unit.CM);",
        "      add(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.addWest(button, 1.0);",
        "      }",
        "    }",
        "  }",
        "}");
  }
}
