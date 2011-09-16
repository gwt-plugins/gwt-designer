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

import com.google.gdt.eclipse.designer.core.model.widgets.generic.GwtGefTest;
import com.google.gdt.eclipse.designer.model.widgets.cell.CellTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.cell.ColumnInfo;
import com.google.gdt.eclipse.designer.parser.ParseFactory;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for <code>com.google.gwt.user.cellview.client.Column</code> in GEF.
 * 
 * @author scheglov_ke
 */
public class ColumnGefTest extends GwtGefTest {
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
  protected void configureProject() throws Exception {
    super.configureProject();
    ParseFactory.disposeSharedGWTState();
  }

  @Override
  public void test_tearDown() throws Exception {
    super.test_tearDown();
    ParseFactory.disposeSharedGWTState();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property "header"
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_directEdit_hasHeaderProperty() throws Exception {
    openJavaInfo(
        "import com.google.gwt.user.cellview.client.*;",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    CellTable cellTable = new CellTable<Object>();",
        "    {",
        "      TextColumn column = new TextColumn<Object>() {",
        "        @Override",
        "        public String getValue(Object object) {",
        "          return object.toString();",
        "        }",
        "      };",
        "      cellTable.addColumn(column, 'Column');",
        "    }",
        "    add(cellTable);",
        "  }",
        "}");
    ColumnInfo column = getJavaInfoByName("column");
    //
    canvas.performDirectEdit(column, "New header");
    assertEditor(
        "import com.google.gwt.user.cellview.client.*;",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    CellTable cellTable = new CellTable<Object>();",
        "    {",
        "      TextColumn column = new TextColumn<Object>() {",
        "        @Override",
        "        public String getValue(Object object) {",
        "          return object.toString();",
        "        }",
        "      };",
        "      cellTable.addColumn(column, 'New header');",
        "    }",
        "    add(cellTable);",
        "  }",
        "}");
  }

  public void test_directEdit_noHeaderProperty() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyCellTable.java",
        getTestSource(
            "import com.google.gwt.cell.client.*;",
            "import com.google.gwt.user.cellview.client.*;",
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
    openJavaInfo(
        "import com.google.gwt.cell.client.*;",
        "import com.google.gwt.user.cellview.client.*;",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    MyCellTable cellTable = new MyCellTable<Object>();",
        "    {",
        "      TextColumn<Object> column = new TextColumn<Object>() {",
        "        @Override",
        "        public String getValue(Object object) {",
        "          return null;",
        "        }",
        "      };",
        "      cellTable.addColumn(column, new TextHeader('Column'), new TextHeader('Footer'));",
        "    }",
        "    add(cellTable);",
        "  }",
        "}");
    CellTableInfo cellTable = getJavaInfoByName("cellTable");
    List<ColumnInfo> columns = cellTable.getColumns();
    assertThat(columns.size()).isEqualTo(2);
    check_noDirectEdit(columns.get(0));
    check_noDirectEdit(columns.get(1));
  }

  private void check_noDirectEdit(ColumnInfo column) {
    boolean checkFailed;
    try {
      canvas.performDirectEdit(column, "New header");
      checkFailed = true;
    } catch (AssertionError e) {
      checkFailed = false;
    }
    if (checkFailed) {
      fail();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Width
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_widthProperty() throws Exception {
    openJavaInfo(
        "import com.google.gwt.user.cellview.client.*;",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    CellTable cellTable = new CellTable<Object>();",
        "    add(cellTable);",
        "    {",
        "      TextColumn column = new TextColumn<Object>() {",
        "        @Override",
        "        public String getValue(Object object) {",
        "          return object.toString();",
        "        }",
        "      };",
        "      cellTable.addColumn(column, 'Column');",
        "    }",
        "  }",
        "}");
    ColumnInfo column = getJavaInfoByName("column");
    //
    canvas.select(column);
    canvas.moveTo(column, -1, 5);
    canvas.beginDrag().dragTo(column, 150 - 1, 0).endDrag();
    assertEditor(
        "import com.google.gwt.user.cellview.client.*;",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    CellTable cellTable = new CellTable<Object>();",
        "    add(cellTable);",
        "    {",
        "      TextColumn column = new TextColumn<Object>() {",
        "        @Override",
        "        public String getValue(Object object) {",
        "          return object.toString();",
        "        }",
        "      };",
        "      cellTable.addColumn(column, 'Column');",
        "      cellTable.setColumnWidth(column, '150px');",
        "    }",
        "  }",
        "}");
  }
}
