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
package com.google.gdt.eclipse.designer.gxt.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.gxt.uibinder.model.GxtUiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;

/**
 * Test for <code>com.extjs.gxt.ui.client.widget.grid.Grid</code> widget.
 * 
 * @author scheglov_ke
 */
public class GridTest extends GxtUiBinderModelTest {
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
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  @UiField(provided=true) com.extjs.gxt.ui.client.widget.grid.Grid grid;",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <xg:Grid wbp:name='grid' ui:field='grid'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    // "grid" was rendered
    WidgetInfo grid = getObjectByName("grid");
    assertEquals(
        "com.extjs.gxt.ui.client.widget.grid.Grid",
        grid.getObject().getClass().getCanonicalName());
  }
}