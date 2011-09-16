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
import com.google.gdt.eclipse.designer.model.widgets.NumberLabelInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;

import org.eclipse.swt.graphics.Image;

import java.math.BigInteger;

/**
 * Test for <code>com.google.gwt.user.client.ui.NumberLabel</code>.
 * 
 * @author scheglov_ke
 */
public class NumberLabelTest extends GwtModelTest {
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
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_presentationIcon_Integer() throws Exception {
    check_presentationIcon("java.lang.Integer", "Integer", "Integer");
  }

  public void test_presentationIcon_Long() throws Exception {
    check_presentationIcon("java.lang.Long", "Long", "Long");
  }

  public void test_presentationIcon_Double() throws Exception {
    check_presentationIcon("java.lang.Double", "Double", "Double");
  }

  /**
   * No special icon for {@link BigInteger}.
   */
  public void test_presentationIcon_BigInteger() throws Exception {
    check_presentationIcon("java.math.BigInteger", "BigInteger", null);
  }

  /**
   * Test that <code>NumberLabel</code> provides text with type name, and also special icon for
   * known and often used types.
   */
  private void check_presentationIcon(String typeName, String textTypeName, String creationId)
      throws Exception {
    parseJavaInfo(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    NumberLabel<" + typeName + "> numberLabel = new NumberLabel<" + typeName + ">();",
        "    add(numberLabel);",
        "  }",
        "}");
    refresh();
    NumberLabelInfo numberLabel = getJavaInfoByName("numberLabel");
    // has special name and icon
    assertEquals("NumberLabel<" + textTypeName + ">", getValueLabelText(numberLabel));
    {
      Image icon = numberLabel.getPresentation().getIcon();
      // "creation" specific icon
      if (creationId != null) {
        assertSame(numberLabel.getDescription().getCreation(creationId).getIcon(), icon);
        assertNotSame(numberLabel.getDescription().getIcon(), icon);
      } else {
        assertSame(numberLabel.getDescription().getIcon(), icon);
      }
    }
  }

  /**
   * @return the inner text of given <code>ValueLabel</code> element.
   */
  private static String getValueLabelText(WidgetInfo valueLabelModel) throws Exception {
    Object element = valueLabelModel.getElement();
    return valueLabelModel.getDOMUtils().getInnerText(element);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Create
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Use predefined creation for "Integer" type.
   */
  public void test_createInteger() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "  }",
            "}");
    // do create
    {
      NumberLabelInfo numberLabel =
          createJavaInfo("com.google.gwt.user.client.ui.NumberLabel", "Integer");
      flowContainer_CREATE(panel, numberLabel, null);
    }
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      NumberLabel<Integer> numberLabel = new NumberLabel<Integer>();",
        "      add(numberLabel);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Use default creation, with "type" type argument.
   */
  public void test_createGeneric() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "  }",
            "}");
    // do create
    {
      NumberLabelInfo numberLabel = createJavaInfo("com.google.gwt.user.client.ui.NumberLabel");
      numberLabel.putTemplateArgument("type", "java.math.BigInteger");
      flowContainer_CREATE(panel, numberLabel, null);
    }
    assertEditor(
        "import java.math.BigInteger;",
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      NumberLabel<BigInteger> numberLabel = new NumberLabel<BigInteger>();",
        "      add(numberLabel);",
        "    }",
        "  }",
        "}");
  }
}