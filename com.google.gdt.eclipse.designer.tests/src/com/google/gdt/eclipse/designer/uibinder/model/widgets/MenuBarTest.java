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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.GwtToolkitDescription;
import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.menu.MenuBarInfo;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.menu.MenuItemInfo;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.menu.MenuItemSeparatorInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.description.FieldInitializerVariableDescription;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link MenuBarInfo} and {@link MenuItemInfo}.
 * 
 * @author scheglov_ke
 */
public class MenuBarTest extends UiBinderModelTest {
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
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    MenuBarInfo bar = getObjectByName("bar");
    //
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
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar' animationEnabled='true'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    MenuBarInfo bar = getObjectByName("bar");
    //
    assertEquals(false, ReflectionUtils.invokeMethod(bar.getObject(), "isAnimationEnabled()"));
  }

  public void test_parse_MenuItem() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem wbp:name='item' text='A'/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:MenuBar wbp:name='bar'>",
        "    <g:MenuItem wbp:name='item' text='A'>");
    refresh();
    MenuBarInfo bar = getObjectByName("bar");
    MenuItemInfo item = getObjectByName("item");
    // only item in "bar"
    assertThat(bar.getAllItems()).containsOnly(item);
    // check "text" property
    Property textProperty = item.getPropertyByTitle("text");
    assertEquals("A", textProperty.getValue());
    textProperty.setValue("B");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem wbp:name='item' text='B'/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_parse_MenuItem_subMenu() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem wbp:name='item' text='Sub'>",
        "        <g:MenuBar wbp:name='subMenu'/>",
        "      </g:MenuItem>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:MenuBar wbp:name='bar'>",
        "    <g:MenuItem wbp:name='item' text='Sub'>",
        "      <g:MenuBar wbp:name='subMenu'>");
    refresh();
    MenuItemInfo item = getObjectByName("item");
    MenuBarInfo subMenu = getObjectByName("subMenu");
    assertSame(subMenu, item.getSubMenu());
  }

  public void test_parse_addSeparator_usingConstructor() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItemSeparator wbp:name='separator'/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:MenuBar wbp:name='bar'>",
        "    <g:MenuItemSeparator wbp:name='separator'>");
    refresh();
    MenuBarInfo bar = getObjectByName("bar");
    MenuItemSeparatorInfo separator = getObjectByName("separator");
    // only separator in "bar"
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sub menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_IMenuInfo_IMenuItemInfo() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem text='Sub'>",
        "        <g:MenuBar vertical='true'>",
        "          <g:MenuItem text='Sub item 1'/>",
        "          <g:MenuItem text='Sub item 2'/>",
        "        </g:MenuBar>",
        "      </g:MenuItem>",
        "      <g:MenuItem text='Separate item'/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "<g:FlowPanel>",
        "  <g:MenuBar wbp:name='bar'>",
        "    <g:MenuItem text='Sub'>",
        "      <g:MenuBar vertical='true'>",
        "        <g:MenuItem text='Sub item 1'>",
        "        <g:MenuItem text='Sub item 2'>",
        "    <g:MenuItem text='Separate item'>");
    refresh();
    MenuBarInfo menu = getObjectByName("bar");
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
          assertThat(bounds.x).isEqualTo(0);
          assertThat(bounds.y).isEqualTo(0);
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
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem wbp:name='item' text='Sub'>",
        "        <g:MenuBar wbp:name='subMenu'/>",
        "      </g:MenuItem>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:MenuBar wbp:name='bar'>",
        "    <g:MenuItem wbp:name='item' text='Sub'>",
        "      <g:MenuBar wbp:name='subMenu'>");
    refresh();
    MenuBarInfo subMenu = getObjectByName("subMenu");
    // delete "subMenu", fortunately "item" can exists without it, so keep it
    subMenu.delete();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem wbp:name='item' text='Sub'/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:MenuBar wbp:name='bar'>",
        "    <g:MenuItem wbp:name='item' text='Sub'>");
  }

  /**
   * Delete menu bar with sub menu.
   */
  public void test_delete_withSubMenu() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem wbp:name='item' text='Sub'>",
        "        <g:MenuBar wbp:name='subMenu'/>",
        "      </g:MenuItem>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    MenuBarInfo bar = getObjectByName("bar");
    // delete "bar" fully
    bar.delete();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel/>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuPolicy: separator
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new <code>MenuItemSeparator</code>.
   */
  public void test_IMenuPolicy_CREATE_MenuItemSeparator() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    MenuBarInfo bar = getObjectByName("bar");
    IMenuInfo barObject = MenuObjectInfoUtils.getMenuInfo(bar);
    IMenuPolicy barPolicy = barObject.getPolicy();
    // don't accept something other than MenuItem or MenuItemSeparator
    assertFalse(barPolicy.validateCreate(new Object()));
    // create new MenuItemSeparator
    MenuItemSeparatorInfo newSeparator =
        createObject("com.google.gwt.user.client.ui.MenuItemSeparator");
    assertTrue(barPolicy.validateCreate(newSeparator));
    barPolicy.commandCreate(newSeparator, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItemSeparator/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_IMenuPolicy_MOVE_MenuItemSeparator() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItemSeparator wbp:name='separator'/>",
        "      <g:MenuItem/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    MenuBarInfo bar = getObjectByName("bar");
    MenuItemSeparatorInfo separator = getObjectByName("separator");
    IMenuInfo barObject = MenuObjectInfoUtils.getMenuInfo(bar);
    IMenuPolicy barPolicy = barObject.getPolicy();
    // don't accept something other than MenuItem or MenuItemSeparator
    assertFalse(barPolicy.validateMove(new Object()));
    assertTrue(barPolicy.validateMove(separator));
    barPolicy.commandMove(separator, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem/>",
        "      <g:MenuItemSeparator wbp:name='separator'/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuPolicy: item
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new <code>MenuItem</code>.
   */
  public void test_IMenuPolicy_CREATE_MenuItem() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    MenuBarInfo bar = getObjectByName("bar");
    IMenuInfo barObject = MenuObjectInfoUtils.getMenuInfo(bar);
    IMenuPolicy policy = barObject.getPolicy();
    // don't accept something other than MenuItem
    assertFalse(policy.validateCreate(new Object()));
    // create new MenuItem
    MenuItemInfo newItem = createObject("com.google.gwt.user.client.ui.MenuItem");
    assertTrue(policy.validateCreate(newItem));
    policy.commandCreate(newItem, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem text='New item'/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Adds new <code>MenuItem</code> with sub-menu.
   */
  public void test_IMenuPolicy_CREATE_MenuItem_subMenu() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    MenuBarInfo bar = getObjectByName("bar");
    IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(bar).getPolicy();
    // use "field initializer"
    {
      GenerationSettings generationSettings =
          GwtToolkitDescription.INSTANCE.getGenerationSettings();
      generationSettings.setVariable(FieldInitializerVariableDescription.INSTANCE);
    }
    // create new MenuItem
    MenuItemInfo newItem = createObject("com.google.gwt.user.client.ui.MenuItem", "withSubMenu");
    assertTrue(policy.validateCreate(newItem));
    policy.commandCreate(newItem, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem text='New menu'>",
        "        <g:MenuBar vertical='true'/>",
        "      </g:MenuItem>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:MenuBar wbp:name='bar'>",
        "    <g:MenuItem text='New menu'>",
        "      <g:MenuBar vertical='true'>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Paste
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_IMenuPolicy_PASTE_validateBad() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'/>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    MenuBarInfo bar = getObjectByName("bar");
    WidgetInfo button = getObjectByName("button");
    // validate
    IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(bar).getPolicy();
    // Object can not be pasted (not list of memento at all)
    assertFalse(policy.validatePaste(new Object()));
    // RootPanel can not be pasted (even it memento)
    {
      XmlObjectMemento memento = XmlObjectMemento.createMemento(button);
      List<XmlObjectMemento> mementos = ImmutableList.of(memento);
      assertFalse(policy.validatePaste(mementos));
    }
  }

  public void test_IMenuPolicy_PASTE() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem wbp:name='item' text='My item'/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    MenuBarInfo bar = getObjectByName("bar");
    // prepare memento
    List<XmlObjectMemento> mementos;
    {
      MenuItemInfo existingItem = getObjectByName("item");
      XmlObjectMemento memento = XmlObjectMemento.createMemento(existingItem);
      mementos = ImmutableList.of(memento);
    }
    // paste new "Item"
    {
      IMenuPolicy policy = MenuObjectInfoUtils.getMenuInfo(bar).getPolicy();
      // validate
      assertTrue(policy.validatePaste(mementos));
      // do paste
      List<?> pastedObjects = policy.commandPaste(mementos, null);
      assertThat(pastedObjects).hasSize(1);
      assertThat(pastedObjects.get(0)).isInstanceOf(MenuItemInfo.class);
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem wbp:name='item' text='My item'/>",
        "      <g:MenuItem text='My item'/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_IMenuPolicy_MOVE() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem wbp:name='item_1' text='A'>",
        "        <g:MenuBar wbp:name='subMenu_1'/>",
        "      </g:MenuItem>",
        "      <g:MenuItem wbp:name='item_2' text='B'>",
        "        <g:MenuBar wbp:name='subMenu_2'/>",
        "      </g:MenuItem>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:MenuBar wbp:name='bar'>",
        "    <g:MenuItem wbp:name='item_1' text='A'>",
        "      <g:MenuBar wbp:name='subMenu_1'>",
        "    <g:MenuItem wbp:name='item_2' text='B'>",
        "      <g:MenuBar wbp:name='subMenu_2'>");
    MenuBarInfo bar = getObjectByName("bar");
    MenuItemInfo item_1 = getObjectByName("item_1");
    MenuItemInfo item_2 = getObjectByName("item_2");
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
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem wbp:name='item_2' text='B'>",
        "        <g:MenuBar wbp:name='subMenu_2'/>",
        "      </g:MenuItem>",
        "      <g:MenuItem wbp:name='item_1' text='A'>",
        "        <g:MenuBar wbp:name='subMenu_1'/>",
        "      </g:MenuItem>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:MenuBar wbp:name='bar'>",
        "    <g:MenuItem wbp:name='item_2' text='B'>",
        "      <g:MenuBar wbp:name='subMenu_2'>",
        "    <g:MenuItem wbp:name='item_1' text='A'>",
        "      <g:MenuBar wbp:name='subMenu_1'>");
  }

  public void test_IMenuPolicy_ADD() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem wbp:name='subMenuItem' text='A'>",
        "        <g:MenuBar wbp:name='subMenu'/>",
        "      </g:MenuItem>",
        "      <g:MenuItem wbp:name='separateItem' text='B'/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:MenuBar wbp:name='bar'>",
        "    <g:MenuItem wbp:name='subMenuItem' text='A'>",
        "      <g:MenuBar wbp:name='subMenu'>",
        "    <g:MenuItem wbp:name='separateItem' text='B'>");
    MenuBarInfo subMenu = getObjectByName("subMenu");
    MenuItemInfo separateItem = getObjectByName("separateItem");
    // can reparent "separateItem" to "subMenu"
    IMenuInfo subMenuObject = MenuObjectInfoUtils.getMenuInfo(subMenu);
    assertTrue(subMenuObject.getPolicy().validateMove(separateItem));
    subMenuObject.getPolicy().commandMove(separateItem, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "      <g:MenuItem wbp:name='subMenuItem' text='A'>",
        "        <g:MenuBar wbp:name='subMenu'>",
        "          <g:MenuItem wbp:name='separateItem' text='B'/>",
        "        </g:MenuBar>",
        "      </g:MenuItem>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:MenuBar wbp:name='bar'>",
        "    <g:MenuItem wbp:name='subMenuItem' text='A'>",
        "      <g:MenuBar wbp:name='subMenu'>",
        "        <g:MenuItem wbp:name='separateItem' text='B'>");
  }
}