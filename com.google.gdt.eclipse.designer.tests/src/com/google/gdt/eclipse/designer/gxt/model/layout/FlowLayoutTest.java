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

import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Test for {@link FlowLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class FlowLayoutTest extends GxtModelTest {
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
    // set FlowLayout
    LayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.FlowLayout");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout(5));",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FlowLayout(5))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/setLayout(new FlowLayout(5))/}");
    assertSame(layout, container.getLayout());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Margins
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMargin_virtual() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FlowDataInfo flowData = FlowLayoutInfo.getFlowData(button);
    // check margins
    assertEquals(0, flowData.getMarginAll());
    assertEquals(0, flowData.getMarginTop());
    assertEquals(0, flowData.getMarginRight());
    assertEquals(0, flowData.getMarginBottom());
    assertEquals(0, flowData.getMarginLeft());
  }

  public void test_getMargin() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FlowData(1, 2, 3, 4));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FlowDataInfo flowData = FlowLayoutInfo.getFlowData(button);
    // check margins
    assertEquals(1, flowData.getMarginTop());
    assertEquals(2, flowData.getMarginRight());
    assertEquals(3, flowData.getMarginBottom());
    assertEquals(4, flowData.getMarginLeft());
  }

  public void test_setMargin_constructor4() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FlowData(1, 2, 3, 4));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FlowDataInfo flowData = FlowLayoutInfo.getFlowData(button);
    // check margins
    flowData.setMarginTop(10);
    flowData.setMarginRight(20);
    flowData.setMarginBottom(30);
    flowData.setMarginLeft(40);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new FlowData(10, 20, 30, 40));",
        "    }",
        "  }",
        "}");
    assertEquals(10, flowData.getMarginTop());
    assertEquals(20, flowData.getMarginRight());
    assertEquals(30, flowData.getMarginBottom());
    assertEquals(40, flowData.getMarginLeft());
  }

  public void test_setMargin_constructor1() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FlowData(1));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FlowDataInfo flowData = FlowLayoutInfo.getFlowData(button);
    // check margins
    flowData.setMarginRight(20);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new FlowData(1, 20, 1, 1));",
        "    }",
        "  }",
        "}");
    assertEquals(20, flowData.getMarginRight());
    assertConstructorSignature(flowData, "<init>(int,int,int,int)");
  }

  public void test_setMargin_virtual() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FlowDataInfo flowData = FlowLayoutInfo.getFlowData(button);
    // check margins
    flowData.setMarginRight(20);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new FlowData(0, 20, 0, 0));",
        "    }",
        "  }",
        "}");
    assertEquals(20, flowData.getMarginRight());
  }

  public void test_marginProperties_get() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FlowData(1, 2, 3, 4));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FlowDataInfo flowData = FlowLayoutInfo.getFlowData(button);
    // check margins
    assertEquals(1, flowData.getPropertyByTitle("margin-top").getValue());
    assertEquals(2, flowData.getPropertyByTitle("margin-right").getValue());
    assertEquals(3, flowData.getPropertyByTitle("margin-bottom").getValue());
    assertEquals(4, flowData.getPropertyByTitle("margin-left").getValue());
  }

  public void test_marginProperties_set() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FlowData(1, 2, 3, 4));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FlowDataInfo flowData = FlowLayoutInfo.getFlowData(button);
    // set margins
    flowData.getPropertyByTitle("margin-top").setValue(10);
    flowData.getPropertyByTitle("margin-right").setValue(20);
    flowData.getPropertyByTitle("margin-bottom").setValue(30);
    flowData.getPropertyByTitle("margin-left").setValue(40);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new FlowData(10, 20, 30, 40));",
        "    }",
        "  }",
        "}");
    // clear "top"
    flowData.getPropertyByTitle("margin-top").setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new FlowData(0, 20, 30, 40));",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link FlowDataInfo#getMarginAll()}.
   */
  public void test_getMarginAll_noCommon() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FlowData(1, 2, 3, 4));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FlowDataInfo flowData = FlowLayoutInfo.getFlowData(button);
    // no common margin
    assertSame(Property.UNKNOWN_VALUE, flowData.getMarginAll());
    {
      Property property = flowData.getPropertyByTitle("margin-all");
      assertNotNull(property);
      assertFalse(property.isModified());
      assertEquals(Property.UNKNOWN_VALUE, property.getValue());
    }
  }

  /**
   * Test for {@link FlowDataInfo#getMarginAll()}.
   */
  public void test_getMarginAll_hasCommon() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FlowData(5, 5, 5, 5));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FlowDataInfo flowData = FlowLayoutInfo.getFlowData(button);
    // no common margin
    assertEquals(5, flowData.getMarginAll());
    {
      Property property = flowData.getPropertyByTitle("margin-all");
      assertNotNull(property);
      assertTrue(property.isModified());
      assertEquals(5, property.getValue());
    }
  }

  public void test_marginProperties_setAll() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FlowData(1, 2, 3, 4));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FlowDataInfo flowData = FlowLayoutInfo.getFlowData(button);
    // check margins
    flowData.getPropertyByTitle("margin-all").setValue(10);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new FlowData(10));",
        "    }",
        "  }",
        "}");
    assertConstructorSignature(flowData, "<init>(int)");
    {
      Property property = flowData.getPropertyByTitle("margin-all");
      assertNotNull(property);
      assertTrue(property.isModified());
      assertEquals(10, property.getValue());
    }
  }

  public void test_marginProperties_setAll_UNKNOWN() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FlowData(10));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FlowDataInfo flowData = FlowLayoutInfo.getFlowData(button);
    // initial state
    assertEquals(10, flowData.getPropertyByTitle("margin-all").getValue());
    // remove margins
    flowData.getPropertyByTitle("margin-all").setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // FlowData was removed, get new one
    flowData = FlowLayoutInfo.getFlowData(button);
    assertEquals(0, flowData.getPropertyByTitle("margin-all").getValue());
  }

  public void test_marginProperties_setAll_virtual() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FlowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FlowDataInfo flowData = FlowLayoutInfo.getFlowData(button);
    // check margins
    flowData.getPropertyByTitle("margin-all").setValue(10);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FlowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new FlowData(10));",
        "    }",
        "  }",
        "}");
    {
      Property property = flowData.getPropertyByTitle("margin-all");
      assertNotNull(property);
      assertTrue(property.isModified());
      assertEquals(10, property.getValue());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void assertConstructorSignature(FlowDataInfo flowData, String expected) {
    ConstructorCreationSupport creationSupport =
        (ConstructorCreationSupport) flowData.getCreationSupport();
    assertEquals(expected, creationSupport.getDescription().getSignature());
  }
}