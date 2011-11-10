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

import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.TabSetInfo;
import com.google.gdt.eclipse.designer.smart.model.form.CanvasItemInfo;
import com.google.gdt.eclipse.designer.smart.model.form.DynamicFormInfo;
import com.google.gdt.eclipse.designer.smart.model.form.FormItemInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.IntValue;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link FormItemInfo}.
 * 
 * @author sablin_aa
 */
public class FormItemTest extends SmartGwtModelTest {
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
  /**
   * Parsing.
   */
  public void test_parse() throws Exception {
    DynamicFormInfo form =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    DynamicForm form = new DynamicForm();",
            "    FormItem field = new TextItem('field', 'Field');",
            "    form.setFields(new FormItem[] { field });",
            "    form.draw();",
            "  }",
            "}");
    form.refresh();
    //
    FormItemInfo formItem = form.getItems().get(0);
    Integer width = Expectations.get(150, new IntValue[]{new IntValue("flanker-linux", 146)});
    assertThat(formItem.getModelBounds()).isEqualTo(new Rectangle(28, 0, width, 22));
  }

  public void test_parse_this() throws Exception {
    DynamicFormInfo form =
        parseJavaInfo(
            "public class Test extends DynamicForm {",
            "  public Test() {",
            "    FormItem field = new TextItem('field', 'Field');",
            "    setFields(new FormItem[] { field });",
            "  }",
            "}");
    form.refresh();
    //
    FormItemInfo formItem = form.getItems().get(0);
    Integer left = Expectations.get(28, new IntValue[]{new IntValue("flanker-linux", 100)});
    Integer width = Expectations.get(150, new IntValue[]{new IntValue("flanker-linux", 146)});
    assertThat(formItem.getModelBounds()).isEqualTo(new Rectangle(left, 0, width, 22));
  }

  /**
   * Test restriction for "name" property value.
   * <p>
   * We need this because SmartGWT does not allow spaces in field name.
   */
  public void test_name_property() throws Exception {
    DynamicFormInfo form =
        parseJavaInfo(
            "public class Test extends DynamicForm {",
            "  public Test() {",
            "    FormItem field = new TextItem('field', 'Field');",
            "    setFields(new FormItem[] { field });",
            "  }",
            "}");
    form.refresh();
    //
    FormItemInfo formItem = form.getItems().get(0);
    Property nameProperty = formItem.getPropertyByTitle("name");
    assertThat(nameProperty.getValue()).isEqualTo("field");
    // set new "name"
    nameProperty.setValue("field 1");
    // check corrected value
    assertThat(nameProperty.getValue()).isEqualTo("field_1");
    assertEditor(
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    FormItem field = new TextItem('field_1', 'Field');",
        "    setFields(new FormItem[] { field });",
        "  }",
        "}");
  }

  /**
   * Test that "title" property is not affected by "name" restriction.
   * <p>
   * http://forums.instantiations.com/viewtopic.php?f=11&t=5461
   */
  public void test_title_property() throws Exception {
    parseJavaInfo(
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    FormItem field = new TextItem('field', 'Field');",
        "    setFields(new FormItem[] { field });",
        "  }",
        "}");
    refresh();
    FormItemInfo field = getJavaInfoByName("field");
    Property property = field.getPropertyByTitle("title");
    // initial value
    assertEquals("Field", property.getValue());
    // set new value, with spaces
    property.setValue("My field");
    assertEquals("My field", property.getValue());
    assertEditor(
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    FormItem field = new TextItem('field', 'My field');",
        "    setFields(new FormItem[] { field });",
        "  }",
        "}");
  }

  /**
   * Live image.
   */
  public void test_liveImage() throws Exception {
    DynamicFormInfo form = parseJavaInfo( // filler
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "  }",
        "}");
    form.refresh();
    //
    FormItemInfo newField = createJavaInfo("com.smartgwt.client.widgets.form.fields.TextItem");
    assertNotNull(newField.getImage());
  }

  public void test_bounds_property() throws Exception {
    DynamicFormInfo form =
        parseJavaInfo(
            "public class Test extends DynamicForm {",
            "  public Test() {",
            "    setItemLayout(FormLayoutType.ABSOLUTE);",
            "    FormItem field = new TextItem('field', 'Field');",
            "    setFields(new FormItem[] { field });",
            "  }",
            "}");
    form.refresh();
    //
    FormItemInfo item = form.getItems().get(0);
    Property property = item.getPropertyByTitle("Bounds");
    assertThat(property).isNotNull();
    assertThat(property).isInstanceOf(ComplexProperty.class);
  }

  public void test_canvasItem() throws Exception {
    DynamicFormInfo form = parseJavaInfo( // filler
        "public class Test extends DynamicForm {",
        "  public Test() {",
        //"    setItemLayout(FormLayoutType.ABSOLUTE);",
        "    CanvasItem field = new CanvasItem('field');",
        "    Label label = new Label('Label');",
        "    field.setCanvas(label);",
        "    setFields(new FormItem[] { field });",
        "  }",
        "}");
    form.refresh();
    //
    CanvasItemInfo canvasItem = (CanvasItemInfo) form.getItems().get(0);
    CanvasInfo canvas = canvasItem.getCanvas();
    assertThat(canvas).isNotNull();
    assertThat(canvasItem.getBounds().getSize()).isEqualTo(new Dimension(350, 100));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setLeft_setTop() throws Exception {
    DynamicFormInfo form =
        parseJavaInfo(
            "public class Test extends DynamicForm {",
            "  public Test() {",
            "    setItemLayout(FormLayoutType.ABSOLUTE);",
            "    FormItem field = new TextItem('field', 'Field');",
            "    field.setLeft(70);",
            "    field.setTop(50);",
            "    setFields(new FormItem[] { field });",
            "  }",
            "}");
    form.refresh();
    //
    FormItemInfo item = form.getItems().get(0);
    Rectangle bounds = item.getBounds();
    assertThat(bounds.getLocation()).isEqualTo(new Point(70, 50));
  }

  public void test_setWidth_setHeight() throws Exception {
    DynamicFormInfo form =
        parseJavaInfo(
            "public class Test extends DynamicForm {",
            "  public Test() {",
            "    setItemLayout(FormLayoutType.ABSOLUTE);",
            "    FormItem field = new TextItem('field', 'Field');",
            "    field.setWidth(150);",
            "    field.setHeight(100);",
            "    setFields(new FormItem[] { field });",
            "  }",
            "}");
    form.refresh();
    //
    FormItemInfo item = form.getItems().get(0);
    Rectangle bounds = item.getBounds();
    assertThat(bounds.getSize()).isEqualTo(new Dimension(150, 100));
  }

  public void test_bounds_update() throws Exception {
    DynamicFormInfo form =
        parseJavaInfo(
            "public class Test extends DynamicForm {",
            "  public Test() {",
            "    setItemLayout(FormLayoutType.ABSOLUTE);",
            "    FormItem field = new TextItem('field', 'Field');",
            "    setFields(new FormItem[] { field });",
            "  }",
            "}");
    form.refresh();
    // set new bounds
    FormItemInfo item = form.getItems().get(0);
    form.command_BOUNDS(item, new Point(30, 20), new Dimension(150, 100));
    form.refresh();
    // check bounds
    Rectangle bounds = item.getBounds();
    assertThat(bounds.getLocation()).isEqualTo(new Point(30, 20));
    assertThat(bounds.getSize()).isEqualTo(new Dimension(150, 100));
    assertEditor(
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    setItemLayout(FormLayoutType.ABSOLUTE);",
        "    FormItem field = new TextItem('field', 'Field');",
        "    field.setLeft(30);",
        "    field.setTop(20);",
        "    field.setWidth(150);",
        "    field.setHeight(100);",
        "    setFields(new FormItem[] { field });",
        "  }",
        "}");
  }

  public void test_setSize_update() throws Exception {
    DynamicFormInfo form =
        parseJavaInfo(
            "public class Test extends DynamicForm {",
            "  public Test() {",
            "    setItemLayout(FormLayoutType.ABSOLUTE);",
            "    FormItem field = new TextItem('field', 'Field');",
            "    field.setWidth(150);",
            "    field.setHeight(100);",
            "    setFields(new FormItem[] { field });",
            "  }",
            "}");
    form.refresh();
    //
    FormItemInfo item = form.getItems().get(0);
    form.command_BOUNDS(item, null, new Dimension(70, 30));
    form.refresh();
    // check bounds
    Rectangle bounds = item.getBounds();
    assertThat(bounds.getSize()).isEqualTo(new Dimension(70, 30));
    assertEditor(
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    setItemLayout(FormLayoutType.ABSOLUTE);",
        "    FormItem field = new TextItem('field', 'Field');",
        "    field.setWidth(70);",
        "    field.setHeight(30);",
        "    setFields(new FormItem[] { field });",
        "  }",
        "}");
  }

  public void test_setLocation_update() throws Exception {
    DynamicFormInfo form =
        parseJavaInfo(
            "public class Test extends DynamicForm {",
            "  public Test() {",
            "    setItemLayout(FormLayoutType.ABSOLUTE);",
            "    FormItem field = new TextItem('field', 'Field');",
            "    field.setLeft(70);",
            "    field.setTop(50);",
            "    setFields(new FormItem[] { field });",
            "  }",
            "}");
    form.refresh();
    //
    FormItemInfo item = form.getItems().get(0);
    form.command_BOUNDS(item, new Point(20, 10), null);
    form.refresh();
    // check bounds
    Rectangle bounds = item.getBounds();
    assertThat(bounds.getLocation()).isEqualTo(new Point(20, 10));
    assertEditor(
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    setItemLayout(FormLayoutType.ABSOLUTE);",
        "    FormItem field = new TextItem('field', 'Field');",
        "    field.setLeft(20);",
        "    field.setTop(10);",
        "    setFields(new FormItem[] { field });",
        "  }",
        "}");
  }

  /**
   * Test dispose objects when it been not rendered.
   */
  public void test_dispose() throws Exception {
    TabSetInfo tabSet =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    TabSet tabSet = new TabSet();",
            "    tabSet.addTab(new Tab('Tab_1'));",
            "    Tab tab = new Tab('Tab_2');",
            "    {",
            "      DynamicForm form = new DynamicForm();",
            "      ButtonItem buttonItem = new ButtonItem('buttonItem', 'Button');",
            "      form.setFields(new FormItem[] { buttonItem });",
            "      tab.setPane(form);",
            "    }",
            "    tabSet.addTab(tab);",
            "    tabSet.draw();",
            "  }",
            "}"});
    tabSet.refresh();
  }
}