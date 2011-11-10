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

import com.google.gdt.eclipse.designer.model.widgets.panels.ILayoutPanelInfo.Anchor;
import com.google.gdt.eclipse.designer.model.widgets.panels.ILayoutPanelInfo.ResizeDirection;
import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.xml.model.TopBoundsSupport;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.StrValue;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test {@link LayoutLayoutPanel_Info}.
 * 
 * @author scheglov_ke
 */
public class LayoutPanelTest extends UiBinderModelTest {
  private static final String MM_100 = Expectations.get("26.5", new StrValue[]{
      new StrValue("scheglov-win", "26.5"),
      new StrValue("flanker-windows", "26.5"),
      new StrValue("sablin-aa", "26.5")});
  private static final String MM_50 = Expectations.get("13.2", new StrValue[]{
      new StrValue("scheglov-win", "13.2"),
      new StrValue("flanker-windows", "13.2"),
      new StrValue("sablin-aa", "13.2")});
  private static final String CM_100 = Expectations.get("2.6", new StrValue[]{
      new StrValue("scheglov-win", "2.6"),
      new StrValue("flanker-windows", "2.6"),
      new StrValue("sablin-aa", "2.6")});
  private static final String EX_100 = Expectations.get("11.1", new StrValue[]{
      new StrValue("scheglov-win", "11.1"),
      new StrValue("flanker-windows", "11.1"),
      new StrValue("sablin-aa", "11.1")});
  private static final String W_MM_after_50MM_150PX = Expectations.get("29.4", new StrValue[]{
      new StrValue("scheglov-win", "29.4"),
      new StrValue("flanker-windows", "29.4")});
  private static final String H_MM_after_50MM_50PX = Expectations.get("16.1", new StrValue[]{
      new StrValue("scheglov-win", "16.1"),
      new StrValue("flanker-windows", "16.1")});

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
  public void test_empty() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel/>",
            "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:LayoutPanel>");
    refresh();
    // default bounds
    assertEquals(new Rectangle(0, 0, 450, 300), frame.getBounds());
    // set new size
    {
      TopBoundsSupport topBoundsSupport = frame.getTopBoundsSupport();
      topBoundsSupport.setSize(500, 400);
      refresh();
      assertEquals(new Rectangle(0, 0, 500, 400), frame.getBounds());
    }
  }

  public void test_withButton() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='10px' right='20px' top='30px' bottom='40px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<g:LayoutPanel>",
        "  <g:Button wbp:name='button'>");
    refresh();
    // bounds
    WidgetInfo button = getObjectByName("button");
    {
      Rectangle expected = new Rectangle(10, 30, 450 - 10 - 20, 300 - 30 - 40);
      assertEquals(expected, button.getBounds());
      assertEquals(expected, button.getModelBounds());
    }
  }

  /**
   * Test for <code>Order</code> actions.
   */
  public void test_contextMenu_order() throws Exception {
    parse(
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='10px' width='100px' top='10px' height='10px'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:layer>",
        "    <g:layer left='10px' width='100px' top='30px' height='10px'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:layer>",
        "    <g:layer left='10px' width='100px' top='50px' height='10px'>",
        "      <g:Button wbp:name='button_3'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button_3 = getObjectByName("button_3");
    // prepare action
    IAction action;
    {
      IMenuManager allManager = getContextMenu(button_3);
      IMenuManager orderManager = findChildMenuManager(allManager, "Order");
      action = findChildAction(orderManager, "Bring to Front");
      assertNotNull(action);
    }
    // run action
    action.run();
    assertXML(
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='10px' width='100px' top='50px' height='10px'>",
        "      <g:Button wbp:name='button_3'/>",
        "    </g:layer>",
        "    <g:layer left='10px' width='100px' top='10px' height='10px'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:layer>",
        "    <g:layer left='10px' width='100px' top='30px' height='10px'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_out() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='10px' right='10px' top='30px' height='100px'>",
        "      <g:FlowPanel wbp:name='panel'/>",
        "    </g:layer>",
        "    <g:layer left='10px' width='150px' top='150px' height='50px'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:layer>",
        "    <g:layer left='200px' width='150px' top='150px' height='50px'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    refresh();
    ComplexPanelInfo panel = getObjectByName("panel");
    WidgetInfo button_1 = getObjectByName("button_1");
    //
    panel.command_MOVE2(button_1, null);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='10px' right='10px' top='30px' height='100px'>",
        "      <g:FlowPanel wbp:name='panel'>",
        "        <g:Button wbp:name='button_1'/>",
        "      </g:FlowPanel>",
        "    </g:layer>",
        "    <g:layer left='200px' width='150px' top='150px' height='50px'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link LayoutPanelInfo#getLocationHint(WidgetInfo, int, int)}.
   */
  public void test_getLocationHint() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer>",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "    <g:layer left='1.0mm' width='1.0cm' top='2.0px' height='1.0cm'>",
            "      <g:Button wbp:name='button_LW_TH'/>",
            "    </g:layer>",
            "    <g:layer left='1.0mm' right='1.0cm' top='2.0cm' bottom='1.0cm'>",
            "      <g:Button wbp:name='button_LR_TB'/>",
            "    </g:layer>",
            "    <g:layer right='10px' width='100px' bottom='20px' height='50px'>",
            "      <g:Button wbp:name='button_RW_BH'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    {
      WidgetInfo button = getObjectByName("button");
      assertEquals("100.0px x 50.0px", frame.getLocationHint(button, 100, 50));
      assertEquals(false, frame.getLocationHint_isTrailing(button, true));
      assertEquals(false, frame.getLocationHint_isTrailing(button, false));
    }
    {
      WidgetInfo button = getObjectByName("button_LW_TH");
      assertEquals(MM_100 + "mm" + " x 50.0px", frame.getLocationHint(button, 100, 50));
      assertEquals(false, frame.getLocationHint_isTrailing(button, true));
      assertEquals(false, frame.getLocationHint_isTrailing(button, false));
    }
    {
      WidgetInfo button = getObjectByName("button_LR_TB");
      assertEquals(MM_50 + "mm" + " x " + CM_100 + "cm", frame.getLocationHint(button, 50, 100));
      assertEquals(false, frame.getLocationHint_isTrailing(button, true));
      assertEquals(false, frame.getLocationHint_isTrailing(button, false));
    }
    {
      WidgetInfo button = getObjectByName("button_RW_BH");
      assertEquals(
          "80.0px x 40.0px",
          frame.getLocationHint(button, 450 - (100 + 80), 300 - (50 + 40)));
      assertEquals(true, frame.getLocationHint_isTrailing(button, true));
      assertEquals(true, frame.getLocationHint_isTrailing(button, false));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LOCATION
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_LOCATION_new() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer>",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    WidgetInfo button = getObjectByName("button");
    button.getBounds().setSize(150, 50);
    frame.command_LOCATION(button, new Point(10, 20));
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='10px' width='150px' top='20px' height='50px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_LOCATION_update_LeftTop_PX() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer left='1px' width='1.0cm' top='2px' height='1.0cm'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    WidgetInfo button = getObjectByName("button");
    frame.command_LOCATION(button, new Point(10, 20));
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='10px' width='1.0cm' top='20px' height='1.0cm'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_LOCATION_update_LeftTop_MM() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer left='1.0mm' width='1.0cm' top='2.0mm' height='1.0cm'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    WidgetInfo button = getObjectByName("button");
    frame.command_LOCATION(button, new Point(100, 50));
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='" + MM_100 + "mm' width='1.0cm' top='" + MM_50 + "mm' height='1.0cm'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_LOCATION_update_RightBottom_PX() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer right='1px' width='100px' bottom='2px' height='50px'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    WidgetInfo box = getObjectByName("button");
    frame.startEdit();
    frame.command_LOCATION(box, new Point(200, 150));
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer right='150px' width='100px' bottom='100px' height='50px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_LOCATION_update_LeftRight_TopBottom_PX() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer left='100px' right='200px' top='100px' bottom='150px'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    WidgetInfo box = getObjectByName("button");
    frame.command_LOCATION(box, new Point(200, 150));
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='200px' right='100px' top='150px' bottom='100px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SIZE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_SIZE_new() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer>",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    WidgetInfo button = getObjectByName("button");
    frame.command_SIZE(
        button,
        new Dimension(200, 50),
        ResizeDirection.TRAILING,
        ResizeDirection.TRAILING);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='0px' width='200px' top='0px' height='50px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_SIZE_update_LeftWidth_TopHeight_TT_PX() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer left='0px' width='100px' top='0px' height='50px'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    WidgetInfo button = getObjectByName("button");
    frame.command_SIZE(
        button,
        new Dimension(150, 75),
        ResizeDirection.TRAILING,
        ResizeDirection.TRAILING);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='0px' width='150px' top='0px' height='75px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_SIZE_update_LeftWidth_TopHeight_TT_MM() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer left='0px' width='10.0mm' top='0px' height='10.0mm'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    WidgetInfo button = getObjectByName("button");
    frame.command_SIZE(
        button,
        new Dimension(100, 50),
        ResizeDirection.TRAILING,
        ResizeDirection.TRAILING);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='0px' width='" + MM_100 + "mm' top='0px' height='" + MM_50 + "mm'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_SIZE_update_LeftWidth_TopHeight_LL_PX() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer left='10px' width='100px' top='20px' height='50px'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    WidgetInfo button = getObjectByName("button");
    frame.command_SIZE(
        button,
        new Dimension(90, 40),
        ResizeDirection.LEADING,
        ResizeDirection.LEADING);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='20px' width='90px' top='30px' height='40px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_SIZE_update_RightWidth_BottomHeight_TT_PX() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer right='100px' width='100px' bottom='100px' height='50px'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    WidgetInfo button = getObjectByName("button");
    frame.command_SIZE(
        button,
        new Dimension(150, 75),
        ResizeDirection.TRAILING,
        ResizeDirection.TRAILING);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer right='50px' width='150px' bottom='75px' height='75px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_SIZE_update_RightWidth_BottomHeight_LL_PX() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer right='100px' width='100px' bottom='100px' height='50px'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    WidgetInfo button = getObjectByName("button");
    frame.command_SIZE(
        button,
        new Dimension(150, 75),
        ResizeDirection.LEADING,
        ResizeDirection.LEADING);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer right='100px' width='150px' bottom='100px' height='75px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_SIZE_update_LeftRight_TopBottom_LL_PX() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer left='100px' right='100px' top='50px' bottom='50px'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    WidgetInfo button = getObjectByName("button");
    frame.command_SIZE(
        button,
        new Dimension(150, 100),
        ResizeDirection.LEADING,
        ResizeDirection.LEADING);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='200px' right='100px' top='150px' bottom='50px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_SIZE_update_LeftRight_TopBottom_TT_PX() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer left='100px' right='100px' top='50px' bottom='50px'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    //
    WidgetInfo button = getObjectByName("button");
    frame.command_SIZE(
        button,
        new Dimension(150, 100),
        ResizeDirection.TRAILING,
        ResizeDirection.TRAILING);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='100px' right='200px' top='50px' bottom='150px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ANCHOR: horizontal
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ANCHOR_N_to_N() throws Exception {
    assertEquals("none", Anchor.NONE.getTitle(true));
    check_ANCHOR_horizontal(Anchor.NONE, "", Anchor.NONE, "");
  }

  public void test_ANCHOR_N_to_LW() throws Exception {
    check_ANCHOR_horizontal(Anchor.NONE, "", Anchor.LEADING, "left='0px' width='450px'");
  }

  public void test_ANCHOR_N_to_RW() throws Exception {
    check_ANCHOR_horizontal(Anchor.NONE, "", Anchor.TRAILING, "right='0px' width='450px'");
  }

  public void test_ANCHOR_N_to_LR() throws Exception {
    check_ANCHOR_horizontal(Anchor.NONE, "", Anchor.BOTH, "left='0px' right='0px'");
  }

  public void test_ANCHOR_LW_to_N() throws Exception {
    assertEquals("left + width", Anchor.LEADING.getTitle(true));
    check_ANCHOR_horizontal(Anchor.LEADING, "left='100px' width='150px'", Anchor.NONE, "");
  }

  public void test_ANCHOR_LW_to_RW() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.LEADING,
        "left='100px' width='150px'",
        Anchor.TRAILING,
        "width='150px' right='200px'");
  }

  public void test_ANCHOR_LW_to_LR() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.LEADING,
        "left='100px' width='150px'",
        Anchor.BOTH,
        "left='100px' right='200px'");
  }

  public void test_ANCHOR_RW_to_N() throws Exception {
    assertEquals("right + width", Anchor.TRAILING.getTitle(true));
    check_ANCHOR_horizontal(Anchor.TRAILING, "right='100px' width='150px'", Anchor.NONE, "");
  }

  public void test_ANCHOR_RW_to_LW() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.TRAILING,
        "right='100px' width='150px'",
        Anchor.LEADING,
        "width='150px' left='200px'");
  }

  public void test_ANCHOR_RW_to_LR() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.TRAILING,
        "right='100px' width='150px'",
        Anchor.BOTH,
        "right='100px' left='200px'");
  }

  public void test_ANCHOR_LR_to_N() throws Exception {
    assertEquals("left + right", Anchor.BOTH.getTitle(true));
    check_ANCHOR_horizontal(Anchor.BOTH, "left='100px' right='150px'", Anchor.NONE, "");
  }

  public void test_ANCHOR_LR_to_LW() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.BOTH,
        "left='100px' right='150px'",
        Anchor.LEADING,
        "left='100px' width='200px'");
  }

  public void test_ANCHOR_LR_to_LW_mm() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.BOTH,
        "left='50.0mm' right='150px'",
        Anchor.LEADING,
        "left='50.0mm' width='" + W_MM_after_50MM_150PX + "mm'");
  }

  public void test_ANCHOR_LR_to_RW() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.BOTH,
        "left='100px' right='150px'",
        Anchor.TRAILING,
        "right='150px' width='200px'");
  }

  public void test_ANCHOR_LR_to_RW_mm() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.BOTH,
        "left='150px' right='50mm'",
        Anchor.TRAILING,
        "right='50mm' width='" + W_MM_after_50MM_150PX + "mm'");
  }

  private void check_ANCHOR_horizontal(Anchor initialAnchor,
      String initialDesc,
      Anchor newAnchor,
      String expectedDesc) throws Exception {
    check_ANCHOR(true, initialAnchor, initialDesc, newAnchor, expectedDesc);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ANCHOR: vertical
  //
  //////////////////////////////////////////////////////////////////////////// 
  public void test_ANCHOR_Nv_to_Nv() throws Exception {
    assertEquals("none", Anchor.NONE.getTitle(true));
    check_ANCHOR_vertical(Anchor.NONE, "", Anchor.NONE, "");
  }

  public void test_ANCHOR_N_to_TH() throws Exception {
    check_ANCHOR_vertical(Anchor.NONE, "", Anchor.LEADING, "top='0px' height='300px'");
  }

  public void test_ANCHOR_N_to_BH() throws Exception {
    check_ANCHOR_vertical(Anchor.NONE, "", Anchor.TRAILING, "bottom='0px' height='300px'");
  }

  public void test_ANCHOR_N_to_TB() throws Exception {
    check_ANCHOR_vertical(Anchor.NONE, "", Anchor.BOTH, "top='0px' bottom='0px'");
  }

  public void test_ANCHOR_TH_to_N() throws Exception {
    assertEquals("top + height", Anchor.LEADING.getTitle(false));
    check_ANCHOR_vertical(Anchor.LEADING, "top='100px' height='150px'", Anchor.NONE, "");
  }

  public void test_ANCHOR_TH_to_BH() throws Exception {
    check_ANCHOR_vertical(
        Anchor.LEADING,
        "top='100px' height='150px'",
        Anchor.TRAILING,
        "height='150px' bottom='50px'");
  }

  public void test_ANCHOR_TH_to_TB() throws Exception {
    check_ANCHOR_vertical(
        Anchor.LEADING,
        "top='100px' height='150px'",
        Anchor.BOTH,
        "top='100px' bottom='50px'");
  }

  public void test_ANCHOR_BH_to_N() throws Exception {
    assertEquals("bottom + height", Anchor.TRAILING.getTitle(false));
    check_ANCHOR_vertical(Anchor.TRAILING, "bottom='100px' height='150px'", Anchor.NONE, "");
  }

  public void test_ANCHOR_BH_to_TH() throws Exception {
    check_ANCHOR_vertical(
        Anchor.TRAILING,
        "bottom='100px' height='150px'",
        Anchor.LEADING,
        "height='150px' top='50px'");
  }

  public void test_ANCHOR_BH_to_TB() throws Exception {
    check_ANCHOR_vertical(
        Anchor.TRAILING,
        "bottom='100px' height='150px'",
        Anchor.BOTH,
        "bottom='100px' top='50px'");
  }

  public void test_ANCHOR_TB_to_N() throws Exception {
    assertEquals("top + bottom", Anchor.BOTH.getTitle(false));
    check_ANCHOR_vertical(Anchor.BOTH, "top='100px' bottom='150px'", Anchor.NONE, "");
  }

  public void test_ANCHOR_TB_to_TH() throws Exception {
    check_ANCHOR_vertical(
        Anchor.BOTH,
        "top='100px' bottom='150px'",
        Anchor.LEADING,
        "top='100px' height='50px'");
  }

  public void test_ANCHOR_TB_to_TH_mm() throws Exception {
    String initialDesc = "top='50mm' bottom='50px'";
    String expectedDesc = "top='50mm' height='" + H_MM_after_50MM_50PX + "mm'";
    check_ANCHOR_vertical(Anchor.BOTH, initialDesc, Anchor.LEADING, expectedDesc);
  }

  public void test_ANCHOR_TB_to_BH() throws Exception {
    check_ANCHOR_vertical(
        Anchor.BOTH,
        "top='100px' bottom='150px'",
        Anchor.TRAILING,
        "bottom='150px' height='50px'");
  }

  public void test_ANCHOR_TB_to_BH_mm() throws Exception {
    String initialDesc = "top='50px' bottom='50mm'";
    String expectedDesc = "bottom='50mm' height='" + H_MM_after_50MM_50PX + "mm'";
    check_ANCHOR_vertical(Anchor.BOTH, initialDesc, Anchor.TRAILING, expectedDesc);
  }

  private void check_ANCHOR_vertical(Anchor initialAnchor,
      String initialDesc,
      Anchor newAnchor,
      String expectedDesc) throws Exception {
    check_ANCHOR(false, initialAnchor, initialDesc, newAnchor, expectedDesc);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ANCHOR utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void check_ANCHOR(boolean horizontal,
      Anchor initialAnchor,
      String initialDesc,
      Anchor newAnchor,
      String expectedDesc) throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer" + getAnchorLine(initialDesc) + ">",
            "      <g:Button wbp:name='button'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    // initial anchor
    {
      Anchor anchor = frame.getAnchor(button, horizontal);
      assertSame(initialAnchor, anchor);
      assertNotNull(anchor.getImage(horizontal));
      assertNotNull(anchor.getSmallImage(horizontal));
    }
    // set new anchor
    frame.command_ANCHOR(button, horizontal, newAnchor);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer" + getAnchorLine(expectedDesc) + ">",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    {
      Anchor anchor = frame.getAnchor(button, horizontal);
      assertSame(newAnchor, anchor);
    }
  }

  private static String getAnchorLine(String desc) {
    if (desc.isEmpty()) {
      return "";
    } else {
      return " " + desc;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment actions
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_alignmentActions_noSelection() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    List<Object> actions = getSelectionActions_noSelection(frame);
    assertThat(actions.isEmpty());
  }

  public void test_alignmentActions_wrongSelection() throws Exception {
    LayoutPanelInfo frame =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    List<Object> actions = getSelectionActions(frame);
    assertThat(actions.isEmpty());
  }

  public void test_alignmentActions_LW_to_RW() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='100px' width='150px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    List<Object> actions = getSelectionActions(button);
    // "left + width" is checked
    {
      IAction action = findAction(actions, "left + width");
      assertTrue(action.isChecked());
    }
    // use "right + width"
    {
      IAction action = findAction(actions, "right + width");
      action.setChecked(true);
      action.run();
    }
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer width='150px' right='200px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_alignmentActions_TH_to_BH() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer top='100px' height='150px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    List<Object> actions = getSelectionActions(button);
    // "top + height" is checked
    {
      IAction action = findAction(actions, "top + height");
      assertTrue(action.isChecked());
    }
    // use "bottom + height"
    {
      IAction action = findAction(actions, "bottom + height");
      action.setChecked(true);
      action.run();
    }
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer height='150px' bottom='50px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location properties
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_properties() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='1px' width='2px' top='1px' height='2px'>",
        "      <g:Button wbp:name='button_LW_TH'/>",
        "    </g:layer>",
        "    <g:layer right='1px' width='2px' bottom='1px' height='2px'>",
        "      <g:Button wbp:name='button_RW_BH'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    refresh();
    // LW TH
    {
      WidgetInfo button = getObjectByName("button_LW_TH");
      assertNotNull(PropertyUtils.getByPath(button, "Anchor H/left"));
      assertNotNull(PropertyUtils.getByPath(button, "Anchor H/left unit"));
      assertNotNull(PropertyUtils.getByPath(button, "Anchor H/width"));
      assertNotNull(PropertyUtils.getByPath(button, "Anchor H/width unit"));
      assertNotNull(PropertyUtils.getByPath(button, "Anchor V/top"));
      assertNotNull(PropertyUtils.getByPath(button, "Anchor V/top unit"));
      assertNotNull(PropertyUtils.getByPath(button, "Anchor V/height"));
      assertNotNull(PropertyUtils.getByPath(button, "Anchor V/height unit"));
    }
    // RW BH
    {
      WidgetInfo button = getObjectByName("button_RW_BH");
      assertNotNull(PropertyUtils.getByPath(button, "Anchor H/right"));
      assertNotNull(PropertyUtils.getByPath(button, "Anchor H/right unit"));
      assertNotNull(PropertyUtils.getByPath(button, "Anchor H/width"));
      assertNotNull(PropertyUtils.getByPath(button, "Anchor H/width unit"));
      assertNotNull(PropertyUtils.getByPath(button, "Anchor V/bottom"));
      assertNotNull(PropertyUtils.getByPath(button, "Anchor V/bottom unit"));
      assertNotNull(PropertyUtils.getByPath(button, "Anchor V/height"));
      assertNotNull(PropertyUtils.getByPath(button, "Anchor V/height unit"));
    }
  }

  public void test_propertiesH_leftValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='100px' width='200px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property leftProperty = PropertyUtils.getByPath(button, "Anchor H/left");
    assertNotNull(leftProperty);
    assertTrue(leftProperty.isModified());
    assertEquals("100.0", getPropertyText(leftProperty));
    leftProperty.setValue(45.0);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='45px' width='200px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_propertiesV_topValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer top='100px' height='50px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property topProperty = PropertyUtils.getByPath(button, "Anchor V/top");
    assertNotNull(topProperty);
    assertTrue(topProperty.isModified());
    assertEquals("100.0", getPropertyText(topProperty));
    topProperty.setValue(45.0);
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer top='45px' height='50px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_propertiesH_leftUnit_PXtoMM() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='100px' width='200px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property leftProperty = PropertyUtils.getByPath(button, "Anchor H/left unit");
    assertNotNull(leftProperty);
    assertTrue(leftProperty.isModified());
    assertEquals("PX", getPropertyText(leftProperty));
    leftProperty.setValue("MM");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='" + MM_100 + "mm' width='200px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_propertiesH_leftUnit_PXtoEX() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='100px' width='200px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property leftProperty = PropertyUtils.getByPath(button, "Anchor H/left unit");
    assertNotNull(leftProperty);
    assertTrue(leftProperty.isModified());
    assertEquals("PX", getPropertyText(leftProperty));
    leftProperty.setValue("EX");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='" + EX_100 + "ex' width='200px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_propertiesV_topUnit_PXtoMM() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer top='100px' height='50px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo button = getObjectByName("button");
    //
    Property topProperty = PropertyUtils.getByPath(button, "Anchor V/top unit");
    assertNotNull(topProperty);
    assertTrue(topProperty.isModified());
    assertEquals("PX", getPropertyText(topProperty));
    topProperty.setValue("MM");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer top='" + MM_100 + "mm' height='50px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }
}