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
import com.google.gdt.eclipse.designer.model.widgets.panels.AbstractWidgetHandle;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.TabPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.TabPanelInfo.WidgetHandle;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link TabPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class TabPanelTest extends GwtModelTest {
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
   * Even empty <code>TabPanel</code> has some reasonable size.
   */
  public void test_empty() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    TabPanel panel = new TabPanel();",
        "    add(panel);",
        "  }",
        "}");
    refresh();
    TabPanelInfo panel = getJavaInfoByName("panel");
    assertThat(panel.getBounds().width).isGreaterThan(120);
    assertThat(panel.getBounds().height).isGreaterThan(20);
  }

  /**
   * If Widget used as "handle", then it should be child of added Widget.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43566
   */
  public void test_WidgetAsHandle() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    TabPanel panel = new TabPanel();",
        "    add(panel);",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.add(button_1, new Label('A'));",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.add(button_2, new Label('B'));",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.FlowPanel} {this} {/add(panel)/}",
        "  {new: com.google.gwt.user.client.ui.TabPanel} {local-unique: panel} {/new TabPanel()/ /add(panel)/ /panel.add(button_1, new Label('A'))/ /panel.add(button_2, new Label('B'))/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button_1} {/new Button()/ /panel.add(button_1, new Label('A'))/}",
        "      {new: com.google.gwt.user.client.ui.Label} {empty} {/panel.add(button_1, new Label('A'))/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button_2} {/new Button()/ /panel.add(button_2, new Label('B'))/}",
        "      {new: com.google.gwt.user.client.ui.Label} {empty} {/panel.add(button_2, new Label('B'))/}");
  }

  /**
   * Test for using bad <code>TabPanel</code> replaced with placeholder.
   */
  public void test_whenPlaceholder() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyTabPanel.java",
        getTestSource(
            "public class MyTabPanel extends TabPanel {",
            "  public MyTabPanel() {",
            "    throw new IllegalStateException('Actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      MyTabPanel panel = new MyTabPanel();",
        "      rootPanel.add(panel);",
        "    }",
        "  }",
        "}");
    refresh();
    TabPanelInfo panel = getJavaInfoByName("panel");
    assertTrue(panel.isPlaceholder());
  }

  /**
   * Has <code>"TabText"</code> property, because association with text used.
   */
  public void test_TabTextProperty() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    TabPanel panel = new TabPanel();",
        "    panel.setPixelSize(400, 300);",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.add(button, 'Some text');",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    Property textProperty = button.getPropertyByTitle("TabText");
    assertNotNull(textProperty);
    assertTrue(textProperty.getCategory().isSystem());
    assertEquals("Some text", textProperty.getValue());
    textProperty.setValue("New text");
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    TabPanel panel = new TabPanel();",
        "    panel.setPixelSize(400, 300);",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.add(button, 'New text');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WidgetHandle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link TabPanelInfo#getWidgetHandles()}.<br>
   * No widgets, no {@link WidgetHandle}'s.
   */
  public void test_WidgetHandle_getWidgetHandles() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    TabPanel panel = new TabPanel();",
        "    add(panel);",
        "  }",
        "}");
    refresh();
    TabPanelInfo panel = getJavaInfoByName("panel");
    assertTrue(panel.getWidgetHandles().isEmpty());
  }

  /**
   * Test for {@link TabPanelInfo#getWidgetHandles()}.
   * <p>
   * {@link WidgetHandle}'s should have some good bounds.
   */
  public void test_WidgetHandle_1() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    TabPanel panel = new TabPanel();",
        "    panel.setPixelSize(400, 300);",
        "    add(panel);",
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
    refresh();
    TabPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    //
    List<WidgetHandle> handles = panel.getWidgetHandles();
    assertThat(handles).hasSize(2);
    {
      WidgetHandle handle = handles.get(0);
      assertSame(button_1, handle.getWidget());
      Rectangle bounds = handle.getBounds();
      assertThat(bounds.x).isLessThan(10);
      assertThat(bounds.y).isEqualTo(0);
      assertThat(bounds.width).isGreaterThan(90);
      assertThat(bounds.height).isGreaterThan(20);
    }
    {
      WidgetHandle handle = handles.get(1);
      assertSame(button_2, handle.getWidget());
      Rectangle bounds = handle.getBounds();
      assertThat(bounds.x).isGreaterThan(10 + 80);
      assertThat(bounds.y).isEqualTo(0);
      assertThat(bounds.width).isGreaterThan(100);
      assertThat(bounds.height).isGreaterThan(20);
    }
  }

  /**
   * Test for {@link AbstractWidgetHandle#show()}.
   */
  public void test_WidgetHandle_2() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    TabPanel panel = new TabPanel();",
        "    panel.setPixelSize(400, 300);",
        "    add(panel);",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.add(button_1, 'A');",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.add(button_2, 'B');",
        "    }",
        "  }",
        "}");
    refresh();
    TabPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
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
   * Use {@link TabPanelInfo#showWidget(WidgetInfo)} show required widget.
   */
  public void test_showWidget_1() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    TabPanel panel = new TabPanel();",
        "    add(panel);",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.add(button_1, 'A');",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.add(button_2, 'B');",
        "    }",
        "  }",
        "}");
    refresh();
    TabPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
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
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    TabPanel panel = new TabPanel();",
        "    add(panel);",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.add(button_1, 'A');",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.add(button_2, 'B');",
        "    }",
        "    {",
        "      Button button_3 = new Button();",
        "      panel.add(button_3, 'B');",
        "    }",
        "  }",
        "}");
    refresh();
    TabPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    WidgetInfo button_3 = getJavaInfoByName("button_3");
    // show "button_3"
    panel.showWidget(button_3);
    assertFalse(isVisible(button_1));
    assertFalse(isVisible(button_2));
    assertTrue(isVisible(button_3));
    // delete "button_3", so "button_1" should be displayed
    button_3.delete();
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    TabPanel panel = new TabPanel();",
        "    add(panel);",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.add(button_1, 'A');",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.add(button_2, 'B');",
        "    }",
        "  }",
        "}");
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // delete "button_2" and "button_1", no exceptions
    button_2.delete();
    button_1.delete();
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    TabPanel panel = new TabPanel();",
        "    add(panel);",
        "  }",
        "}");
  }

  /**
   * When move selected widget, it should be made visible.
   */
  public void test_showWidget_3() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    TabPanel panel = new TabPanel();",
        "    add(panel);",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.add(button_1, 'A');",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.add(button_2, 'B');",
        "    }",
        "    {",
        "      Button button_3 = new Button();",
        "      panel.add(button_3, 'C');",
        "    }",
        "  }",
        "}");
    refresh();
    TabPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    WidgetInfo button_3 = getJavaInfoByName("button_3");
    // initial state
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    assertFalse(isVisible(button_3));
    // move "button_3"
    panel.command_MOVE2(button_3, button_2);
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    TabPanel panel = new TabPanel();",
        "    add(panel);",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.add(button_1, 'A');",
        "    }",
        "    {",
        "      Button button_3 = new Button();",
        "      panel.add(button_3, 'C');",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.add(button_2, 'B');",
        "    }",
        "  }",
        "}");
    assertFalse(isVisible(button_1));
    assertFalse(isVisible(button_2));
    assertTrue(isVisible(button_3));
  }

  /**
   * When select some widget in {@link TabPanelInfo}, it should be displayed.
   */
  public void _test_showWidget_4() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    TabPanel panel = new TabPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button_1 = new Button();",
            "      panel.add(button_1, 'A');",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      panel.add(button_2, 'B');",
            "    }",
            "    panel.showWidget(0);",
            "  }",
            "}");
    frame.refresh();
    TabPanelInfo panel = (TabPanelInfo) frame.getChildrenWidgets().get(0);
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
   * When select some widget that is indirect child of {@link TabPanelInfo}, it should be displayed.
   */
  public void test_showWidget_5() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    TabPanel panel = new TabPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button_1 = new Button();",
            "      panel.add(button_1, 'A');",
            "    }",
            "    {",
            "      FlowPanel flowPanel = new FlowPanel();",
            "      panel.add(flowPanel, 'B');",
            "      {",
            "        Button button_2 = new Button();",
            "        flowPanel.add(button_2);",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    TabPanelInfo panel = (TabPanelInfo) frame.getChildrenWidgets().get(0);
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
   * While <code>TabPanel</code> is subclass of <code>Composite</code>, it is not simple container.
   */
  public void test_notSimpleContainer() throws Exception {
    TabPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends TabPanel {",
            "  public Test() {",
            "  }",
            "}");
    // TabPanel is not simple container
    assertHasWidgetSimpleContainer(panel, true, false);
  }

  /**
   * Test for {@link TabPanelInfo#command_CREATE2(WidgetInfo, WidgetInfo)}.
   */
  public void test_CREATE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      TabPanel panel = new TabPanel();",
            "      rootPanel.add(panel);",
            "      {",
            "        Button button_1 = new Button();",
            "        panel.add(button_1, 'A');",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    TabPanelInfo panel = (TabPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    assertTrue(isVisible(button_1));
    // do CREATE
    WidgetInfo newButton = createButton();
    panel.command_CREATE2(newButton, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      TabPanel panel = new TabPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.add(button_1, 'A');",
        "      }",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button, 'New tab', false);",
        "        button.setSize('5cm', '3cm');",
        "      }",
        "    }",
        "  }",
        "}");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(newButton));
  }

  /**
   * Test for {@link TabPanelInfo#command_MOVE2(WidgetInfo, WidgetInfo)}.
   */
  public void test_ADD() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      TabPanel panel = new TabPanel();",
            "      rootPanel.add(panel);",
            "      {",
            "        Button button_1 = new Button();",
            "        panel.add(button_1, 'A');",
            "      }",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      rootPanel.add(button_2);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    TabPanelInfo panel = (TabPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = frame.getChildrenWidgets().get(1);
    assertTrue(isVisible(button_1));
    assertTrue(isVisible(button_2));
    // do ADD
    panel.command_MOVE2(button_2, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      TabPanel panel = new TabPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.add(button_1, 'A');",
        "      }",
        "      {",
        "        Button button_2 = new Button();",
        "        panel.add(button_2, 'New tab', false);",
        "        button_2.setSize('5cm', '3cm');",
        "      }",
        "    }",
        "  }",
        "}");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * Test for {@link TabPanelInfo#command_MOVE2(WidgetInfo, WidgetInfo)}.
   */
  public void test_MOVE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      TabPanel panel = new TabPanel();",
            "      rootPanel.add(panel);",
            "      {",
            "        Button button_1 = new Button();",
            "        panel.add(button_1, 'A');",
            "      }",
            "      {",
            "        Button button_2 = new Button();",
            "        panel.add(button_2, 'B');",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    TabPanelInfo panel = (TabPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // do MOVE
    panel.command_MOVE2(button_2, button_1);
    frame.refresh();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      TabPanel panel = new TabPanel();",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button_2 = new Button();",
        "        panel.add(button_2, 'B');",
        "      }",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.add(button_1, 'A');",
        "      }",
        "    }",
        "  }",
        "}");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Copy/paste of {@link WidgetInfo} on {@link TabPanelInfo}.
   */
  public void test_clipboard_Widget() throws Exception {
    final TabPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabPanel {",
            "  public Test() {",
            "    {",
            "      Button myButton = new Button();",
            "      add(myButton, 'tab', false);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    {
      WidgetInfo myButton = getJavaInfoByName("myButton");
      doCopyPaste(myButton, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          panel.command_CREATE2(copy, null);
        }
      });
    }
    assertEditor(
        "public class Test extends TabPanel {",
        "  public Test() {",
        "    {",
        "      Button myButton = new Button();",
        "      add(myButton, 'tab', false);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New tab', false);",
        "      button.setSize('5cm', '3cm');",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.TabPanel} {this} {/add(myButton, 'tab', false)/ /add(button, 'New tab', false)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: myButton} {/new Button()/ /add(myButton, 'tab', false)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /add(button, 'New tab', false)/ /button.setSize('5cm', '3cm')/}");
  }

  /**
   * Copy/paste of {@link TabPanelInfo} with {@link WidgetInfo}s.
   */
  public void test_clipboard_Panel() throws Exception {
    final ComplexPanelInfo frame =
        parseJavaInfo(
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "    {",
            "      TabPanel panel = new TabPanel();",
            "      {",
            "        Button buttonA = new Button('A');",
            "        panel.add(buttonA, 'Header A', false);",
            "      }",
            "      {",
            "        Button buttonB = new Button('B');",
            "        panel.add(buttonB, 'Header B', false);",
            "      }",
            "      add(panel);",
            "    }",
            "  }",
            "}");
    refresh();
    //
    {
      TabPanelInfo panel = getJavaInfoByName("panel");
      doCopyPaste(panel, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          frame.command_CREATE2(copy, null);
        }
      });
    }
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      TabPanel panel = new TabPanel();",
        "      {",
        "        Button buttonA = new Button('A');",
        "        panel.add(buttonA, 'Header A', false);",
        "      }",
        "      {",
        "        Button buttonB = new Button('B');",
        "        panel.add(buttonB, 'Header B', false);",
        "      }",
        "      add(panel);",
        "    }",
        "    {",
        "      TabPanel tabPanel = new TabPanel();",
        "      add(tabPanel);",
        "      {",
        "        Button button = new Button('A');",
        "        tabPanel.add(button, 'Header A', false);",
        "        button.setSize('5cm', '3cm');",
        "      }",
        "      {",
        "        Button button = new Button('B');",
        "        tabPanel.add(button, 'Header B', false);",
        "        button.setSize('5cm', '3cm');",
        "      }",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.FlowPanel} {this} {/add(panel)/ /add(tabPanel)/}",
        "  {new: com.google.gwt.user.client.ui.TabPanel} {local-unique: panel} {/new TabPanel()/ /panel.add(buttonA, 'Header A', false)/ /panel.add(buttonB, 'Header B', false)/ /add(panel)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: buttonA} {/new Button('A')/ /panel.add(buttonA, 'Header A', false)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: buttonB} {/new Button('B')/ /panel.add(buttonB, 'Header B', false)/}",
        "  {new: com.google.gwt.user.client.ui.TabPanel} {local-unique: tabPanel} {/new TabPanel()/ /add(tabPanel)/ /tabPanel.add(button, 'Header A', false)/ /tabPanel.add(button, 'Header B', false)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button('A')/ /tabPanel.add(button, 'Header A', false)/ /button.setSize('5cm', '3cm')/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button('B')/ /tabPanel.add(button, 'Header B', false)/ /button.setSize('5cm', '3cm')/}");
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