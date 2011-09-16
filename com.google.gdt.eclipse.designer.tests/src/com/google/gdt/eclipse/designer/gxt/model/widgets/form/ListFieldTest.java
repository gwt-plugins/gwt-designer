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
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;

/**
 * Test for <code>com.extjs.gxt.ui.client.widget.form.ListField</code>.
 * 
 * @author scheglov_ke
 */
public class ListFieldTest extends GxtModelTest {
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
  /**
   * In general <code>ListField</code> requires <code>ListStore</code>, but as separate invocation,
   * not as argument of constructor. So, it is possible that it is missing and we should ensure it
   * to avoid parsing exception at design time.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48001
   */
  public void test_parseNoListStore() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    ListField field = new ListField();",
            "    add(field);",
            "  }",
            "}");
    refresh();
    // no exceptions
    assertNoErrors(container);
  }
}