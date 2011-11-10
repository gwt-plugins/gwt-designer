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
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.StringListPropertyEditor;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.IntValue;

import static org.fest.assertions.Assertions.assertThat;

import org.fest.assertions.Delta;

/**
 * Test for {@link DockLayoutPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class DockLayoutPanelTest extends UiBinderModelTest {
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
   * Even empty <code>DockLayoutPanel</code> should have one <code>Widget</code>.
   */
  public void test_empty() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DockLayoutPanel wbp:name='panel' width='150' height='100'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:FlowPanel>",
        "  <g:DockLayoutPanel wbp:name='panel' width='150' height='100'>");
    refresh();
    DockLayoutPanelInfo panel = getObjectByName("panel");
    // bounds
    assertEquals(new Rectangle(0, 0, 150, 100), panel.getBounds());
    // has widget
    assertEquals(1, ScriptUtils.evaluate("getWidgetCount()", panel.getObject()));
  }

  public void test_parse_this() throws Exception {
    DockLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'>",
            "    <g:west size='2.0'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:west>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:DockLayoutPanel unit='CM'>",
        "  <g:Button wbp:name='button'>");
    refresh();
    // "panel" bounds
    {
      Rectangle bounds = panel.getBounds();
      assertEquals(new Rectangle(0, 0, 450, 300), bounds);
    }
    // "button" bounds
    {
      WidgetInfo button = getObjectByName("button");
      Rectangle bounds = button.getBounds();
      int widthGreatThat =
          Expectations.get(75, new IntValue[]{new IntValue("flanker-windows", 70)});
      assertThat(bounds.width).isGreaterThan(widthGreatThat);
      assertThat(bounds.height).isEqualTo(300);
    }
  }

  public void test_CREATE_it() throws Exception {
    ComplexPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    DockLayoutPanelInfo panel = createObject("com.google.gwt.user.client.ui.DockLayoutPanel");
    frame.command_CREATE2(panel, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DockLayoutPanel unit='EM' width='150px' height='100px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Change "Unit" property
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_propertyUnit() throws Exception {
    DockLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel/>",
            "</ui:UiBinder>");
    refresh();
    // Unit property
    Property unitProperty = panel.getPropertyByTitle("Unit");
    assertNotNull(unitProperty);
    assertTrue(unitProperty.getCategory().isSystem());
    // default value
    assertFalse(unitProperty.isModified());
    assertEquals("PX", getPropertyText(unitProperty));
    // set value
    setPropertyText(unitProperty, "MM");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='MM'/>",
        "</ui:UiBinder>");
    assertTrue(unitProperty.isModified());
    assertEquals("MM", getPropertyText(unitProperty));
  }

  /**
   * When "Unit" is changing, we should change size values accordingly.
   */
  public void test_propertyUnit_changeHorizontal() throws Exception {
    DockLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'>",
            "    <g:west size='2.0'>",
            "      <g:Button/>",
            "    </g:west>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    Property unitProperty = panel.getPropertyByTitle("Unit");
    setPropertyText(unitProperty, "MM");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='MM'>",
        "    <g:west size='20.0'>",
        "      <g:Button/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  /**
   * When "Unit" is changing, we should change size values accordingly.
   */
  public void test_propertyUnit_changeVertical() throws Exception {
    DockLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='MM'>",
            "    <g:north size='25'>",
            "      <g:Button/>",
            "    </g:north>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    Property unitProperty = panel.getPropertyByTitle("Unit");
    setPropertyText(unitProperty, "CM");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:north size='2.5'>",
        "      <g:Button/>",
        "    </g:north>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
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
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'>",
            "    <g:west size='1.0'>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:west>",
            "    <g:north size='1.0'>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:north>",
            "    <g:east size='1.0'>",
            "      <g:Button wbp:name='button_3'/>",
            "    </g:east>",
            "    <g:south size='1.0'>",
            "      <g:Button wbp:name='button_4'/>",
            "    </g:south>",
            "    <g:center>",
            "      <g:Button wbp:name='button_5'/>",
            "    </g:center>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    {
      WidgetInfo button_1 = getObjectByName("button_1");
      assertEquals("WEST", panel.getEdge(button_1));
      assertTrue(panel.isHorizontalEdge(button_1));
      assertFalse(panel.isVerticalEdge(button_1));
    }
    {
      WidgetInfo button_2 = getObjectByName("button_2");
      assertEquals("NORTH", panel.getEdge(button_2));
      assertFalse(panel.isHorizontalEdge(button_2));
      assertTrue(panel.isVerticalEdge(button_2));
    }
    {
      WidgetInfo button_3 = getObjectByName("button_3");
      assertEquals("EAST", panel.getEdge(button_3));
      assertTrue(panel.isHorizontalEdge(button_3));
      assertFalse(panel.isVerticalEdge(button_3));
    }
    {
      WidgetInfo button_4 = getObjectByName("button_4");
      assertEquals("SOUTH", panel.getEdge(button_4));
      assertFalse(panel.isHorizontalEdge(button_4));
      assertTrue(panel.isVerticalEdge(button_4));
    }
    {
      WidgetInfo button_5 = getObjectByName("button_5");
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
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'>",
            "    <g:west size='1.0'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:west>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    panel.setEdge(button, "NORTH");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:north size='1.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:north>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    assertEquals("NORTH", panel.getEdge(button));
  }

  /**
   * Test for {@link DockLayoutPanelInfo#setEdge(WidgetInfo, String)}.
   */
  public void test_setEdge_sideToCenter() throws Exception {
    DockLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'>",
            "    <g:west size='1.0'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:west>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    panel.setEdge(button, "CENTER");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:center>",
        "      <g:Button wbp:name='button'/>",
        "    </g:center>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    assertEquals("CENTER", panel.getEdge(button));
  }

  /**
   * Test for {@link DockLayoutPanelInfo#setEdge(WidgetInfo, String)}.
   */
  public void test_setEdge_centerToSide() throws Exception {
    DockLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='MM'>",
            "    <g:center>",
            "      <g:Button wbp:name='button'/>",
            "    </g:center>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    panel.setEdge(button, "WEST");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='MM'>",
        "    <g:west size='10.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    assertEquals("WEST", panel.getEdge(button));
  }

  /**
   * Test for decorating {@link WidgetInfo} presentation text.
   */
  public void test_decorateWidgetText() throws Exception {
    parse(
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:west size='1.0'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:west>",
        "    <g:north size='1.0'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:north>",
        "    <g:east size='1.0'>",
        "      <g:Button wbp:name='button_3'/>",
        "    </g:east>",
        "    <g:south size='1.0'>",
        "      <g:Button wbp:name='button_4'/>",
        "    </g:south>",
        "    <g:center>",
        "      <g:Button wbp:name='button_5'/>",
        "    </g:center>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    refresh();
    {
      WidgetInfo button_1 = getObjectByName("button_1");
      String text = ObjectsLabelProvider.INSTANCE.getText(button_1);
      assertThat(text).startsWith("WEST - ");
    }
    {
      WidgetInfo button_2 = getObjectByName("button_2");
      String text = ObjectsLabelProvider.INSTANCE.getText(button_2);
      assertThat(text).startsWith("NORTH - ");
    }
    {
      WidgetInfo button_3 = getObjectByName("button_3");
      String text = ObjectsLabelProvider.INSTANCE.getText(button_3);
      assertThat(text).startsWith("EAST - ");
    }
    {
      WidgetInfo button_4 = getObjectByName("button_4");
      String text = ObjectsLabelProvider.INSTANCE.getText(button_4);
      assertThat(text).startsWith("SOUTH - ");
    }
    {
      WidgetInfo button_5 = getObjectByName("button_5");
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
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'>",
            "    <g:west size='1.0'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:west>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // prepare "Edge" property
    Property property = button.getPropertyByTitle("Edge");
    assertNotNull(property);
    assertEquals("WEST", getPropertyText(property));
    assertInstanceOf(StringListPropertyEditor.class, property.getEditor());
    // set value
    property.setValue("NORTH");
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:north size='1.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:north>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    assertEquals("NORTH", panel.getEdge(button));
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
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='MM'>",
        "    <g:west size='1.0'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:west>",
        "    <g:north size='2.0'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:north>",
        "    <g:east size='3.0'>",
        "      <g:Button wbp:name='button_3'/>",
        "    </g:east>",
        "    <g:south size='4.0'>",
        "      <g:Button wbp:name='button_4'/>",
        "    </g:south>",
        "    <g:center>",
        "      <g:Button wbp:name='button_5'/>",
        "    </g:center>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    refresh();
    {
      WidgetInfo button_1 = getObjectByName("button_1");
      assertEquals(1.0, getSize(button_1), 0.001);
    }
    {
      WidgetInfo button_2 = getObjectByName("button_2");
      assertEquals(2.0, getSize(button_2), 0.001);
    }
    {
      WidgetInfo button_3 = getObjectByName("button_3");
      assertEquals(3.0, getSize(button_3), 0.001);
    }
    {
      WidgetInfo button_4 = getObjectByName("button_4");
      assertEquals(4.0, getSize(button_4), 0.001);
    }
    {
      WidgetInfo button_5 = getObjectByName("button_5");
      assertSame(null, getSize(button_5));
    }
  }

  /**
   * Test for {@link DockLayoutPanelInfo#getSize(WidgetInfo)}.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?45071
   */
  public void test_getSize_integer() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='MM'>",
        "    <g:west size='20'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    double size = getSize(button);
    assertThat(size).isEqualTo(20.0, Delta.delta(0.001));
  }

  /**
   * Test for {@link DockLayoutPanelInfo#setSize(WidgetInfo, double)}.
   */
  public void test_setSize() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:west size='1.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    setSize(button, 2.52);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:west size='2.5'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  private static Double getSize(WidgetInfo widget) {
    String signature = "getSize(" + WidgetInfo.class.getName() + ")";
    return (Double) ReflectionUtils.invokeMethodEx(widget.getParent(), signature, widget);
  }

  private static void setSize(WidgetInfo widget, double size) {
    String signature = "setSize(" + WidgetInfo.class.getName() + ",double)";
    ReflectionUtils.invokeMethodEx(widget.getParent(), signature, widget, size);
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
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='PX'/>",
            "</ui:UiBinder>");
    refresh();
    //
    double units = panel.getSizeInUnits(100, false);
    assertThat(units).isEqualTo(100.0, Delta.delta(0.001));
  }

  /**
   * Test for {@link DockLayoutPanelInfo#getSizeInUnits(int, boolean)}.
   */
  public void test_getSizeInUnits_CM() throws Exception {
    DockLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'/>",
            "</ui:UiBinder>");
    refresh();
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
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='PX'/>",
            "</ui:UiBinder>");
    refresh();
    //
    assertEquals("100.0px", panel.getUnitSizeTooltip(100.0));
  }

  /**
   * Test for {@link DockLayoutPanelInfo#getUnitSizeTooltip(double)}.
   */
  public void test_getUnitSizeTooltip_CM() throws Exception {
    DockLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'/>",
            "</ui:UiBinder>");
    refresh();
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
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='PX'>",
            "    <g:west size='0.0'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:west>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    panel.setReasonableSize(button);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='PX'>",
        "    <g:west size='100.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link DockLayoutPanelInfo#setReasonableSize(WidgetInfo)}.
   */
  public void test_setReasonableSize_CM() throws Exception {
    DockLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'>",
            "    <g:west size='0.0'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:west>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    panel.setReasonableSize(button);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:west size='1.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link DockLayoutPanelInfo#setReasonableSize(WidgetInfo)}.
   */
  public void test_setReasonableSize_MM() throws Exception {
    DockLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='MM'>",
            "    <g:west size='0.0'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:west>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    panel.setReasonableSize(button);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='MM'>",
        "    <g:west size='10.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link DockLayoutPanelInfo#setReasonableSize(WidgetInfo)}.
   */
  public void test_setReasonableSize_IN() throws Exception {
    DockLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='IN'>",
            "    <g:west size='0.0'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:west>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    panel.setReasonableSize(button);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='IN'>",
        "    <g:west size='1.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Test for {@link DockLayoutPanelInfo#getReasonableSize(WidgetInfo)}.
   */
  public void test_getReasonableSize_PT() throws Exception {
    DockLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='PT'>",
            "    <g:west size='0.0'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:west>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    panel.setReasonableSize(button);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='PT'>",
        "    <g:west size='74.9'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    final DockLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='PX'/>",
            "</ui:UiBinder>");
    refresh();
    //
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        WidgetInfo newButton = createButton();
        panel.command_CREATE2(newButton, null);
        panel.setEdge(newButton, "NORTH");
        panel.setReasonableSize(newButton);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='PX'>",
        "    <g:north size='100.0'>",
        "      <g:Button/>",
        "    </g:north>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_MOVE() throws Exception {
    DockLayoutPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'>",
            "    <g:west size='1.0'>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:west>",
            "    <g:north size='2.0'>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:north>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    //
    flowContainer_MOVE(panel, button_2, button_1);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:north size='2.0'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:north>",
        "    <g:west size='1.0'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clipboard() throws Exception {
    final ComplexPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:DockLayoutPanel wbp:name='panel' unit='CM'>",
            "      <g:west size='1.0'>",
            "        <g:Button text='1'/>",
            "      </g:west>",
            "      <g:north size='2.0'>",
            "        <g:Button text='2'/>",
            "      </g:north>",
            "      <g:center>",
            "        <g:Button text='3'/>",
            "      </g:center>",
            "    </g:DockLayoutPanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    {
      DockLayoutPanelInfo panel = getObjectByName("panel");
      doCopyPaste(panel, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          flowContainer_CREATE(frame, copy, null);
        }
      });
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DockLayoutPanel wbp:name='panel' unit='CM'>",
        "      <g:west size='1.0'>",
        "        <g:Button text='1'/>",
        "      </g:west>",
        "      <g:north size='2.0'>",
        "        <g:Button text='2'/>",
        "      </g:north>",
        "      <g:center>",
        "        <g:Button text='3'/>",
        "      </g:center>",
        "    </g:DockLayoutPanel>",
        "    <g:DockLayoutPanel unit='CM'>",
        "      <g:west size='1.0'>",
        "        <g:Button text='1'/>",
        "      </g:west>",
        "      <g:north size='2.0'>",
        "        <g:Button text='2'/>",
        "      </g:north>",
        "      <g:center>",
        "        <g:Button text='3'/>",
        "      </g:center>",
        "    </g:DockLayoutPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }
}