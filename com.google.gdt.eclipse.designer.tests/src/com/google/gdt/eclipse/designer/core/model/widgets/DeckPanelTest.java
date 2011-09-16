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
import com.google.gdt.eclipse.designer.model.widgets.panels.DeckPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link DeckPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class DeckPanelTest extends GwtModelTest {
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
   * Even empty <code>DeckPanel</code> has some reasonable size.
   */
  public void test_empty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DeckPanel panel = new DeckPanel();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    DeckPanelInfo panel = (DeckPanelInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getBounds().width).isGreaterThan(150);
    assertThat(panel.getBounds().height).isGreaterThan(20);
  }

  /**
   * Use {@link DeckPanelInfo#showWidget(WidgetInfo)} property to show required widgets.
   */
  public void test_showWidget_internal_1() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DeckPanel panel = new DeckPanel();",
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
    DeckPanelInfo panel = (DeckPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    // by default "button_1" is displayed
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // show "button_2"
    panel.showWidget(button_2);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    DeckPanel panel = new DeckPanel();",
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
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * When delete some widget, show widget "0" or nothing.
   */
  public void test_showWidget_internal_2() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DeckPanel panel = new DeckPanel();",
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
    DeckPanelInfo panel = (DeckPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    // "button_2" is displayed
    panel.showWidget(button_2);
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
    // delete "button_2", so "button_1" should be displayed
    button_2.delete();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    DeckPanel panel = new DeckPanel();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.add(button_1);",
        "    }",
        "  }",
        "}");
    assertTrue(isVisible(button_1));
    // delete "button_1", remove "showWidget()"
    button_1.delete();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    DeckPanel panel = new DeckPanel();",
        "    rootPanel.add(panel);",
        "  }",
        "}");
  }

  /**
   * When move selected widget, it should still be selected.
   */
  public void test_showWidget_internal_3() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DeckPanel panel = new DeckPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button_1 = new Button();",
            "      panel.add(button_1);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      panel.add(button_2);",
            "    }",
            "    {",
            "      Button button_3 = new Button();",
            "      panel.add(button_3);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    DeckPanelInfo panel = (DeckPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    WidgetInfo button_3 = panel.getChildrenWidgets().get(2);
    //
    panel.showWidget(button_3);
    assertFalse(isVisible(button_1));
    assertFalse(isVisible(button_2));
    assertTrue(isVisible(button_3));
    // move "button_3"
    panel.command_MOVE2(button_3, button_2);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    DeckPanel panel = new DeckPanel();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.add(button_1);",
        "    }",
        "    {",
        "      Button button_3 = new Button();",
        "      panel.add(button_3);",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.add(button_2);",
        "    }",
        "  }",
        "}");
    assertFalse(isVisible(button_1));
    assertFalse(isVisible(button_2));
    assertTrue(isVisible(button_3));
  }

  /**
   * When select some widget in {@link DeckPanelInfo}, it should be displayed.
   */
  public void test_showWidget_internal_4() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DeckPanel panel = new DeckPanel();",
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
    DeckPanelInfo panel = (DeckPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    // initially "button_1" is visible
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // send "selecting" broadcast
    {
      boolean[] refreshFlag = new boolean[]{false};
      frame.getBroadcastObject().selecting(button_2, refreshFlag);
      assertTrue(refreshFlag[0]);
      frame.refresh();
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    DeckPanel panel = new DeckPanel();",
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
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * When select some widget that is indirect child of {@link DeckPanelInfo}, it should be
   * displayed.
   */
  public void test_showWidget_internal_5() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DeckPanel panel = new DeckPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button_1 = new Button();",
            "      panel.add(button_1);",
            "    }",
            "    {",
            "      FlowPanel flowPanel = new FlowPanel();",
            "      panel.add(flowPanel);",
            "      {",
            "        Button button_2 = new Button();",
            "        flowPanel.add(button_2);",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    DeckPanelInfo panel = (DeckPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    ComplexPanelInfo flowPanel = (ComplexPanelInfo) panel.getChildrenWidgets().get(1);
    WidgetInfo button_2 = flowPanel.getChildrenWidgets().get(0);
    // initially "button_1" is visible
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(flowPanel));
    // send "selecting" broadcast
    {
      boolean[] refreshFlag = new boolean[]{false};
      frame.getBroadcastObject().selecting(button_2, refreshFlag);
      assertTrue(refreshFlag[0]);
      frame.refresh();
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    DeckPanel panel = new DeckPanel();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.add(button_1);",
        "    }",
        "    {",
        "      FlowPanel flowPanel = new FlowPanel();",
        "      panel.add(flowPanel);",
        "      {",
        "        Button button_2 = new Button();",
        "        flowPanel.add(button_2);",
        "      }",
        "    }",
        "  }",
        "}");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(flowPanel));
  }

  /**
   * When select some widget that is indirect child of {@link DeckPanelInfo}, it should be
   * displayed.<br>
   * In this case selecting widget is already displayed, so no additional showing required.
   */
  public void test_showWidget_internal_6() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DeckPanel panel = new DeckPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    DeckPanelInfo panel = (DeckPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    assertTrue(isVisible(button));
    // send "selecting" broadcast
    {
      boolean[] refreshFlag = new boolean[]{false};
      frame.getBroadcastObject().selecting(button, refreshFlag);
      assertFalse(refreshFlag[0]);
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    DeckPanel panel = new DeckPanel();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.add(button);",
        "    }",
        "  }",
        "}");
    assertTrue(isVisible(button));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setWidget(0) for runtime
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When delete some widget, invocation of <code>DeckPanel.showWidget()</code> should be changed to
   * use <code>0</code> as argument.
   */
  public void test_showWidget_API_1() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DeckPanel panel = new DeckPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button_1 = new Button();",
            "      panel.add(button_1);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      panel.add(button_2);",
            "    }",
            "    {",
            "      Button button_3 = new Button();",
            "      panel.add(button_3);",
            "    }",
            "    panel.showWidget(1);",
            "  }",
            "}");
    frame.refresh();
    DeckPanelInfo panel = (DeckPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_3 = panel.getChildrenWidgets().get(2);
    // delete "button_3", showWidget(1) should be changed to showWidget(0)
    {
      button_3.delete();
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    DeckPanel panel = new DeckPanel();",
          "    rootPanel.add(panel);",
          "    {",
          "      Button button_1 = new Button();",
          "      panel.add(button_1);",
          "    }",
          "    {",
          "      Button button_2 = new Button();",
          "      panel.add(button_2);",
          "    }",
          "    panel.showWidget(0);",
          "  }",
          "}");
    }
  }

  /**
   * When delete last widget, invocation of <code>DeckPanel.showWidget()</code> should be removed.
   */
  public void test_showWidget_API_2() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DeckPanel panel = new DeckPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.add(button);",
            "    }",
            "    panel.showWidget(0);",
            "  }",
            "}");
    frame.refresh();
    DeckPanelInfo panel = (DeckPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    // delete "button", showWidget(0) should be removed
    button.delete();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    DeckPanel panel = new DeckPanel();",
        "    rootPanel.add(panel);",
        "  }",
        "}");
  }

  /**
   * When move out last widget, invocation of <code>DeckPanel.showWidget()</code> should be removed.
   */
  public void test_showWidget_API_3() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    DeckPanel panel = new DeckPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.add(button);",
            "    }",
            "    panel.showWidget(0);",
            "  }",
            "}");
    frame.refresh();
    DeckPanelInfo panel = (DeckPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    // move "button", showWidget(0) should be removed
    frame.command_MOVE2(button, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    DeckPanel panel = new DeckPanel();",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * When first widget added, then <code>DeckPanel.showWidget()</code> should be added.
   */
  public void test_showWidget_API_4() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      DeckPanel panel = new DeckPanel();",
            "      rootPanel.add(panel);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    DeckPanelInfo panel = (DeckPanelInfo) frame.getChildrenWidgets().get(0);
    // do CREATE
    WidgetInfo newButton = createButton();
    panel.command_CREATE2(newButton, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      DeckPanel panel = new DeckPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button);",
        "      }",
        "      panel.showWidget(0);",
        "    }",
        "  }",
        "}");
  }

  /**
   * When first widget added to new <code>DeckPanel</code>, then <code>DeckPanel.showWidget()</code>
   * should be added.
   */
  public void test_showWidget_API_5() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // create DeckPanel
    DeckPanelInfo newDeckPanel = createJavaInfo("com.google.gwt.user.client.ui.DeckPanel");
    frame.command_CREATE2(newDeckPanel, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      DeckPanel deckPanel = new DeckPanel();",
        "      rootPanel.add(deckPanel);",
        "    }",
        "  }",
        "}");
    // create Button
    WidgetInfo newButton = createButton();
    newDeckPanel.command_CREATE2(newButton, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      DeckPanel deckPanel = new DeckPanel();",
        "      rootPanel.add(deckPanel);",
        "      {",
        "        Button button = new Button();",
        "        deckPanel.add(button);",
        "      }",
        "      deckPanel.showWidget(0);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_flowContainers() throws Exception {
    DeckPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends DeckPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHasWidgetFlowContainer(panel, true);
    assertHasWidgetFlowContainer(panel, false);
  }

  /**
   * Test for {@link DeckPanelInfo#command_CREATE2(WidgetInfo, WidgetInfo)}.
   */
  public void test_CREATE() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      DeckPanel panel = new DeckPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.add(button_1);",
        "      }",
        "    }",
        "  }",
        "}");
    refresh();
    DeckPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    assertTrue(isVisible(button_1));
    // do CREATE
    WidgetInfo newButton = createButton();
    panel.command_CREATE2(newButton, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      DeckPanel panel = new DeckPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.add(button_1);",
        "      }",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button);",
        "      }",
        "    }",
        "  }",
        "}");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(newButton));
  }

  /**
   * Test for {@link DeckPanelInfo#command_MOVE2(WidgetInfo, WidgetInfo)}.
   */
  public void test_MOVE() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      DeckPanel panel = new DeckPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.add(button_1);",
        "      }",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      rootPanel.add(button_2);",
        "    }",
        "  }",
        "}");
    refresh();
    DeckPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    assertTrue(isVisible(button_1));
    // do MOVE
    panel.command_MOVE2(button_2, button_1);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      DeckPanel panel = new DeckPanel();",
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
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * Test for copy/paste {@link DeckPanelInfo} with its children.
   */
  public void test_clipboard() throws Exception {
    final RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      DeckPanel panel = new DeckPanel();",
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
      DeckPanelInfo panel = (DeckPanelInfo) frame.getChildrenWidgets().get(0);
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
        "      DeckPanel panel = new DeckPanel();",
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
        "      DeckPanel deckPanel = new DeckPanel();",
        "      rootPanel.add(deckPanel);",
        "      {",
        "        Button button = new Button('A');",
        "        deckPanel.add(button);",
        "      }",
        "      {",
        "        Button button = new Button('B');",
        "        deckPanel.add(button);",
        "      }",
        "      deckPanel.showWidget(0);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if object of given {@link WidgetInfo} is visible.
   */
  private static boolean isVisible(WidgetInfo widget) throws Exception {
    return Boolean.TRUE.equals(ReflectionUtils.invokeMethod(widget.getObject(), "isVisible()"));
  }
}