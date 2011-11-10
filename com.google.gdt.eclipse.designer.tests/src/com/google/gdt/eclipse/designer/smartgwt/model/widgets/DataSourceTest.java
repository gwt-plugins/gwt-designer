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
package com.google.gdt.eclipse.designer.smartgwt.model.widgets;

import com.google.gdt.eclipse.designer.smart.model.CalendarInfo;
import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.FilterBuilderInfo;
import com.google.gdt.eclipse.designer.smart.model.data.DataSourceFieldInfo;
import com.google.gdt.eclipse.designer.smart.model.data.DataSourceInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.editor.ObjectPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link DataSourceInfo}.
 * 
 * @author sablin_aa
 */
public class DataSourceTest extends SmartGwtModelTest {
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
    CanvasInfo calendar =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Calendar calendar = new Calendar();",
            "    DataSource dataSource = new DataSource();",
            "    dataSource.setID('DS');",
            "    DataSourceBooleanField dataSourceBooleanField = new DataSourceBooleanField('dsBooleanField');",
            "    dataSource.addField(dataSourceBooleanField);",
            "    DataSourceDateField dataSourceDateField = new DataSourceDateField('dsDateField');",
            "    dataSource.setFields(dataSourceDateField);",
            "    calendar.setDataSource(dataSource);",
            "    calendar.draw();",
            "  }",
            "}"});
    calendar.refresh();
    //
    DataSourceInfo dataSource =
        NonVisualBeanContainerInfo.get(calendar).getChildren(DataSourceInfo.class).get(0);
    assertThat(NonVisualBeanInfo.getNonVisualInfo(dataSource).getLocation()).isNull();
    // check fields
    List<DataSourceFieldInfo> dsDirectFields = dataSource.getChildren(DataSourceFieldInfo.class);
    assertThat(dsDirectFields.size()).isEqualTo(2);
    assertThat(
        ReflectionUtils.isSuccessorOf(
            dsDirectFields.get(0).getObject(),
            "com.smartgwt.client.data.fields.DataSourceBooleanField")).isTrue();
    assertThat(
        ReflectionUtils.isSuccessorOf(
            dsDirectFields.get(1).getObject(),
            "com.smartgwt.client.data.fields.DataSourceDateField")).isTrue();
    //
    AbstractArrayObjectInfo objectFieldsInfo =
        dataSource.getChildren(AbstractArrayObjectInfo.class).get(0);
    assertThat(objectFieldsInfo.getItems().size()).isEqualTo(1);
    assertThat(objectFieldsInfo.getChildren(DataSourceFieldInfo.class).size()).isEqualTo(0);
  }

  /**
   * http://forums.instantiations.com/viewtopic.php?f=11&t=5401
   */
  public void test_parse_anonymous() throws Exception {
    CanvasInfo calendar =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Calendar calendar = new Calendar();",
            "    DataSource dataSource = new RestDataSource() {};",
            "    calendar.setDataSource(dataSource);",
            "    calendar.draw();",
            "  }",
            "}"});
    calendar.refresh();
    assertNoErrors(calendar);
    //
    List<DataSourceInfo> dataSources =
        NonVisualBeanContainerInfo.get(calendar).getChildren(DataSourceInfo.class);
    assertThat(dataSources).isNotEmpty();
    assertThat(calendar.getPropertyByTitle("dataSource").getValue()).isSameAs(
        dataSources.get(0).getObject());
  }

  /**
   * Test for no "destroy()" on dispose DataSource if it is not rendered.
   * <p>
   * Note: no error messages windows must be popped up due this test.
   */
  public void test_noDestroy() throws Exception {
    CanvasInfo calendar =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  /**",
            "  *@wbp.nonvisual location=0,0",
            "  */",
            "  private DataSource dataSource = new DataSource();",
            "  public void onModuleLoad() {",
            "    dataSource.setID('DS');",
            "    Calendar calendar = new Calendar();",
            "    calendar.draw();",
            "  }",
            "}"});
    calendar.refresh();
    assertNoErrors(calendar);
    JavaInfo dataSource = getJavaInfoByName("dataSource");
    assertNoErrors(dataSource);
    // no error messages must be popped
  }

  /**
   * {@link DataSourceFieldInfo} added before {@link DataSourceInfo} association.
   */
  public void test_addField() throws Exception {
    CanvasInfo calendar =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Calendar calendar = new Calendar();",
            "    DataSource dataSource = new DataSource();",
            "    calendar.setDataSource(dataSource);",
            "    calendar.draw();",
            "  }",
            "}"});
    calendar.refresh();
    //
    DataSourceInfo dataSource =
        NonVisualBeanContainerInfo.get(calendar).getChildren(DataSourceInfo.class).get(0);
    // create new DataSourceField
    DataSourceFieldInfo dataSourceField =
        createJavaInfo("com.smartgwt.client.data.fields.DataSourceBooleanField");
    {
      FlowContainer flowContainer = new FlowContainerFactory(dataSource, false).get().get(0);
      assertTrue(flowContainer.validateComponent(dataSourceField));
      flowContainer.command_CREATE(dataSourceField, null);
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Calendar calendar = new Calendar();",
        "    DataSource dataSource = new DataSource();",
        "    {",
        "      DataSourceBooleanField dataSourceBooleanField = new DataSourceBooleanField('newDSBooleanField_1', 'New BooleanField');",
        "      dataSource.addField(dataSourceBooleanField);",
        "    }",
        "    calendar.setDataSource(dataSource);",
        "    calendar.draw();",
        "  }",
        "}");
  }

  public void test_addField_pseudo() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    DataSource dataSource = new DataSource();",
            "    FilterBuilder filterBuilder = new FilterBuilder();",
            "    filterBuilder.setDataSource(dataSource);",
            "    canvas.addChild(filterBuilder);",
            "    Calendar calendar = new Calendar();",
            "    calendar.setDataSource(dataSource);",
            "    canvas.addChild(calendar);",
            "    canvas.draw();",
            "  }",
            "}"});
    canvas.refresh();
    //
    DataSourceInfo dataSource =
        NonVisualBeanContainerInfo.get(canvas).getChildren(DataSourceInfo.class).get(0);
    assertThat(canvas.getChildren(FilterBuilderInfo.class).size()).isEqualTo(1);
    assertThat(canvas.getChildren(CalendarInfo.class).size()).isEqualTo(1);
    // create new DataSourceField
    DataSourceFieldInfo dataSourceField =
        createJavaInfo("com.smartgwt.client.data.fields.DataSourceBooleanField");
    {
      FlowContainer flowContainer = new FlowContainerFactory(dataSource, false).get().get(0);
      assertTrue(flowContainer.validateComponent(dataSourceField));
      flowContainer.command_CREATE(dataSourceField, null);
    }
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    DataSource dataSource = new DataSource();",
        "    {",
        "      DataSourceBooleanField dataSourceBooleanField = new DataSourceBooleanField('newDSBooleanField_1', 'New BooleanField');",
        "      dataSource.addField(dataSourceBooleanField);",
        "    }",
        "    FilterBuilder filterBuilder = new FilterBuilder();",
        "    filterBuilder.setDataSource(dataSource);",
        "    canvas.addChild(filterBuilder);",
        "    Calendar calendar = new Calendar();",
        "    calendar.setDataSource(dataSource);",
        "    canvas.addChild(calendar);",
        "    canvas.draw();",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests DataSource_Info#calculateStatementTarget(JavaInfo, Property, List<ASTNode>)
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_assignmentTarget_1() throws Exception {
    CanvasInfo calendar =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Calendar calendar = new Calendar();",
            "    DataSource dataSource = new DataSource();",
            "    dataSource.setID('testDS');",
            "    calendar.draw();",
            "  }",
            "}"});
    calendar.refresh();
    //
    GenericProperty property = (GenericProperty) calendar.getPropertyByTitle("dataSource");
    ObjectPropertyEditor editor = (ObjectPropertyEditor) property.getEditor();
    DataSourceInfo dataSource =
        NonVisualBeanContainerInfo.get(calendar).getChildren(DataSourceInfo.class).get(0);
    // check special target
    assertNull(dataSource.calculateStatementTarget(calendar));
    // assign
    editor.setComponent(property, dataSource);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private DataSource dataSource;",
        "  public void onModuleLoad() {",
        "    Calendar calendar = new Calendar();",
        "    calendar.setDataSource(getDataSource());",
        "    calendar.draw();",
        "  }",
        "  private DataSource getDataSource() {",
        "    if (dataSource == null) {",
        "      dataSource = new DataSource();",
        "      dataSource.setID('testDS');",
        "    }",
        "    return dataSource;",
        "  }",
        "}");
  }

  public void test_assignmentTarget_2() throws Exception {
    CanvasInfo calendar =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    DataSource dataSource = new DataSource();",
            "    dataSource.setID('testDS');",
            "    Calendar calendar = new Calendar();",
            "    calendar.draw();",
            "  }",
            "}"});
    calendar.refresh();
    //
    GenericProperty property = (GenericProperty) calendar.getPropertyByTitle("dataSource");
    ObjectPropertyEditor editor = (ObjectPropertyEditor) property.getEditor();
    DataSourceInfo dataSource =
        NonVisualBeanContainerInfo.get(calendar).getChildren(DataSourceInfo.class).get(0);
    // check special target
    StatementTarget target = dataSource.calculateStatementTarget(calendar);
    assertNotNull(target);
    Statement statement =
        AstNodeUtils.getEnclosingStatement(this.<ASTNode>getNode("new Calendar()"));
    assertTarget(target, null, statement, false);
    // assign
    editor.setComponent(property, dataSource);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    DataSource dataSource = new DataSource();",
        "    dataSource.setID('testDS');",
        "    Calendar calendar = new Calendar();",
        "    calendar.setDataSource(dataSource);",
        "    calendar.draw();",
        "  }",
        "}");
  }

  public void test_assignmentTarget_field_1() throws Exception {
    CanvasInfo calendar =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "/**",
            "  *@wbp.nonvisual location=0,0",
            "  */",
            "  private DataSource dataSource = new DataSource();",
            "  public void onModuleLoad() {",
            "    Calendar calendar = new Calendar();",
            "    calendar.draw();",
            "  }",
            "}"});
    calendar.refresh();
    //
    GenericProperty property = (GenericProperty) calendar.getPropertyByTitle("dataSource");
    ObjectPropertyEditor editor = (ObjectPropertyEditor) property.getEditor();
    DataSourceInfo dataSource =
        NonVisualBeanContainerInfo.get(calendar).getChildren(DataSourceInfo.class).get(0);
    // check special target
    assertNull(dataSource.calculateStatementTarget(calendar));
    // assign
    editor.setComponent(property, dataSource);
    assertEditor(
        "public class Test implements EntryPoint {",
        "/**",
        "  *@wbp.nonvisual location=0,0",
        "  */",
        "  private DataSource dataSource = new DataSource();",
        "  public void onModuleLoad() {",
        "    Calendar calendar = new Calendar();",
        "    calendar.setDataSource(dataSource);",
        "    calendar.draw();",
        "  }",
        "}");
  }

  public void test_assignmentTarget_field_2() throws Exception {
    CanvasInfo calendar =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "/**",
            "  *@wbp.nonvisual location=0,0",
            "  */",
            "  private DataSource dataSource = new DataSource();",
            "  private Calendar calendar = new Calendar();",
            "  public void onModuleLoad() {",
            "    calendar.draw();",
            "  }",
            "}"});
    calendar.refresh();
    GenericProperty property = (GenericProperty) calendar.getPropertyByTitle("dataSource");
    ObjectPropertyEditor editor = (ObjectPropertyEditor) property.getEditor();
    DataSourceInfo dataSource =
        NonVisualBeanContainerInfo.get(calendar).getChildren(DataSourceInfo.class).get(0);
    // check special target
    assertNull(dataSource.calculateStatementTarget(calendar));
    // assign
    editor.setComponent(property, dataSource);
    assertEditor(
        "public class Test implements EntryPoint {",
        "/**",
        "  *@wbp.nonvisual location=0,0",
        "  */",
        "  private DataSource dataSource = new DataSource();",
        "  private Calendar calendar = new Calendar();",
        "  public void onModuleLoad() {",
        "    calendar.setDataSource(dataSource);",
        "    calendar.draw();",
        "  }",
        "}");
  }

  public void test_assignmentTarget_field_3() throws Exception {
    CanvasInfo calendar =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "/**",
            "  *@wbp.nonvisual location=0,0",
            "  */",
            "  private DataSource dataSource = new DataSource();",
            "  private Calendar calendar = new Calendar();",
            "  public void onModuleLoad() {",
            "    dataSource.setID('testDS');",
            "    calendar.draw();",
            "  }",
            "}"});
    calendar.refresh();
    GenericProperty property = (GenericProperty) calendar.getPropertyByTitle("dataSource");
    ObjectPropertyEditor editor = (ObjectPropertyEditor) property.getEditor();
    DataSourceInfo dataSource =
        NonVisualBeanContainerInfo.get(calendar).getChildren(DataSourceInfo.class).get(0);
    // check special target
    StatementTarget target = dataSource.calculateStatementTarget(calendar);
    assertNotNull(target);
    Statement statement = AstNodeUtils.getEnclosingStatement(this.<ASTNode>getNode("testDS"));
    assertTarget(target, null, statement, false);
    // assign
    editor.setComponent(property, dataSource);
    assertEditor(
        "public class Test implements EntryPoint {",
        "/**",
        "  *@wbp.nonvisual location=0,0",
        "  */",
        "  private DataSource dataSource = new DataSource();",
        "  private Calendar calendar = new Calendar();",
        "  public void onModuleLoad() {",
        "    dataSource.setID('testDS');",
        "    calendar.setDataSource(dataSource);",
        "    calendar.draw();",
        "  }",
        "}");
  }

  public void test_assignmentTarget_lazy_1() throws Exception {
    CanvasInfo calendar =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  private DataSource dataSource;",
            "  public void onModuleLoad() {",
            "    Calendar calendar = new Calendar();",
            "    calendar.draw();",
            "  }",
            "/**",
            "*@wbp.nonvisual location=0,0",
            "*/",
            "  private DataSource getDataSource() {",
            "    if (dataSource == null) {",
            "      dataSource = new DataSource();",
            "      dataSource.setID('testDS');",
            "    }",
            "    return dataSource;",
            "  }",
            "}"});
    calendar.refresh();
    GenericProperty property = (GenericProperty) calendar.getPropertyByTitle("dataSource");
    ObjectPropertyEditor editor = (ObjectPropertyEditor) property.getEditor();
    DataSourceInfo dataSource =
        NonVisualBeanContainerInfo.get(calendar).getChildren(DataSourceInfo.class).get(0);
    // check special target
    assertNull(dataSource.calculateStatementTarget(calendar));
    // assign
    editor.setComponent(property, dataSource);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private DataSource dataSource;",
        "  public void onModuleLoad() {",
        "    Calendar calendar = new Calendar();",
        "    calendar.setDataSource(getDataSource());",
        "    calendar.draw();",
        "  }",
        "/**",
        "*@wbp.nonvisual location=0,0",
        "*/",
        "  private DataSource getDataSource() {",
        "    if (dataSource == null) {",
        "      dataSource = new DataSource();",
        "      dataSource.setID('testDS');",
        "    }",
        "    return dataSource;",
        "  }",
        "}");
  }

  public void test_assignmentTarget_lazy_2() throws Exception {
    CanvasInfo calendar =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  private DataSource dataSource;",
            "  private Calendar calendar = new Calendar();",
            "  public void onModuleLoad() {",
            "    calendar.draw();",
            "  }",
            "/**",
            "*@wbp.nonvisual location=0,0",
            "*/",
            "  private DataSource getDataSource() {",
            "    if (dataSource == null) {",
            "      dataSource = new DataSource();",
            "      dataSource.setID('testDS');",
            "    }",
            "    return dataSource;",
            "  }",
            "}"});
    calendar.refresh();
    GenericProperty property = (GenericProperty) calendar.getPropertyByTitle("dataSource");
    ObjectPropertyEditor editor = (ObjectPropertyEditor) property.getEditor();
    DataSourceInfo dataSource =
        NonVisualBeanContainerInfo.get(calendar).getChildren(DataSourceInfo.class).get(0);
    // check special target
    assertNull(dataSource.calculateStatementTarget(calendar));
    // assign
    editor.setComponent(property, dataSource);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private DataSource dataSource;",
        "  private Calendar calendar = new Calendar();",
        "  public void onModuleLoad() {",
        "    calendar.setDataSource(getDataSource());",
        "    calendar.draw();",
        "  }",
        "/**",
        "*@wbp.nonvisual location=0,0",
        "*/",
        "  private DataSource getDataSource() {",
        "    if (dataSource == null) {",
        "      dataSource = new DataSource();",
        "      dataSource.setID('testDS');",
        "    }",
        "    return dataSource;",
        "  }",
        "}");
  }

  public void test_assignmentTarget_lazy_3() throws Exception {
    CanvasInfo calendar =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  private DataSource dataSource;",
            "  private Calendar calendar = new Calendar();",
            "  public void onModuleLoad() {",
            "    getDataSource().setDataURL('url');",
            "    calendar.draw();",
            "  }",
            "/**",
            "*@wbp.nonvisual location=0,0",
            "*/",
            "  private DataSource getDataSource() {",
            "    if (dataSource == null) {",
            "      dataSource = new DataSource();",
            "      dataSource.setID('testDS');",
            "    }",
            "    return dataSource;",
            "  }",
            "}"});
    calendar.refresh();
    GenericProperty property = (GenericProperty) calendar.getPropertyByTitle("dataSource");
    ObjectPropertyEditor editor = (ObjectPropertyEditor) property.getEditor();
    DataSourceInfo dataSource =
        NonVisualBeanContainerInfo.get(calendar).getChildren(DataSourceInfo.class).get(0);
    // check special target
    StatementTarget target = dataSource.calculateStatementTarget(calendar);
    assertNotNull(target);
    Statement statement = AstNodeUtils.getEnclosingStatement(this.<ASTNode>getNode("url"));
    assertTarget(target, null, statement, false);
    // assign
    editor.setComponent(property, dataSource);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  private DataSource dataSource;",
        "  private Calendar calendar = new Calendar();",
        "  public void onModuleLoad() {",
        "    getDataSource().setDataURL('url');",
        "    calendar.setDataSource(getDataSource());",
        "    calendar.draw();",
        "  }",
        "/**",
        "*@wbp.nonvisual location=0,0",
        "*/",
        "  private DataSource getDataSource() {",
        "    if (dataSource == null) {",
        "      dataSource = new DataSource();",
        "      dataSource.setID('testDS');",
        "    }",
        "    return dataSource;",
        "  }",
        "}");
  }
}