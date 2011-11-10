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

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link DeckPanelInfo} widget.
 * 
 * @author scheglov_ke
 */
public class DeckPanelTest extends UiBinderModelTest {
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
   * Even empty <code>DockPanel</code> should have some reasonable size.
   */
  public void test_empty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DeckPanel wbp:name='panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    DeckPanelInfo panel = getObjectByName("panel");
    // bounds
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.width).isGreaterThan(130);
      assertThat(bounds.height).isGreaterThan(20);
    }
  }

  public void test_flowContainers() throws Exception {
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DeckPanel/>",
            "</ui:UiBinder>");
    assertHasWidgetFlowContainer(panel, true);
    assertHasWidgetFlowContainer(panel, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Active widget
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Use {@link DeckPanelInfo#showWidget(WidgetInfo)} property to show required widgets.
   */
  public void test_showWidget_internal_1() throws Exception {
    DeckPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DeckPanel>",
            "    <g:Button wbp:name='button_1'/>",
            "    <g:Button wbp:name='button_2'/>",
            "  </g:DeckPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    // by default "button_1" is displayed
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // show "button_2"
    panel.showWidget(button_2);
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * When delete some widget, show widget "0" or nothing.
   */
  public void test_showWidget_internal_2() throws Exception {
    DeckPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DeckPanel>",
            "    <g:Button wbp:name='button_1'/>",
            "    <g:Button wbp:name='button_2'/>",
            "  </g:DeckPanel>",
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
        "  <g:DeckPanel>",
        "    <g:Button wbp:name='button_1'/>",
        "  </g:DeckPanel>",
        "</ui:UiBinder>");
    assertTrue(isVisible(button_1));
    // delete "button_1", remove "showWidget()"
    button_1.delete();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DeckPanel/>",
        "</ui:UiBinder>");
  }

  /**
   * When move selected widget, it should still be selected.
   */
  public void test_showWidget_internal_3() throws Exception {
    DeckPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DeckPanel>",
            "    <g:Button wbp:name='button_1'/>",
            "    <g:Button wbp:name='button_2'/>",
            "    <g:Button wbp:name='button_3'/>",
            "  </g:DeckPanel>",
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
    panel.command_MOVE2(button_3, button_2);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DeckPanel>",
        "    <g:Button wbp:name='button_1'/>",
        "    <g:Button wbp:name='button_3'/>",
        "    <g:Button wbp:name='button_2'/>",
        "  </g:DeckPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertFalse(isVisible(button_2));
    assertTrue(isVisible(button_3));
  }

  /**
   * When select some widget in {@link DeckPanelInfo}, it should be displayed.
   */
  public void test_showWidget_internal_4() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DeckPanel>",
        "    <g:Button wbp:name='button_1'/>",
        "    <g:Button wbp:name='button_2'/>",
        "  </g:DeckPanel>",
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
        "  <g:DeckPanel>",
        "    <g:Button wbp:name='button_1'/>",
        "    <g:Button wbp:name='button_2'/>",
        "  </g:DeckPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * When select some widget that is indirect child of {@link DeckPanelInfo}, it should be
   * displayed.
   */
  public void test_showWidget_internal_5() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DeckPanel>",
        "    <g:Button wbp:name='button_1'/>",
        "    <g:FlowPanel wbp:name='flowPanel'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:FlowPanel>",
        "  </g:DeckPanel>",
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
   * When select some widget that is indirect child of {@link DeckPanelInfo}, it should be
   * displayed.
   * <p>
   * In this case selecting widget is already displayed, so no additional showing required.
   */
  public void test_showWidget_internal_6() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DeckPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:DeckPanel>",
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
        "  <g:DeckPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:DeckPanel>",
        "</ui:UiBinder>");
    assertTrue(isVisible(button));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    DeckPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DeckPanel>",
            "    <g:Button wbp:name='button_1'/>",
            "  </g:DeckPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    // initial state
    assertTrue(isVisible(button_1));
    // do CREATE
    WidgetInfo newButton = createButton();
    flowContainer_CREATE(panel, newButton, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DeckPanel>",
        "    <g:Button wbp:name='button_1'/>",
        "    <g:Button/>",
        "  </g:DeckPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(newButton));
  }

  public void test_MOVE_reorder() throws Exception {
    DeckPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DeckPanel>",
            "    <g:Button wbp:name='button_1'/>",
            "    <g:Button wbp:name='button_2'/>",
            "  </g:DeckPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    // initial state
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // do MOVE
    flowContainer_MOVE(panel, button_2, button_1);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DeckPanel>",
        "    <g:Button wbp:name='button_2'/>",
        "    <g:Button wbp:name='button_1'/>",
        "  </g:DeckPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  public void test_MOVE_reparent() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DeckPanel wbp:name='panel'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:DeckPanel>",
        "    <g:Button wbp:name='button_2'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    DeckPanelInfo panel = getObjectByName("panel");
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    // initial state
    assertTrue(isVisible(button_1));
    // do MOVE
    flowContainer_MOVE(panel, button_2, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DeckPanel wbp:name='panel'>",
        "      <g:Button wbp:name='button_1'/>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:DeckPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * Test for copy/paste {@link DeckPanelInfo} with its children.
   */
  public void test_clipboard() throws Exception {
    final ComplexPanelInfo flowPanel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:DeckPanel wbp:name='panel'>",
            "      <g:Button text='A'/>",
            "      <g:Button text='B'/>",
            "    </g:DeckPanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    {
      DeckPanelInfo panel = getObjectByName("panel");
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
        "    <g:DeckPanel wbp:name='panel'>",
        "      <g:Button text='A'/>",
        "      <g:Button text='B'/>",
        "    </g:DeckPanel>",
        "    <g:DeckPanel>",
        "      <g:Button text='A'/>",
        "      <g:Button text='B'/>",
        "    </g:DeckPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
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