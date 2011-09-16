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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.layout.table.TableLayoutInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

/**
 * Test for {@link LayoutDataInfo}.
 * 
 * @author scheglov_ke
 */
public class LayoutDataTest extends GxtModelTest {
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
  public void test_LayoutData_property() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new RowData(200.0, 100.0));",
            "    }",
            "  }",
            "}");
    WidgetInfo button = container.getWidgets().get(0);
    // prepare "LayoutData" property
    Property property = button.getPropertyByTitle("LayoutData");
    assertNotNull(property);
    assertTrue(property.isModified());
    // property "LayoutData" is cached
    assertSame(property, button.getPropertyByTitle("LayoutData"));
    // check sub-properties
    assertEquals(200.0, PropertyUtils.getByPath(button, "LayoutData/width").getValue());
    assertEquals(100.0, PropertyUtils.getByPath(button, "LayoutData/height").getValue());
    // property "Class" is filterer
    assertNull(PropertyUtils.getByPath(button, "LayoutData/Class"));
    // remove LayoutData using property
    property.setValue(Property.UNKNOWN_VALUE);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new RowLayout())/ /add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.RowData} {virtual-layout-data} {}");
  }

  public void test_inlineIfPossible() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button();",
            "      RowData rowData = new RowData(200.0, 100.0);",
            "      add(button, rowData);",
            "    }",
            "  }",
            "}");
    ExecutionUtils.refresh(container);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new RowData(200.0, 100.0));",
        "    }",
        "  }",
        "}");
  }

  public void test_removeIfDefault() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, new RowData(200.0, Style.DEFAULT));",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new RowLayout())/ /add(button, new RowData(200.0, Style.DEFAULT))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button, new RowData(200.0, Style.DEFAULT))/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.RowData} {empty} {/add(button, new RowData(200.0, Style.DEFAULT))/}");
    WidgetInfo button = container.getWidgets().get(0);
    LayoutDataInfo layoutData = LayoutInfo.getLayoutData(button);
    // set Style.DEFAULT, so all properties are default and LayoutData can be removed
    layoutData.getPropertyByTitle("width").setValue(-1);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new RowLayout())/ /add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.RowData} {virtual-layout-data} {}");
  }

  /**
   * Dangling {@link LayoutDataInfo} without parent {@link WidgetInfo} should be ignored.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43534
   */
  public void test_ignoreDangling() throws Exception {
    parseJavaInfo(
        "public class Test extends LayoutContainer {",
        "  private RowData rowData = new RowData();",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {}",
        "  {implicit-layout: default} {implicit-layout} {}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Virtual
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_virtual() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new RowLayout())/ /add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.RowData} {virtual-layout-data} {}");
    WidgetInfo button = container.getWidgets().get(0);
    LayoutDataInfo layoutData = LayoutInfo.getLayoutData(button);
    // CreationSupport
    {
      CreationSupport creationSupport = layoutData.getCreationSupport();
      assertInstanceOf(VirtualLayoutDataCreationSupport.class, creationSupport);
      assertEquals(
          "virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.RowData",
          creationSupport.toString());
      assertFalse(creationSupport.isJavaInfo(null));
      assertEquals(null, creationSupport.getNode());
      assertEquals(null, ((IImplicitCreationSupport) creationSupport).getImplicitClipboard());
    }
    // VariableSupport
    {
      VariableSupport variableSupport = layoutData.getVariableSupport();
      assertInstanceOf(VirtualLayoutDataVariableSupport.class, variableSupport);
      assertTrue(variableSupport.isDefault());
      assertEquals("virtual-layout-data", variableSupport.toString());
      assertEquals("(virtual layout data)", variableSupport.getTitle());
      try {
        variableSupport.getStatementTarget();
        fail();
      } catch (IllegalStateException e) {
      }
    }
    // delete, do nothing
    assertTrue(layoutData.canDelete());
    layoutData.delete();
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new RowLayout())/ /add(button)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /add(button)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.RowData} {virtual-layout-data} {}");
    assertSame(layoutData, LayoutInfo.getLayoutData(button));
  }

  public void test_materializeVirtual_1() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    WidgetInfo button = container.getWidgets().get(0);
    LayoutDataInfo layoutData = LayoutInfo.getLayoutData(button);
    // virtual initially
    {
      assertInstanceOf(VirtualLayoutDataCreationSupport.class, layoutData.getCreationSupport());
      assertInstanceOf(VirtualLayoutDataVariableSupport.class, layoutData.getVariableSupport());
    }
    // set property, so materialize
    layoutData.getPropertyByTitle("width").setValue(100.0);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Button button = new Button();",
        "      add(button, new RowData(100.0, Style.DEFAULT, new Margins()));",
        "    }",
        "  }",
        "}");
    // now materialized
    {
      assertInstanceOf(ConstructorCreationSupport.class, layoutData.getCreationSupport());
      assertInstanceOf(EmptyVariableSupport.class, layoutData.getVariableSupport());
    }
  }

  public void test_materializeVirtual_2() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button = new Button();",
            "      add(button, null);",
            "    }",
            "  }",
            "}");
    WidgetInfo button = container.getWidgets().get(0);
    LayoutDataInfo layoutData = LayoutInfo.getLayoutData(button);
    // do materialize
    {
      NodeTarget target = getNodeStatementTarget(container, false, 1);
      assertEquals("rowData.", layoutData.getVariableSupport().getAccessExpression(target));
    }
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  private RowData rowData;",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Button button = new Button();",
        "      rowData = new RowData(Style.DEFAULT, Style.DEFAULT, new Margins());",
        "      add(button, rowData);",
        "    }",
        "  }",
        "}");
    // now materialized
    {
      assertInstanceOf(ConstructorCreationSupport.class, layoutData.getCreationSupport());
      assertInstanceOf(FieldUniqueVariableSupport.class, layoutData.getVariableSupport());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation: name, based on template
  //
  ////////////////////////////////////////////////////////////////////////////
  private void check_nameTemplate(String template, String... lines) throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new TableLayout(1));",
            "    {",
            "      Button button = new Button();",
            "      add(button, new TableData());",
            "    }",
            "  }",
            "}");
    container.refresh();
    WidgetInfo button = getJavaInfoByName("button");
    Activator.getDefault().getPreferenceStore().setValue(
        com.google.gdt.eclipse.designer.preferences.IPreferenceConstants.P_LAYOUT_DATA_NAME_TEMPLATE,
        template);
    TableLayoutInfo.getTableData(button).getPropertyByTitle("margin").setValue(5);
    assertEditor(lines);
  }

  /**
   * Template "${defaultName}" means that name should be based on name of type.
   */
  public void test_nameTemplate_useDefaultName() throws Exception {
    check_nameTemplate(
        org.eclipse.wb.internal.core.model.variable.SyncParentChildVariableNameSupport.TEMPLATE_FOR_DEFAULT,
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button();",
        "      TableData tableData = new TableData();",
        "      tableData.setMargin(5);",
        "      add(button, tableData);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Generate name using "${dataAcronym}${controlName-cap}" template.
   */
  public void test_nameTemplate_alternativeTemplate_1() throws Exception {
    check_nameTemplate(
        "${dataAcronym}${widgetName-cap}",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button();",
        "      TableData tdButton = new TableData();",
        "      tdButton.setMargin(5);",
        "      add(button, tdButton);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Generate name using "${controlName}${dataClassName}" template.
   */
  public void test_nameTemplate_alternativeTemplate_2() throws Exception {
    check_nameTemplate(
        "${widgetName}${dataClassName}",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new TableLayout(1));",
        "    {",
        "      Button button = new Button();",
        "      TableData buttonTableData = new TableData();",
        "      buttonTableData.setMargin(5);",
        "      add(button, buttonTableData);",
        "    }",
        "  }",
        "}");
  }
}