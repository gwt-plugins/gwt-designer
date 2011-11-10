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
package com.google.gdt.eclipse.designer.gwtext;

import com.google.gdt.eclipse.designer.gwtext.model.widgets.ColumnConfigInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.GridPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.CompositeInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.graphics.Image;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link GridPanelInfo}.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 */
public class GridPanelTest extends GwtExtModelTest {
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
  public void test_parseEmpty() throws Exception {
    GridPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends GridPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    assertFalse(panel.hasLayout());
    assertNoErrors(panel);
    // check hierarchy
    assertHierarchy("{this: com.gwtext.client.widgets.grid.GridPanel} {this} {}");
  }

  /**
   * We have <code>setStore()</code> invocation, however it was not executed (because of instance
   * method), so we need to set <code>Store</code>.
   */
  public void test_parseWithInvalid_setStore() throws Exception {
    GridPanelInfo panel =
        parseJavaInfo(
            "import com.gwtext.client.data.Store;",
            "public class Test extends GridPanel {",
            "  public Test() {",
            "    setStore(createStore());",
            "  }",
            "  private Store createStore() {",
            "    return null;",
            "  }",
            "}");
    panel.refresh();
    // check hierarchy
    assertHierarchy("{this: com.gwtext.client.widgets.grid.GridPanel} {this} {/setStore(createStore())/}");
  }

