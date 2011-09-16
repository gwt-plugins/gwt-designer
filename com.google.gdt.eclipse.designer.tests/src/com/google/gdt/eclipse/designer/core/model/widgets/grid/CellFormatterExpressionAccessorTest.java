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
package com.google.gdt.eclipse.designer.core.model.widgets.grid;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.CellFormatterExpressionAccessor;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;

import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Test for {@link CellFormatterExpressionAccessor}.
 * 
 * @author scheglov_ke
 */
public class CellFormatterExpressionAccessorTest extends GwtModelTest {
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
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    FlexTable panel = new FlexTable();",
            "    rootPanel.add(panel);",
            "    panel.setWidget(0, 0, new Button());",
            "  }",
            "}");
    frame.refresh();
    HTMLTableInfo panel = (HTMLTableInfo) frame.getChildrenWidgets().get(0);
    WidgetInfo button = panel.getChildrenWidgets().get(0);
    // prepare accessor
    ExpressionAccessor accessor = new CellFormatterExpressionAccessor("setVisible", "boolean");
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
      boolean success = accessor.setExpression(button, "true");
      assertTrue(success);
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    FlexTable panel = new FlexTable();",
          "    rootPanel.add(panel);",
          "    panel.setWidget(0, 0, new Button());",
          "    panel.getCellFormatter().setVisible(0, 0, true);",
          "  }",
          "}");
    }
    // now we have Expression
    {
      Expression expression = accessor.getExpression(button);
      assertNotNull(expression);
      assertEquals("true", m_lastEditor.getSource(expression));
    }
    // update Expression
    {
      boolean hasChanges = accessor.setExpression(button, "false");
      assertTrue(hasChanges);
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    FlexTable panel = new FlexTable();",
          "    rootPanel.add(panel);",
          "    panel.setWidget(0, 0, new Button());",
          "    panel.getCellFormatter().setVisible(0, 0, false);",
          "  }",
          "}");
    }
    // set same Expression, no changes
    {
      String expectedSource = m_lastEditor.getSource();
      boolean success = accessor.setExpression(button, "false");
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
          "    FlexTable panel = new FlexTable();",
          "    rootPanel.add(panel);",
          "    panel.setWidget(0, 0, new Button());",
          "  }",
          "}");
    }
    // again no Expression
    assertNull(accessor.getExpression(button));
  }
}