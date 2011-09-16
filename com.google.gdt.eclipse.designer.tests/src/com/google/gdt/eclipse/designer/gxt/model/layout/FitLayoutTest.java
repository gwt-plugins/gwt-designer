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
import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Test for {@link FitLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class FitLayoutTest extends GxtModelTest {
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
    // set FitLayout
    FitLayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.FitLayout");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FitLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FitLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FitLayout} {empty} {/setLayout(new FitLayout())/}");
    assertSame(layout, container.getLayout());
  }

  public void test_command_CREATE() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FitLayout());",
            "  }",
            "}");
    container.refresh();
    FitLayoutInfo layout = (FitLayoutInfo) container.getLayout();
    //
    ComponentInfo newButton = createButton();
    layout.command_CREATE(newButton, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FitLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FitLayout())/ /add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FitLayout} {empty} {/setLayout(new FitLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button empty} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FitData} {virtual-layout-data} {}");
  }

  /**
   * Test for simple container support.
   */
  public void test_simpleContainer() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FitLayout());",
            "  }",
            "}");
    container.refresh();
    FitLayoutInfo layout = (FitLayoutInfo) container.getLayout();
    SimpleContainer simpleContainer = new SimpleContainerFactory(layout, true).get().get(0);
    // empty initially
    assertTrue(simpleContainer.isEmpty());
    // add new Button
    ComponentInfo newButton = createButton();
    assertTrue(simpleContainer.validateComponent(newButton));
    simpleContainer.command_CREATE(newButton);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FitLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // has child
    assertFalse(simpleContainer.isEmpty());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Margins
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMargin_get() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FitLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FitData(1, 2, 3, 4));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FitDataInfo fitData = FitLayoutInfo.getFitData(button);
    // check margins
    assertSame(Property.UNKNOWN_VALUE, fitData.getMarginAll());
    assertEquals(1, fitData.getMarginTop());
    assertEquals(2, fitData.getMarginRight());
    assertEquals(3, fitData.getMarginBottom());
    assertEquals(4, fitData.getMarginLeft());
  }

  public void test_getMargin_virtual() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FitLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FitDataInfo fitData = FitLayoutInfo.getFitData(button);
    // check margins
    assertEquals(0, fitData.getMarginAll());
    assertEquals(0, fitData.getMarginTop());
    assertEquals(0, fitData.getMarginRight());
    assertEquals(0, fitData.getMarginBottom());
    assertEquals(0, fitData.getMarginLeft());
  }

  public void test_marginProperties_get() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FitLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FitData(1, 2, 3, 4));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FitDataInfo fitData = FitLayoutInfo.getFitData(button);
    // check margins
    assertSame(Property.UNKNOWN_VALUE, fitData.getPropertyByTitle("margin-all").getValue());
    assertEquals(1, fitData.getPropertyByTitle("margin-top").getValue());
    assertEquals(2, fitData.getPropertyByTitle("margin-right").getValue());
    assertEquals(3, fitData.getPropertyByTitle("margin-bottom").getValue());
    assertEquals(4, fitData.getPropertyByTitle("margin-left").getValue());
  }
}