/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gdt.eclipse.designer.gwtext.layout;

import com.google.gdt.eclipse.designer.gwtext.GwtExtModelTest;
import com.google.gdt.eclipse.designer.gwtext.model.layout.RowLayoutDataInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.RowLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

/**
 * Tests for {@link RowLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class RowLayoutTest extends GwtExtModelTest {
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
            "    setLayout(new RowLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    assertInstanceOf(RowLayoutInfo.class, panel.getLayout());
    //
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    assertNotNull(RowLayoutInfo.getRowData(label));
  }

  public void test_MOVE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    Panel panel = new Panel();",
            "    panel.setLayout(new RowLayout());",
            "    {",
            "      Label label_1 = new Label();",
            "      panel.add(label_1);",
            "    }",
            "    {",
            "      Label label_2 = new Label();",
            "      panel.add(label_2);",
            "    }",
            "    rootPanel.add(panel);",
            "  }",
            "}");
    frame.refresh();
    PanelInfo panel = (PanelInfo) frame.getChildrenWidgets().get(0);
    RowLayoutInfo layout = (RowLayoutInfo) panel.getLayout();
    //
    WidgetInfo label_1 = panel.getChildrenWidgets().get(0);
    layout.command_MOVE(label_1, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    Panel panel = new Panel();",
        "    panel.setLayout(new RowLayout());",
        "    {",
        "      Label label_2 = new Label();",
        "      panel.add(label_2);",
        "    }",
        "    {",
        "      Label label_1 = new Label();",
        "      panel.add(label_1);",
        "    }",
        "    rootPanel.add(panel);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // RowLayoutData
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link RowLayoutDataInfo#setHeight(int)} and
   * {@link RowLayoutDataInfo#setHeight(String)}.
   */
  public void test_RowLayoutData_setHeightMethods() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    RowLayoutDataInfo rowData = RowLayoutInfo.getRowData(label);
    // set "int"
    rowData.setHeight(50);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new RowLayoutData(50));",
        "    }",
        "  }",
        "}");
    // set "string"
    rowData.setHeight("20%");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new RowLayoutData('20%'));",
        "    }",
        "  }",
        "}");
    // set "int"
    rowData.setHeight(100);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new RowLayoutData(100));",
        "    }",
        "  }",
        "}");
  }

  public void test_RowLayoutData_heightProperties() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    RowLayoutDataInfo rowData = RowLayoutInfo.getRowData(label);
    // set "int"
    rowData.getPropertyByTitle("height(int)").setValue(50);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new RowLayoutData(50));",
        "    }",
        "  }",
        "}");
    // set "string"
    rowData.getPropertyByTitle("height(java.lang.String)").setValue("20%");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new RowLayoutData('20%'));",
        "    }",
        "  }",
        "}");
    // set "int"
    rowData.getPropertyByTitle("height(int)").setValue(100);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new RowLayoutData(100));",
        "    }",
        "  }",
        "}");
  }

  public void test_RowLayoutData_materializeHeightAsString() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    RowLayoutDataInfo rowData = RowLayoutInfo.getRowData(label);
    // set "string"
    rowData.getPropertyByTitle("height(java.lang.String)").setValue("20%");
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Label label = new Label();",
        "      add(label, new RowLayoutData('20%'));",
        "    }",
        "  }",
        "}");
  }
}