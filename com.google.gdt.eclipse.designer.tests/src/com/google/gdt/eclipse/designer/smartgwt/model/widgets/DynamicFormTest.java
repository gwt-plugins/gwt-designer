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
import com.google.gdt.eclipse.designer.smart.model.form.CanvasItemInfo;
import com.google.gdt.eclipse.designer.smart.model.form.DynamicFormInfo;
import com.google.gdt.eclipse.designer.smart.model.form.FormItemInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.nonvisual.ArrayObjectInfo;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link DynamicFormInfo}.
 * 
 * @author sablin_aa
 */
public class DynamicFormTest extends SmartGwtModelTest {
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
    assertFalse(form.isAbsoluteItemLayout());
    List<FormItemInfo> list = form.getItems();
    assertEquals(1, list.size());
    List<ArrayObjectInfo> arrayInfos = form.getChildren(ArrayObjectInfo.class);
    assertEquals(1, arrayInfos.size());
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
    assertFalse(form.isAbsoluteItemLayout());
    List<FormItemInfo> list = form.getItems();
    assertEquals(1, list.size());
    List<ArrayObjectInfo> arrayInfos = form.getChildren(ArrayObjectInfo.class);
    assertEquals(1, arrayInfos.size());
  }

  /**
   * Absolute item layout.
   */
  public void test_ABSOLUTE() throws Exception {
    DynamicFormInfo form = parseJavaInfo( // filler
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    setItemLayout(FormLayoutType.ABSOLUTE);",
        "  }",
        "}");
    form.refresh();
    // 
    assertTrue(form.isAbsoluteItemLayout());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands: for FormItem
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_item_CREATE() throws Exception {
    DynamicFormInfo form = parseJavaInfo( // filler
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "  }",
        "}");
    form.refresh();
    // create field
    FormItemInfo newField = createJavaInfo("com.smartgwt.client.widgets.form.fields.TextItem");
    form.command_CREATE(newField, null);
    //
    assertEditor(
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    setFields(new FormItem[] { new TextItem('newTextItem_1', 'New TextItem')});",
        "  }",
        "}");
  }

  public void test_item_MOVE() throws Exception {
    DynamicFormInfo form =
        parseJavaInfo( // filler
            "public class Test extends DynamicForm {",
            "  public Test() {",
            "    setFields(new FormItem[] { new TextItem('newTextItem'), new DateItem('newDateItem')});",
            "  }",
            "}");
    form.refresh();
    // 
    List<FormItemInfo> fields = form.getItems();
    FormItemInfo textField = fields.get(0);
    FormItemInfo dateField = fields.get(1);
    // move field
    form.command_MOVE(dateField, textField);
    //
    assertEditor(
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    setFields(new FormItem[] { new DateItem('newDateItem'), new TextItem('newTextItem')});",
        "  }",
        "}");
  }

  public void test_item_MOVE_ext() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo( // filler
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    Canvas canvas = new Canvas();",
            "    DynamicForm form = new DynamicForm();",
            "    form.setFields(new FormItem[] { new TextItem('newTextItem'), new DateItem('newDateItem')});",
            "    canvas.addChild(form);",
            "    canvas.draw();",
            "  }",
            "}");
    canvas.refresh();
    DynamicFormInfo form = canvas.getChildren(DynamicFormInfo.class).get(0);
    List<FormItemInfo> items = form.getItems();
    assertThat(items.size()).isEqualTo(2);
    // create new form
    DynamicFormInfo newForm = createJavaInfo("com.smartgwt.client.widgets.form.DynamicForm");
    canvas.command_absolute_CREATE(newForm, null);
    // move field
    newForm.command_MOVE(items.get(0), null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    Canvas canvas = new Canvas();",
        "    DynamicForm form = new DynamicForm();",
        "    form.setFields(new FormItem[] { new DateItem('newDateItem')});",
        "    canvas.addChild(form);",
        "    {",
        "      DynamicForm dynamicForm = new DynamicForm();",
        "      dynamicForm.setFields(new FormItem[] { new TextItem('newTextItem')});",
        "      canvas.addChild(dynamicForm);",
        "    }",
        "    canvas.draw();",
        "  }",
        "}");
  }

  public void test_item_BOUNDS() throws Exception {
    DynamicFormInfo form = parseJavaInfo( // filler
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    setItemLayout(FormLayoutType.ABSOLUTE);",
        "    setFields(new FormItem[] { new TextItem('newTextItem')});",
        "  }",
        "}");
    form.refresh();
    // set location
    FormItemInfo formItem = form.getItems().get(0);
    Point location = new Point(30, 20);
    form.command_BOUNDS(formItem, location, null);
    form.refresh();
    assertThat(formItem.getBounds().getLocation()).isEqualTo(location);
    assertEditor(
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    setItemLayout(FormLayoutType.ABSOLUTE);",
        "    TextItem textItem = new TextItem('newTextItem');",
        "    textItem.setLeft(30);",
        "    textItem.setTop(20);",
        "    setFields(new FormItem[] { textItem});",
        "  }",
        "}");
    // set size
    form.command_BOUNDS(formItem, null, new Dimension(60, 50));
    form.refresh();
    assertThat(formItem.getBounds().getSize()).isEqualTo(new Dimension(60, 50));
    assertEditor(
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    setItemLayout(FormLayoutType.ABSOLUTE);",
        "    TextItem textItem = new TextItem('newTextItem');",
        "    textItem.setWidth(60);",
        "    textItem.setHeight(50);",
        "    textItem.setLeft(30);",
        "    textItem.setTop(20);",
        "    setFields(new FormItem[] { textItem});",
        "  }",
        "}");
    // change location 
    form.command_BOUNDS(formItem, new Point(10, 10), null);
    form.refresh();
    assertThat(formItem.getBounds().getLocation()).isEqualTo(new Point(10, 10));
    assertEditor(
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    setItemLayout(FormLayoutType.ABSOLUTE);",
        "    TextItem textItem = new TextItem('newTextItem');",
        "    textItem.setWidth(60);",
        "    textItem.setHeight(50);",
        "    textItem.setLeft(10);",
        "    textItem.setTop(10);",
        "    setFields(new FormItem[] { textItem});",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands: for Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE() throws Exception {
    DynamicFormInfo form = parseJavaInfo( // filler
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "  }",
        "}");
    form.refresh();
    // create field
    CanvasInfo newLabel = createJavaInfo("com.smartgwt.client.widgets.Label");
    form.command_CREATE(newLabel, null);
    assertThat(newLabel.getParent()).isInstanceOf(FormItemInfo.class);
    assertThat(newLabel.getParent().getParent()).isSameAs(form);
    //
    assertEditor(
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    CanvasItem canvasItem = new CanvasItem('newCanvasItem_1', 'New CanvasItem');",
        "    {",
        "      Label label = new Label('New Label');",
        "      canvasItem.setCanvas(label);",
        "    }",
        "    setFields(new FormItem[] { canvasItem});",
        "  }",
        "}");
  }

  public void test_canvas_MOVE() throws Exception {
    DynamicFormInfo form = parseJavaInfo( // filler
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    CanvasItem canvasItem_1 = new CanvasItem('newCanvasItem_1');",
        "    {",
        "      Label label = new Label('New Label');",
        "      canvasItem_1.setCanvas(label);",
        "    }",
        "    CanvasItem canvasItem_2 = new CanvasItem('newCanvasItem_1');",
        "    {",
        "      Button button = new Button();",
        "      canvasItem_2.setCanvas(button);",
        "    }",
        "    setFields(new FormItem[] { canvasItem_1, canvasItem_2 });",
        "  }",
        "}");
    form.refresh();
    // 
    List<FormItemInfo> fields = form.getItems();
    CanvasItemInfo labelItem = (CanvasItemInfo) fields.get(0);
    CanvasItemInfo buttonItem = (CanvasItemInfo) fields.get(1);
    // move canvas
    form.command_MOVE(buttonItem.getCanvas(), labelItem);
    // 
    assertEditor(
        "public class Test extends DynamicForm {",
        "  public Test() {",
        "    CanvasItem canvasItem_1 = new CanvasItem('newCanvasItem_1');",
        "    {",
        "      Label label = new Label('New Label');",
        "      canvasItem_1.setCanvas(label);",
        "    }",
        "    CanvasItem canvasItem_2 = new CanvasItem('newCanvasItem_1');",
        "    {",
        "      Button button = new Button();",
        "      canvasItem_2.setCanvas(button);",
        "    }",
        "    setFields(new FormItem[] { canvasItem_2, canvasItem_1 });",
        "  }",
        "}");
  }
}