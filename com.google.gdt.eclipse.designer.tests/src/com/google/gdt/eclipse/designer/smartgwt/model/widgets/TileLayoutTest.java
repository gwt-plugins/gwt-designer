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

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.TileLayoutInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test <code>com.smartgwt.client.widgets.tile.TileLayout</code>
 * 
 * @author scheglov_ke
 */
public class TileLayoutTest extends SmartGwtModelTest {
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
  public void test_parse_onRootPanel() throws Exception {
    dontUseSharedGWTState();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      TileLayout tileLayout = new TileLayout();",
            "      rootPanel.add(tileLayout);",
            "      {",
            "        Button button = new Button();",
            "        tileLayout.addTile(button);",
            "      }",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(tileLayout)/}",
        "  {new: com.smartgwt.client.widgets.tile.TileLayout} {local-unique: tileLayout} {/new TileLayout()/ /rootPanel.add(tileLayout)/ /tileLayout.addTile(button)/}",
        "    {new: com.smartgwt.client.widgets.Button} {local-unique: button} {/new Button()/ /tileLayout.addTile(button)/}");
    frame.refresh();
    //
    CanvasInfo button = getJavaInfoByName("button");
    Rectangle bounds = button.getBounds();
    assertThat(bounds.x).isGreaterThanOrEqualTo(0);
    assertThat(bounds.y).isGreaterThanOrEqualTo(0);
  }

  public void test_parse_this() throws Exception {
    TileLayoutInfo layout =
        parseJavaInfo(
            "public class Test extends TileLayout {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      addTile(button);",
            "    }",
            "  }",
            "}");
    layout.refresh();
    CanvasInfo button = getJavaInfoByName("button");
    Rectangle bounds = button.getBounds();
    assertThat(bounds.x).isEqualTo(5);
    assertThat(bounds.y).isEqualTo(5);
  }

  public void test_CREATE() throws Exception {
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
    TileLayoutInfo newLayout = createJavaInfo("com.smartgwt.client.widgets.tile.TileLayout");
    canvas.command_absolute_CREATE(newLayout, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends Canvas {",
        "  public Test() {",
        "    {",
        "      TileLayout tileLayout = new TileLayout();",
        "      addChild(tileLayout);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Flow container
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_flowContainer_CREATE() throws Exception {
    TileLayoutInfo layout =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends TileLayout {",
            "  public Test() {",
            "  }",
            "}");
    layout.refresh();
    // prepare FlowContainer
    FlowContainer flowContainer;
    {
      List<FlowContainer> flowContainers = new FlowContainerFactory(layout, true).get();
      assertThat(flowContainers).hasSize(1);
      flowContainer = flowContainers.get(0);
      assertTrue(flowContainer.isHorizontal());
    }
    // do create
    WidgetInfo newButton = createButton();
    assertTrue(flowContainer.validateComponent(newButton));
    flowContainer.command_CREATE(newButton, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends TileLayout {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      addTile(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_flowContainer_MOVE() throws Exception {
    TileLayoutInfo layout =
        parseJavaInfo(
            "public class Test extends TileLayout {",
            "  public Test() {",
            "    setRect(0, 0, 350, 200);",
            "    {",
            "      Button button_1 = new Button();",
            "      addTile(button_1);",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      addTile(button_2);",
            "    }",
            "  }",
            "}");
    CanvasInfo button_1 = getJavaInfoByName("button_1");
    CanvasInfo button_2 = getJavaInfoByName("button_2");
    layout.refresh();
    //
    FlowContainer flowContainer = new FlowContainerFactory(layout, true).get().get(0);
    assertTrue(flowContainer.validateComponent(button_2));
    assertTrue(flowContainer.validateReference(button_1));
    flowContainer.command_MOVE(button_2, button_1);
    assertEditor(
        "public class Test extends TileLayout {",
        "  public Test() {",
        "    setRect(0, 0, 350, 200);",
        "    {",
        "      Button button_2 = new Button();",
        "      addTile(button_2);",
        "    }",
        "    {",
        "      Button button_1 = new Button();",
        "      addTile(button_1);",
        "    }",
        "  }",
        "}");
  }
}