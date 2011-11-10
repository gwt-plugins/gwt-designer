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

import com.google.gdt.eclipse.designer.gwtext.model.widgets.ContainerInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.MenuInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests for {@link MenuInfo}.
 * 
 * @author scheglov_ke
 */
public class MenuTest extends GwtExtModelTest {
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
  public void test_parse_addItem() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Toolbar toolbar = new Toolbar();",
            "      toolbar.setAutoHeight(true);",
            "      {",
            "        ToolbarMenuButton button = new ToolbarMenuButton();",
            "        {",
            "          Menu menu = new Menu();",
            "          menu.addItem(new Item('A'));",
            "          menu.addItem(new Item('B'));",
            "          button.setMenu(menu);",
            "        }",
            "        toolbar.addButton(button);",
            "      }",
            "      add(toolbar);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(toolbar)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.Toolbar} {local-unique: toolbar} {/new Toolbar()/ /toolbar.setAutoHeight(true)/ /toolbar.addButton(button)/ /add(toolbar)/}",
        "    {new: com.gwtext.client.widgets.ToolbarMenuButton} {local-unique: button} {/new ToolbarMenuButton()/ /button.setMenu(menu)/ /toolbar.addButton(button)/}",
        "      {new: com.gwtext.client.widgets.menu.Menu} {local-unique: menu} {/new Menu()/ /menu.addItem(new Item('A'))/ /menu.addItem(new Item('B'))/ /button.setMenu(menu)/}",
        "        {new: com.gwtext.client.widgets.menu.Item} {empty} {/menu.addItem(new Item('A'))/}",
        "        {new: com.gwtext.client.widgets.menu.Item} {empty} {/menu.addItem(new Item('B'))/}");
  }

  public void test_parse_addSeparator() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Toolbar toolbar = new Toolbar();",
            "      toolbar.setAutoHeight(true);",
            "      {",
            "        ToolbarMenuButton button = new ToolbarMenuButton();",
            "        {",
            "          Menu menu = new Menu();",
            "          menu.addSeparator();",
            "          button.setMenu(menu);",
            "        }",
            "        toolbar.addButton(button);",
            "      }",
            "      add(toolbar);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(toolbar)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.Toolbar} {local-unique: toolbar} {/new Toolbar()/ /toolbar.setAutoHeight(true)/ /toolbar.addButton(button)/ /add(toolbar)/}",
        "    {new: com.gwtext.client.widgets.ToolbarMenuButton} {local-unique: button} {/new ToolbarMenuButton()/ /button.setMenu(menu)/ /toolbar.addButton(button)/}",
        "      {new: com.gwtext.client.widgets.menu.Menu} {local-unique: menu} {/new Menu()/ /menu.addSeparator()/ /button.setMenu(menu)/}",
        "        {void} {void} {/menu.addSeparator()/}");
    // find addSeparator()
    JavaInfo separator;
    {
      ContainerInfo toolbar = (ContainerInfo) panel.getChildrenWidgets().get(0);
      WidgetInfo button = toolbar.getChildrenWidgets().get(0);
      MenuInfo menu = button.getChildren(MenuInfo.class).get(0);
      separator = menu.getChildrenJava().get(0);
    }
    // check properties
    Property[] properties = separator.getProperties();
    assertThat(properties).isEmpty();
  }

  public void test_parse_addText() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Toolbar toolbar = new Toolbar();",
            "      toolbar.setAutoHeight(true);",
            "      {",
            "        ToolbarMenuButton button = new ToolbarMenuButton();",
            "        {",
            "          Menu menu = new Menu();",
            "          menu.addText('txt');",
            "          button.setMenu(menu);",
            "        }",
            "        toolbar.addButton(button);",
            "      }",
            "      add(toolbar);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(toolbar)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}");
  }

  public void test_parse_addItem_TextItem() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Toolbar toolbar = new Toolbar();",
            "      toolbar.setAutoHeight(true);",
            "      {",
            "        ToolbarMenuButton button = new ToolbarMenuButton();",
            "        {",
            "          Menu menu = new Menu();",
            "          menu.addItem(new TextItem('txt'));",
            "          button.setMenu(menu);",
            "        }",
            "        toolbar.addButton(button);",
            "      }",
            "      add(toolbar);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(toolbar)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.Toolbar} {local-unique: toolbar} {/new Toolbar()/ /toolbar.setAutoHeight(true)/ /toolbar.addButton(button)/ /add(toolbar)/}",
        "    {new: com.gwtext.client.widgets.ToolbarMenuButton} {local-unique: button} {/new ToolbarMenuButton()/ /button.setMenu(menu)/ /toolbar.addButton(button)/}",
        "      {new: com.gwtext.client.widgets.menu.Menu} {local-unique: menu} {/new Menu()/ /menu.addItem(new TextItem('txt'))/ /button.setMenu(menu)/}",
        "        {new: com.gwtext.client.widgets.menu.TextItem} {empty} {/menu.addItem(new TextItem('txt'))/}");
  }

  public void test_parse_addItem_Separator() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Toolbar toolbar = new Toolbar();",
            "      toolbar.setAutoHeight(true);",
            "      {",
            "        ToolbarMenuButton button = new ToolbarMenuButton();",
            "        {",
            "          Menu menu = new Menu();",
            "          menu.addItem(new Separator());",
            "          button.setMenu(menu);",
            "        }",
            "        toolbar.addButton(button);",
            "      }",
            "      add(toolbar);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(toolbar)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.Toolbar} {local-unique: toolbar} {/new Toolbar()/ /toolbar.setAutoHeight(true)/ /toolbar.addButton(button)/ /add(toolbar)/}",
        "    {new: com.gwtext.client.widgets.ToolbarMenuButton} {local-unique: button} {/new ToolbarMenuButton()/ /button.setMenu(menu)/ /toolbar.addButton(button)/}",
        "      {new: com.gwtext.client.widgets.menu.Menu} {local-unique: menu} {/new Menu()/ /menu.addItem(new Separator())/ /button.setMenu(menu)/}",
        "        {new: com.gwtext.client.widgets.menu.Separator} {empty} {/menu.addItem(new Separator())/}");
  }

  public void test_parse_RootPanel_addItem_Separator() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Toolbar toolbar = new Toolbar();",
            "      toolbar.setAutoHeight(true);",
            "      {",
            "        ToolbarButton button = new ToolbarButton();",
            "        {",
            "          Menu menu = new Menu();",
            "          menu.addItem(new Separator());",
            "          button.setMenu(menu);",
            "        }",
            "        toolbar.addButton(button);",
            "      }",
            "      rootPanel.add(toolbar);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(toolbar)/}",
        "  {new: com.gwtext.client.widgets.Toolbar} {local-unique: toolbar} {/new Toolbar()/ /toolbar.setAutoHeight(true)/ /toolbar.addButton(button)/ /rootPanel.add(toolbar)/}",
        "    {new: com.gwtext.client.widgets.ToolbarButton} {local-unique: button} {/new ToolbarButton()/ /button.setMenu(menu)/ /toolbar.addButton(button)/}",
        "      {new: com.gwtext.client.widgets.menu.Menu} {local-unique: menu} {/new Menu()/ /menu.addItem(new Separator())/ /button.setMenu(menu)/}",
        "        {new: com.gwtext.client.widgets.menu.Separator} {empty} {/menu.addItem(new Separator())/}");
  }
}