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
import com.google.gdt.eclipse.designer.gxt.model.layout.table.TableRowLayoutInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;

/**
 * Test for {@link TableRowLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class TableRowLayoutTest extends GxtModelTest {
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
    // set TableRowLayout
    TableRowLayoutInfo layout =
        createJavaInfo("com.extjs.gxt.ui.client.widget.layout.TableRowLayout");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableRowLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new TableRowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.TableRowLayout} {empty} {/setLayout(new TableRowLayout())/}");
    assertSame(layout, container.getLayout());
  }

  public void test_parse() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableRowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    container.refresh();
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new TableRowLayout())/ /add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.TableRowLayout} {empty} {/setLayout(new TableRowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.TableData} {virtual-layout-data} {}");
  }

  public void test_command_CREATE() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableRowLayout());",
            "  }",
            "}");
    container.refresh();
    TableRowLayoutInfo layout = (TableRowLayoutInfo) container.getLayout();
    //
    ComponentInfo newButton = createButton();
    FlowContainer flowContainer = new FlowContainerFactory(layout, true).get().get(0);
    flowContainer.command_CREATE(newButton, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableRowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new TableRowLayout())/ /add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.TableRowLayout} {empty} {/setLayout(new TableRowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button empty} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.TableData} {virtual-layout-data} {}");
  }
}