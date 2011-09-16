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

import com.google.gdt.eclipse.designer.gxt.model.GxtGefTest;

/**
 * Test for {@link ContentPanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class ContentPanelGefTest extends GxtGefTest {
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
   * If <code>ContentPanel</code> is placeholder, this may cause problems because we call many it
   * methods.
   */
  public void test_ContentPanel_whenPlaceholder() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "public class MyPanel extends ContentPanel {",
            "  public MyPanel() {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse, no exceptions expected
    openLayoutContainer(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    MyPanel panel = new MyPanel();",
        "    add(panel);",
        "  }",
        "}");
  }
}