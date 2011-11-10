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
 * Test for {@link VBoxLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class VBoxLayoutTest extends GxtModelTest {
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
  public void test_setLayout() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    // set VBoxLayout
    VBoxLayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.VBoxLayout");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new VBoxLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new VBoxLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.VBoxLayout} {empty} {/setLayout(new VBoxLayout())/}");
    assertSame(layout, container.getLayout());
  }

  public void test_flex() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new VBoxLayout());",
            "    {",
            "      Button button = new Button();",
            "      VBoxLayoutData data = new VBoxLayoutData();",
            "      data.setFlex(2.3);",
            "      add(button, data);",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    VBoxLayoutDataInfo boxData = VBoxLayoutInfo.getVBoxData(button);
    // current value
    assertEquals(2, boxData.getFlex());
    // set new value
    boxData.setFlex(3);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new VBoxLayout());",
        "    {",
        "      Button button = new Button();",
        "      VBoxLayoutData data = new VBoxLayoutData();",
        "      data.setFlex(3.0);",
        "      add(button, data);",
        "    }",
        "  }",
        "}");
    // set default value
    boxData.setFlex(0);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new VBoxLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Margins
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMargin() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new VBoxLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new VBoxLayoutData(1, 2, 3, 4));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    VBoxLayoutDataInfo boxData = VBoxLayoutInfo.getVBoxData(button);
    // check margins
    assertEquals(1, boxData.getMarginTop());
    assertEquals(2, boxData.getMarginRight());
    assertEquals(3, boxData.getMarginBottom());
    assertEquals(4, boxData.getMarginLeft());
  }

  public void test_getMargin_virtual() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new VBoxLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    VBoxLayoutDataInfo boxData = VBoxLayoutInfo.getVBoxData(button);
    // check margins
    assertEquals(0, boxData.getMarginTop());
    assertEquals(0, boxData.getMarginRight());
    assertEquals(0, boxData.getMarginBottom());
    assertEquals(0, boxData.getMarginLeft());
  }

  public void test_marginProperties_get() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new VBoxLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new VBoxLayoutData(1, 2, 3, 4));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    VBoxLayoutDataInfo boxData = VBoxLayoutInfo.getVBoxData(button);
    // check margins
    assertEquals(1, boxData.getPropertyByTitle("margin-top").getValue());
    assertEquals(2, boxData.getPropertyByTitle("margin-right").getValue());
    assertEquals(3, boxData.getPropertyByTitle("margin-bottom").getValue());
    assertEquals(4, boxData.getPropertyByTitle("margin-left").getValue());
  }

  public void test_setMargin() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new VBoxLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new VBoxLayoutData());",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    VBoxLayoutDataInfo boxData = VBoxLayoutInfo.getVBoxData(button);
    // set margin
    boxData.setMarginTop(10);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new VBoxLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new VBoxLayoutData(10, 0, 0, 0));",
        "    }",
        "  }",
        "}");
  }

  public void test_setMarginAll() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new VBoxLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new VBoxLayoutData());",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    VBoxLayoutDataInfo boxData = VBoxLayoutInfo.getVBoxData(button);
    Property marginAllProperty = boxData.getPropertyByTitle("margin-all");
    // set margin
    marginAllProperty.setValue(10);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new VBoxLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new VBoxLayoutData(10, 10, 10, 10));",
        "    }",
        "  }",
        "}");
    // set margin
    marginAllProperty.setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new VBoxLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_setMargin_deleteWhenZero() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new VBoxLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new VBoxLayoutData(10, 0, 0, 0));",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = container.getWidgets().get(0);
    VBoxLayoutDataInfo boxData = VBoxLayoutInfo.getVBoxData(button);
    Property marginTopProperty = boxData.getPropertyByTitle("margin-top");
    // set margin
    marginTopProperty.setValue(0);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new VBoxLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }
}