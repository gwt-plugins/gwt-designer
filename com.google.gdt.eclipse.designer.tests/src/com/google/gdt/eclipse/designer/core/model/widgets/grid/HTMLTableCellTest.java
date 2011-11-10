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
package com.google.gdt.eclipse.designer.core.model.widgets.grid;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.ColumnInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.RowInfo;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for "Cell" property for {@link WidgetInfo}-s on {@link HTMLTableInfo}.
 * 
 * @author scheglov_ke
 */
public class HTMLTableCellTest extends GwtModelTest {
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
  // FlexTableHelper
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_defaultValues() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button());",
            "  }",
            "}");
    frame.refresh();
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    //
    Property cellProperty = button.getPropertyByTitle("Cell");
    assertNotNull(cellProperty);
    Property[] subProperties = getSubProperties(cellProperty);
    assertThat(subProperties).hasSize(10);
    // horizontalAlignment
    {
      Property property = subProperties[0];
      assertEquals("horizontalAlignment", property.getTitle());
      assertFalse(property.isModified());
      assertEquals(ColumnInfo.Alignment.UNKNOWN, property.getValue());
    }
    // verticalAlignment
    {
      Property property = subProperties[1];
      assertEquals("verticalAlignment", property.getTitle());
      assertFalse(property.isModified());
      assertEquals(RowInfo.Alignment.UNKNOWN, property.getValue());
    }
    // width
    {
      Property property = subProperties[2];
      assertEquals("width", property.getTitle());
      assertFalse(property.isModified());
      assertEquals(Property.UNKNOWN_VALUE, property.getValue());
    }
    // height
    {
      Property property = subProperties[3];
      assertEquals("height", property.getTitle());
      assertFalse(property.isModified());
      assertEquals(Property.UNKNOWN_VALUE, property.getValue());
    }
    // styleName
    {
      Property property = subProperties[4];
      assertEquals("styleName", property.getTitle());
      assertFalse(property.isModified());
      assertEquals(Property.UNKNOWN_VALUE, property.getValue());
    }
    // stylePrimaryName
    {
      Property property = subProperties[5];
      assertEquals("stylePrimaryName", property.getTitle());
      assertFalse(property.isModified());
      assertEquals(Property.UNKNOWN_VALUE, property.getValue());
    }
    // visible
    {
      Property property = subProperties[6];
      assertEquals("visible", property.getTitle());
      assertFalse(property.isModified());
      assertEquals(Boolean.TRUE, property.getValue());
    }
    // wordWrap
    {
      Property property = subProperties[7];
      assertEquals("wordWrap", property.getTitle());
      assertFalse(property.isModified());
      assertEquals(Boolean.FALSE, property.getValue());
    }
    // optional "colSpan"
    {
      Property property = subProperties[8];
      assertEquals("colSpan", property.getTitle());
      assertFalse(property.isModified());
      assertEquals(1, property.getValue());
    }
    // optional "rowSpan"
    {
      Property property = subProperties[9];
      assertEquals("rowSpan", property.getTitle());
      assertFalse(property.isModified());
      assertEquals(1, property.getValue());
    }
  }

  public void test_setVisible() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button());",
            "    panel.getCellFormatter().setVisible(0, 0, false);",
            "  }",
            "}");
    frame.refresh();
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    //
    Property property = PropertyUtils.getByPath(button, "Cell/visible");
    assertTrue(property.isModified());
    assertEquals(Boolean.FALSE, property.getValue());
  }

  public void test_setColSpan() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button());",
            "    panel.getFlexCellFormatter().setColSpan(0, 0, 2);",
            "  }",
            "}");
    frame.refresh();
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    Property property = PropertyUtils.getByPath(button, "Cell/colSpan");
    // initial value
    assertTrue(property.isModified());
    assertEquals(2, property.getValue());
    // set "3"
    property.setValue(3);
    assertEquals(3, property.getValue());
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button());",
        "    panel.getFlexCellFormatter().setColSpan(0, 0, 3);",
        "  }",
        "}");
    // set Property.UNKNOWN_VALUE, so delete
    property.setValue(Property.UNKNOWN_VALUE);
    assertEquals(1, property.getValue());
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button());",
        "  }",
        "}");
  }

  public void test_setRowSpan() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button());",
            "    panel.setWidget(1, 0, new Button());",
            "    panel.setWidget(2, 0, new Button());",
            "    panel.setWidget(0, 1, new Button());",
            "    panel.getFlexCellFormatter().setRowSpan(0, 1, 2);",
            "  }",
            "}");
    frame.refresh();
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(3);
    Property property = PropertyUtils.getByPath(button, "Cell/rowSpan");
    // initial value
    assertTrue(property.isModified());
    assertEquals(2, property.getValue());
    // set "3"
    property.setValue(3);
    assertEquals(3, property.getValue());
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button());",
        "    panel.setWidget(1, 0, new Button());",
        "    panel.setWidget(2, 0, new Button());",
        "    panel.setWidget(0, 1, new Button());",
        "    panel.getFlexCellFormatter().setRowSpan(0, 1, 3);",
        "    FlexTableHelper.fixRowSpan(panel);",
        "  }",
        "}");
    // set Property.UNKNOWN_VALUE, so delete
    property.setValue(Property.UNKNOWN_VALUE);
    assertEquals(1, property.getValue());
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button());",
        "    panel.setWidget(1, 0, new Button());",
        "    panel.setWidget(2, 0, new Button());",
        "    panel.setWidget(0, 1, new Button());",
        "    FlexTableHelper.fixRowSpan(panel);",
        "  }",
        "}");
  }

  public void test_setHorizontalAlignment() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button());",
            "    panel.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);",
            "  }",
            "}");
    frame.refresh();
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    Property property = PropertyUtils.getByPath(button, "Cell/horizontalAlignment");
    // initial value
    assertTrue(property.isModified());
    assertEquals(ColumnInfo.Alignment.RIGHT, property.getValue());
    // set "center"
    property.setValue(ColumnInfo.Alignment.CENTER);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button());",
        "    panel.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);",
        "  }",
        "}");
    // set "unknown", so remove
    property.setValue(ColumnInfo.Alignment.UNKNOWN);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button());",
        "  }",
        "}");
    // set "center", so add
    property.setValue(ColumnInfo.Alignment.CENTER);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button());",
        "    panel.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);",
        "  }",
        "}");
    // set Property.UNKNOWN_VALUE, so remove
    property.setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button());",
        "  }",
        "}");
  }

  public void test_setVerticalAlignment() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button());",
            "    panel.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_BOTTOM);",
            "  }",
            "}");
    frame.refresh();
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    //
    Property property = PropertyUtils.getByPath(button, "Cell/verticalAlignment");
    // current value
    assertTrue(property.isModified());
    assertEquals(RowInfo.Alignment.BOTTOM, property.getValue());
    // set "center"
    property.setValue(RowInfo.Alignment.MIDDLE);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button());",
        "    panel.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);",
        "  }",
        "}");
    // set "unknown", so remove
    property.setValue(RowInfo.Alignment.UNKNOWN);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button());",
        "  }",
        "}");
    // set "middle", so add
    property.setValue(RowInfo.Alignment.MIDDLE);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button());",
        "    panel.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);",
        "  }",
        "}");
    // set Property.UNKNOWN_VALUE, so remove
    property.setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    FlexTable panel = new FlexTable();",
        "    rootPanel.add(panel);",
        "    panel.setWidget(0, 0, new Button());",
        "  }",
        "}");
  }
}