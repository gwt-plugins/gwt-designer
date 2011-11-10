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
package com.google.gdt.eclipse.designer.gwtext;

import com.google.gdt.eclipse.designer.gwtext.model.layout.FormLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.FieldInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.FormPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link FormPanelInfo}.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 */
public class FormPanelTest extends GwtExtModelTest {
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
    FormPanelInfo form =
        parseJavaInfo(
            "public class Test extends FormPanel {",
            "  public Test() {",
            "    {",
            "      TextField field_1 = new TextField();",
            "      add(field_1);",
            "    }",
            "  }",
            "}");
    form.refresh();
    assertNoErrors(form);
    //
    assertTrue(form.hasLayout());
    assertInstanceOf(FormLayoutInfo.class, form.getLayout());
  }

  /**
   * <code>FormPanel</code> requires at least one <code>Field</code>, we already ensure this in
   * source. However if user already has invalid source, we should handle this without exceptions.
   */
  public void test_parse_empty() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      FormPanel panel = new FormPanel();",
            "      rootPanel.add(panel);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    assertNoErrors(frame);
  }

  /**
   * Some methods can be invoked only when <code>Element</code> is not created yet. So, we should
   * ensure that <code>Element</code> creation does not happen too early.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?42815
   */
  public void test_parseTimeField() throws Exception {
    FormPanelInfo form =
        parseJavaInfo(
            "public class Test extends FormPanel {",
            "  public Test() {",
            "    {",
            "      TimeField field = new TimeField();",
            "      field.setIncrement(10);",
            "      add(field);",
            "    }",
            "  }",
            "}");
    assertNoErrors(form);
  }

  public void test_CREATE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // create FormPanel
    FormPanelInfo form = (FormPanelInfo) createWidget("com.gwtext.client.widgets.form.FormPanel");
    // add FormPanel on root panel
    frame.command_CREATE2(form, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      FormPanel formPanel = new FormPanel();",
        "      {",
        "        TextField textField = new TextField('New text field', 'text_field', 100);",
        "        formPanel.add(textField);",
        "      }",
        "      rootPanel.add(formPanel);",
        "    }",
        "  }",
        "}");
  }

  public void test_removeFields() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      FormPanel form = new FormPanel();",
            "      {",
            "        TextField field_1 = new TextField();",
            "        form.add(field_1);",
            "      }",
            "      {",
            "        TextField field_2 = new TextField();",
            "        form.add(field_2);",
            "      }",
            "      rootPanel.add(form);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // initial state
    FormPanelInfo form = frame.getChildren(FormPanelInfo.class).get(0);
    List<FieldInfo> fields = form.getFields();
    assertThat(fields).hasSize(2);
    FieldInfo field_1 = fields.get(0);
    FieldInfo field_2 = fields.get(1);
    // delete "field_1", "field_2" still alive
    {
      field_1.delete();
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootPanel rootPanel = RootPanel.get();",
          "    {",
          "      FormPanel form = new FormPanel();",
          "      {",
          "        TextField field_2 = new TextField();",
          "        form.add(field_2);",
          "      }",
          "      rootPanel.add(form);",
          "    }",
          "  }",
          "}");
    }
    // delete "field_2", some default Field should be added
    field_2.delete();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      FormPanel form = new FormPanel();",
        "      {",
        "        TextField textField = new TextField('New text field', 'text_field', 100);",
        "        form.add(textField);",
        "      }",
        "      rootPanel.add(form);",
        "    }",
        "  }",
        "}");
    // delete "form" itself
    form.delete();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "  }",
        "}");
  }
}