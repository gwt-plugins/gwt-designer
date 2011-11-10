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
package com.google.gdt.eclipse.designer.smartgwt.model.widgets;

import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.TabSetInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.nonvisual.ArrayObjectInfo;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for typical table/grid with fields set via invoke 'setFields(array[])'.
 * 
 * @author sablin_aa
 */
public abstract class AbstractTableTest<T extends CanvasInfo, F extends AbstractComponentInfo>
    extends
      SmartGwtModelTest {
  protected final String m_tableClassName;
  protected final String m_tableClassShortName;
  protected final String m_fieldClassName;
  protected final String m_fieldClassShortName;

  protected AbstractTableTest(String tableClassName, String fieldClassName) {
    m_tableClassName = tableClassName;
    m_tableClassShortName = CodeUtils.getShortClass(tableClassName);
    m_fieldClassName = fieldClassName;
    m_fieldClassShortName = CodeUtils.getShortClass(fieldClassName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parsing.
   */
  public void test_parse() throws Exception {
    T table =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "  " + m_tableClassShortName + " table = new " + m_tableClassShortName + "();",
            "    "
                + m_fieldClassShortName
                + " field = new "
                + m_fieldClassShortName
                + "('field', 'Field');",
            "    table.setFields(new " + m_fieldClassShortName + "[] { field });",
            "    table.draw();",
            "  }",
            "}");
    table.refresh();
    //
    List<?> list = (List<?>) ReflectionUtils.invokeMethodEx(table, "getFields()");
    assertEquals(1, list.size());
    List<ArrayObjectInfo> arrayInfos = table.getChildren(ArrayObjectInfo.class);
    assertEquals(1, arrayInfos.size());
  }

  public void test_parse_this() throws Exception {
    T table =
        parseJavaInfo(
            "public class Test extends " + m_tableClassName + " {",
            "  public Test() {",
            "    "
                + m_fieldClassShortName
                + " field = new "
                + m_fieldClassShortName
                + "('field', 'Field');",
            "    setFields(new " + m_fieldClassShortName + "[] { field });",
            "  }",
            "}");
    table.refresh();
    //
    List<?> list = (List<?>) ReflectionUtils.invokeMethodEx(table, "getFields()");
    assertEquals(1, list.size());
    List<ArrayObjectInfo> arrayInfos = table.getChildren(ArrayObjectInfo.class);
    assertEquals(1, arrayInfos.size());
  }

  /**
   * Creation fields.
   */
  public void test_CREATE() throws Exception {
    T table =
        parseJavaInfo(
            "public class Test extends " + m_tableClassName + " {",
            "  public Test() {",
            "  }",
            "}");
    table.refresh();
    // create field
    F newField = createJavaInfo(m_fieldClassName);
    String fieldInfoClassName = newField.getClass().getName();
    ReflectionUtils.invokeMethodEx(table, "command_CREATE("
        + fieldInfoClassName
        + ","
        + fieldInfoClassName
        + ")", newField, null);
    //
    assertEditor(
        "public class Test extends " + m_tableClassName + " {",
        "  public Test() {",
        "    setFields(new "
            + m_fieldClassShortName
            + "[] { new "
            + m_fieldClassShortName
            + "('newField', 'New Field')});",
        "  }",
        "}");
  }

  /**
   * Move/reorder fields.
   */
  @SuppressWarnings("unchecked")
  public void test_MOVE() throws Exception {
    T table =
        parseJavaInfo( // filler
            "public class Test extends " + m_tableClassName + " {",
            "  public Test() {",
            "  setFields( new "
                + m_fieldClassShortName
                + "[] { new "
                + m_fieldClassShortName
                + "('field0'), new "
                + m_fieldClassShortName
                + "('field1')});",
            "  }",
            "}");
    table.refresh();
    //
    List<?> list = (List<?>) ReflectionUtils.invokeMethodEx(table, "getFields()");
    assertThat(list.size()).isEqualTo(2);
    F field0 = (F) list.get(0);
    F field1 = (F) list.get(1);
    ReflectionUtils.invokeMethodEx(table, "command_MOVE("
        + field1.getClass().getName()
        + ","
        + field0.getClass().getName()
        + ")", field1, field0);
    assertEditor(
        "public class Test extends " + m_tableClassName + " {",
        "  public Test() {",
        "  setFields( new "
            + m_fieldClassShortName
            + "[] { new "
            + m_fieldClassShortName
            + "('field1'), new "
            + m_fieldClassShortName
            + "('field0')});",
        "  }",
        "}");
  }

  @SuppressWarnings("unchecked")
  public void test_ADD() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "  Canvas canvas = new Canvas();",
            "  " + m_tableClassShortName + " table = new " + m_tableClassShortName + "();",
            "    "
                + m_fieldClassShortName
                + " field = new "
                + m_fieldClassShortName
                + "('field', 'Field');",
            "    table.setFields(new " + m_fieldClassShortName + "[] { field });",
            "    canvas.addChild(table);",
            "    canvas.draw();",
            "  }",
            "}");
    canvas.refresh();
    // table
    T table = getJavaInfoByName("table");
    List<?> fields = (List<?>) ReflectionUtils.invokeMethodEx(table, "getFields()");
    // new table 
    T newTable = createJavaInfo(m_tableClassName);
    canvas.command_absolute_CREATE(newTable, null);
    // move field
    F field = (F) fields.get(0);
    ReflectionUtils.invokeMethodEx(newTable, "command_MOVE("
        + field.getClass().getName()
        + ","
        + field.getClass().getName()
        + ")", field, null);
    final String newTableVariable = NamesManager.getName(newTable);
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "  Canvas canvas = new Canvas();",
        "  " + m_tableClassShortName + " table = new " + m_tableClassShortName + "();",
        "    canvas.addChild(table);",
        "    {",
        "      "
            + m_tableClassShortName
            + " "
            + newTableVariable
            + " = new "
            + m_tableClassShortName
            + "();",
        "      " // filler
            + m_fieldClassShortName
            + " field = new "
            + m_fieldClassShortName
            + "('field', 'Field');",
        "      " + newTableVariable + ".setFields(new " + m_fieldClassShortName + "[] { field});",
        "      canvas.addChild(" + newTableVariable + ");",
        "    }",
        "    canvas.draw();",
        "  }",
        "}");
  }

  /**
   * Test dispose objects when it been not rendered.
   */
  public void test_dispose() throws Exception {
    TabSetInfo tabSet =
        parseJavaInfo(new String[]{
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    TabSet tabSet = new TabSet();",
            "    tabSet.addTab(new Tab('Tab_1'));",
            "    Tab tab = new Tab('Tab_2');",
            "    {",
            "      " + m_tableClassShortName + " table = new " + m_tableClassShortName + "();",
            "      "
                + m_fieldClassShortName
                + " field = new "
                + m_fieldClassShortName
                + "('field', 'Field');",
            "      table.setFields(new " + m_fieldClassShortName + "[] { field });",
            "      tab.setPane(table);",
            "    }",
            "    tabSet.addTab(tab);",
            "    tabSet.draw();",
            "  }",
            "}"});
    tabSet.refresh();
  }
}