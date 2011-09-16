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
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

/**
 * Tests for <code>com.extjs.gxt.ui.client.widget.button.ButtonGroup</code>.
 * 
 * @author scheglov_ke
 */
public class ButtonGroupTest extends GxtModelTest {
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
   * We should be able to parse <code>ButtonGroup</code> with layout, but without children.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44166
   */
  public void test_parseEmpty_onThisContainer() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      ButtonGroup buttonGroup = new ButtonGroup(1);",
            "      buttonGroup.setLayout(new FlowLayout(5));",
            "      add(buttonGroup);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(buttonGroup)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.button.ButtonGroup} {local-unique: buttonGroup} {/new ButtonGroup(1)/ /buttonGroup.setLayout(new FlowLayout(5))/ /add(buttonGroup)/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/buttonGroup.setLayout(new FlowLayout(5))/}");
    container.refresh();
    assertNoErrors(container);
  }

  /**
   * We should be able to parse <code>ButtonGroup</code> with layout, but without children.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44166
   */
  public void test_parseEmpty_onRootPanel() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      ButtonGroup buttonGroup = new ButtonGroup(1);",
            "      buttonGroup.setLayout(new FlowLayout(5));",
            "      rootPanel.add(buttonGroup);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/ /rootPanel.add(buttonGroup)/}",
        "  {new: com.extjs.gxt.ui.client.widget.button.ButtonGroup} {local-unique: buttonGroup} {/new ButtonGroup(1)/ /buttonGroup.setLayout(new FlowLayout(5))/ /rootPanel.add(buttonGroup)/}",
        "    {new: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {empty} {/buttonGroup.setLayout(new FlowLayout(5))/}");
    frame.refresh();
    assertNoErrors(frame);
  }
}