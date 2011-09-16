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
package com.google.gdt.eclipse.designer.smartgwt.model.widgets;

import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.FilterBuilderInfo;
import com.google.gdt.eclipse.designer.smart.model.ListGridInfo;
import com.google.gdt.eclipse.designer.smart.model.data.DataSourceInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.ObjectPropertyEditor;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link FilterBuilderInfo}.
 * 
 * @author sablin_aa
 */
public class FilterBuilderTest extends SmartGwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parsing empty <code>com.smartgwt.client.widgets.form.FilterBuilder</code>.
   */
  public void test_parse_empty() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    final Canvas canvas = new Canvas();",
            "    FilterBuilder fb = new FilterBuilder();",
            "    canvas.addChild(fb);",
            "    canvas.draw();",
            "  }",
            "}");
    canvas.refresh();
    FilterBuilderInfo filterBuilder = canvas.getChildren(FilterBuilderInfo.class).get(0);
    //
    assertThat(filterBuilder.getChildren()).isEmpty();
    assertThat(filterBuilder.getDataSource()).isNull();
    Property property = filterBuilder.getPropertyByTitle("dataSource");
    assertThat(property).isNotNull();
    assertThat(property.getValue()).isNotNull(); // fake data source used
  }

  /**
   * Parsing <code>com.smartgwt.client.widgets.form.FilterBuilder</code> with assigned DataSource.
   */
  public void test_parse_dataSource() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    final Canvas canvas = new Canvas();",
            "    DataSource dataSource = new DataSource();",
            "	 dataSource.addField(new DataSourceTextField('newDSTextField_1', 'New TextField'));",
            "    FilterBuilder fb = new FilterBuilder();",
            "    fb.setDataSource(dataSource);",
            "    canvas.addChild(fb);",
            "    ListGrid listGrid = new ListGrid();",
            "    listGrid.setDataSource(dataSource);",
            "    canvas.addChild(listGrid);",
            "    listGrid.moveTo(44, 90);",
            "    canvas.draw();",
            "  }",
            "}");
    canvas.refresh();
    FilterBuilderInfo filterBuilder = canvas.getChildren(FilterBuilderInfo.class).get(0);
    DataSourceInfo dataSource = filterBuilder.getDataSource();
    //
    assertThat(filterBuilder.getChildren()).isEmpty();
    Property property = filterBuilder.getPropertyByTitle("dataSource");
    assertThat(property).isNotNull();
    assertThat(property.getValue()).isSameAs(dataSource.getObject());
  }

  public void test_set_dataSource() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    final Canvas canvas = new Canvas();",
            "    FilterBuilder fb = new FilterBuilder();",
            "    canvas.addChild(fb);",
            "    ListGrid listGrid = new ListGrid();",
            "    DataSource dataSource = new DataSource();",
            "	 dataSource.addField(new DataSourceTextField('newDSTextField_1', 'New TextField'));",
            "    listGrid.setDataSource(dataSource);",
            "    canvas.addChild(listGrid);",
            "    listGrid.moveTo(44, 90);",
            "    canvas.draw();",
            "  }",
            "}");
    canvas.refresh();
    FilterBuilderInfo filterBuilder = canvas.getChildren(FilterBuilderInfo.class).get(0);
    ListGridInfo listGrid = canvas.getChildren(ListGridInfo.class).get(0);
    DataSourceInfo dataSource;
    {
      Property property = listGrid.getPropertyByTitle("dataSource");
      ObjectPropertyEditor editor = (ObjectPropertyEditor) property.getEditor();
      dataSource = (DataSourceInfo) editor.getValueComponent(property);
    }
    //
    assertThat(filterBuilder.getChildren()).isEmpty();
    assertThat(filterBuilder.getDataSource()).isNull();
    Property property = filterBuilder.getPropertyByTitle("dataSource");
    assertThat(property).isNotNull();
    assertThat(property.getValue()).isNotNull(); // fake data source used
    // assign DataSiurce
    ObjectPropertyEditor editor = (ObjectPropertyEditor) property.getEditor();
    editor.setComponent((GenericProperty) property, dataSource);
    canvas.refresh();
    // check
    assertThat(filterBuilder.getChildren()).isEmpty();
    assertThat(filterBuilder.getDataSource()).isSameAs(dataSource);
    assertThat(property.getValue()).isSameAs(dataSource.getObject());
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private DataSource dataSource;",
        "  public void onModuleLoad() {",
        "    final Canvas canvas = new Canvas();",
        "    FilterBuilder fb = new FilterBuilder();",
        "    fb.setDataSource(getDataSource());",
        "    canvas.addChild(fb);",
        "    ListGrid listGrid = new ListGrid();",
        "    listGrid.setDataSource(getDataSource());",
        "    canvas.addChild(listGrid);",
        "    listGrid.moveTo(44, 90);",
        "    canvas.draw();",
        "  }",
        "  private DataSource getDataSource() {",
        "    if (dataSource == null) {",
        "      dataSource = new DataSource();",
        "      dataSource.addField(new DataSourceTextField('newDSTextField_1', 'New TextField'));",
        "    }",
        "    return dataSource;",
        "  }",
        "}");
  }
}