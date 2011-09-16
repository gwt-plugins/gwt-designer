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
import com.google.gdt.eclipse.designer.gxt.model.layout.FormLayoutInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.FieldInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.SliderInfo;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link SliderFieldInfo}.
 * 
 * @author scheglov_ke
 */
public class SliderFieldTest extends GxtModelTest {
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
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      Slider slider = new Slider();",
            "      SliderField sliderField = new SliderField(slider);",
            "      add(sliderField);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FormLayout())/ /add(sliderField)/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FormLayout} {empty} {/setLayout(new FormLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.form.SliderField} {local-unique: sliderField} {/new SliderField(slider)/ /add(sliderField)/}",
        "    {new: com.extjs.gxt.ui.client.widget.Slider} {local-unique: slider} {/new Slider()/ /new SliderField(slider)/}",
        "    {virtual-layout_data: com.extjs.gxt.ui.client.widget.layout.FormData} {virtual-layout-data} {}");
    // 
    container.refresh();
    assertNoErrors(container);
    // SliderField has only one constructor
    {
      SliderFieldInfo sliderField = getJavaInfoByName("sliderField");
      List<ConstructorDescription> constructors = sliderField.getDescription().getConstructors();
      assertThat(constructors).hasSize(1);
    }
    // Slider
    {
      SliderInfo slider = getJavaInfoByName("slider");
      // Slider can not be deleted, because SliderField needs it
      assertFalse(slider.canDelete());
      assertFalse(JavaInfoUtils.canReparent(slider));
      // Slider should not be visible on design canvas, because we want that user click and move field
      assertVisibleInGraphical(slider, false);
    }
  }

  /**
   * Test for new {@link SliderFieldInfo}.
   */
  public void test_CREATE() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "  }",
            "}");
    container.refresh();
    FormLayoutInfo layout = (FormLayoutInfo) container.getLayout();
    // add new SliderField
    final FlowContainer flowContainer = new FlowContainerFactory(layout, false).get().get(0);
    ExecutionUtils.run(container, new RunnableEx() {
      public void run() throws Exception {
        FieldInfo newField = createJavaInfo("com.extjs.gxt.ui.client.widget.form.SliderField");
        assertTrue(flowContainer.validateComponent(newField));
        flowContainer.command_CREATE(newField, null);
      }
    });
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      Slider slider = new Slider();",
        "      slider.setValue(30);",
        "      SliderField sliderField = new SliderField(slider);",
        "      add(sliderField, new FormData('100%'));",
        "      sliderField.setFieldLabel('New SliderField');",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/setLayout(new FormLayout())/ /add(sliderField, new FormData('100%'))/}",
        "  {new: com.extjs.gxt.ui.client.widget.layout.FormLayout} {empty} {/setLayout(new FormLayout())/}",
        "  {new: com.extjs.gxt.ui.client.widget.form.SliderField} {local-unique: sliderField} {/new SliderField(slider)/ /sliderField.setFieldLabel('New SliderField')/ /add(sliderField, new FormData('100%'))/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.FormData} {empty} {/add(sliderField, new FormData('100%'))/}",
        "    {new: com.extjs.gxt.ui.client.widget.Slider} {local-unique: slider} {/new SliderField(slider)/ /slider.setValue(30)/ /new Slider()/}");
  }
}