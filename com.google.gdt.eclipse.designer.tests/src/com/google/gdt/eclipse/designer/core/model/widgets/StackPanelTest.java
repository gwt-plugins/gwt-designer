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
import com.google.gdt.eclipse.designer.model.widgets.panels.AbstractWidgetHandle;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.StackPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.StackPanelInfo.WidgetHandle;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link StackPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class StackPanelTest extends GwtModelTest {
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
   * Even empty <code>StackPanel</code> has some reasonable size.
   */
  public void test_empty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    StackPanel panel = new StackPanel();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    StackPanelInfo panel = (StackPanelInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getBounds().width).isGreaterThan(140);
    assertThat(panel.getBounds().height).isGreaterThan(20);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "StackText" property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Has <code>"StackText"</code> property, because association with text used.
   */
  public void test_StackTextProperty_0() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    StackPanel panel = new StackPanel();",
            "    panel.setPixelSize(400, 300);",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.add(button, 'Some text');",
            "    }",
            "  }",
            "}");
    frame.refresh();
    StackPanelInfo panel = (StackPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    //
    Property textProperty = button.getPropertyByTitle("StackText");
    assertNotNull(textProperty);
    assertTrue(textProperty.getCategory().isSystem());
    assertEquals("Some text", textProperty.getValue());
    textProperty.setValue("New text");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    StackPanel panel = new StackPanel();",
        "    panel.setPixelSize(400, 300);",
        "    rootPanel.add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.add(button, 'New text');",
        "    }",
        "  }",
        "}");
  }

  /**
   * No <code>"StackText"</code> property, because association without text used.
   */
  public void test_StackTextProperty_1() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    StackPanel panel = new StackPanel();",
            "    panel.setPixelSize(400, 300);",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    StackPanelInfo panel = (StackPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    //
    Property textProperty = button.getPropertyByTitle("StackText");
    assertNull(textProperty);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WidgetHandle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link StackPanelInfo#getWidgetHandles()}.
   * <p>
   * No widgets, no {@link WidgetHandle}'s.
   */
  public void test_WidgetHandle_empty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    StackPanel panel = new StackPanel();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    StackPanelInfo panel = (StackPanelInfo) frame.getChildrenWidgets().get(0);
    assertTrue(panel.getWidgetHandles().isEmpty());
  }

  /**
   * Test for {@link StackPanelInfo#getWidgetHandles()}.
   * <p>
   * {@link WidgetHandle}'s should have some good bounds.
   */
  public void test_WidgetHandle_getBounds() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    StackPanel panel = new StackPanel();",
            "    panel.setPixelSize(400, 300);",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button_1 = new Button();",
            "      panel.add(button_1, 'First widget');",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      panel.add(button_2, 'Second widget');",
            "    }",
            "  }",
            "}");
    frame.refresh();
    StackPanelInfo panel = (StackPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    //
    List<WidgetHandle> handles = panel.getWidgetHandles();
    assertThat(handles).hasSize(2);
    {
      WidgetHandle handle = handles.get(0);
      assertSame(button_1, handle.getWidget());
      assertThat(handle.getBounds().x).isEqualTo(0);
      assertThat(handle.getBounds().y).isEqualTo(0);
      assertThat(handle.getBounds().width).isGreaterThan(390);
      assertThat(handle.getBounds().height).isGreaterThan(20);
    }
    {
      WidgetHandle handle = handles.get(1);
      assertSame(button_2, handle.getWidget());
      assertThat(handle.getBounds().x).isEqualTo(0);
      assertThat(handle.getBounds().y).isGreaterThan(250);
      assertThat(handle.getBounds().width).isGreaterThan(390);
      assertThat(handle.getBounds().height).isGreaterThan(20);
    }
  }

  /**
   * Test for {@link AbstractWidgetHandle#show()}.
   */
  public void test_WidgetHandle_show() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    StackPanel panel = new StackPanel();",
            "    panel.setPixelSize(400, 300);",
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
    StackPanelInfo panel = (StackPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    //
    panel.getWidgetHandles().get(1).show();
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visible stack element
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Use {@link StackPanelInfo#showWidget(WidgetInfo)} show required widget.
   */
  public void test_showWidget_1() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    StackPanel panel = new StackPanel();",
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
    StackPanelInfo panel = (StackPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    // by default "button_1" is displayed
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // show "button_2" (no source should be changed)
    {
      String expectedSource = m_lastEditor.getSource();
      panel.showWidget(button_2);
      assertEditor(expectedSource, m_lastEditor);
    }
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * When delete some widget, show widget "0" or nothing.
   */
  public void test_showWidget_2() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    StackPanel panel = new StackPanel();",
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
    StackPanelInfo panel = (StackPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    WidgetInfo button_3 = panel.getChildrenWidgets().get(2);
    // show "button_3"
    panel.showWidget(button_3);
    assertFalse(isVisible(button_1));
    assertFalse(isVisible(button_2));
    assertTrue(isVisible(button_3));
    // delete "button_3", so "button_1" should be displayed
    button_3.delete();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    StackPanel panel = new StackPanel();",
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
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // delete "button_2" and "button_1", no exceptions
    button_2.delete();
    button_1.delete();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    StackPanel panel = new StackPanel();",
        "    rootPanel.add(panel);",
        "  }",
        "}");
  }

  /**
   * When move selected widget, it should be made visible.
   */
  public void test_showWidget_3() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    StackPanel panel = new StackPanel();",
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
    StackPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    WidgetInfo button_3 = getJavaInfoByName("button_3");
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    assertFalse(isVisible(button_3));
    // move "button_3"
    panel.command_MOVE2(button_3, button_2);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    StackPanel panel = new StackPanel();",
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
   * When select some widget in {@link StackPanelInfo}, it should be displayed.
   */
  public void _test_showWidget_4() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    StackPanel panel = new StackPanel();",
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
    frame.refresh();
    StackPanelInfo panel = (StackPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // send "selecting" broadcast
    String expectedSource = m_lastEditor.getSource();
    {
      boolean[] refreshFlag = new boolean[]{false};
      frame.getBroadcastObject().selecting(button_2, refreshFlag);
      assertTrue(refreshFlag[0]);
    }
    assertEditor(expectedSource, m_lastEditor);
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * When select some widget that is indirect child of {@link StackPanelInfo}, it should be
   * displayed.
   */
  public void test_showWidget_5() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    StackPanel panel = new StackPanel();",
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
    StackPanelInfo panel = (StackPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    ComplexPanelInfo flowPanel = (ComplexPanelInfo) panel.getChildrenWidgets().get(1);
    WidgetInfo button_2 = flowPanel.getChildrenWidgets().get(0);
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(flowPanel));
    // send "selecting" broadcast
    String expectedSource = m_lastEditor.getSource();
    {
      boolean[] refreshFlag = new boolean[]{false};
      frame.getBroadcastObject().selecting(button_2, refreshFlag);
      assertTrue(refreshFlag[0]);
      frame.refresh();
    }
    assertEditor(expectedSource, m_lastEditor);
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(flowPanel));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link StackPanelInfo#command_CREATE2(WidgetInfo, WidgetInfo)}.
   */
  public void test_CREATE() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      StackPanel panel = new StackPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.add(button_1);",
        "      }",
        "    }",
        "  }",
        "}");
    refresh();
    StackPanelInfo panel = getJavaInfoByName("panel");
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
        "      StackPanel panel = new StackPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.add(button_1);",
        "      }",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button, 'New widget', false);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(newButton));
  }

  /**
   * Test for {@link StackPanelInfo#command_MOVE2(WidgetInfo, WidgetInfo)}.
   */
  public void test_ADD() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      StackPanel panel = new StackPanel();",
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
    frame.refresh();
    StackPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    assertTrue(isVisible(button_1));
    assertTrue(isVisible(button_2));
    // do ADD
    panel.command_MOVE2(button_2, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      StackPanel panel = new StackPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.add(button_1);",
        "      }",
        "      {",
        "        Button button_2 = new Button();",
        "        panel.add(button_2, 'New widget', false);",
        "        button_2.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * Test for {@link StackPanelInfo#command_MOVE2(WidgetInfo, WidgetInfo)}.
   */
  public void test_MOVE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      StackPanel panel = new StackPanel();",
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
    StackPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // do MOVE
    panel.command_MOVE2(button_2, button_1);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      StackPanel panel = new StackPanel();",
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
   * Test for copy/paste {@link StackPanelInfo} with its children.
   */
  public void test_clipboard() throws Exception {
    final RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      StackPanel panel = new StackPanel();",
            "      rootPanel.add(panel);",
            "      {",
            "        Button button = new Button();",
            "        panel.add(button, 'Title', true);",
            "        button.setSize('100%', '100%');",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    {
      StackPanelInfo panel = (StackPanelInfo) frame.getChildrenWidgets().get(0);
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
        "      StackPanel panel = new StackPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button, 'Title', true);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "    {",
        "      StackPanel stackPanel = new StackPanel();",
        "      rootPanel.add(stackPanel);",
        "      {",
        "        Button button = new Button();",
        "        stackPanel.add(button, 'Title', true);",
        "        button.setSize('100%', '100%');",
        "      }",
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
  private boolean isVisible(WidgetInfo widget) throws Exception {
    return Boolean.TRUE.equals(ReflectionUtils.invokeMethod(widget.getObject(), "isVisible()"));
  }
}