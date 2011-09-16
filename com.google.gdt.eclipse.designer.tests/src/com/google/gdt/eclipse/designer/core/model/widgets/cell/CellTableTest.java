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
package com.google.gdt.eclipse.designer.core.model.widgets.cell;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.cell.AbstractHasDataInfo;
import com.google.gdt.eclipse.designer.model.widgets.cell.CellTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.cell.ColumnInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.parser.ParseFactory;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.IntValue;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for <code>com.google.gwt.user.cellview.client.CellTable</code>.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 */
public class CellTableTest extends GwtModelTest {
  private static final int MCW = 119;

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
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureNewProject() throws Exception {
    super.configureNewProject();
    ParseFactory.disposeSharedGWTState();
  }

  @Override
  protected void configureModule(ModuleDescription moduleDescription) throws Exception {
    super.configureModule(moduleDescription);
    // prepare classes
    setFileContentSrc(
        "test/client/User.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test.client;",
            "public class User {",
            "}"));
    setFileContentSrc(
        "test/client/MyColumn.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test.client;",
            "import com.google.gwt.user.cellview.client.TextColumn;",
            "public class MyColumn<T> extends TextColumn<T> {",
            "  public String getValue(T object) {",
            "    return '<const>';",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/TableFactory.java",
        getTestSource(
            "public class TableFactory {",
            "  public static <T> CellTable<T> createTable() {",
            "    CellTable<T> table = new CellTable<T>();",
            "    table.addColumn(new MyColumn<T>(), 'First');",
            "    return table;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/Customer.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test.client;",
            "public class Customer {",
            "  public Customer(String name, String email) {",
            "  }",
            "  public String getName() {",
            "    return 'Some name';",
            "  }",
            "}"));
    setFileContentSrc(
        "test/client/CustomerColumn.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test.client;",
            "import com.google.gwt.user.cellview.client.TextColumn;",
            "public class CustomerColumn extends TextColumn<Customer> {",
            "  public String getValue(Customer customer) {",
            "    return customer.getName();",
            "  }",
            "}"));
    forgetCreatedResources();
  }

  @Override
  public void test_tearDown() throws Exception {
    super.test_tearDown();
    ParseFactory.disposeSharedGWTState();
  }

  @Override
  protected String[] getTestSource_decorate(String... lines) {
    lines =
        CodeUtils.join(new String[]{
            "import java.util.ArrayList;",
            "import java.util.Collections;",
            "import java.util.Comparator;",
            "import java.util.Date;",
            "import com.google.gwt.safehtml.shared.SafeHtmlBuilder;",
            "import com.google.gwt.cell.client.*;",
            "import com.google.gwt.cell.client.Cell.Context;",
            "import com.google.gwt.user.cellview.client.*;",}, lines);
    return super.getTestSource_decorate(lines);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse columns
  //
  ////////////////////////////////////////////////////////////////////////////
  private AbstractHasDataInfo test_parse(String... lines) throws Exception {
    parseJavaInfo(lines);
    refresh();
    assertNoErrors(m_lastParseInfo);
    CellTableInfo cellTable = getJavaInfoByName("cellTable");
    // check class
    {
      Class<?> classCellTable =
          m_lastLoader.loadClass("com.google.gwt.user.cellview.client.CellTable");
      assertSame(classCellTable, cellTable.getObject().getClass());
    }
    // check size
    {
      Dimension size = cellTable.getBounds().getSize();
      assertThat(size.width).isGreaterThan(200);
      assertThat(size.height).isGreaterThan(50);
    }
    // check columns
    {
      List<ColumnInfo> columns = cellTable.getChildren(ColumnInfo.class);
      assertThat(columns).hasSize(2);
      return cellTable;
    }
  }

  public void test_parse_1() throws Exception {
    test_parse(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CellTable cellTable = new CellTable();",
        "      {",
        "        AbstractCell<Double> columnCell = new AbstractCell<Double>() {",
        "          @Override",
        "          public void render(Context context, Double value, SafeHtmlBuilder sb) {",
        "            sb.append(value);",
        "          }",
        "        };",
        "        Column<Object, Double> column = new Column<Object, Double>(columnCell) {",
        "          @Override",
        "          public Double getValue(Object object) {",
        "            return null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column);",
        "      }",
        "      {",
        "        AbstractCell<Integer> columnCell = new AbstractCell<Integer>() {",
        "          @Override",
        "          public void render(Context context, Integer value, SafeHtmlBuilder sb) {",
        "            sb.append(value);",
        "          }",
        "        };",
        "        Column<Object, Integer> column = new Column<Object, Integer>(columnCell) {",
        "          @Override",
        "          public Integer getValue(Object object) {",
        "            return null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column, 'column 2');",
        "      }",
        "      rootPanel.add(cellTable);",
        "    }",
        "  }",
        "}");
  }

  public void test_parse_2() throws Exception {
    test_parse(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CellTable<Number> cellTable = new CellTable<Number>();",
        "      {",
        "        AbstractCell<Double> columnCell = new AbstractCell<Double>() {",
        "          @Override",
        "          public void render(Context context, Double value, SafeHtmlBuilder sb) {",
        "            sb.append(value);",
        "          }",
        "        };",
        "        Column<Number, Double> column = new Column<Number, Double>(columnCell) {",
        "          @Override",
        "          public Double getValue(Number object) {",
        "            return object.doubleValue();",
        "          }",
        "        };",
        "        Header<String> header = new Header<String>(",
        "          new AbstractCell<String>() {",
        "            @Override",
        "            public void render(Context context, String value, SafeHtmlBuilder sb) {",
        "              sb.appendEscaped(value);",
        "            }",
        "          }) {",
        "          @Override",
        "          public String getValue() {",
        "            return 'column 1';",
        "          }",
        "        };",
        "        cellTable.addColumn(column, header);",
        "      }",
        "      {",
        "        AbstractCell<Integer> columnCell = new AbstractCell<Integer>() {",
        "          @Override",
        "          public void render(Context context, Integer value, SafeHtmlBuilder sb) {",
        "            sb.append(value);",
        "          }",
        "        };",
        "        Column<Number, Integer> column = new Column<Number, Integer>(columnCell) {",
        "          @Override",
        "          public Integer getValue(Number object) {",
        "            return object.intValue();",
        "          }",
        "        };",
        "        cellTable.addColumn(column, new TextHeader('column 2'));",
        "      }",
        "      rootPanel.add(cellTable);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Even empty <code>CellTable</code> should have reasonable size, to ensure this we fill it with
   * artificial columns and items.
   */
  public void test_parseEmpty() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    CellTable cellTable = new CellTable();",
        "    add(cellTable);",
        "  }",
        "}");
    refresh();
    CellTableInfo cellTable = getJavaInfoByName("cellTable");
    // has reasonable size
    {
      Rectangle bounds = cellTable.getBounds();
      assertThat(bounds.width).isGreaterThan(200).isLessThan(300);
      assertThat(bounds.height).isGreaterThan(150);
    }
  }

  /**
   * If <code>CellTable</code> was created in factory, with at least <code>Column</code>, then we
   * should not added our artificial columns.
   */
  public void test_parseEmpty_hasFactoryColumns() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = TableFactory.createTable();",
        "      add(cellTable);",
        "    }",
        "  }",
        "}");
    refresh();
    CellTableInfo table = getJavaInfoByName("cellTable");
    // should have only one MyColumn
    {
      Rectangle bounds = table.getBounds();
      assertThat(bounds.width).isEqualTo(MCW);
    }
  }

  /**
   * Test that we support custom <code>Column</code>s and at same time show rows.
   */
  public void test_withSpecialCustomColumn() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    CellTable<Customer> cellTable = new CellTable<Customer>();",
        "    add(cellTable);",
        "    {",
        "      CustomerColumn column = new CustomerColumn();",
        "      cellTable.addColumn(column);",
        "    }",
        "    {",
        "      MyColumn<Customer> column = new MyColumn<Customer>();",
        "      cellTable.addColumn(column);",
        "    }",
        "  }",
        "}");
    refresh();
    assertNoErrors(m_lastParseInfo);
    CellTableInfo cellTable = getJavaInfoByName("cellTable");
    // has reasonable size
    {
      Rectangle bounds = cellTable.getBounds();
      assertThat(bounds.width).isGreaterThan(200).isLessThan(300);
      assertThat(bounds.height).isGreaterThan(120);
    }
  }

  public void test_createCellTable() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "  }",
            "}");
    refresh();
    {
      CellTableInfo cellTable = createJavaInfo("com.google.gwt.user.cellview.client.CellTable");
      cellTable.putTemplateArgument("rowType", "test.client.User");
      flowContainer_CREATE(panel, cellTable, null);
    }
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = new CellTable<User>();",
        "      add(cellTable);",
        "    }",
        "  }",
        "}");
    assertNoErrors(panel);
  }

  public void test_createColumn() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    CellTable<User> cellTable = new CellTable<User>();",
        "    add(cellTable);",
        "  }",
        "}");
    refresh();
    CellTableInfo cellTable = getJavaInfoByName("cellTable");
    // do create
    {
      ColumnInfo newColumn = createJavaInfo("test.client.MyColumn");
      flowContainer_CREATE(cellTable, newColumn, null);
    }
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    CellTable<User> cellTable = new CellTable<User>();",
        "    add(cellTable);",
        "    {",
        "      MyColumn myColumn = new MyColumn();",
        "      cellTable.addColumn(myColumn, 'New Column');",
        "    }",
        "  }",
        "}");
  }

  public void test_clipboard() throws Exception {
    final ComplexPanelInfo panel =
        parseJavaInfo(
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "    {",
            "      CellTable<User> table = new CellTable<User>();",
            "      add(table);",
            "      {",
            "        MyColumn<User> column = new MyColumn<User>() {",
            "          public String getValue(User user) {",
            "            return 'name';",
            "          }",
            "        };",
            "        table.addColumn(column, 'User name');",
            "      }",
            "      {",
            "        Column<User, Number> column = new Column<User, Number>(new NumberCell()) {",
            "          @Override",
            "          public Number getValue(User object) {",
            "            return 42;",
            "          }",
            "        };",
            "        table.addColumn(column, 'Age');",
            "      }",
            "    }",
            "  }",
            "}");
    refresh();
    {
      WidgetInfo table = getJavaInfoByName("table");
      doCopyPaste(table, new PasteProcedure<WidgetInfo>() {
        @Override
        public void run(WidgetInfo copy) throws Exception {
          flowContainer_CREATE(panel, copy, null);
        }
      });
    }
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> table = new CellTable<User>();",
        "      add(table);",
        "      {",
        "        MyColumn<User> column = new MyColumn<User>() {",
        "          public String getValue(User user) {",
        "            return 'name';",
        "          }",
        "        };",
        "        table.addColumn(column, 'User name');",
        "      }",
        "      {",
        "        Column<User, Number> column = new Column<User, Number>(new NumberCell()) {",
        "          @Override",
        "          public Number getValue(User object) {",
        "            return 42;",
        "          }",
        "        };",
        "        table.addColumn(column, 'Age');",
        "      }",
        "    }",
        "    {",
        "      CellTable<User> cellTable = new CellTable<User>();",
        "      add(cellTable);",
        "      {",
        "        MyColumn<User> myColumn = new MyColumn<User>() {",
        "          public String getValue(User user) {",
        "            return (String) null;",
        "          }",
        "        };",
        "        cellTable.addColumn(myColumn, 'User name');",
        "      }",
        "      {",
        "        Column<User, Number> column = new Column<User, Number>((Cell) null) {",
        "          public Number getValue(User object) {",
        "            return (Number) null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column, 'Age');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Columns bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_columnBounds_withHeader() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = new CellTable<User>();",
        "      {",
        "        MyColumn<User> column = new MyColumn<User>();",
        "        cellTable.addColumn(column);",
        "      }",
        "      {",
        "        MyColumn<User> column = new MyColumn<User>();",
        "        cellTable.addColumn(column);",
        "      }",
        "      {",
        "        MyColumn<User> column = new MyColumn<User>();",
        "        cellTable.addColumn(column, 'Long column header');",
        "      }",
        "      add(cellTable);",
        "    }",
        "  }",
        "}");
    refresh();
    CellTableInfo table = getJavaInfoByName("cellTable");
    // prepare Column-s
    List<ColumnInfo> columns = table.getColumns();
    assertThat(columns).hasSize(3);
    // check bounds for each Column
    int height = table.getHeaderHeight();
    {
      ColumnInfo column_1 = columns.get(0);
      assertThat(column_1.getModelBounds()).isEqualTo(new Rectangle(0, 0, MCW, height));
    }
    {
      ColumnInfo column_2 = columns.get(1);
      assertThat(column_2.getModelBounds()).isEqualTo(new Rectangle(MCW, 0, MCW, height));
    }
    {
      ColumnInfo column_3 = columns.get(2);
      int width = Expectations.get(210, new IntValue[]{new IntValue("flanker-linux", 210)});
      assertThat(column_3.getModelBounds()).isEqualTo(new Rectangle(2 * MCW, 0, width, height));
    }
  }

  /**
   * No header text, so height of column headers is "0".
   */
  public void test_columnBounds_noHeader() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = new CellTable<User>();",
        "      {",
        "        MyColumn<User> column = new MyColumn<User>();",
        "        cellTable.addColumn(column);",
        "      }",
        "      {",
        "        MyColumn<User> column = new MyColumn<User>();",
        "        cellTable.addColumn(column);",
        "      }",
        "      add(cellTable);",
        "    }",
        "  }",
        "}");
    refresh();
    CellTableInfo table = getJavaInfoByName("cellTable");
    // prepare Column-s
    List<ColumnInfo> columns = table.getColumns();
    assertThat(columns).hasSize(2);
    // check bounds for each Column
    {
      ColumnInfo column_1 = columns.get(0);
      assertThat(column_1.getModelBounds()).isEqualTo(new Rectangle(0, 0, MCW, 0));
    }
    {
      ColumnInfo column_2 = columns.get(1);
      assertThat(column_2.getModelBounds()).isEqualTo(new Rectangle(MCW, 0, MCW, 0));
    }
  }

  /**
   * If <code>CellTable</code> was created in factory, with at least <code>Column</code>, then we
   * should not added our artificial columns.
   */
  public void test_columnBounds_hasFactoryColumns() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = TableFactory.createTable();",
        "      add(cellTable);",
        "      {",
        "        MyColumn<User> column = new MyColumn<User>();",
        "        cellTable.addColumn(column);",
        "      }",
        "    }",
        "  }",
        "}");
    refresh();
    CellTableInfo table = getJavaInfoByName("cellTable");
    int height = table.getHeaderHeight();
    // has single Column model
    List<ColumnInfo> columns = table.getColumns();
    assertThat(columns).hasSize(1);
    // this Column is "second", so has corresponding bounds
    {
      ColumnInfo column = columns.get(0);
      assertThat(column.getModelBounds()).isEqualTo(new Rectangle(MCW, 0, MCW, height));
    }
  }
}