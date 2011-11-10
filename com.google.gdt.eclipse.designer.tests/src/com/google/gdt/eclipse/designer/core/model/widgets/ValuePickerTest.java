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
import com.google.gdt.eclipse.designer.model.widgets.ValuePickerInfo;
import com.google.gdt.eclipse.designer.model.widgets.cell.CellListInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Test {@link ValuePickerInfo}.
 * 
 * @author sablin_aa
 */
public class ValuePickerTest extends GwtModelTest {
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
            "import com.google.gwt.user.cellview.client.CellList;",
            "import com.google.gwt.cell.client.TextCell;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      ValuePicker<String> valuePicker = new ValuePicker<String>(new CellList<String>(new TextCell()));",
            "      rootPanel.add(valuePicker);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ValuePickerInfo valuePicker = (ValuePickerInfo) frame.getChildrenWidgets().get(0);
    CellListInfo cellList = valuePicker.getCellList();
    Class<?> cellListClass = cellList.getDescription().getComponentClass();
    assertTrue(ReflectionUtils.isSuccessorOf(
        cellListClass,
        "com.google.gwt.user.cellview.client.CellList"));
    // CellList should not be visible on design canvas, because we want that user click and move ValuePicker
    assertVisibleInGraphical(cellList, false);
    assertVisibleInTree(cellList, true);
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
    ValuePickerInfo valuePicker = createJavaInfo("com.google.gwt.user.client.ui.ValuePicker");
    frame.command_CREATE2(valuePicker, null);
    // check
    assertNoErrors(valuePicker);
    {
      CellListInfo cellList = valuePicker.getCellList();
      Class<?> cellListClass = cellList.getDescription().getComponentClass();
      assertTrue(ReflectionUtils.isSuccessorOf(
          cellListClass,
          "com.google.gwt.user.cellview.client.CellList"));
      assertNoErrors(cellList);
    }
    assertEditor(
        "import com.google.gwt.user.cellview.client.CellList;",
        "import com.google.gwt.cell.client.TextCell;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      ValuePicker valuePicker = new ValuePicker(new CellList(new TextCell()));",
        "      rootPanel.add(valuePicker);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test removing child CellList & restore it by default CellList.
   */
  public void test_canDelete_CellList() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.google.gwt.user.cellview.client.CellList;",
            "import com.google.gwt.cell.client.Cell.Context;",
            "import com.google.gwt.cell.client.AbstractCell;",
            "import com.google.gwt.safehtml.shared.SafeHtmlBuilder;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      CellList cellList = new CellList(",
            "        new AbstractCell() {",
            "          @Override",
            "          public void render(Context context, Object value, SafeHtmlBuilder sb) { }",
            "        });",
            "      ValuePicker<String> valuePicker = new ValuePicker<String>(cellList);",
            "      rootPanel.add(valuePicker);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ValuePickerInfo valuePicker = (ValuePickerInfo) frame.getChildrenWidgets().get(0);
    CellListInfo cellList = valuePicker.getCellList();
    // 
    assertFalse(cellList.canDelete());
  }
}