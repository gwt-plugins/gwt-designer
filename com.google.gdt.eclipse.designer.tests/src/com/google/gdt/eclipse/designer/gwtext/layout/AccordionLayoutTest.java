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
package com.google.gdt.eclipse.designer.gwtext.layout;

import com.google.gdt.eclipse.designer.gwtext.GwtExtModelTest;
import com.google.gdt.eclipse.designer.gwtext.model.layout.AccordionLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link AccordionLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class AccordionLayoutTest extends GwtExtModelTest {
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
  public void test_parse() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AccordionLayout());",
            "    {",
            "      Panel internalPanel = new Panel('Required title');",
            "      add(internalPanel);",
            "    }",
            "  }",
            "}");
    AccordionLayoutInfo layout = (AccordionLayoutInfo) panel.getLayout();
    // no LayoutData
    {
      WidgetInfo internalPanel = panel.getChildrenWidgets().get(0);
      assertNull(LayoutInfo.getLayoutData(internalPanel));
    }
    // no SimpleContainer
    {
      List<SimpleContainer> containers = new SimpleContainerFactory(layout, true).get();
      assertThat(containers).isEmpty();
    }
    {
      List<SimpleContainer> containers = new SimpleContainerFactory(layout, false).get();
      assertThat(containers).isEmpty();
    }
    // has FlowContainer
    {
      List<FlowContainer> containers = new FlowContainerFactory(layout, true).get();
      assertThat(containers).hasSize(1);
    }
  }

  public void test_CREATE_normalPanel_withTitle() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AccordionLayout());",
            "  }",
            "}");
    panel.refresh();
    AccordionLayoutInfo layout = (AccordionLayoutInfo) panel.getLayout();
    //
    FlowContainer flowContainer = new FlowContainerFactory(layout, true).get().get(0);
    PanelInfo newPanel = createJavaInfo("com.gwtext.client.widgets.Panel", null);
    flowContainer.command_CREATE(newPanel, null);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AccordionLayout());",
        "    {",
        "      Panel panel = new Panel('New Panel');",
        "      add(panel);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_emptyPanel_noTitle() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AccordionLayout());",
            "  }",
            "}");
    panel.refresh();
    AccordionLayoutInfo layout = (AccordionLayoutInfo) panel.getLayout();
    //
    FlowContainer flowContainer = new FlowContainerFactory(layout, true).get().get(0);
    PanelInfo newPanel = createJavaInfo("com.gwtext.client.widgets.Panel", "empty");
    flowContainer.command_CREATE(newPanel, null);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new AccordionLayout());",
        "    {",
        "      Panel panel = new Panel();",
        "      panel.setTitle('New Panel');",
        "      add(panel);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Manage expanded
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_manageExpanded() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AccordionLayout());",
            "    {",
            "      Panel panel_1 = new Panel('AAA');",
            "      add(panel_1);",
            "    }",
            "    {",
            "      Panel panel_2 = new Panel('BBB');",
            "      add(panel_2);",
            "    }",
            "    {",
            "      Panel panel_3 = new Panel('CCC');",
            "      add(panel_3);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    List<WidgetInfo> panels = panel.getChildrenWidgets();
    // initially "panel_1" is expanded
    assertExpanded(panel, 0);
    // notify about "panel_2"
    {
      boolean shouldRefresh = notifySelecting(panels.get(1));
      assertTrue(shouldRefresh);
      panel.refresh();
      // now "panel_2" is expanded
      assertExpanded(panel, 1);
    }
    // second notification about "panel_2" does not cause refresh()
    {
      boolean shouldRefresh = notifySelecting(panels.get(1));
      assertFalse(shouldRefresh);
    }
  }

  public void test_manageExpanded_innerWidget() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AccordionLayout());",
            "    {",
            "      Panel panel_1 = new Panel('AAA');",
            "      {",
            "        Panel panel_12 = new Panel('BBB');",
            "        {",
            "          Panel panel_123 = new Panel('CCC');",
            "          panel_12.add(panel_123);",
            "        }",
            "        panel_1.add(panel_12);",
            "      }",
            "      add(panel_1);",
            "    }",
            "    {",
            "      Panel panel_2 = new Panel('BBB');",
            "      add(panel_2);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    PanelInfo panel_1 = (PanelInfo) panel.getChildrenWidgets().get(0);
    PanelInfo panel_12 = (PanelInfo) panel_1.getChildrenWidgets().get(0);
    PanelInfo panel_123 = (PanelInfo) panel_12.getChildrenWidgets().get(0);
    PanelInfo panel_2 = (PanelInfo) panel.getChildrenWidgets().get(1);
    // initially "panel_1" is expanded
    assertExpanded(panel, 0);
    // notify about "panel_2"
    {
      boolean shouldRefresh = notifySelecting(panel_2);
      assertTrue(shouldRefresh);
      panel.refresh();
      // now "panel_2" is expanded
      assertExpanded(panel, 1);
    }
    // notify about "panel_123"
    {
      boolean shouldRefresh = notifySelecting(panel_123);
      assertTrue(shouldRefresh);
      panel.refresh();
      // now "panel_1" is expanded
      assertExpanded(panel, 0);
    }
  }

  public void test_manageExpanded_delete() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new AccordionLayout());",
            "    {",
            "      Panel panel_1 = new Panel('AAA');",
            "      add(panel_1);",
            "    }",
            "    {",
            "      Panel panel_2 = new Panel('BBB');",
            "      add(panel_2);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    List<WidgetInfo> panels = panel.getChildrenWidgets();
    // initially "panel_1" is expanded
    assertExpanded(panel, 0);
    // expand "panel_2"
    {
      notifySelecting(panels.get(1));
      panel.refresh();
      assertExpanded(panel, 1);
    }
    // delete "panel_2", so "panel_1" should be expanded
    {
      panels.get(1).delete();
      panel.refresh();
      assertExpanded(panel, 0);
    }
    // delete "panel_1", no exceptions
    {
      panels.get(0).delete();
      panel.refresh();
    }
  }

  private static void assertExpanded(PanelInfo accordionPanel, int expectedIndex) throws Exception {
    List<WidgetInfo> panels = accordionPanel.getChildrenWidgets();
    for (int index = 0; index < panels.size(); index++) {
      WidgetInfo panel = panels.get(index);
      boolean collapsed =
          (Boolean) ReflectionUtils.invokeMethod(panel.getObject(), "isCollapsed()");
      if (index == expectedIndex) {
        assertFalse(index + " should be expanded", collapsed);
      } else {
        assertTrue(index + " is unexpectedly expanded", collapsed);
      }
    }
  }
}