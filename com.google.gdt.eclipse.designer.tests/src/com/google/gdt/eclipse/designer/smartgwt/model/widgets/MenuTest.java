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
import com.google.gdt.eclipse.designer.smart.model.TabSetInfo;
import com.google.gdt.eclipse.designer.smart.model.menu.MenuInfo;
import com.google.gdt.eclipse.designer.smart.model.menu.MenuItemInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link MenuInfo}, {@link MenuItemInfo}.
 * 
 * @author sablin_aa
 */
public class MenuTest extends SmartGwtModelTest {
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
            "    Menu menu = new Menu();",
            "    MenuItem menuItem = new MenuItem('MenuItem');",
            "    menu.setItems(menuItem);",
            "    canvas.setContextMenu(menu);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    MenuInfo menu = canvas.getChildren(MenuInfo.class).get(0);
    //
    List<MenuItemInfo> items = menu.getItems();
    assertThat(items.size()).isEqualTo(1);
    MenuItemInfo item = items.get(0);
    assertThat(item.getSubmenu()).isNull();
  }

  public void test_item_CREATE() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    Menu menu = new Menu();",
            "    canvas.setContextMenu(menu);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    MenuInfo menu = canvas.getChildren(MenuInfo.class).get(0);
    // create new MenuItem
    MenuItemInfo newItem = createJavaInfo("com.smartgwt.client.widgets.menu.MenuItem");
    {
      FlowContainer flowContainer = new FlowContainerFactory(menu, false).get().get(0);
      assertTrue(flowContainer.validateComponent(newItem));
      flowContainer.command_CREATE(newItem, null);
    }
    assertThat(newItem.getMenu()).isSameAs(menu);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    Menu menu = new Menu();",
        "    {",
        "      MenuItem menuItem = new MenuItem('New MenuItem');",
        "      menu.addItem(menuItem);",
        "    }",
        "    canvas.setContextMenu(menu);",
        "    canvas.draw();",
        "  }",
        "}");
  }

  public void test_item_MOVE() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    Menu menu = new Menu();",
            "    MenuItem menuItem = new MenuItem('MenuItem');",
            "    menu.setItems(menuItem, new MenuItemSeparator());",
            "    canvas.setContextMenu(menu);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    MenuInfo menu = canvas.getChildren(MenuInfo.class).get(0);
    //
    List<MenuItemInfo> items = menu.getItems();
    assertThat(items.size()).isEqualTo(2);
    MenuItemInfo item = items.get(0);
    MenuItemInfo separator = items.get(1);
    {
      FlowContainer flowContainer = new FlowContainerFactory(menu, false).get().get(0);
      flowContainer.command_MOVE(separator, item);
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    Menu menu = new Menu();",
        "    MenuItem menuItem = new MenuItem('MenuItem');",
        "    MenuItemSeparator menuItemSeparator = new MenuItemSeparator();",
        "    menu.addItem(menuItemSeparator);",
        "    menu.setItems(menuItem);",
        "    canvas.setContextMenu(menu);",
        "    canvas.draw();",
        "  }",
        "}");
  }

  /**
   * Test dispose objects when it been not rendered.
   */
  public void test_dispose() throws Exception {
    TabSetInfo tabSet =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    TabSet tabSet = new TabSet();",
            "    tabSet.addTab(new Tab('Tab_1'));",
            "    Tab tab = new Tab('Tab_2');",
            "    {",
            "      Canvas canvas = new Canvas();",
            "      Menu menu = new Menu();",
            "      MenuItem menuItem = new MenuItem('MenuItem');",
            "      menu.setItems(menuItem);",
            "      canvas.addChild(menu);",
            "      tab.setPane(canvas);",
            "    }",
            "    tabSet.addTab(tab);",
            "    tabSet.draw();",
            "  }",
            "}"});
    tabSet.refresh();
  }
}