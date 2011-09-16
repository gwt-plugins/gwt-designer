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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.LazyPanelInfo;

/**
 * Test for {@link LazyPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class LazyPanelTest extends GwtModelTest {
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
    LazyPanelInfo panel =
        parseJavaInfo(
            "public class Test extends LazyPanel {",
            "  protected Widget createWidget() {",
            "    return new Button();",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.LazyPanel} {this} {}",
        "  {new: com.google.gwt.user.client.ui.Button} {empty} {/new Button()/}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  public void test_setSize_forContent() throws Exception {
    LazyPanelInfo panel =
        parseJavaInfo(
            "public class Test extends LazyPanel {",
            "  public Test() {",
            "  }",
            "  protected Widget createWidget() {",
            "    return new Button();",
            "  }",
            "}");
    panel.refresh();
    WidgetInfo button = panel.getWidget();
    //
    button.getSizeSupport().setSize("200px", "100px");
    assertEditor(
        "public class Test extends LazyPanel {",
        "  public Test() {",
        "  }",
        "  protected Widget createWidget() {",
        "    Button button = new Button();",
        "    button.setSize('200px', '100px');",
        "    return button;",
        "  }",
        "}");
  }
}