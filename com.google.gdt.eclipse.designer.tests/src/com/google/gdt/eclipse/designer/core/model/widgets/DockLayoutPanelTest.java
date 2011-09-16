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
import com.google.gdt.eclipse.designer.model.widgets.panels.DockLayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootLayoutPanelInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.StringListPropertyEditor;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.DblValue;
import org.eclipse.wb.tests.designer.Expectations.IntValue;

import static org.fest.assertions.Assertions.assertThat;

import org.fest.assertions.Delta;

/**
 * Test for {@link DockLayoutPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class DockLayoutPanelTest extends GwtModelTest {
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
  public void test_parse_ClassInstanceCreation() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    DockLayoutPanel panel = new DockLayoutPanel(Unit.PX);",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    assertHierarchy(
        "{RootLayoutPanel.get()} {local-unique: rootPanel} {/RootLayoutPanel.get()/ /rootPanel.add(panel)/}",
        "  {new: com.google.gwt.user.client.ui.DockLayoutPanel} {local-unique: panel} {/new DockLayoutPanel(Unit.PX)/ /rootPanel.add(panel)/}");
    frame.refresh();
    DockLayoutPanelInfo panel = getJavaInfoByName("panel");
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

  public void test_parse_this() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 2.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // bounds
    assertEquals(new Rectangle(0, 0, 450, 300), panel.getBounds());
    {
      WidgetInfo button = getJavaInfoByName("button");
      Rectangle bounds = button.getBounds();
      int widthGreatThat =
          Expectations.get(75, new IntValue[]{new IntValue("flanker-windows", 70)});
      assertThat(bounds.width).isGreaterThan(widthGreatThat);
      assertThat(bounds.height).isEqualTo(300);
    }
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

  public void test_CREATE_it() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "  }",
            "}");
    frame.refresh();
    //
    DockLayoutPanelInfo panel = createJavaInfo("com.google.gwt.user.client.ui.DockLayoutPanel");
    frame.command_CREATE2(panel, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.EM);",
        "      rootPanel.add(dockLayoutPanel);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Change "Unit" property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When "Unit" is changing, we should change size values accordingly.
   */
  public void test_propertyUnit_changeHorizontal() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 2.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    Property unitProperty = panel.getPropertyByTitle("Unit");
    setPropertyText(unitProperty, "MM");
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.MM);",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 20.0);",
        "    }",
        "  }",
        "}");
  }

  /**
   * When "Unit" is changing, we should change size values accordingly.
   */
  public void test_propertyUnit_changeVertical() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.MM);",
            "    {",
            "      Button button = new Button();",
            "      addNorth(button, 25.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    Property unitProperty = panel.getPropertyByTitle("Unit");
    setPropertyText(unitProperty, "CM");
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button = new Button();",
        "      addNorth(button, 2.5);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getEdge() setEdge() 
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DockLayoutPanelInfo#getEdge(WidgetInfo)}.
   */
  public void test_getEdge() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "    {",
            "      Button button_1 = new Button();",
            "      addWest(button_1, 1.0);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      addNorth(button_2, 1.0);",
            "    }",
            "    {",
            "      Button button_3 = new Button();",
            "      addEast(button_3, 1.0);",
            "    }",
            "    {",
            "      Button button_4 = new Button();",
            "      addSouth(button_4, 1.0);",
            "    }",
            "    {",
            "      Button button_5 = new Button();",
            "      add(button_5);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    {
      WidgetInfo button_1 = getJavaInfoByName("button_1");
      assertEquals("WEST", panel.getEdge(button_1));
      assertTrue(panel.isHorizontalEdge(button_1));
      assertFalse(panel.isVerticalEdge(button_1));
    }
    {
      WidgetInfo button_2 = getJavaInfoByName("button_2");
      assertEquals("NORTH", panel.getEdge(button_2));
      assertFalse(panel.isHorizontalEdge(button_2));
      assertTrue(panel.isVerticalEdge(button_2));
    }
    {
      WidgetInfo button_3 = getJavaInfoByName("button_3");
      assertEquals("EAST", panel.getEdge(button_3));
      assertTrue(panel.isHorizontalEdge(button_3));
      assertFalse(panel.isVerticalEdge(button_3));
    }
    {
      WidgetInfo button_4 = getJavaInfoByName("button_4");
      assertEquals("SOUTH", panel.getEdge(button_4));
      assertFalse(panel.isHorizontalEdge(button_4));
      assertTrue(panel.isVerticalEdge(button_4));
    }
    {
      WidgetInfo button_5 = getJavaInfoByName("button_5");
      assertEquals("CENTER", panel.getEdge(button_5));
      assertFalse(panel.isHorizontalEdge(button_5));
      assertFalse(panel.isVerticalEdge(button_5));
    }
  }

  /**
   * Test for {@link DockLayoutPanelInfo#setEdge(WidgetInfo, String)}.
   */
  public void test_setEdge_betweenSides() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 1.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    panel.setEdge(button, "NORTH");
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button = new Button();",
        "      addNorth(button, 1.0);",
        "    }",
        "  }",
        "}");
    assertEquals("NORTH", panel.getEdge(button));
  }

  /**
   * Test for {@link DockLayoutPanelInfo#setEdge(WidgetInfo, String)}.
   */
  public void test_setEdge_sideToCenter() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 1.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    panel.setEdge(button, "CENTER");
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertEquals("CENTER", panel.getEdge(button));
  }

  /**
   * Test for {@link DockLayoutPanelInfo#setEdge(WidgetInfo, String)}.
   */
  public void test_setEdge_centerToSide() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.MM);",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    panel.setEdge(button, "WEST");
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.MM);",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 10.0);",
        "    }",
        "  }",
        "}");
    assertEquals("WEST", panel.getEdge(button));
  }

  public void test_setEdge_whenHasCenter_alreadyBeforeCenter() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "    {",
            "      Button button_1 = new Button();",
            "      addWest(button_1, 1.0);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      addEast(button_2, 1.0);",
            "    }",
            "    {",
            "      Button center = new Button();",
            "      add(center);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button_1");
    panel.setEdge(button, "NORTH");
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button_1 = new Button();",
        "      addNorth(button_1, 1.0);",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      addEast(button_2, 1.0);",
        "    }",
        "    {",
        "      Button center = new Button();",
        "      add(center);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for decorating {@link WidgetInfo} presentation text.
   */
  public void test_decorateWidgetText() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "    {",
            "      Button button_1 = new Button();",
            "      addWest(button_1, 1.0);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      addNorth(button_2, 1.0);",
            "    }",
            "    {",
            "      Button button_3 = new Button();",
            "      addEast(button_3, 1.0);",
            "    }",
            "    {",
            "      Button button_4 = new Button();",
            "      addSouth(button_4, 1.0);",
            "    }",
            "    {",
            "      Button button_5 = new Button();",
            "      add(button_5);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    {
      WidgetInfo button_1 = getJavaInfoByName("button_1");
      String text = ObjectsLabelProvider.INSTANCE.getText(button_1);
      assertThat(text).startsWith("WEST - ");
    }
    {
      WidgetInfo button_2 = getJavaInfoByName("button_2");
      String text = ObjectsLabelProvider.INSTANCE.getText(button_2);
      assertThat(text).startsWith("NORTH - ");
    }
    {
      WidgetInfo button_3 = getJavaInfoByName("button_3");
      String text = ObjectsLabelProvider.INSTANCE.getText(button_3);
      assertThat(text).startsWith("EAST - ");
    }
    {
      WidgetInfo button_4 = getJavaInfoByName("button_4");
      String text = ObjectsLabelProvider.INSTANCE.getText(button_4);
      assertThat(text).startsWith("SOUTH - ");
    }
    {
      WidgetInfo button_5 = getJavaInfoByName("button_5");
      String text = ObjectsLabelProvider.INSTANCE.getText(button_5);
      assertThat(text).startsWith("CENTER - ");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property "Edge"
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for "Edge" property, contributed to child {@link WidgetInfo}.
   */
  public void test_propertyEdge() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 1.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    WidgetInfo button = getJavaInfoByName("button");
    // prepare "Edge" property
    Property property = button.getPropertyByTitle("Edge");
    assertNotNull(property);
    assertEquals("WEST", getPropertyText(property));
    assertInstanceOf(StringListPropertyEditor.class, property.getEditor());
    // set value
    property.setValue("NORTH");
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button = new Button();",
        "      addNorth(button, 1.0);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getSize()  setSize()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DockLayoutPanelInfo#getSize(WidgetInfo)}.
   */
  public void test_getSize() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.MM);",
            "    {",
            "      Button button_1 = new Button();",
            "      addWest(button_1, 1.0);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      addNorth(button_2, 2.0);",
            "    }",
            "    {",
            "      Button button_3 = new Button();",
            "      addEast(button_3, 3.0);",
            "    }",
            "    {",
            "      Button button_4 = new Button();",
            "      addSouth(button_4, 4.0);",
            "    }",
            "    {",
            "      Button button_5 = new Button();",
            "      add(button_5);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    {
      WidgetInfo button_1 = getJavaInfoByName("button_1");
      assertEquals(1.0, getSize(button_1), 0.001);
    }
    {
      WidgetInfo button_2 = getJavaInfoByName("button_2");
      assertEquals(2.0, getSize(button_2), 0.001);
    }
    {
      WidgetInfo button_3 = getJavaInfoByName("button_3");
      assertEquals(3.0, getSize(button_3), 0.001);
    }
    {
      WidgetInfo button_4 = getJavaInfoByName("button_4");
      assertEquals(4.0, getSize(button_4), 0.001);
    }
    {
      WidgetInfo button_5 = getJavaInfoByName("button_5");
      assertSame(null, getSize(button_5));
    }
  }

  /**
   * Test for {@link DockLayoutPanelInfo#getSize(WidgetInfo)}.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?45071
   */
  public void test_getSize_integer() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.MM);",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 20);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    assertEquals(20.0, getSize(button), 0.001);
  }

  /**
   * Test for {@link DockLayoutPanelInfo#setSize(WidgetInfo, double)}.
   */
  public void test_setSize() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 1.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    setSize(button, 2.52);
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 2.5);",
        "    }",
        "  }",
        "}");
  }

  private static Double getSize(WidgetInfo widget) {
    return (Double) ReflectionUtils.invokeMethodEx(
        widget.getParent(),
        "getSize(com.google.gdt.eclipse.designer.model.widgets.WidgetInfo)",
        widget);
  }

  private static Double setSize(WidgetInfo widget, double size) {
    return (Double) ReflectionUtils.invokeMethodEx(
        widget.getParent(),
        "setSize(com.google.gdt.eclipse.designer.model.widgets.WidgetInfo,double)",
        widget,
        size);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getSizeInUnits()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DockLayoutPanelInfo#getSizeInUnits(int, boolean)}.
   */
  public void test_getSizeInUnits_PX() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.PX);",
            "  }",
            "}");
    panel.refresh();
    //
    double units = panel.getSizeInUnits(100, false);
    assertThat(units).isEqualTo(100.0, Delta.delta(0.001));
  }

  /**
   * Test for {@link DockLayoutPanelInfo#getSizeInUnits(int, boolean)}.
   */
  public void test_getSizeInUnits_CM() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "  }",
            "}");
    panel.refresh();
    //
    double units = panel.getSizeInUnits(100, false);
    assertThat(units).isGreaterThan(2.0).isLessThan(3.0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getUnitSizeTooltip()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DockLayoutPanelInfo#getUnitSizeTooltip(double)}.
   */
  public void test_getUnitSizeTooltip_PX() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.PX);",
            "  }",
            "}");
    panel.refresh();
    //
    assertEquals("100.0px", panel.getUnitSizeTooltip(100.0));
  }

  /**
   * Test for {@link DockLayoutPanelInfo#getUnitSizeTooltip(double)}.
   */
  public void test_getUnitSizeTooltip_CM() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "  }",
            "}");
    panel.refresh();
    //
    assertEquals("3.5cm", panel.getUnitSizeTooltip(3.512));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Reasonable size
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DockLayoutPanelInfo#setReasonableSize(WidgetInfo)}.
   */
  public void test_setReasonableSize_PX() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.PX);",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 0.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    panel.setReasonableSize(button);
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.PX);",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 100.0);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link DockLayoutPanelInfo#setReasonableSize(WidgetInfo)}.
   */
  public void test_setReasonableSize_CM() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 0.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    panel.setReasonableSize(button);
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 1.0);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link DockLayoutPanelInfo#setReasonableSize(WidgetInfo)}.
   */
  public void test_setReasonableSize_MM() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.MM);",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 0.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    panel.setReasonableSize(button);
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.MM);",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 10.0);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link DockLayoutPanelInfo#setReasonableSize(WidgetInfo)}.
   */
  public void test_setReasonableSize_IN() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.IN);",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 0.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    panel.setReasonableSize(button);
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.IN);",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 1.0);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link DockLayoutPanelInfo#getReasonableSize(WidgetInfo)}.
   */
  public void test_getReasonableSize_PT() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.PT);",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 0.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    double size =
        (Double) ReflectionUtils.invokeMethod(
            panel,
            "getReasonableSize(com.google.gdt.eclipse.designer.model.widgets.WidgetInfo)",
            button);
    double lessThat =
        Expectations.get(75.0, new DblValue[]{
            new DblValue("scheglov_ke", 75.0),
            new DblValue("flanker-windows", 75.0)});
    assertThat(size).isGreaterThan(60.0).isLessThan(lessThat);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "  }",
            "}");
    panel.refresh();
    //
    WidgetInfo newButton = createButton();
    panel.command_CREATE2(newButton, null);
    panel.setEdge(newButton, "WEST");
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 1.0);",
        "    }",
        "  }",
        "}");
  }

  /**
   * New widget should be added before CENTER, but after other edges.
   */
  public void test_CREATE_whenHasCenter() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "    {",
            "      Button east = new Button();",
            "      addWest(east, 1.0);",
            "    }",
            "    {",
            "      Button center = new Button();",
            "      add(center);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    WidgetInfo newButton = createButton();
    panel.command_CREATE2(newButton, null);
    panel.setEdge(newButton, "WEST");
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button east = new Button();",
        "      addWest(east, 1.0);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      addWest(button, 1.0);",
        "    }",
        "    {",
        "      Button center = new Button();",
        "      add(center);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DockLayoutPanel {",
            "  public Test() {",
            "    super(Unit.CM);",
            "    {",
            "      Button button_1 = new Button();",
            "      addWest(button_1, 1.0);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      addNorth(button_2, 2.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button_1 = getJavaInfoByName("button_1");
    WidgetInfo button_2 = getJavaInfoByName("button_2");
    //
    panel.command_MOVE2(button_2, button_1);
    assertEditor(
        "public class Test extends DockLayoutPanel {",
        "  public Test() {",
        "    super(Unit.CM);",
        "    {",
        "      Button button_2 = new Button();",
        "      addNorth(button_2, 2.0);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      addWest(button_1, 1.0);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard() throws Exception {
    final RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      DockLayoutPanel panel = new DockLayoutPanel(Unit.CM);",
            "      rootPanel.add(panel);",
            "      {",
            "        Button button = new Button();",
            "        panel.addWest(button, 1.0);",
            "      }",
            "      {",
            "        Button button = new Button();",
            "        panel.addNorth(button, 2.0);",
            "      }",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    {
      DockLayoutPanelInfo panel = getJavaInfoByName("panel");
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
        "      DockLayoutPanel panel = new DockLayoutPanel(Unit.CM);",
        "      rootPanel.add(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.addWest(button, 1.0);",
        "      }",
        "      {",
        "        Button button = new Button();",
        "        panel.addNorth(button, 2.0);",
        "      }",
        "    }",
        "    {",
        "      DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.CM);",
        "      rootPanel.add(dockLayoutPanel);",
        "      {",
        "        Button button = new Button();",
        "        dockLayoutPanel.addWest(button, 1.0);",
        "      }",
        "      {",
        "        Button button = new Button();",
        "        dockLayoutPanel.addNorth(button, 2.0);",
        "      }",
        "    }",
        "  }",
        "}");
  }
}