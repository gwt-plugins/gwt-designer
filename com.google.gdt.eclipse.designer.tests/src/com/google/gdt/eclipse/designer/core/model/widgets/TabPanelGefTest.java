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
import com.google.gdt.eclipse.designer.model.widgets.panels.TabPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.TabPanelInfo.WidgetHandle;

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;

/**
 * Test for {@link TabPanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class TabPanelGefTest extends GwtGefTest {
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
    TabPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends TabPanel {",
            "  public Test() {",
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
    TabPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends TabPanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, 0.5, 5).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends TabPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New tab', false);",
        "      button.setSize('5cm', '3cm');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_canvas_before() throws Exception {
    TabPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('Existing');",
            "      add(button, 'Existing', false);",
            "    }",
            "  }",
            "}");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, 5, 0).click();
    assertEditor(
        "public class Test extends TabPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New tab', false);",
        "      button.setSize('5cm', '3cm');",
        "    }",
        "    {",
        "      Button button = new Button('Existing');",
        "      add(button, 'Existing', false);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_canvas_after() throws Exception {
    TabPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('Existing');",
            "      add(button, 'Existing', false);",
            "    }",
            "  }",
            "}");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, -5, 0).click();
    assertEditor(
        "public class Test extends TabPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('Existing');",
        "      add(button, 'Existing', false);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New tab', false);",
        "      button.setSize('5cm', '3cm');",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_tree() throws Exception {
    TabPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends TabPanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    loadButton();
    tree.moveOn(panel).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends TabPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New tab', false);",
        "      button.setSize('5cm', '3cm');",
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
    TabPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      Button existing = new Button();",
            "      add(existing, 'Existing', false);",
            "    }",
            "  }",
            "}");
    WidgetInfo button = getJavaInfoByName("existing");
    //
    doCopyPaste(button);
    canvas.moveTo(panel, 5, 0).click();
    assertEditor(
        "public class Test extends TabPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New tab', false);",
        "      button.setSize('5cm', '3cm');",
        "    }",
        "    {",
        "      Button existing = new Button();",
        "      add(existing, 'Existing', false);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_widget() throws Exception {
    TabPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'A', false);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'B', false);",
            "    }",
            "  }",
            "}");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    // drag "button_2" itself
    tree.select(button_2);
    //canvas.beginDrag(button_2).dragTo(panel, 5, 0).endDrag();
    canvas.beginDrag(button_2).dragTo(panel, 5, 0);
    canvas.endDrag();
    assertEditor(
        "public class Test extends TabPanel {",
        "  public Test() {",
        "    {",
        "      Button button_2 = new Button();",
        "      add(button_2, 'B', false);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      add(button_1, 'A', false);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_header_before() throws Exception {
    TabPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'A', false);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'B', false);",
            "    }",
            "  }",
            "}");
    // drag header of "button_2"
    {
      WidgetHandle widgetHandle = panel.getWidgetHandles().get(1);
      Point handleCenter = widgetHandle.getBounds().getCenter();
      canvas.moveTo(panel, handleCenter.x, handleCenter.y);
      canvas.beginDrag().dragTo(panel, 5, 0).endDrag();
    }
    assertEditor(
        "public class Test extends TabPanel {",
        "  public Test() {",
        "    {",
        "      Button button_2 = new Button();",
        "      add(button_2, 'B', false);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      add(button_1, 'A', false);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_header_after() throws Exception {
    TabPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'A', false);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'B', false);",
            "    }",
            "    {",
            "      Button button_3 = new Button();",
            "      add(button_3, 'C', false);",
            "    }",
            "  }",
            "}");
    // drag header of "button_1"
    {
      WidgetHandle widgetHandle = panel.getWidgetHandles().get(0);
      Point handleCenter = widgetHandle.getBounds().getCenter();
      canvas.moveTo(panel, handleCenter.x, handleCenter.y);
      canvas.beginDrag().dragTo(panel, -5, 0).endDrag();
    }
    assertEditor(
        "public class Test extends TabPanel {",
        "  public Test() {",
        "    {",
        "      Button button_2 = new Button();",
        "      add(button_2, 'B', false);",
        "    }",
        "    {",
        "      Button button_3 = new Button();",
        "      add(button_3, 'C', false);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      add(button_1, 'A', false);",
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
        "      TabPanel panel = new TabPanel();",
        "      add(panel);",
        "      panel.setPixelSize(250, 150);",
        "    }",
        "    {",
        "      Button button = new Button('button');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    TabPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 0.5, 0.5).endDrag();
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      TabPanel panel = new TabPanel();",
        "      add(panel);",
        "      panel.setPixelSize(250, 150);",
        "      {",
        "        Button button = new Button('button');",
        "        panel.add(button, 'New tab', false);",
        "        button.setSize('5cm', '3cm');",
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
    TabPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'A', false);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'A', false);",
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
    TabPanelInfo panel =
        openJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      add(button, 'AAA', false);",
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
        "public class Test extends TabPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button, '123', false);",
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
