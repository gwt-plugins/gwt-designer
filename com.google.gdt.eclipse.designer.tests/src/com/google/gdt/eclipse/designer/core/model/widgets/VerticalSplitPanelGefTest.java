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
import com.google.gdt.eclipse.designer.model.widgets.panels.VerticalSplitPanelInfo;

/**
 * Test for {@link VerticalSplitPanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class VerticalSplitPanelGefTest extends GwtGefTest {
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
  public void test_canvas_CREATE_top() throws Exception {
    check_canvas_CREATE(0.5, 0.1, "setTopWidget");
  }

  public void test_canvas_CREATE_bottom() throws Exception {
    check_canvas_CREATE(0.5, 0.9, "setBottomWidget");
  }

  private void check_canvas_CREATE(double x, double y, String method) throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "    }",
        "  }",
        "}");
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    loadButton();
    canvas.moveTo(panel, x, y).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
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
   * Only one "top" allowed.
   */
  public void test_canvas_CREATE_usedTop() throws Exception {
    check_canvas_CREATE_used(0.5, 0.1, "setTopWidget");
  }

  /**
   * Only one "bottom" allowed.
   */
  public void test_canvas_CREATE_usedBottom() throws Exception {
    check_canvas_CREATE_used(0.5, 0.9, "setBottomWidget");
  }

  private void check_canvas_CREATE_used(double x, double y, String method) throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel." + method + "(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
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
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button existing = new Button();",
        "        panel.setTopWidget(existing);",
        "        existing.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo existing = getJavaInfoByName("existing");
    //
    doCopyPaste(existing);
    canvas.moveTo(panel, 0.5, 0.9).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button existing = new Button();",
        "        panel.setTopWidget(existing);",
        "        existing.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button button = new Button();",
        "        panel.setBottomWidget(button);",
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
            "      VerticalSplitPanel panel = new VerticalSplitPanel();",
            "      add(panel);",
            "      panel.setSize('150px', '100px');",
            "      {",
            "        Button button = new Button('top');",
            "        panel.setTopWidget(button);",
            "        button.setSize('100%', '100%');",
            "      }",
            "      {",
            "        Button button = new Button('bottom');",
            "        panel.setBottomWidget(button);",
            "        button.setSize('100%', '100%');",
            "      }",
            "    }",
            "  }",
            "}");
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    // do copy/paste
    doCopyPaste(panel);
    canvas.moveTo(flowPanel, 0.9, 0.9).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      add(panel);",
        "      panel.setSize('150px', '100px');",
        "      {",
        "        Button button = new Button('top');",
        "        panel.setTopWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button button = new Button('bottom');",
        "        panel.setBottomWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "    {",
        "      VerticalSplitPanel verticalSplitPanel = new VerticalSplitPanel();",
        "      add(verticalSplitPanel);",
        "      verticalSplitPanel.setSize('150px', '100px');",
        "      {",
        "        Button button = new Button('top');",
        "        verticalSplitPanel.setTopWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button button = new Button('bottom');",
        "        verticalSplitPanel.setBottomWidget(button);",
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
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.setTopWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 0.5, 0.9).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.setBottomWidget(button);",
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
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      add(panel);",
        "      panel.setSize('150px', '100px');",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 0.5, 0.9).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      add(panel);",
        "      panel.setSize('150px', '100px');",
        "      {",
        "        Button button = new Button();",
        "        panel.setBottomWidget(button);",
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
  public void test_tree_CREATE_hasEmptyTop() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "    }",
        "  }",
        "}");
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo newButton = loadButton();
    tree.moveOn(panel).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.setTopWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    tree.assertPrimarySelected(newButton);
  }

  public void test_tree_CREATE_hasEmptyBottom() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button topButton = new Button();",
        "        panel.setTopWidget(topButton);",
        "        topButton.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo newButton = loadButton();
    tree.moveOn(panel).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button topButton = new Button();",
        "        panel.setTopWidget(topButton);",
        "        topButton.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button button = new Button();",
        "        panel.setBottomWidget(button);",
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
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button topButton = new Button();",
        "        panel.setTopWidget(topButton);",
        "        topButton.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button bottomButton = new Button();",
        "        panel.setBottomWidget(bottomButton);",
        "        bottomButton.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
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
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button existing = new Button();",
        "        panel.setTopWidget(existing);",
        "        existing.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo existingButton = getJavaInfoByName("existing");
    //
    doCopyPaste(existingButton);
    tree.moveOn(panel).click();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test() {",
        "    {",
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button existing = new Button();",
        "        panel.setTopWidget(existing);",
        "        existing.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button button = new Button();",
        "        panel.setBottomWidget(button);",
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
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.setTopWidget(button_1);",
        "        button_1.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button button_2 = new Button();",
        "        panel.setBottomWidget(button_2);",
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
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      initWidget(panel);",
        "      {",
        "        Button button_2 = new Button();",
        "        panel.setBottomWidget(button_2);",
        "        button_2.setSize('100%', '100%');",
        "      }",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.setTopWidget(button_1);",
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
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      add(panel);",
        "      panel.setSize('150px', '100px');",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button");
    //
    tree.startDrag(button).dragOn(panel).endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      add(panel);",
        "      panel.setSize('150px', '100px');",
        "      {",
        "        Button button = new Button();",
        "        panel.setTopWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
  }
}
