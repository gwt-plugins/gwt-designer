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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.widgets.generic.GwtGefTest;
import com.google.gdt.eclipse.designer.model.widgets.menu.MenuBarInfo;
import com.google.gdt.eclipse.designer.model.widgets.menu.MenuItemInfo;
import com.google.gdt.eclipse.designer.model.widgets.menu.MenuItemSeparatorInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;

/**
 * Test for {@link MenuBarInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class MenuBarGefTest extends GwtGefTest {
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
  public void test_canvas_CREATE_MenuBar() throws Exception {
    ComplexPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    loadCreationTool("com.google.gwt.user.client.ui.MenuBar");
    canvas.moveTo(panel, 0.5, 0.5).click();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      MenuBar menuBar = new MenuBar(false);",
        "      add(menuBar);",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_CREATE_MenuItem() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      MenuBar menuBar = new MenuBar(false);",
        "      add(menuBar);",
        "    }",
        "  }",
        "}");
    MenuBarInfo menuBar = getJavaInfoByName("menuBar");
    //
    MenuItemInfo newItem = loadCreationTool("com.google.gwt.user.client.ui.MenuItem");
    canvas.moveTo(menuBar, 5, 0.5).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      MenuBar menuBar = new MenuBar(false);",
        "      add(menuBar);",
        "      {",
        "        MenuItem menuItem = new MenuItem('New item', false, (Command) null);",
        "        menuBar.addItem(menuItem);",
        "      }",
        "    }",
        "  }",
        "}");
    canvas.assertNotNullEditPart(newItem);
    canvas.assertPrimarySelected(newItem);
  }

  public void test_canvas_CREATE_MenuItemSeparator() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      MenuBar menuBar = new MenuBar(false);",
        "      add(menuBar);",
        "    }",
        "  }",
        "}");
    MenuBarInfo menuBar = getJavaInfoByName("menuBar");
    //
    MenuItemSeparatorInfo newSeparator =
        loadCreationTool("com.google.gwt.user.client.ui.MenuItemSeparator");
    canvas.moveTo(menuBar, 5, 0.5).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      MenuBar menuBar = new MenuBar(false);",
        "      add(menuBar);",
        "      {",
        "        MenuItemSeparator separator = new MenuItemSeparator();",
        "        menuBar.addSeparator(separator);",
        "      }",
        "    }",
        "  }",
        "}");
    canvas.assertNotNullEditPart(newSeparator);
    canvas.assertPrimarySelected(newSeparator);
  }

  public void test_canvas_CREATE_subMenu() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      MenuBar menuBar = new MenuBar(false);",
        "      add(menuBar);",
        "    }",
        "  }",
        "}");
    MenuBarInfo menuBar = getJavaInfoByName("menuBar");
    //
    loadCreationTool("com.google.gwt.user.client.ui.MenuItem", "withSubMenu");
    canvas.moveTo(menuBar, 5, 0.5).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      MenuBar menuBar = new MenuBar(false);",
        "      add(menuBar);",
        "      {",
        "        MenuBar menuBar_1 = new MenuBar(true);",
        "        MenuItem menuItem = new MenuItem('New menu', false, menuBar_1);",
        "        menuBar.addItem(menuItem);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_canvas_CREATE_MenuItem_intoSubMenu() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      MenuBar menuBar = new MenuBar(false);",
        "      add(menuBar);",
        "      {",
        "        MenuBar subMenu = new MenuBar(true);",
        "        MenuItem subMenuItem = new MenuItem('My menu', false, subMenu);",
        "        menuBar.addItem(subMenuItem);",
        "      }",
        "    }",
        "  }",
        "}");
    MenuItemInfo subMenuItem = getJavaInfoByName("subMenuItem");
    MenuBarInfo subMenu = getJavaInfoByName("subMenu");
    //
    loadCreationTool("com.google.gwt.user.client.ui.MenuItem");
    // initially no EditPart for "subMenu"
    canvas.assertNullEditPart(subMenu);
    // move on "subMenuItem", show "subMenu"
    canvas.moveTo(subMenuItem, 0.5, 0.5);
    canvas.assertNotNullEditPart(subMenu);
    // move on "subMenu" and click
    canvas.moveTo(subMenu, 0.5, 0.5).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      MenuBar menuBar = new MenuBar(false);",
        "      add(menuBar);",
        "      {",
        "        MenuBar subMenu = new MenuBar(true);",
        "        MenuItem subMenuItem = new MenuItem('My menu', false, subMenu);",
        "        {",
        "          MenuItem menuItem = new MenuItem('New item', false, (Command) null);",
        "          subMenu.addItem(menuItem);",
        "        }",
        "        menuBar.addItem(subMenuItem);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE_MenuBar() throws Exception {
    ComplexPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "  }",
            "}");
    //
    loadCreationTool("com.google.gwt.user.client.ui.MenuBar");
    tree.moveOn(panel).click();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      MenuBar menuBar = new MenuBar(false);",
        "      add(menuBar);",
        "    }",
        "  }",
        "}");
  }

  public void test_tree_CREATE_MenuItem() throws Exception {
    openJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      MenuBar menuBar = new MenuBar(false);",
        "      add(menuBar);",
        "    }",
        "  }",
        "}");
    MenuBarInfo menuBar = getJavaInfoByName("menuBar");
    //
    loadCreationTool("com.google.gwt.user.client.ui.MenuItem");
    tree.moveOn(menuBar).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      MenuBar menuBar = new MenuBar(false);",
        "      add(menuBar);",
        "      {",
        "        MenuItem menuItem = new MenuItem('New item', false, (Command) null);",
        "        menuBar.addItem(menuItem);",
        "      }",
        "    }",
        "  }",
        "}");
  }
}
