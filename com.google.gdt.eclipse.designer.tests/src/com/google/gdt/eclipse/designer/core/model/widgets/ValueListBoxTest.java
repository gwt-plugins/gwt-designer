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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.CompositeInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Test support <code>com.google.gwt.user.client.ui.ValueListBox</code>.
 * 
 * @author sablin_aa
 */
public class ValueListBoxTest extends GwtModelTest {
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
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.google.gwt.text.client.IntegerRenderer;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      ValueListBox valueListBox = new ValueListBox(IntegerRenderer.instance());",
            "      rootPanel.add(valueListBox);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    CompositeInfo valueListBox = (CompositeInfo) frame.getChildrenWidgets().get(0);
    Class<?> valueListBoxClass = valueListBox.getDescription().getComponentClass();
    assertTrue(ReflectionUtils.isSuccessorOf(
        valueListBoxClass,
        "com.google.gwt.user.client.ui.ValueListBox"));
  }

  /**
   * Test creation child {@link Widjet_Info} for constructor parameter of CellList.
   */
  public void test_CREATE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // create
    CompositeInfo valuePicker = createJavaInfo("com.google.gwt.user.client.ui.ValueListBox");
    frame.command_CREATE2(valuePicker, null);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      ValueListBox valueListBox = new ValueListBox(IntegerRenderer.instance());",
        "      rootPanel.add(valueListBox);",
        "    }",
        "  }",
        "}");
  }
}