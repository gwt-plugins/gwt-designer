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
import com.google.gdt.eclipse.designer.gxt.model.layout.table.TableLayoutInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

/**
 * Tests for {@link TableLayoutInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class TableLayoutGefTest extends GxtGefTest {
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
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE() throws Exception {
    LayoutContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "  }",
            "}");
    // use canvas
    loadCreationTool("com.extjs.gxt.ui.client.widget.button.Button", "empty");
    canvas.moveTo(container, 10, 10).click();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_MOVE() throws Exception {
    openLayoutContainer(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    // use canvas
    canvas.beginDrag(button).target(button).outX(+10).inY(0.5).drag().endDrag();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(2));",
        "    add(new Text());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * There was bug in ADD operation.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47780
   */
  public void test_canvas_ADD() throws Exception {
    openLayoutContainer(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      LayoutContainer container = new LayoutContainer();",
        "      container.setHeight(100);",
        "      container.setLayout(new TableLayout(1));",
        "      add(container);",
        "    }",
        "    {",
        "      Button button = new Button('Button');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    LayoutContainerInfo container = getJavaInfoByName("container");
    WidgetInfo button = getJavaInfoByName("button");
    // use canvas
    canvas.beginDrag(button).dragTo(container, 10, 10).endDrag();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      LayoutContainer container = new LayoutContainer();",
        "      container.setHeight(100);",
        "      container.setLayout(new TableLayout(1));",
        "      {",
        "        Button button = new Button('Button');",
        "        container.add(button);",
        "      }",
        "      add(container);",
        "    }",
        "  }",
        "}");
  }
}
