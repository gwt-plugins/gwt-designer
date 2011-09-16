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

import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.GwtToolkitDescription;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.menu.MenuBarInfo;
import com.google.gdt.eclipse.designer.model.widgets.menu.MenuItemInfo;
import com.google.gdt.eclipse.designer.model.widgets.menu.MenuItemSeparatorInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.ConstructorChildAssociation;
import org.eclipse.wb.core.model.association.ImplicitFactoryArgumentAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.variable.description.FieldInitializerVariableDescription;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

import static org.easymock.EasyMock.capture;
import static org.fest.assertions.Assertions.assertThat;

import org.easymock.Capture;
import org.easymock.EasyMock;

import java.util.List;

/**
 * Test for {@link MenuBarInfo} and {@link MenuItemInfo}.
 * 
 * @author scheglov_ke
 */
public class MenuBarTest extends GwtModelTest {
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
   * Even empty <code>MenuBar</code> has some reasonable size.
   */
  public void test_empty() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "  }",
        "}");
    refresh();
    MenuBarInfo bar = getJavaInfoByName("bar");
    assertThat(bar.getItems()).isEmpty();
    assertThat(bar.getAllItems()).isEmpty();
    assertThat(bar.getBounds().width).isGreaterThan(100);
    assertThat(bar.getBounds().height).isGreaterThan(20);
  }

  /**
   * Animation for <code>MenuBar</code> looks nice, but does not allow us to get screen shots of
   * items. So, we should disable it.
   */
  public void test_disableAnimation() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    bar.setAnimationEnabled(true);",
        "    rootPanel.add(bar);",
        "  }",
        "}");
    refresh();
    //
    MenuBarInfo bar = getJavaInfoByName("bar");
    assertEquals(false, ReflectionUtils.invokeMethod(bar.getObject(), "isAnimationEnabled()"));
  }

  public void test_parse_addItem_usingConstructor() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "      {",
        "        MenuItem menuItem = new MenuItem('A', (Command) null);",
        "        bar.addItem(menuItem);",
        "      }",
        "  }",
        "}");
    refresh();
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(bar)/}",
        "  {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: bar} {/new MenuBar()/ /rootPanel.add(bar)/ /bar.addItem(menuItem)/}",
        "    {new: com.google.gwt.user.client.ui.MenuItem} {local-unique: menuItem} {/new MenuItem('A', (Command) null)/ /bar.addItem(menuItem)/}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    MenuItemInfo item = bar.getItems().get(0);
    // association
    assertInstanceOf(InvocationChildAssociation.class, item.getAssociation());
    // only item in "bar"
    assertThat(bar.getAllItems()).containsOnly(item);
    // check "Constructor" property
    {
      assertNotNull(PropertyUtils.getByPath(item, "Constructor/text"));
      assertNotNull(PropertyUtils.getByPath(item, "Constructor/cmd"));
    }
    // check "text" property
    Property textProperty = item.getPropertyByTitle("text");
    assertEquals("A", textProperty.getValue());
    textProperty.setValue("B");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "      {",
        "        MenuItem menuItem = new MenuItem('B', (Command) null);",
        "        bar.addItem(menuItem);",
        "      }",
        "  }",
        "}");
  }

  public void test_parse_addItem_withCommand() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      bar.addItem('A', (Command) null);",
        "    }",
        "  }",
        "}");
    refresh();
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(bar)/}",
        "  {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: bar} {/new MenuBar()/ /rootPanel.add(bar)/ /bar.addItem('A', (Command) null)/}",
        "    {implicit-factory} {empty} {/bar.addItem('A', (Command) null)/}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    MenuItemInfo item = bar.getItems().get(0);
    // association
    assertInstanceOf(InvocationVoidAssociation.class, item.getAssociation());
    // only item in "bar"
    assertThat(bar.getAllItems()).containsOnly(item);
    // check "Factory" property
    {
      assertNotNull(PropertyUtils.getByPath(item, "Factory/text"));
      assertNotNull(PropertyUtils.getByPath(item, "Factory/cmd"));
    }
    // check "text" property
    Property textProperty = item.getPropertyByTitle("text");
    assertEquals("A", textProperty.getValue());
    textProperty.setValue("B");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      bar.addItem('B', (Command) null);",
        "    }",
        "  }",
        "}");
  }

  public void test_parse_addItem_subMenu() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuBar subMenu = new MenuBar(false);",
        "      bar.addItem('Sub', subMenu);",
        "    }",
        "  }",
        "}");
    refresh();
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(bar)/}",
        "  {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: bar} {/new MenuBar()/ /rootPanel.add(bar)/ /bar.addItem('Sub', subMenu)/}",
        "    {implicit-factory} {empty} {/bar.addItem('Sub', subMenu)/}",
        "      {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: subMenu} {/new MenuBar(false)/ /bar.addItem('Sub', subMenu)/}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    MenuItemInfo item = bar.getItems().get(0);
    MenuBarInfo subMenu = item.getSubMenu();
    assertThat(subMenu.getAssociation()).isInstanceOf(ImplicitFactoryArgumentAssociation.class);
    // clipboard
    assertClipboardSource(
        item,
        "%parent%.addItem(\"Sub\", (com.google.gwt.user.client.ui.MenuBar) null)");
  }

  public void test_parse_newItem_subMenu() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuBar subMenu = new MenuBar(false);",
        "      MenuItem menuItem = new MenuItem('Sub', subMenu);",
        "      bar.addItem(menuItem);",
        "    }",
        "  }",
        "}");
    refresh();
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(bar)/}",
        "  {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: bar} {/new MenuBar()/ /rootPanel.add(bar)/ /bar.addItem(menuItem)/}",
        "    {new: com.google.gwt.user.client.ui.MenuItem} {local-unique: menuItem} {/new MenuItem('Sub', subMenu)/ /bar.addItem(menuItem)/}",
        "      {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: subMenu} {/new MenuBar(false)/ /new MenuItem('Sub', subMenu)/}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    MenuItemInfo item = bar.getItems().get(0);
    MenuBarInfo subMenu = item.getSubMenu();
    assertThat(item.getAssociation()).isInstanceOf(InvocationChildAssociation.class);
    assertThat(subMenu.getAssociation()).isInstanceOf(ConstructorChildAssociation.class);
    // clipboard
    assertClipboardSource(
        item,
        "new com.google.gwt.user.client.ui.MenuItem(\"Sub\", (com.google.gwt.user.client.ui.MenuBar) null)");
  }

  /**
   * When <code>MenuItem</code> is disabled, we can not show its <code>MenuBar</code>, so we should
   * ensure that <code>MenuItem</code> is enabled.
   * <p>
   * http://code.google.com/p/google-web-toolkit/issues/detail?id=6552
   */
  public void test_parse_subMenu_disabled() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    MenuBar bar = new MenuBar();",
        "    add(bar);",
        "    {",
        "      MenuBar subMenu = new MenuBar(false);",
        "      MenuItem menuItem = new MenuItem('Sub', subMenu);",
        "      menuItem.setEnabled(false);",
        "      bar.addItem(menuItem);",
        "    }",
        "  }",
        "}");
    refresh();
    assertNoErrors(m_lastParseInfo);
  }

  public void test_parse_addSeparator_usingConstructor() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "      {",
        "        MenuItemSeparator separator = new MenuItemSeparator();",
        "        bar.addSeparator(separator);",
        "      }",
        "  }",
        "}");
    refresh();
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(bar)/}",
        "  {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: bar} {/new MenuBar()/ /rootPanel.add(bar)/ /bar.addSeparator(separator)/}",
        "    {new: com.google.gwt.user.client.ui.MenuItemSeparator} {local-unique: separator} {/new MenuItemSeparator()/ /bar.addSeparator(separator)/}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    MenuItemSeparatorInfo separator = (MenuItemSeparatorInfo) bar.getAllItems().get(0);
    // association
    assertInstanceOf(InvocationChildAssociation.class, separator.getAssociation());
    // only item in "bar"
    assertThat(bar.getAllItems()).containsOnly(separator);
    // IMenuItemInfo
    {
      IMenuItemInfo separatorObject;
      {
        IMenuInfo barObject = MenuObjectInfoUtils.getMenuInfo(bar);
        List<IMenuItemInfo> items = barObject.getItems();
        assertThat(items).hasSize(1);
        separatorObject = items.get(0);
      }
      // ask IMenuItemInfo directly
      assertNull(separator.getAdapter(List.class));
      assertSame(separatorObject, MenuObjectInfoUtils.getMenuItemInfo(separator));
      // model
      assertSame(separator, separatorObject.getModel());
      assertSame(separator, separatorObject.getToolkitModel());
      // presentation
      assertNull(separatorObject.getImage());
      assertNotNull(separatorObject.getBounds());
      // access
      assertNull(separatorObject.getMenu());
      assertSame(IMenuPolicy.NOOP, separatorObject.getPolicy());
    }
  }

  /**
   * Anonymous <code>Command</code> is usual situation, so we should silently replace it with
   * <code>null</code> and don't show anything in log.
   */
  public void test_parse_withAnonymousCommand() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    MenuBar bar = new MenuBar();",
            "    rootPanel.add(bar);",
            "    {",
            "      MenuItem menuItem = bar.addItem('A', new Command() {",
            "        public void execute() {",
            "        }",
            "      });",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(bar)/}",
        "  {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: bar} {/new MenuBar()/ /rootPanel.add(bar)/ /bar.addItem('A', new Command() {\n\t\t\t\tpublic void execute() {\n\t\t\t\t}\n\t\t\t})/}",
        "    {implicit-factory} {local-unique: menuItem} {/bar.addItem('A', new Command() {\n\t\t\t\tpublic void execute() {\n\t\t\t\t}\n\t\t\t})/}");
    //
    refresh();
    assertNoErrors(frame);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sub menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_IMenuInfo_IMenuItemInfo() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    MenuBar bar = new MenuBar();",
            "    rootPanel.add(bar, 10, 10);",
            "    {",
            "      MenuBar subMenu = new MenuBar(true);",
            "      bar.addItem('Sub', subMenu);",
            "      subMenu.addItem('Sub item 1', (Command) null);",
            "      subMenu.addItem('Sub item 2', (Command) null);",
            "    }",
            "    bar.addItem('Separate item', (Command) null);",
            "  }",
            "}");
    refresh();
    MenuBarInfo menu = (MenuBarInfo) frame.getChildrenWidgets().get(0);
    MenuItemInfo item_1 = menu.getItems().get(0);
    MenuItemInfo item_2 = menu.getItems().get(1);
    MenuBarInfo subMenu = item_1.getSubMenu();
    MenuItemInfo subMenuItem_1 = subMenu.getItems().get(0);
    MenuItemInfo subMenuItem_2 = subMenu.getItems().get(1);
    // bad adapters
    {
      assertNull(menu.getAdapter(List.class));
      assertNull(item_1.getAdapter(List.class));
    }
    // IMenuInfo
    {
      IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menu);
      assertSame(menu, menuObject.getModel());
      // presentation
      {
        // no need for image, because this MenuBar is visible on usual screen shot
        assertNull(menuObject.getImage());
        // some reasonable bounds
        {
          Rectangle bounds = menuObject.getBounds();
          assertThat(bounds.x).isEqualTo(10);
          assertThat(bounds.y).isEqualTo(10);
          assertThat(bounds.width).isGreaterThan(30);
          assertThat(bounds.height).isGreaterThan(20);
        }
      }
      // access
      assertTrue(menuObject.isHorizontal());
      {
        List<IMenuItemInfo> itemObjects = menuObject.getItems();
        assertThat(itemObjects).hasSize(2);
        // "Sub" item
        {
          IMenuItemInfo itemObject = itemObjects.get(0);
          assertSame(item_1, itemObject.getModel());
          // presentation
          {
            // items don't have screen shot, we show them on menu shots
            assertNull(itemObject.getImage());
            // reasonable bounds
            {
              Rectangle bounds = itemObject.getBounds();
              assertThat(bounds.x).isEqualTo(2);
              assertThat(bounds.y).isEqualTo(2);
              assertThat(bounds.width).isGreaterThan(30);
              assertThat(bounds.height).isGreaterThan(20);
            }
          }
          // sub-menu
          {
            IMenuInfo subMenuObject = itemObject.getMenu();
            assertSame(subMenu, subMenuObject.getModel());
            // access
            assertFalse(subMenuObject.isHorizontal());
            {
              List<IMenuItemInfo> subMenuItemObjects = subMenuObject.getItems();
              assertThat(subMenuItemObjects).hasSize(2);
              assertSame(subMenuItem_1, subMenuItemObjects.get(0).getModel());
              assertSame(subMenuItem_2, subMenuItemObjects.get(1).getModel());
            }
            // presentation
            assertNotNull(subMenuObject.getImage());
            {
              Rectangle bounds = subMenuObject.getBounds();
              assertThat(bounds.width).isGreaterThan(85);
              assertThat(bounds.height).isGreaterThan(45);
            }
          }
        }
        // "Separate item"
        {
          IMenuItemInfo itemObject = itemObjects.get(1);
          assertSame(item_2, itemObject.getModel());
          // no sub-menu
          assertNull(itemObject.getMenu());
          // no policy
          assertSame(IMenuPolicy.NOOP, itemObject.getPolicy());
        }
      }
    }
  }

  public void test_delete_subMenu() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuBar subMenu = new MenuBar(false);",
        "      bar.addItem('Sub', subMenu);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(bar)/}",
        "  {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: bar} {/new MenuBar()/ /rootPanel.add(bar)/ /bar.addItem('Sub', subMenu)/}",
        "    {implicit-factory} {empty} {/bar.addItem('Sub', subMenu)/}",
        "      {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: subMenu} {/new MenuBar(false)/ /bar.addItem('Sub', subMenu)/}");
    MenuBarInfo subMenu = getJavaInfoByName("subMenu");
    // delete "subMenu", should delete also "item"
    subMenu.delete();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(bar)/}",
        "  {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: bar} {/new MenuBar()/ /rootPanel.add(bar)/}");
  }

  /**
   * Delete menu bar with sub menu.
   */
  public void test_delete_withSubMenu() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuBar subMenu = new MenuBar(false);",
        "      bar.addItem('Sub', subMenu);",
        "    }",
        "  }",
        "}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    // delete "bar" fully
    bar.delete();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuPolicy: separator
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new <code>MenuItemSeparator</code> using
   * <code>MenuBar.addSeparator(MenuItemSeparator)</code>.
   */
  public void test_IMenuPolicy_CREATE_MenuItemSeparator_newInstance() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "  }",
        "}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    IMenuInfo barObject = MenuObjectInfoUtils.getMenuInfo(bar);
    IMenuPolicy barPolicy = barObject.getPolicy();
    // don't accept something other than MenuItem or MenuItemSeparator
    assertFalse(barPolicy.validateCreate(new Object()));
    // create new MenuItemSeparator
    MenuItemSeparatorInfo newSeparator =
        createJavaInfo("com.google.gwt.user.client.ui.MenuItemSeparator");
    assertTrue(barPolicy.validateCreate(newSeparator));
    barPolicy.commandCreate(newSeparator, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuItemSeparator separator = new MenuItemSeparator();",
        "      bar.addSeparator(separator);",
        "    }",
        "  }",
        "}");
  }

  public void test_IMenuPolicy_MOVE_MenuItemSeparator() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuItemSeparator separator = new MenuItemSeparator();",
        "      bar.addSeparator(separator);",
        "    }",
        "    {",
        "      MenuItem menuItem = new MenuItem('Item', false, (Command) null);",
        "      bar.addItem(menuItem);",
        "    }",
        "  }",
        "}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    MenuItemSeparatorInfo separator = (MenuItemSeparatorInfo) bar.getAllItems().get(0);
    IMenuInfo barObject = MenuObjectInfoUtils.getMenuInfo(bar);
    IMenuPolicy barPolicy = barObject.getPolicy();
    // don't accept something other than MenuItem or MenuItemSeparator
    assertFalse(barPolicy.validateMove(new Object()));
    assertTrue(barPolicy.validateMove(separator));
    barPolicy.commandMove(separator, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuItem menuItem = new MenuItem('Item', false, (Command) null);",
        "      bar.addItem(menuItem);",
        "    }",
        "    {",
        "      MenuItemSeparator separator = new MenuItemSeparator();",
        "      bar.addSeparator(separator);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuPolicy: item
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new <code>MenuItem</code> using <code>MenuBar.addItem(MenuItem)</code>.
   */
  public void test_IMenuPolicy_CREATE_MenuItem_newInstance() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "  }",
        "}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    IMenuInfo barObject = MenuObjectInfoUtils.getMenuInfo(bar);
    IMenuPolicy policy = barObject.getPolicy();
    // don't accept something other than MenuItem
    assertFalse(policy.validateCreate(new Object()));
    // create new MenuItem
    MenuItemInfo newItem = createJavaInfo("com.google.gwt.user.client.ui.MenuItem");
    assertTrue(policy.validateCreate(newItem));
    policy.commandCreate(newItem, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuItem menuItem = new MenuItem('New item', false, (Command) null);",
        "      bar.addItem(menuItem);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Adds new <code>MenuItem</code> with sub-menu.
   * <p>
   * It is hard to generate correct code for "field initializer" variable, so we use script to force
   * "field unique" variable for <code>MenuItem</code>.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43781
   */
  public void test_IMenuPolicy_CREATE_MenuItem_newInstance_subMenu_whenFieldInitializer()
      throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  private final MenuBar bar = new MenuBar();",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    rootPanel.add(bar);",
        "  }",
        "}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(bar).getPolicy();
    // use "field initializer"
    {
      GenerationSettings generationSettings =
          GwtToolkitDescription.INSTANCE.getGenerationSettings();
      generationSettings.setVariable(FieldInitializerVariableDescription.INSTANCE);
    }
    // create new MenuItem
    MenuItemInfo newItem = createJavaInfo("com.google.gwt.user.client.ui.MenuItem", "withSubMenu");
    assertTrue(policy.validateCreate(newItem));
    policy.commandCreate(newItem, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private final MenuBar bar = new MenuBar();",
        "  private MenuItem menuItem;",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuBar menuBar = new MenuBar(true);",
        "      menuItem = new MenuItem('New menu', false, menuBar);",
        "      bar.addItem(menuItem);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(bar)/}",
        "  {new: com.google.gwt.user.client.ui.MenuBar} {field-initializer: bar} {/new MenuBar()/ /rootPanel.add(bar)/ /bar.addItem(menuItem)/}",
        "    {new: com.google.gwt.user.client.ui.MenuItem withSubMenu} {field-unique: menuItem} {/new MenuItem('New menu', false, menuBar)/ /bar.addItem(menuItem)/}",
        "      {new: com.google.gwt.user.client.ui.MenuBar vertical} {local-unique: menuBar} {/new MenuBar(true)/ /new MenuItem('New menu', false, menuBar)/}");
  }

  /**
   * Adds new <code>MenuItem</code> using <code>MenuBar.addItem(String,asHTML,Command)</code>.
   */
  public void test_IMenuPolicy_CREATE_MenuItem_factoryMethod() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "  }",
        "}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    IMenuInfo barObject = MenuObjectInfoUtils.getMenuInfo(bar);
    IMenuPolicy policy = barObject.getPolicy();
    // prepare CreationSupport
    CreationSupport creationSupport;
    {
      String signature = "addItem(java.lang.String,boolean,com.google.gwt.user.client.Command)";
      String source = "addItem(\"New item\", false, (com.google.gwt.user.client.Command) null)";
      creationSupport = new ImplicitFactoryCreationSupport(signature, source);
    }
    // create new MenuItem
    MenuItemInfo newItem =
        (MenuItemInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            "com.google.gwt.user.client.ui.MenuItem",
            creationSupport);
    assertTrue(policy.validateCreate(newItem));
    policy.commandCreate(newItem, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      bar.addItem('New item', false, (Command) null);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Adds new <code>MenuItem</code> with sub-menu.
   */
  public void test_IMenuPolicy_CREATE_MenuItem_newInstance_subMenu() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "  }",
        "}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    IMenuInfo barObject = MenuObjectInfoUtils.getMenuInfo(bar);
    IMenuPolicy policy = barObject.getPolicy();
    // don't accept something other than MenuItem
    assertFalse(policy.validateCreate(new Object()));
    // create new MenuItem
    MenuItemInfo newItem = createJavaInfo("com.google.gwt.user.client.ui.MenuItem", "withSubMenu");
    assertTrue(policy.validateCreate(newItem));
    policy.commandCreate(newItem, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuBar menuBar = new MenuBar(true);",
        "      MenuItem menuItem = new MenuItem('New menu', false, menuBar);",
        "      bar.addItem(menuItem);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(bar)/}",
        "  {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: bar} {/new MenuBar()/ /rootPanel.add(bar)/ /bar.addItem(menuItem)/}",
        "    {new: com.google.gwt.user.client.ui.MenuItem withSubMenu} {local-unique: menuItem} {/new MenuItem('New menu', false, menuBar)/ /bar.addItem(menuItem)/}",
        "      {new: com.google.gwt.user.client.ui.MenuBar vertical} {local-unique: menuBar} {/new MenuBar(true)/ /new MenuItem('New menu', false, menuBar)/}");
    {
      MenuBarInfo subMenu = newItem.getSubMenu();
      Association association = subMenu.getAssociation();
      assertInstanceOf(ConstructorChildAssociation.class, association);
      assertEquals("new MenuItem(\"New menu\", false, menuBar)", association.getSource());
      assertSame(subMenu, association.getJavaInfo());
    }
  }

  public void test_IMenuPolicy_PASTE_validateBad() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    MenuBar bar = new MenuBar();",
            "    rootPanel.add(bar);",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    refresh();
    MenuBarInfo bar = getJavaInfoByName("bar");
    WidgetInfo button = frame.getChildrenWidgets().get(1);
    // validate
    IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(bar).getPolicy();
    // Object can not be pasted (not list of memento at all)
    assertFalse(policy.validatePaste(new Object()));
    // RootPanel can not be pasted (even it memento)
    {
      JavaInfoMemento memento = JavaInfoMemento.createMemento(button);
      List<JavaInfoMemento> mementos = ImmutableList.of(memento);
      assertFalse(policy.validatePaste(mementos));
    }
  }

  public void test_IMenuPolicy_PASTE() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuItem existingItem = new MenuItem('My item', false, (Command) null);",
        "      bar.addItem(existingItem);",
        "    }",
        "  }",
        "}");
    refresh();
    MenuBarInfo bar = getJavaInfoByName("bar");
    // create new item
    List<JavaInfoMemento> mementos;
    {
      MenuItemInfo existingItem = bar.getItems().get(0);
      JavaInfoMemento memento = JavaInfoMemento.createMemento(existingItem);
      mementos = ImmutableList.of(memento);
    }
    // paste "newItem"
    {
      IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(bar).getPolicy();
      // validate
      assertTrue(policy.validatePaste(mementos));
      // do paste
      List<?> pastedObjects = policy.commandPaste(mementos, null);
      assertThat(pastedObjects).hasSize(1);
      assertThat(pastedObjects.get(0)).isInstanceOf(MenuItemInfo.class);
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuItem existingItem = new MenuItem('My item', false, (Command) null);",
        "      bar.addItem(existingItem);",
        "    }",
        "    {",
        "      MenuItem menuItem = new MenuItem('My item', false, (Command) null);",
        "      bar.addItem(menuItem);",
        "    }",
        "  }",
        "}");
  }

  public void test_IMenuPolicy_MOVE() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuBar subMenu_1 = new MenuBar(false);",
        "      bar.addItem('A', subMenu_1);",
        "    }",
        "    {",
        "      MenuBar subMenu_2 = new MenuBar(false);",
        "      bar.addItem('B', subMenu_2);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(bar)/}",
        "  {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: bar} {/new MenuBar()/ /rootPanel.add(bar)/ /bar.addItem('A', subMenu_1)/ /bar.addItem('B', subMenu_2)/}",
        "    {implicit-factory} {empty} {/bar.addItem('A', subMenu_1)/}",
        "      {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: subMenu_1} {/new MenuBar(false)/ /bar.addItem('A', subMenu_1)/}",
        "    {implicit-factory} {empty} {/bar.addItem('B', subMenu_2)/}",
        "      {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: subMenu_2} {/new MenuBar(false)/ /bar.addItem('B', subMenu_2)/}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    MenuItemInfo item_1 = bar.getItems().get(0);
    MenuItemInfo item_2 = bar.getItems().get(1);
    // can not move "bar" on "subMenu_1"
    {
      MenuBarInfo subMenu_1 = item_1.getSubMenu();
      IMenuInfo subMenuObject_1 = MenuObjectInfoUtils.getMenuInfo(subMenu_1);
      assertFalse(subMenuObject_1.getPolicy().validateMove(bar));
    }
    // move "item_2" before "item_1"
    {
      IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(bar).getPolicy();
      // validate
      {
        assertFalse(policy.validateMove(new Object()));
        assertTrue(policy.validateMove(item_2));
      }
      // do move
      policy.commandMove(item_2, item_1);
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuBar subMenu_2 = new MenuBar(false);",
        "      MenuItem menuItem = bar.addItem('B', subMenu_2);",
        "    }",
        "    {",
        "      MenuBar subMenu_1 = new MenuBar(false);",
        "      bar.addItem('A', subMenu_1);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(bar)/}",
        "  {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: bar} {/new MenuBar()/ /rootPanel.add(bar)/ /bar.addItem('A', subMenu_1)/ /bar.addItem('B', subMenu_2)/}",
        "    {implicit-factory} {local-unique: menuItem} {/bar.addItem('B', subMenu_2)/}",
        "      {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: subMenu_2} {/new MenuBar(false)/ /bar.addItem('B', subMenu_2)/}",
        "    {implicit-factory} {empty} {/bar.addItem('A', subMenu_1)/}",
        "      {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: subMenu_1} {/new MenuBar(false)/ /bar.addItem('A', subMenu_1)/}");
  }

  public void test_IMenuPolicy_ADD() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuBar subMenu = new MenuBar(false);",
        "      bar.addItem('A', subMenu);",
        "    }",
        "    {",
        "      MenuItem menuItem = new MenuItem('Some item', (Command) null);",
        "      bar.addItem(menuItem);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(bar)/}",
        "  {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: bar} {/new MenuBar()/ /rootPanel.add(bar)/ /bar.addItem('A', subMenu)/ /bar.addItem(menuItem)/}",
        "    {implicit-factory} {empty} {/bar.addItem('A', subMenu)/}",
        "      {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: subMenu} {/new MenuBar(false)/ /bar.addItem('A', subMenu)/}",
        "    {new: com.google.gwt.user.client.ui.MenuItem} {local-unique: menuItem} {/new MenuItem('Some item', (Command) null)/ /bar.addItem(menuItem)/}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    MenuItemInfo subMenuItem = bar.getItems().get(0);
    MenuItemInfo separateItem = bar.getItems().get(1);
    MenuBarInfo subMenu = subMenuItem.getSubMenu();
    // can reparent "separateItem" to "subMenu"
    IMenuInfo subMenuObject = MenuObjectInfoUtils.getMenuInfo(subMenu);
    assertTrue(subMenuObject.getPolicy().validateMove(separateItem));
    subMenuObject.getPolicy().commandMove(separateItem, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuBar subMenu = new MenuBar(false);",
        "      bar.addItem('A', subMenu);",
        "      {",
        "        MenuItem menuItem = new MenuItem('Some item', (Command) null);",
        "        subMenu.addItem(menuItem);",
        "      }",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(bar)/}",
        "  {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: bar} {/new MenuBar()/ /rootPanel.add(bar)/ /bar.addItem('A', subMenu)/}",
        "    {implicit-factory} {empty} {/bar.addItem('A', subMenu)/}",
        "      {new: com.google.gwt.user.client.ui.MenuBar} {local-unique: subMenu} {/new MenuBar(false)/ /bar.addItem('A', subMenu)/ /subMenu.addItem(menuItem)/}",
        "        {new: com.google.gwt.user.client.ui.MenuItem} {local-unique: menuItem} {/new MenuItem('Some item', (Command) null)/ /subMenu.addItem(menuItem)/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Open command
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Only one "Open Command" action should be in context menu of item.
   */
  public void test_MenuItem_openCommand_onlyOneInContextMenu() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuItem menuItem = new MenuItem('A', (Command) null);",
        "      bar.addItem(menuItem);",
        "    }",
        "    {",
        "      MenuItem menuItem = new MenuItem('B', (Command) null);",
        "      bar.addItem(menuItem);",
        "    }",
        "  }",
        "}");
    MenuBarInfo bar = getJavaInfoByName("bar");
    MenuItemInfo item = bar.getItems().get(0);
    //
    IMenuManager contextMenu = getContextMenu(item);
    List<IAction> actions = findChildActions(contextMenu, "Open Command");
    assertThat(actions).hasSize(1);
  }

  /**
   * Test for {@link MenuItemInfo#openCommand()}.
   * <p>
   * When <code>MenuItem</code> created using <code>MenuBar.addItem()</code>.
   */
  public void test_MenuItem_openCommand_addItem_useMethod() throws Exception {
    useStrictEvaluationMode(false);
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    MenuBar bar = new MenuBar();",
            "    rootPanel.add(bar);",
            "    {",
            "      MenuItem menuItem = bar.addItem('A', new Command() {",
            "        public void execute() {",
            "        }",
            "      });",
            "    }",
            "  }",
            "}");
    refresh();
    MenuBarInfo bar = getJavaInfoByName("bar");
    final MenuItemInfo item = bar.getItems().get(0);
    //
    {
      RunnableEx runnable = new RunnableEx() {
        public void run() throws Exception {
          item.openCommand();
        }
      };
      check_menuItem_openCommand0(frame, runnable);
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuItem menuItem = bar.addItem('A', new Command() {",
        "        public void execute() {",
        "        }",
        "      });",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link MenuItemInfo#openCommand()}.
   */
  public void test_MenuItem_openCommand_constructor_useMethod() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    MenuBar bar = new MenuBar();",
            "    rootPanel.add(bar);",
            "    {",
            "      MenuItem menuItem = new MenuItem('A', (Command) null);",
            "      bar.addItem(menuItem);",
            "    }",
            "  }",
            "}");
    refresh();
    MenuBarInfo bar = getJavaInfoByName("bar");
    final MenuItemInfo item = bar.getItems().get(0);
    //
    {
      RunnableEx runnable = new RunnableEx() {
        public void run() throws Exception {
          item.openCommand();
        }
      };
      check_menuItem_openCommand(frame, runnable);
    }
    // clipboard
    assertClipboardSource(
        item,
        "new com.google.gwt.user.client.ui.MenuItem(\"A\", (com.google.gwt.user.client.Command) null)");
  }

  /**
   * Test for "Open Command" action in context menu.
   */
  public void test_MenuItem_openCommand_constructor_useContextMenu() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    MenuBar bar = new MenuBar();",
            "    rootPanel.add(bar);",
            "    {",
            "      MenuItem menuItem = new MenuItem('A', (Command) null);",
            "      bar.addItem(menuItem);",
            "    }",
            "  }",
            "}");
    refresh();
    MenuBarInfo bar = getJavaInfoByName("bar");
    final MenuItemInfo item = bar.getItems().get(0);
    //
    RunnableEx runnable = new RunnableEx() {
      public void run() throws Exception {
        IAction action = findChildAction(getContextMenu(item), "Open Command");
        assertNotNull(action);
        action.run();
      }
    };
    check_menuItem_openCommand(frame, runnable);
  }

  private void check_menuItem_openCommand(JavaInfo rootJavaInfo, RunnableEx runnable)
      throws Exception {
    check_menuItem_openCommand0(rootJavaInfo, runnable);
    // check source
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuItem menuItem = new MenuItem('A', new Command() {",
        "        public void execute() {",
        "        }",
        "      });",
        "      bar.addItem(menuItem);",
        "    }",
        "  }",
        "}");
  }

  private void check_menuItem_openCommand0(JavaInfo rootJavaInfo, RunnableEx runnable)
      throws Exception {
    // set mock for DesignPageSite
    IDesignPageSite pageSite;
    Capture<Integer> openSourcePosition = new Capture<Integer>();
    {
      pageSite = EasyMock.createStrictMock(IDesignPageSite.class);
      pageSite.openSourcePosition(capture(openSourcePosition));
      EasyMock.replay(pageSite);
      // do set
      DesignPageSite.Helper.setSite(rootJavaInfo, pageSite);
    }
    // open Command
    runnable.run();
    waitEventLoop(0);
    EasyMock.verify(pageSite);
    // check captured position
    {
      assertTrue(openSourcePosition.hasCaptured());
      assertTrue(openSourcePosition.getValue() != 0);
      MethodDeclaration openMethod = m_lastEditor.getEnclosingMethod(openSourcePosition.getValue());
      assertEquals("execute", openMethod.getName().getIdentifier());
    }
  }

  /**
   * Test for {@link MenuItemInfo#openCommand()}.
   */
  public void test_MenuItem_openCommand_externalCommand() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyCommand.java",
        getTestSource(
            "public class MyCommand implements Command {",
            "  public void execute() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    MenuBar bar = new MenuBar();",
            "    rootPanel.add(bar);",
            "    {",
            "      MenuItem menuItem = new MenuItem('A', new MyCommand());",
            "      bar.addItem(menuItem);",
            "    }",
            "  }",
            "}");
    refresh();
    MenuBarInfo bar = getJavaInfoByName("bar");
    MenuItemInfo item = bar.getItems().get(0);
    // set mock for DesignPageSite
    IDesignPageSite pageSite;
    Capture<Integer> openSourcePosition = new Capture<Integer>();
    {
      pageSite = EasyMock.createStrictMock(IDesignPageSite.class);
      pageSite.openSourcePosition(capture(openSourcePosition));
      EasyMock.replay(pageSite);
      // do set
      DesignPageSite.Helper.setSite(frame, pageSite);
    }
    // open Command
    item.openCommand();
    waitEventLoop(0);
    EasyMock.verify(pageSite);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MenuBar bar = new MenuBar();",
        "    rootPanel.add(bar);",
        "    {",
        "      MenuItem menuItem = new MenuItem('A', new MyCommand());",
        "      bar.addItem(menuItem);",
        "    }",
        "  }",
        "}");
    // check captured position
    {
      assertTrue(openSourcePosition.hasCaptured());
      assertTrue(openSourcePosition.getValue() != 0);
      ASTNode enclosingNode = m_lastEditor.getEnclosingNode(openSourcePosition.getValue());
      assertEquals("new MyCommand()", m_lastEditor.getSource(enclosingNode));
    }
  }
}