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

/**
 * Test for <code>HtmlContainer</code>.
 * 
 * @author scheglov_ke
 */
public class HtmlContainerTest extends GxtModelTest {
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
  public void test_0() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    {",
            "      HtmlContainer container = new HtmlContainer('<div class=b1></div>');",
            "      {",
            "        Button button = new Button();",
            "        container.add(button, 'div.b1');",
            "      }",
            "      add(container);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(container)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.HtmlContainer} {local-unique: container} {/new HtmlContainer('<div class=b1></div>')/ /container.add(button, 'div.b1')/ /add(container)/}",
        "    {new: com.extjs.gxt.ui.client.widget.button.Button} {local-unique: button} {/new Button()/ /container.add(button, 'div.b1')/}");
  }
}