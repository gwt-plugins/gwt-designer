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
import com.google.gdt.eclipse.designer.model.widgets.panels.PopupPanelInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.graphics.Image;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test {@link PopupPanelInfo}.
 * 
 * @author scheglov_ke
 */
public class PopupPanelTest extends GwtModelTest {
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
  public void test_filled() throws Exception {
    PopupPanelInfo panel =
        parseJavaInfo(
            "public class Test extends PopupPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      setWidget(button);",
            "      button.setPixelSize(450, 300);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.PopupPanel} {this} {/setWidget(button)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /setWidget(button)/ /button.setPixelSize(450, 300)/}");
    WidgetInfo button = panel.getWidget();
    // do refresh()
    panel.refresh();
    assertFalse(panel.isEmpty());
    // check PopupPanel bounds
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.x).isEqualTo(0);
      assertThat(bounds.y).isEqualTo(0);
      assertThat(bounds.width).isEqualTo(450);
      assertThat(bounds.height).isEqualTo(300);
    }
    {
      Image image = panel.getImage();
      assertNotNull(image);
      assertThat(image.getBounds().width).isEqualTo(450);
      assertThat(image.getBounds().height).isEqualTo(300);
    }
    // check Button bounds
    {
      Rectangle bounds = button.getBounds();
      assertEquals(new Rectangle(0, 0, 450, 300), bounds);
    }
  }

  public void test_empty() throws Exception {
    PopupPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends PopupPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy("{this: com.google.gwt.user.client.ui.PopupPanel} {this} {}");
    // do refresh()
    panel.refresh();
    assertTrue(panel.isEmpty());
    assertNull(panel.getWidget());
  }

  public void test_setSize() throws Exception {
    PopupPanelInfo panel =
        parseJavaInfo(
            "public class Test extends PopupPanel {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      setWidget(button);",
            "      button.setPixelSize(150, 100);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // check initial size
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.width).isGreaterThanOrEqualTo(150);
      assertThat(bounds.height).isGreaterThanOrEqualTo(100);
    }
    // set new size
    panel.getTopBoundsSupport().setSize(300, 200);
    panel.refresh();
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.width).isGreaterThanOrEqualTo(300);
      assertThat(bounds.height).isGreaterThanOrEqualTo(200);
    }
    assertEditor(
        "public class Test extends PopupPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      setWidget(button);",
        "      button.setSize('300px', '200px');",
        "    }",
        "  }",
        "}");
  }
}