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
package com.google.gdt.eclipse.designer.core.model.widgets.generic;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

/**
 * Test for size management in {@link PanelInfo}.
 * 
 * @author scheglov_ke
 */
public class SizeManagementTest extends GwtModelTest {
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
  // Remove size on MOVE out
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_onChildOut_removeSize__never() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends FlowPanel {",
            "}"));
    setFileContentSrc(
        "test/client/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='onChildOut.removeSize'>never</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyPanel panel = new MyPanel();",
            "      rootPanel.add(panel);",
            "      {",
            "        Button button = new Button();",
            "        panel.add(button);",
            "        button.setSize('100%', '50px');",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ComplexPanelInfo panel = (ComplexPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    // move "button"
    frame.startEdit();
    frame.command_MOVE2(button, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      MyPanel panel = new MyPanel();",
        "      rootPanel.add(panel);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      button.setSize('100%', '50px');",
        "    }",
        "  }",
        "}");
  }

  public void test_onChildOut_removeSize__always() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends FlowPanel {",
            "}"));
    setFileContentSrc(
        "test/client/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='onChildOut.removeSize'>always</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyPanel panel = new MyPanel();",
            "      rootPanel.add(panel);",
            "      {",
            "        Button button = new Button();",
            "        panel.add(button);",
            "        button.setSize('100%', '50px');",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ComplexPanelInfo panel = (ComplexPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    // move "button"
    frame.startEdit();
    frame.command_MOVE2(button, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      MyPanel panel = new MyPanel();",
        "      rootPanel.add(panel);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Set size on CREATE/ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets some not constant size on create.
   */
  public void test_onChildAdd_setSize__CREATE_withSize() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends FlowPanel {",
            "}"));
    setFileContentSrc(
        "test/client/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='onChildAdd.setWidth'>100%</parameter>",
            "    <parameter name='onChildAdd.setHeight'>75</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyPanel panel = new MyPanel();",
            "      rootPanel.add(panel);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ComplexPanelInfo panel = (ComplexPanelInfo) frame.getChildrenWidgets().get(0);
    // create new Button
    WidgetInfo newButton = createButton();
    frame.startEdit();
    panel.command_CREATE2(newButton, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      MyPanel panel = new MyPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button);",
        "        button.setSize('100%', '75');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Use empty string for size, so don't set size.
   */
  public void test_onChildAdd_setSize__CREATE_noSize() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends FlowPanel {",
            "}"));
    setFileContentSrc(
        "test/client/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='onChildAdd.setWidth'>null</parameter>",
            "    <parameter name='onChildAdd.setHeight'>null</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyPanel panel = new MyPanel();",
            "      rootPanel.add(panel);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ComplexPanelInfo panel = (ComplexPanelInfo) frame.getChildrenWidgets().get(0);
    // create new Button
    WidgetInfo newButton = createButton();
    frame.startEdit();
    panel.command_CREATE2(newButton, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      MyPanel panel = new MyPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * During move size is ignored.
   */
  public void test_onChildAdd_setSize__MOVE() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends FlowPanel {",
            "}"));
    setFileContentSrc(
        "test/client/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='onChildAdd.setWidth'>100%</parameter>",
            "    <parameter name='onChildAdd.setHeight'>75</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyPanel panel = new MyPanel();",
            "      rootPanel.add(panel);",
            "      {",
            "        Button button_1 = new Button();",
            "        panel.add(button_1);",
            "      }",
            "      {",
            "        Button button_2 = new Button();",
            "        panel.add(button_2);",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ComplexPanelInfo panel = (ComplexPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    // move "button"
    frame.startEdit();
    panel.command_MOVE2(button_2, button_1);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      MyPanel panel = new MyPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button_2 = new Button();",
        "        panel.add(button_2);",
        "      }",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.add(button_1);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_onChildAdd_setSize__ADD() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends FlowPanel {",
            "}"));
    setFileContentSrc(
        "test/client/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='onChildAdd.setWidth'>100%</parameter>",
            "    <parameter name='onChildAdd.setHeight'>75</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyPanel panel = new MyPanel();",
            "      rootPanel.add(panel);",
            "    }",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ComplexPanelInfo panel = (ComplexPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = frame.getChildrenWidgets().get(1);
    // move "button"
    frame.startEdit();
    panel.command_MOVE2(button, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      MyPanel panel = new MyPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button);",
        "        button.setSize('100%', '75');",
        "      }",
        "    }",
        "  }",
        "}");
  }
}