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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.DockLayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootLayoutPanelInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.GenericProperty;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for <code>com.google.gwt.user.client.ui.SplitLayoutPanel</code>
 * 
 * @author scheglov_ke
 */
public class SplitLayoutPanelTest extends GwtModelTest {
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
  public void test_parse_ClassInstanceCreation() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    SplitLayoutPanel panel = new SplitLayoutPanel();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    assertHierarchy(
        "{RootLayoutPanel.get()} {local-unique: rootPanel} {/RootLayoutPanel.get()/ /rootPanel.add(panel)/}",
        "  {new: com.google.gwt.user.client.ui.SplitLayoutPanel} {local-unique: panel} {/new SplitLayoutPanel()/ /rootPanel.add(panel)/}");
    frame.refresh();
    DockLayoutPanelInfo panel = getJavaInfoByName("panel");
    // bounds
    assertEquals(new Rectangle(0, 0, 450, 300), panel.getBounds());
    // no Unit property
    {
      GenericProperty unitProperty = (GenericProperty) panel.getPropertyByTitle("Unit");
      assertNull(unitProperty);
    }
  }

  public void test_parse_this() throws Exception {
    DockLayoutPanelInfo panel =
        parseJavaInfo(
            "public class Test extends SplitLayoutPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      addWest(button, 200.0);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // bounds
    assertEquals(new Rectangle(0, 0, 450, 300), panel.getBounds());
    {
      WidgetInfo button = getJavaInfoByName("button");
      Rectangle bounds = button.getBounds();
      assertThat(bounds.width).isEqualTo(200);
      assertThat(bounds.height).isEqualTo(300);
    }
  }

  public void test_CREATE_it() throws Exception {
    RootLayoutPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "  }",
            "}");
    frame.refresh();
    //
    DockLayoutPanelInfo panel = createJavaInfo("com.google.gwt.user.client.ui.SplitLayoutPanel");
    frame.command_CREATE2(panel, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      SplitLayoutPanel splitLayoutPanel = new SplitLayoutPanel();",
        "      rootPanel.add(splitLayoutPanel);",
        "    }",
        "  }",
        "}");
  }
}