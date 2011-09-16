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

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.FieldInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;

/**
 * Test for {@link FormLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class FormLayoutTest extends GxtModelTest {
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
    container.refresh();
    // set FormLayout
    FormLayoutInfo layout = createJavaInfo("com.extjs.gxt.ui.client.widget.layout.FormLayout");
    container.setLayout(layout);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FormLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FormLayout} {empty} {/setLayout(new FormLayout())/}");
    assertSame(layout, container.getLayout());
  }

  public void test_parse() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      LabelField labelField = new LabelField();",
            "      add(labelField);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FormLayout())/ /add(labelField)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FormLayout} {empty} {/setLayout(new FormLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.form.LabelField} {local-unique: labelField} {/new LabelField()/ /add(labelField)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FormData} {virtual-layout-data} {}");
    container.refresh();
    // 
    WidgetInfo field = container.getWidgets().get(0);
    FormDataInfo formData = FormLayoutInfo.getFormData(field);
    assertNotNull(formData);
  }

  /**
   * Test for flow container support.
   */
  public void test_flowContainer() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "  }",
            "}");
    container.refresh();
    FormLayoutInfo layout = (FormLayoutInfo) container.getLayout();
    FlowContainer flowContainer = new FlowContainerFactory(layout, false).get().get(0);
    // add new LabelField
    FieldInfo newField = createJavaInfo("com.extjs.gxt.ui.client.widget.form.LabelField", "empty");
    assertTrue(flowContainer.validateComponent(newField));
    flowContainer.command_CREATE(newField, null);
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      LabelField labelField = new LabelField();",
        "      add(labelField, new FormData('100%'));",
        "    }",
        "  }",
        "}");
  }
}