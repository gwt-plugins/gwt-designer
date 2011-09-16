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

/**
 * Test for {@link ColumnLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class ColumnLayoutTest extends GxtModelTest {
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
  // Test
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
    // set ColumnLayout
    ColumnLayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.ColumnLayout");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new ColumnLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new ColumnLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.ColumnLayout} {empty} {/setLayout(new ColumnLayout())/}");
    assertSame(layout, container.getLayout());
  }

  public void test_parse() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new ColumnLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    container.refresh();
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new ColumnLayout())/ /add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.ColumnLayout} {empty} {/setLayout(new ColumnLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.ColumnData} {virtual-layout-data} {}");
  }

  public void test_command_CREATE() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new ColumnLayout());",
            "  }",
            "}");
    container.refresh();
    ColumnLayoutInfo layout = (ColumnLayoutInfo) container.getLayout();
    //
    ComponentInfo newButton = createButton();
    layout.command_CREATE(newButton, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new ColumnLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new ColumnLayout())/ /add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.ColumnLayout} {empty} {/setLayout(new ColumnLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button empty} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.ColumnData} {virtual-layout-data} {}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ColumnData
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ColumnDataInfo#getWidth()}.
   */
  public void test_ColumnData_getWidth() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new ColumnLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new ColumnData(100.0));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    ColumnDataInfo columnData = ColumnLayoutInfo.getColumnData(button);
    assertEquals(100.0, columnData.getWidth(), 0.001);
  }

  /**
   * Test for {@link ColumnDataInfo#setWidth(double)}.
   */
  public void test_ColumnData_setWidth() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new ColumnLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new ColumnData(100.0));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    ColumnDataInfo columnData = ColumnLayoutInfo.getColumnData(button);
    // set new values
    columnData.setWidth(0.8);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new ColumnLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new ColumnData(0.8));",
        "    }",
        "  }",
        "}");
    assertEquals(0.8, columnData.getWidth(), 0.001);
  }

  public void test_ColumnData_setWidth_DEFAULT() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new ColumnLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new ColumnData(100.0));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    // initial state
    ColumnDataInfo columnData = ColumnLayoutInfo.getColumnData(button);
    assertEquals(100.0, columnData.getWidth(), 0.001);
    // set default
    columnData.setWidth(0);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new ColumnLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // old "columnData" was removed
    columnData = ColumnLayoutInfo.getColumnData(button);
    assertEquals(-1.0, columnData.getWidth(), 0.001);
  }
}