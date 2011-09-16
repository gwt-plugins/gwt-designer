/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.menu.ItemInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.menu.MenuBarInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.menu.MenuBarItemInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.menu.MenuInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.menu.MenuItemInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.graphics.Image;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link MenuBarInfo}, {@link MenuBarItemInfo}, {@link MenuInfo}.
 * 
 * @author scheglov_ke
 */
public class MenuTest extends GxtModelTest {
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
  // MenuBar
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Even empty <code>MenuBar</code> should have reasonable size.
   */
  public void test_MenuBar_parseEmpty() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      MenuBar bar = new MenuBar();",
            "      add(bar);",
            "    }",
            "  }",
            "}");
    container.refresh();
    MenuBarInfo panel = (MenuBarInfo) container.getWidgets().get(0);
    // 
    Rectangle bounds = panel.getBounds();
    assertThat(bounds.width).isGreaterThan(100);
    assertThat(bounds.height).isGreaterThan(20);
  }

  public void test_MenuBar_parse() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    MenuBar bar = new MenuBar();",
            "    {",
            "      MenuBarItem menuBarItem = new MenuBarItem('New MenuBarItem', new Menu());",
            "      bar.add(menuBarItem);",
            "    }",
            "    add(bar);",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(bar)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.menu.MenuBar} {local-unique: bar} {/new MenuBar()/ /bar.add(menuBarItem)/ /add(bar)/}",
        "    {new: com.extjs.gxt.ui.client.widget.menu.MenuBarItem} {local-unique: menuBarItem} {/new MenuBarItem('New MenuBarItem', new Menu())/ /bar.add(menuBarItem)/}",
        "      {new: com.extjs.gxt.ui.client.widget.menu.Menu} {empty} {/new MenuBarItem('New MenuBarItem', new Menu())/}");
    container.refresh();
    MenuBarInfo bar = (MenuBarInfo) container.getWidgets().get(0);
    //
    assertThat(bar.getItems()).hasSize(1);
  }

  public void test_MenuBar_IMenuInfo() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    MenuBar bar = new MenuBar();",
            "    {",
            "      MenuBarItem menuBarItem = new MenuBarItem('New MenuBarItem', new Menu());",
            "      bar.add(menuBarItem);",
            "    }",
            "    add(bar);",
            "  }",
            "}");
    container.refresh();
    MenuBarInfo bar = (MenuBarInfo) container.getWidgets().get(0);
    // prepare MenuBarItem_Info
    MenuBarItemInfo item;
    {
      List<MenuBarItemInfo> items = bar.getItems();
      assertThat(items).hasSize(1);
      item = items.get(0);
    }
    // prepare Menu_Info, child of "item"
    MenuInfo subMenu = item.getSubMenu();
    assertNotNull(subMenu);
    assertTrue(subMenu.isSubMenu());
    // get unsupported adaptable
    assertNull(bar.getAdapter(String.class));
    assertNull(item.getAdapter(String.class));
    // IMenuInfo
    {
      IMenuInfo barObject = MenuObjectInfoUtils.getMenuInfo(bar);
      assertSame(bar, barObject.getModel());
      assertSame(bar, barObject.getToolkitModel());
      // presentation
      {
        // no need for image, because this MenuBar is visible on usual screen shot
        assertNull(barObject.getImage());
        // some reasonable bounds
        {
          Rectangle bounds = barObject.getBounds();
          assertThat(bounds.x).isEqualTo(0);
          assertThat(bounds.y).isEqualTo(-1);
          assertThat(bounds.width).isEqualTo(450);
          assertThat(bounds.height).isGreaterThan(20);
        }
      }
      // access
      assertTrue(barObject.isHorizontal());
      {
        List<IMenuItemInfo> itemObjects = barObject.getItems();
        assertThat(itemObjects).hasSize(1);
        {
          IMenuItemInfo itemObject = itemObjects.get(0);
          assertSame(item, itemObject.getModel());
          // presentation
          {
            // no need for image, because parent MenuBar is visible on usual screen shot
            assertNull(itemObject.getImage());
            // some reasonable bounds
            {
              Rectangle bounds = itemObject.getBounds();
              assertThat(bounds.x).isEqualTo(3);
              assertThat(bounds.y).isEqualTo(3);
              assertThat(bounds.width).isGreaterThan(100);
              assertThat(bounds.height).isGreaterThan(15);
            }
          }
          // no policy
          assertSame(IMenuPolicy.NOOP, itemObject.getPolicy());
          // sub menu
          {
            IMenuInfo menuObject = itemObject.getMenu();
            assertSame(subMenu, menuObject.getModel());
          }
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MenuBar: IMenuPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MenuBar_IMenuPolicy_CREATE() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    MenuBar bar = new MenuBar();",
            "    add(bar);",
            "  }",
            "}");
    container.refresh();
    MenuBarInfo bar = (MenuBarInfo) container.getWidgets().get(0);
    IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(bar).getPolicy();
    // don't accept anything other than MenuBarItem
    assertFalse(policy.validateCreate(new Object()));
    // create new MenuBarItem
    MenuBarItemInfo newItem = createJavaInfo("com.extjs.gxt.ui.client.widget.menu.MenuBarItem");
    assertTrue(policy.validateCreate(newItem));
    policy.commandCreate(newItem, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    MenuBar bar = new MenuBar();",
        "    {",
        "      MenuBarItem menuBarItem = new MenuBarItem('New MenuBarItem', new Menu());",
        "      bar.add(menuBarItem);",
        "    }",
        "    add(bar);",
        "  }",
        "}");
  }

  public void test_MenuBar_IMenuPolicy_PASTE_validateBad() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    MenuBar bar = new MenuBar();",
            "    add(bar);",
            "  }",
            "}");
    container.refresh();
    MenuBarInfo bar = (MenuBarInfo) container.getWidgets().get(0);
    IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(bar).getPolicy();
    // Object can not be pasted (not list of memento at all)
    assertFalse(policy.validatePaste(new Object()));
    // MenuBar can not be pasted (even if it has memento)
    {
      JavaInfoMemento memento = JavaInfoMemento.createMemento(bar);
      List<JavaInfoMemento> mementos = ImmutableList.of(memento);
      assertFalse(policy.validatePaste(mementos));
    }
  }

  public void test_MenuBar_IMenuPolicy_PASTE() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    MenuBar bar = new MenuBar();",
            "    {",
            "      MenuBarItem item = new MenuBarItem('A', new Menu());",
            "      bar.add(item);",
            "    }",
            "    add(bar);",
            "  }",
            "}");
    container.refresh();
    MenuBarInfo bar = (MenuBarInfo) container.getWidgets().get(0);
    // prepare mementos
    List<JavaInfoMemento> mementos;
    {
      MenuBarItemInfo existingItem = bar.getItems().get(0);
      JavaInfoMemento memento = JavaInfoMemento.createMemento(existingItem);
      mementos = ImmutableList.of(memento);
    }
    // paste
    {
      IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(bar).getPolicy();
      // validate
      assertTrue(policy.validatePaste(mementos));
      // do paste
      List<?> pastedObjects = policy.commandPaste(mementos, null);
      assertThat(pastedObjects).hasSize(1);
      assertThat(pastedObjects.get(0)).isInstanceOf(MenuBarItemInfo.class);
    }
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    MenuBar bar = new MenuBar();",
        "    {",
        "      MenuBarItem item = new MenuBarItem('A', new Menu());",
        "      bar.add(item);",
        "    }",
        "    {",
        "      MenuBarItem menuBarItem = new MenuBarItem('A', new Menu());",
        "      bar.add(menuBarItem);",
        "    }",
        "    add(bar);",
        "  }",
        "}");
  }

  public void test_MenuBar_IMenuPolicy_MOVE() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    MenuBar bar = new MenuBar();",
            "    {",
            "      MenuBarItem item_1 = new MenuBarItem('A', new Menu());",
            "      bar.add(item_1);",
            "    }",
            "    {",
            "      MenuBarItem item_2 = new MenuBarItem('B', new Menu());",
            "      bar.add(item_2);",
            "    }",
            "    add(bar);",
            "  }",
            "}");
    container.refresh();
    MenuBarInfo bar = (MenuBarInfo) container.getWidgets().get(0);
    IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(bar).getPolicy();
    MenuBarItemInfo item_1 = bar.getItems().get(0);
    MenuBarItemInfo item_2 = bar.getItems().get(1);
    // invalid move
    assertFalse(policy.validateMove(new Object()));
    // move "item_2" before "item_1"
    {
      // validate
      assertTrue(policy.validateMove(item_2));
      // do move
      policy.commandMove(item_2, item_1);
    }
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    MenuBar bar = new MenuBar();",
        "    {",
        "      MenuBarItem item_2 = new MenuBarItem('B', new Menu());",
        "      bar.add(item_2);",
        "    }",
        "    {",
        "      MenuBarItem item_1 = new MenuBarItem('A', new Menu());",
        "      bar.add(item_1);",
        "    }",
        "    add(bar);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ColorMenu() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    ColorMenu menu = new ColorMenu();",
            "    add(menu);",
            "  }",
            "}");
    container.refresh();
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(menu)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.menu.ColorMenu} {local-unique: menu} {/new ColorMenu()/ /add(menu)/}");
    // ColorMenu shows "shadow" in wrong position, so we disable this
    MenuInfo menu = (MenuInfo) container.getWidgets().get(0);
    assertEquals(false, ReflectionUtils.invokeMethod(menu.getObject(), "getShadow()"));
  }

  public void test_DateMenu() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    DateMenu menu = new DateMenu();",
            "    add(menu);",
            "  }",
            "}");
    container.refresh();
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(menu)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.menu.DateMenu} {local-unique: menu} {/new DateMenu()/ /add(menu)/}");
    // DateMenu shows "shadow" in wrong position, so we disable this
    MenuInfo menu = (MenuInfo) container.getWidgets().get(0);
    assertEquals(false, ReflectionUtils.invokeMethod(menu.getObject(), "getShadow()"));
  }

  public void test_Menu_IMenuInfo() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    MenuBar bar = new MenuBar();",
            "    {",
            "      Menu menu = new Menu();",
            "      {",
            "        MenuItem item = new MenuItem('A');",
            "        menu.add(item);",
            "      }",
            "      MenuBarItem menuBarItem = new MenuBarItem('MenuBarItem', menu);",
            "      bar.add(menuBarItem);",
            "    }",
            "    add(bar);",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(bar)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.menu.MenuBar} {local-unique: bar} {/new MenuBar()/ /bar.add(menuBarItem)/ /add(bar)/}",
        "    {new: com.extjs.gxt.ui.client.widget.menu.MenuBarItem} {local-unique: menuBarItem} {/new MenuBarItem('MenuBarItem', menu)/ /bar.add(menuBarItem)/}",
        "      {new: com.extjs.gxt.ui.client.widget.menu.Menu} {local-unique: menu} {/new Menu()/ /menu.add(item)/ /new MenuBarItem('MenuBarItem', menu)/}",
        "        {new: com.extjs.gxt.ui.client.widget.menu.MenuItem} {local-unique: item} {/new MenuItem('A')/ /menu.add(item)/}");
    container.refresh();
    MenuBarInfo bar = (MenuBarInfo) container.getWidgets().get(0);
    MenuInfo menu = bar.getItems().get(0).getSubMenu();
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menu);
    ComponentInfo item = menu.getItems().get(0);
    // get unsupported adaptable
    assertNull(menu.getAdapter(String.class));
    assertSame(menu, menuObject.getModel());
    // presentation
    {
      // we should prepare image for Menu
      {
        Image image = menuObject.getImage();
        assertNotNull(image);
        org.eclipse.swt.graphics.Rectangle bounds = image.getBounds();
        assertThat(bounds.width).isEqualTo(120);
        assertThat(bounds.height).isEqualTo(30);
      }
      // some reasonable bounds
      {
        Rectangle bounds = menuObject.getBounds();
        assertThat(bounds.width).isEqualTo(120);
        assertThat(bounds.height).isEqualTo(30);
      }
    }
    // access
    assertFalse(menuObject.isHorizontal());
    // IMenuItem-s
    {
      List<IMenuItemInfo> items = menuObject.getItems();
      assertThat(items).hasSize(1);
      assertSame(item, items.get(0).getModel());
    }
  }

  public void test_Menu_IMenuPopupInfo() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    Button button = new Button();",
            "    add(button);",
            "    {",
            "      Menu menu = new Menu();",
            "      button.setMenu(menu);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(button)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/ /button.setMenu(menu)/}",
        "    {new: com.extjs.gxt.ui.client.widget.menu.Menu} {local-unique: menu} {/new Menu()/ /button.setMenu(menu)/}");
    container.refresh();
    ComponentInfo button = (ComponentInfo) container.getWidgets().get(0);
    MenuInfo menu = button.getChildren(MenuInfo.class).get(0);
    assertFalse(menu.isSubMenu());
    // IMenuPopupInfo
    IMenuPopupInfo popupObject = MenuObjectInfoUtils.getMenuPopupInfo(menu);
    assertSame(menu, popupObject.getModel());
    // presentation
    {
      // use icon as "popup" image
      {
        Image image = popupObject.getImage();
        assertSame(menu.getDescription().getIcon(), image);
      }
      // some reasonable bounds
      {
        Rectangle bounds = popupObject.getBounds();
        assertThat(bounds.width).isEqualTo(16);
        assertThat(bounds.height).isEqualTo(16);
      }
    }
    // access
    {
      IMenuInfo menuObject = popupObject.getMenu();
      assertSame(MenuObjectInfoUtils.getMenuInfo(menu), menuObject);
      assertSame(menuObject, menuObject.getModel());
    }
    assertNotNull(popupObject.getPolicy());
  }

  public void test_Menu_IMenuInfo_genericComponentAsItem() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    MenuBar bar = new MenuBar();",
            "    {",
            "      Menu menu = new Menu();",
            "      {",
            "        Button button = new Button();",
            "        menu.add(button);",
            "      }",
            "      MenuBarItem menuBarItem = new MenuBarItem('MenuBarItem', menu);",
            "      bar.add(menuBarItem);",
            "    }",
            "    add(bar);",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(bar)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.menu.MenuBar} {local-unique: bar} {/new MenuBar()/ /bar.add(menuBarItem)/ /add(bar)/}",
        "    {new: com.extjs.gxt.ui.client.widget.menu.MenuBarItem} {local-unique: menuBarItem} {/new MenuBarItem('MenuBarItem', menu)/ /bar.add(menuBarItem)/}",
        "      {new: com.extjs.gxt.ui.client.widget.menu.Menu} {local-unique: menu} {/new Menu()/ /menu.add(button)/ /new MenuBarItem('MenuBarItem', menu)/}",
        "        {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /menu.add(button)/}");
    container.refresh();
    MenuBarInfo bar = (MenuBarInfo) container.getWidgets().get(0);
    MenuInfo menu = bar.getItems().get(0).getSubMenu();
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menu);
    ComponentInfo button = menu.getItems().get(0);
    // IMenuItem-s
    {
      List<IMenuItemInfo> items = menuObject.getItems();
      assertThat(items).hasSize(1);
      IMenuItemInfo item = items.get(0);
      assertSame(button, item.getModel());
      // presentation
      {
        // no need for image, because parent Menu will include it
        assertNull(item.getImage());
        // some reasonable bounds
        {
          Rectangle bounds = item.getBounds();
          assertThat(bounds.x).isEqualTo(4);
          assertThat(bounds.y).isEqualTo(4);
          assertThat(bounds.width).isGreaterThan(5);
          assertThat(bounds.height).isGreaterThan(20);
        }
      }
      // access
      assertNull(item.getMenu());
      assertSame(IMenuPolicy.NOOP, item.getPolicy());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu: IMenuPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_Menu_IMenuPolicy_CREATE_MenuItem() throws Exception {
    MenuInfo menu =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Menu {",
            "  public Test() {",
            "  }",
            "}");
    menu.refresh();
    IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(menu).getPolicy();
    // don't accept anything other than Component
    assertFalse(policy.validateCreate(new Object()));
    // create new MenuItem
    MenuItemInfo newItem = createJavaInfo("com.extjs.gxt.ui.client.widget.menu.MenuItem");
    assertTrue(policy.validateCreate(newItem));
    policy.commandCreate(newItem, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends Menu {",
        "  public Test() {",
        "    {",
        "      MenuItem menuItem = new MenuItem('New MenuItem');",
        "      add(menuItem);",
        "    }",
        "  }",
        "}");
  }

  public void test_Menu_IMenuPolicy_CREATE_Menu() throws Exception {
    MenuInfo menu =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Menu {",
            "  public Test() {",
            "  }",
            "}");
    menu.refresh();
    IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(menu).getPolicy();
    // don't accept anything other than Component
    assertFalse(policy.validateCreate(new Object()));
    // create new Menu
    MenuInfo newMenu = createJavaInfo("com.extjs.gxt.ui.client.widget.menu.Menu");
    assertTrue(policy.validateCreate(newMenu));
    policy.commandCreate(newMenu, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends Menu {",
        "  public Test() {",
        "    {",
        "      MenuItem menuItem = new MenuItem('New MenuItem');",
        "      add(menuItem);",
        "      {",
        "        Menu menu = new Menu();",
        "        menuItem.setSubMenu(menu);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_Menu_IMenuPolicy_PASTE_validateBad() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      Menu menu = new Menu();",
            "      add(menu);",
            "    }",
            "    add(new com.google.gwt.user.client.ui.Button());",
            "  }",
            "}");
    container.refresh();
    MenuInfo menu = (MenuInfo) container.getWidgets().get(0);
    IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(menu).getPolicy();
    // Object can not be pasted (not list of memento at all)
    assertFalse(policy.validatePaste(new Object()));
    // GWT Button can not be pasted (even if it has memento)
    {
      WidgetInfo button = container.getWidgets().get(1);
      JavaInfoMemento memento = JavaInfoMemento.createMemento(button);
      List<JavaInfoMemento> mementos = ImmutableList.of(memento);
      assertFalse(policy.validatePaste(mementos));
    }
  }

  public void test_Menu_IMenuPolicy_PASTE() throws Exception {
    MenuInfo menu =
        parseJavaInfo(
            "public class Test extends Menu {",
            "  public Test() {",
            "    {",
            "      MenuItem item = new MenuItem('A');",
            "      add(item);",
            "    }",
            "  }",
            "}");
    menu.refresh();
    IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(menu).getPolicy();
    // prepare mementos
    List<JavaInfoMemento> mementos;
    {
      ComponentInfo existingItem = menu.getItems().get(0);
      JavaInfoMemento memento = JavaInfoMemento.createMemento(existingItem);
      mementos = ImmutableList.of(memento);
    }
    // paste
    {
      // validate
      assertTrue(policy.validatePaste(mementos));
      // do paste
      List<?> pastedObjects = policy.commandPaste(mementos, null);
      assertThat(pastedObjects).hasSize(1);
      assertThat(pastedObjects.get(0)).isInstanceOf(ComponentInfo.class);
    }
    assertEditor(
        "public class Test extends Menu {",
        "  public Test() {",
        "    {",
        "      MenuItem item = new MenuItem('A');",
        "      add(item);",
        "    }",
        "    {",
        "      MenuItem menuItem = new MenuItem('A');",
        "      add(menuItem);",
        "    }",
        "  }",
        "}");
  }

  public void test_Menu_IMenuPolicy_MOVE() throws Exception {
    MenuInfo menu =
        parseJavaInfo(
            "public class Test extends Menu {",
            "  public Test() {",
            "    {",
            "      MenuItem item_1 = new MenuItem('A');",
            "      add(item_1);",
            "    }",
            "    {",
            "      MenuItem item_2 = new MenuItem('B');",
            "      add(item_2);",
            "    }",
            "  }",
            "}");
    menu.refresh();
    IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(menu).getPolicy();
    ComponentInfo item_1 = menu.getItems().get(0);
    ComponentInfo item_2 = menu.getItems().get(1);
    // invalid move
    assertFalse(policy.validateMove(new Object()));
    // move "item_2" before "item_1"
    {
      // validate
      assertTrue(policy.validateMove(item_2));
      // do move
      policy.commandMove(item_2, item_1);
    }
    assertEditor(
        "public class Test extends Menu {",
        "  public Test() {",
        "    {",
        "      MenuItem item_2 = new MenuItem('B');",
        "      add(item_2);",
        "    }",
        "    {",
        "      MenuItem item_1 = new MenuItem('A');",
        "      add(item_1);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_contextMenu_parse() throws Exception {
    parseJavaInfo(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    Menu menu = new Menu();",
        "    setContextMenu(menu);",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setContextMenu(menu)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.menu.Menu} {local-unique: menu} {/new Menu()/ /setContextMenu(menu)/}");
  }

  /**
   * Test that <code>Component</code> has simple container for dropping <code>Menu</code>.
   */
  public void test_contextMenu_simpleContainer() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    container.refresh();
    //
    MenuInfo newMenu = createJavaInfo("com.extjs.gxt.ui.client.widget.menu.Menu");
    for (SimpleContainer simpleContainer : new SimpleContainerFactory(container, true).get()) {
      if (simpleContainer.validateComponent(newMenu)) {
        simpleContainer.command_CREATE(newMenu);
        break;
      }
    }
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
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setContextMenu(menu)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.menu.Menu} {local-unique: menu} {/new Menu()/ /setContextMenu(menu)/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MenuItem
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_SeparatorMenuItem_parse() throws Exception {
    MenuInfo menu =
        parseJavaInfo(
            "public class Test extends Menu {",
            "  public Test() {",
            "    {",
            "      SeparatorMenuItem item = new SeparatorMenuItem();",
            "      add(item);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.menu.Menu} {this} {/add(item)/}",
        "  {new: com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem} {local-unique: item} {/new SeparatorMenuItem()/ /add(item)/}");
    menu.refresh();
    ItemInfo item = (ItemInfo) menu.getItems().get(0);
    //
    IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(item);
    assertNotNull(itemObject);
    assertSame(item, itemObject.getModel());
    // presentation
    {
      // no need for image, because parent Menu will include it
      assertNull(itemObject.getImage());
      // some reasonable bounds
      {
        Rectangle bounds = itemObject.getBounds();
        assertEquals(new Rectangle(4, 3, 450 - 4 - 4, 7), bounds);
      }
    }
    // access
    assertNull(itemObject.getMenu());
    assertSame(IMenuPolicy.NOOP, itemObject.getPolicy());
  }

  public void test_MenuItem_parse() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      Menu menu = new Menu();",
            "      add(menu);",
            "      {",
            "        MenuItem item = new MenuItem();",
            "        menu.add(item);",
            "        {",
            "          Menu subMenu = new Menu();",
            "          item.setSubMenu(subMenu);",
            "        }",
            "      }",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(menu)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.menu.Menu} {local-unique: menu} {/new Menu()/ /add(menu)/ /menu.add(item)/}",
        "    {new: com.extjs.gxt.ui.client.widget.menu.MenuItem} {local-unique: item} {/new MenuItem()/ /menu.add(item)/ /item.setSubMenu(subMenu)/}",
        "      {new: com.extjs.gxt.ui.client.widget.menu.Menu} {local-unique: subMenu} {/new Menu()/ /item.setSubMenu(subMenu)/}");
    container.refresh();
    MenuInfo menu = (MenuInfo) container.getWidgets().get(0);
    MenuItemInfo item = (MenuItemInfo) menu.getItems().get(0);
    // prepare "subMenu"
    MenuInfo subMenu = item.getSubMenu();
    assertNotNull(subMenu);
    // prepare object for "subMenu"
    IMenuInfo subMenuObject;
    {
      IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menu);
      IMenuItemInfo itemObject = menuObject.getItems().get(0);
      subMenuObject = itemObject.getMenu();
    }
    // check
    assertNotNull(subMenuObject);
    assertSame(subMenu, subMenuObject.getModel());
    {
      Image image = subMenu.getImage();
      assertNotNull(image);
    }
    {
      Rectangle bounds = subMenu.getBounds();
      assertEquals(120, bounds.width);
      assertEquals(30, bounds.height);
    }
  }
}