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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.gdt.eclipse.designer.gxt.model.GxtGefTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

/**
 * Tests for {@link BorderLayoutInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class BorderLayoutGefTest extends GxtGefTest {
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
  // Canvas CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * <code>BorderLayout</code> required <code>BoxComponent</code> children.
   */
  public void test_canvas_CREATE_notBoxComponent() throws Exception {
    LayoutContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "  }",
            "}");
    // can not drop standard Button
    loadButton();
    canvas.moveTo(container, 10, 0.5);
    canvas.assertCommandNull();
  }

  public void test_canvas_CREATE_NORTH() throws Exception {
    test_canvas_CREATE(0.5, 10, "NORTH");
  }

  public void test_canvas_CREATE_WEST() throws Exception {
    test_canvas_CREATE(10, 0.5, "WEST");
  }

  public void test_canvas_CREATE_SOUTH() throws Exception {
    test_canvas_CREATE(0.5, -10, "SOUTH");
  }

  public void test_canvas_CREATE_EAST() throws Exception {
    test_canvas_CREATE(-10, 0.5, "EAST");
  }

  public void test_canvas_CREATE_CENTER() throws Exception {
    test_canvas_CREATE(0.5, 0.5, "CENTER");
  }

  private void test_canvas_CREATE(double x, double y, String region) throws Exception {
    LayoutContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "  }",
            "}");
    loadGxtButton();
    // use canvas
    canvas.moveTo(container, x, y).click();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new BorderLayoutData(LayoutRegion." + region + "));",
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
    LayoutContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new BorderLayoutData(LayoutRegion.NORTH));",
            "    }",
            "  }",
            "}");
    WidgetInfo button = getJavaInfoByName("button");
    // use canvas
    canvas.beginDrag(button).dragTo(container, 10, 0.5).endDrag();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new BorderLayoutData(LayoutRegion.WEST));",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_ADD() throws Exception {
    openLayoutContainer(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      LayoutContainer container = new LayoutContainer();",
        "      add(container);",
        "      container.setLayout(new BorderLayout());",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    LayoutContainerInfo container = getJavaInfoByName("container");
    WidgetInfo button = getJavaInfoByName("button");
    // use canvas
    canvas.beginDrag(button).dragTo(container, -10, 0.5).endDrag();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    {",
        "      LayoutContainer container = new LayoutContainer();",
        "      {",
        "        Button button = new Button();",
        "        container.add(button, new BorderLayoutData(LayoutRegion.EAST));",
        "      }",
        "      add(container);",
        "      container.setLayout(new BorderLayout());",
        "    }",
        "  }",
        "}");
  }
}
