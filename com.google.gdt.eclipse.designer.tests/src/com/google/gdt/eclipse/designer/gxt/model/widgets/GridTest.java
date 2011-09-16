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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.gdt.eclipse.designer.gxt.model.GxtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;

import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationChildEllipsisAssociation;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.swt.graphics.Image;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for <code>Grid</code>.
 * 
 * @author scheglov_ke
 */
public class GridTest extends GxtModelTest {
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
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.Collections;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(Collections.<ColumnConfig>emptyList()));",
            "      add(grid);",
            "      grid.setBorders(true);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), new ColumnModel(Collections.<ColumnConfig>emptyList()))/ /add(grid)/ /grid.setBorders(true)/}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Search for columns only for {@link ConstructorCreationSupport}.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44824
   */
  public void test_withExposed() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyPanel.java",
        getTestSource(
            "import java.util.Collections;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class MyPanel extends ContentPanel {",
            "  protected Grid grid;",
            "  public MyPanel() {",
            "    grid = new Grid(new ListStore(), new ColumnModel(Collections.<ColumnConfig>emptyList()));",
            "    add(grid);",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    WidgetInfo panel =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.client.MyPanel} {this} {}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {field: com.extjs.gxt.ui.client.widget.grid.Grid} {grid} {}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * If <code>Grid</code> was replaced with placeholder, we should not call its methods.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47945
   */
  public void test_whenPlaceholder() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/MyGrid.java",
        getTestSource(
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class MyGrid extends Grid {",
            "  public MyGrid() {",
            "    throw new IllegalStateException();",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    parseJavaInfo(
        "// filler filler filler",
        "public class Test extends ContentPanel {",
        "  public Test() {",
        "    MyGrid grid = new MyGrid();",
        "    add(grid);",
        "  }",
        "}");
    refresh();
    WidgetInfo grid = getJavaInfoByName("grid");
    assertTrue(grid.isPlaceholder());
  }

  /**
   * If we can not evaluate <code>ColumnModel</code>, so replace it with <code>null</code>, we
   * should replace it with empty, because <code>GridView</code> does not like <code>null</code>.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43315
   */
  public void test_null_ColumnModel() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.Collections;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      Grid grid = new Grid(new ListStore(), null);",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), null)/ /add(grid)/}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * User can create <code>ColumnModel</code> as external class, so we will not able to get its
   * <code>ColumnConfig</code> list.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48414
   */
  public void test_external_ColumnModel() throws Exception {
    dontUseSharedGWTState();
    m_waitForAutoBuild = true;
    parseJavaInfo(
        "import java.util.ArrayList;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      Grid grid = new Grid(new ListStore(), new PersonColumnModel());",
        "      add(grid);",
        "    }",
        "  }",
        "}",
        "",
        "class PersonColumnModel extends ColumnModel {",
        "  public PersonColumnModel() {",
        "    super(new ArrayList<ColumnConfig>());",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), new PersonColumnModel())/ /add(grid)/}");
  }

  /**
   * If we can not evaluate <code>ListStore</code>, so replace it with <code>null</code>, we should
   * replace it with empty, because <code>GridView</code> does not like <code>null</code>.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44403
   */
  public void test_null_ListStore() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      Grid grid = new Grid(null, null);",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(null, null)/ /add(grid)/}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * If we can not evaluate <code>ListStore</code>, so replace it with <code>null</code>, we should
   * replace it with empty, because <code>GridView</code> does not like <code>null</code>.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?44403
   */
  public void test_null_ListStore_GroupingView() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      Grid grid = new Grid(null, null);",
            "      grid.setView(new GroupingView());",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(null, null)/ /grid.setView(new GroupingView())/ /add(grid)/}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  public void test_CREATE() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.Collections;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // prepare new Grid
    WidgetInfo newGrid = createJavaInfo("com.extjs.gxt.ui.client.widget.grid.Grid");
    // check "live image"
    {
      Image liveImage = newGrid.getImage();
      assertEquals(302, liveImage.getBounds().width);
      assertEquals(202, liveImage.getBounds().height);
    }
    // do create
    panel.command_CREATE2(newGrid, null);
    assertEditor(
        "import java.util.Collections;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(Collections.<ColumnConfig>emptyList()));",
        "      add(grid);",
        "      grid.setBorders(true);",
        "    }",
        "  }",
        "}");
    panel.refresh();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Columns: parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_columns_parse_1() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.List;",
            "import java.util.ArrayList;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
            "      {",
            "        ColumnConfig column = new ColumnConfig();",
            "        configs.add(column);",
            "      }",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), new ColumnModel(configs))/ /add(grid)/}",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {local-unique: column} {/new ColumnConfig()/ /configs.add(column)/}");
    //
    panel.refresh();
    assertNoErrors(panel);
    // check ColumnConfig_Info
    ColumnConfigInfo column = getJavaInfoByName("column");
    {
      ColumnConfigAssociation association = (ColumnConfigAssociation) column.getAssociation();
      assertTrue(association.canDelete());
      {
        MethodInvocation invocation = association.getInvocation();
        assertNotNull(invocation);
        assertEquals("configs.add(column)", m_lastEditor.getSource(invocation));
      }
      {
        Statement statement = association.getStatement();
        assertNotNull(statement);
        assertEquals("configs.add(column);", m_lastEditor.getSource(statement));
      }
    }
  }

  /**
   * <code>ColumnModel</code> in variable.
   */
  public void test_columns_parse_2() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.List;",
            "import java.util.ArrayList;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
            "      {",
            "        ColumnConfig column = new ColumnConfig();",
            "        configs.add(column);",
            "      }",
            "      ColumnModel cm = new ColumnModel(configs);",
            "      Grid grid = new Grid(new ListStore(), cm);",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), cm)/ /add(grid)/}",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {local-unique: column} {/new ColumnConfig()/ /configs.add(column)/}");
    panel.refresh();
    assertNoErrors(panel);
  }

  public void test_columns_parse_ignoreAddNotInColumnList() throws Exception {
    parseJavaInfo(
        "import java.util.List;",
        "import java.util.ArrayList;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
        "      {",
        "        ColumnConfig column = new ColumnConfig();",
        "        // ignored because not SimpleName",
        "        new ArrayList().add(column);",
        "        // ignored because not 'configs'",
        "        List justArrayList = new ArrayList();",
        "        justArrayList.add(column);",
        "      }",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
        "      add(grid);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), new ColumnModel(configs))/ /add(grid)/}");
  }

  /**
   * Two grids with <code>ColumnModel</code> in variable.
   */
  public void test_columns_parse_twoGrids() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.List;",
            "import java.util.ArrayList;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
            "      {",
            "        ColumnConfig column_1 = new ColumnConfig();",
            "        configs.add(column_1);",
            "      }",
            "      ColumnModel cm = new ColumnModel(configs);",
            "      Grid grid_1 = new Grid(new ListStore(), cm);",
            "      add(grid_1);",
            "    }",
            "    {",
            "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
            "      {",
            "        ColumnConfig column_2 = new ColumnConfig();",
            "        configs.add(column_2);",
            "      }",
            "      ColumnModel cm = new ColumnModel(configs);",
            "      Grid grid_2 = new Grid(new ListStore(), cm);",
            "      add(grid_2);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid_1)/ /add(grid_2)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid_1} {/new Grid(new ListStore(), cm)/ /add(grid_1)/}",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {local-unique: column_1} {/new ColumnConfig()/ /configs.add(column_1)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid_2} {/new Grid(new ListStore(), cm)/ /add(grid_2)/}",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {local-unique: column_2} {/new ColumnConfig()/ /configs.add(column_2)/}");
    panel.refresh();
    assertNoErrors(panel);
  }

  public void test_columns_parse_andRender() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.List;",
            "import java.util.ArrayList;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
            "      {",
            "        ColumnConfig column = new ColumnConfig('id.1', 'First Column', 100);",
            "        configs.add(column);",
            "      }",
            "      {",
            "        ColumnConfig column = new ColumnConfig('id.2', 'Second Column', 200);",
            "        configs.add(column);",
            "      }",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridInfo grid = getJavaInfoByName("grid");
    List<?> columns =
        (List<?>) ScriptUtils.evaluate("getColumnModel().getColumns()", grid.getObject());
    assertThat(columns).hasSize(2);
    {
      Object column = columns.get(0);
      assertEquals("id.1", ScriptUtils.evaluate("getId()", column));
      assertEquals("First Column", ScriptUtils.evaluate("getHeader()", column));
      assertEquals(100, ScriptUtils.evaluate("getWidth()", column));
    }
    {
      Object column = columns.get(1);
      assertEquals("id.2", ScriptUtils.evaluate("getId()", column));
      assertEquals("Second Column", ScriptUtils.evaluate("getHeader()", column));
      assertEquals(200, ScriptUtils.evaluate("getWidth()", column));
    }
  }

  public void test_ColumnConfig_properties() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.List;",
            "import java.util.ArrayList;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
            "      {",
            "        ColumnConfig column = new ColumnConfig();",
            "        configs.add(column);",
            "      }",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    ColumnConfigInfo column = getJavaInfoByName("column");
    column.setWidth(200);
    assertEditor(
        "import java.util.List;",
        "import java.util.ArrayList;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
        "      {",
        "        ColumnConfig column = new ColumnConfig();",
        "        column.setWidth(200);",
        "        configs.add(column);",
        "      }",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
        "      add(grid);",
        "    }",
        "  }",
        "}");
  }

  /**
   * We should not execute <code>ColumnConfig.setHidden(true)</code>, because it causes
   * {@link NullPointerException}.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?45078
   */
  public void test_ColumnConfig_setHidden() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.List;",
            "import java.util.ArrayList;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
            "      {",
            "        ColumnConfig column = new ColumnConfig();",
            "        column.setHidden(true);",
            "        configs.add(column);",
            "      }",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Columns: operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_columns_DELETE() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.List;",
            "import java.util.ArrayList;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
            "      {",
            "        ColumnConfig column = new ColumnConfig();",
            "        configs.add(column);",
            "      }",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), new ColumnModel(configs))/ /add(grid)/}",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {local-unique: column} {/new ColumnConfig()/ /configs.add(column)/}");
    panel.refresh();
    GridInfo grid = getJavaInfoByName("grid");
    //
    List<ColumnConfigInfo> columns = grid.getColumns();
    assertThat(columns).hasSize(1);
    ColumnConfigInfo column = columns.get(0);
    assertTrue(column.canDelete());
    column.delete();
    assertEditor(
        "import java.util.List;",
        "import java.util.ArrayList;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
        "      add(grid);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), new ColumnModel(configs))/ /add(grid)/}");
  }

  /**
   * Test for {@link GridInfo#command_CREATE(ColumnConfigInfo, ColumnConfigInfo)}.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?45668
   */
  public void test_columns_CREATE_nullColumnModel() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.Collections;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      Grid grid = new Grid(new ListStore(), (ColumnModel) null);",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridInfo grid = getJavaInfoByName("grid");
    //
    ColumnConfigInfo newColumn = createJavaInfo("com.extjs.gxt.ui.client.widget.grid.ColumnConfig");
    grid.command_CREATE(newColumn, null);
    assertEditor(
        "import java.util.Collections;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "import java.util.List;",
        "import java.util.ArrayList;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
        "      {",
        "        ColumnConfig columnConfig = new ColumnConfig('id', 'New Column', 150);",
        "        configs.add(columnConfig);",
        "      }",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
        "      add(grid);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), new ColumnModel(configs))/ /add(grid)/}",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {local-unique: columnConfig} {/new ColumnConfig('id', 'New Column', 150)/ /configs.add(columnConfig)/}");
  }

  /**
   * Test for {@link GridInfo#command_CREATE(ColumnConfigInfo, ColumnConfigInfo)}.
   */
  public void test_columns_CREATE_noColumnsList() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.Collections;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(Collections.<ColumnConfig>emptyList()));",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridInfo grid = getJavaInfoByName("grid");
    //
    ColumnConfigInfo newColumn = createJavaInfo("com.extjs.gxt.ui.client.widget.grid.ColumnConfig");
    grid.command_CREATE(newColumn, null);
    assertEditor(
        "import java.util.Collections;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "import java.util.List;",
        "import java.util.ArrayList;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
        "      {",
        "        ColumnConfig columnConfig = new ColumnConfig('id', 'New Column', 150);",
        "        configs.add(columnConfig);",
        "      }",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
        "      add(grid);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), new ColumnModel(configs))/ /add(grid)/}",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {local-unique: columnConfig} {/new ColumnConfig('id', 'New Column', 150)/ /configs.add(columnConfig)/}");
  }

  /**
   * Test for {@link GridInfo#command_CREATE(ColumnConfigInfo, ColumnConfigInfo)}.
   */
  public void test_columns_CREATE_noOtherColumns() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.List;",
            "import java.util.ArrayList;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridInfo grid = getJavaInfoByName("grid");
    //
    ColumnConfigInfo newColumn = createJavaInfo("com.extjs.gxt.ui.client.widget.grid.ColumnConfig");
    grid.command_CREATE(newColumn, null);
    assertEditor(
        "import java.util.List;",
        "import java.util.ArrayList;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
        "      {",
        "        ColumnConfig columnConfig = new ColumnConfig('id', 'New Column', 150);",
        "        configs.add(columnConfig);",
        "      }",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
        "      add(grid);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), new ColumnModel(configs))/ /add(grid)/}",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {local-unique: columnConfig} {/new ColumnConfig('id', 'New Column', 150)/ /configs.add(columnConfig)/}");
  }

  /**
   * Test for {@link GridInfo#command_CREATE(ColumnConfigInfo, ColumnConfigInfo)}.
   */
  public void test_columns_CREATE_beforeOtherColumns() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.List;",
            "import java.util.ArrayList;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
            "      {",
            "        ColumnConfig column_1 = new ColumnConfig();",
            "        configs.add(column_1);",
            "      }",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridInfo grid = getJavaInfoByName("grid");
    ColumnConfigInfo column_1 = getJavaInfoByName("column_1");
    //
    ColumnConfigInfo newColumn = createJavaInfo("com.extjs.gxt.ui.client.widget.grid.ColumnConfig");
    grid.command_CREATE(newColumn, column_1);
    assertEditor(
        "import java.util.List;",
        "import java.util.ArrayList;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
        "      {",
        "        ColumnConfig columnConfig = new ColumnConfig('id', 'New Column', 150);",
        "        configs.add(columnConfig);",
        "      }",
        "      {",
        "        ColumnConfig column_1 = new ColumnConfig();",
        "        configs.add(column_1);",
        "      }",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
        "      add(grid);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), new ColumnModel(configs))/ /add(grid)/}",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {local-unique: columnConfig} {/new ColumnConfig('id', 'New Column', 150)/ /configs.add(columnConfig)/}",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {local-unique: column_1} {/new ColumnConfig()/ /configs.add(column_1)/}");
  }

  /**
   * Test for {@link GridInfo#command_CREATE(ColumnConfigInfo, ColumnConfigInfo)}.
   * <p>
   * When <code>Grid</code> itself was just created.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47667
   */
  public void test_columns_CREATE_intoNewGrid() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // create Grid
    GridInfo newGrid = createJavaInfo("com.extjs.gxt.ui.client.widget.grid.Grid");
    flowContainer_CREATE(panel, newGrid, null);
    assertEditor(
        "import com.extjs.gxt.ui.client.widget.grid.Grid;",
        "import com.extjs.gxt.ui.client.store.ListStore;",
        "import com.extjs.gxt.ui.client.widget.grid.ColumnModel;",
        "import java.util.Collections;",
        "import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(Collections.<ColumnConfig>emptyList()));",
        "      add(grid);",
        "      grid.setBorders(true);",
        "    }",
        "  }",
        "}");
    //
    ColumnConfigInfo newColumn = createJavaInfo("com.extjs.gxt.ui.client.widget.grid.ColumnConfig");
    newGrid.command_CREATE(newColumn, null);
    assertEditor(
        "import com.extjs.gxt.ui.client.widget.grid.Grid;",
        "import com.extjs.gxt.ui.client.store.ListStore;",
        "import com.extjs.gxt.ui.client.widget.grid.ColumnModel;",
        "import java.util.Collections;",
        "import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;",
        "import java.util.List;",
        "import java.util.ArrayList;",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
        "      {",
        "        ColumnConfig columnConfig = new ColumnConfig('id', 'New Column', 150);",
        "        configs.add(columnConfig);",
        "      }",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
        "      add(grid);",
        "      grid.setBorders(true);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), new ColumnModel(configs))/ /grid.setBorders(true)/ /add(grid)/}",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {local-unique: columnConfig} {/new ColumnConfig('id', 'New Column', 150)/ /configs.add(columnConfig)/}");
  }

  /**
   * Test for {@link GridInfo#command_MOVE(ColumnConfigInfo, ColumnConfigInfo)}.
   */
  public void test_columns_MOVE_beforeOtherColumns() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.List;",
            "import java.util.ArrayList;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
            "      {",
            "        ColumnConfig column_1 = new ColumnConfig();",
            "        configs.add(column_1);",
            "      }",
            "      {",
            "        ColumnConfig column_2 = new ColumnConfig();",
            "        configs.add(column_2);",
            "      }",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridInfo grid = getJavaInfoByName("grid");
    ColumnConfigInfo column_1 = getJavaInfoByName("column_1");
    ColumnConfigInfo column_2 = getJavaInfoByName("column_2");
    //
    grid.command_MOVE(column_2, column_1);
    assertEditor(
        "import java.util.List;",
        "import java.util.ArrayList;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
        "      {",
        "        ColumnConfig column_2 = new ColumnConfig();",
        "        configs.add(column_2);",
        "      }",
        "      {",
        "        ColumnConfig column_1 = new ColumnConfig();",
        "        configs.add(column_1);",
        "      }",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
        "      add(grid);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link GridInfo#command_MOVE(ColumnConfigInfo, ColumnConfigInfo)}.
   */
  public void test_columns_MOVE_toLast() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.List;",
            "import java.util.ArrayList;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
            "      {",
            "        ColumnConfig column_1 = new ColumnConfig();",
            "        configs.add(column_1);",
            "      }",
            "      {",
            "        ColumnConfig column_2 = new ColumnConfig();",
            "        configs.add(column_2);",
            "      }",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridInfo grid = getJavaInfoByName("grid");
    ColumnConfigInfo column_1 = getJavaInfoByName("column_1");
    //
    grid.command_MOVE(column_1, null);
    assertEditor(
        "import java.util.List;",
        "import java.util.ArrayList;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();",
        "      {",
        "        ColumnConfig column_2 = new ColumnConfig();",
        "        configs.add(column_2);",
        "      }",
        "      {",
        "        ColumnConfig column_1 = new ColumnConfig();",
        "        configs.add(column_1);",
        "      }",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(configs));",
        "      add(grid);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Arrays.asList() operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * <code>ColumnModel</code> with Arrays.asList().
   */
  public void test_columns_parse_asArray() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.Arrays;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      ColumnConfig column = new ColumnConfig();",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(Arrays.asList(column)));",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), new ColumnModel(Arrays.asList(column)))/ /add(grid)/}",
        "    Arrays.asList []",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {local-unique: column} {/new ColumnConfig()/ /Arrays.asList(column)/}");
    //
    panel.refresh();
    assertNoErrors(panel);
    // check ColumnConfig_Info
    ColumnConfigInfo column = getJavaInfoByName("column");
    {
      Association association = column.getAssociation();
      assertThat(association).isInstanceOf(InvocationChildEllipsisAssociation.class);
      assertTrue(association.canDelete());
    }
  }

  /**
   * Test for {@link GridInfo#command_CREATE(ColumnConfigInfo, ColumnConfigInfo)}.
   */
  public void test_columns_CREATE_asArray() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.Arrays;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      ColumnConfig column_1 = new ColumnConfig();",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(Arrays.asList(column_1)));",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridInfo grid = getJavaInfoByName("grid");
    ColumnConfigInfo column_1 = getJavaInfoByName("column_1");
    //
    ColumnConfigInfo newColumn = createJavaInfo("com.extjs.gxt.ui.client.widget.grid.ColumnConfig");
    grid.command_CREATE(newColumn, column_1);
    assertNoLoggedExceptions();
    assertNoErrors(grid);
    assertEditor(
        "import java.util.Arrays;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      ColumnConfig column_1 = new ColumnConfig();",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(Arrays.asList(new ColumnConfig('id', 'New Column', 150), column_1)));",
        "      add(grid);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.google.gwt.user.client.ui.HorizontalPanel} {this} {/add(grid)/}",
        "  {new: com.extjs.gxt.ui.client.widget.grid.Grid} {local-unique: grid} {/new Grid(new ListStore(), new ColumnModel(Arrays.asList(new ColumnConfig('id', 'New Column', 150), column_1)))/ /add(grid)/}",
        "    Arrays.asList []",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {empty} {/Arrays.asList(new ColumnConfig('id', 'New Column', 150), column_1)/}",
        "    {new: com.extjs.gxt.ui.client.widget.grid.ColumnConfig} {local-unique: column_1} {/new ColumnConfig()/ /Arrays.asList(new ColumnConfig('id', 'New Column', 150), column_1)/}");
  }

  /**
   * Test for {@link GridInfo#command_MOVE(ColumnConfigInfo, ColumnConfigInfo)}.
   */
  public void test_columns_MOVE_asArray() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.Arrays;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      ColumnConfig column_1 = new ColumnConfig();",
            "      ColumnConfig column_2 = new ColumnConfig();",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(Arrays.asList(column_1, column_2)));",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridInfo grid = getJavaInfoByName("grid");
    ColumnConfigInfo column_1 = getJavaInfoByName("column_1");
    //
    grid.command_MOVE(column_1, null);
    assertNoLoggedExceptions();
    assertNoErrors(grid);
    assertEditor(
        "import java.util.Arrays;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      ColumnConfig column_1 = new ColumnConfig();",
        "      ColumnConfig column_2 = new ColumnConfig();",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(Arrays.asList(column_2, column_1)));",
        "      add(grid);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for removing last column.
   */
  public void test_columns_DELETE_asArray() throws Exception {
    ComplexPanelInfo panel =
        parseJavaInfo(
            "import java.util.Arrays;",
            "import com.extjs.gxt.ui.client.store.*;",
            "import com.extjs.gxt.ui.client.widget.grid.*;",
            "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
            "  public Test() {",
            "    {",
            "      ColumnConfig column = new ColumnConfig();",
            "      Grid grid = new Grid(new ListStore(), new ColumnModel(Arrays.asList(column)));",
            "      add(grid);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    GridInfo grid = getJavaInfoByName("grid");
    ColumnConfigInfo column = getJavaInfoByName("column");
    //
    assertThat(column.canDelete()).isTrue();
    column.delete();
    panel.refresh();
    //
    assertNoLoggedExceptions();
    assertNoErrors(grid);
    assertEditor(
        "import java.util.Arrays;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "import java.util.Collections;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(Collections.<ColumnConfig>emptyList()));",
        "      add(grid);",
        "    }",
        "  }",
        "}");
    // and now add new column
    ColumnConfigInfo newColumn = createJavaInfo("com.extjs.gxt.ui.client.widget.grid.ColumnConfig");
    grid.command_CREATE(newColumn, null);
    assertNoLoggedExceptions();
    assertNoErrors(grid);
    assertEditor(
        "import java.util.Arrays;",
        "import com.extjs.gxt.ui.client.store.*;",
        "import com.extjs.gxt.ui.client.widget.grid.*;",
        "import java.util.Collections;",
        "public class Test extends com.google.gwt.user.client.ui.HorizontalPanel {",
        "  public Test() {",
        "    {",
        "      Grid grid = new Grid(new ListStore(), new ColumnModel(Arrays.asList(new ColumnConfig('id', 'New Column', 150))));",
        "      add(grid);",
        "    }",
        "  }",
        "}");
  }
}