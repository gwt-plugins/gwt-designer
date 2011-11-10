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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.HorizontalPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.model.util.PropertyUtils;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link HorizontalPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class HorizontalPanelTest extends GwtModelTest {
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
   * Even empty <code>HorizontalPanel</code> has some reasonable size.
   */
  public void test_empty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    HorizontalPanel panel = new HorizontalPanel();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    HorizontalPanelInfo panel = (HorizontalPanelInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getBounds().width).isGreaterThan(140);
    assertThat(panel.getBounds().height).isGreaterThan(20);
  }

  /**
   * Test for "Cell" property.
   */
  public void test_propertyCell() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    HorizontalPanel panel = new HorizontalPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    assertNotNull(PropertyUtils.getByPath(button, "Cell"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_flowContainers() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends HorizontalPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHasWidgetFlowContainer(panel, true);
    assertHasWidgetFlowContainer(panel, false);
  }

  /**
   * Test for {@link HorizontalPanelInfo#command_CREATE2(WidgetInfo, WidgetInfo)}.
   */
  public void test_CREATE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    HorizontalPanel panel = new HorizontalPanel();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    HorizontalPanelInfo panel = (HorizontalPanelInfo) frame.getChildrenWidgets().get(0);
    //
    WidgetInfo newButton = createButton();
    panel.command_CREATE2(newButton, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    HorizontalPanel panel = new HorizontalPanel();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link HorizontalPanelInfo#command_MOVE2(WidgetInfo, WidgetInfo)}.
   */
  public void test_MOVE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    HorizontalPanel panel = new HorizontalPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button_1 = new Button();",
            "      panel.add(button_1);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      panel.add(button_2);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    HorizontalPanelInfo panel = (HorizontalPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    //
    panel.command_MOVE2(button_2, button_1);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    HorizontalPanel panel = new HorizontalPanel();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.add(button_2);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.add(button_1);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for copy/paste {@link HorizontalPanelInfo} with its children.
   */
  public void test_clipboard() throws Exception {
    final RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      HorizontalPanel panel = new HorizontalPanel();",
            "      rootPanel.add(panel);",
            "      {",
            "        Button button_1 = new Button('A');",
            "        panel.add(button_1);",
            "      }",
            "      {",
            "        Button button_2 = new Button('B');",
            "        panel.add(button_2);",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    {
      HorizontalPanelInfo panel = getJavaInfoByName("panel");
      doCopyPaste(panel, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          frame.command_CREATE2(copy, null);
        }
      });
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      HorizontalPanel panel = new HorizontalPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button_1 = new Button('A');",
        "        panel.add(button_1);",
        "      }",
        "      {",
        "        Button button_2 = new Button('B');",
        "        panel.add(button_2);",
        "      }",
        "    }",
        "    {",
        "      HorizontalPanel horizontalPanel = new HorizontalPanel();",
        "      rootPanel.add(horizontalPanel);",
        "      {",
        "        Button button = new Button('A');",
        "        horizontalPanel.add(button);",
        "      }",
        "      {",
        "        Button button = new Button('B');",
        "        horizontalPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }
}