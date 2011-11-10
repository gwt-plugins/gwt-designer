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

import com.google.gdt.eclipse.designer.gxt.model.GxtGefTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.menu.MenuBarInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.menu.MenuBarItemInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.menu.MenuInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.menu.MenuItemInfo;

import org.eclipse.wb.core.model.JavaInfo;

/**
 * Test for {@link MenuInfo} and related in GEF.
 * 
 * @author scheglov_ke
 */
public class MenuGefTest extends GxtGefTest {
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
   * <code>MenuBarItem</code> can not be dropped on anything except <code>MenuBar</code>.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?46201
   */
  public void test_MenuBarItem_CREATE() throws Exception {
    ContainerInfo container =
        openLayoutContainer(
            "// filler filler filler filler filler",
            "public class Test extends HorizontalPanel {",
            "  public Test() {",
            "  }",
            "}");
    // load MenuBarItem
    loadCreationTool("com.extjs.gxt.ui.client.widget.menu.MenuBarItem");
    // can not drop on "canvas"
    {
      canvas.moveTo(container, 10, 10);
      canvas.assertCommandNull();
    }
    // can not drop on "tree"
    {
      tree.moveOn(container);
      tree.assertCommandNull();
    }
  }

  /**
   * Test for dropping new <code>MenuBarItem</code> on <code>MenuBar</code>.
   */
  public void test_MenuBar_CREATE_item() throws Exception {
    ContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      MenuBar bar = new MenuBar();",
            "      add(bar);",
            "    }",
            "  }",
            "}");
    MenuBarInfo bar = (MenuBarInfo) container.getWidgets().get(0);
    // drop MenuBarItem
    loadCreationTool("com.extjs.gxt.ui.client.widget.menu.MenuBarItem");
    canvas.moveTo(bar, 10, 10);
    canvas.click();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      MenuBar bar = new MenuBar();",
        "      {",
        "        MenuBarItem menuBarItem = new MenuBarItem('New MenuBarItem', new Menu());",
        "        bar.add(menuBarItem);",
        "      }",
        "      add(bar);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for dropping new <code>MenuItem</code> on <code>Menu</code>.
   */
  public void test_Menu_CREATE_item_onCanvas() throws Exception {
    ContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      MenuBar bar = new MenuBar();",
            "      {",
            "        MenuBarItem menuBarItem = new MenuBarItem('MenuBarItem', new Menu());",
            "        bar.add(menuBarItem);",
            "      }",
            "      add(bar);",
            "    }",
            "  }",
            "}");
    MenuBarInfo bar = (MenuBarInfo) container.getWidgets().get(0);
    MenuBarItemInfo barItem = bar.getItems().get(0);
    MenuInfo menu = barItem.getSubMenu();
    // drop MenuItem
    JavaInfo newItem = loadCreationTool("com.extjs.gxt.ui.client.widget.menu.MenuItem");
    // "menu" not open
    canvas.assertNullEditPart(menu);
    // open "menu"
    canvas.target(barItem).in(0.5, 0.5).move();
    canvas.assertNotNullEditPart(menu);
    // drop into "menu"
    canvas.moveTo(menu, 10, 10);
    canvas.click();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      MenuBar bar = new MenuBar();",
        "      {",
        "        Menu menu = new Menu();",
        "        {",
        "          MenuItem menuItem = new MenuItem('New MenuItem');",
        "          menu.add(menuItem);",
        "        }",
        "        MenuBarItem menuBarItem = new MenuBarItem('MenuBarItem', menu);",
        "        bar.add(menuBarItem);",
        "      }",
        "      add(bar);",
        "    }",
        "  }",
        "}");
    // "newItem" should be selected and "menu" still open
    canvas.assertNotNullEditPart(menu);
    canvas.assertNotNullEditPart(newItem);
    canvas.assertPrimarySelected(newItem);
  }

  /**
   * Test for dropping new <code>MenuItem</code> on <code>Menu</code>.
   */
  public void test_Menu_CREATE_item_inTree() throws Exception {
    ContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      MenuBar bar = new MenuBar();",
            "      {",
            "        MenuBarItem menuBarItem = new MenuBarItem('MenuBarItem', new Menu());",
            "        bar.add(menuBarItem);",
            "      }",
            "      add(bar);",
            "    }",
            "  }",
            "}");
    MenuBarInfo bar = (MenuBarInfo) container.getWidgets().get(0);
    MenuBarItemInfo barItem = bar.getItems().get(0);
    MenuInfo menu = barItem.getSubMenu();
    // drop MenuItem
    JavaInfo newItem = loadCreationTool("com.extjs.gxt.ui.client.widget.menu.MenuItem");
    m_viewerTree.expandAll();
    tree.moveOn(menu);
    tree.assertFeedback_on(menu);
    tree.click();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      MenuBar bar = new MenuBar();",
        "      {",
        "        Menu menu = new Menu();",
        "        {",
        "          MenuItem menuItem = new MenuItem('New MenuItem');",
        "          menu.add(menuItem);",
        "        }",
        "        MenuBarItem menuBarItem = new MenuBarItem('MenuBarItem', menu);",
        "        bar.add(menuBarItem);",
        "      }",
        "      add(bar);",
        "    }",
        "  }",
        "}");
    // "newItem" should be selected and "menu" still open
    canvas.assertNotNullEditPart(menu);
    canvas.assertNotNullEditPart(newItem);
    canvas.assertPrimarySelected(newItem);
  }

  /**
   * We should be able to move <code>MenuBar</code> on container.
   */
  public void test_MOVEonAbsoluteLayout_MenuBar() throws Exception {
    ContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      MenuBar bar = new MenuBar();",
            "      add(bar);",
            "    }",
            "  }",
            "}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    // 
    canvas.beginDrag(bar).dragTo(container, 100, 50).endDrag();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AbsoluteLayout());",
        "    {",
        "      MenuBar bar = new MenuBar();",
        "      add(bar, new AbsoluteData(100, 50));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Inner <code>Menu</code> is <code>Component</code>, however we should not move it.
   */
  public void test_MOVEonAbsoluteLayout_innerMenu() throws Exception {
    ContainerInfo container =
        openLayoutContainer(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      MenuBar bar = new MenuBar();",
            "      {",
            "        MenuBarItem item = new MenuBarItem('MenuBarItem', new Menu());",
            "        bar.add(item);",
            "      }",
            "      add(bar);",
            "    }",
            "  }",
            "}");
    String source = m_lastEditor.getSource();
    MenuBarItemInfo item = getJavaInfoByName("item");
    MenuInfo menu = item.getSubMenu();
    // 
    canvas.click(item);
    canvas.beginDrag(menu).dragTo(container, 100, 150).endDrag();
    // no changes
    assertEditor(source, m_lastEditor);
  }

  public void test_dropMenu_asContextMenu() throws Exception {
    ContainerInfo container =
        openLayoutContainer(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    //
    loadCreationTool("com.extjs.gxt.ui.client.widget.menu.Menu");
    canvas.create().moveTo(container, 100, 100).click();
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      Menu menu = new Menu();",
        "      setContextMenu(menu);",
        "    }",
        "  }",
        "}");
  }

  public void test_dontMoveContextMenu() throws Exception {
    ContainerInfo container =
        openLayoutContainer(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AbsoluteLayout());",
            "    {",
            "      Menu menu = new Menu();",
            "      setContextMenu(menu);",
            "    }",
            "  }",
            "}");
    MenuInfo menu = getJavaInfoByName("menu");
    //
    String source = m_lastEditor.getSource();
    canvas.beginDrag(menu).dragTo(container, 100, 100).endDrag();
    assertEditor(source, m_lastEditor);
  }

  /**
   * Test for dropping sub-<code>Menu</code> on <code>MenuItem</code>.
   */
  public void test_MenuItem_CREATE_dropSubMenu() throws Exception {
    openLayoutContainer(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      MenuBar bar = new MenuBar();",
        "      {",
        "        Menu menu = new Menu();",
        "        {",
        "          MenuItem menuItem = new MenuItem('New MenuItem');",
        "          menu.add(menuItem);",
        "        }",
        "        MenuBarItem menuBarItem = new MenuBarItem('MenuBarItem', menu);",
        "        bar.add(menuBarItem);",
        "      }",
        "      add(bar);",
        "    }",
        "  }",
        "}");
    MenuItemInfo menuItem = getJavaInfoByName("menuItem");
    //
    tree.select(menuItem);
    loadCreationTool("com.extjs.gxt.ui.client.widget.menu.Menu");
    canvas.moveTo(menuItem, 20, 10).click();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      MenuBar bar = new MenuBar();",
        "      {",
        "        Menu menu = new Menu();",
        "        {",
        "          MenuItem menuItem = new MenuItem('New MenuItem');",
        "          menu.add(menuItem);",
        "          {",
        "            Menu menu_1 = new Menu();",
        "            menuItem.setSubMenu(menu_1);",
        "          }",
        "        }",
        "        MenuBarItem menuBarItem = new MenuBarItem('MenuBarItem', menu);",
        "        bar.add(menuBarItem);",
        "      }",
        "      add(bar);",
        "    }",
        "  }",
        "}");
  }
}