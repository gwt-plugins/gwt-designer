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

import com.google.gdt.eclipse.designer.gwtext.model.widgets.EditorInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.FieldInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link EditorInfo}.
 * 
 * @author sablin_aa
 */
public class EditorTest extends GwtExtModelTest {
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
  public void test_liveImage() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    {
      // create Editor
      WidgetInfo button = createWidget("com.gwtext.client.widgets.Editor");
      assertThat(button).isNotNull();
      assertThat(button.getImage()).isNotNull();
    }
  }

  public void test_create() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // create Editor
    EditorInfo editor = (EditorInfo) createWidget("com.gwtext.client.widgets.Editor");
    // add Editor on root panel
    frame.command_CREATE2(editor, null);
    assertEditor(
        "import com.gwtext.client.widgets.Editor;",
        "import com.gwtext.client.widgets.form.TextField;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Editor editor = new Editor();",
        "      {",
        "        TextField textField = new TextField('New text field', 'text_field', 150);",
        "        editor.setField(textField);",
        "      }",
        "      rootPanel.add(editor);",
        "    }",
        "  }",
        "}");
    // Editor already contains one Field (TextField by default)
    List<FieldInfo> children = editor.getChildren(FieldInfo.class);
    assertThat(children).hasSize(1);
    assertThat(children.get(0).getDescription().getComponentClass().getCanonicalName()).isEqualTo(
        "com.gwtext.client.widgets.form.TextField");
  }

  public void test_remove_field() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.Editor;",
            "import com.gwtext.client.widgets.form.TextField;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Editor editor = new Editor();",
            "      {",
            "        TextField text_field = new TextField('New text field', 'Text_Field', 150);",
            "        editor.setField(text_field);",
            "      }",
            "      rootPanel.add(editor);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // initial state
    EditorInfo editor = frame.getChildren(EditorInfo.class).get(0);
    assertThat(editor.getChildren(FieldInfo.class)).hasSize(1);
    // remove field
    FieldInfo field = editor.getChildren(FieldInfo.class).get(0);
    field.delete();
    frame.refresh();
    // in background default field was set to Editor
    assertEditor(
        "import com.gwtext.client.widgets.Editor;",
        "import com.gwtext.client.widgets.form.TextField;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      Editor editor = new Editor();",
        "      {",
        "        TextField textField = new TextField('New text field', 'text_field', 150);",
        "        editor.setField(textField);",
        "      }",
        "      rootPanel.add(editor);",
        "    }",
        "  }",
        "}");
    // 
    List<FieldInfo> children = editor.getChildren(FieldInfo.class);
    assertThat(children).hasSize(1);
    assertThat(children.get(0).getDescription().getComponentClass().getCanonicalName()).isEqualTo(
        "com.gwtext.client.widgets.form.TextField");
  }

  public void test_change_field() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.gwtext.client.widgets.Editor;",
            "import com.gwtext.client.widgets.form.TextField;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Editor editor = new Editor();",
            "      {",
            "        TextField text_field = new TextField('Text field');",
            "        editor.setField(text_field);",
            "      }",
            "      rootPanel.add(editor);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // initial state
    EditorInfo editor = frame.getChildren(EditorInfo.class).get(0);
    assertThat(editor.getChildren(FieldInfo.class)).hasSize(1);
    // change field
    // TODO
    assertThat(true).isFalse();
  }
}