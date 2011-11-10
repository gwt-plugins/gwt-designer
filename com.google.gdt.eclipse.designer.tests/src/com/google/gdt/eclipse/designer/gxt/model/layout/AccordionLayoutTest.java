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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ContentPanelInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Test for {@link AccordionLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class AccordionLayoutTest extends GxtModelTest {
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
  public void test_setLayout() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    // set AccordionLayout
    AccordionLayoutInfo layout =
        createJavaInfo("com.extjs.gxt.ui.client.widget.layout.AccordionLayout");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AccordionLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new AccordionLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.AccordionLayout} {empty} {/setLayout(new AccordionLayout())/}");
    assertSame(layout, container.getLayout());
  }

  /**
   * Test for flow container support.
   */
  public void test_flowContainer() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AccordionLayout());",
            "  }",
            "}");
    container.refresh();
    AccordionLayoutInfo layout = (AccordionLayoutInfo) container.getLayout();
    FlowContainer flowContainer = new FlowContainerFactory(layout, false).get().get(0);
    // add new ContentPanel
    ContentPanelInfo newPanel = createJavaInfo("com.extjs.gxt.ui.client.widget.ContentPanel");
    assertTrue(flowContainer.validateComponent(newPanel));
    flowContainer.command_CREATE(newPanel, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new AccordionLayout());",
        "    {",
        "      ContentPanel contentPanel = new ContentPanel();",
        "      contentPanel.setHeading('New ContentPanel');",
        "      contentPanel.setCollapsible(true);",
        "      add(contentPanel);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Manage active
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_manageActive() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AccordionLayout());",
            "    {",
            "      ContentPanel panel_1 = new ContentPanel();",
            "      add(panel_1);",
            "    }",
            "    {",
            "      ContentPanel panel_2 = new ContentPanel();",
            "      add(panel_2);",
            "    }",
            "    {",
            "      ContentPanel panel_3 = new ContentPanel();",
            "      add(panel_3);",
            "    }",
            "  }",
            "}");
    container.refresh();
    List<WidgetInfo> widgets = container.getWidgets();
    // initially "panel_1" is expanded
    assertActiveIndex(container, 0);
    // notify about "panel_2"
    {
      boolean shouldRefresh = notifySelecting(widgets.get(1));
      assertTrue(shouldRefresh);
      container.refresh();
      // now "panel_2" is expanded
      assertActiveIndex(container, 1);
    }
    // second notification about "panel_2" does not cause refresh()
    {
      boolean shouldRefresh = notifySelecting(widgets.get(1));
      assertFalse(shouldRefresh);
    }
  }

  public void test_manageActive_withTopComponent() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends Window {",
            "  public Test() {",
            "    setLayout(new AccordionLayout());",
            "    {",
            "      ToolBar toolBar = new ToolBar();",
            "      setTopComponent(toolBar);",
            "    }",
            "    {",
            "      ContentPanel panel_1 = new ContentPanel();",
            "      add(panel_1);",
            "    }",
            "    {",
            "      ContentPanel panel_2 = new ContentPanel();",
            "      add(panel_2);",
            "    }",
            "  }",
            "}");
    container.refresh();
    // initially "panel_1" is expanded
    assertActiveIndex(container, 1);
  }

  private static void assertActiveIndex(LayoutContainerInfo container, int expectedIndex)
      throws Exception {
    WidgetInfo actual = ((AccordionLayoutInfo) container.getLayout()).getActivePanel();
    WidgetInfo expected = container.getWidgets().get(expectedIndex);
    assertEquals(expected, actual);
    assertEquals(true, ReflectionUtils.invokeMethod(expected.getObject(), "isExpanded()"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cases
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If child <code>ContentPanel</code> of <code>AccordionLayout</code> was not created and replaced
   * with placeholder, we should not call its methods and fail because of this.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?45266
   */
  public void test_whenChildContentPanel_replacedWithPlaceholder() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/BadPanel.java",
        getTestSource(
            "public class BadPanel extends ContentPanel {",
            "  public BadPanel() {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new AccordionLayout());",
            "    {",
            "      BadPanel panel = new BadPanel();",
            "      add(panel);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new AccordionLayout())/ /add(panel)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.AccordionLayout} {empty} {/setLayout(new AccordionLayout())/}",
        "  {new: test.client.BadPanel} {local-unique: panel} {/new BadPanel()/ /add(panel)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FitData} {virtual-layout-data} {}");
    container.refresh();
    //
    ContentPanelInfo panel = getJavaInfoByName("panel");
    assertTrue(panel.isPlaceholder());
  }
}