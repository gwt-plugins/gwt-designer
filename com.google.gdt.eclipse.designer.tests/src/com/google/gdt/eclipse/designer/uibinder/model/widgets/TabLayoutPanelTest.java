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

import com.google.gdt.eclipse.designer.model.widgets.panels.AbstractWidgetHandle;
import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.TabLayoutPanelInfo.WidgetHandle;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.ITypedProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link TabLayoutPanelInfo} widget.
 * 
 * @author scheglov_ke
 */
public class TabLayoutPanelTest extends UiBinderModelTest {
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
   * Even empty <code>TabLayoutPanel</code> should have one <code>Widget</code>.
   */
  public void test_empty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TabLayoutPanel wbp:name='panel' barHeight='2.0' barUnit='EM' width='150px' height='100px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    TabLayoutPanelInfo panel = getObjectByName("panel");
    // bounds
    assertEquals(new Rectangle(0, 0, 150, 100), panel.getBounds());
    // has widget
    assertEquals(1, ScriptUtils.evaluate("getWidgetCount()", panel.getObject()));
  }

  /**
   * Even empty <code>TabLayoutPanel</code> should have one <code>Widget</code>.
   */
  public void test_empty_this() throws Exception {
    TabLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabLayoutPanel barHeight='2.0' barUnit='EM'/>",
            "</ui:UiBinder>");
    refresh();
    // bounds
    assertEquals(new Rectangle(0, 0, 450, 300), panel.getBounds());
    // has widget
    assertEquals(1, ScriptUtils.evaluate("getWidgetCount()", panel.getObject()));
  }

  public void test_flowContainers() throws Exception {
    TabLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabLayoutPanel barHeight='2.0' barUnit='EM'/>",
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
    TabLayoutPanelInfo panel = createObject("com.google.gwt.user.client.ui.TabLayoutPanel");
    flowContainer_CREATE(flowPanel, panel, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TabLayoutPanel barHeight='2' barUnit='EM' width='250px' height='150px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_barUnitProperty() throws Exception {
    TabLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabLayoutPanel barHeight='2.0' barUnit='EM'/>",
            "</ui:UiBinder>");
    refresh();
    // prepare property
    Property property = panel.getPropertyByTitle("barUnit");
    assertNotNull(property);
    assertTrue(property.getCategory().isSystem());
    assertTrue(property.isModified());
    assertEquals("EM", getPropertyText(property));
    // check type
    Class<?> classUnit = m_lastLoader.loadClass("com.google.gwt.dom.client.Style$Unit");
    assertSame(classUnit, ((ITypedProperty) property).getType());
    // set new value
    property.setValue(ReflectionUtils.getFieldObject(classUnit, "CM"));
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabLayoutPanel barHeight='2.0' barUnit='CM'/>",
        "</ui:UiBinder>");
    assertTrue(property.isModified());
    assertEquals("CM", getPropertyText(property));
    // remove value, ignored
    property.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabLayoutPanel barHeight='2.0' barUnit='CM'/>",
        "</ui:UiBinder>");
    assertTrue(property.isModified());
    assertEquals("CM", getPropertyText(property));
  }

  public void test_barHeightProperty() throws Exception {
    TabLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabLayoutPanel barHeight='2.0' barUnit='EM'/>",
            "</ui:UiBinder>");
    refresh();
    // prepare property
    Property property = panel.getPropertyByTitle("barHeight");
    assertNotNull(property);
    assertTrue(property.getCategory().isSystem());
    assertTrue(property.isModified());
    assertEquals("2.0", getPropertyText(property));
    // set new value, as Integer
    property.setValue(Integer.valueOf(3));
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabLayoutPanel barHeight='3' barUnit='EM'/>",
        "</ui:UiBinder>");
    assertTrue(property.isModified());
    assertEquals("3.0", getPropertyText(property));
    // set new value, as Double
    property.setValue(Double.valueOf(2.1));
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabLayoutPanel barHeight='2.1' barUnit='EM'/>",
        "</ui:UiBinder>");
    assertTrue(property.isModified());
    assertEquals("2.1", getPropertyText(property));
    // remove value, ignored
    property.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabLayoutPanel barHeight='2.1' barUnit='EM'/>",
        "</ui:UiBinder>");
    assertTrue(property.isModified());
    assertEquals("2.1", getPropertyText(property));
  }

  public void test_TabText() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "    <g:tab>",
        "      <g:header>AAA</g:header>",
        "      <g:Button wbp:name='button'/>",
        "    </g:tab>",
        "  </g:TabLayoutPanel>",
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
        "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "    <g:tab>",
        "      <g:header>AA</g:header>",
        "      <g:Button wbp:name='button'/>",
        "    </g:tab>",
        "  </g:TabLayoutPanel>",
        "</ui:UiBinder>");
    assertTrue(property.isModified());
    assertEquals("AA", property.getValue());
    // remove value, ignored
    property.setValue(Property.UNKNOWN_VALUE);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "    <g:tab>",
        "      <g:header>AA</g:header>",
        "      <g:Button wbp:name='button'/>",
        "    </g:tab>",
        "  </g:TabLayoutPanel>",
        "</ui:UiBinder>");
    assertTrue(property.isModified());
    assertEquals("AA", property.getValue());
  }

  /**
   * We should support custom headers too.
   */
  public void test_customHeader() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "    <g:tab>",
        "      <g:customHeader size='7'>",
        "        <g:Label>Advanced Search</g:Label>",
        "      </g:customHeader>",
        "      <g:Button wbp:name='button'/>",
        "    </g:tab>",
        "  </g:TabLayoutPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "  <g:Button wbp:name='button'>",
        "    <g:Label>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // uses "customHeader", so no "TabText" property
    {
      Property property = button.getPropertyByTitle("TabText");
      assertNull(property);
    }
    // no changes it hierarchy
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "  <g:Button wbp:name='button'>",
        "    <g:Label>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Active widget
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Use {@link TabLayoutPanelInfo#showWidget(WidgetInfo)} property to show required widgets.
   */
  public void test_showWidget_internal_1() throws Exception {
    TabLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
            "    <g:tab>",
            "      <g:header>AAA</g:header>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:tab>",
            "    <g:tab>",
            "      <g:header>BBB</g:header>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:tab>",
            "  </g:TabLayoutPanel>",
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
    TabLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
            "    <g:tab>",
            "      <g:header>AAA</g:header>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:tab>",
            "    <g:tab>",
            "      <g:header>BBB</g:header>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:tab>",
            "  </g:TabLayoutPanel>",
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
        "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "    <g:tab>",
        "      <g:header>AAA</g:header>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:tab>",
        "  </g:TabLayoutPanel>",
        "</ui:UiBinder>");
    assertTrue(isVisible(button_1));
    // delete "button_1", remove "showWidget()"
    button_1.delete();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabLayoutPanel barHeight='2' barUnit='EM'/>",
        "</ui:UiBinder>");
  }

  /**
   * When move selected widget, it should still be selected.
   */
  public void test_showWidget_internal_3() throws Exception {
    TabLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
            "    <g:tab>",
            "      <g:header>AAA</g:header>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:tab>",
            "    <g:tab>",
            "      <g:header>BBB</g:header>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:tab>",
            "    <g:tab>",
            "      <g:header>CCC</g:header>",
            "      <g:Button wbp:name='button_3'/>",
            "    </g:tab>",
            "  </g:TabLayoutPanel>",
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
        "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "    <g:tab>",
        "      <g:header>AAA</g:header>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:tab>",
        "    <g:tab>",
        "      <g:header>CCC</g:header>",
        "      <g:Button wbp:name='button_3'/>",
        "    </g:tab>",
        "    <g:tab>",
        "      <g:header>BBB</g:header>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:tab>",
        "  </g:TabLayoutPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertFalse(isVisible(button_2));
    assertTrue(isVisible(button_3));
  }

  /**
   * When select some widget in {@link TabLayoutPanelInfo}, it should be displayed.
   */
  public void test_showWidget_internal_4() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "    <g:tab>",
        "      <g:header>AAA</g:header>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:tab>",
        "    <g:tab>",
        "      <g:header>BBB</g:header>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:tab>",
        "  </g:TabLayoutPanel>",
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
        "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "    <g:tab>",
        "      <g:header>AAA</g:header>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:tab>",
        "    <g:tab>",
        "      <g:header>BBB</g:header>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:tab>",
        "  </g:TabLayoutPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  /**
   * When select some widget that is indirect child of {@link TabLayoutPanelInfo}, it should be
   * displayed.
   */
  public void test_showWidget_internal_5() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "    <g:tab>",
        "      <g:header>AAA</g:header>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:tab>",
        "    <g:tab>",
        "      <g:header>BBB</g:header>",
        "      <g:FlowPanel wbp:name='flowPanel'>",
        "        <g:Button wbp:name='button_2'/>",
        "      </g:FlowPanel>",
        "    </g:tab>",
        "  </g:TabLayoutPanel>",
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
        "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "    <g:tab>",
        "      <g:header>AAA</g:header>",
        "      <g:Button wbp:name='button'/>",
        "    </g:tab>",
        "  </g:TabLayoutPanel>",
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
        "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "    <g:tab>",
        "      <g:header>AAA</g:header>",
        "      <g:Button wbp:name='button'/>",
        "    </g:tab>",
        "  </g:TabLayoutPanel>",
        "</ui:UiBinder>");
    assertTrue(isVisible(button));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    TabLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
            "    <g:tab>",
            "      <g:header>AAA</g:header>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:tab>",
            "  </g:TabLayoutPanel>",
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
        "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "    <g:tab>",
        "      <g:header>AAA</g:header>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:tab>",
        "    <g:tab>",
        "      <g:header>New tab</g:header>",
        "      <g:Button/>",
        "    </g:tab>",
        "  </g:TabLayoutPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(newButton));
  }

  public void test_MOVE_reorder() throws Exception {
    TabLayoutPanelInfo panel =
        parse(
            "<ui:UiBinder>",
            "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
            "    <g:tab>",
            "      <g:header>AAA</g:header>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:tab>",
            "    <g:tab>",
            "      <g:header>BBB</g:header>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:tab>",
            "  </g:TabLayoutPanel>",
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
        "<ui:UiBinder>",
        "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
        "    <g:tab>",
        "      <g:header>BBB</g:header>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:tab>",
        "    <g:tab>",
        "      <g:header>AAA</g:header>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:tab>",
        "  </g:TabLayoutPanel>",
        "</ui:UiBinder>");
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  public void test_MOVE_reparent() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TabLayoutPanel wbp:name='panel' barHeight='2' barUnit='EM' width='250px' height='200px'>",
        "      <g:tab>",
        "        <g:header>AAA</g:header>",
        "        <g:Button wbp:name='button_1'/>",
        "      </g:tab>",
        "    </g:TabLayoutPanel>",
        "    <g:Button wbp:name='button_2'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    TabLayoutPanelInfo panel = getObjectByName("panel");
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
        "    <g:TabLayoutPanel wbp:name='panel' barHeight='2' barUnit='EM' width='250px' height='200px'>",
        "      <g:tab>",
        "        <g:header>AAA</g:header>",
        "        <g:Button wbp:name='button_1'/>",
        "      </g:tab>",
        "      <g:tab>",
        "        <g:header>New tab</g:header>",
        "        <g:Button wbp:name='button_2'/>",
        "      </g:tab>",
        "    </g:TabLayoutPanel>",
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
            "    <g:TabLayoutPanel wbp:name='panel' barHeight='2' barUnit='EM'>",
            "      <g:tab>",
            "        <g:header>AAA</g:header>",
            "        <g:Button wbp:name='button'/>",
            "      </g:tab>",
            "    </g:TabLayoutPanel>",
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
        "    <g:TabLayoutPanel wbp:name='panel' barHeight='2' barUnit='EM'/>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test for copy/paste {@link TabLayoutPanelInfo} with its children.
   */
  public void test_clipboard() throws Exception {
    final ComplexPanelInfo flowPanel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:TabLayoutPanel wbp:name='panel' barHeight='1' barUnit='CM'>",
            "      <g:tab>",
            "        <g:header>AAA</g:header>",
            "        <g:Button text='A'/>",
            "      </g:tab>",
            "      <g:tab>",
            "        <g:header>BBB</g:header>",
            "        <g:Button text='B'/>",
            "      </g:tab>",
            "    </g:TabLayoutPanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    {
      TabLayoutPanelInfo panel = getObjectByName("panel");
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
        "    <g:TabLayoutPanel wbp:name='panel' barHeight='1' barUnit='CM'>",
        "      <g:tab>",
        "        <g:header>AAA</g:header>",
        "        <g:Button text='A'/>",
        "      </g:tab>",
        "      <g:tab>",
        "        <g:header>BBB</g:header>",
        "        <g:Button text='B'/>",
        "      </g:tab>",
        "    </g:TabLayoutPanel>",
        "    <g:TabLayoutPanel barHeight='1' barUnit='CM'>",
        "      <g:tab>",
        "        <g:header>AAA</g:header>",
        "        <g:Button text='A'/>",
        "      </g:tab>",
        "      <g:tab>",
        "        <g:header>BBB</g:header>",
        "        <g:Button text='B'/>",
        "      </g:tab>",
        "    </g:TabLayoutPanel>",
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
    TabLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabLayoutPanel barHeight='2' barUnit='EM'/>",
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
    TabLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
            "    <g:tab>",
            "      <g:header>AAA</g:header>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:tab>",
            "    <g:tab>",
            "      <g:header>BBB</g:header>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:tab>",
            "  </g:TabLayoutPanel>",
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
      assertEquals(new Rectangle(0, 9, 45, 27), bounds);
    }
    {
      WidgetHandle handle = handles.get(1);
      assertSame(button_2, handle.getWidget());
      Rectangle bounds = handle.getBounds();
      assertEquals(new Rectangle(45, 9, 42, 27), bounds);
    }
  }

  /**
   * Test for {@link AbstractWidgetHandle#show()}.
   */
  public void test_WidgetHandle_show() throws Exception {
    TabLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabLayoutPanel barHeight='2' barUnit='EM'>",
            "    <g:tab>",
            "      <g:header>AAA</g:header>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:tab>",
            "    <g:tab>",
            "      <g:header>BBB</g:header>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:tab>",
            "  </g:TabLayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    // initial state
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    //
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
    return widget.getBounds().height > 5;
  }
}