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
package com.google.gdt.eclipse.designer.core.model.widgets.cell;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.cell.AbstractHasDataInfo;
import com.google.gdt.eclipse.designer.model.widgets.cell.ColumnInfo;
import com.google.gdt.eclipse.designer.model.widgets.cell.DataGridInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.parser.ParseFactory;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for <code>com.google.gwt.user.cellview.client.DataGrid</code>.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 */
public class DataGridTest extends GwtModelTest {
  private static final int MCW = 225;

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
            "  public static <T> DataGrid<T> createTable() {",
            "    DataGrid<T> grid = new DataGrid<T>();",
            "    grid.addColumn(new MyColumn<T>(), 'First');",
            "    return grid;",
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
    DataGridInfo dataGrid = getJavaInfoByName("dataGrid");
    // check class
    {
      Class<?> classDataGrid =
          m_lastLoader.loadClass("com.google.gwt.user.cellview.client.DataGrid");
      assertSame(classDataGrid, dataGrid.getObject().getClass());
    }
    // check columns
    {
      List<ColumnInfo> columns = dataGrid.getChildren(ColumnInfo.class);
      assertThat(columns).hasSize(2);
      return dataGrid;
    }
  }

  public void test_parse_1() throws Exception {
    test_parse(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      DataGrid dataGrid = new DataGrid();",
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
        "        dataGrid.addColumn(column);",
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
        "        dataGrid.addColumn(column, 'column 2');",
        "      }",
        "      rootPanel.add(dataGrid);",
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
        "      DataGrid<Number> dataGrid = new DataGrid<Number>();",
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
        "        dataGrid.addColumn(column, header);",
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
        "        dataGrid.addColumn(column, new TextHeader('column 2'));",
        "      }",
        "      rootPanel.add(dataGrid);",
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
   * Even empty <code>DataGrid</code> should have reasonable size, to ensure this we fill it with
   * artificial columns and items.
   */
  public void test_parseEmpty() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    DataGrid dataGrid = new DataGrid();",
        "    add(dataGrid);",
        "  }",
        "}");
    refresh();
    DataGridInfo dataGrid = getJavaInfoByName("dataGrid");
    // has columns/rows
    assertEquals(3, ScriptUtils.evaluate("getColumnCount()", dataGrid.getObject()));
    assertEquals(5, ScriptUtils.evaluate("getRowCount()", dataGrid.getObject()));
  }

  /**
   * If <code>DataGrid</code> was created in factory, with at least <code>Column</code>, then we
   * should not added our artificial columns.
   */
  public void test_parseEmpty_hasFactoryColumns() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      DataGrid<User> dataGrid = TableFactory.createTable();",
        "      add(dataGrid);",
        "    }",
        "  }",
        "}");
    refresh();
    DataGridInfo grid = getJavaInfoByName("dataGrid");
    // should have only one MyColumn
    assertEquals(1, ScriptUtils.evaluate("getColumnCount()", grid.getObject()));
  }

  /**
   * Test that we support custom <code>Column</code>s and at same time show rows.
   */
  public void test_withSpecialCustomColumn() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    DataGrid<Customer> dataGrid = new DataGrid<Customer>();",
        "    add(dataGrid);",
        "    {",
        "      CustomerColumn column = new CustomerColumn();",
        "      dataGrid.addColumn(column);",
        "    }",
        "    {",
        "      MyColumn<Customer> column = new MyColumn<Customer>();",
        "      dataGrid.addColumn(column);",
        "    }",
        "  }",
        "}");
    refresh();
    assertNoErrors(m_lastParseInfo);
    DataGridInfo dataGrid = getJavaInfoByName("dataGrid");
    // has columns/rows
    assertEquals(2, ScriptUtils.evaluate("getColumnCount()", dataGrid.getObject()));
    assertEquals(5, ScriptUtils.evaluate("getRowCount()", dataGrid.getObject()));
  }

  public void test_createDataGrid() throws Exception {
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
      DataGridInfo dataGrid = createJavaInfo("com.google.gwt.user.cellview.client.DataGrid");
      dataGrid.putTemplateArgument("rowType", "test.client.User");
      flowContainer_CREATE(panel, dataGrid, null);
    }
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      DataGrid<User> dataGrid = new DataGrid<User>();",
        "      add(dataGrid);",
        "      dataGrid.setWidth('250px');",
        "      dataGrid.setHeight('200px');",
        "    }",
        "  }",
        "}");
    assertNoErrors(panel);
  }

  public void test_createColumn() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    DataGrid<User> dataGrid = new DataGrid<User>();",
        "    add(dataGrid);",
        "  }",
        "}");
    refresh();
    DataGridInfo dataGrid = getJavaInfoByName("dataGrid");
    // do create
    {
      ColumnInfo newColumn = createJavaInfo("test.client.MyColumn");
      flowContainer_CREATE(dataGrid, newColumn, null);
    }
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    DataGrid<User> dataGrid = new DataGrid<User>();",
        "    add(dataGrid);",
        "    {",
        "      MyColumn myColumn = new MyColumn();",
        "      dataGrid.addColumn(myColumn, 'New Column');",
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
            "      DataGrid<User> grid = new DataGrid<User>();",
            "      add(grid);",
            "      {",
            "        MyColumn<User> column = new MyColumn<User>() {",
            "          public String getValue(User user) {",
            "            return 'name';",
            "          }",
            "        };",
            "        grid.addColumn(column, 'User name');",
            "      }",
            "      {",
            "        Column<User, Number> column = new Column<User, Number>(new NumberCell()) {",
            "          @Override",
            "          public Number getValue(User object) {",
            "            return 42;",
            "          }",
            "        };",
            "        grid.addColumn(column, 'Age');",
            "      }",
            "    }",
            "  }",
            "}");
    refresh();
    {
      WidgetInfo grid = getJavaInfoByName("grid");
      doCopyPaste(grid, new PasteProcedure<WidgetInfo>() {
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
        "      DataGrid<User> grid = new DataGrid<User>();",
        "      add(grid);",
        "      {",
        "        MyColumn<User> column = new MyColumn<User>() {",
        "          public String getValue(User user) {",
        "            return 'name';",
        "          }",
        "        };",
        "        grid.addColumn(column, 'User name');",
        "      }",
        "      {",
        "        Column<User, Number> column = new Column<User, Number>(new NumberCell()) {",
        "          @Override",
        "          public Number getValue(User object) {",
        "            return 42;",
        "          }",
        "        };",
        "        grid.addColumn(column, 'Age');",
        "      }",
        "    }",
        "    {",
        "      DataGrid<User> dataGrid = new DataGrid<User>();",
        "      add(dataGrid);",
        "      {",
        "        MyColumn<User> myColumn = new MyColumn<User>() {",
        "          public String getValue(User user) {",
        "            return (String) null;",
        "          }",
        "        };",
        "        dataGrid.addColumn(myColumn, 'User name');",
        "      }",
        "      {",
        "        Column<User, Number> column = new Column<User, Number>((Cell) null) {",
        "          public Number getValue(User object) {",
        "            return (Number) null;",
        "          }",
        "        };",
        "        dataGrid.addColumn(column, 'Age');",
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
        "      DataGrid<User> dataGrid = new DataGrid<User>();",
        "      {",
        "        MyColumn<User> column = new MyColumn<User>();",
        "        dataGrid.addColumn(column);",
        "      }",
        "      {",
        "        MyColumn<User> column = new MyColumn<User>();",
        "        dataGrid.addColumn(column);",
        "      }",
        "      {",
        "        MyColumn<User> column = new MyColumn<User>();",
        "        dataGrid.addColumn(column, 'Long column header');",
        "      }",
        "      add(dataGrid);",
        "    }",
        "  }",
        "}");
    refresh();
    DataGridInfo grid = getJavaInfoByName("dataGrid");
    // prepare Column-s
    List<ColumnInfo> columns = grid.getColumns();
    assertThat(columns).hasSize(3);
    // check bounds for each Column
    int height = grid.getHeaderHeight();
    {
      ColumnInfo column_1 = columns.get(0);
      assertThat(column_1.getModelBounds()).isEqualTo(new Rectangle(0, 0, 150, height));
    }
    {
      ColumnInfo column_2 = columns.get(1);
      assertThat(column_2.getModelBounds()).isEqualTo(new Rectangle(150, 0, 150, height));
    }
    {
      ColumnInfo column_3 = columns.get(2);
      assertThat(column_3.getModelBounds()).isEqualTo(new Rectangle(2 * 150, 0, 150, height));
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
        "      DataGrid<User> dataGrid = new DataGrid<User>();",
        "      {",
        "        MyColumn<User> column = new MyColumn<User>();",
        "        dataGrid.addColumn(column);",
        "      }",
        "      {",
        "        MyColumn<User> column = new MyColumn<User>();",
        "        dataGrid.addColumn(column);",
        "      }",
        "      add(dataGrid);",
        "    }",
        "  }",
        "}");
    refresh();
    DataGridInfo grid = getJavaInfoByName("dataGrid");
    // prepare Column-s
    List<ColumnInfo> columns = grid.getColumns();
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
   * If <code>DataGrid</code> was created in factory, with at least <code>Column</code>, then we
   * should not added our artificial columns.
   */
  public void test_columnBounds_hasFactoryColumns() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      DataGrid<User> dataGrid = TableFactory.createTable();",
        "      add(dataGrid);",
        "      {",
        "        MyColumn<User> column = new MyColumn<User>();",
        "        dataGrid.addColumn(column);",
        "      }",
        "    }",
        "  }",
        "}");
    refresh();
    DataGridInfo grid = getJavaInfoByName("dataGrid");
    int height = grid.getHeaderHeight();
    // has single Column model
    List<ColumnInfo> columns = grid.getColumns();
    assertThat(columns).hasSize(1);
    // this Column is "second", so has corresponding bounds
    {
      ColumnInfo column = columns.get(0);
      assertThat(column.getModelBounds()).isEqualTo(new Rectangle(MCW, 0, MCW, height));
    }
  }
}