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

import com.google.gdt.eclipse.designer.gwtext.model.widgets.PagingToolbarInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.PanelInfo;

/**
 * Tests for {@link PagingToolbarInfo}.
 * 
 * @author scheglov_ke
 */
public class PagingToolbarTest extends GwtExtModelTest {
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
            "      PagingToolbar toolbar = new PagingToolbar(new SimpleStore('field', new Object[]{}));",
            "      add(toolbar);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(toolbar)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.PagingToolbar} {local-unique: toolbar} {/new PagingToolbar(new SimpleStore('field', new Object[]{}))/ /add(toolbar)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
    panel.refresh();
    assertNoErrors(panel);
  }

  public void test_CREATE() throws Exception {
    PanelInfo panel =
        parseJavaInfo(
            "public class Test extends Panel {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}");
    panel.refresh();
    //
    PagingToolbarInfo toolbar = createJavaInfo("com.gwtext.client.widgets.PagingToolbar");
    panel.getLayout().command_CREATE(toolbar, null);
    assertEditor(
        "public class Test extends Panel {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      PagingToolbar pagingToolbar = new PagingToolbar(new SimpleStore('field', new Object[]{}));",
        "      add(pagingToolbar);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.gwtext.client.widgets.Panel} {this} {/setLayout(new RowLayout())/ /add(pagingToolbar)/}",
        "  {new: com.gwtext.client.widgets.layout.RowLayout} {empty} {/setLayout(new RowLayout())/}",
        "  {new: com.gwtext.client.widgets.PagingToolbar} {local-unique: pagingToolbar} {/new PagingToolbar(new SimpleStore('field', new Object[]{}))/ /add(pagingToolbar)/}",
        "    {virtual-layout_data: com.gwtext.client.widgets.layout.RowLayoutData} {virtual-layout-data} {}");
  }
}