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
package com.google.gdt.eclipse.designer.gxt.model.widgets.form;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.layout.FormDataInfo;
import com.google.gdt.eclipse.designer.gxt.model.layout.FormLayoutInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ButtonInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.FieldInfo;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link FormPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class FormPanelTest extends GxtModelTest {
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
   * By default {@link FormLayoutInfo} is used.
   */
  public void test_implicitLayout_FormLayout() throws Exception {
    FormPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends FormPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.form.FormPanel} {this} {}",
        "  {implicit-layout: com.extjs.gxt.ui.client.widget.layout.FormLayout} {implicit-layout} {}");
    assertThat(panel.getLayout()).isInstanceOf(FormLayoutInfo.class);
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Test that implicit {@link FormLayoutInfo} works, so {@link FormDataInfo} is set.
   */
  public void test_parseWithField() throws Exception {
    parseJavaInfo(
        "// filler filler filler",
        "public class Test extends FormPanel {",
        "  public Test() {",
        "    {",
        "      TextField field = new TextField();",
        "      add(field);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.form.FormPanel} {this} {/add(field)/}",
        "  {implicit-layout: com.extjs.gxt.ui.client.widget.layout.FormLayout} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.form.TextField} {local-unique: field} {/new TextField()/ /add(field)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FormData} {virtual-layout-data} {}");
  }

  public void test_addField() throws Exception {
    FormPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends FormPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    //
    FieldInfo field = createJavaInfo("com.extjs.gxt.ui.client.widget.form.TextField");
    panel.getLayout().command_CREATE(field);
    assertEditor(
        "// filler filler filler",
        "public class Test extends FormPanel {",
        "  public Test() {",
        "    {",
        "      TextField textField = new TextField();",
        "      add(textField, new FormData('100%'));",
        "      textField.setFieldLabel('New TextField');",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.form.FormPanel} {this} {/add(textField, new FormData('100%'))/}",
        "  {implicit-layout: com.extjs.gxt.ui.client.widget.layout.FormLayout} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.form.TextField} {local-unique: textField} {/new TextField()/ /textField.setFieldLabel('New TextField')/ /add(textField, new FormData('100%'))/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.FormData} {empty} {/add(textField, new FormData('100%'))/}");
  }

  public void test_ButtonBar_addButton() throws Exception {
    FormPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends FormPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    //
    ButtonInfo field = createButton();
    panel.command_ButtonBar_CREATE(field, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends FormPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      addButton(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.form.FormPanel} {this} {/addButton(button)/}",
        "  {implicit-layout: com.extjs.gxt.ui.client.widget.layout.FormLayout} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.button.Button empty} {local-unique: button} {/new Button()/ /addButton(button)/}");
  }
}