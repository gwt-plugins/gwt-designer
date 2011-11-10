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
import com.google.gdt.eclipse.designer.model.widgets.panels.DockPanelInfo;

/**
 * Test for {@link DockPanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class DockPanelGefTest extends GwtGefTest {
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
  public void test_canvas_CREATE_WEST() throws Exception {
    check_canvas_CREATE(0.1, 0.5, "WEST");
  }

  public void test_canvas_CREATE_NORTH() throws Exception {
    check_canvas_CREATE(0.5, 0.1, "NORTH");
  }

  public void test_canvas_CREATE_EAST() throws Exception {
    check_canvas_CREATE(0.9, 0.5, "EAST");
  }

  public void test_canvas_CREATE_SOUTH() throws Exception {
    check_canvas_CREATE(0.5, 0.9, "SOUTH");
  }

  public void test_canvas_CREATE_CENTER() throws Exception {
    check_canvas_CREATE(0.5, 0.5, "CENTER");
  }

  private void check_canvas_CREATE(double x, double y, String direction) throws Exception {
    DockPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends DockPanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    loadButton();
    canvas.moveTo(panel, x, y).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends DockPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, DockPanel." + direction + ");",
        "    }",
        "  }",
        "}");
  }

  /**
   * Only one CENTER allowed.
   */
  public void test_canvas_CREATE_hasCENTER() throws Exception {
    DockPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends DockPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      add(button, DockPanel.CENTER);",
            "    }",
            "  }",
            "}");
    //
    loadButton();
    canvas.moveTo(panel, 0.5, 0.5);
    canvas.assertCommandNull();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Canvas.PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_PASTE_onIt() throws Exception {
    DockPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends DockPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      add(button, DockPanel.WEST);",
            "    }",
            "  }",
            "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    doCopyPaste(button);
    canvas.moveTo(panel, 0.9, 0.5).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends DockPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, DockPanel.WEST);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button, DockPanel.EAST);",
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
            "      DockPanel panel = new DockPanel();",
            "      add(panel);",
            "      {",
            "        Button button = new Button();",
            "        panel.add(button, DockPanel.SOUTH);",
            "      }",
            "    }",
            "  }",
            "}");
    // do copy/paste
    DockPanelInfo panel = getJavaInfoByName("panel");
    doCopyPaste(panel);
    canvas.moveTo(flowPanel, 0.9, 0.1).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      DockPanel panel = new DockPanel();",
        "      add(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button, DockPanel.SOUTH);",
        "      }",
        "    }",
        "    {",
        "      DockPanel dockPanel = new DockPanel();",
        "      add(dockPanel);",
        "      {",
        "        Button button = new Button();",
        "        dockPanel.add(button, DockPanel.SOUTH);",
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
    DockPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends DockPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      add(button, DockPanel.WEST);",
            "    }",
            "  }",
            "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 0.9, 0.5).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends DockPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, DockPanel.EAST);",
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
        "      DockPanel panel = new DockPanel();",
        "      add(panel);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    DockPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 0.9, 0.5).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      DockPanel panel = new DockPanel();",
        "      add(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button, DockPanel.EAST);",
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
  public void test_tree_CREATE() throws Exception {
    DockPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends DockPanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    WidgetInfo newButton = loadButton();
    tree.moveOn(panel).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends DockPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, DockPanel.WEST);",
        "    }",
        "  }",
        "}");
    tree.assertPrimarySelected(newButton);
  }

  public void test_tree_PASTE() throws Exception {
    openJavaInfo(
        "public class Test extends DockPanel {",
        "  public Test() {",
        "    {",
        "      Button existing = new Button();",
        "      add(existing, DockPanel.EAST);",
        "    }",
        "  }",
        "}");
    WidgetInfo existingButton = getJavaInfoByName("existing");
    //
    doCopyPaste(existingButton);
    tree.moveBefore(existingButton).click();
    assertEditor(
        "public class Test extends DockPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, DockPanel.WEST);",
        "    }",
        "    {",
        "      Button existing = new Button();",
        "      add(existing, DockPanel.EAST);",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_MOVE() throws Exception {
    openJavaInfo(
        "public class Test extends DockPanel {",
        "  public Test() {",
        "    {",
        "      Button button_1 = new Button();",
        "      add(button_1, DockPanel.WEST);",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      add(button_2, DockPanel.EAST);",
        "    }",
        "  }",
        "}");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    //
    tree.startDrag(button_2).dragBefore(button_1).endDrag();
    assertEditor(
        "public class Test extends DockPanel {",
        "  public Test() {",
        "    {",
        "      Button button_2 = new Button();",
        "      add(button_2, DockPanel.EAST);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      add(button_1, DockPanel.WEST);",
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
        "      DockPanel panel = new DockPanel();",
        "      add(panel);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    DockPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button");
    //
    tree.startDrag(button).dragOn(panel).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      DockPanel panel = new DockPanel();",
        "      add(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button, DockPanel.WEST);",
        "      }",
        "    }",
        "  }",
        "}");
  }
}
