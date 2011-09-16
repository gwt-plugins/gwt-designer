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
package com.google.gdt.eclipse.designer.core.model.property;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.property.AlignStringPropertyEditor;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link AlignStringPropertyEditor}.
 * 
 * @author sablin_aa
 */
public class AlignStringPropertyEditorTest extends GwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dontUseSharedGWTState();
  }

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
  public void test_noValue() throws Exception {
    configureProject();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyButton button = new MyButton();",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    //
    Property alignProperty = button.getPropertyByTitle("align");
    assertFalse(alignProperty.isModified());
    assertNull(getPropertyText(alignProperty));
    assertThat(alignProperty.getEditor()).isInstanceOf(AlignStringPropertyEditor.class);
  }

  public void test_value() throws Exception {
    configureProject();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      MyButton button = new MyButton();",
            "      button.setAlign('tl?-bl');",
            "      rootPanel.add(button);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    //
    Property alignProperty = button.getPropertyByTitle("align");
    assertTrue(alignProperty.isModified());
    assertThat(getPropertyText(alignProperty)).isEqualTo("tl?-bl");
  }

  public void test_dialog() throws Exception {
    // create dialog
    final AlignStringPropertyEditor.AlignDialog dialog =
        new AlignStringPropertyEditor.AlignDialog(new Shell());
    dialog.setValue("tl?-br");
    // check state
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        dialog.open();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Align chooser");
        // check element anchor
        assertGroup(
            (Group) ReflectionUtils.getFieldObject(dialog, "m_elementAnchorGroup"),
            "tl",
            true);
        // check target anchor
        assertGroup(
            (Group) ReflectionUtils.getFieldObject(dialog, "m_targetAnchorGroup"),
            "br",
            false);
        // close dialog
        context.clickButton("OK"); // dialog.close();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void configureProject() throws Exception {
    setFileContentSrc(
        "test/client/MyButton.java",
        getTestSource(
            "public class MyButton extends Button {",
            "  public void setAlign(String align) {",
            "  }",
            "}"));
    setFileContentSrc("test/client/MyButton.wbp-component.xml", DesignerTestCase.getSourceDQ(
        "<?xml version='1.0' encoding='UTF-8'?>",
        "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
        "  <property id='setAlign(java.lang.String)'>",
        "    <editor id='gwt.alignString'/>",
        "  </property>",
        "</component>"));
    waitForAutoBuild();
  }

  private static void assertGroup(Group stateGroup, String align, boolean adjusted) {
    Control[] children = stateGroup.getChildren();
    for (Control control : children) {
      if (control instanceof Button) {
        Button button = (Button) control;
        if ((button.getStyle() & SWT.RADIO) != 0) {
          if (align.equals(button.getText())) {
            assertThat(button.getSelection()).isTrue();
          } else {
            assertThat(button.getSelection()).isFalse();
          }
        }
        if ((button.getStyle() & SWT.CHECK) != 0) {
          assertThat(button.getSelection()).isEqualTo(adjusted);
        }
      }
    }
  }
}