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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;

import org.eclipse.swt.graphics.Image;

import java.util.Arrays;

/**
 * Test for <code>TreeGrid</code>.
 * 
 * @author scheglov_ke
 */
public class TreeGridTest extends GxtModelTest {
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
   * We don't evaluate columns, so using <code>setAutoExpandColumn()</code> causes exception in GXT.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43275
   */
  public void test_ignore_setAutoExpandColumn() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "import java.util.ArrayList;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FitLayout());",
            "    {",
            "      TreeGrid treeGrid = new TreeGrid(new TreeStore(), new ColumnModel(new ArrayList<ColumnConfig>()));",
            "      treeGrid.setAutoExpandColumn('name');",
            "      add(treeGrid);",
            "    }",
            "  }",
            "}");
    container.refresh();
    assertNoErrors(container);
  }

  public void test_CREATE() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // prepare new Grid
    WidgetInfo newGrid = createJavaInfo("com.extjs.gxt.ui.client.widget.treegrid.TreeGrid");
    // check "live image"
    {
      Image liveImage = newGrid.getImage();
      assertEquals(302, liveImage.getBounds().width);
      assertEquals(202, liveImage.getBounds().height);
    }
    // do create
    panel.command_CREATE2(newGrid, null);
    assertEditor(
        "import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;",
        "import com.extjs.gxt.ui.client.store.TreeStore;",
        "import com.extjs.gxt.ui.client.widget.grid.ColumnModel;",
        "import java.util.ArrayList;",
        "import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      TreeGrid treeGrid = new TreeGrid(new TreeStore(), new ColumnModel(new ArrayList<ColumnConfig>()));",
        "      add(treeGrid);",
        "      treeGrid.setBorders(true);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Evaluating <code>null</code> {@link com.extjs.gxt.ui.client.store.TreeStore} parameter in
   * constructor.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44674
   */
  public void test_noStore() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "import java.util.ArrayList;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;",
            "public class Test extends LayoutContainer {",
            "  TreeStore m_store;",
            "  public Test() {",
            "    setLayout(new FitLayout());",
            "    {",
            "      TreeGrid treeGrid = new TreeGrid(getStore(), new ColumnModel(new ArrayList<ColumnConfig>()));",
            "      add(treeGrid);",
            "    }",
            "  }",
            "  private TreeStore getStore() {",
            "    return m_store;",
            "  }",
            "}");
    container.refresh();
    assertNoErrors(container);
  }

  /**
   * We need columns before association.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?46775
   */
  public void test_requiredColumnWith_TreeGridCellRenderer() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "import java.util.ArrayList;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FitLayout());",
            "    {",
            "      ArrayList columns = new ArrayList();",
            "      {",
            "        ColumnConfig column = new ColumnConfig();",
            "        column.setId('id');",
            "        column.setWidth(200);",
            "        columns.add(column);",
            "      }",
            "      ColumnModel cm = new ColumnModel(columns);",
            "      TreeGrid treeGrid = new TreeGrid(new TreeStore(), cm);",
            "      add(treeGrid);",
            "    }",
            "  }",
            "}");
    container.refresh();
    assertNoErrors(container);
  }

  /**
   * We should not fail when {@link Arrays#asList(Object...)} is used to set columns.
   */
  public void test_Array_asList_forColumns() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "import java.util.Arrays;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FitLayout());",
            "    {",
            "      ColumnConfig column = new ColumnConfig();",
            "      column.setId('id');",
            "      column.setWidth(200);",
            "      ",
            "      ColumnModel cm = new ColumnModel(Arrays.asList(column));",
            "      TreeGrid treeGrid = new TreeGrid(new TreeStore(), cm);",
            "      add(treeGrid);",
            "    }",
            "  }",
            "}");
    // note: no columns right now
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FitLayout())/ /add(treeGrid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FitLayout} {empty} {/setLayout(new FitLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.treegrid.TreeGrid} {local-unique: treeGrid} {/new TreeGrid(new TreeStore(), cm)/ /add(treeGrid)/}",
        "    Arrays.asList []",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {local-unique: column} {/new ColumnConfig()/ /column.setId('id')/ /column.setWidth(200)/ /Arrays.asList(column)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FitData} {virtual-layout-data} {}");
    // refresh
    container.refresh();
    assertNoErrors(container);
  }
}