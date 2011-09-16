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

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.TreeInfo;
import com.google.gdt.eclipse.designer.model.widgets.TreeItemInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.core.model.association.ImplicitFactoryArgumentAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link TreeInfo} and {@link TreeItemInfo}.
 * 
 * @author scheglov_ke
 */
public class TreeTest extends GwtModelTest {
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
   * Even empty <code>Tree</code> should have some reasonable size.
   */
  public void test_parse_empty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Tree tree = new Tree();",
            "      rootPanel.add(tree);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    TreeInfo tree = (TreeInfo) frame.getChildrenWidgets().get(0);
    Rectangle bounds = tree.getBounds();
    assertThat(bounds.width).isGreaterThan(50);
    assertThat(bounds.height).isGreaterThan(20);
  }

  /**
   * <code>Tree</code> does not like <code>null</code> as <code>TreeImages</code>.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44201
   */
  public void test_parse_nullImages() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Tree tree = new Tree((TreeImages) null);",
            "      rootPanel.add(tree);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // addItem() variants parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_parse_addItem_asItem() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Tree tree = new Tree();",
            "      rootPanel.add(tree);",
            "      {",
            "        TreeItem item = new TreeItem('Item text');",
            "        tree.addItem(item);",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // check hierarchy
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(tree)/}",
        "  {new: com.google.gwt.user.client.ui.Tree} {local-unique: tree} {/new Tree()/ /rootPanel.add(tree)/ /tree.addItem(item)/}",
        "    {new: com.google.gwt.user.client.ui.TreeItem} {local-unique: item} {/new TreeItem('Item text')/ /tree.addItem(item)/}");
  }

  public void test_parse_addItem_asString() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Tree tree = new Tree();",
            "      rootPanel.add(tree);",
            "      {",
            "        TreeItem item = tree.addItem('Item text');",
            "        item.setState(true);",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // check hierarchy
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(tree)/}",
        "  {new: com.google.gwt.user.client.ui.Tree} {local-unique: tree} {/new Tree()/ /rootPanel.add(tree)/ /tree.addItem('Item text')/}",
        "    {implicit-factory} {local-unique: item} {/tree.addItem('Item text')/ /item.setState(true)/}");
  }

  public void test_parse_addItem_asWidget() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Tree tree = new Tree();",
            "      rootPanel.add(tree);",
            "      {",
            "        Button button = new Button('Button text');",
            "        TreeItem item = tree.addItem(button);",
            "        item.setState(true);",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // check hierarchy
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(tree)/}",
        "  {new: com.google.gwt.user.client.ui.Tree} {local-unique: tree} {/new Tree()/ /rootPanel.add(tree)/ /tree.addItem(button)/}",
        "    {implicit-factory} {local-unique: item} {/tree.addItem(button)/ /item.setState(true)/}",
        "      {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button('Button text')/ /tree.addItem(button)/}");
    TreeInfo tree = (TreeInfo) frame.getChildrenWidgets().get(0);
    TreeItemInfo treeItem = tree.getItems().get(0);
    WidgetInfo button = treeItem.getWidget();
    // check association type, but not operations (see test for this association)
    assertInstanceOf(ImplicitFactoryArgumentAssociation.class, button.getAssociation());
    assertTrue(button.canDelete());
    assertFalse(JavaInfoUtils.canMove(button));
    assertTrue(JavaInfoUtils.canReparent(button));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for adding {@link TreeItemInfo}.
   */
  public void test_tree_CREATE_TreeItem() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Tree tree = new Tree();",
            "      rootPanel.add(tree);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    TreeInfo tree = (TreeInfo) frame.getChildrenWidgets().get(0);
    // do CREATE
    TreeItemInfo newItem = createJavaInfo("com.google.gwt.user.client.ui.TreeItem");
    flowContainer_CREATE(tree, newItem, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "      {",
        "        TreeItem treeItem = new TreeItem('New item');",
        "        tree.addItem(treeItem);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for adding {@link TreeItemInfo} based on {@link WidgetInfo}.
   */
  public void test_tree_CREATE_Widget() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Tree tree = new Tree();",
            "      rootPanel.add(tree);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    TreeInfo tree = (TreeInfo) frame.getChildrenWidgets().get(0);
    // do CREATE
    WidgetInfo newButton = createJavaInfo("com.google.gwt.user.client.ui.Button");
    flowContainer_CREATE(tree, newButton, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "      {",
        "        TreeItem treeItem = tree.addItem(new Button('New button'));",
        "      }",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(tree)/}",
        "  {new: com.google.gwt.user.client.ui.Tree} {local-unique: tree} {/new Tree()/ /rootPanel.add(tree)/ /tree.addItem(new Button('New button'))/}",
        "    {implicit-factory} {local-unique: treeItem} {/tree.addItem(new Button('New button'))/}",
        "      {new: com.google.gwt.user.client.ui.Button} {empty} {/tree.addItem(new Button('New button'))/}");
  }

  /**
   * Test for {@link TreeInfo#command_MOVE2(TreeItemInfo, TreeItemInfo)}.
   */
  public void test_tree_MOVE_reparent() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Tree tree_1 = new Tree();",
            "      rootPanel.add(tree_1);",
            "      {",
            "        TreeItem treeItem = new TreeItem();",
            "        tree_1.addItem(treeItem);",
            "      }",
            "    }",
            "    {",
            "      Tree tree_2 = new Tree();",
            "      rootPanel.add(tree_2);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    TreeInfo tree_1 = (TreeInfo) frame.getChildrenWidgets().get(0);
    TreeInfo tree_2 = (TreeInfo) frame.getChildrenWidgets().get(1);
    // do MOVE
    TreeItemInfo item = tree_1.getItems().get(0);
    flowContainer_MOVE(tree_2, item, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree_1 = new Tree();",
        "      rootPanel.add(tree_1);",
        "    }",
        "    {",
        "      Tree tree_2 = new Tree();",
        "      rootPanel.add(tree_2);",
        "      {",
        "        TreeItem treeItem = new TreeItem();",
        "        tree_2.addItem(treeItem);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Item
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invocation <code>TreeItem.setState()</code> should be added after all children items.
   */
  public void test_itemState() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Tree tree = new Tree();",
            "      rootPanel.add(tree);",
            "      {",
            "        TreeItem item_0 = new TreeItem('000');",
            "        tree.addItem(item_0);",
            "        {",
            "          TreeItem item_1 = new TreeItem('111');",
            "          item_0.addItem(item_1);",
            "        }",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    TreeInfo tree = (TreeInfo) frame.getChildrenWidgets().get(0);
    TreeItemInfo item_0 = tree.getItems().get(0);
    // change "state" manually
    item_0.getPropertyByTitle("state").setValue(true);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "      {",
        "        TreeItem item_0 = new TreeItem('000');",
        "        tree.addItem(item_0);",
        "        {",
        "          TreeItem item_1 = new TreeItem('111');",
        "          item_0.addItem(item_1);",
        "        }",
        "        item_0.setState(true);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TreeItem commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for adding {@link TreeItemInfo}.
   */
  public void test_treeItem_CREATE_TreeItem() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Tree tree = new Tree();",
            "      rootPanel.add(tree);",
            "      {",
            "        TreeItem existingItem = new TreeItem();",
            "        tree.addItem(existingItem);",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    TreeInfo tree = (TreeInfo) frame.getChildrenWidgets().get(0);
    TreeItemInfo existingItem = tree.getItems().get(0);
    // do CREATE
    TreeItemInfo item = createJavaInfo("com.google.gwt.user.client.ui.TreeItem");
    flowContainer_CREATE(existingItem, item, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "      {",
        "        TreeItem existingItem = new TreeItem();",
        "        tree.addItem(existingItem);",
        "        {",
        "          TreeItem treeItem = new TreeItem('New item');",
        "          existingItem.addItem(treeItem);",
        "        }",
        "        existingItem.setState(true);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for adding {@link TreeItemInfo} based on {@link WidgetInfo}.
   */
  public void test_treeItem_CREATE_Widget() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Tree tree = new Tree();",
            "      rootPanel.add(tree);",
            "      {",
            "        TreeItem existingItem = new TreeItem();",
            "        tree.addItem(existingItem);",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    TreeInfo tree = (TreeInfo) frame.getChildrenWidgets().get(0);
    TreeItemInfo existingItem = tree.getItems().get(0);
    // do CREATE
    WidgetInfo newButton = createJavaInfo("com.google.gwt.user.client.ui.Button");
    flowContainer_CREATE(existingItem, newButton, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "      {",
        "        TreeItem existingItem = new TreeItem();",
        "        tree.addItem(existingItem);",
        "        {",
        "          TreeItem treeItem = existingItem.addItem(new Button('New button'));",
        "        }",
        "        existingItem.setState(true);",
        "      }",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(tree)/}",
        "  {new: com.google.gwt.user.client.ui.Tree} {local-unique: tree} {/new Tree()/ /rootPanel.add(tree)/ /tree.addItem(existingItem)/}",
        "    {new: com.google.gwt.user.client.ui.TreeItem} {local-unique: existingItem} {/new TreeItem()/ /tree.addItem(existingItem)/ /existingItem.addItem(new Button('New button'))/ /existingItem.setState(true)/}",
        "      {implicit-factory} {local-unique: treeItem} {/existingItem.addItem(new Button('New button'))/}",
        "        {new: com.google.gwt.user.client.ui.Button} {empty} {/existingItem.addItem(new Button('New button'))/}");
  }

  /**
   * Test for {@link TreeItemInfo#command_MOVE2(TreeItemInfo, TreeItemInfo)}.
   */
  public void test_treeItem_MOVE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Tree tree = new Tree();",
            "      rootPanel.add(tree);",
            "      {",
            "        TreeItem item_1 = new TreeItem();",
            "        tree.addItem(item_1);",
            "      }",
            "      {",
            "        TreeItem item_2 = new TreeItem();",
            "        tree.addItem(item_2);",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // prepare widgets
    TreeInfo tree = (TreeInfo) frame.getChildrenWidgets().get(0);
    TreeItemInfo item_1 = tree.getItems().get(0);
    TreeItemInfo item_2 = tree.getItems().get(1);
    assertThat(item_1.getItems()).isEmpty();
    // do MOVE
    flowContainer_MOVE(item_1, item_2, null);
    assertThat(item_1.getItems()).containsOnly(item_2);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "      {",
        "        TreeItem item_1 = new TreeItem();",
        "        tree.addItem(item_1);",
        "        {",
        "          TreeItem item_2 = new TreeItem();",
        "          item_1.addItem(item_2);",
        "        }",
        "        item_1.setState(true);",
        "      }",
        "    }",
        "  }",
        "}");
  }
}