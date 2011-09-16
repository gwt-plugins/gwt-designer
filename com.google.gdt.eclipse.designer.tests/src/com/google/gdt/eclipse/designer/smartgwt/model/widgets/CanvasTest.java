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
package com.google.gdt.eclipse.designer.smartgwt.model.widgets;

import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;
import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.LayoutInfo;
import com.google.gdt.eclipse.designer.smart.model.WidgetCanvasInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider.ModuleModification;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link CanvasInfo}.
 * 
 * @author scheglov_ke
 */
public class CanvasTest extends SmartGwtModelTest {
  public static final int BUTTON_WIDTH = 100;
  public static final int BUTTON_HEIGHT = 22;

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
   * Test parse on RootPanel.
   */
  public void test_parse_onRootPanel() throws Exception {
    dontUseSharedGWTState();
    RootPanelInfo rootPanel =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Canvas canvas = new Canvas();",
            "    rootPanel.add(canvas, 10, 10);",
            "    canvas.setSize('250px', '200px');",
            "  }",
            "}");
    rootPanel.refresh();
    CanvasInfo canvas = rootPanel.getChildren(CanvasInfo.class).get(0);
    assertThat(canvas.getBounds()).isEqualTo(new Rectangle(10, 10, 250, 200));
  }

  /**
   * Test for SmartGWT when "rename-to" attribute is used.
   * <p>
   * http://forums.instantiations.com/viewtopic.php?f=11&t=5298
   */
  @DisposeProjectAfter
  public void test_parse_whenRenameTo() throws Exception {
    dontUseSharedGWTState();
    // update module file to use "rename-to"
    DefaultModuleProvider.modify(getTestModuleDescription(), new ModuleModification() {
      public void modify(ModuleElement moduleElement) throws Exception {
        moduleElement.setAttribute("rename-to", "myModuleName");
      }
    });
    waitForAutoBuild();
    // parse
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      IButton button = new IButton();",
        "      rootPanel.add(button);",
        "    }",
        "  }",
        "}");
    refresh();
    CanvasInfo button = getJavaInfoByName("button");
    // assert that correct path to background image is used
    String elementString = button.getElement().toString();
    assertThat(elementString).contains("/myModuleName/sc/skins/");
  }

  /**
   * Test parse with Canvas.draw().
   */
  public void test_parse_draw() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    canvas.setWidth(250);",
            "    canvas.setHeight(200);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    assertThat(canvas.getBounds()).isEqualTo(new Rectangle(0, 0, 250, 200));
  }

  /**
   * Test parse 'this'.
   */
  public void test_parse_this() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test extends Canvas {",
            "  public Test() {",
            "    setWidth(250);",
            "    setHeight(200);",
            "  }",
            "}"});
    canvas.refresh();
    assertThat(canvas.getBounds()).isEqualTo(new Rectangle(0, 0, 250, 200));
  }

  /**
   * We should unwrap <code>Element</code> when search SmartGWT widget in table.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47231
   */
  public void test_parse_inGrid() throws Exception {
    dontUseSharedGWTState();
    parseJavaInfo(
        "import com.google.gwt.user.client.ui.Grid;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    Grid grid = new Grid(2, 2);",
        "    rootPanel.add(grid);",
        "    {",
        "      Button button = new Button('A');",
        "      grid.setWidget(0, 0, button);",
        "    }",
        "  }",
        "}");
    refresh();
    HTMLTableInfo grid = getJavaInfoByName("grid");
    CanvasInfo button = getJavaInfoByName("button");
    //
    IGridInfo gridInfo = grid.getGridInfo();
    assertEquals(new Rectangle(0, 0, 0, 0), gridInfo.getComponentCells(button));
  }

  /**
   * Test for {@link CanvasInfo#getWidgets()}.
   */
  public void test_getWidgets() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      addChild(button_1);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      addChild(button_2);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button_1 = getJavaInfoByName("button_1");
    CanvasInfo button_2 = getJavaInfoByName("button_2");
    assertThat(canvas.getWidgets()).containsExactly(button_1, button_2);
  }

  /**
   * Test for {@link CanvasInfo#isExactlyCanvas()}.
   */
  public void test_isExactlyCanvas() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      addChild(button);",
            "    }",
            "  }",
            "}");
    CanvasInfo button = getJavaInfoByName("button");
    assertTrue(canvas.isExactlyCanvas());
    assertFalse(button.isExactlyCanvas());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution location/size method and fetching bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * In GWT2 we need to update absolute bounds on "2px" to reflect border of "RootPanel".
   */
  public void test_absoluteBounds_inGWT2() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    setBorder('1px solid red');",
            "  }",
            "}");
    canvas.refresh();
    // "red" border should be at (0,0)
    {
      Image image = canvas.getImage();
      RGB rgb = getPixelRGB(image, 0, 0);
      assertRGB(rgb, 0xFF, 0x00, 0x00);
    }
  }

  public void test_onCanvas_setLeft_setTop() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      button.setLeft(100);",
            "      button.setTop(50);",
            "      addChild(button);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    // check that location applied
    Rectangle bounds = button.getBounds();
    assertThat(bounds.x).isEqualTo(100);
    assertThat(bounds.y).isEqualTo(50);
    assertThat(bounds.width).isEqualTo(BUTTON_WIDTH);
    assertThat(bounds.height).isEqualTo(BUTTON_HEIGHT);
  }

  public void test_onCanvas_moveTo() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "      button.moveTo(100, 50);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    // check that location applied
    Rectangle bounds = button.getBounds();
    assertThat(bounds.x).isEqualTo(100);
    assertThat(bounds.y).isEqualTo(50);
    assertThat(bounds.width).isEqualTo(BUTTON_WIDTH);
    assertThat(bounds.height).isEqualTo(BUTTON_HEIGHT);
  }

  public void test_onCanvas_setRect() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "      button.setRect(100, 50, 200, 75);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    // check that location/size applied
    Rectangle bounds = button.getBounds();
    assertThat(bounds.x).isEqualTo(100);
    assertThat(bounds.y).isEqualTo(50);
    assertThat(bounds.width).isEqualTo(200);
    assertThat(bounds.height).isEqualTo(75);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Size
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setSize_resizeTo() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "      button.resizeTo(50, 25);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    //
    button.getSizeSupport().setSize(100, 50);
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      button.setSize('100px', '50px');",
        "      addChild(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_setSize_setRect_intWidthHeight() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "      button.setRect(1, 2, 50, 25);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    //
    button.getSizeSupport().setSize(100, 50);
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      addChild(button);",
        "      button.setRect(1, 2, 100, 50);",
        "    }",
        "  }",
        "}");
  }

  public void test_setSize_setRect_intWidth() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "      button.setRect(1, 2, 50, 25);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    //
    button.getSizeSupport().setSize("100px", null);
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      addChild(button);",
        "      button.setRect(1, 2, 100, 25);",
        "    }",
        "  }",
        "}");
  }

  public void test_setSize_setRect_intHeight() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "      button.setRect(1, 2, 50, 25);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    //
    button.getSizeSupport().setSize(null, "75px");
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      addChild(button);",
        "      button.setRect(1, 2, 50, 75);",
        "    }",
        "  }",
        "}");
  }

  public void test_setSize_setRect_stringWidthHeight() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "      button.setRect(1, 2, 50, 25);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    //
    button.getSizeSupport().setSize("75%", "50px");
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      button.setSize('75%', '50px');",
        "      addChild(button);",
        "      button.moveTo(1, 2);",
        "    }",
        "  }",
        "}");
  }

  public void test_setSize_setRect_stringWidth() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "      button.setRect(1, 2, 50, 25);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    //
    button.getSizeSupport().setSize("75%", null);
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      button.setWidth('75%');",
        "      addChild(button);",
        "      button.moveTo(1, 2);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // BOUNDS
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_BOUNDS_setLocationSize_removeExistingInvocations() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "      button.setRect(1, 2, 3, 4);",
            "      button.moveTo(1, 2);",
            "      button.setLeft(1);",
            "      button.setLeft('1px');",
            "      button.setTop(2);",
            "      button.setTop('2px');",
            "      button.resizeTo(3, 4);",
            "      button.setSize('3px', '4px');",
            "      button.setWidth(3);",
            "      button.setHeight(4);",
            "      button.setWidth('3px');",
            "      button.setHeight('4px');",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    //
    canvas.command_BOUNDS(button, new Point(10, 20), new Dimension(50, 25));
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      addChild(button);",
        "      button.setRect(10, 20, 50, 25);",
        "    }",
        "  }",
        "}");
  }

  public void test_BOUNDS_setRect_updateLocation() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "      button.setRect(1, 2, 50, 25);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    //
    canvas.command_BOUNDS(button, new Point(10, 20), null);
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      addChild(button);",
        "      button.setRect(10, 20, 50, 25);",
        "    }",
        "  }",
        "}");
  }

  public void test_BOUNDS_setRect_setSize() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "      button.setRect(1, 2, 3, 4);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    //
    canvas.command_BOUNDS(button, null, new Dimension(100, 25));
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      addChild(button);",
        "      button.setRect(1, 2, 100, 25);",
        "    }",
        "  }",
        "}");
  }

  public void test_BOUNDS_moveTo_updateLocation() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "      button.moveTo(1, 2);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    //
    canvas.command_BOUNDS(button, new Point(10, 20), null);
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      addChild(button);",
        "      button.moveTo(10, 20);",
        "    }",
        "  }",
        "}");
  }

  public void test_BOUNDS_setLocation() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    //
    canvas.command_BOUNDS(button, new Point(10, 20), null);
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      addChild(button);",
        "      button.moveTo(10, 20);",
        "    }",
        "  }",
        "}");
  }

  public void test_BOUNDS_setLocation_removeLeftTop() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "      button.setLeft(1);",
            "      button.setTop(2);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    //
    canvas.command_BOUNDS(button, new Point(10, 20), null);
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      addChild(button);",
        "      button.moveTo(10, 20);",
        "    }",
        "  }",
        "}");
  }

  public void test_BOUNDS_moveTo_setSize() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button('My Button');",
            "      addChild(button);",
            "      button.moveTo(1, 2);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    //
    canvas.command_BOUNDS(button, null, new Dimension(100, 25));
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button('My Button');",
        "      addChild(button);",
        "      button.setRect(1, 2, 100, 25);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CanvasInfo#command_absolute_CREATE(WidgetInfo, WidgetInfo)}.
   */
  public void test_CREATE_noSibling() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Canvas {",
            "  public Test() {",
            "  }",
            "}");
    canvas.refresh();
    assertThat(canvas.getWidgets()).isEmpty();
    //
    WidgetInfo newButton = createButton();
    canvas.command_absolute_CREATE(newButton, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      addChild(button);",
        "    }",
        "  }",
        "}");
    assertThat(canvas.getWidgets()).containsExactly(newButton);
  }

  /**
   * Test for {@link CanvasInfo#command_absolute_CREATE(WidgetInfo, WidgetInfo)}.
   */
  public void test_CREATE_beforeSibling() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      addChild(button_1);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button_1 = getJavaInfoByName("button_1");
    //
    WidgetInfo newButton = createButton();
    canvas.command_absolute_CREATE(newButton, button_1);
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      addChild(button);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      addChild(button_1);",
        "    }",
        "  }",
        "}");
    assertThat(canvas.getWidgets()).containsExactly(newButton, button_1);
  }

  /**
   * Test for {@link CanvasInfo#command_absolute_CREATE(WidgetInfo, WidgetInfo)}.
   */
  public void test_CREATE_Widget() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Canvas {",
            "  public Test() {",
            "  }",
            "}");
    canvas.refresh();
    assertThat(canvas.getWidgets()).isEmpty();
    // create GWT TextBox
    WidgetInfo newTextBox = createJavaInfo("com.google.gwt.user.client.ui.TextBox");
    canvas.command_absolute_CREATE(newTextBox, null);
    // check
    assertThat(canvas.getWidgets()).excludes(newTextBox);
    WidgetCanvasInfo widgetCanvas = canvas.getChildren(WidgetCanvasInfo.class).get(0);
    // check presentation
    {
      IObjectPresentation presentation = widgetCanvas.getPresentation();
      IObjectPresentation widgetPresentation = newTextBox.getPresentation();
      assertThat(presentation.getText().startsWith(widgetPresentation.getText())).isTrue();
      assertThat(presentation.getIcon()).isSameAs(widgetPresentation.getIcon());
    }
    assertThat(widgetCanvas.getWidget()).isSameAs(newTextBox);
    assertEditor(
        "import com.google.gwt.user.client.ui.TextBox;",
        "// filler filler filler",
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      WidgetCanvas widgetCanvas = new WidgetCanvas(new TextBox());",
        "      addChild(widgetCanvas);",
        "    }",
        "  }",
        "}");
    // check presentation
    IObjectPresentation presentation = widgetCanvas.getPresentation();
    assertThat(presentation.getChildrenGraphical()).excludes(newTextBox);
    assertThat(presentation.getChildrenTree()).excludes(newTextBox);
    // check properties
    {
      Property[] canvasProperties = widgetCanvas.getProperties();
      Property[] textProperties = newTextBox.getProperties();
      assertThat(canvasProperties).contains((Object[]) textProperties);
    }
    Property canvasProperty = widgetCanvas.getPropertyByTitle("Canvas");
    assertThat(canvasProperty).isInstanceOf(ComplexProperty.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CanvasInfo#command_absolute_MOVE(WidgetInfo, WidgetInfo)}.
   */
  public void test_MOVE() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      addChild(button_1);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      addChild(button_2);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button_1 = getJavaInfoByName("button_1");
    CanvasInfo button_2 = getJavaInfoByName("button_2");
    assertThat(canvas.getWidgets()).containsExactly(button_1, button_2);
    //
    canvas.command_absolute_MOVE(button_2, button_1);
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      Button button_2 = new Button();",
        "      addChild(button_2);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      addChild(button_1);",
        "    }",
        "  }",
        "}");
    assertThat(canvas.getWidgets()).containsExactly(button_2, button_1);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move out
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When we move {@link WidgetInfo} out of {@link CanvasInfo}, its location information should be
   * removed.
   */
  public void test_moveOut_removeLocation() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      button.setLeft(100);",
            "      button.setTop(50);",
            "      addChild(button);",
            "    }",
            "    {",
            "      VLayout vLayout = new VLayout();",
            "      addChild(vLayout);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    LayoutInfo layout = getJavaInfoByName("vLayout");
    //
    FlowContainer flowContainer = new FlowContainerFactory(layout, true).get().get(0);
    flowContainer.command_MOVE(button, null);
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      VLayout vLayout = new VLayout();",
        "      {",
        "        Button button = new Button();",
        "        vLayout.addMember(button);",
        "      }",
        "      addChild(vLayout);",
        "    }",
        "  }",
        "}");
  }

  /**
   * When we move {@link WidgetInfo} out of {@link CanvasInfo}, its location information should be
   * removed.
   */
  public void test_moveOut_removeLocation_setRect() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      button.setRect(100, 50, 200, 40);",
            "      addChild(button);",
            "    }",
            "    {",
            "      VLayout vLayout = new VLayout();",
            "      addChild(vLayout);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    LayoutInfo layout = getJavaInfoByName("vLayout");
    //
    FlowContainer flowContainer = new FlowContainerFactory(layout, true).get().get(0);
    flowContainer.command_MOVE(button, null);
    assertEditor(
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      VLayout vLayout = new VLayout();",
        "      {",
        "        Button button = new Button();",
        "        vLayout.addMember(button);",
        "        button.resizeTo(200, 40);",
        "      }",
        "      addChild(vLayout);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property "Bounds"
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_boundsProperty() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      addChild(button);",
            "      button.setRect(1, 2, 3, 4);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    //
    Property boundsProperty = button.getPropertyByTitle("Bounds");
    assertNotNull(boundsProperty);
    // x
    {
      Property xProperty = PropertyUtils.getByPath(button, "Bounds/x");
      assertNotNull(xProperty);
      assertEquals(1, xProperty.getValue());
      xProperty.setValue(10);
      assertEditor(
          "public class Test extends Canvas {",
          "  public Test() {",
          "    {",
          "      Button button = new Button();",
          "      addChild(button);",
          "      button.setRect(10, 2, 3, 4);",
          "    }",
          "  }",
          "}");
    }
    // y
    {
      Property yProperty = PropertyUtils.getByPath(button, "Bounds/y");
      assertNotNull(yProperty);
      assertEquals(2, yProperty.getValue());
      yProperty.setValue(20);
      assertEditor(
          "public class Test extends Canvas {",
          "  public Test() {",
          "    {",
          "      Button button = new Button();",
          "      addChild(button);",
          "      button.setRect(10, 20, 3, 4);",
          "    }",
          "  }",
          "}");
    }
    // width
    {
      Property widthProperty = PropertyUtils.getByPath(button, "Bounds/width");
      assertNotNull(widthProperty);
      assertEquals(3, widthProperty.getValue());
      widthProperty.setValue(30);
      assertEditor(
          "public class Test extends Canvas {",
          "  public Test() {",
          "    {",
          "      Button button = new Button();",
          "      addChild(button);",
          "      button.setRect(10, 20, 30, 4);",
          "    }",
          "  }",
          "}");
    }
    // height
    {
      Property heightProperty = PropertyUtils.getByPath(button, "Bounds/height");
      assertNotNull(heightProperty);
      assertEquals(4, heightProperty.getValue());
      heightProperty.setValue(40);
      assertEditor(
          "public class Test extends Canvas {",
          "  public Test() {",
          "    {",
          "      Button button = new Button();",
          "      addChild(button);",
          "      button.setRect(10, 20, 30, 40);",
          "    }",
          "  }",
          "}");
    }
  }

  /**
   * Root canvas must ignore any set location invocations.
   */
  public void test_root_noPosition() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    canvas.setLeft('30%');",
            "    canvas.draw();",
            "  }",
            "}");
    canvas.refresh();
    // check left position 
    {
      Integer left = (Integer) ReflectionUtils.invokeMethod2(canvas.getObject(), "getLeft");
      assertThat(left).isEqualTo(0);
    }
    {
      Integer left = (Integer) ReflectionUtils.invokeMethod2(canvas.getObject(), "getAbsoluteLeft");
      assertThat(left).isEqualTo(0);
    }
    //
    assertThat(canvas.getAbsoluteBounds()).isEqualTo(new Rectangle(0, 0, 450, 300));
  }
}