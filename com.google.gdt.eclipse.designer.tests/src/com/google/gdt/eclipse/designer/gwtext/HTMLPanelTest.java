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
package com.google.gdt.eclipse.designer.gwtext;

import com.google.gdt.eclipse.designer.gwtext.model.widgets.HTMLPanelInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;

/**
 * Tests for {@link HTMLPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class HTMLPanelTest extends GwtExtModelTest {
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
            "    setLayout(new RowLayout());",
            "    {",
            "      HTMLPanel htmlPanel = new HTMLPanel('my HTML', 10);",
            "      add(htmlPanel);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(htmlPanel)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.HTMLPanel} {local-unique: htmlPanel} {/new HTMLPanel('my HTML', 10)/ /add(htmlPanel)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
    HTMLPanelInfo htmlPanel = (HTMLPanelInfo) panel.getChildrenWidgets().get(0);
    // check properties
    assertEquals("my HTML", htmlPanel.getPropertyByTitle("html").getValue());
    assertEquals(10, htmlPanel.getPropertyByTitle("paddings").getValue());
  }
}