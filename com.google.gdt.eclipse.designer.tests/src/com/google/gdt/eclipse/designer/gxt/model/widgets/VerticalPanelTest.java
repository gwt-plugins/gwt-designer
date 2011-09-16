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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;

/**
 * Test for {@link VerticalPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class VerticalPanelTest extends GxtModelTest {
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
  public void test_parse_virtualLayoutData() throws Exception {
    VerticalPanelInfo panel =
        parseJavaInfo(
            "public class Test extends VerticalPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.VerticalPanel} {this} {/add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.TableData} {virtual-layout-data} {}");
    assertFalse(panel.hasLayout());
    // "button" has LayoutData property
    WidgetInfo button = panel.getWidgets().get(0);
    assertNotNull(button.getPropertyByTitle("LayoutData"));
  }

  public void test_parse_explicitLayoutData() throws Exception {
    VerticalPanelInfo panel =
        parseJavaInfo(
            "public class Test extends VerticalPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      add(button, new TableData());",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.VerticalPanel} {this} {/add(button, new TableData())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button, new TableData())/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.TableData} {empty} {/add(button, new TableData())/}");
    // "button" has LayoutData property
    WidgetInfo button = panel.getWidgets().get(0);
    assertNotNull(button.getPropertyByTitle("LayoutData"));
  }

  public void test_MOVE_out() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      VerticalPanel panel = new VerticalPanel();",
            "      {",
            "        Button button = new Button();",
            "        panel.add(button, new TableData());",
            "      }",
            "      add(panel);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(panel)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.VerticalPanel} {local-unique: panel} {/new VerticalPanel()/ /panel.add(button, new TableData())/ /add(panel)/}",
        "    {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /panel.add(button, new TableData())/}",
        "      {new: com.extjs.gxt.ui.client.widget.layout.TableData} {empty} {/panel.add(button, new TableData())/}");
    VerticalPanelInfo panel = (VerticalPanelInfo) container.getWidgets().get(0);
    WidgetInfo button = panel.getWidgets().get(0);
    // move "button" to "container"
    container.getLayout().command_MOVE(button, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      VerticalPanel panel = new VerticalPanel();",
        "      add(panel);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(panel)/ /add(button)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.VerticalPanel} {local-unique: panel} {/new VerticalPanel()/ /add(panel)/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}");
  }

  public void test_command_CREATE() throws Exception {
    VerticalPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends VerticalPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    //
    ComponentInfo newButton = createButton();
    FlowContainer flowContainer = new FlowContainerFactory(panel, true).get().get(0);
    flowContainer.command_CREATE(newButton, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends VerticalPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.VerticalPanel} {this} {/add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button empty} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.TableData} {virtual-layout-data} {}");
  }
}