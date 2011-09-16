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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;

/**
 * Test for <code>com.extjs.gxt.ui.client.widget.form.ComboBox</code>.
 * 
 * @author scheglov_ke
 */
public class ComboBoxTest extends GxtModelTest {
  public void test_CREATE() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // prepare new ComboBox
    WidgetInfo newCombo = createJavaInfo("com.extjs.gxt.ui.client.widget.form.ComboBox");
    // do create
    panel.command_CREATE2(newCombo, null);
    assertEditor(
        "import com.extjs.gxt.ui.client.store.ListStore;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      ComboBox comboBox = new ComboBox();",
        "      comboBox.setStore(new ListStore());",
        "      add(comboBox);",
        "      comboBox.setFieldLabel('New ComboBox');",
        "    }",
        "  }",
        "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Ignore "ComboBox.setStore(null)", use empty "ListStore()" instead.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44578
   */
  public void test_parse_setStore_null() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      ComboBox comboBox = new ComboBox();",
            "      comboBox.setStore(null);",
            "      add(comboBox);",
            "    }",
            "  }",
            "}");
    container.refresh();
    assertNoErrors(container);
  }
}