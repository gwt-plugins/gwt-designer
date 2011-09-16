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
import com.google.gdt.eclipse.designer.model.widgets.CompositeInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

/**
 * Test for <code>DateBox</code>.
 * 
 * @author scheglov_ke
 */
public class DateBoxTest extends GwtModelTest {
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
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.google.gwt.user.datepicker.client.DateBox;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      DateBox dateBox = new DateBox();",
            "      rootPanel.add(dateBox);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    CompositeInfo dateBox = (CompositeInfo) frame.getChildrenWidgets().get(0);
    assertFalse(dateBox.shouldDrawDotsBorder());
  }
}