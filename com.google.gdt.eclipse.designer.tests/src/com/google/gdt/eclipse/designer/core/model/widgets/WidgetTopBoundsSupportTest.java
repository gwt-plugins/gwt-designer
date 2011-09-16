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
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetTopBoundsSupport;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.graphics.Image;

/**
 * Test {@link WidgetTopBoundsSupport}.
 * 
 * @author scheglov_ke
 */
public class WidgetTopBoundsSupportTest extends GwtModelTest {
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
  public void test_noSizeInvocations() throws Exception {
    WidgetInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends HorizontalPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // initial size
    {
      Rectangle bounds = panel.getBounds();
      assertEquals(450, bounds.width);
      assertEquals(300, bounds.height);
    }
    // set bounds
    int newWidth = 500;
    int newHeight = 350;
    panel.getTopBoundsSupport().setSize(newWidth, newHeight);
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends HorizontalPanel {",
        "  public Test() {",
        "  }",
        "}");
    // check that size applied
    panel.refresh();
    {
      Rectangle bounds = panel.getBounds();
      assertEquals(newWidth, bounds.width);
      assertEquals(newHeight, bounds.height);
    }
  }

  public void test_sizeInvocation_setSize_strings() throws Exception {
    WidgetInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends HorizontalPanel {",
            "  public Test() {",
            "    setSize('300px', '200px');",
            "  }",
            "}");
    panel.refresh();
    // initial size
    {
      Rectangle bounds = panel.getBounds();
      assertEquals(300, bounds.width);
      assertEquals(200, bounds.height);
    }
    // set bounds
    int newWidth = 500;
    int newHeight = 350;
    panel.getTopBoundsSupport().setSize(newWidth, newHeight);
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends HorizontalPanel {",
        "  public Test() {",
        "    setSize('500px', '350px');",
        "  }",
        "}");
    // check that size applied
    panel.refresh();
    {
      Rectangle bounds = panel.getBounds();
      assertEquals(newWidth, bounds.width);
      assertEquals(newHeight, bounds.height);
    }
  }

  public void test_sizeInvocation_setWidth_setHeight_string() throws Exception {
    WidgetInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends HorizontalPanel {",
            "  public Test() {",
            "    setWidth('300px');",
            "    setHeight('200px');",
            "  }",
            "}");
    panel.refresh();
    // initial size
    {
      Rectangle bounds = panel.getBounds();
      assertEquals(300, bounds.width);
      assertEquals(200, bounds.height);
    }
    // set bounds
    int newWidth = 500;
    int newHeight = 350;
    panel.getTopBoundsSupport().setSize(newWidth, newHeight);
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends HorizontalPanel {",
        "  public Test() {",
        "    setWidth('500px');",
        "    setHeight('350px');",
        "  }",
        "}");
    // check that size applied
    panel.refresh();
    {
      Rectangle bounds = panel.getBounds();
      assertEquals(newWidth, bounds.width);
      assertEquals(newHeight, bounds.height);
    }
  }

  /**
   * Test that zero size does not cause exception.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47814
   */
  public void test_zeroSize() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "  }",
            "}");
    refresh();
    assertNoErrors(panel);
    // initially 450x300
    {
      Image image = panel.getImage();
      assertEquals(450, image.getBounds().width);
      assertEquals(300, image.getBounds().height);
    }
    // set -100x-100 size, no exception
    {
      panel.getTopBoundsSupport().setSize(-100, -100);
      refresh();
      // however 1x1 image generated
      Image image = panel.getImage();
      assertEquals(1, image.getBounds().width);
      assertEquals(1, image.getBounds().height);
    }
  }

  /**
   * There was decision to check that <code>Widget</code> is attached to <code>RootPanel</code> in
   * "applyTopBoundsScript" script. However we call this script only in there are no
   * <code>setSize()</code> invocations. So, widget with such invocations left unattached and not
   * visible.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47903
   */
  public void test_notThisWidget_hasSize() throws Exception {
    parseJavaInfo(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  /** @wbp.parser.entryPoint */",
        "  public void entryMethod() {",
        "    FlowPanel flowPanel = new FlowPanel();",
        "    flowPanel.setSize('500px', '200px');",
        "  }",
        "}");
    refresh();
    ComplexPanelInfo flowPanel = getJavaInfoByName("flowPanel");
    // has size 500x200
    {
      Image image = flowPanel.getImage();
      assertEquals(500, image.getBounds().width);
      assertEquals(200, image.getBounds().height);
    }
  }
}