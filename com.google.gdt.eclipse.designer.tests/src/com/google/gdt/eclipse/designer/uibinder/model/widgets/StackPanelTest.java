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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.StackPanelInfo.WidgetHandle;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link StackPanelInfo} widget.
 * 
 * @author scheglov_ke
 */
public class StackPanelTest extends UiBinderModelTest {
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
   * Even empty <code>StackPanel</code> should have some reasonable size.
   */
  public void test_empty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:StackPanel wbp:name='panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    StackPanelInfo panel = getObjectByName("panel");
    // bounds
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.width).isGreaterThan(130);
      assertThat(bounds.height).isGreaterThan(20);
    }
  }

  public void test_flowContainers() throws Exception {
    StackPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel/>",
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
    StackPanelInfo panel = createObject("com.google.gwt.user.client.ui.StackPanel");
    flowContainer_CREATE(flowPanel, panel, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:StackPanel/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // StackText property
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_StackText_existing() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button g:StackPanel-text='aaa' wbp:name='button'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = button.getPropertyByTitle("StackText");
    assertNotNull(property);
    assertTrue(property.getCategory().isSystem());
    assertTrue(property.isModified());
    assertEquals("aaa", property.getValue());
    // set new value
    property.setValue("aa");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button g:StackPanel-text='aa' wbp:name='button'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
    assertTrue(property.isModified());
    assertEquals("aa", property.getValue());
    // remove value
    property.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
    assertFalse(property.isModified());
    assertEquals(Property.UNKNOWN_VALUE, property.getValue());
  }

  public void test_StackText_new() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property property = button.getPropertyByTitle("StackText");
    assertNotNull(property);
    assertFalse(property.isModified());
    assertEquals(Property.UNKNOWN_VALUE, property.getValue());
    // set new value
    property.setValue("New text");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button' g:StackPanel-text='New text'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
    assertTrue(property.isModified());
    assertEquals("New text", property.getValue());
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
    StackPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel/>",
            "</ui:UiBinder>");
    refresh();
    assertThat(panel.getWidgetHandles()).isEmpty();
  }

  /**
   * Test for {@link StackPanelInfo#getWidgetHandles()}.
   * <p>
   * {@link WidgetHandle}'s should have some good bounds.
   */
  public void test_WidgetHandle_getBounds() throws Exception {
    StackPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel>",
            "    <g:Button wbp:name='button_1' g:StackPanel-text='First widget'/>",
            "    <g:Button wbp:name='button_2' g:StackPanel-text='Second widget'/>",
            "  </g:StackPanel>",
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
   * Test for {@link WidgetHandle#show()}.
   */
  public void test_WidgetHandle_show() throws Exception {
    StackPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel>",
            "    <g:Button wbp:name='button_1'/>",
            "    <g:Button wbp:name='button_2'/>",
            "  </g:StackPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    //
    panel.getWidgetHandles().get(1).show();
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Active widget
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Use {@link StackPanelInfo#showWidget(WidgetInfo)} property to show required widgets.
   */
  public void test_showWidget_internal_1() throws Exception {
    StackPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel>",
            "    <g:Button wbp:name='button_1'/>",
            "    <g:Button wbp:name='button_2'/>",
            "  </g:StackPanel>",
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
    StackPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel>",
            "    <g:Button wbp:name='button_1'/>",
            "    <g:Button wbp:name='button_2'/>",
            "  </g:StackPanel>",
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
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button_1'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
    assertTrue(isVisible(button_1));
    // delete "button_1", remove "showWidget()"
    button_1.delete();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel/>",
        "</ui:UiBinder>");
  }

  /**
   * When move selected widget, it should still be selected.
   */
  public void test_showWidget_internal_3() throws Exception {
    StackPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel>",
            "    <g:Button wbp:name='button_1'/>",
            "    <g:Button wbp:name='button_2'/>",
            "    <g:Button wbp:name='button_3'/>",
            "  </g:StackPanel>",
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
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button_1'/>",
        "    <g:Button wbp:name='button_3'/>",
        "    <g:Button wbp:name='button_2'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertFalse(isVisible(button_2));
    assertTrue(isVisible(button_3));
  }

  /**
   * When select some widget in {@link StackPanelInfo}, it should be displayed.
   */
  public void test_showWidget_internal_4() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button_1'/>",
        "    <g:Button wbp:name='button_2'/>",
        "  </g:StackPanel>",
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
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button_1'/>",
        "    <g:Button wbp:name='button_2'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * When select some widget that is indirect child of {@link StackPanelInfo}, it should be
   * displayed.
   */
  public void test_showWidget_internal_5() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button_1'/>",
        "    <g:FlowPanel wbp:name='flowPanel'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:FlowPanel>",
        "  </g:StackPanel>",
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
   * When select some widget that is indirect child of {@link StackPanelInfo}, it should be
   * displayed.
   * <p>
   * In this case selecting widget is already displayed, so no additional showing required.
   */
  public void test_showWidget_internal_6() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:StackPanel>",
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
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
    assertTrue(isVisible(button));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    StackPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel>",
            "    <g:Button wbp:name='button_1'/>",
            "  </g:StackPanel>",
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
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button_1'/>",
        "    <g:Button width='100%' height='100%' g:StackPanel-text='New widget'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(newButton));
  }

  public void test_MOVE_reorder() throws Exception {
    StackPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel>",
            "    <g:Button wbp:name='button_1' g:StackPanel-text='A'/>",
            "    <g:Button wbp:name='button_2' g:StackPanel-text='B'/>",
            "  </g:StackPanel>",
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
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button_2' g:StackPanel-text='B'/>",
        "    <g:Button wbp:name='button_1' g:StackPanel-text='A'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  public void test_MOVE_reparent() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:StackPanel wbp:name='panel'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:StackPanel>",
        "    <g:Button wbp:name='button_2'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    StackPanelInfo panel = getObjectByName("panel");
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
        "    <g:StackPanel wbp:name='panel'>",
        "      <g:Button wbp:name='button_1'/>",
        "      <g:Button wbp:name='button_2' width='100%' height='100%' g:StackPanel-text='New widget'/>",
        "    </g:StackPanel>",
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
            "    <g:StackPanel wbp:name='panel'>",
            "      <g:Button wbp:name='button' g:StackPanel-text='txt' width='100%' height='100%'/>",
            "    </g:StackPanel>",
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
        "    <g:StackPanel wbp:name='panel'/>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test for copy/paste {@link StackPanelInfo} with its children.
   */
  public void test_clipboard() throws Exception {
    final ComplexPanelInfo flowPanel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:StackPanel wbp:name='panel'>",
            "      <g:Button g:StackPanel-text='spA' text='A'/>",
            "      <g:Button text='B'/>",
            "    </g:StackPanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    {
      StackPanelInfo panel = getObjectByName("panel");
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
        "    <g:StackPanel wbp:name='panel'>",
        "      <g:Button g:StackPanel-text='spA' text='A'/>",
        "      <g:Button text='B'/>",
        "    </g:StackPanel>",
        "    <g:StackPanel>",
        "      <g:Button width='100%' height='100%' g:StackPanel-text='spA' text='A'/>",
        "      <g:Button width='100%' height='100%' text='B'/>",
        "    </g:StackPanel>",
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