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
import com.google.gdt.eclipse.designer.smart.model.menu.MenuButtonInfo;
import com.google.gdt.eclipse.designer.smart.model.menu.MenuInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link MenuButtonInfo}.
 * 
 * @author sablin_aa
 */
public class MenuButtonTest extends SmartGwtModelTest {
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
  public void test_parse_menuButton() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    MenuButton menuButton = new MenuButton('New MenuButton');",
            "    Menu menu = new Menu();",
            "    MenuItem menuItem = new MenuItem('New MenuItem');",
            "    menu.addItem(menuItem);",
            "    menuButton.setMenu(menu);",
            "    canvas.addChild(menuButton);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    MenuButtonInfo menuButton = canvas.getChildren(MenuButtonInfo.class).get(0);
    //
    MenuInfo menu = menuButton.getMenu();
    assertThat(menu).isNotNull();
    assertThat(menu.getItems().size()).isEqualTo(1);
  }
}