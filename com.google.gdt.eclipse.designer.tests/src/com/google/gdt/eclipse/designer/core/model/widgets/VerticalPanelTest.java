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
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.VerticalPanelInfo;

import org.eclipse.wb.internal.core.model.util.PropertyUtils;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link VerticalPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class VerticalPanelTest extends GwtModelTest {
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
   * Even empty <code>HorizontalPanel</code> has some reasonable size.
   */
  public void test_empty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    VerticalPanel panel = new VerticalPanel();",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    VerticalPanelInfo panel = (VerticalPanelInfo) frame.getChildrenWidgets().get(0);
    assertThat(panel.getBounds().width).isGreaterThan(140);
    assertThat(panel.getBounds().height).isGreaterThan(20);
  }

  /**
   * Test for "Cell" property.
   */
  public void test_propertyCell() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    VerticalPanel panel = new VerticalPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      panel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    //
    WidgetInfo button = getJavaInfoByName("button");
    assertNotNull(PropertyUtils.getByPath(button, "Cell"));
  }

  public void test_flowContainers() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends VerticalPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHasWidgetFlowContainer(panel, true);
    assertHasWidgetFlowContainer(panel, false);
  }
}