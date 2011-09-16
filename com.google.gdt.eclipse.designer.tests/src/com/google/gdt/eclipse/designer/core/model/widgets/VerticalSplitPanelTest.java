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
import com.google.gdt.eclipse.designer.model.widgets.panels.VerticalSplitPanelInfo;

import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link VerticalSplitPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class VerticalSplitPanelTest extends GwtModelTest {
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
  // getTopWidget() and getBottomWidget()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link VerticalSplitPanelInfo#getTopWidget()}.
   */
  public void test_getTopWidget_noWidget() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    VerticalSplitPanel panel = new VerticalSplitPanel();",
        "    add(panel);",
        "  }",
        "}");
    refresh();
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    assertSame(null, panel.getTopWidget());
  }

  /**
   * Test for {@link VerticalSplitPanelInfo#getTopWidget()}.
   */
  public void test_getTopWidget_setTopWidget() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    VerticalSplitPanel panel = new VerticalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setTopWidget(button);",
        "    }",
        "  }",
        "}");
    refresh();
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    assertSame(button, panel.getTopWidget());
  }

  /**
   * Test for {@link VerticalSplitPanelInfo#getBottomWidget()}.
   */
  public void test_getBottomWidget_noWidget() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    VerticalSplitPanel panel = new VerticalSplitPanel();",
        "    add(panel);",
        "  }",
        "}");
    refresh();
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    assertSame(null, panel.getBottomWidget());
  }

  /**
   * Test for {@link VerticalSplitPanelInfo#getBottomWidget()}.
   */
  public void test_getBottomWidget_setBottomWidget() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    VerticalSplitPanel panel = new VerticalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setBottomWidget(button);",
        "    }",
        "  }",
        "}");
    refresh();
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    assertSame(button, panel.getBottomWidget());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Region
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getEmptyRegion_noWidgets() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    VerticalSplitPanel panel = new VerticalSplitPanel();",
        "    add(panel);",
        "  }",
        "}");
    refresh();
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    assertEquals("top", panel.getEmptyRegion());
  }

  public void test_getEmptyRegion_hasTop() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    VerticalSplitPanel panel = new VerticalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setTopWidget(button);",
        "    }",
        "  }",
        "}");
    refresh();
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    assertEquals("bottom", panel.getEmptyRegion());
  }

  public void test_getEmptyRegion_hasBottom() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    VerticalSplitPanel panel = new VerticalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setBottomWidget(button);",
        "    }",
        "  }",
        "}");
    refresh();
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    assertEquals("top", panel.getEmptyRegion());
  }

  public void test_getEmptyRegion_hasTop_hasBottom() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    VerticalSplitPanel panel = new VerticalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button = new Button();",
        "      panel.setTopWidget(button);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      panel.setBottomWidget(button);",
        "    }",
        "  }",
        "}");
    refresh();
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    //
    assertEquals(null, panel.getEmptyRegion());
  }

  /**
   * Region should be reflected in child {@link WidgetInfo} title.
   */
  public void test_regionInTitle() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    VerticalSplitPanel panel = new VerticalSplitPanel();",
        "    add(panel);",
        "    {",
        "      Button button_1 = new Button();",
        "      panel.setTopWidget(button_1);",
        "    }",
        "    {",
        "      Button button_2 = new Button();",
        "      panel.setBottomWidget(button_2);",
        "    }",
        "  }",
        "}");
    refresh();
    VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
    WidgetInfo button_1 = panel.getChildrenWidgets().get(0);
    WidgetInfo button_2 = panel.getChildrenWidgets().get(1);
    // check title decorations
    {
      String title = ObjectsLabelProvider.INSTANCE.getText(button_1);
      assertThat(title).startsWith("top - ");
    }
    {
      String title = ObjectsLabelProvider.INSTANCE.getText(button_2);
      assertThat(title).startsWith("bottom - ");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for copy/paste {@link VerticalSplitPanelInfo} with its children.
   */
  public void test_clipboard() throws Exception {
    final ComplexPanelInfo frame =
        parseJavaInfo(
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "    {",
            "      VerticalSplitPanel panel = new VerticalSplitPanel();",
            "      add(panel);",
            "      {",
            "        Button button = new Button();",
            "        panel.setTopWidget(button);",
            "        button.setSize('100%', '100%');",
            "      }",
            "    }",
            "  }",
            "}");
    refresh();
    // do copy/paste
    {
      VerticalSplitPanelInfo panel = getJavaInfoByName("panel");
      doCopyPaste(panel, new PasteProcedure<WidgetInfo>() {
        public void run(WidgetInfo copy) throws Exception {
          flowContainer_CREATE(frame, copy, null);
        }
      });
    }
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      VerticalSplitPanel panel = new VerticalSplitPanel();",
        "      add(panel);",
        "      {",
        "        Button button = new Button();",
        "        panel.setTopWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "    {",
        "      VerticalSplitPanel verticalSplitPanel = new VerticalSplitPanel();",
        "      add(verticalSplitPanel);",
        "      {",
        "        Button button = new Button();",
        "        verticalSplitPanel.setTopWidget(button);",
        "        button.setSize('100%', '100%');",
        "      }",
        "    }",
        "  }",
        "}");
  }
}