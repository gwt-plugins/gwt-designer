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
package com.google.gdt.eclipse.designer.core.model.property.accessor;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.property.accessor.CellExpressionAccessor;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Test for {@link CellExpressionAccessor}.
 * 
 * @author scheglov_ke
 */
public class CellExpressionAccessorTest extends GwtModelTest {
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
  public void test_ALL() throws Exception {
    setFileContentSrc(
        "test.client/MyPanel.java",
        getTestSource(
            "public class MyPanel extends AbsolutePanel {",
            "  public void setFoo(Widget widget, int value) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <method-order>",
            "    <method signature='setFoo(com.google.gwt.user.client.ui.Widget,int)' order='afterAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    MyPanel panel = new MyPanel();",
            "    rootPanel.add(panel);",
            "    {",
            "      Button button = new Button();",
            "      button.setEnabled(true);",
            "      panel.add(button);",
            "      button.setEnabled(true);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ComplexPanelInfo panel = (ComplexPanelInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    // prepare accessor
    ExpressionAccessor accessor = new CellExpressionAccessor(panel, "setFoo", "int");
    // initially no Expression
    assertNull(accessor.getExpression(button));
    // try to remove Expression, no changes
    {
      String expectedSource = m_lastEditor.getSource();
      boolean success = accessor.setExpression(button, null);
      assertFalse(success);
      assertEditor(expectedSource, m_lastEditor);
    }
    // add new Expression
    {
      boolean success = accessor.setExpression(button, "111");
      assertTrue(success);
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    MyPanel panel = new MyPanel();",
          "    rootPanel.add(panel);",
          "    {",
          "      Button button = new Button();",
          "      button.setEnabled(true);",
          "      panel.add(button);",
          "      panel.setFoo(button, 111);",
          "      button.setEnabled(true);",
          "    }",
          "  }",
          "}");
    }
    // now we have Expression
    {
      Expression expression = accessor.getExpression(button);
      assertNotNull(expression);
      assertEquals("111", m_lastEditor.getSource(expression));
    }
    // update Expression
    {
      boolean hasChanges = accessor.setExpression(button, "222");
      assertTrue(hasChanges);
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    MyPanel panel = new MyPanel();",
          "    rootPanel.add(panel);",
          "    {",
          "      Button button = new Button();",
          "      button.setEnabled(true);",
          "      panel.add(button);",
          "      panel.setFoo(button, 222);",
          "      button.setEnabled(true);",
          "    }",
          "  }",
          "}");
    }
    // set same Expression, no changes
    {
      String expectedSource = m_lastEditor.getSource();
      boolean success = accessor.setExpression(button, "222");
      assertTrue(success);
      assertEditor(expectedSource, m_lastEditor);
    }
    // remove Expression
    {
      boolean success = accessor.setExpression(button, null);
      assertTrue(success);
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    MyPanel panel = new MyPanel();",
          "    rootPanel.add(panel);",
          "    {",
          "      Button button = new Button();",
          "      button.setEnabled(true);",
          "      panel.add(button);",
          "      button.setEnabled(true);",
          "    }",
          "  }",
          "}");
    }
    // again no Expression
    assertNull(accessor.getExpression(button));
  }
}