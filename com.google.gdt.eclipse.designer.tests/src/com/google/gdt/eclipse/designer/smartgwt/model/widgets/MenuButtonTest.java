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