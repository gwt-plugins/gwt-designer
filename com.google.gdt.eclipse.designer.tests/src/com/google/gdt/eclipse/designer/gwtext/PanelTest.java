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

import com.google.gdt.eclipse.designer.gwtext.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests for {@link PanelInfo}.
 * 
 * @author scheglov_ke
 */
public class PanelTest extends GwtExtModelTest {
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
  public void test_parseEmpty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.Panel;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Panel panel = new Panel();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(panel)/}",
        "  {new: com.gwtext.client.widgets.Panel} {local-unique: panel} {/new Panel()/ /rootPanel.add(panel)/}",
        "    {implicit-layout: default} {implicit-layout} {}");
    assertInstanceOf(PanelInfo.class, frame.getChildrenWidgets().get(0));
  }

  public void test_liveImage() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    {
      PanelInfo panel = (PanelInfo) createWidget("com.gwtext.client.widgets.Panel");
      assertThat(panel).isNotNull();
      assertThat(panel.getImage()).isNotNull();
    }
  }

  /**
   * Test for {@link PanelInfo#shouldDrawDotsBorder()}.
   */
  public void test_1() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Panel panel = new Panel('Panel with border');",
            "      rootPanel.add(panel);",
            "    }",
            "    {",
            "      Panel panel = new Panel();",
            "      panel.setBorder(false);",
            "      rootPanel.add(panel);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // has border
    {
      PanelInfo panel = (PanelInfo) frame.getChildrenWidgets().get(0);
      assertEquals(true, panel.getPropertyByTitle("border").getValue());
      assertFalse(panel.shouldDrawDotsBorder());
    }
    // no border
    {
      PanelInfo panel = (PanelInfo) frame.getChildrenWidgets().get(1);
      assertEquals(false, panel.getPropertyByTitle("border").getValue());
      assertTrue(panel.shouldDrawDotsBorder());
    }
  }

  public void test_clientAreaInsets() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setTitle('My title');",
            "  }",
            "}");
    panel.refresh();
    // check insets
    Insets insets = panel.getClientAreaInsets();
    assertEquals(new Insets(26, 0, 0, 0), insets);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cases
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Combination of <code>Panel</code> with "html" property and <code>TreePanel</code> child is
   * buggy.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44153
   */
  public void test_removePropertyHtml_whenAddChild() throws Exception {
    final PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setHtml('My html');",
            "  }",
            "}");
    panel.refresh();
    //
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        ComponentInfo treePanel = createJavaInfo("com.gwtext.client.widgets.tree.TreePanel");
        panel.getLayout().command_CREATE(treePanel, null);
      }
    });
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
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
  }
}