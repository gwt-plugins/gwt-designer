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
import com.google.gdt.eclipse.designer.model.widgets.panels.AbsolutePanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.parser.ParseFactory;

import org.eclipse.wb.draw2d.IPositionConstants;

/**
 * Test for {@link AbsolutePanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class AbsolutePanelGefTest extends GwtGefTest {
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
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean m_initialized = false;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (!m_initialized) {
      ParseFactory.disposeSharedGWTState();
      prepareBox();
      forgetCreatedResources();
      m_initialized = true;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE() throws Exception {
    AbsolutePanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends AbsolutePanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    loadCreationBox();
    canvas.sideMode().create(100, 50);
    canvas.moveTo(panel, 30, 40).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    {",
        "      Box box = new Box();",
        "      add(box, 30, 40);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_PASTE() throws Exception {
    AbsolutePanelInfo panel =
        openJavaInfo(
            "public class Test extends AbsolutePanel {",
            "  public Test() {",
            "    {",
            "      Box existing = new Box();",
            "      add(existing, 5, 5);",
            "    }",
            "  }",
            "}");
    // do copy/paste
    {
      WidgetInfo existing = getJavaInfoByName("existing");
      doCopyPaste(existing);
    }
    canvas.sideMode().create(100, 50);
    canvas.moveTo(panel, 200, 100).click();
    assertEditor(
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    {",
        "      Box existing = new Box();",
        "      add(existing, 5, 5);",
        "    }",
        "    {",
        "      Box box = new Box();",
        "      add(box, 200, 100);",
        "      box.setSize('100px', '50px');",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_MOVE() throws Exception {
    AbsolutePanelInfo panel =
        openJavaInfo(
            "public class Test extends AbsolutePanel {",
            "  public Test() {",
            "    Box box = new Box();",
            "    add(box, 5, 5);",
            "  }",
            "}");
    WidgetInfo box = getJavaInfoByName("box");
    //
    canvas.sideMode().beginMove(box);
    canvas.dragTo(panel, 30, 40).endDrag();
    assertEditor(
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    Box box = new Box();",
        "    add(box, 30, 40);",
        "  }",
        "}");
  }

  public void test_canvas_ADD() throws Exception {
    openJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      AbsolutePanel panel = new AbsolutePanel();",
        "      add(panel);",
        "      panel.setPixelSize(200, 150);",
        "    }",
        "    {",
        "      Box box = new Box();",
        "      add(box);",
        "    }",
        "  }",
        "}");
    ComplexPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo box = getJavaInfoByName("box");
    //
    canvas.sideMode().beginMove(box);
    canvas.dragTo(panel, 20, 30).endDrag();
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      AbsolutePanel panel = new AbsolutePanel();",
        "      add(panel);",
        "      panel.setPixelSize(200, 150);",
        "      {",
        "        Box box = new Box();",
        "        panel.add(box, 20, 30);",
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
  public void test_resize_WEST() throws Exception {
    openJavaInfo(
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    Box box = new Box();",
        "    add(box, 150, 100);",
        "    box.setSize('100px', '50px');",
        "  }",
        "}");
    WidgetInfo box = getJavaInfoByName("box");
    //
    canvas.beginResize(box, IPositionConstants.WEST);
    canvas.dragOn(-25, 0).endDrag();
    assertEditor(
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    Box box = new Box();",
        "    add(box, 125, 100);",
        "    box.setSize('125px', '50px');",
        "  }",
        "}");
  }

  public void test_resize_EAST() throws Exception {
    openJavaInfo(
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    Box box = new Box();",
        "    add(box, 150, 100);",
        "    box.setSize('100px', '50px');",
        "  }",
        "}");
    WidgetInfo box = getJavaInfoByName("box");
    //
    canvas.beginResize(box, IPositionConstants.EAST);
    canvas.dragOn(25, 0).endDrag();
    assertEditor(
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    Box box = new Box();",
        "    add(box, 150, 100);",
        "    box.setSize('125px', '50px');",
        "  }",
        "}");
  }

  public void test_resize_NORTH() throws Exception {
    openJavaInfo(
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    Box box = new Box();",
        "    add(box, 150, 100);",
        "    box.setSize('100px', '50px');",
        "  }",
        "}");
    WidgetInfo box = getJavaInfoByName("box");
    //
    canvas.beginResize(box, IPositionConstants.NORTH);
    canvas.dragOn(0, 25).endDrag();
    assertEditor(
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    Box box = new Box();",
        "    add(box, 150, 125);",
        "    box.setSize('100px', '25px');",
        "  }",
        "}");
  }

  public void test_resize_SOUTH() throws Exception {
    openJavaInfo(
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    Box box = new Box();",
        "    add(box, 150, 100);",
        "    box.setSize('100px', '50px');",
        "  }",
        "}");
    WidgetInfo box = getJavaInfoByName("box");
    //
    canvas.beginResize(box, IPositionConstants.SOUTH);
    canvas.dragOn(0, 25).endDrag();
    assertEditor(
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    Box box = new Box();",
        "    add(box, 150, 100);",
        "    box.setSize('100px', '75px');",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE() throws Exception {
    AbsolutePanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends AbsolutePanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    loadCreationBox();
    tree.moveOn(panel).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    {",
        "      Box box = new Box();",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_PASTE() throws Exception {
    AbsolutePanelInfo panel =
        openJavaInfo(
            "public class Test extends AbsolutePanel {",
            "  public Test() {",
            "    {",
            "      Box existing = new Box();",
            "      add(existing, 5, 5);",
            "    }",
            "  }",
            "}");
    // do copy/paste
    {
      WidgetInfo existing = getJavaInfoByName("existing");
      doCopyPaste(existing);
    }
    tree.moveOn(panel).click();
    assertEditor(
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    {",
        "      Box existing = new Box();",
        "      add(existing, 5, 5);",
        "    }",
        "    {",
        "      Box box = new Box();",
        "      add(box);",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_MOVE() throws Exception {
    openJavaInfo(
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    {",
        "      Box box_1 = new Box();",
        "      add(box_1, 5, 5);",
        "    }",
        "    {",
        "      Box box_2 = new Box();",
        "      add(box_2, 5, 100);",
        "    }",
        "  }",
        "}");
    WidgetInfo box_1 = getJavaInfoByName("box_1");
    WidgetInfo box_2 = getJavaInfoByName("box_2");
    //
    tree.startDrag(box_2).dragBefore(box_1).endDrag();
    assertEditor(
        "public class Test extends AbsolutePanel {",
        "  public Test() {",
        "    {",
        "      Box box_2 = new Box();",
        "      add(box_2, 5, 100);",
        "    }",
        "    {",
        "      Box box_1 = new Box();",
        "      add(box_1, 5, 5);",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_ADD() throws Exception {
    openJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      AbsolutePanel panel = new AbsolutePanel();",
        "      add(panel);",
        "    }",
        "    {",
        "      Box box = new Box();",
        "      add(box);",
        "    }",
        "  }",
        "}");
    ComplexPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo box = getJavaInfoByName("box");
    //
    tree.startDrag(box).dragOn(panel).endDrag();
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      AbsolutePanel panel = new AbsolutePanel();",
        "      add(panel);",
        "      {",
        "        Box box = new Box();",
        "        panel.add(box);",
        "      }",
        "    }",
        "  }",
        "}");
  }

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
}