  public void test_parseOnRootPanel() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      GridPanel grid = new GridPanel();",
            "      rootPanel.add(grid, 10, 10);",
            "    }",
            "  }",
            "}");
    assertNoErrors(frame);
    // refresh()
    frame.refresh();
    assertNoErrors(frame);
    // check hierarchy
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(grid, 10, 10)/}",
        "  {new: com.gwtext.client.widgets.grid.GridPanel} {local-unique: grid} {/new GridPanel()/ /rootPanel.add(grid, 10, 10)/}");
  }

  /**
   * <code>GridPanel</code> required store and column models. We set them directly before executing
   * association (i.e. before its rendering). But if <code>GridPanel</code> is not bound to any
   * parent, we can not do this. So, dangling <code>GridPanel</code> causes problem.
   * <p>
   * We solve this problem be removing dangling <code>GridPanel</code> before searching root.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42199
   */
  public void test_parseDangling() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  private  GridPanel grid = new GridPanel();",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    assertHierarchy("{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/}");
    //
    frame.refresh();
    assertNoErrors(frame);
  }

  public void test_parseOnComposite() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "public class Test extends Composite {",
            "  public Test() {",
            "    GridPanel grid = new GridPanel();",
            "    initWidget(grid);",
            "    grid.setSize('300px', '200px');",
            "  }",
            "}");
    assertNoErrors(composite);
    // check hierarchy
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.Composite} {this} {/initWidget(grid)/}",
        "  {new: com.gwtext.client.widgets.grid.GridPanel} {local-unique: grid} {/new GridPanel()/ /initWidget(grid)/ /grid.setSize('300px', '200px')/}");
    // refresh()
    composite.refresh();
    assertNoErrors(composite);
    // check bounds
    {
      Rectangle compositeBounds = composite.getBounds();
      assertThat(compositeBounds.width).isEqualTo(300);
      assertThat(compositeBounds.height).isEqualTo(200);
    }
    {
      Rectangle gridBounds = composite.getWidget().getBounds();
      assertThat(gridBounds.width).isEqualTo(300);
      assertThat(gridBounds.height).isEqualTo(200);
    }
  }

  /**
   * We should ensure required models for <code>GridPanel</code> before executing any association,
   * including previously unsupported <code>setWidget(row,column,Widget)</code> invocation.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43281
   */
  public void test_parseOnFlexTable() throws Exception {
    CompositeInfo composite =
        parseJavaInfo(
            "import com.google.gwt.user.client.ui.FlexTable;",
            "public class Test extends Composite {",
            "  public Test() {",
            "    FlexTable flexTable = new FlexTable();",
            "    initWidget(flexTable);",
            "    {",
            "      GridPanel grid = new GridPanel();",
            "      flexTable.setWidget(0, 0, grid);",
            "    }",
            "  }",
            "}");
    composite.refresh();
    assertNoErrors(composite);
  }

  public void test_liveImage() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    //
    GridPanelInfo gridPanel = createJavaInfo("com.gwtext.client.widgets.grid.GridPanel");
    gridPanel.putArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
    {
      Image image = gridPanel.getImage();
      org.eclipse.swt.graphics.Rectangle bounds = image.getBounds();
      assertThat(bounds.width).isEqualTo(320);
      assertThat(bounds.height).isEqualTo(100);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Columns
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_parse_columns() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      GridPanel grid = new GridPanel();",
            "      ColumnConfig[] columns = new ColumnConfig[] {",
            "        new ColumnConfig('AAA', 'f1', 50), new ColumnConfig('BBB', 'f3'), };",
            "      grid.setColumnModel(new ColumnModel(columns));",
            "      rootPanel.add(grid, 100, 100);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    GridPanelInfo grid = (GridPanelInfo) frame.getChildrenJava().get(0);
    assertNoErrors(frame);
    assertHierarchy( // filler
    "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(grid, 100, 100)/}\n"
        + "	{new: com.gwtext.client.widgets.grid.GridPanel} {local-unique: grid} {/new GridPanel()/ /grid.setColumnModel(new ColumnModel(columns))/ /rootPanel.add(grid, 100, 100)/}\n"
        + "		{new: com.gwtext.client.widgets.grid.ColumnConfig} {empty} {/new ColumnConfig(\"AAA\", \"f1\", 50)/}\n"
        + "		{new: com.gwtext.client.widgets.grid.ColumnConfig} {empty} {/new ColumnConfig(\"BBB\", \"f3\")/}");
    List<ColumnConfigInfo> columns = grid.getColumns();
    assertThat(columns.size()).isEqualTo(2);
    assertThat(columns.get(0).getBounds()).isEqualTo(new Rectangle(2, 2, 50, 23));
    assertThat(columns.get(1).getBounds()).isEqualTo(new Rectangle(52, 2, 100, 23));
  }

  public void test_column_CREATE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      GridPanel grid = new GridPanel();",
            "      rootPanel.add(grid, 100, 100);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    GridPanelInfo grid = (GridPanelInfo) frame.getChildrenJava().get(0);
    // create column
    {
      ColumnConfigInfo newColumn = createJavaInfo("com.gwtext.client.widgets.grid.ColumnConfig");
      flowContainer_CREATE(grid, newColumn, null);
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      GridPanel grid = new GridPanel();",
        "      grid.setColumnModel(new ColumnModel(new ColumnConfig[] {new ColumnConfig('New Column', 'columnIndex', 150)}));",
        "      rootPanel.add(grid, 100, 100);",
        "    }",
        "  }",
        "}");
    // 48137 - bad binding for "new ColumnModel(ColumnConfig[])"
    assertNoErrors(frame);
  }

  public void test_column_MOVE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      GridPanel grid = new GridPanel();",
            "      ColumnConfig[] columns = new ColumnConfig[] {",
            "        new ColumnConfig('AAA', 'f1', 50), new ColumnConfig('BBB', 'f3'), };",
            "      grid.setColumnModel(new ColumnModel(columns));",
            "      rootPanel.add(grid, 100, 100);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    GridPanelInfo grid = (GridPanelInfo) frame.getChildrenJava().get(0);
    // create column
    List<ColumnConfigInfo> columns = grid.getColumns();
    ColumnConfigInfo columnA = columns.get(0);
    ColumnConfigInfo columnB = columns.get(1);
    flowContainer_MOVE(grid, columnB, columnA);
    assertEditor(// filler;
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      GridPanel grid = new GridPanel();",
        "      ColumnConfig[] columns = new ColumnConfig[] {",
        "        new ColumnConfig('BBB', 'f3'), new ColumnConfig('AAA', 'f1', 50), };",
        "      grid.setColumnModel(new ColumnModel(columns));",
        "      rootPanel.add(grid, 100, 100);",
        "    }",
        "  }",
        "}");
  }
}