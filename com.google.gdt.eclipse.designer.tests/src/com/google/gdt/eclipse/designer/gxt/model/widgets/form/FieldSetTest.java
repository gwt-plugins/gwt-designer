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
package com.google.gdt.eclipse.designer.gxt.model.widgets.form;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;

/**
 * Test for {@link FieldSetInfo}.
 * 
 * @author scheglov_ke
 */
public class FieldSetTest extends GxtModelTest {
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
  // Client area insets
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_clientAreaInsets() throws Exception {
    FieldSetInfo fieldSet =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends FieldSet {",
            "  public Test() {",
            "  }",
            "}");
    fieldSet.refresh();
    // check insets
    Insets insets = fieldSet.getClientAreaInsets();
    assertEquals(new Insets(10, 11, 12, 11), insets);
  }

  /**
   * Check that "model" bounds of <code>Button</code> on <code>FieldSet</code> is valid, calculated
   * with applying "client area insets" of <code>FieldSet</code>.
   */
  public void test_clientAreaInsets_appliedToModelBounds_ofChildren() throws Exception {
    FieldSetInfo fieldSet =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends FieldSet {",
            "  public Test() {",
            "    {",
            "      Button button = new Button();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    fieldSet.refresh();
    ComponentInfo button = getJavaInfoByName("button");
    //
    Insets insets = fieldSet.getClientAreaInsets();
    Rectangle buttonParentBounds = button.getBounds();
    Rectangle buttonModelBounds = button.getModelBounds();
    assertEquals(buttonParentBounds.x, buttonModelBounds.x + insets.left);
    assertEquals(buttonParentBounds.y, buttonModelBounds.y + insets.top);
  }

  public void test_clientAreaInsets_withHeading() throws Exception {
    FieldSetInfo fieldSet =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends FieldSet {",
            "  public Test() {",
            "    setHeading('New FieldSet');",
            "  }",
            "}");
    fieldSet.refresh();
    // check insets
    Insets insets = fieldSet.getClientAreaInsets();
    assertEquals(new Insets(24, 11, 12, 11), insets);
  }

  public void test_clientAreaInsets_withHeadingCollapsible() throws Exception {
    FieldSetInfo fieldSet =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends FieldSet {",
            "  public Test() {",
            "    setHeading('New FieldSet');",
            "    setCollapsible(true);",
            "  }",
            "}");
    fieldSet.refresh();
    // check insets
    Insets insets = fieldSet.getClientAreaInsets();
    assertEquals(new Insets(26, 11, 12, 11), insets);
  }
}