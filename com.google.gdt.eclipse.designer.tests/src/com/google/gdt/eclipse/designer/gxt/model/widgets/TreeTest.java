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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for <code>Tree</code>.
 * 
 * @author scheglov_ke
 */
public class TreeTest extends GxtModelTest {
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
  public void test_parse_getRootItem() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "import com.extjs.gxt.ui.client.widget.tree.*;",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      Tree tree = new Tree();",
            "      add(tree);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(tree)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.tree.Tree} {local-unique: tree} {/new Tree()/ /add(tree)/}",
        "    {method: public com.extjs.gxt.ui.client.widget.tree.TreeItem com.extjs.gxt.ui.client.widget.tree.Tree.getRootItem()} {property} {}");
    TreeInfo tree = (TreeInfo) panel.getWidgets().get(0);
    // check non-trivial properties
    assertNotNull(tree.getPropertyByTitle("checkNodes"));
    assertNotNull(tree.getPropertyByTitle("checkStyle"));
  }

  public void test_TreeItem_CREATE() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "import com.extjs.gxt.ui.client.widget.tree.*;",
            "public class Test extends HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      Tree tree = new Tree();",
            "      add(tree);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TreeInfo tree = (TreeInfo) panel.getWidgets().get(0);
    TreeItemInfo rootItem = tree.getRootItem();
    // add TreeItem
    TreeItemInfo newItem_1;
    {
      newItem_1 = createJavaInfo("com.extjs.gxt.ui.client.widget.tree.TreeItem");
      doTreeItem_CREATE(rootItem, newItem_1);
      assertEditor(
          "import com.extjs.gxt.ui.client.widget.tree.*;",
          "public class Test extends HorizontalPanel {",
          "  public Test() {",
          "    {",
          "      Tree tree = new Tree();",
          "      {",
          "        TreeItem treeItem = new TreeItem('New TreeItem');",
          "        tree.getRootItem().add(treeItem);",
          "      }",
          "      add(tree);",
          "    }",
          "  }",
          "}");
    }
    // add second level TreeItem
    {
      TreeItemInfo newItem_2 = createJavaInfo("com.extjs.gxt.ui.client.widget.tree.TreeItem");
      doTreeItem_CREATE(newItem_1, newItem_2);
      assertEditor(
          "import com.extjs.gxt.ui.client.widget.tree.*;",
          "public class Test extends HorizontalPanel {",
          "  public Test() {",
          "    {",
          "      Tree tree = new Tree();",
          "      {",
          "        TreeItem treeItem = new TreeItem('New TreeItem');",
          "        treeItem.setExpanded(true);",
          "        tree.getRootItem().add(treeItem);",
          "        {",
          "          TreeItem treeItem_1 = new TreeItem('New TreeItem');",
          "          treeItem.add(treeItem_1);",
          "        }",
          "      }",
          "      add(tree);",
          "    }",
          "  }",
          "}");
    }
  }

  public void test_TreeItem_MOVE() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "import com.extjs.gxt.ui.client.widget.tree.*;",
            "public class Test extends HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      Tree tree = new Tree();",
            "      add(tree);",
            "      {",
            "        TreeItem item_1 = new TreeItem();",
            "        tree.getRootItem().add(item_1);",
            "        {",
            "          TreeItem item_1_1 = new TreeItem();",
            "          item_1.add(item_1_1);",
            "        }",
            "      }",
            "      {",
            "        TreeItem item_2 = new TreeItem();",
            "        tree.getRootItem().add(item_2);",
            "      }",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TreeInfo tree = (TreeInfo) panel.getWidgets().get(0);
    TreeItemInfo rootItem = tree.getRootItem();
    TreeItemInfo item_1 = rootItem.getItems().get(0);
    TreeItemInfo item_1_1 = item_1.getItems().get(0);
    TreeItemInfo item_2 = rootItem.getItems().get(1);
    // do move
    doTreeItem_MOVE(item_2, item_1_1, null);
    assertEditor(
        "import com.extjs.gxt.ui.client.widget.tree.*;",
        "public class Test extends HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      Tree tree = new Tree();",
        "      add(tree);",
        "      {",
        "        TreeItem item_1 = new TreeItem();",
        "        tree.getRootItem().add(item_1);",
        "      }",
        "      {",
        "        TreeItem item_2 = new TreeItem();",
        "        item_2.setExpanded(true);",
        "        tree.getRootItem().add(item_2);",
        "        {",
        "          TreeItem item_1_1 = new TreeItem();",
        "          item_2.add(item_1_1);",
        "        }",
        "      }",
        "    }",
        "  }",
        "}");
  }

  private static void doTreeItem_CREATE(TreeItemInfo parent, TreeItemInfo child) throws Exception {
    FlowContainer flowContainer = getFlowContainer(parent);
    assertTrue(flowContainer.validateComponent(child));
    flowContainer.command_CREATE(child, null);
  }

  private static void doTreeItem_MOVE(TreeItemInfo parent, TreeItemInfo child, TreeItemInfo nextItem)
      throws Exception {
    FlowContainer flowContainer = getFlowContainer(parent);
    assertTrue(flowContainer.validateComponent(child));
    flowContainer.command_MOVE(child, nextItem);
  }

  private static FlowContainer getFlowContainer(TreeItemInfo parent) {
    List<FlowContainer> containers = new FlowContainerFactory(parent, true).get();
    assertThat(containers).hasSize(1);
    return containers.get(0);
  }
}