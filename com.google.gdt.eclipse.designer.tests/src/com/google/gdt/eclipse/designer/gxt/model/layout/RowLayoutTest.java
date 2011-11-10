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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

/**
 * Test for {@link RowLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class RowLayoutTest extends GxtModelTest {
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
  // setLayout()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setLayout_HORIZONTAL() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    // set RowLayout
    LayoutInfo layout =
        createJavaInfo("com.extjs.gxt.ui.client.widget.layout.RowLayout", "horizontal");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new RowLayout(Orientation.HORIZONTAL));",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new RowLayout(Orientation.HORIZONTAL))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.RowLayout horizontal} {empty} {/setLayout(new RowLayout(Orientation.HORIZONTAL))/}");
    assertSame(layout, container.getLayout());
  }

  public void test_setLayout_VERTICAL() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    // set RowLayout
    LayoutInfo layout =
        createJavaInfo("com.extjs.gxt.ui.client.widget.layout.RowLayout", "vertical");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new RowLayout(Orientation.VERTICAL));",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new RowLayout(Orientation.VERTICAL))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.RowLayout vertical} {empty} {/setLayout(new RowLayout(Orientation.VERTICAL))/}");
    assertSame(layout, container.getLayout());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isHorizontal()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link RowLayoutInfo#isHorizontal()}.
   */
  public void test_isHorizontal_true() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new RowLayout(Orientation.HORIZONTAL));",
            "  }",
            "}");
    container.refresh();
    RowLayoutInfo layout = (RowLayoutInfo) container.getLayout();
    assertTrue(layout.isHorizontal());
  }

  /**
   * Test for {@link RowLayoutInfo#isHorizontal()}.
   */
  public void test_isHorizontal_false() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new RowLayout(Orientation.VERTICAL));",
            "  }",
            "}");
    container.refresh();
    RowLayoutInfo layout = (RowLayoutInfo) container.getLayout();
    assertFalse(layout.isHorizontal());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // RowData
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link RowDataInfo#getWidth()} and {@link RowDataInfo#getHeight()}.
   */
  public void test_RowData_getWidth_getHeight() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new RowData(200, 0.5));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    RowDataInfo rowData = RowLayoutInfo.getRowData(button);
    assertEquals(200.0, rowData.getWidth(), 0.001);
    assertEquals(0.5, rowData.getHeight(), 0.001);
  }

  /**
   * Test for {@link RowDataInfo#setWidth(double)} and {@link RowDataInfo#setHeight(double)}.
   */
  public void test_RowData_setWidth_setHeight() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new RowData(200, 0.5));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    RowDataInfo rowData = RowLayoutInfo.getRowData(button);
    // set new values
    rowData.setWidth(0.8);
    rowData.setHeight(150);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new RowData(0.8, 150.0));",
        "    }",
        "  }",
        "}");
    assertEquals(0.8, rowData.getWidth(), 0.001);
    assertEquals(150, rowData.getHeight(), 0.001);
  }

  public void test_RowData_setWidth_DEFAULT() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new RowData(200, 100));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    RowDataInfo rowData = RowLayoutInfo.getRowData(button);
    assertEquals(200, rowData.getWidth(), 0.001);
    assertEquals(100, rowData.getHeight(), 0.001);
    // set default
    rowData.setWidth(-1);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new RowData(Style.DEFAULT, 100));",
        "    }",
        "  }",
        "}");
    assertEquals(-1, rowData.getWidth(), 0.001);
    assertEquals(100, rowData.getHeight(), 0.001);
  }

  public void test_RowData_setHeight_DEFAULT() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new RowData(200, 100));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    RowDataInfo rowData = RowLayoutInfo.getRowData(button);
    assertEquals(200, rowData.getWidth(), 0.001);
    assertEquals(100, rowData.getHeight(), 0.001);
    // set default
    rowData.setHeight(-1);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new RowData(200, Style.DEFAULT));",
        "    }",
        "  }",
        "}");
    assertEquals(200, rowData.getWidth(), 0.001);
    assertEquals(-1, rowData.getHeight(), 0.001);
  }
}