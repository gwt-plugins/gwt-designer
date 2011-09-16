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
import com.google.gdt.eclipse.designer.gwtext.model.layout.FitLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

/**
 * Tests for {@link FitLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class FitLayoutTest extends GwtExtModelTest {
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
            "    setLayout(new FitLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    assertInstanceOf(FitLayoutInfo.class, panel.getLayout());
    //
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    assertNull(LayoutInfo.getLayoutData(label));
  }

  /**
   * Test for {@link FitLayoutInfo#command_CREATE(WidgetInfo)}.
   */
  public void test_CREATE() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new FitLayout());",
            "  }",
            "}");
    panel.refresh();
    FitLayoutInfo layout = (FitLayoutInfo) panel.getLayout();
    // create Label
    WidgetInfo label = createJavaInfo("com.gwtext.client.widgets.form.Label");
    layout.command_CREATE(label);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new FitLayout());",
        "    {",
        "      Label label = new Label('New Label');",
        "      add(label);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link FitLayoutInfo#command_ADD(WidgetInfo)}.
   */
  public void test_ADD() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Panel fitPanel = new Panel();",
            "      fitPanel.setLayout(new FitLayout());",
            "      add(fitPanel);",
            "    }",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    PanelInfo fitPanel = (PanelInfo) panel.getChildrenWidgets().get(0);
    WidgetInfo label = panel.getChildrenWidgets().get(1);
    FitLayoutInfo layout = (FitLayoutInfo) fitPanel.getLayout();
    // reparent Label
    layout.command_ADD(label);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Panel fitPanel = new Panel();",
        "      fitPanel.setLayout(new FitLayout());",
        "      {",
        "        Label label = new Label();",
        "        fitPanel.add(label);",
        "      }",
        "      add(fitPanel);",
        "    }",
        "  }",
        "}");
  }
}