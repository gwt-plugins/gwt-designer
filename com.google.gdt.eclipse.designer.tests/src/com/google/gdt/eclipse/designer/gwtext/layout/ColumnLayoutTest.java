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
import com.google.gdt.eclipse.designer.gwtext.model.layout.ColumnLayoutDataInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.ColumnLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.property.Property;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests for {@link ColumnLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class ColumnLayoutTest extends GwtExtModelTest {
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
            "    setLayout(new ColumnLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    assertInstanceOf(ColumnLayoutInfo.class, panel.getLayout());
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new ColumnLayout())/ /add(label)/}",
        "  {new: com.gwtext.client.widgets.layout.ColumnLayout} {empty} {/setLayout(new ColumnLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.ColumnLayoutData} {virtual-layout-data} {}");
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    ColumnLayoutDataInfo layoutData = ColumnLayoutInfo.getColumnData(label);
    assertNotNull(layoutData);
    // set width
    {
      layoutData.setWidth(0.2);
      assertEditor(
          "public class Test extends Panel {",
          "  public Test() {",
          "    setLayout(new ColumnLayout());",
          "    {",
          "      Label label = new Label();",
          "      add(label, new ColumnLayoutData(0.2));",
          "    }",
          "  }",
          "}");
    }
    // ColumnLayoutData should have top level "width" property
    {
      Property widthProperty = layoutData.getPropertyByTitle("width");
      assertNotNull(widthProperty);
      assertEquals(0.2d, widthProperty.getValue());
    }
  }

  /**
   * Test for complex "LayoutData" property.
   */
  public void test_LayoutData_property_whenWasVirtual() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new ColumnLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    // initially it is virtual, so no "LayoutData" property
    assertNull(label.getPropertyByTitle("LayoutData"));
    // set "width", so materialize
    ColumnLayoutInfo.getColumnData(label).setWidth(0.2);
    // now we have "LayoutData" property
    Property layoutDataProperty = label.getPropertyByTitle("LayoutData");
    assertNotNull(layoutDataProperty);
    // sub properties
    {
      Property[] subProperties = getSubProperties(layoutDataProperty);
      assertThat(subProperties).hasSize(1);
      assertEquals("width", subProperties[0].getTitle());
    }
    // delete RowLayoutData
    layoutDataProperty.setValue(Property.UNKNOWN_VALUE);
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new ColumnLayout())/ /add(label)/}",
        "  {new: com.gwtext.client.widgets.layout.ColumnLayout} {empty} {/setLayout(new ColumnLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.ColumnLayoutData} {virtual-layout-data} {}");
    // again virtual, so no "LayoutData" property
    assertNull(label.getPropertyByTitle("LayoutData"));
  }
}