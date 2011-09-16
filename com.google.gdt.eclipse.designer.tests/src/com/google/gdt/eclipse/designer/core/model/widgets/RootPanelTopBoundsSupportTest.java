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
import com.google.gdt.eclipse.designer.model.widgets.RootPanelTopBoundsSupport;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.graphics.Image;

/**
 * Test for {@link RootPanelTopBoundsSupport}.
 * 
 * @author scheglov_ke
 */
public class RootPanelTopBoundsSupportTest extends GwtModelTest {
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
  public void test_sizeFromResource() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // default size
    {
      Rectangle bounds = frame.getBounds();
      assertEquals(new Rectangle(0, 0, 450, 300), bounds);
    }
    // set new size
    {
      frame.getTopBoundsSupport().setSize(400, 200);
      frame.refresh();
      //
      Rectangle bounds = frame.getBounds();
      assertEquals(400, bounds.width);
      assertEquals(200, bounds.height);
    }
  }

  public void test_setPixelSize() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    rootPanel.setPixelSize(800, 600);",
            "  }",
            "}");
    frame.refresh();
    assertEquals(800, frame.getBounds().width);
    assertEquals(600, frame.getBounds().height);
  }

  /**
   * Test that zero size does not cause exception.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47814
   */
  public void test_zeroSize() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    refresh();
    assertNoErrors(frame);
    // initially 450x300
    {
      Image image = frame.getImage();
      assertEquals(450, image.getBounds().width);
      assertEquals(300, image.getBounds().height);
    }
    // set -500x-500 size, no exception
    {
      frame.getTopBoundsSupport().setSize(-500, -500);
      refresh();
      assertNoErrors(frame);
    }
  }
}