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
import com.google.gdt.eclipse.designer.model.widgets.panels.RootLayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.TabLayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.TabLayoutPanelInfo.WidgetHandle;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link TabLayoutPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class TabLayoutPanelTest extends GwtModelTest {
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
  // Parse empty
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_parseEmpty_ClassInstanceCreation() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    TabLayoutPanel panel = new TabLayoutPanel(1.5, Unit.EM);",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    TabLayoutPanelInfo panel = getJavaInfoByName("panel");
    // bounds
    assertEquals(new Rectangle(0, 0, 450, 300), panel.getBounds());
  }

  public void test_parseEmpty_this() throws Exception {
    TabLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // bounds
    assertEquals(new Rectangle(0, 0, 450, 300), panel.getBounds());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If "handle" widget is added to {@link TabLayoutPanelInfo} just as secondary widget, to remember
   * reference, it does not become managed {@link WidgetInfo} for this {@link TabLayoutPanelInfo}
   * and should not be attempted to active.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48429
   */
  public void test_filterNonChildren() throws Exception {
    parseJavaInfo(
        "public class Test extends StackLayoutPanel {",
        "  public Test() {",
        "    super(Unit.EM);",
        "    {",
        "      TabLayoutPanel tabPanel = new TabLayoutPanel(1.5, Unit.EM);",
        "      add(tabPanel, new HTML('TabPanel'), 1.0);",
        "      {",
        "        Button button = new Button();",
        "        tabPanel.add(button, 'MyTab', false);",
        "      }",
        "    }",
        "  }",
        "}");
    refresh();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WidgetHandle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link TabLayoutPanelInfo#getWidgetHandles()}.
   * <p>
   * No widgets, no {@link WidgetHandle}'s.
   */
  public void test_WidgetHandle_empty() throws Exception {
    TabLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "  }",
            "}");
    panel.refresh();
    assertTrue(panel.getWidgetHandles().isEmpty());
  }

  /**
   * Test for {@link TabLayoutPanelInfo#getWidgetHandles()}.
   * <p>
   * {@link WidgetHandle}'s should have some good bounds.
   */
  public void test_WidgetHandle_getBounds() throws Exception {
    TabLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'ABC', false);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'ABCABC', false);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    //
    List<WidgetHandle> handles = panel.getWidgetHandles();
    assertThat(handles).hasSize(2);
    WidgetHandle handle_1 = handles.get(0);
    WidgetHandle handle_2 = handles.get(1);
    Rectangle bounds_1 = handle_1.getBounds();
    Rectangle bounds_2 = handle_2.getBounds();
    {
      assertSame(button_1, handle_1.getWidget());
      assertThat(bounds_1.x).isEqualTo(0);
      assertThat(bounds_1.y).isEqualTo(0);
      assertThat(bounds_1.width).isGreaterThan(40).isLessThan(60);
      assertThat(bounds_1.height).isGreaterThan(20);
    }
    {
      assertSame(button_2, handle_2.getWidget());
      assertThat(bounds_2.x).isEqualTo(bounds_1.right());
      assertThat(bounds_2.y).isEqualTo(0);
      assertThat(bounds_2.width).isGreaterThan(70).isLessThan(90);
      assertThat(bounds_2.height).isEqualTo(bounds_1.height);
    }
  }

  /**
   * Test for {@link AbstractWidgetHandle#show()}.
   */
  public void test_WidgetHandle_show() throws Exception {
    TabLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'ABC', false);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'ABCABC', false);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    assertTrue(isVisible(panel, button_1));
    assertFalse(isVisible(panel, button_2));
    //
    panel.getWidgetHandles().get(1).show();
    assertFalse(isVisible(panel, button_1));
    assertTrue(isVisible(panel, button_2));
  }

  public void test_TabText_property() throws Exception {
    TabLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "    {",
            "      Button button = new Button();",
            "      add(button, 'My widget', false);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    Property property = button.getPropertyByTitle("TabText");
    assertNotNull(property);
    assertTrue(property.getCategory().isSystem());
    assertEquals("My widget", property.getValue());
    // test that "TabText" is not considered as "text" property of "button"
    assertEquals("button", ObjectsLabelProvider.INSTANCE.getText(button));
    // set new value
    property.setValue("New text");
    assertEditor(
        "public class Test extends TabLayoutPanel {",
        "  public Test() {",
        "    super(1.5, Unit.EM);",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New text', false);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE_this() throws Exception {
    TabLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "  }",
            "}");
    panel.refresh();
    //
    WidgetInfo newButton = createButton();
    panel.command_CREATE2(newButton, null);
    assertEditor(
        "public class Test extends TabLayoutPanel {",
        "  public Test() {",
        "    super(1.5, Unit.EM);",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New Widget', false);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.TabLayoutPanel} {this} {/add(button, 'New Widget', false)/}",
        "  {new: com.google.gwt.user.client.ui.Button empty} {local-unique: button} {/new Button()/ /add(button, 'New Widget', false)/}");
  }

  public void test_CREATE_local() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    TabLayoutPanel panel = new TabLayoutPanel(1.5, Unit.EM);",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    TabLayoutPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo newButton = createButton();
    panel.command_CREATE2(newButton, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    TabLayoutPanel panel = new TabLayoutPanel(1.5, Unit.EM);",
        "    {",
        "      Button button = new Button();",
        "      panel.add(button, 'New Widget', false);",
        "    }",
        "    rootPanel.add(panel);",
        "  }",
        "}");
    assertHierarchy(
        "{RootLayoutPanel.get()} {local-unique: rootPanel} {/RootLayoutPanel.get()/ /rootPanel.add(panel)/}",
        "  {new: com.google.gwt.user.client.ui.TabLayoutPanel} {local-unique: panel} {/new TabLayoutPanel(1.5, Unit.EM)/ /rootPanel.add(panel)/ /panel.add(button, 'New Widget', false)/}",
        "    {new: com.google.gwt.user.client.ui.Button empty} {local-unique: button} {/new Button()/ /panel.add(button, 'New Widget', false)/}");
  }

  public void test_ADD_local() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      TabLayoutPanel panel = new TabLayoutPanel(1.5, Unit.EM);",
            "      rootPanel.add(panel);",
            "    }",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    TabLayoutPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo button = getJavaInfoByName("button");
    panel.command_MOVE2(button, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      TabLayoutPanel panel = new TabLayoutPanel(1.5, Unit.EM);",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button, 'New Widget', false);",
        "      }",
        "      rootPanel.add(panel);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootLayoutPanel.get()} {local-unique: rootPanel} {/RootLayoutPanel.get()/ /rootPanel.add(panel)/}",
        "  {new: com.google.gwt.user.client.ui.TabLayoutPanel} {local-unique: panel} {/new TabLayoutPanel(1.5, Unit.EM)/ /rootPanel.add(panel)/ /panel.add(button, 'New Widget', false)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /panel.add(button, 'New Widget', false)/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Copy/paste of {@link WidgetInfo} on {@link TabLayoutPanelInfo}.
   */
  public void test_clipboard_Widget() throws Exception {
    final TabLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
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
        "public class Test extends TabLayoutPanel {",
        "  public Test() {",
        "    super(1.5, Unit.EM);",
        "    {",
        "      Button myButton = new Button();",
        "      add(myButton, 'tab', false);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button, 'New Widget', false);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.TabLayoutPanel} {this} {/add(myButton, 'tab', false)/ /add(button, 'New Widget', false)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: myButton} {/new Button()/ /add(myButton, 'tab', false)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /add(button, 'New Widget', false)/}");
  }

  /**
   * Copy/paste of {@link TabLayoutPanelInfo} with {@link WidgetInfo}s.
   */
  public void test_clipboard_Panel() throws Exception {
    final RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      TabLayoutPanel panel = new TabLayoutPanel(2.4, Unit.EM);",
            "      {",
            "        Button buttonA = new Button('A');",
            "        panel.add(buttonA, 'Header A', false);",
            "      }",
            "      {",
            "        Button buttonB = new Button('B');",
            "        panel.add(buttonB, 'Header B', false);",
            "      }",
            "      rootPanel.add(panel);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    {
      TabLayoutPanelInfo panel = getJavaInfoByName("panel");
      doCopyPaste(panel, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          frame.command_CREATE2(copy, null);
        }
      });
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      TabLayoutPanel panel = new TabLayoutPanel(2.4, Unit.EM);",
        "      {",
        "        Button buttonA = new Button('A');",
        "        panel.add(buttonA, 'Header A', false);",
        "      }",
        "      {",
        "        Button buttonB = new Button('B');",
        "        panel.add(buttonB, 'Header B', false);",
        "      }",
        "      rootPanel.add(panel);",
        "    }",
        "    {",
        "      TabLayoutPanel tabLayoutPanel = new TabLayoutPanel(2.4, Unit.EM);",
        "      {",
        "        Button button = new Button('A');",
        "        tabLayoutPanel.add(button, 'Header A', false);",
        "      }",
        "      {",
        "        Button button = new Button('B');",
        "        tabLayoutPanel.add(button, 'Header B', false);",
        "      }",
        "      rootPanel.add(tabLayoutPanel);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootLayoutPanel.get()} {local-unique: rootPanel} {/RootLayoutPanel.get()/ /rootPanel.add(panel)/ /rootPanel.add(tabLayoutPanel)/}",
        "  {new: com.google.gwt.user.client.ui.TabLayoutPanel} {local-unique: panel} {/new TabLayoutPanel(2.4, Unit.EM)/ /panel.add(buttonA, 'Header A', false)/ /panel.add(buttonB, 'Header B', false)/ /rootPanel.add(panel)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: buttonA} {/new Button('A')/ /panel.add(buttonA, 'Header A', false)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: buttonB} {/new Button('B')/ /panel.add(buttonB, 'Header B', false)/}",
        "  {new: com.google.gwt.user.client.ui.TabLayoutPanel} {local-unique: tabLayoutPanel} {/new TabLayoutPanel(2.4, Unit.EM)/ /rootPanel.add(tabLayoutPanel)/ /tabLayoutPanel.add(button, 'Header A', false)/ /tabLayoutPanel.add(button, 'Header B', false)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button('A')/ /tabLayoutPanel.add(button, 'Header A', false)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button('B')/ /tabLayoutPanel.add(button, 'Header B', false)/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Manage active
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_manageActive() throws Exception {
    TabLayoutPanelInfo container =
        parseJavaInfo(
            "public class Test extends TabLayoutPanel {",
            "  public Test() {",
            "    super(1.5, Unit.EM);",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, 'A', false);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, 'B', false);",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    // initially "button_1" is active
    assertActiveWidget(container, button_1);
    // notify about "button_2"
    {
      boolean shouldRefresh = notifySelecting(button_2);
      assertTrue(shouldRefresh);
      container.refresh();
      // now "button_2" is active
      assertActiveWidget(container, button_2);
    }
    // second notification about "button_2" does not cause refresh()
    {
      boolean shouldRefresh = notifySelecting(button_2);
      assertFalse(shouldRefresh);
    }
  }

  private static void assertActiveWidget(TabLayoutPanelInfo panel, WidgetInfo expected)
      throws Exception {
    assertSame(expected, panel.getActiveWidget());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link WidgetInfo} is visible.
   */
  private boolean isVisible(TabLayoutPanelInfo panel, WidgetInfo widget) throws Exception {
    return panel.getActiveWidget() == widget;
  }
}