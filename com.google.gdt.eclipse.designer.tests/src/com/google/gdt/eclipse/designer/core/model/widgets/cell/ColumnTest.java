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

import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.cell.CellTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.cell.ColumnInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.parser.ParseFactory;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import static org.easymock.EasyMock.capture;
import static org.fest.assertions.Assertions.assertThat;

import org.easymock.Capture;
import org.easymock.EasyMock;

import java.util.List;

/**
 * Test for <code>com.google.gwt.user.cellview.client.Column</code>.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 */
public class ColumnTest extends GwtModelTest {
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
    // prepare User class for content
    setFileContentSrc(
        "test/client/User.java",
        getSourceDQ(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test.client;",
            "public class User {",
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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_createDefault() throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    CellTable<User> cellTable = new CellTable<User>();",
        "    add(cellTable);",
        "    {",
        "      AbstractCell<Double> columnCell = new AbstractCell<Double>() {",
        "        @Override",
        "        public void render(Context context, Double value, SafeHtmlBuilder sb) {",
        "          sb.append(value);",
        "        }",
        "      };",
        "      Column<User, Double> column = new Column<User, Double>(columnCell) {",
        "        @Override",
        "        public Double getValue(User object) {",
        "          return null;",
        "        }",
        "      };",
        "      cellTable.addColumn(column);",
        "    }",
        "  }",
        "}");
    refresh();
    assertNoErrors(m_lastParseInfo);
    CellTableInfo cellTable = getJavaInfoByName("cellTable");
    // 1 column initially
    assertThat(cellTable.getColumns()).hasSize(1);
    // add new Column
    ColumnInfo newColumn = createJavaInfo("com.google.gwt.user.cellview.client.Column");
    flowContainer_CREATE(cellTable, newColumn, null);
    assertThat(cellTable.getColumns()).hasSize(2);
    assertNoErrors(m_lastParseInfo);
    assertEditor(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    CellTable<User> cellTable = new CellTable<User>();",
        "    add(cellTable);",
        "    {",
        "      AbstractCell<Double> columnCell = new AbstractCell<Double>() {",
        "        @Override",
        "        public void render(Context context, Double value, SafeHtmlBuilder sb) {",
        "          sb.append(value);",
        "        }",
        "      };",
        "      Column<User, Double> column = new Column<User, Double>(columnCell) {",
        "        @Override",
        "        public Double getValue(User object) {",
        "          return null;",
        "        }",
        "      };",
        "      cellTable.addColumn(column);",
        "    }",
        "    {",
        "      Column<User, String> column = new Column<User, String>(new TextCell()) {",
        "        @Override",
        "        public String getValue(User object) {",
        "          return (String) null;",
        "        }",
        "      };",
        "      cellTable.addColumn(column, 'New Column');",
        "    }",
        "  }",
        "}");
  }

