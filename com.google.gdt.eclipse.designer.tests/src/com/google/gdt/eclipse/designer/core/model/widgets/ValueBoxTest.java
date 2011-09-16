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
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.apache.commons.lang.ArrayUtils;

/**
 * Test <code>com.google.gwt.user.client.ui.ValueBox</code>.
 * 
 * @author sablin_aa
 */
public class ValueBoxTest extends GwtModelTest {
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
   * Test creation widgets based on <code>com.google.gwt.user.client.ui.ValueBox</code>.
   */
  private void test_CREATE(String widgetClass) throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // create
    WidgetInfo widget = createJavaInfo(widgetClass);
    frame.command_CREATE2(widget, null);
    // check
    assertNoErrors(widget);
    {
      // check description 
      byte[] vbIconData = getValueBoxDescription().getIcon().getImageData().data;
      byte[] widgetIconData = widget.getDescription().getIcon().getImageData().data;
      assertFalse(ArrayUtils.isEquals(vbIconData, widgetIconData));
    }
    //
    String shortClass = CodeUtils.getShortClass(widgetClass);
    String uniqueName = NamesManager.getDefaultName(widgetClass);
    assertEditor(
    //"import " + widgetClass + ";",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      " + shortClass + " " + uniqueName + " = new " + shortClass + "();",
        "      rootPanel.add(" + uniqueName + ");",
        "    }",
        "  }",
        "}");
  }

  private ComponentDescription getValueBoxDescription() throws Exception {
    return ComponentDescriptionHelper.getDescription(
        m_lastEditor,
        "com.google.gwt.user.client.ui.ValueBox");
  }

  public void test_CREATE_IntegerBox() throws Exception {
    test_CREATE("com.google.gwt.user.client.ui.IntegerBox");
  }

  public void test_CREATE_LongBox() throws Exception {
    test_CREATE("com.google.gwt.user.client.ui.LongBox");
  }

  public void test_CREATE_DoubleBox() throws Exception {
    test_CREATE("com.google.gwt.user.client.ui.DoubleBox");
  }
}