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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.model.widgets.panels.AbstractWidgetHandle;
import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.TabPanelInfo.WidgetHandle;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link TabPanelInfo} widget.
 * 
 * @author scheglov_ke
 */
public class TabPanelTest extends UiBinderModelTest {
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
   * Even empty <code>TabPanel</code> should have one <code>Widget</code>.
   */
  public void test_empty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TabPanel wbp:name='panel' width='150' height='100'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    TabPanelInfo panel = getObjectByName("panel");
    // bounds
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.x).isEqualTo(0);
      assertThat(bounds.y).isEqualTo(0);
      assertThat(bounds.width).isEqualTo(150);
      assertThat(bounds.height).isGreaterThan(100);
    }
    // has widget
    assertEquals(1, ScriptUtils.evaluate("getWidgetCount()", panel.getObject()));
  }

  /**
   * Even empty <code>TabPanel</code> should have one <code>Widget</code>.
   */
  public void test_empty_this() throws Exception {
    TabPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel/>",
            "</ui:UiBinder>");
    refresh();
    // bounds
    assertEquals(new Rectangle(0, 0, 450, 300), panel.getBounds());
    // has widget
    assertEquals(1, ScriptUtils.evaluate("getWidgetCount()", panel.getObject()));
  }

  public void test_flowContainers() throws Exception {
    TabPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel/>",
            "</ui:UiBinder>");
    assertHasWidgetFlowContainer(panel, false);
  }

  public void test_CREATE_this() throws Exception {
    ComplexPanelInfo flowPanel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    TabPanelInfo panel = createObject("com.google.gwt.user.client.ui.TabPanel");
    flowContainer_CREATE(flowPanel, panel, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TabPanel/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_TabText() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = button.getPropertyByTitle("TabText");
    assertNotNull(property);
    assertTrue(property.getCategory().isSystem());
    assertTrue(property.isModified());
    assertEquals("AAA", property.getValue());
    // set new value
    property.setValue("AA");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='AA'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    assertTrue(property.isModified());
    assertEquals("AA", property.getValue());
    // remove value, ignored
    property.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='AA'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    assertTrue(property.isModified());
    assertEquals("AA", property.getValue());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Active widget
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Use {@link TabPanelInfo#showWidget(WidgetInfo)} property to show required widgets.
   */
  public void test_showWidget_internal_1() throws Exception {
    TabPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel>",
            "    <g:Tab text='AAA'>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:Tab>",
            "    <g:Tab text='BBB'>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:Tab>",
            "  </g:TabPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    // by default "button_1" is displayed
    assertSame(button_1, panel.getActiveWidget());
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // show "button_2"
    panel.showWidget(button_2);
    assertSame(button_2, panel.getActiveWidget());
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * When delete some widget, show widget "0" or nothing.
   */
  public void test_showWidget_internal_2() throws Exception {
    TabPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel>",
            "    <g:Tab text='AAA'>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:Tab>",
            "    <g:Tab text='BBB'>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:Tab>",
            "  </g:TabPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    // "button_2" is displayed
    panel.showWidget(button_2);
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
    // delete "button_2", so "button_1" should be displayed
    button_2.delete();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    assertTrue(isVisible(button_1));
    // delete "button_1", remove "showWidget()"
    button_1.delete();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel/>",
        "</ui:UiBinder>");
  }

  /**
   * When move selected widget, it should still be selected.
   */
  public void test_showWidget_internal_3() throws Exception {
    TabPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel>",
            "    <g:Tab text='AAA'>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:Tab>",
            "    <g:Tab text='BBB'>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:Tab>",
            "    <g:Tab text='CCC'>",
            "      <g:Button wbp:name='button_3'/>",
            "    </g:Tab>",
            "  </g:TabPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    WidgetInfo button_3 = getObjectByName("button_3");
    //
    panel.showWidget(button_3);
    assertFalse(isVisible(button_1));
    assertFalse(isVisible(button_2));
    assertTrue(isVisible(button_3));
    // move "button_3"
    flowContainer_MOVE(panel, button_3, button_2);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:Tab>",
        "    <g:Tab text='CCC'>",
        "      <g:Button wbp:name='button_3'/>",
        "    </g:Tab>",
        "    <g:Tab text='BBB'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertFalse(isVisible(button_2));
    assertTrue(isVisible(button_3));
  }

  /**
   * When select some widget in {@link TabPanelInfo}, it should be displayed.
   */
  public void test_showWidget_internal_4() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:Tab>",
        "    <g:Tab text='BBB'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    // initially "button_1" is visible
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // send "selecting" broadcast
    {
      boolean refreshFlag = notifySelecting(button_2);
      assertTrue(refreshFlag);
      refresh();
    }
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:Tab>",
        "    <g:Tab text='BBB'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * When select some widget that is indirect child of {@link TabPanelInfo}, it should be displayed.
   */
  public void test_showWidget_internal_5() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:Tab>",
        "    <g:Tab text='BBB'>",
        "      <g:FlowPanel wbp:name='flowPanel'>",
        "        <g:Button wbp:name='button_2'/>",
        "      </g:FlowPanel>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    ComplexPanelInfo flowPanel = getObjectByName("flowPanel");
    // initially "button_1" is visible
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(flowPanel));
    // send "selecting" broadcast
    {
      boolean refreshFlag = notifySelecting(button_2);
      assertTrue(refreshFlag);
      refresh();
    }
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(flowPanel));
  }

  /**
   * If selecting widget is already displayed, then no additional showing required.
   */
  public void test_showWidget_internal_6() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // already visible
    assertTrue(isVisible(button));
    // send "selecting" broadcast
    {
      boolean refreshFlag = notifySelecting(button);
      assertFalse(refreshFlag);
    }
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    assertTrue(isVisible(button));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    TabPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel>",
            "    <g:Tab text='AAA'>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:Tab>",
            "  </g:TabPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    // initial state
    assertTrue(isVisible(button_1));
    // do CREATE
    WidgetInfo newButton = createButton();
    panel.command_CREATE2(newButton, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:Tab>",
        "    <g:Tab text='New tab'>",
        "      <g:Button width='5cm' height='3cm'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(newButton));
  }

  public void test_MOVE_reorder() throws Exception {
    TabPanelInfo panel =
        parse(
            "<ui:UiBinder>",
            "  <g:TabPanel>",
            "    <g:Tab text='AAA'>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:Tab>",
            "    <g:Tab text='BBB'>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:Tab>",
            "  </g:TabPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    // initial state
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // do MOVE
    panel.command_MOVE2(button_2, button_1);
    assertXML(
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='BBB'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:Tab>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  public void test_MOVE_reparent() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TabPanel wbp:name='panel'>",
        "      <g:Tab text='AAA'>",
        "        <g:Button wbp:name='button_1'/>",
        "      </g:Tab>",
        "    </g:TabPanel>",
        "    <g:Button wbp:name='button_2'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    TabPanelInfo panel = getObjectByName("panel");
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    // initial state
    assertTrue(isVisible(button_1));
    // do MOVE
    panel.command_MOVE2(button_2, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TabPanel wbp:name='panel'>",
        "      <g:Tab text='AAA'>",
        "        <g:Button wbp:name='button_1'/>",
        "      </g:Tab>",
        "      <g:Tab text='New tab'>",
        "        <g:Button wbp:name='button_2' width='5cm' height='3cm'/>",
        "      </g:Tab>",
        "    </g:TabPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * When move from <code>StackPanel</code>, "text", "width" and "height" attributes should be
   * cleared.
   */
  public void test_MOVE_out() throws Exception {
    ComplexPanelInfo flowPanel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:TabPanel wbp:name='panel'>",
            "      <g:Tab text='AAA'>",
            "        <g:Button wbp:name='button' width='5cm' height='3cm'/>",
            "      </g:Tab>",
            "    </g:TabPanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // do MOVE
    flowContainer_MOVE(flowPanel, button, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TabPanel wbp:name='panel'/>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test for copy/paste {@link TabPanelInfo} with its children.
   */
  public void test_clipboard() throws Exception {
    final ComplexPanelInfo flowPanel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:TabPanel wbp:name='panel'>",
            "      <g:Tab text='AAA'>",
            "        <g:Button text='A'/>",
            "      </g:Tab>",
            "      <g:Tab text='BBB'>",
            "        <g:Button text='B'/>",
            "      </g:Tab>",
            "    </g:TabPanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    {
      TabPanelInfo panel = getObjectByName("panel");
      doCopyPaste(panel, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          flowContainer_CREATE(flowPanel, copy, null);
        }
      });
    }
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TabPanel wbp:name='panel'>",
        "      <g:Tab text='AAA'>",
        "        <g:Button text='A'/>",
        "      </g:Tab>",
        "      <g:Tab text='BBB'>",
        "        <g:Button text='B'/>",
        "      </g:Tab>",
        "    </g:TabPanel>",
        "    <g:TabPanel>",
        "      <g:Tab text='AAA'>",
        "        <g:Button width='5cm' height='3cm' text='A'/>",
        "      </g:Tab>",
        "      <g:Tab text='BBB'>",
        "        <g:Button width='5cm' height='3cm' text='B'/>",
        "      </g:Tab>",
        "    </g:TabPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
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
    TabPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    assertTrue(panel.getWidgetHandles().isEmpty());
  }

  /**
   * Test for {@link StackPanelInfo#getWidgetHandles()}.
   * <p>
   * {@link WidgetHandle}'s should have some good bounds.
   */
  public void test_WidgetHandle_getBounds() throws Exception {
    TabPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel>",
            "    <g:Tab text='First widget'>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:Tab>",
            "    <g:Tab text='Second widget'>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:Tab>",
            "  </g:TabPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
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
  public void test_WidgetHandle_show() throws Exception {
    TabPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel>",
            "    <g:Tab text='AAA'>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:Tab>",
            "    <g:Tab text='BBB'>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:Tab>",
            "  </g:TabPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    // initial state
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // show "button_2"
    panel.getWidgetHandles().get(1).show();
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
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
    return widget.getBounds().height != 0;
  }
}