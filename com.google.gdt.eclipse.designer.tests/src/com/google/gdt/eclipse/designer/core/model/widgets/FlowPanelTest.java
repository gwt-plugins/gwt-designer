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
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for <code>FlowPanel</code> support.
 * 
 * @author scheglov_ke
 */
public class FlowPanelTest extends GwtModelTest {
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
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_flowContainers() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHasWidgetFlowContainer(panel, true);
    assertHasWidgetFlowContainer(panel, false);
  }

  public void test_CREATE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlowPanel flowPanel = new FlowPanel();",
            "    rootPanel.add(flowPanel);",
            "  }",
            "}");
    frame.refresh();
    PanelInfo flowPanel = (PanelInfo) frame.getChildrenWidgets().get(0);
    // do CREATE
    WidgetInfo newButton = createButton();
    flowContainer_CREATE(flowPanel, newButton, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlowPanel flowPanel = new FlowPanel();",
        "    rootPanel.add(flowPanel);",
        "    {",
        "      Button button = new Button();",
        "      flowPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_ADD() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      FlowPanel flowPanel_1 = new FlowPanel();",
            "      rootPanel.add(flowPanel_1);",
            "      {",
            "        Button button = new Button();",
            "        flowPanel_1.add(button);",
            "      }",
            "    }",
            "    {",
            "      FlowPanel flowPanel_2 = new FlowPanel();",
            "      rootPanel.add(flowPanel_2);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ComplexPanelInfo panel_2 = getJavaInfoByName("flowPanel_2");
    WidgetInfo button = getJavaInfoByName("button");
    // do ADD
    flowContainer_MOVE(panel_2, button, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      FlowPanel flowPanel_1 = new FlowPanel();",
        "      rootPanel.add(flowPanel_1);",
        "    }",
        "    {",
        "      FlowPanel flowPanel_2 = new FlowPanel();",
        "      rootPanel.add(flowPanel_2);",
        "      {",
        "        Button button = new Button();",
        "        flowPanel_2.add(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Generic commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FlowContainer_Support#command_absolute_CREATE(WidgetInfo, WidgetInfo)}.
   */
  public void test_CREATE2() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "public class MyPanel extends Button {",
            "  public void fakeAdd(Widget widget) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='flowContainer'>true</parameter>",
            "    <parameter name='flowContainer.horizontal'>true</parameter>",
            "    <parameter name='flowContainer.association'>%parent%.fakeAdd(%child%)</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    MyPanel myPanel = new MyPanel();",
            "    rootPanel.add(myPanel);",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo myPanel = frame.getChildrenWidgets().get(0);
    // prepare FlowContainer
    FlowContainer flowContainer = getFlowContainer(myPanel);
    assertTrue(flowContainer.isHorizontal());
    // do CREATE
    WidgetInfo newButton = createButton();
    flowContainer.command_CREATE(newButton, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    MyPanel myPanel = new MyPanel();",
        "    rootPanel.add(myPanel);",
        "    {",
        "      Button button = new Button();",
        "      myPanel.fakeAdd(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link FlowContainer_Support#command_absolute_MOVE(WidgetInfo, WidgetInfo)}.
   */
  public void test_ADD2() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "public class MyPanel extends Button {",
            "  public void fakeAdd(Widget widget) {",
            "    DOM.appendChild(getElement(), widget.getElement());",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='fakeAdd'>",
            "      <parameter type='com.google.gwt.user.client.ui.Widget' child='true'/>",
            "    </method>",
            "  </methods>",
            "  <parameters>",
            "    <parameter name='flowContainer'>true</parameter>",
            "    <parameter name='flowContainer.horizontal'>true</parameter>",
            "    <parameter name='flowContainer.association'>%parent%.fakeAdd(%child%)</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    final RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyPanel myPanel_1 = new MyPanel();",
            "      rootPanel.add(myPanel_1);",
            "      {",
            "        Button button = new Button();",
            "        myPanel_1.fakeAdd(button);",
            "      }",
            "    }",
            "    {",
            "      MyPanel myPanel_2 = new MyPanel();",
            "      rootPanel.add(myPanel_2);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo panel_1 = frame.getChildrenWidgets().get(0);
    WidgetInfo panel_2 = frame.getChildrenWidgets().get(1);
    WidgetInfo button = panel_1.getChildren(WidgetInfo.class).get(0);
    // do ADD
    FlowContainer flowContainer_2 = getFlowContainer(panel_2);
    flowContainer_2.command_MOVE(button, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      MyPanel myPanel_1 = new MyPanel();",
        "      rootPanel.add(myPanel_1);",
        "    }",
        "    {",
        "      MyPanel myPanel_2 = new MyPanel();",
        "      rootPanel.add(myPanel_2);",
        "      {",
        "        Button button = new Button();",
        "        myPanel_2.fakeAdd(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Subclasses
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for parsing <code>HorizontalPanel</code>.
   */
  public void test_HorizontalPanel() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    HorizontalPanel panel = new HorizontalPanel();",
            "    rootPanel.add(panel);",
            "    panel.setPixelSize(300, 200);",
            "  }",
            "}");
    frame.refresh();
    //
    PanelInfo panel = (PanelInfo) frame.getChildrenWidgets().get(0);
    assertEquals(300, panel.getBounds().width);
    assertEquals(200, panel.getBounds().height);
    // check FlowContainer
    FlowContainer flowContainer = getFlowContainer(panel);
    assertTrue(flowContainer.isHorizontal());
  }

  /**
   * Test for parsing <code>VerticalPanel</code>.
   */
  public void test_VerticalPanel() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    VerticalPanel panel = new VerticalPanel();",
            "    rootPanel.add(panel);",
            "    panel.setPixelSize(300, 200);",
            "  }",
            "}");
    frame.refresh();
    //
    PanelInfo panel = (PanelInfo) frame.getChildrenWidgets().get(0);
    assertEquals(300, panel.getBounds().width);
    assertEquals(200, panel.getBounds().height);
    // check FlowContainer
    FlowContainer flowContainer = getFlowContainer(panel);
    assertFalse(flowContainer.isHorizontal());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return some {@link FlowContainer} for given {@link JavaInfo}.
   */
  private static FlowContainer getFlowContainer(JavaInfo container) {
    List<FlowContainer> containers = new FlowContainerFactory(container, false).get();
    assertThat(containers).isNotEmpty();
    return containers.get(0);
  }
}