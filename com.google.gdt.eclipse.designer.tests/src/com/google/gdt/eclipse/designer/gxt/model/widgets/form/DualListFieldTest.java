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
import com.google.gdt.eclipse.designer.gxt.model.widgets.FieldInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;

/**
 * Test for <code>com.extjs.gxt.ui.client.widget.form.DualListField</code>.
 * 
 * @author scheglov_ke
 */
public class DualListFieldTest extends GxtModelTest {
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
  // Client area insets
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    refresh();
    // add new DualListField
    {
      FieldInfo newField = createJavaInfo("com.extjs.gxt.ui.client.widget.form.DualListField");
      container.getLayout().command_CREATE(newField);
    }
    assertEditor(
        "import com.extjs.gxt.ui.client.store.ListStore;",
        "// filler filler filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      DualListField dualListField = new DualListField();",
        "      dualListField.getFromList().setStore(new ListStore());",
        "      dualListField.getToList().setStore(new ListStore());",
        "      add(dualListField);",
        "      dualListField.setFieldLabel('New DualListField');",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(dualListField)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.form.DualListField} {local-unique: dualListField} {/new DualListField()/ /dualListField.setFieldLabel('New DualListField')/ /add(dualListField)/ /dualListField.getToList()/ /dualListField.getFromList()/}");
    // no exception
    refresh();
    assertNoErrors(container);
  }
}