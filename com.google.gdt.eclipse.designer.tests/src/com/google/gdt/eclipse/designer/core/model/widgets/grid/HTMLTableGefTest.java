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
package com.google.gdt.eclipse.designer.core.model.widgets.grid;

import com.google.gdt.eclipse.designer.core.model.widgets.generic.GwtGefTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.GridInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;

/**
 * Test for {@link HTMLTableInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class HTMLTableGefTest extends GwtGefTest {
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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We declare "flow container" for <code>HTMLTable</code>, but it is mostly for re-order and for
   * programmatic use. When user drops new widgets in tree this causes setting more than one widget
   * in cell 0x0.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47257
   */
  public void test_tree_noCREATE() throws Exception {
    HTMLTableInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends Grid {",
            "  public Test() {",
            "  }",
            "}");
    // try to drop new Button
    loadButton();
    tree.moveOn(panel);
    tree.assertCommandNull();
  }

  /**
   * We declare "flow container" for <code>HTMLTable</code>, but it is mostly for re-order and for
   * programmatic use. When user re-parents widget in tree this causes incorrect model state.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47820
   */
  public void test_tree_noADD() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Grid grid = new Grid(2, 1);",
        "      add(grid);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    GridInfo grid = getJavaInfoByName("grid");
    WidgetInfo button = getJavaInfoByName("button");
    // try to move "button" on "grid"
    tree.startDrag(button).dragOn(grid);
    tree.assertFeedback_notOn(grid);
  }

  /**
   * <code>HTMLTable</code> should support reordering its widgets.
   */
  public void test_tree_MOVE() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends Grid {",
        "  public Test() {",
        "    super(2, 1);",
        "    {",
        "      Button button_1 = new Button('A');",
        "      setWidget(0, 0, button_1);",
        "    }",
        "    {",
        "      Button button_2 = new Button('B');",
        "      setWidget(1, 0, button_2);",
        "    }",
        "  }",
        "}");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    // move "button_2" before "button_1"
    tree.startDrag(button_2).dragBefore(button_1);
    tree.assertCommandNotNull();
    tree.endDrag();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends Grid {",
        "  public Test() {",
        "    super(2, 1);",
        "    {",
        "      Button button_2 = new Button('B');",
        "      setWidget(1, 0, button_2);",
        "    }",
        "    {",
        "      Button button_1 = new Button('A');",
        "      setWidget(0, 0, button_1);",
        "    }",
        "  }",
        "}");
  }

  /**
   * If <code>HTMLTable</code> was not created and replaced with placeholder, we should not call its
   * methods and fail because of this.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43997
   */
  public void test_ifExceptionInCreation() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "public class MyPanel extends Grid {",
            "  public MyPanel() {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      MyPanel panel = new MyPanel();",
        "      rootPanel.add(panel);",
        "    }",
        "  }",
        "}");
    // select, no exceptions
    HTMLTableInfo panel = getJavaInfoByName("panel");
    canvas.select(panel);
  }
}
