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
import com.google.gdt.eclipse.designer.gwtext.model.layout.CardLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.core.model.property.Property;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link CardLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class CardLayoutTest extends GwtExtModelTest {
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
            "    setLayout(new CardLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    CardLayoutInfo layout = (CardLayoutInfo) panel.getLayout();
    // no LayoutData
    {
      WidgetInfo label = panel.getChildrenWidgets().get(0);
      assertNull(LayoutInfo.getLayoutData(label));
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
    // has FlowContainer for tree
    {
      List<FlowContainer> containers = new FlowContainerFactory(layout, false).get();
      assertThat(containers).hasSize(1);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Manage active
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_manageActive() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "    {",
            "      Panel panel_1 = new Panel();",
            "      add(panel_1);",
            "    }",
            "    {",
            "      Panel panel_2 = new Panel();",
            "      add(panel_2);",
            "    }",
            "    {",
            "      Panel panel_3 = new Panel();",
            "      add(panel_3);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    List<WidgetInfo> panels = panel.getChildrenWidgets();
    // initially "panel_1" is expanded
    assertActiveIndex(panel, 0);
    // notify about "panel_2"
    {
      boolean shouldRefresh = notifySelecting(panels.get(1));
      assertTrue(shouldRefresh);
      panel.refresh();
      // now "panel_2" is expanded
      assertActiveIndex(panel, 1);
    }
    // second notification about "panel_2" does not cause refresh()
    {
      boolean shouldRefresh = notifySelecting(panels.get(1));
      assertFalse(shouldRefresh);
    }
  }

  private static void assertActiveIndex(PanelInfo cardPanel, int expected) throws Exception {
    int actual = ((CardLayoutInfo) cardPanel.getLayout()).getActiveIndex();
    assertEquals(expected, actual);
  }

  public void test_case_40821() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "    {",
            "      Panel panel_1 = new Panel();",
            "      add(panel_1);",
            "    }",
            "    {",
            "      Panel panel_2 = new Panel();",
            "      add(panel_2);",
            "    }",
            "  }",
            "}");
    // layout
    CardLayoutInfo layout = (CardLayoutInfo) panel.getChildrenJava().get(0);
    assertSame(layout, panel.getLayout());
    // set active item
    Property activeItemProperty = layout.getPropertyByTitle("activeItem(int)");
    activeItemProperty.setValue(1);
    // check source
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    CardLayout cardLayout = new CardLayout();",
        "    setLayout(cardLayout);",
        "    {",
        "      Panel panel_1 = new Panel();",
        "      add(panel_1);",
        "    }",
        "    {",
        "      Panel panel_2 = new Panel();",
        "      add(panel_2);",
        "    }",
        "    cardLayout.setActiveItem(1);",
        "  }",
        "}");
  }
}