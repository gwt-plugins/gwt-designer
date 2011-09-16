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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import java.util.List;

/**
 * Test for {@link BorderLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class BorderLayoutTest extends GxtModelTest {
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
  public void test_setLayout() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    // set BorderLayout
    BorderLayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.BorderLayout");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new BorderLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.BorderLayout} {empty} {/setLayout(new BorderLayout())/}");
    assertSame(layout, container.getLayout());
  }

  /**
   * <code>Layout#setRenderHidden(boolean)</code> causes problem for "filler" component without
   * data.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44888
   */
  public void test_empty_when_setRenderHidden() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    BorderLayout borderLayout = new BorderLayout();",
            "    borderLayout.setRenderHidden(true);",
            "    setLayout(borderLayout);",
            "  }",
            "}");
    container.refresh();
    assertNoErrors(container);
  }

  /**
   * Test for {@link BorderLayoutDataInfo#getRegion()}.
   */
  public void test_getRegion() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(new Button());",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST));",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.NORTH));",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.EAST));",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.SOUTH));",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.CENTER));",
            "  }",
            "}");
    container.refresh();
    List<WidgetInfo> widgets = container.getWidgets();
    // virtual
    {
      WidgetInfo widget = widgets.get(0);
      BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
      assertEquals(null, borderData.getRegion());
    }
    // WEST
    {
      WidgetInfo widget = widgets.get(1);
      BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
      assertEquals("WEST", borderData.getRegion());
    }
    // NORTH
    {
      WidgetInfo widget = widgets.get(2);
      BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
      assertEquals("NORTH", borderData.getRegion());
    }
    // EAST
    {
      WidgetInfo widget = widgets.get(3);
      BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
      assertEquals("EAST", borderData.getRegion());
    }
    // SOUTH
    {
      WidgetInfo widget = widgets.get(4);
      BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
      assertEquals("SOUTH", borderData.getRegion());
    }
    // CENTER
    {
      WidgetInfo widget = widgets.get(5);
      BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
      assertEquals("CENTER", borderData.getRegion());
    }
  }

  /**
   * Test for {@link BorderLayoutDataInfo#setRegion(String)}.
   */
  public void test_setRegion() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST));",
            "  }",
            "}");
    container.refresh();
    // initial state
    WidgetInfo widget = container.getWidgets().get(0);
    BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
    assertEquals("WEST", borderData.getRegion());
    // WEST -> NORTH
    borderData.setRegion("NORTH");
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    add(new Button(), new BorderLayoutData(LayoutRegion.NORTH));",
        "  }",
        "}");
    assertEquals("NORTH", borderData.getRegion());
  }

  /**
   * Test for {@link BorderLayoutDataInfo#setRegion(String)}.
   */
  public void test_setRegion_virtual() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(new Button());",
            "  }",
            "}");
    container.refresh();
    // initial state
    WidgetInfo widget = container.getWidgets().get(0);
    BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
    assertEquals(null, borderData.getRegion());
    // WEST -> NORTH
    borderData.setRegion("NORTH");
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    add(new Button(), new BorderLayoutData(LayoutRegion.NORTH));",
        "  }",
        "}");
    assertEquals("NORTH", borderData.getRegion());
  }

  /**
   * Test for {@link BorderLayoutInfo#getWidget(String)}.
   */
  public void test_getWidget() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST));",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.CENTER));",
            "  }",
            "}");
    container.refresh();
    BorderLayoutInfo layout = (BorderLayoutInfo) container.getLayout();
    List<WidgetInfo> widgets = container.getWidgets();
    //
    assertSame(widgets.get(0), layout.getWidget("WEST"));
    assertSame(widgets.get(1), layout.getWidget("CENTER"));
    assertSame(null, layout.getWidget("NORTH"));
  }

  public void test_decorateWidgetText_withRegionName() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      Button button_0 = new Button();",
            "      add(button_0);",
            "    }",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1, new BorderLayoutData(LayoutRegion.WEST));",
            "    }",
            "  }",
            "}");
    container.refresh();
    List<WidgetInfo> widgets = container.getWidgets();
    // virtual
    {
      WidgetInfo widget = widgets.get(0);
      String text = ObjectsLabelProvider.INSTANCE.getText(widget);
      assertEquals("button_0", text);
    }
    // WEST
    {
      WidgetInfo widget = widgets.get(1);
      String text = ObjectsLabelProvider.INSTANCE.getText(widget);
      assertEquals("WEST - button_1", text);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Size
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link BorderLayoutDataInfo#getSize()}.
   */
  public void test_getSize() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST, 100.0f));",
            "  }",
            "}");
    container.refresh();
    //
    WidgetInfo widget = container.getWidgets().get(0);
    BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
    assertEquals(100.0f, borderData.getSize(), 0.001f);
  }

  /**
   * Test for {@link BorderLayoutDataInfo#getSize()}.
   */
  public void test_getSize_default() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST));",
            "  }",
            "}");
    container.refresh();
    //
    WidgetInfo widget = container.getWidgets().get(0);
    BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
    assertEquals(200.0f, borderData.getSize(), 0.001f);
  }

  /**
   * Test for {@link BorderLayoutDataInfo#getSize()}.
   */
  public void test_getSize_virtual() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(new Button());",
            "  }",
            "}");
    container.refresh();
    //
    WidgetInfo widget = container.getWidgets().get(0);
    BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
    assertEquals(200.0f, borderData.getSize(), 0.001f);
  }

  /**
   * Test for {@link BorderLayoutDataInfo#setSize(float)}.
   */
  public void test_setSize() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST, 150.0f));",
            "  }",
            "}");
    container.refresh();
    //
    WidgetInfo widget = container.getWidgets().get(0);
    BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
    borderData.setSize(150.0f);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST, 150.0f));",
        "  }",
        "}");
    assertEquals(150.0f, borderData.getSize(), 0.001f);
  }

  /**
   * Test for {@link BorderLayoutDataInfo#setSize(float)}.
   */
  public void test_setSize_addConstructorArgument() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST));",
            "  }",
            "}");
    container.refresh();
    //
    WidgetInfo widget = container.getWidgets().get(0);
    BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
    borderData.setSize(150.0f);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST, 150.0f));",
        "  }",
        "}");
    assertEquals(150.0f, borderData.getSize(), 0.001f);
  }

  /**
   * Test for {@link BorderLayoutDataInfo#setSize(float)}.
   */
  public void test_setSize_removeConstructorArgument_set200() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST, 150.0f));",
            "  }",
            "}");
    container.refresh();
    //
    WidgetInfo widget = container.getWidgets().get(0);
    BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
    borderData.setSize(200.0f);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST));",
        "  }",
        "}");
    assertEquals(200.0f, borderData.getSize(), 0.001f);
  }

  /**
   * Test for {@link BorderLayoutDataInfo#setSize(float)}.
   */
  public void test_setSize_removeConstructorArgument_setUnknown() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST, 150.0f));",
            "  }",
            "}");
    container.refresh();
    //
    WidgetInfo widget = container.getWidgets().get(0);
    BorderLayoutDataInfo borderData = BorderLayoutInfo.getBorderData(widget);
    borderData.getPropertyByTitle("size").setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST));",
        "  }",
        "}");
    assertEquals(200.0f, borderData.getSize(), 0.001f);
  }

  /**
   * Size has default value <code>200</code>, but as {@link Integer}, not {@link Float}.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43223
   */
  public void test_removeConstructorArgument_sizeIsDefault_butInteger() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST, 200));",
            "  }",
            "}");
    container.refresh();
    //
    ExecutionUtils.refresh(container);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    add(new Button(), new BorderLayoutData(LayoutRegion.WEST));",
        "  }",
        "}");
  }
}