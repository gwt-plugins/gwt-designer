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
import com.google.gdt.eclipse.designer.model.widgets.CompositeInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.HTMLPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.FlexTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.GridInfo;

import org.eclipse.wb.draw2d.IPositionConstants;

/**
 * Test for {@link FlexTableInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class FlexTableGefTest extends GwtGefTest {
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
  public void test_case39835() throws Exception {
    CompositeInfo composite =
        openComposite(
            "public class Test extends Composite {",
            "  public Test() {",
            "    Grid grid = new Grid(1, 2);",
            "    initWidget(grid);",
            "    {",
            "      FlexTable flexTable = new FlexTable();",
            "      grid.setWidget(0, 1, flexTable);",
            "      {",
            "        Grid grid_1 = new Grid(1, 2);",
            "        flexTable.setWidget(0, 0, grid_1);",
            "        grid_1.setWidget(0, 0, new Label('AAAAAA'));",
            "        grid_1.setWidget(0, 1, new Label('BBBBBB'));",
            "      }",
            "    }",
            "  }",
            "}");
    GridInfo grid = (GridInfo) composite.getWidget();
    FlexTableInfo flexTable = (FlexTableInfo) grid.getChildrenWidgets().get(0);
    GridInfo grid_1 = (GridInfo) flexTable.getChildrenWidgets().get(0);
    WidgetInfo label = grid_1.getChildrenWidgets().get(0);
    // select "label"
    canvas.select(label);
    // use "Delete" action
    getDeleteAction().run();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test() {",
        "    Grid grid = new Grid(1, 2);",
        "    initWidget(grid);",
        "    {",
        "      FlexTable flexTable = new FlexTable();",
        "      grid.setWidget(0, 1, flexTable);",
        "      {",
        "        Grid grid_1 = new Grid(1, 2);",
        "        flexTable.setWidget(0, 0, grid_1);",
        "        grid_1.setWidget(0, 1, new Label('BBBBBB'));",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Selection of {@link WidgetInfo} on {@link HTMLPanelInfo} should provide normal resize handles.
   */
  public void test_hasResizeHandles() throws Exception {
    openComposite(
        "public class Test extends Composite {",
        "  public Test() {",
        "    Grid grid = new Grid(1, 1);",
        "    initWidget(grid);",
        "    {",
        "      Button button = new Button('My Button');",
        "      grid.setWidget(0, 0, button);",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    // select "button"
    canvas.select(button);
    canvas.beginResize(button, IPositionConstants.SOUTH);
    canvas.dragTo(button, 0, 100).endDrag();
    assertEditor(
        "public class Test extends Composite {",
        "  public Test() {",
        "    Grid grid = new Grid(1, 1);",
        "    initWidget(grid);",
        "    {",
        "      Button button = new Button('My Button');",
        "      grid.setWidget(0, 0, button);",
        "      button.setHeight('100px');",
        "    }",
        "  }",
        "}");
  }
}
