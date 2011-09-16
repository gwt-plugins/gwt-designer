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
package com.google.gdt.eclipse.designer.gwtext.layout;

import com.google.gdt.eclipse.designer.gwtext.GwtExtModelTest;
import com.google.gdt.eclipse.designer.gwtext.model.layout.AnchorLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.layout.FormLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

/**
 * Tests for {@link FormLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class FormLayoutTest extends GwtExtModelTest {
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
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      Label label = new Label();",
            "      add(label);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new FormLayout())/ /add(label)/}",
        "  {new: com.gwtext.client.widgets.layout.FormLayout} {empty} {/setLayout(new FormLayout())/}",
        "  {new: com.gwtext.client.widgets.form.Label} {local-unique: label} {/new Label()/ /add(label)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.AnchorLayoutData} {virtual-layout-data} {}");
    //
    assertInstanceOf(FormLayoutInfo.class, panel.getLayout());
    WidgetInfo label = panel.getChildrenWidgets().get(0);
    assertNotNull(AnchorLayoutInfo.getAnchorData(label));
  }
}