/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gdt.eclipse.designer.gxt.model.property;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;

import org.eclipse.wb.core.controls.CSpinner;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

import java.awt.Insets;

/**
 * Test for {@link MarginsPropertyEditor}.
 * 
 * @author scheglov_ke
 */
public class MarginsPropertyEditorTest extends GxtModelTest {
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
  // Converter
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MarginsConverter}.
   */
  public void test_Converter() throws Exception {
    ExpressionConverter converter = MarginsConverter.INSTANCE;
    {
      String expected = "(com.extjs.gxt.ui.client.util.Margins) null";
      String actual = converter.toJavaSource(null, null);
      assertEquals(expected, actual);
    }
    {
      String expected = "new com.extjs.gxt.ui.client.util.Margins(1, 2, 3, 4)";
      String actual = converter.toJavaSource(null, new Insets(1, 4, 3, 2));
      assertEquals(expected, actual);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_0() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "    FlowLayout flowLayout = new FlowLayout();",
            "    setLayout(flowLayout);",
            "  }",
            "}");
    container.refresh();
    final Property property = container.getLayout().getPropertyByTitle("margins");
    // initially no margins
    assertFalse(property.isModified());
    assertEquals(null, getPropertyText(property));
    // set value
    property.setValue(new Insets(1, 4, 3, 2));
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    FlowLayout flowLayout = new FlowLayout();",
        "    flowLayout.setMargins(new Margins(1, 2, 3, 4));",
        "    setLayout(flowLayout);",
        "  }",
        "}");
    assertTrue(property.isModified());
    assertEquals("(1, 2, 3, 4)", getPropertyText(property));
    // remove value using editor
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("margins");
        // check existing values
        assertEquals(1, getSpinnerValue(context, "Top:"));
        assertEquals(2, getSpinnerValue(context, "Right:"));
        assertEquals(3, getSpinnerValue(context, "Bottom:"));
        assertEquals(4, getSpinnerValue(context, "Left:"));
        // reset to default
        context.clickButton("Default");
      }

      private int getSpinnerValue(UiContext context, String text) {
        Control control = context.getControlAfterLabel(text);
        CSpinner spinner = (CSpinner) control;
        return spinner.getSelection();
      }
    });
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    FlowLayout flowLayout = new FlowLayout();",
        "    setLayout(flowLayout);",
        "  }",
        "}");
    assertFalse(property.isModified());
    assertEquals(null, getPropertyText(property));
    // set value using editor
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("margins");
        // check existing values
        setSpinnerValue(context, "Top:", 10);
        setSpinnerValue(context, "Right:", 20);
        setSpinnerValue(context, "Bottom:", 30);
        setSpinnerValue(context, "Left:", 40);
        // reset to default
        context.clickButton("OK");
      }

      private void setSpinnerValue(UiContext context, String text, int value) {
        CSpinner spinner = getSpinner(context, text);
        spinner.setSelection(value);
        spinner.notifyListeners(SWT.Selection, null);
      }

      private CSpinner getSpinner(UiContext context, String text) {
        return (CSpinner) context.getControlAfterLabel(text);
      }
    });
    assertEditor(
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    FlowLayout flowLayout = new FlowLayout();",
        "    flowLayout.setMargins(new Margins(10, 20, 30, 40));",
        "    setLayout(flowLayout);",
        "  }",
        "}");
    assertTrue(property.isModified());
    assertEquals("(10, 20, 30, 40)", getPropertyText(property));
  }
}