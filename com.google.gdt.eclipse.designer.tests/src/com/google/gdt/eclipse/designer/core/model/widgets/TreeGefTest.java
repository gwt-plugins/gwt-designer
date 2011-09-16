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
import com.google.gdt.eclipse.designer.model.widgets.TreeInfo;
import com.google.gdt.eclipse.designer.model.widgets.TreeItemInfo;

/**
 * Test for {@link TreeInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class TreeGefTest extends GwtGefTest {
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
  // Tree operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE_TreeItem() throws Exception {
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "    }",
        "  }",
        "}");
    TreeInfo treeWidget = getJavaInfoByName("tree");
    //
    loadCreationTool("com.google.gwt.user.client.ui.TreeItem");
    tree.moveOn(treeWidget).click();
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
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(tree)/}",
        "  {new: com.google.gwt.user.client.ui.Tree} {local-unique: tree} {/new Tree()/ /rootPanel.add(tree)/ /tree.addItem(treeItem)/}",
        "    {new: com.google.gwt.user.client.ui.TreeItem} {local-unique: treeItem} {/new TreeItem('New item')/ /tree.addItem(treeItem)/}");
  }

  public void test_tree_CREATE_Widget() throws Exception {
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "    }",
        "  }",
        "}");
    TreeInfo treeWidget = getJavaInfoByName("tree");
    //
    loadCreationTool("com.google.gwt.user.client.ui.Button");
    tree.moveOn(treeWidget).click();
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

  public void test_tree_MOVE_TreeItem() throws Exception {
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "      {",
        "        TreeItem treeItem_1 = new TreeItem('Item 1');",
        "        tree.addItem(treeItem_1);",
        "      }",
        "      {",
        "        TreeItem treeItem_2 = new TreeItem('Item 2');",
        "        tree.addItem(treeItem_2);",
        "      }",
        "    }",
        "  }",
        "}");
    TreeItemInfo item_1 = getJavaInfoByName("treeItem_1");
    TreeItemInfo item_2 = getJavaInfoByName("treeItem_2");
    //
    tree.startDrag(item_2);
    tree.dragBefore(item_1).endDrag();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "      {",
        "        TreeItem treeItem_2 = new TreeItem('Item 2');",
        "        tree.addItem(treeItem_2);",
        "      }",
        "      {",
        "        TreeItem treeItem_1 = new TreeItem('Item 1');",
        "        tree.addItem(treeItem_1);",
        "      }",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(tree)/}",
        "  {new: com.google.gwt.user.client.ui.Tree} {local-unique: tree} {/new Tree()/ /rootPanel.add(tree)/ /tree.addItem(treeItem_1)/ /tree.addItem(treeItem_2)/}",
        "    {new: com.google.gwt.user.client.ui.TreeItem} {local-unique: treeItem_2} {/new TreeItem('Item 2')/ /tree.addItem(treeItem_2)/}",
        "    {new: com.google.gwt.user.client.ui.TreeItem} {local-unique: treeItem_1} {/new TreeItem('Item 1')/ /tree.addItem(treeItem_1)/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TreeItem operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_item_CREATE_TreeItem() throws Exception {
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "      {",
        "        TreeItem existingItem = new TreeItem('Existing item');",
        "        tree.addItem(existingItem);",
        "      }",
        "    }",
        "  }",
        "}");
    TreeItemInfo existingItem = getJavaInfoByName("existingItem");
    //
    loadCreationTool("com.google.gwt.user.client.ui.TreeItem");
    tree.moveOn(existingItem).click();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "      {",
        "        TreeItem existingItem = new TreeItem('Existing item');",
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
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(tree)/}",
        "  {new: com.google.gwt.user.client.ui.Tree} {local-unique: tree} {/new Tree()/ /rootPanel.add(tree)/ /tree.addItem(existingItem)/}",
        "    {new: com.google.gwt.user.client.ui.TreeItem} {local-unique: existingItem} {/new TreeItem('Existing item')/ /tree.addItem(existingItem)/ /existingItem.addItem(treeItem)/ /existingItem.setState(true)/}",
        "      {new: com.google.gwt.user.client.ui.TreeItem} {local-unique: treeItem} {/new TreeItem('New item')/ /existingItem.addItem(treeItem)/}");
  }

  public void test_item_CREATE_Widget() throws Exception {
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "      {",
        "        TreeItem existingItem = new TreeItem('Existing item');",
        "        tree.addItem(existingItem);",
        "      }",
        "    }",
        "  }",
        "}");
    TreeItemInfo existingItem = getJavaInfoByName("existingItem");
    //
    loadCreationTool("com.google.gwt.user.client.ui.Button");
    tree.moveOn(existingItem).click();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "      {",
        "        TreeItem existingItem = new TreeItem('Existing item');",
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
        "    {new: com.google.gwt.user.client.ui.TreeItem} {local-unique: existingItem} {/new TreeItem('Existing item')/ /tree.addItem(existingItem)/ /existingItem.addItem(new Button('New button'))/ /existingItem.setState(true)/}",
        "      {implicit-factory} {local-unique: treeItem} {/existingItem.addItem(new Button('New button'))/}",
        "        {new: com.google.gwt.user.client.ui.Button} {empty} {/existingItem.addItem(new Button('New button'))/}");
  }

  public void test_item_MOVE_TreeItem() throws Exception {
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "      {",
        "        TreeItem existingItem = new TreeItem('Existing item');",
        "        tree.addItem(existingItem);",
        "        {",
        "          TreeItem item_1 = new TreeItem('Item 1');",
        "          existingItem.addItem(item_1);",
        "        }",
        "        {",
        "          TreeItem item_2 = new TreeItem('Item 2');",
        "          existingItem.addItem(item_2);",
        "        }",
        "        existingItem.setState(true);",
        "      }",
        "    }",
        "  }",
        "}");
    TreeItemInfo item_1 = getJavaInfoByName("item_1");
    TreeItemInfo item_2 = getJavaInfoByName("item_2");
    //
    tree.startDrag(item_2);
    tree.dragBefore(item_1).endDrag();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Tree tree = new Tree();",
        "      rootPanel.add(tree);",
        "      {",
        "        TreeItem existingItem = new TreeItem('Existing item');",
        "        tree.addItem(existingItem);",
        "        {",
        "          TreeItem item_2 = new TreeItem('Item 2');",
        "          existingItem.addItem(item_2);",
        "        }",
        "        {",
        "          TreeItem item_1 = new TreeItem('Item 1');",
        "          existingItem.addItem(item_1);",
        "        }",
        "        existingItem.setState(true);",
        "      }",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(tree)/}",
        "  {new: com.google.gwt.user.client.ui.Tree} {local-unique: tree} {/new Tree()/ /rootPanel.add(tree)/ /tree.addItem(existingItem)/}",
        "    {new: com.google.gwt.user.client.ui.TreeItem} {local-unique: existingItem} {/new TreeItem('Existing item')/ /tree.addItem(existingItem)/ /existingItem.addItem(item_1)/ /existingItem.addItem(item_2)/ /existingItem.setState(true)/}",
        "      {new: com.google.gwt.user.client.ui.TreeItem} {local-unique: item_2} {/new TreeItem('Item 2')/ /existingItem.addItem(item_2)/}",
        "      {new: com.google.gwt.user.client.ui.TreeItem} {local-unique: item_1} {/new TreeItem('Item 1')/ /existingItem.addItem(item_1)/}");
  }
}
