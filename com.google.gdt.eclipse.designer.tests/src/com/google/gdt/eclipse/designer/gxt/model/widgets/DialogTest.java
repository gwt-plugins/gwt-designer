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
import com.google.gdt.eclipse.designer.gxt.model.widgets.DialogInfo.DialogButton_Info;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.editor.DesignPageSite;

import static org.fest.assertions.Assertions.assertThat;

import org.easymock.Capture;
import org.easymock.EasyMock;

import java.util.List;

/**
 * Test for {@link DialogInfo}.
 * 
 * @author scheglov_ke
 */
public class DialogTest extends GxtModelTest {
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
    DialogInfo dialog =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Dialog {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.Dialog} {this} {}",
        "  {implicit-layout: default} {implicit-layout} {}");
    dialog.refresh();
    assertNoErrors(dialog);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DialogInfo#getDialogButtons()}.
   */
  public void test_getDialogButtons_OK() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Dialog {",
            "  public Test() {",
            "  }",
            "}");
    dialog.refresh();
    //
    List<DialogButton_Info> buttons = dialog.getDialogButtons();
    assertThat(buttons).hasSize(1);
    {
      DialogButton_Info button = buttons.get(0);
      assertEquals("OK", button.getId().getName());
      // object
      button.hashCode();
      assertTrue(button.equals(button));
      assertFalse(button.equals(this));
      // bounds
      Rectangle bounds = button.getBounds();
      assertThat(bounds.x).isGreaterThan(300).isLessThan(400);
      assertThat(bounds.y).isGreaterThan(200).isLessThan(300);
      assertThat(bounds.width).isEqualTo(75);
      assertThat(bounds.height).isGreaterThan(20);
    }
  }

  /**
   * Test for {@link DialogInfo#getDialogButtons()}.
   */
  public void test_getDialogButtons_OK_CANCEL() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "public class Test extends Dialog {",
            "  public Test() {",
            "    setButtons(Dialog.OKCANCEL);",
            "  }",
            "}");
    dialog.refresh();
    //
    List<DialogButton_Info> buttons = dialog.getDialogButtons();
    assertThat(buttons).hasSize(2);
    {
      DialogButton_Info button = buttons.get(0);
      assertEquals("OK", button.getId().getName());
    }
    {
      DialogButton_Info button = buttons.get(1);
      assertEquals("CANCEL", button.getId().getName());
    }
    assertFalse(buttons.get(0).equals(buttons.get(1)));
  }

  /**
   * Test for {@link DialogButton_Info#open()}.
   */
  public void test_DialogButton_open() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends Dialog {",
            "  public Test() {",
            "  }",
            "}");
    dialog.refresh();
    DialogButton_Info button = dialog.getDialogButtons().get(0);
    // set mock for DesignPageSite
    IDesignPageSite pageSite;
    Capture<Integer> openSourcePosition = new Capture<Integer>();
    {
      pageSite = EasyMock.createStrictMock(IDesignPageSite.class);
      DesignPageSite.Helper.setSite(dialog, pageSite);
      pageSite.openSourcePosition(org.easymock.EasyMock.capture(openSourcePosition));
      EasyMock.replay(pageSite);
    }
    // open
    button.open();
    waitEventLoop(0);
    assertEditor(
        "// filler filler filler",
        "public class Test extends Dialog {",
        "  public Test() {",
        "  }",
        "  protected void onButtonPressed(Button button) {",
        "    if (button == getButtonBar().getItemByItemId(OK)) {",
        "      // TODO",
        "    }",
        "    super.onButtonPressed(button);",
        "  }",
        "}");
    EasyMock.verify(pageSite);
    // check captured position
    {
      assertTrue(openSourcePosition.hasCaptured());
      int position = openSourcePosition.getValue();
      assertThat(position).isPositive();
      assertThat(m_lastEditor.getSource().substring(position)).startsWith(
          "if (button == getButtonBar().getItemByItemId(OK)) {");
    }
    // open it again, same position expected
    {
      String expectedsource = m_lastEditor.getSource();
      // reset mock
      {
        EasyMock.reset(pageSite);
        pageSite.openSourcePosition(openSourcePosition.getValue());
        EasyMock.replay(pageSite);
      }
      // open
      button.open();
      assertEquals(expectedsource, m_lastEditor.getSource());
    }
  }

  /**
   * Test for {@link DialogButton_Info#open()}.
   */
  public void test_DialogButton_open_otherButtons() throws Exception {
    DialogInfo dialog =
        parseJavaInfo(
            "public class Test extends Dialog {",
            "  public Test() {",
            "    setButtons(Dialog.OKCANCEL);",
            "  }",
            "  protected void onButtonPressed(Button button) {",
            "    if (button == getButtonBar().getItemByItemId(CANCEL)) {",
            "    }",
            "    super.onButtonPressed(button);",
            "  }",
            "}");
    dialog.refresh();
    DialogButton_Info button = dialog.getDialogButtons().get(0);
    // set mock for DesignPageSite
    IDesignPageSite pageSite;
    {
      pageSite = EasyMock.createStrictMock(IDesignPageSite.class);
      DesignPageSite.Helper.setSite(dialog, pageSite);
      pageSite.openSourcePosition(org.easymock.EasyMock.anyInt());
      EasyMock.replay(pageSite);
    }
    // open
    button.open();
    waitEventLoop(0);
    assertEditor(
        "public class Test extends Dialog {",
        "  public Test() {",
        "    setButtons(Dialog.OKCANCEL);",
        "  }",
        "  protected void onButtonPressed(Button button) {",
        "    if (button == getButtonBar().getItemByItemId(OK)) {",
        "      // TODO",
        "    }",
        "    if (button == getButtonBar().getItemByItemId(CANCEL)) {",
        "    }",
        "    super.onButtonPressed(button);",
        "  }",
        "}");
    EasyMock.verify(pageSite);
  }
}