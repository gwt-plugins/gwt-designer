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
import com.google.gdt.eclipse.designer.model.widgets.panels.StackLayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.StackLayoutPanelInfo.WidgetHandle;
import com.google.gdt.eclipse.designer.model.widgets.panels.StackPanelInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link StackLayoutPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class StackLayoutPanelTest extends GwtModelTest {
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
            "    StackLayoutPanel panel = new StackLayoutPanel(Unit.PX);",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
    StackLayoutPanelInfo panel = getJavaInfoByName("panel");
    // bounds
    assertEquals(new Rectangle(0, 0, 450, 300), panel.getBounds());
    // Unit property
    {
      GenericProperty unitProperty = (GenericProperty) panel.getPropertyByTitle("Unit");
      assertNotNull(unitProperty);
      {
        Class<?> type = unitProperty.getType();
        assertNotNull(type);
        assertEquals("com.google.gwt.dom.client.Style$Unit", type.getName());
      }
      assertTrue(unitProperty.getCategory().isSystem());
      assertEquals("PX", getPropertyText(unitProperty));
    }
  }

  public void test_parseEmpty_this() throws Exception {
    StackLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends StackLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // bounds
    assertEquals(new Rectangle(0, 0, 450, 300), panel.getBounds());
    // Unit property
    {
      GenericProperty unitProperty = (GenericProperty) panel.getPropertyByTitle("Unit");
      assertNotNull(unitProperty);
      {
        Class<?> type = unitProperty.getType();
        assertNotNull(type);
        assertEquals("com.google.gwt.dom.client.Style$Unit", type.getName());
      }
      assertTrue(unitProperty.getCategory().isSystem());
      assertEquals("CM", getPropertyText(unitProperty));
    }
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
    StackLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends StackLayoutPanel {",
            "  public Test() {",
            "    super(Unit.EM);",
            "  }",
            "}");
    panel.refresh();
    assertTrue(panel.getWidgetHandles().isEmpty());
  }

  /**
   * Test for {@link StackPanelInfo#getWidgetHandles()}.
   * <p>
   * {@link WidgetHandle}'s should have some good bounds.
   */
  public void test_WidgetHandle_getBounds() throws Exception {
    StackLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends StackLayoutPanel {",
            "  public Test() {",
            "    super(Unit.EM);",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, new HTML('First widget'), 1.0);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, new HTML('Second widget'), 1.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    //
    List<WidgetHandle> handles = panel.getWidgetHandles();
    assertThat(handles).hasSize(2);
    {
      WidgetHandle handle = handles.get(0);
      assertSame(button_1, handle.getWidget());
      Rectangle bounds = handle.getBounds();
      assertThat(bounds.x).isEqualTo(0);
      assertThat(bounds.y).isEqualTo(0);
      assertThat(bounds.width).isGreaterThan(390);
      assertThat(bounds.height).isGreaterThan(15);
    }
    {
      WidgetHandle handle = handles.get(1);
      assertSame(button_2, handle.getWidget());
      Rectangle bounds = handle.getBounds();
      assertThat(bounds.x).isEqualTo(0);
      assertThat(bounds.y).isGreaterThan(250);
      assertThat(bounds.width).isGreaterThan(390);
      assertThat(bounds.height).isGreaterThan(15);
    }
  }

  /**
   * Test for {@link StackPanelInfo#getWidgetHandles()}.
   * <p>
   * Test that unit "CM" also handled correctly. There was bug that we removed "fixedRuler" during
   * clearing <code>RootPanel</code>.
   */
  public void test_WidgetHandle_unitCM() throws Exception {
    dontUseSharedGWTState();
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      StackLayoutPanel panel = new StackLayoutPanel(Unit.CM);",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button, new HTML('My header'), 1.0);",
        "      }",
        "      rootPanel.add(panel);",
        "      panel.setSize('250px', '200px');",
        "    }",
        "  }",
        "}");
    refresh();
    StackLayoutPanelInfo panel = getJavaInfoByName("panel");
    //
    List<WidgetHandle> handles = panel.getWidgetHandles();
    assertThat(handles).hasSize(1);
    {
      WidgetHandle handle = handles.get(0);
      Rectangle bounds = handle.getBounds();
      assertThat(bounds.x).isEqualTo(0);
      assertThat(bounds.y).isEqualTo(0);
      assertThat(bounds.width).isEqualTo(250);
      assertThat(bounds.height).isGreaterThan(30);
    }
  }

  /**
   * Test for {@link AbstractWidgetHandle#show()}.
   */
  public void test_WidgetHandle_show() throws Exception {
    StackLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends StackLayoutPanel {",
            "  public Test() {",
            "    super(Unit.EM);",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, new HTML('First widget'), 1.0);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2, new HTML('Second widget'), 1.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    //
    panel.getWidgetHandles().get(1).show();
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  public void test_HeaderText_property() throws Exception {
    StackLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends StackLayoutPanel {",
            "  public Test() {",
            "    super(Unit.EM);",
            "    {",
            "      Button button = new Button();",
            "      add(button, new HTML('My widget'), 1.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    Property property = button.getPropertyByTitle("HeaderText");
    assertNotNull(property);
    assertTrue(property.getCategory().isSystem());
    assertEquals("My widget", property.getValue());
    // test that "HeaderText" is not considered as "text" property of "button"
    assertEquals("button", ObjectsLabelProvider.INSTANCE.getText(button));
    // set new value
    property.setValue("New text");
    assertEditor(
        "public class Test extends StackLayoutPanel {",
        "  public Test() {",
        "    super(Unit.EM);",
        "    {",
        "      Button button = new Button();",
        "      add(button, new HTML('New text'), 1.0);",
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
    StackLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends StackLayoutPanel {",
            "  public Test() {",
            "    super(Unit.EM);",
            "  }",
            "}");
    panel.refresh();
    //
    WidgetInfo newButton = createButton();
    panel.command_CREATE2(newButton, null);
    assertEditor(
        "public class Test extends StackLayoutPanel {",
        "  public Test() {",
        "    super(Unit.EM);",
        "    {",
        "      Button button = new Button();",
        "      add(button, new HTML('New Widget'), 2.0);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.StackLayoutPanel} {this} {/add(button, new HTML('New Widget'), 2.0)/}",
        "  {new: com.google.gwt.user.client.ui.Button empty} {local-unique: button} {/new Button()/ /add(button, new HTML('New Widget'), 2.0)/}",
        "    {new: com.google.gwt.user.client.ui.HTML} {empty} {/add(button, new HTML('New Widget'), 2.0)/}");
  }

  public void test_CREATE_local() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    StackLayoutPanel panel = new StackLayoutPanel(Unit.EM);",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    StackLayoutPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo newButton = createButton();
    panel.command_CREATE2(newButton, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    StackLayoutPanel panel = new StackLayoutPanel(Unit.EM);",
        "    {",
        "      Button button = new Button();",
        "      panel.add(button, new HTML('New Widget'), 2.0);",
        "    }",
        "    rootPanel.add(panel);",
        "  }",
        "}");
    assertHierarchy(
        "{RootLayoutPanel.get()} {local-unique: rootPanel} {/RootLayoutPanel.get()/ /rootPanel.add(panel)/}",
        "  {new: com.google.gwt.user.client.ui.StackLayoutPanel} {local-unique: panel} {/new StackLayoutPanel(Unit.EM)/ /rootPanel.add(panel)/ /panel.add(button, new HTML('New Widget'), 2.0)/}",
        "    {new: com.google.gwt.user.client.ui.Button empty} {local-unique: button} {/new Button()/ /panel.add(button, new HTML('New Widget'), 2.0)/}",
        "      {new: com.google.gwt.user.client.ui.HTML} {empty} {/panel.add(button, new HTML('New Widget'), 2.0)/}");
  }

  public void test_ADD_local() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      StackLayoutPanel panel = new StackLayoutPanel(Unit.EM);",
            "      rootPanel.add(panel);",
            "    }",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    StackLayoutPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo button = getJavaInfoByName("button");
    panel.command_MOVE2(button, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      StackLayoutPanel panel = new StackLayoutPanel(Unit.EM);",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button, new HTML('New Widget'), 2.0);",
        "      }",
        "      rootPanel.add(panel);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootLayoutPanel.get()} {local-unique: rootPanel} {/RootLayoutPanel.get()/ /rootPanel.add(panel)/}",
        "  {new: com.google.gwt.user.client.ui.StackLayoutPanel} {local-unique: panel} {/new StackLayoutPanel(Unit.EM)/ /rootPanel.add(panel)/ /panel.add(button, new HTML('New Widget'), 2.0)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /panel.add(button, new HTML('New Widget'), 2.0)/}",
        "      {new: com.google.gwt.user.client.ui.HTML} {empty} {/panel.add(button, new HTML('New Widget'), 2.0)/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Copy/paste of {@link WidgetInfo} on {@link StackLayoutPanelInfo}.
   */
  public void test_clipboard_Widget() throws Exception {
    final StackLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends StackLayoutPanel {",
            "  public Test() {",
            "    super(Unit.EM);",
            "    {",
            "      Button myButton = new Button();",
            "      add(myButton, new HTML('header'), 1.5);",
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
        "public class Test extends StackLayoutPanel {",
        "  public Test() {",
        "    super(Unit.EM);",
        "    {",
        "      Button myButton = new Button();",
        "      add(myButton, new HTML('header'), 1.5);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button, new HTML('New Widget'), 2.0);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.StackLayoutPanel} {this} {/add(myButton, new HTML('header'), 1.5)/ /add(button, new HTML('New Widget'), 2.0)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: myButton} {/new Button()/ /add(myButton, new HTML('header'), 1.5)/}",
        "    {new: com.google.gwt.user.client.ui.HTML} {empty} {/add(myButton, new HTML('header'), 1.5)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /add(button, new HTML('New Widget'), 2.0)/}",
        "    {new: com.google.gwt.user.client.ui.HTML} {empty} {/add(button, new HTML('New Widget'), 2.0)/}");
  }

  /**
   * Copy/paste of {@link StackLayoutPanelInfo} with {@link WidgetInfo}s.
   */
  public void test_clipboard_Panel() throws Exception {
    final RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      StackLayoutPanel panel = new StackLayoutPanel(Unit.EM);",
            "      {",
            "        Button buttonA = new Button('A');",
            "        panel.add(buttonA, new HTML('Header A'), 1.0);",
            "      }",
            "      {",
            "        Button buttonB = new Button('B');",
            "        panel.add(buttonB, new HTML('Header B'), 3.5);",
            "      }",
            "      rootPanel.add(panel);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    {
      StackLayoutPanelInfo panel = getJavaInfoByName("panel");
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
        "      StackLayoutPanel panel = new StackLayoutPanel(Unit.EM);",
        "      {",
        "        Button buttonA = new Button('A');",
        "        panel.add(buttonA, new HTML('Header A'), 1.0);",
        "      }",
        "      {",
        "        Button buttonB = new Button('B');",
        "        panel.add(buttonB, new HTML('Header B'), 3.5);",
        "      }",
        "      rootPanel.add(panel);",
        "    }",
        "    {",
        "      StackLayoutPanel stackLayoutPanel = new StackLayoutPanel(Unit.EM);",
        "      {",
        "        Button button = new Button('A');",
        "        stackLayoutPanel.add(button, new HTML('Header A'), 1.0);",
        "      }",
        "      {",
        "        Button button = new Button('B');",
        "        stackLayoutPanel.add(button, new HTML('Header B'), 3.5);",
        "      }",
        "      rootPanel.add(stackLayoutPanel);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{RootLayoutPanel.get()} {local-unique: rootPanel} {/RootLayoutPanel.get()/ /rootPanel.add(panel)/ /rootPanel.add(stackLayoutPanel)/}",
        "  {new: com.google.gwt.user.client.ui.StackLayoutPanel} {local-unique: panel} {/new StackLayoutPanel(Unit.EM)/ /panel.add(buttonA, new HTML('Header A'), 1.0)/ /panel.add(buttonB, new HTML('Header B'), 3.5)/ /rootPanel.add(panel)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: buttonA} {/new Button('A')/ /panel.add(buttonA, new HTML('Header A'), 1.0)/}",
        "      {new: com.google.gwt.user.client.ui.HTML} {empty} {/panel.add(buttonA, new HTML('Header A'), 1.0)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: buttonB} {/new Button('B')/ /panel.add(buttonB, new HTML('Header B'), 3.5)/}",
        "      {new: com.google.gwt.user.client.ui.HTML} {empty} {/panel.add(buttonB, new HTML('Header B'), 3.5)/}",
        "  {new: com.google.gwt.user.client.ui.StackLayoutPanel} {local-unique: stackLayoutPanel} {/new StackLayoutPanel(Unit.EM)/ /rootPanel.add(stackLayoutPanel)/ /stackLayoutPanel.add(button, new HTML('Header A'), 1.0)/ /stackLayoutPanel.add(button, new HTML('Header B'), 3.5)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button('A')/ /stackLayoutPanel.add(button, new HTML('Header A'), 1.0)/}",
        "      {new: com.google.gwt.user.client.ui.HTML} {empty} {/stackLayoutPanel.add(button, new HTML('Header A'), 1.0)/}",
        "    {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button('B')/ /stackLayoutPanel.add(button, new HTML('Header B'), 3.5)/}",
        "      {new: com.google.gwt.user.client.ui.HTML} {empty} {/stackLayoutPanel.add(button, new HTML('Header B'), 3.5)/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link WidgetInfo} is visible.
   */
  private boolean isVisible(WidgetInfo widget) throws Exception {
    return widget.getBounds().height != 0;
  }
}