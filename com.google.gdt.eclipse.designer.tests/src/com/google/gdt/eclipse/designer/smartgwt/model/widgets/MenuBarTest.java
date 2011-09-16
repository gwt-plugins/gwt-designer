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
package com.google.gdt.eclipse.designer.smartgwt.model.widgets;

import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.menu.MenuBarInfo;
import com.google.gdt.eclipse.designer.smart.model.menu.MenuInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link MenuBarInfo}.
 * 
 * @author sablin_aa
 */
public class MenuBarTest extends SmartGwtModelTest {
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
  public void test_parse() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    MenuBar menuBar = new MenuBar();",
            "    Menu menu = new Menu();",
            "    MenuItem menuItem = new MenuItem('New MenuItem');",
            "    menu.addItem(menuItem);",
            "    menuBar.setMenus(new Menu[] { menu });",
            "    canvas.addChild(menuBar);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    MenuBarInfo menuBar = canvas.getChildren(MenuBarInfo.class).get(0);
    // 
    assertThat(menuBar.getMenusArrayInfo()).isNotNull();
    List<MenuInfo> menus = menuBar.getMenus();
    assertThat(menus.size()).isEqualTo(1);
    assertThat(menus.get(0).getItems().size()).isEqualTo(1);
  }

  public void test_parse_empty() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    MenuBar menuBar = new MenuBar();",
            "    canvas.addChild(menuBar);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    MenuBarInfo menuBar = canvas.getChildren(MenuBarInfo.class).get(0);
    // 
    assertThat(menuBar.getMenusArrayInfo()).isNotNull();
    List<MenuInfo> menus = menuBar.getMenus();
    assertThat(menus).isEmpty();
  }

  public void test_CREATE() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    // 
    MenuBarInfo menuBar = createJavaInfo("com.smartgwt.client.widgets.menu.MenuBar");
    // check "live" image
    assertThat(menuBar.getImage()).isNotNull();
    // do create
    canvas.command_absolute_CREATE(menuBar, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    {",
        "      MenuBar menuBar = new MenuBar();",
        "      canvas.addChild(menuBar);",
        "    }",
        "    canvas.draw();",
        "  }",
        "}");
  }

  public void test_menu_CREATE() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    MenuBar menuBar = new MenuBar();",
            "    Menu menu = new Menu();",
            "    MenuItem menuItem = new MenuItem('New MenuItem');",
            "    menu.addItem(menuItem);",
            "    menuBar.setMenus(new Menu[] { menu });",
            "    canvas.addChild(menuBar);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    MenuBarInfo menuBar = canvas.getChildren(MenuBarInfo.class).get(0);
    // create new Menu
    MenuInfo newMenu = createJavaInfo("com.smartgwt.client.widgets.menu.Menu");
    menuBar.command_CREATE(newMenu, menuBar.getMenus().get(0));
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    MenuBar menuBar = new MenuBar();",
        "    Menu menu = new Menu();",
        "    MenuItem menuItem = new MenuItem('New MenuItem');",
        "    menu.addItem(menuItem);",
        "    menuBar.setMenus(new Menu[] { new Menu(), menu });",
        "    canvas.addChild(menuBar);",
        "    canvas.draw();",
        "  }",
        "}");
  }

  public void test_menu_MOVE() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    MenuBar menuBar = new MenuBar();",
            "    Menu menu = new Menu();",
            "    MenuItem menuItem = new MenuItem('New MenuItem');",
            "    menu.addItem(menuItem);",
            "    menuBar.setMenus(new Menu[] { new Menu(), menu });",
            "    canvas.addChild(menuBar);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    MenuBarInfo menuBar = canvas.getChildren(MenuBarInfo.class).get(0);
    // move Menu
    menuBar.command_MOVE(menuBar.getMenus().get(0), null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    MenuBar menuBar = new MenuBar();",
        "    Menu menu = new Menu();",
        "    MenuItem menuItem = new MenuItem('New MenuItem');",
        "    menu.addItem(menuItem);",
        "    menuBar.setMenus(new Menu[] { menu, new Menu() });",
        "    canvas.addChild(menuBar);",
        "    canvas.draw();",
        "  }",
        "}");
  }

  public void test_menu_ADD() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    MenuBar menuBar = new MenuBar();",
            "    {",
            "      Menu menu = new Menu();",
            "      MenuItem menuItem = new MenuItem('New MenuItem');",
            "      menu.addItem(menuItem);",
            "      menuBar.setMenus(new Menu[] { new Menu(), menu });",
            "    }",
            "    canvas.addChild(menuBar);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    MenuBarInfo menuBar = canvas.getChildren(MenuBarInfo.class).get(0);
    // 
    MenuBarInfo newMenuBar = createJavaInfo("com.smartgwt.client.widgets.menu.MenuBar");
    canvas.command_absolute_CREATE(newMenuBar, null);
    //
    MenuInfo menu = menuBar.getMenus().get(1);
    newMenuBar.command_MOVE(menu, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    MenuBar menuBar = new MenuBar();",
        "    {",
        "      menuBar.setMenus(new Menu[] { new Menu() });",
        "    }",
        "    canvas.addChild(menuBar);",
        "    {",
        "      MenuBar menuBar_1 = new MenuBar();",
        "      Menu menu = new Menu();",
        "      MenuItem menuItem = new MenuItem('New MenuItem');",
        "      menu.addItem(menuItem);",
        "      menuBar_1.setMenus(new Menu[] { menu});",
        "      canvas.addChild(menuBar_1);",
        "    }",
        "    canvas.draw();",
        "  }",
        "}");
  }
}