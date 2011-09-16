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

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link ToolBarInfo}.
 * 
 * @author scheglov_ke
 */
public class ToolBarTest extends GxtModelTest {
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
  // MenuBar
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Even empty <code>ToolBar</code> should have reasonable size.
   */
  public void test_parseEmpty() throws Exception {
    ContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      ToolBar bar = new ToolBar();",
            "      add(bar);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(bar)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.toolbar.ToolBar} {local-unique: bar} {/new ToolBar()/ /add(bar)/}");
    container.refresh();
    ToolBarInfo bar = (ToolBarInfo) container.getWidgets().get(0);
    assertThat(bar.getItems()).isEmpty();
    // 
    Rectangle bounds = bar.getBounds();
    assertThat(bounds.width).isGreaterThan(100);
    assertThat(bounds.height).isGreaterThan(15);
  }

  public void test_parse() throws Exception {
    ToolBarInfo bar =
        parseJavaInfo(
            "public class Test extends ToolBar {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.toolbar.ToolBar} {this} {/add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}");
    assertThat(bar.getItems()).hasSize(1);
  }

  public void test_CREATE() throws Exception {
    ToolBarInfo bar =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends ToolBar {",
            "  public Test() {",
            "  }",
            "}");
    bar.refresh();
    //
    FlowContainer flowContainer = new FlowContainerFactory(bar, true).get().get(0);
    flowContainer.command_CREATE(createButton(), null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends ToolBar {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.toolbar.ToolBar} {this} {/add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button empty} {local-unique: button} {/new Button()/ /add(button)/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FillToolItem
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for using <code>com.extjs.gxt.ui.client.widget.toolbar.FillToolItem</code>.
   */
  public void test_FillToolItem_middle() throws Exception {
    ToolBarInfo bar =
        parseJavaInfo(
            "public class Test extends ToolBar {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1);",
            "    }",
            "    {",
            "      FillToolItem item = new FillToolItem();",
            "      add(item);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.toolbar.ToolBar} {this} {/add(button_1)/ /add(item)/ /add(button_2)/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button_1} {/new Button()/ /add(button_1)/}",
        "  {new: com.extjs.gxt.ui.client.widget.toolbar.FillToolItem} {local-unique: item} {/new FillToolItem()/ /add(item)/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button_2} {/new Button()/ /add(button_2)/}");
    bar.refresh();
    // prepare bounds for prev/next items
    Rectangle bounds_1 = bar.getItems().get(0).getBounds();
    Rectangle bounds_2 = bar.getItems().get(2).getBounds();
    // check "fill" bounds
    ComponentInfo fillItem = bar.getItems().get(1);
    Rectangle bounds = fillItem.getBounds();
    assertThat(bounds.left()).isEqualTo(bounds_1.right());
    assertThat(bounds.right()).isEqualTo(bounds_2.left());
  }

  /**
   * Test for using <code>com.extjs.gxt.ui.client.widget.toolbar.FillToolItem</code>.
   */
  public void test_FillToolItem_first() throws Exception {
    ToolBarInfo bar =
        parseJavaInfo(
            "public class Test extends ToolBar {",
            "  public Test() {",
            "    {",
            "      FillToolItem item = new FillToolItem();",
            "      add(item);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2);",
            "    }",
            "  }",
            "}");
    bar.refresh();
    // prepare bounds for prev/next items
    Rectangle bounds_2 = bar.getItems().get(1).getBounds();
    // check "fill" bounds
    ComponentInfo fillItem = bar.getItems().get(0);
    Rectangle bounds = fillItem.getBounds();
    assertThat(bounds.left()).isEqualTo(0);
    assertThat(bounds.right()).isEqualTo(bounds_2.left());
  }

  /**
   * Test for using <code>com.extjs.gxt.ui.client.widget.toolbar.FillToolItem</code>.
   */
  public void test_FillToolItem_last() throws Exception {
    ToolBarInfo bar =
        parseJavaInfo(
            "public class Test extends ToolBar {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button();",
            "      add(button_1);",
            "    }",
            "    {",
            "      FillToolItem item = new FillToolItem();",
            "      add(item);",
            "    }",
            "  }",
            "}");
    bar.refresh();
    // prepare bounds for prev/next items
    Rectangle bounds_1 = bar.getItems().get(0).getBounds();
    // check "fill" bounds
    ComponentInfo fillItem = bar.getItems().get(1);
    Rectangle bounds = fillItem.getBounds();
    assertThat(bounds.left()).isEqualTo(bounds_1.right());
    assertThat(bounds.right()).isEqualTo(bar.getBounds().width);
  }
}