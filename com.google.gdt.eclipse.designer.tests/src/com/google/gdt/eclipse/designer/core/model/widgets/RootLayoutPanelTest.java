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

import com.google.common.base.Function;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ILayoutPanelInfo.Anchor;
import com.google.gdt.eclipse.designer.model.widgets.panels.ILayoutPanelInfo.ResizeDirection;
import com.google.gdt.eclipse.designer.model.widgets.panels.LayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootLayoutPanelCreationSupport;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootLayoutPanelInfo;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.StrValue;

import org.eclipse.jface.action.IAction;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.List;

/**
 * Test {@link RootLayoutPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class RootLayoutPanelTest extends GwtModelTest {
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
      new StrValue("scheglov-win", "10.8"),
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
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "  }",
            "}");
    assertHierarchy("{RootLayoutPanel.get()} {local-unique: rootPanel} {/RootLayoutPanel.get()/}");
    frame.refresh();
    // RootLayoutPanel_CreationSupport
    {
      RootLayoutPanelCreationSupport creationSupport =
          (RootLayoutPanelCreationSupport) frame.getCreationSupport();
      assertEquals("RootLayoutPanel.get()", m_lastEditor.getSource(creationSupport.getNode()));
      assertFalse(creationSupport.canDelete());
      assertFalse(creationSupport.canReorder());
      assertFalse(creationSupport.canReparent());
    }
    // default bounds
    assertEquals(new Rectangle(0, 0, 450, 300), frame.getBounds());
    // set new size
    {
      TopBoundsSupport topBoundsSupport = frame.getTopBoundsSupport();
      topBoundsSupport.setSize(500, 400);
      frame.refresh();
      assertEquals(new Rectangle(0, 0, 500, 400), frame.getBounds());
    }
  }

  public void test_withButton() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetLeftRight(button, 10, Unit.PX, 20, Unit.PX);",
            "      rootPanel.setWidgetTopBottom(button, 30, Unit.PX, 40, Unit.PX);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{RootLayoutPanel.get()} {local-unique: rootPanel} {/RootLayoutPanel.get()/ /rootPanel.add(button)/ /rootPanel.setWidgetLeftRight(button, 10, Unit.PX, 20, Unit.PX)/ /rootPanel.setWidgetTopBottom(button, 30, Unit.PX, 40, Unit.PX)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /rootPanel.add(button)/ /rootPanel.setWidgetLeftRight(button, 10, Unit.PX, 20, Unit.PX)/ /rootPanel.setWidgetTopBottom(button, 30, Unit.PX, 40, Unit.PX)/}");
    frame.refresh();
    // bounds
    WidgetInfo button = getJavaInfoByName("button");
    {
      Rectangle expected = new Rectangle(10, 30, 450 - 10 - 20, 300 - 30 - 40);
      assertEquals(expected, button.getBounds());
      assertEquals(expected, button.getModelBounds());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_out() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      FlowPanel panel = new FlowPanel();",
            "      rootPanel.add(panel);",
            "      rootPanel.setWidgetLeftRight(panel, 10, Unit.PX, 10, Unit.PX);",
            "      rootPanel.setWidgetTopHeight(panel, 10, Unit.PX, 100, Unit.PX);",
            "    }",
            "    {",
            "      Button button_1 = new Button();",
            "      rootPanel.add(button_1);",
            "      rootPanel.setWidgetLeftWidth(button_1, 10, Unit.PX, 150, Unit.PX);",
            "      rootPanel.setWidgetTopHeight(button_1, 150, Unit.PX, 50, Unit.PX);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      rootPanel.add(button_2);",
            "      rootPanel.setWidgetLeftWidth(button_2, 200, Unit.PX, 150, Unit.PX);",
            "      rootPanel.setWidgetTopHeight(button_2, 150, Unit.PX, 50, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ComplexPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button_1");
    //
    panel.command_MOVE2(button, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      FlowPanel panel = new FlowPanel();",
        "      rootPanel.add(panel);",
        "      rootPanel.setWidgetLeftRight(panel, 10, Unit.PX, 10, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(panel, 10, Unit.PX, 100, Unit.PX);",
        "      {",
        "        Button button_1 = new Button();",
        "        panel.add(button_1);",
        "      }",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      rootPanel.add(button_2);",
        "      rootPanel.setWidgetLeftWidth(button_2, 200, Unit.PX, 150, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(button_2, 150, Unit.PX, 50, Unit.PX);",
        "    }",
        "  }",
        "}");
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
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "    }",
            "    {",
            "      Button button_LW_TH = new Button();",
            "      rootPanel.add(button_LW_TH);",
            "      rootPanel.setWidgetLeftWidth(button_LW_TH, 1.0, Unit.MM, 1.0, Unit.CM);",
            "      rootPanel.setWidgetTopHeight(button_LW_TH, 2.0, Unit.PX, 1.0, Unit.CM);",
            "    }",
            "    {",
            "      Button button_LR_TB = new Button();",
            "      rootPanel.add(button_LR_TB);",
            "      rootPanel.setWidgetLeftRight(button_LR_TB, 1.0, Unit.MM, 1.0, Unit.CM);",
            "      rootPanel.setWidgetTopBottom(button_LR_TB, 2.0, Unit.CM, 1.0, Unit.CM);",
            "    }",
            "    {",
            "      Button button_RW_BH = new Button();",
            "      rootPanel.add(button_RW_BH);",
            "      rootPanel.setWidgetRightWidth(button_RW_BH, 10.0, Unit.PX, 100, Unit.PX);",
            "      rootPanel.setWidgetBottomHeight(button_RW_BH, 20.0, Unit.PX, 50, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    {
      WidgetInfo button = getJavaInfoByName("button");
      assertEquals("100.0px x 50.0px", frame.getLocationHint(button, 100, 50));
      assertEquals(false, frame.getLocationHint_isTrailing(button, true));
      assertEquals(false, frame.getLocationHint_isTrailing(button, false));
    }
    {
      WidgetInfo button = getJavaInfoByName("button_LW_TH");
      assertEquals(MM_100 + "mm" + " x 50.0px", frame.getLocationHint(button, 100, 50));
      assertEquals(false, frame.getLocationHint_isTrailing(button, true));
      assertEquals(false, frame.getLocationHint_isTrailing(button, false));
    }
    {
      WidgetInfo button = getJavaInfoByName("button_LR_TB");
      assertEquals(MM_50 + "mm" + " x " + CM_100 + "cm", frame.getLocationHint(button, 50, 100));
      assertEquals(false, frame.getLocationHint_isTrailing(button, true));
      assertEquals(false, frame.getLocationHint_isTrailing(button, false));
    }
    {
      WidgetInfo button = getJavaInfoByName("button_RW_BH");
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
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    button.getBounds().setSize(150, 150);
    frame.command_LOCATION(button, new Point(10, 20));
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 10.0, Unit.PX, 150.0, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(button, 20.0, Unit.PX, 150.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_LOCATION_update_LeftTop_PX() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetLeftWidth(button, 1.0, Unit.PX, 1.0, Unit.CM);",
            "      rootPanel.setWidgetTopHeight(button, 2.0, Unit.PX, 1.0, Unit.CM);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    frame.command_LOCATION(button, new Point(10, 20));
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 10.0, Unit.PX, 1.0, Unit.CM);",
        "      rootPanel.setWidgetTopHeight(button, 20.0, Unit.PX, 1.0, Unit.CM);",
        "    }",
        "  }",
        "}");
  }

  public void test_LOCATION_update_LeftTop_MM() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetLeftWidth(button, 1.0, Unit.MM, 1.0, Unit.CM);",
            "      rootPanel.setWidgetTopHeight(button, 2.0, Unit.MM, 1.0, Unit.CM);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    frame.command_LOCATION(button, new Point(100, 50));
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, " + MM_100 + ", Unit.MM, 1.0, Unit.CM);",
        "      rootPanel.setWidgetTopHeight(button, " + MM_50 + ", Unit.MM, 1.0, Unit.CM);",
        "    }",
        "  }",
        "}");
  }

  public void test_LOCATION_update_RightBottom_PX() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetRightWidth(button, 1.0, Unit.PX, 100, Unit.PX);",
            "      rootPanel.setWidgetBottomHeight(button, 2.0, Unit.PX, 50, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo box = getJavaInfoByName("button");
    frame.command_LOCATION(box, new Point(200, 150));
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetRightWidth(button, 150.0, Unit.PX, 100, Unit.PX);",
        "      rootPanel.setWidgetBottomHeight(button, 100.0, Unit.PX, 50, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_LOCATION_update_LeftRight_TopBottom_PX() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetLeftRight(button, 100.0, Unit.PX, 200, Unit.PX);",
            "      rootPanel.setWidgetTopBottom(button, 100.0, Unit.PX, 150, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo box = getJavaInfoByName("button");
    frame.command_LOCATION(box, new Point(200, 150));
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftRight(button, 200.0, Unit.PX, 100.0, Unit.PX);",
        "      rootPanel.setWidgetTopBottom(button, 150.0, Unit.PX, 100.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SIZE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_SIZE_new() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    frame.command_SIZE(
        button,
        new Dimension(200, 50),
        ResizeDirection.TRAILING,
        ResizeDirection.TRAILING);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 0.0, Unit.PX, 200.0, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(button, 0.0, Unit.PX, 50.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_SIZE_update_LeftWidth_TopHeight_TT_PX() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetLeftWidth(button, 0.0, Unit.PX, 100.0, Unit.PX);",
            "      rootPanel.setWidgetTopHeight(button, 0.0, Unit.PX, 50.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    frame.command_SIZE(
        button,
        new Dimension(150, 75),
        ResizeDirection.TRAILING,
        ResizeDirection.TRAILING);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 0.0, Unit.PX, 150.0, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(button, 0.0, Unit.PX, 75.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_SIZE_update_LeftWidth_TopHeight_TT_MM() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetLeftWidth(button, 0.0, Unit.PX, 10.0, Unit.MM);",
            "      rootPanel.setWidgetTopHeight(button, 0.0, Unit.PX, 10.0, Unit.MM);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    frame.command_SIZE(
        button,
        new Dimension(100, 50),
        ResizeDirection.TRAILING,
        ResizeDirection.TRAILING);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 0.0, Unit.PX, " + MM_100 + ", Unit.MM);",
        "      rootPanel.setWidgetTopHeight(button, 0.0, Unit.PX, " + MM_50 + ", Unit.MM);",
        "    }",
        "  }",
        "}");
  }

  public void test_SIZE_update_LeftWidth_TopHeight_LL_PX() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetLeftWidth(button, 10.0, Unit.PX, 100.0, Unit.PX);",
            "      rootPanel.setWidgetTopHeight(button, 20.0, Unit.PX, 50.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    frame.command_SIZE(
        button,
        new Dimension(90, 40),
        ResizeDirection.LEADING,
        ResizeDirection.LEADING);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 20.0, Unit.PX, 90.0, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(button, 30.0, Unit.PX, 40.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_SIZE_update_RightWidth_BottomHeight_TT_PX() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetRightWidth(button, 100.0, Unit.PX, 100.0, Unit.PX);",
            "      rootPanel.setWidgetBottomHeight(button, 100.0, Unit.PX, 50.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    frame.command_SIZE(
        button,
        new Dimension(150, 75),
        ResizeDirection.TRAILING,
        ResizeDirection.TRAILING);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetRightWidth(button, 50.0, Unit.PX, 150.0, Unit.PX);",
        "      rootPanel.setWidgetBottomHeight(button, 75.0, Unit.PX, 75.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_SIZE_update_RightWidth_BottomHeight_LL_PX() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetRightWidth(button, 100.0, Unit.PX, 100.0, Unit.PX);",
            "      rootPanel.setWidgetBottomHeight(button, 100.0, Unit.PX, 50.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    frame.command_SIZE(
        button,
        new Dimension(150, 75),
        ResizeDirection.LEADING,
        ResizeDirection.LEADING);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetRightWidth(button, 100.0, Unit.PX, 150.0, Unit.PX);",
        "      rootPanel.setWidgetBottomHeight(button, 100.0, Unit.PX, 75.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_SIZE_update_LeftRight_TopBottom_LL_PX() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetLeftRight(button, 100.0, Unit.PX, 100.0, Unit.PX);",
            "      rootPanel.setWidgetTopBottom(button, 50.0, Unit.PX, 50.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    frame.command_SIZE(
        button,
        new Dimension(150, 100),
        ResizeDirection.LEADING,
        ResizeDirection.LEADING);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftRight(button, 200.0, Unit.PX, 100.0, Unit.PX);",
        "      rootPanel.setWidgetTopBottom(button, 150.0, Unit.PX, 50.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_SIZE_update_LeftRight_TopBottom_TT_PX() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetLeftRight(button, 100.0, Unit.PX, 100.0, Unit.PX);",
            "      rootPanel.setWidgetTopBottom(button, 50.0, Unit.PX, 50.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    frame.command_SIZE(
        button,
        new Dimension(150, 100),
        ResizeDirection.TRAILING,
        ResizeDirection.TRAILING);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftRight(button, 100.0, Unit.PX, 200.0, Unit.PX);",
        "      rootPanel.setWidgetTopBottom(button, 50.0, Unit.PX, 150.0, Unit.PX);",
        "    }",
        "  }",
        "}");
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
    check_ANCHOR_horizontal(
        Anchor.NONE,
        "",
        Anchor.LEADING,
        "LeftWidth 0.0, Unit.PX, 450.0, Unit.PX");
  }

  public void test_ANCHOR_N_to_RW() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.NONE,
        "",
        Anchor.TRAILING,
        "RightWidth 0.0, Unit.PX, 450.0, Unit.PX");
  }

  public void test_ANCHOR_N_to_LR() throws Exception {
    check_ANCHOR_horizontal(Anchor.NONE, "", Anchor.BOTH, "LeftRight 0.0, Unit.PX, 0.0, Unit.PX");
  }

  public void test_ANCHOR_LW_to_N() throws Exception {
    assertEquals("left + width", Anchor.LEADING.getTitle(true));
    check_ANCHOR_horizontal(
        Anchor.LEADING,
        "LeftWidth 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.NONE,
        "");
  }

  public void test_ANCHOR_LW_to_RW() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.LEADING,
        "LeftWidth 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.TRAILING,
        "RightWidth 200.0, Unit.PX, 150.0, Unit.PX");
  }

  public void test_ANCHOR_LW_to_LR() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.LEADING,
        "LeftWidth 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.BOTH,
        "LeftRight 100.0, Unit.PX, 200.0, Unit.PX");
  }

  public void test_ANCHOR_RW_to_N() throws Exception {
    assertEquals("right + width", Anchor.TRAILING.getTitle(true));
    check_ANCHOR_horizontal(
        Anchor.TRAILING,
        "RightWidth 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.NONE,
        "");
  }

  public void test_ANCHOR_RW_to_LW() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.TRAILING,
        "RightWidth 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.LEADING,
        "LeftWidth 200.0, Unit.PX, 150.0, Unit.PX");
  }

  public void test_ANCHOR_RW_to_LR() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.TRAILING,
        "RightWidth 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.BOTH,
        "LeftRight 200.0, Unit.PX, 100.0, Unit.PX");
  }

  public void test_ANCHOR_LR_to_N() throws Exception {
    assertEquals("left + right", Anchor.BOTH.getTitle(true));
    check_ANCHOR_horizontal(
        Anchor.BOTH,
        "LeftRight 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.NONE,
        "");
  }

  public void test_ANCHOR_LR_to_LW() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.BOTH,
        "LeftRight 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.LEADING,
        "LeftWidth 100.0, Unit.PX, 200.0, Unit.PX");
  }

  public void test_ANCHOR_LR_to_LW_mm() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.BOTH,
        "LeftRight 50.0, Unit.MM, 150.0, Unit.PX",
        Anchor.LEADING,
        "LeftWidth 50.0, Unit.MM, " + W_MM_after_50MM_150PX + ", Unit.MM");
  }

  public void test_ANCHOR_LR_to_RW() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.BOTH,
        "LeftRight 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.TRAILING,
        "RightWidth 150.0, Unit.PX, 200.0, Unit.PX");
  }

  public void test_ANCHOR_LR_to_RW_mm() throws Exception {
    check_ANCHOR_horizontal(
        Anchor.BOTH,
        "LeftRight 150.0, Unit.PX, 50.0, Unit.MM",
        Anchor.TRAILING,
        "RightWidth 50.0, Unit.MM, " + W_MM_after_50MM_150PX + ", Unit.MM");
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
  public void test_ANCHOR_TH_to_N() throws Exception {
    assertEquals("top + height", Anchor.LEADING.getTitle(false));
    check_ANCHOR_vertical(
        Anchor.LEADING,
        "TopHeight 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.NONE,
        "");
  }

  public void test_ANCHOR_TH_to_BH() throws Exception {
    check_ANCHOR_vertical(
        Anchor.LEADING,
        "TopHeight 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.TRAILING,
        "BottomHeight 50.0, Unit.PX, 150.0, Unit.PX");
  }

  public void test_ANCHOR_TH_to_TB() throws Exception {
    check_ANCHOR_vertical(
        Anchor.LEADING,
        "TopHeight 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.BOTH,
        "TopBottom 100.0, Unit.PX, 50.0, Unit.PX");
  }

  public void test_ANCHOR_BH_to_N() throws Exception {
    assertEquals("bottom + height", Anchor.TRAILING.getTitle(false));
    check_ANCHOR_vertical(
        Anchor.TRAILING,
        "BottomHeight 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.NONE,
        "");
  }

  public void test_ANCHOR_BH_to_TH() throws Exception {
    check_ANCHOR_vertical(
        Anchor.TRAILING,
        "BottomHeight 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.LEADING,
        "TopHeight 50.0, Unit.PX, 150.0, Unit.PX");
  }

  public void test_ANCHOR_BH_to_TB() throws Exception {
    check_ANCHOR_vertical(
        Anchor.TRAILING,
        "BottomHeight 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.BOTH,
        "TopBottom 50.0, Unit.PX, 100.0, Unit.PX");
  }

  public void test_ANCHOR_TB_to_N() throws Exception {
    assertEquals("top + bottom", Anchor.BOTH.getTitle(false));
    check_ANCHOR_vertical(Anchor.BOTH, "TopBottom 100.0, Unit.PX, 150.0, Unit.PX", Anchor.NONE, "");
  }

  public void test_ANCHOR_TB_to_TH() throws Exception {
    check_ANCHOR_vertical(
        Anchor.BOTH,
        "TopBottom 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.LEADING,
        "TopHeight 100.0, Unit.PX, 50.0, Unit.PX");
  }

  public void test_ANCHOR_TB_to_TH_mm() throws Exception {
    check_ANCHOR_vertical(
        Anchor.BOTH,
        "TopBottom 50.0, Unit.MM, 50.0, Unit.PX",
        Anchor.LEADING,
        "TopHeight 50.0, Unit.MM, " + H_MM_after_50MM_50PX + ", Unit.MM");
  }

  public void test_ANCHOR_TB_to_BH() throws Exception {
    check_ANCHOR_vertical(
        Anchor.BOTH,
        "TopBottom 100.0, Unit.PX, 150.0, Unit.PX",
        Anchor.TRAILING,
        "BottomHeight 150.0, Unit.PX, 50.0, Unit.PX");
  }

  public void test_ANCHOR_TB_to_BH_mm() throws Exception {
    check_ANCHOR_vertical(
        Anchor.BOTH,
        "TopBottom 50.0, Unit.PX, 50.0, Unit.MM",
        Anchor.TRAILING,
        "BottomHeight 50.0, Unit.MM, " + H_MM_after_50MM_50PX + ", Unit.MM");
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
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      " + getAnchorLine(initialDesc),
            "    }",
            "  }",
            "}");
    anchor_removeEmptyLine_inEditor();
    frame.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    // initial anchor
    {
      Anchor anchor = frame.getAnchor(button, horizontal);
      assertSame(initialAnchor, anchor);
      assertNotNull(anchor.getImage(horizontal));
      assertNotNull(anchor.getSmallImage(horizontal));
    }
    // set new anchor
    frame.command_ANCHOR(button, horizontal, newAnchor);
    anchor_assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      " + getAnchorLine(expectedDesc),
        "    }",
        "  }",
        "}");
    {
      Anchor anchor = frame.getAnchor(button, horizontal);
      assertSame(newAnchor, anchor);
    }
  }

  private void anchor_removeEmptyLine_inEditor() throws Exception {
    String source = m_lastEditor.getSource();
    String strToRemove = "\t\t\t\n";
    int index = source.indexOf(strToRemove);
    if (index != -1) {
      m_lastEditor.replaceSubstring(index, strToRemove.length(), "");
    }
  }

  private void anchor_assertEditor(String... lines) {
    try {
      m_assertEditor_expectedSourceProcessor = new Function<String, String>() {
        public String apply(String source) {
          String strToRemove = "\t\t\t\n";
          return StringUtils.remove(source, strToRemove);
        }
      };
      assertEditor(lines);
    } finally {
      m_assertEditor_expectedSourceProcessor = null;
    }
  }

  private static String getAnchorLine(String desc) {
    String[] parts = StringUtils.split(desc, " ", 2);
    if (desc.isEmpty()) {
      return "";
    } else {
      return MessageFormat.format("rootPanel.setWidget{0}(button, {1});", parts[0], parts[1]);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment actions
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_alignmentActions_noSelection() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "  }",
            "}");
    frame.refresh();
    //
    List<Object> actions = getSelectionActions_noSelection(frame);
    assertThat(actions.isEmpty());
  }

  public void test_alignmentActions_wrongSelection() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "  }",
            "}");
    frame.refresh();
    //
    List<Object> actions = getSelectionActions(frame);
    assertThat(actions.isEmpty());
  }

  public void test_alignmentActions_LW_to_RW() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetLeftWidth(button, 100, Unit.PX, 150, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = getJavaInfoByName("button");
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
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetRightWidth(button, 200.0, Unit.PX, 150, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_alignmentActions_TH_to_BH() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetTopHeight(button, 100, Unit.PX, 150, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = getJavaInfoByName("button");
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
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetBottomHeight(button, 50.0, Unit.PX, 150, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location properties
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_propertiesH_leftValue() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetLeftWidth(button, 100.0, Unit.PX, 200.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    Property leftProperty = PropertyUtils.getByPath(button, "Anchor H/left");
    assertNotNull(leftProperty);
    assertTrue(leftProperty.isModified());
    assertEquals("100.0", getPropertyText(leftProperty));
    leftProperty.setValue(45.0);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 45.0, Unit.PX, 200.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_propertiesV_topValue() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetTopHeight(button, 100.0, Unit.PX, 50.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    Property topProperty = PropertyUtils.getByPath(button, "Anchor V/top");
    assertNotNull(topProperty);
    assertTrue(topProperty.isModified());
    assertEquals("100.0", getPropertyText(topProperty));
    topProperty.setValue(45.0);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetTopHeight(button, 45.0, Unit.PX, 50.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_propertiesH_leftUnit_PXtoMM() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetLeftWidth(button, 100.0, Unit.PX, 200.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    Property leftProperty = PropertyUtils.getByPath(button, "Anchor H/left unit");
    assertNotNull(leftProperty);
    assertTrue(leftProperty.isModified());
    assertEquals("PX", getPropertyText(leftProperty));
    leftProperty.setValue("MM");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, " + MM_100 + ", Unit.MM, 200.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_propertiesH_leftUnit_PXtoEX() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetLeftWidth(button, 100.0, Unit.PX, 200.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    Property leftProperty = PropertyUtils.getByPath(button, "Anchor H/left unit");
    assertNotNull(leftProperty);
    assertTrue(leftProperty.isModified());
    assertEquals("PX", getPropertyText(leftProperty));
    leftProperty.setValue("EX");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, " + EX_100 + ", Unit.EX, 200.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_propertiesV_topUnit_PXtoMM() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button);",
            "      rootPanel.setWidgetTopHeight(button, 100.0, Unit.PX, 50.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    Property topProperty = PropertyUtils.getByPath(button, "Anchor V/top unit");
    assertNotNull(topProperty);
    assertTrue(topProperty.isModified());
    assertEquals("PX", getPropertyText(topProperty));
    topProperty.setValue("MM");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetTopHeight(button, " + MM_100 + ", Unit.MM, 50.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }
}