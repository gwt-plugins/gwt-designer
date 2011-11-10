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

import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Test for {@link FillLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class FillLayoutTest extends GxtModelTest {
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
  public void test_setLayout() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    // set FillLayout
    FillLayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.FillLayout");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FillLayout(Orientation.HORIZONTAL));",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FillLayout(Orientation.HORIZONTAL))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FillLayout} {empty} {/setLayout(new FillLayout(Orientation.HORIZONTAL))/}");
    assertSame(layout, container.getLayout());
  }

  public void test_setLayout_HORIZONTAL() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    // set FillLayout
    FillLayoutInfo layout =
        createJavaInfo("com.extjs.gxt.ui.client.widget.layout.FillLayout", "horizontal");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FillLayout(Orientation.HORIZONTAL));",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FillLayout(Orientation.HORIZONTAL))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FillLayout horizontal} {empty} {/setLayout(new FillLayout(Orientation.HORIZONTAL))/}");
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
    // set FillLayout
    FillLayoutInfo layout =
        createJavaInfo("com.extjs.gxt.ui.client.widget.layout.FillLayout", "vertical");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FillLayout(Orientation.VERTICAL));",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FillLayout(Orientation.VERTICAL))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FillLayout vertical} {empty} {/setLayout(new FillLayout(Orientation.VERTICAL))/}");
    assertSame(layout, container.getLayout());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isHorizontal()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FillLayoutInfo#isHorizontal()}.
   */
  public void test_isHorizontal_true() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FillLayout(Orientation.HORIZONTAL));",
            "  }",
            "}");
    container.refresh();
    FillLayoutInfo layout = (FillLayoutInfo) container.getLayout();
    assertTrue(layout.isHorizontal());
  }

  /**
   * Test for {@link FillLayoutInfo#isHorizontal()}.
   */
  public void test_isHorizontal_false() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FillLayout(Orientation.VERTICAL));",
            "  }",
            "}");
    container.refresh();
    FillLayoutInfo layout = (FillLayoutInfo) container.getLayout();
    assertFalse(layout.isHorizontal());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Margins
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMargin_get() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FillData(1, 2, 3, 4));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FillDataInfo fillData = FillLayoutInfo.getFillData(button);
    // check margins
    assertSame(Property.UNKNOWN_VALUE, fillData.getMarginAll());
    assertEquals(1, fillData.getMarginTop());
    assertEquals(2, fillData.getMarginRight());
    assertEquals(3, fillData.getMarginBottom());
    assertEquals(4, fillData.getMarginLeft());
  }

  public void test_getMargin_virtual() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FillDataInfo fillData = FillLayoutInfo.getFillData(button);
    // check margins
    assertEquals(0, fillData.getMarginAll());
    assertEquals(0, fillData.getMarginTop());
    assertEquals(0, fillData.getMarginRight());
    assertEquals(0, fillData.getMarginBottom());
    assertEquals(0, fillData.getMarginLeft());
  }

  public void test_marginProperties_get() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new FillData(1, 2, 3, 4));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    FillDataInfo fillData = FillLayoutInfo.getFillData(button);
    // check margins
    assertSame(Property.UNKNOWN_VALUE, fillData.getPropertyByTitle("margin-all").getValue());
    assertEquals(1, fillData.getPropertyByTitle("margin-top").getValue());
    assertEquals(2, fillData.getPropertyByTitle("margin-right").getValue());
    assertEquals(3, fillData.getPropertyByTitle("margin-bottom").getValue());
    assertEquals(4, fillData.getPropertyByTitle("margin-left").getValue());
  }
}