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
import com.google.gdt.eclipse.designer.gwtext.model.widgets.TreePanelInfo;

/**
 * Tests for <code>FocusPanel</code>.
 * 
 * @author scheglov_ke
 */
public class FocusPanelTest extends GwtExtModelTest {
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
  public void test_parse() throws Exception {
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