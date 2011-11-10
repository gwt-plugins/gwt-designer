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
import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;

import java.util.List;

/**
 * Test for {@link CardLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class CardLayoutTest extends GxtModelTest {
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
    // set CardLayout
    CardLayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.CardLayout");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new CardLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new CardLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.CardLayout} {empty} {/setLayout(new CardLayout())/}");
    assertSame(layout, container.getLayout());
  }

  /**
   * Test for dangling {@link CardLayoutInfo}.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?46008
   */
  public void test_dangling() throws Exception {
    parseJavaInfo(
        "public class Test extends LayoutContainer {",
        "  private CardLayout m_cardLayout = new CardLayout();",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    add(new Button());",
        "  }",
        "}");
    refresh();
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout())/ /add(new Button())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {empty} {/add(new Button())/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FlowData} {virtual-layout-data} {}");
  }

  /**
   * Test for flow container support.
   */
  public void test_flowContainer() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "  }",
            "}");
    container.refresh();
    CardLayoutInfo layout = (CardLayoutInfo) container.getLayout();
    FlowContainer flowContainer = new FlowContainerFactory(layout, false).get().get(0);
    // add new Button
    ComponentInfo newButton = createButton();
    assertTrue(flowContainer.validateComponent(newButton));
    flowContainer.command_CREATE(newButton, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new CardLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
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
            "    setLayout(new CardLayout());",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2);",
            "    }",
            "    {",
            "      Button button_3 = new Button();",
            "      add(button_3);",
            "    }",
            "  }",
            "}");
    container.refresh();
    List<WidgetInfo> widgets = container.getWidgets();
    // initially "button_1" is expanded
    assertActiveIndex(container, 0);
    // notify about "button_2"
    {
      boolean shouldRefresh = notifySelecting(widgets.get(1));
      assertTrue(shouldRefresh);
      container.refresh();
      // now "button_2" is expanded
      assertActiveIndex(container, 1);
    }
    // second notification about "button_2" does not cause refresh()
    {
      boolean shouldRefresh = notifySelecting(widgets.get(1));
      assertFalse(shouldRefresh);
    }
  }

  private static void assertActiveIndex(LayoutContainerInfo container, int expectedIndex)
      throws Exception {
    WidgetInfo actual = ((CardLayoutInfo) container.getLayout()).getActiveWidget();
    WidgetInfo expected = container.getWidgets().get(expectedIndex);
    assertEquals(expected, actual);
  }
}