  public void test_createIntoNewCellTable() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "  }",
            "}");
    refresh();
    // add new CellTable
    CellTableInfo cellTable;
    {
      cellTable = createJavaInfo("com.google.gwt.user.cellview.client.CellTable");
      cellTable.putTemplateArgument("rowType", "test.client.User");
      flowContainer_CREATE(panel, cellTable, null);
    }
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = new CellTable<User>();",
        "      add(cellTable);",
        "    }",
        "  }",
        "}");
    assertThat(cellTable.getColumns()).isEmpty();
    // add new Column
    {
      ColumnInfo column = createJavaInfo("com.google.gwt.user.cellview.client.Column");
      flowContainer_CREATE(cellTable, column, null);
      assertThat(cellTable.getColumns()).containsExactly(column);
    }
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = new CellTable<User>();",
        "      add(cellTable);",
        "      {",
        "        Column<User, String> column = new Column<User, String>(new TextCell()) {",
        "          @Override",
        "          public String getValue(User object) {",
        "            return (String) null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column, 'New Column');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  private void test_create(String creationId, String... source) throws Exception {
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = new CellTable<User>();",
        "      add(cellTable);",
        "    }",
        "  }",
        "}");
    refresh();
    //
    CellTableInfo cellTable = getJavaInfoByName("cellTable");
    assertNoErrors(cellTable);
    // create column 
    ColumnInfo newColumn = createJavaInfo("com.google.gwt.user.cellview.client.Column", creationId);
    flowContainer_CREATE(cellTable, newColumn, null);
    // check
    assertNoErrors(m_lastParseInfo);
    assertThat(cellTable.getColumns()).hasSize(1);
    assertEditor(source);
    // presentation icon
    assertSame(
        newColumn.getPresentation().getIcon(),
        newColumn.getDescription().getCreation(creationId).getIcon());
  }

  public void test_edittext() throws Exception {
    test_create(
        "edittext",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = new CellTable<User>();",
        "      add(cellTable);",
        "      {",
        "        Column<User, String> column = new Column<User, String>(new EditTextCell()) {",
        "          @Override",
        "          public String getValue(User object) {",
        "            return (String) null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column, 'New Column');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_number() throws Exception {
    test_create(
        "number",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = new CellTable<User>();",
        "      add(cellTable);",
        "      {",
        "        Column<User, Number> column = new Column<User, Number>(new NumberCell()) {",
        "          @Override",
        "          public Number getValue(User object) {",
        "            return (Number) null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column, 'New Column');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_button() throws Exception {
    test_create(
        "button",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = new CellTable<User>();",
        "      add(cellTable);",
        "      {",
        "        Column<User, String> column = new Column<User, String>(new ButtonCell()) {",
        "          @Override",
        "          public String getValue(User object) {",
        "            return (String) null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column, 'New Column');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_checkbox() throws Exception {
    test_create(
        "checkbox",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = new CellTable<User>();",
        "      add(cellTable);",
        "      {",
        "        Column<User, Boolean> column = new Column<User, Boolean>(new CheckboxCell()) {",
        "          @Override",
        "          public Boolean getValue(User object) {",
        "            return (Boolean) null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column, 'New Column');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_selection() throws Exception {
    test_create(
        "selection",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = new CellTable<User>();",
        "      add(cellTable);",
        "      {",
        "        Column<User, String> column = new Column<User, String>(new SelectionCell(new ArrayList<String>())) {",
        "          @Override",
        "          public String getValue(User object) {",
        "            return (String) null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column, 'New Column');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_date() throws Exception {
    test_create(
        "date",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = new CellTable<User>();",
        "      add(cellTable);",
        "      {",
        "        Column<User, Date> column = new Column<User, Date>(new DateCell()) {",
        "          @Override",
        "          public Date getValue(User object) {",
        "            return (Date) null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column, 'New Column');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_datepicker() throws Exception {
    test_create(
        "datepicker",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      CellTable<User> cellTable = new CellTable<User>();",
        "      add(cellTable);",
        "      {",
        "        Column<User, Date> column = new Column<User, Date>(new DatePickerCell()) {",
        "          @Override",
        "          public Date getValue(User object) {",
        "            return (Date) null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column, 'New Column');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property "header"
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test property "header".
   */
  public void test_headerProperty() throws Exception {
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CellTable<Number> cellTable = new CellTable<Number>();",
        "      {",
        "        TextColumn<Number> column_1 = new TextColumn<Number>() {",
        "          @Override",
        "          public String getValue(Number object) {",
        "            return null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column_1);",
        "      }",
        "      {",
        "        TextColumn<Number> column_2 = new TextColumn<Number>() {",
        "          @Override",
        "          public String getValue(Number object) {",
        "            return null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column_2, 'Column 2');",
        "      }",
        "      {",
        "        TextColumn<Number> column_3 = new TextColumn<Number>() {",
        "          @Override",
        "          public String getValue(Number object) {",
        "            return null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column_3, 'Column 3', 'Footer 3');",
        "      }",
        "      {",
        "        TextColumn<Number> column_4 = new TextColumn<Number>() {",
        "          @Override",
        "          public String getValue(Number object) {",
        "            return null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column_4, new TextHeader('column 4'));",
        "      }",
        "      {",
        "        TextColumn<Number> column_5 = new TextColumn<Number>() {",
        "          @Override",
        "          public String getValue(Number object) {",
        "            return null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column_5, new TextHeader('Column 5'), new TextHeader('Footer 5'));",
        "      }",
        "      rootPanel.add(cellTable);",
        "    }",
        "  }",
        "}");
    refresh();
    assertNoErrors(m_lastParseInfo);
    CellTableInfo cellTable = getJavaInfoByName("cellTable");
    // columns
    List<ColumnInfo> columns = cellTable.getColumns();
    assertThat(columns).hasSize(5);
    // check columns
    assert_column_header(columns.get(0), true, false, null, "col1");
    assert_column_header(columns.get(1), true, true, "Column 2", null);
    assert_column_header(columns.get(2), true, true, "Column 3", "col3");
    assert_column_header(columns.get(3), true, true, null, "col4");
    assert_column_header(columns.get(4), false, false, null, null);
    // check source
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      CellTable<Number> cellTable = new CellTable<Number>();",
        "      {",
        "        TextColumn<Number> column_1 = new TextColumn<Number>() {",
        "          @Override",
        "          public String getValue(Number object) {",
        "            return null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column_1, 'col1');",
        "      }",
        "      {",
        "        TextColumn<Number> column_2 = new TextColumn<Number>() {",
        "          @Override",
        "          public String getValue(Number object) {",
        "            return null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column_2);",
        "      }",
        "      {",
        "        TextColumn<Number> column_3 = new TextColumn<Number>() {",
        "          @Override",
        "          public String getValue(Number object) {",
        "            return null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column_3, 'col3', 'Footer 3');",
        "      }",
        "      {",
        "        TextColumn<Number> column_4 = new TextColumn<Number>() {",
        "          @Override",
        "          public String getValue(Number object) {",
        "            return null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column_4, 'col4');",
        "      }",
        "      {",
        "        TextColumn<Number> column_5 = new TextColumn<Number>() {",
        "          @Override",
        "          public String getValue(Number object) {",
        "            return null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column_5, new TextHeader('Column 5'), new TextHeader('Footer 5'));",
        "      }",
        "      rootPanel.add(cellTable);",
        "    }",
        "  }",
        "}");
  }

  public void test_noHeaderProperty() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyCellTable.java",
        getTestSource(
            "public class MyCellTable<T extends Object> extends CellTable<T> {",
            "  private final TextColumn<T> textColumn;",
            "  public MyCellTable() {",
            "    textColumn = new TextColumn<T>() {",
            "      @Override",
            "      public String getValue(T object) {",
            "        return object.toString();",
            "      }",
            "    };",
            "    addColumn(getTextColumn(), 'Column');",
            "  }",
            "  public TextColumn<T> getTextColumn() {",
            "    return textColumn;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    parseJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      MyCellTable<Number> cellTable = new MyCellTable<Number>();",
        "      {",
        "        TextColumn<Number> column = new TextColumn<Number>() {",
        "          @Override",
        "          public String getValue(Number object) {",
        "            return null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column, 'Header', 'Footer');",
        "      }",
        "      rootPanel.add(cellTable);",
        "    }",
        "  }",
        "}");
    refresh();
    assertNoErrors(m_lastParseInfo);
    //
    CellTableInfo cellTable = getJavaInfoByName("cellTable");
    List<ColumnInfo> columns = cellTable.getColumns();
    assertThat(columns.size()).isEqualTo(2);
    ColumnInfo exposedColumn = columns.get(0);
    ColumnInfo column = columns.get(1);
    assertThat(column).isNotSameAs(exposedColumn);
    // check
    assert_column_header(exposedColumn, false, false, null, "Will be ignored");
    assert_column_header(column, true, true, "Header", "New header");
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      MyCellTable<Number> cellTable = new MyCellTable<Number>();",
        "      {",
        "        TextColumn<Number> column = new TextColumn<Number>() {",
        "          @Override",
        "          public String getValue(Number object) {",
        "            return null;",
        "          }",
        "        };",
        "        cellTable.addColumn(column, 'New header', 'Footer');",
        "      }",
        "      rootPanel.add(cellTable);",
        "    }",
        "  }",
        "}");
  }

  private void assert_column_header(ColumnInfo column,
      boolean exist,
      boolean modified,
      Object value,
      Object newValue) throws Exception {
    Property property = column.getHeaderProperty();
    if (exist) {
      assertThat(property).isNotNull();
      assertThat(property.isModified()).isEqualTo(modified);
      assertThat(column.getPropertyByTitle("header")).isSameAs(property);
      if (value != null) {
        assertThat(property.getValue()).isEqualTo(value);
      } else {
        assertThat(property.getValue()).isNull();
      }
      property.setValue(newValue);
    } else {
      assertThat(property).isNull();
      assertThat(column.getPropertyByTitle("header")).isNull();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property "width"
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No "width" property for GWT before 2.2.
   */
  @DisposeProjectAfter
  public void test_widthProperty_oldVersion() throws Exception {
    do_projectDispose();
    do_projectCreate();
    configureNewProject(GTestUtils.getLocation_21());
    dontUseSharedGWTState();
    // parse
    parseJavaInfo(get_widthProperty_lines(null));
    refresh();
    ColumnInfo column = getJavaInfoByName("column");
    // no "width" property
    Property property = column.getPropertyByTitle("width");
    assertNull(property);
  }

  /**
   * Test for "width" property.
   */
  public void test_widthProperty_asString() throws Exception {
    parseJavaInfo(get_widthProperty_lines("'150px'"));
    refresh();
    ColumnInfo column = getJavaInfoByName("column");
    // prepare property
    Property property = column.getPropertyByTitle("width");
    assertNotNull(property);
    // has value
    assertTrue(property.isModified());
    assertEquals("150px", property.getValue());
    // set value
    property.setValue("5cm");
    assertEditor(get_widthProperty_lines("'5cm'"));
    // remove value
    property.setValue(null);
    assertEditor(get_widthProperty_lines(null));
    // remove value (again), ignored
    property.setValue(null);
    assertEditor(get_widthProperty_lines(null));
    // generate new setColumnWidth()
    property.setValue("40mm");
    assertEditor(get_widthProperty_lines("'40mm'"));
    // remove again
    property.setValue(Property.UNKNOWN_VALUE);
    assertEditor(get_widthProperty_lines(null));
  }

  /**
   * Test for "width" property.
   */
  public void test_widthProperty_asValueUnit() throws Exception {
    parseJavaInfo(get_widthProperty_lines("30, Unit.MM"));
    refresh();
    ColumnInfo column = getJavaInfoByName("column");
    // prepare property
    Property property = column.getPropertyByTitle("width");
    assertNotNull(property);
    // has value
    assertTrue(property.isModified());
    assertEquals("30.0mm", property.getValue());
    // set value
    property.setValue("5cm");
    assertEditor(get_widthProperty_lines("5.0, Unit.CM"));
    // no unit, use PX
    property.setValue("123");
    assertEditor(get_widthProperty_lines("123.0, Unit.PX"));
    // bad value, ignored
    property.setValue("qwerty");
    assertEditor(get_widthProperty_lines("123.0, Unit.PX"));
    // bad unit, ignored
    property.setValue("10qw");
    assertEditor(get_widthProperty_lines("123.0, Unit.PX"));
    // remove value
    property.setValue(null);
    assertEditor(get_widthProperty_lines(null));
    // remove value (again)
    property.setValue(null);
  }

  private static String[] get_widthProperty_lines(String width) {
    String[] widthLines;
    if (width != null) {
      widthLines = new String[]{"      setColumnWidth(column, " + width + ");"};
    } else {
      widthLines = new String[]{};
    }
    return CodeUtils.join(new String[]{
        "public class Test extends CellTable {",
        "  public Test() {",
        "    {",
        "      TextColumn<Object> column = new TextColumn<Object>() {",
        "        @Override",
        "        public String getValue(Object object) {",
        "          return null;",
        "        }",
        "      };",
        "      addColumn(column);",}, widthLines, new String[]{
        "    // filler filler filler filler filler",
        "    // filler filler filler filler filler",
        "    // filler filler filler filler filler",
        "    }",
        "  }",
        "}"});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property "comparator"
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for property "comparator".
   */
  public void test_comparatorProperty_hasValue() throws Exception {
    parseJavaInfo(
        "import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;",
        "public class Test extends CellTable<User> {",
        "  public Test() {",
        "    ListHandler<User> sortHandler = new ListHandler<User>(Collections.<User>emptyList());",
        "    addColumnSortHandler(sortHandler);",
        "    {",
        "      TextColumn<User> column = new TextColumn<User>() {",
        "        @Override",
        "        public String getValue(User object) {",
        "          return null;",
        "        }",
        "      };",
        "      addColumn(column);",
        "      sortHandler.setComparator(column, new Comparator<User>() {",
        "        @Override",
        "        public int compare(User o1, User o2) {",
        "          return 0;",
        "        }",
        "      });",
        "    }",
        "  }",
        "}");
    refresh();
    ColumnInfo column = getJavaInfoByName("column");
    // prepare property
    Property property = column.getPropertyByTitle("comparator");
    assertNotNull(property);
    // has value
    assertTrue(property.isModified());
    assertEquals("<comparator>", property.getValue());
    assertEquals("<comparator>", getPropertyText(property));
    // open Comparator
    {
      // set mock for DesignPageSite
      IDesignPageSite pageSite;
      Capture<Integer> openSourcePosition = new Capture<Integer>();
      {
        pageSite = EasyMock.createStrictMock(IDesignPageSite.class);
        pageSite.openSourcePosition(capture(openSourcePosition));
        EasyMock.replay(pageSite);
        // do set
        DesignPageSite.Helper.setSite(column, pageSite);
      }
      // ask open
      property.getEditor().doubleClick(property, null);
      waitEventLoop(0);
      // test results
      EasyMock.verify(pageSite);
      assertEquals(
          getNode("new Comparator").getStartPosition(),
          openSourcePosition.getValue().intValue());
    }
    // remove
    property.setValue(Property.UNKNOWN_VALUE);
    assertFalse(property.isModified());
    assertEquals("<empty>", property.getValue());
    assertEquals("<empty>", getPropertyText(property));
    assertEditor(
        "import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;",
        "public class Test extends CellTable<User> {",
        "  public Test() {",
        "    ListHandler<User> sortHandler = new ListHandler<User>(Collections.<User>emptyList());",
        "    addColumnSortHandler(sortHandler);",
        "    {",
        "      TextColumn<User> column = new TextColumn<User>() {",
        "        @Override",
        "        public String getValue(User object) {",
        "          return null;",
        "        }",
        "      };",
        "      addColumn(column);",
        "    }",
        "  }",
        "}");
    // try to remove again, no changes
    property.setValue(Property.UNKNOWN_VALUE);
  }

  /**
   * Test for property "comparator".
   * <p>
   * No existing <code>ListHandler.setComparator(Column,Comparator)</code>, generate new.
   */
  public void test_comparatorProperty_addInvocation() throws Exception {
    parseJavaInfo(
        "import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;",
        "public class Test extends CellTable<User> {",
        "  public Test() {",
        "    ListHandler<User> sortHandler = new ListHandler<User>(Collections.<User>emptyList());",
        "    addColumnSortHandler(sortHandler);",
        "    {",
        "      TextColumn<User> column = new TextColumn<User>() {",
        "        @Override",
        "        public String getValue(User object) {",
        "          return null;",
        "        }",
        "      };",
        "      addColumn(column);",
        "    }",
        "  }",
        "}");
    refresh();
    ColumnInfo column = getJavaInfoByName("column");
    // prepare property
    Property property = column.getPropertyByTitle("comparator");
    assertNotNull(property);
    // no value
    assertFalse(property.isModified());
    assertEquals("<empty>", property.getValue());
    assertEquals("<empty>", getPropertyText(property));
    // ask open
    {
      DesignPageSite.Helper.setSite(column, DesignPageSite.EMPTY);
      property.getEditor().doubleClick(property, null);
    }
    assertTrue(property.isModified());
    assertEquals("<comparator>", property.getValue());
    assertEquals("<comparator>", getPropertyText(property));
    assertEditor(
        "import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;",
        "public class Test extends CellTable<User> {",
        "  public Test() {",
        "    ListHandler<User> sortHandler = new ListHandler<User>(Collections.<User>emptyList());",
        "    addColumnSortHandler(sortHandler);",
        "    {",
        "      TextColumn<User> column = new TextColumn<User>() {",
        "        @Override",
        "        public String getValue(User object) {",
        "          return null;",
        "        }",
        "      };",
        "      column.setSortable(true);",
        "      addColumn(column);",
        "      sortHandler.setComparator(column, new Comparator<User>() {",
        "        public int compare(User o1, User o2) {",
        "          return 0;",
        "        }",
        "      });",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for property "comparator".
   * <p>
   * No existing <code>ListHandler.setComparator(Column,Comparator)</code>, generate new.
   */
  public void test_comparatorProperty_addInvocation_otherListHandlers() throws Exception {
    parseJavaInfo(
        "import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;",
        "public class Test extends CellTable<User> {",
        "  public Test() {",
        "    ListHandler<User> sortHandlerBadOne = null;",
        "    ListHandler<User> sortHandler = new ListHandler<User>(Collections.<User>emptyList());",
        "    ListHandler<User> sortHandlerBadTwo = null;",
        "    addColumnSortHandler(sortHandler);",
        "    {",
        "      TextColumn<User> column = new TextColumn<User>() {",
        "        @Override",
        "        public String getValue(User object) {",
        "          return null;",
        "        }",
        "      };",
        "      addColumn(column);",
        "    }",
        "  }",
        "}");
    refresh();
    ColumnInfo column = getJavaInfoByName("column");
    // prepare property
    Property property = column.getPropertyByTitle("comparator");
    assertNotNull(property);
    // ask open
    {
      DesignPageSite.Helper.setSite(column, DesignPageSite.EMPTY);
      property.getEditor().doubleClick(property, null);
    }
    assertEditor(
        "import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;",
        "public class Test extends CellTable<User> {",
        "  public Test() {",
        "    ListHandler<User> sortHandlerBadOne = null;",
        "    ListHandler<User> sortHandler = new ListHandler<User>(Collections.<User>emptyList());",
        "    ListHandler<User> sortHandlerBadTwo = null;",
        "    addColumnSortHandler(sortHandler);",
        "    {",
        "      TextColumn<User> column = new TextColumn<User>() {",
        "        @Override",
        "        public String getValue(User object) {",
        "          return null;",
        "        }",
        "      };",
        "      column.setSortable(true);",
        "      addColumn(column);",
        "      sortHandler.setComparator(column, new Comparator<User>() {",
        "        public int compare(User o1, User o2) {",
        "          return 0;",
        "        }",
        "      });",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for property "comparator".
   * <p>
   * No <code>ListHandler</code> instance, generate new.
   */
  public void test_comparatorProperty_addInvocation_noListHandler() throws Exception {
    parseJavaInfo(
        "public class Test extends CellTable<User> {",
        "  public Test() {",
        "    {",
        "      TextColumn<User> column = new TextColumn<User>() {",
        "        @Override",
        "        public String getValue(User object) {",
        "          return null;",
        "        }",
        "      };",
        "      addColumn(column);",
        "    }",
        "  }",
        "}");
    refresh();
    ColumnInfo column = getJavaInfoByName("column");
    // prepare property
    Property property = column.getPropertyByTitle("comparator");
    assertNotNull(property);
    // ask open
    {
      DesignPageSite.Helper.setSite(column, DesignPageSite.EMPTY);
      property.getEditor().doubleClick(property, null);
    }
    assertEditor(
        "import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;",
        "public class Test extends CellTable<User> {",
        "  private ListHandler<User> sortHandler = new ListHandler<User>(Collections.<User>emptyList());",
        "  public Test() {",
        "    addColumnSortHandler(sortHandler);",
        "    {",
        "      TextColumn<User> column = new TextColumn<User>() {",
        "        @Override",
        "        public String getValue(User object) {",
        "          return null;",
        "        }",
        "      };",
        "      column.setSortable(true);",
        "      addColumn(column);",
        "      sortHandler.setComparator(column, new Comparator<User>() {",
        "        public int compare(User o1, User o2) {",
        "          return 0;",
        "        }",
        "      });",
        "    }",
        "  }",
        "}");
  }
}