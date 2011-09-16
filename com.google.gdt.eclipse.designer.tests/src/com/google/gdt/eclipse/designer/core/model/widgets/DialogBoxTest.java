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
 * Test {@link DialogBoxInfo}.
 * 
 * @author scheglov_ke
 */
public class DialogBoxTest extends GwtModelTest {
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
            "public class Test extends DialogBox {",
            "  public Test() {",
            "    setText('DialogBox');",
            "    {",
            "      Button button = new Button();",
            "      setWidget(button);",
            "      button.setPixelSize(300, 200);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.DialogBox} {this} {/setText('DialogBox')/ /setWidget(button)/}",
        "  {new: com.google.gwt.user.client.ui.Button} {local-unique: button} {/new Button()/ /setWidget(button)/ /button.setPixelSize(300, 200)/}");
    WidgetInfo button = panel.getWidget();
    // do refresh()
    panel.refresh();
    assertFalse(panel.isEmpty());
    // check DialogBox bounds
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.x).isEqualTo(0);
      assertThat(bounds.y).isEqualTo(0);
      assertThat(bounds.width).isEqualTo(300);
      assertThat(bounds.height).isGreaterThan(200 + 20);
    }
    {
      Image image = panel.getImage();
      assertNotNull(image);
    }
    // check Button bounds
    {
      Rectangle bounds = button.getBounds();
      assertThat(bounds.x).isEqualTo(0);
      assertThat(bounds.y).isGreaterThan(20);
      assertThat(bounds.width).isEqualTo(300);
      assertThat(bounds.height).isEqualTo(200);
    }
  }

  public void test_empty() throws Exception {
    PopupPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DialogBox {",
            "  public Test() {",
            "    setText('DialogBox');",
            "  }",
            "}");
    // do refresh()
    panel.refresh();
    assertTrue(panel.isEmpty());
    assertNull(panel.getWidget());
    // check DialogBox bounds
    {
      Rectangle bounds = panel.getBounds();
      assertThat(bounds.x).isEqualTo(0);
      assertThat(bounds.y).isEqualTo(0);
      assertThat(bounds.width).isGreaterThan(110);
      assertThat(bounds.height).isGreaterThan(40);
    }
  }

  public void test_setSize() throws Exception {
    PopupPanelInfo panel =
        parseJavaInfo(
            "public class Test extends DialogBox {",
            "  public Test() {",
            "    setText('DialogBox');",
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
      assertThat(bounds.width).isEqualTo(300);
      assertThat(bounds.height).isEqualTo(200);
    }
    assertEditor(
        "public class Test extends DialogBox {",
        "  public Test() {",
        "    setText('DialogBox');",
        "    {",
        "      Button button = new Button();",
        "      setWidget(button);",
        "      button.setSize('300px', '178px');",
        "    }",
        "  }",
        "}");
  }
}