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
package com.google.gdt.eclipse.designer.gwtext;

import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.TreeNodeInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.TreePanelInfo;

import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;

/**
 * Tests for {@link TreePanelInfo}.
 * 
 * @author scheglov_ke
 */
public class TreePanelTest extends GwtExtModelTest {
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
  public void test_parseRootNode() throws Exception {
    TreePanelInfo treePanel =
        parseJavaInfo(
            "public class Test extends TreePanel {",
            "  public Test() {",
            "    setRootNode(new TreeNode('(root)'));",
            "  }",
            "}");
    treePanel.refresh();
    assertNoErrors(treePanel);
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.tree.TreePanel} {this} {/setRootNode(new TreeNode('(root)'))/}",
        "  {new: com.gwtext.client.widgets.tree.TreeNode} {empty} {/setRootNode(new TreeNode('(root)'))/}");
    assertFalse(treePanel.hasLayout());
  }

  public void test_parseTreeNodes() throws Exception {
    TreePanelInfo treePanel =
        parseJavaInfo(
            "public class Test extends TreePanel {",
            "  public Test() {",
            "    TreeNode root = new TreeNode('R');",
            "    {",
            "      TreeNode child = new TreeNode('A');",
            "      root.appendChild(child);",
            "    }",
            "    {",
            "      TreeNode child = new TreeNode('B');",
            "      root.appendChild(child);",
            "    }",
            "    setRootNode(root);",
            "  }",
            "}");
    treePanel.refresh();
    assertNoErrors(treePanel);
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.tree.TreePanel} {this} {/setRootNode(root)/}",
        "  {new: com.gwtext.client.widgets.tree.TreeNode} {local-unique: root} {/new TreeNode('R')/ /root.appendChild(child)/ /root.appendChild(child)/ /setRootNode(root)/}",
        "    {new: com.gwtext.client.widgets.tree.TreeNode} {local-unique: child} {/new TreeNode('A')/ /root.appendChild(child)/}",
        "    {new: com.gwtext.client.widgets.tree.TreeNode} {local-unique: child} {/new TreeNode('B')/ /root.appendChild(child)/}");
  }

  public void test_appendTreeNode() throws Exception {
    TreePanelInfo treePanel =
        parseJavaInfo(
            "public class Test extends TreePanel {",
            "  public Test() {",
            "    TreeNode root = new TreeNode('R');",
            "    setRootNode(root);",
            "  }",
            "}");
    treePanel.refresh();
    TreeNodeInfo rootNode = treePanel.getRootNode();
    //
    TreeNodeInfo newNode = createJavaInfo("com.gwtext.client.widgets.tree.TreeNode");
    AssociationObject association =
        AssociationObjects.invocationChild("%parent%.appendChild(%child%)", true);
    JavaInfoUtils.add(newNode, association, rootNode, null);
    assertEditor(
        "public class Test extends TreePanel {",
        "  public Test() {",
        "    TreeNode root = new TreeNode('R');",
        "    {",
        "      TreeNode treeNode = new TreeNode('New TreeNode', '');",
        "      root.appendChild(treeNode);",
        "    }",
        "    setRootNode(root);",
        "  }",
        "}");
  }

  public void test_CREATE() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new FitLayout());",
            "  }",
            "}");
    panel.refresh();
    //
    TreePanelInfo treePanel =
        (TreePanelInfo) createWidget("com.gwtext.client.widgets.tree.TreePanel");
    panel.getLayout().command_CREATE(treePanel, null);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new FitLayout());",
        "    {",
        "      TreePanel treePanel = new TreePanel();",
        "      {",
        "        TreeNode treeNode = new TreeNode('(Root)', '');",
        "        treePanel.setRootNode(treeNode);",
        "      }",
        "      add(treePanel);",
        "    }",
        "  }",
        "}");
    panel.refresh();
  }

  /**
   * When delete root node, new one should be added, because <code>TreePanel</code> requires it.
   */
  public void test_delete() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new FitLayout());",
            "    {",
            "      TreePanel treePanel = new TreePanel();",
            "      {",
            "        TreeNode root = new TreeNode('Initial root');",
            "        treePanel.setRootNode(root);",
            "      }",
            "      add(treePanel);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    TreePanelInfo treePanel = (TreePanelInfo) panel.getChildrenWidgets().get(0);
    // delete "root"
    treePanel.getRootNode().delete();
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new FitLayout());",
        "    {",
        "      TreePanel treePanel = new TreePanel();",
        "      {",
        "        TreeNode treeNode = new TreeNode('(Root)', '');",
        "        treePanel.setRootNode(treeNode);",
        "      }",
        "      add(treePanel);",
        "    }",
        "  }",
        "}");
    // delete "treePanel" itself
    treePanel.delete();
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new FitLayout());",
        "  }",
        "}");
  }
}