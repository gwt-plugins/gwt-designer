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

import org.eclipse.wb.draw2d.geometry.Dimension;

/**
 * Test for {@link ViewportInfo}.
 * 
 * @author scheglov_ke
 */
public class ViewportTest extends GxtModelTest {
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
   * In general <code>Viewport</code> has useful feature - automatically fill full window, when it
   * is resized. However this feature looks as bug for users - they expect that it will have
   * specified size, or at least will not resize after refresh.
   * <p>
   * So, here we test that <code>Viewport</code> uses specified size.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44462
   */
  public void test_useSpecifiedSize() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Viewport viewport = new Viewport();",
            "      rootPanel.add(viewport);",
            "      viewport.setSize(300, 200);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ViewportInfo viewport = getJavaInfoByName("viewport");
    //
    assertEquals(new Dimension(300, 200), viewport.getBounds().getSize());
  }
}