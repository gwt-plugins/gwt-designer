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
import com.google.gdt.eclipse.designer.gxt.model.widgets.PortalInfo.ColumnInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.RectValue;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link PortalInfo}.
 * 
 * @author scheglov_ke
 */
public class PortalTest extends GxtModelTest {
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
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(2);",
            "    {",
            "      Portlet portlet = new Portlet();",
            "      add(portlet, 0);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.custom.Portal} {this} {/add(portlet, 0)/}",
        "  {new: com.extjs.gxt.ui.client.widget.custom.Portlet} {local-unique: portlet} {/new Portlet()/ /add(portlet, 0)/}",
        "    {implicit-layout: com.extjs.gxt.ui.client.widget.layout.FlowLayout} {implicit-layout} {}");
    //
    portal.refresh();
    assertNoErrors(portal);
  }

  public void test_CREATE_portalItself() throws Exception {
    LayoutContainerInfo container =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends LayoutContainer {",
            "  public Test() {",
            "  }",
            "}");
    container.refresh();
    //
    PortalInfo portal = createJavaInfo("com.extjs.gxt.ui.client.widget.custom.Portal");
    container.getLayout().command_CREATE(portal);
    assertNoErrors(container);
    assertEditor(
        "// filler filler filler",
        "public class Test extends LayoutContainer {",
        "  public Test() {",
        "    {",
        "      Portal portal = new Portal(2);",
        "      add(portal);",
        "      portal.setColumnWidth(0, 0.7);",
        "      portal.setColumnWidth(1, 0.3);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: com.extjs.gxt.ui.client.widget.LayoutContainer} {this} {/add(portal)/}",
        "  {implicit-layout: default} {implicit-layout} {}",
        "  {new: com.extjs.gxt.ui.client.widget.custom.Portal} {local-unique: portal} {/new Portal(2)/ /portal.setColumnWidth(1, 0.3)/ /portal.setColumnWidth(0, 0.7)/ /add(portal)/}");
  }

  /**
   * We should exclude bottom "10px" margin from portlet bounds.
   */
  public void test_portletBounds_withoutBottomMargin() throws Exception {
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(1);",
            "    setColumnWidth(0, 200);",
            "    add(new Portlet(), 0);",
            "  }",
            "}");
    portal.refresh();
    //
    PortletInfo portlet = portal.getPortlets().get(0);
    Rectangle expected =
        Expectations.get(null, new RectValue[]{
            new RectValue("scheglov-win", new Rectangle(10, 10, 200 - 10, 61 - 10)),
            new RectValue("Flanker-Windows", new Rectangle(10, 10, 200 - 10, 61 - 10)),
            new RectValue("SABLIN-AA", new Rectangle(10, 10, 200 - 10, 59 - 10))});
    assertEquals(expected, portlet.getBounds());
    assertEquals(expected, portlet.getModelBounds());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Columns
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link PortalInfo#getColumns()} and other simple getters.
   */
  public void test_Column_get() throws Exception {
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(2);",
            "    setColumnWidth(0, 0.7);",
            "    setColumnWidth(1, 200);",
            "  }",
            "}");
    portal.refresh();
    // prepare columns
    List<ColumnInfo> columns = portal.getColumns();
    assertThat(columns).hasSize(2);
    // test getBounds()
    assertEquals(new Rectangle(0, 0, 450, 300), portal.getBounds());
    {
      ColumnInfo column = columns.get(0);
      assertEquals(new Rectangle(10, 0, 151, 300), column.getBounds());
      assertEquals(0.7, column.getWidth(), 1E-10);
    }
    {
      ColumnInfo column = columns.get(1);
      assertEquals(new Rectangle(171, 0, 190, 300), column.getBounds());
      assertEquals(200.0, column.getWidth(), 1E-10);
    }
  }

  /**
   * Test for {@link PortalInfo#getColumns()} and other simple getters.
   */
  public void test_Column_get2() throws Exception {
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(2);",
            "    setColumnWidth(0, 0.7);",
            "    setColumnWidth(1, 0.3);",
            "    add(new Portlet(), 0);",
            "    add(new Portlet(), 0);",
            "    add(new Portlet(), 1);",
            "  }",
            "}");
    portal.refresh();
    List<PortletInfo> portlets = portal.getPortlets();
    // prepare columns
    List<ColumnInfo> columns = portal.getColumns();
    assertThat(columns).hasSize(2);
    //
    {
      ColumnInfo column = columns.get(0);
      assertSame(portal, column.getPortal());
      assertThat(column.getPortlets()).containsExactly(portlets.get(0), portlets.get(1));
    }
    {
      ColumnInfo column = columns.get(1);
      assertSame(portal, column.getPortal());
      assertThat(column.getPortlets()).containsExactly(portlets.get(2));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column: setWidth()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ColumnInfo#setWidth(double)}.
   */
  public void test_Column_setWidth_updateOther() throws Exception {
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(1);",
            "    setColumnWidth(0, 0.7);",
            "  }",
            "}");
    portal.refresh();
    //
    ColumnInfo column = portal.getColumns().get(0);
    column.setWidth(100);
    assertEditor(
        "public class Test extends Portal {",
        "  public Test() {",
        "    super(1);",
        "    setColumnWidth(0, 100.0);",
        "  }",
        "}");
  }

  /**
   * Test for {@link ColumnInfo#setWidth(double)}.
   */
  public void test_Column_setWidth_beforeOther() throws Exception {
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(5);",
            "    setColumnWidth(2, 0.1);",
            "    setEnabled(true);",
            "  }",
            "}");
    portal.refresh();
    //
    ColumnInfo column = portal.getColumns().get(0);
    column.setWidth(100);
    assertEditor(
        "public class Test extends Portal {",
        "  public Test() {",
        "    super(5);",
        "    setColumnWidth(0, 100.0);",
        "    setColumnWidth(2, 0.1);",
        "    setEnabled(true);",
        "  }",
        "}");
  }

  /**
   * Test for {@link ColumnInfo#setWidth(double)}.
   */
  public void test_Column_setWidth_afterOther() throws Exception {
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(5);",
            "    setColumnWidth(2, 0.1);",
            "    setEnabled(true);",
            "  }",
            "}");
    portal.refresh();
    //
    ColumnInfo column = portal.getColumns().get(4);
    column.setWidth(100);
    assertEditor(
        "public class Test extends Portal {",
        "  public Test() {",
        "    super(5);",
        "    setColumnWidth(2, 0.1);",
        "    setColumnWidth(4, 100.0);",
        "    setEnabled(true);",
        "  }",
        "}");
  }

  /**
   * Test for {@link ColumnInfo#setWidth(double)}.
   */
  public void test_Column_setWidth_noExisting() throws Exception {
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(5);",
            "    setEnabled(true);",
            "  }",
            "}");
    portal.refresh();
    //
    ColumnInfo column = portal.getColumns().get(2);
    column.setWidth(100);
    assertEditor(
        "public class Test extends Portal {",
        "  public Test() {",
        "    super(5);",
        "    setColumnWidth(2, 100.0);",
        "    setEnabled(true);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getColumnReference()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link PortalInfo#getColumnReference(int)}
   */
  public void test_getColumnReference_hasTargetInNextColumn() throws Exception {
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(2);",
            "    add(new Portlet(), 1);",
            "  }",
            "}");
    portal.refresh();
    //
    PortletInfo expected = portal.getPortlets().get(0);
    assertSame(expected, invoke_getColumnReference(portal, 0));
  }

  /**
   * Test for {@link PortalInfo#getColumnReference(int)}
   */
  public void test_getColumnReference_hasTargetInNextNextColumn() throws Exception {
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(3);",
            "    add(new Portlet(), 2);",
            "  }",
            "}");
    portal.refresh();
    //
    PortletInfo expected = portal.getPortlets().get(0);
    assertSame(expected, invoke_getColumnReference(portal, 0));
  }

  /**
   * Test for {@link PortalInfo#getColumnReference(int)}
   */
  public void test_getColumnReference_noTarget() throws Exception {
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(2);",
            "  }",
            "}");
    portal.refresh();
    //
    assertSame(null, invoke_getColumnReference(portal, 0));
  }

  /**
   * Test for {@link PortalInfo#getColumnReference(int)}
   */
  public void test_getColumnReference_ignorePreviousColumns() throws Exception {
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(3);",
            "    add(new Portlet(), 0);",
            "    add(new Portlet(), 2);",
            "  }",
            "}");
    portal.refresh();
    //
    PortletInfo expected = portal.getPortlets().get(1);
    assertSame(expected, invoke_getColumnReference(portal, 1));
  }

  private PortletInfo invoke_getColumnReference(PortalInfo portal, int targetColumn)
      throws Exception {
    return (PortletInfo) ReflectionUtils.invokeMethod(
        portal,
        "getColumnReference(int)",
        targetColumn);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column: CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_column_CREATE_beforeExisting() throws Exception {
    final PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(2);",
            "    setColumnWidth(0, 0.7);",
            "    setColumnWidth(1, 0.3);",
            "    {",
            "      Portlet portlet_0 = new Portlet();",
            "      add(portlet_0, 0);",
            "    }",
            "    {",
            "      Portlet portlet_1 = new Portlet();",
            "      add(portlet_1, 1);",
            "    }",
            "  }",
            "}");
    portal.refresh();
    portal.startEdit();
    //
    ExecutionUtils.run(portal, new RunnableEx() {
      public void run() throws Exception {
        List<ColumnInfo> columns = portal.getColumns();
        ColumnInfo nextColumn = columns.get(1);
        ColumnInfo newColumn = portal.command_CREATE(nextColumn);
        assertEquals(1, columns.indexOf(newColumn));
        assertEquals(2, columns.indexOf(nextColumn));
      }
    });
    assertEditor(
        "public class Test extends Portal {",
        "  public Test() {",
        "    super(3);",
        "    setColumnWidth(0, 0.7);",
        "    setColumnWidth(1, 100.0);",
        "    setColumnWidth(2, 0.3);",
        "    {",
        "      Portlet portlet_0 = new Portlet();",
        "      add(portlet_0, 0);",
        "    }",
        "    {",
        "      Portlet portlet_1 = new Portlet();",
        "      add(portlet_1, 2);",
        "    }",
        "  }",
        "}");
  }

  public void test_column_CREATE_noColumns() throws Exception {
    final PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(0);",
            "  }",
            "}");
    portal.refresh();
    //
    ExecutionUtils.run(portal, new RunnableEx() {
      public void run() throws Exception {
        List<ColumnInfo> columns = portal.getColumns();
        ColumnInfo newColumn = portal.command_CREATE(null);
        assertEquals(0, columns.indexOf(newColumn));
      }
    });
    assertEditor(
        "public class Test extends Portal {",
        "  public Test() {",
        "    super(1);",
        "    setColumnWidth(0, 100.0);",
        "  }",
        "}");
    // we still should have column
    assertThat(portal.getColumns()).hasSize(1);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column: DELETE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link PortalInfo#command_DELETE(ColumnInfo)}.
   */
  public void test_column_DELETE() throws Exception {
    final PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(3);",
            "    setColumnWidth(0, 100.0);",
            "    setColumnWidth(1, 200.0);",
            "    setColumnWidth(2, 300.0);",
            "    {",
            "      Portlet portlet_0 = new Portlet();",
            "      add(portlet_0, 0);",
            "    }",
            "    {",
            "      Portlet portlet_1 = new Portlet();",
            "      add(portlet_1, 1);",
            "    }",
            "    {",
            "      Portlet portlet_2 = new Portlet();",
            "      add(portlet_2, 2);",
            "    }",
            "  }",
            "}");
    portal.refresh();
    //
    ExecutionUtils.run(portal, new RunnableEx() {
      public void run() throws Exception {
        ColumnInfo column = portal.getColumns().get(1);
        portal.command_DELETE(column);
        assertThat(portal.getColumns()).excludes(column);
      }
    });
    assertEditor(
        "public class Test extends Portal {",
        "  public Test() {",
        "    super(2);",
        "    setColumnWidth(0, 100.0);",
        "    setColumnWidth(1, 300.0);",
        "    {",
        "      Portlet portlet_0 = new Portlet();",
        "      add(portlet_0, 0);",
        "    }",
        "    {",
        "      Portlet portlet_2 = new Portlet();",
        "      add(portlet_2, 1);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Column: MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link PortalInfo#command_MOVE(ColumnInfo, ColumnInfo)}.
   */
  public void test_column_MOVE_last() throws Exception {
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(3);",
            "    setColumnWidth(0, 100.0);",
            "    setColumnWidth(1, 200.0);",
            "    setColumnWidth(2, 300.0);",
            "    {",
            "      Portlet portlet_0 = new Portlet();",
            "      add(portlet_0, 0);",
            "    }",
            "    {",
            "      Portlet portlet_1 = new Portlet();",
            "      add(portlet_1, 1);",
            "    }",
            "    {",
            "      Portlet portlet_2 = new Portlet();",
            "      add(portlet_2, 2);",
            "    }",
            "  }",
            "}");
    portal.refresh();
    //
    ColumnInfo column;
    {
      List<ColumnInfo> columns = portal.getColumns();
      column = columns.get(1);
      portal.command_MOVE(column, null);
    }
    assertEditor(
        "public class Test extends Portal {",
        "  public Test() {",
        "    super(3);",
        "    setColumnWidth(0, 100.0);",
        "    setColumnWidth(1, 300.0);",
        "    setColumnWidth(2, 200.0);",
        "    {",
        "      Portlet portlet_0 = new Portlet();",
        "      add(portlet_0, 0);",
        "    }",
        "    {",
        "      Portlet portlet_2 = new Portlet();",
        "      add(portlet_2, 1);",
        "    }",
        "    {",
        "      Portlet portlet_1 = new Portlet();",
        "      add(portlet_1, 2);",
        "    }",
        "  }",
        "}");
    assertEquals(2, portal.getColumns().indexOf(column));
  }

  /**
   * Test for {@link PortalInfo#command_MOVE(ColumnInfo, ColumnInfo)}.
   */
  public void test_column_MOVE_forward() throws Exception {
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(3);",
            "    setColumnWidth(0, 100.0);",
            "    setColumnWidth(1, 200.0);",
            "    setColumnWidth(2, 300.0);",
            "    {",
            "      Portlet portlet_0 = new Portlet();",
            "      add(portlet_0, 0);",
            "    }",
            "    {",
            "      Portlet portlet_1 = new Portlet();",
            "      add(portlet_1, 1);",
            "    }",
            "    {",
            "      Portlet portlet_2 = new Portlet();",
            "      add(portlet_2, 2);",
            "    }",
            "  }",
            "}");
    portal.refresh();
    //
    ColumnInfo column;
    {
      List<ColumnInfo> columns = portal.getColumns();
      column = columns.get(0);
      portal.command_MOVE(column, columns.get(2));
    }
    assertEditor(
        "public class Test extends Portal {",
        "  public Test() {",
        "    super(3);",
        "    setColumnWidth(0, 200.0);",
        "    setColumnWidth(1, 100.0);",
        "    setColumnWidth(2, 300.0);",
        "    {",
        "      Portlet portlet_1 = new Portlet();",
        "      add(portlet_1, 0);",
        "    }",
        "    {",
        "      Portlet portlet_0 = new Portlet();",
        "      add(portlet_0, 1);",
        "    }",
        "    {",
        "      Portlet portlet_2 = new Portlet();",
        "      add(portlet_2, 2);",
        "    }",
        "  }",
        "}");
    assertEquals(1, portal.getColumns().indexOf(column));
  }

  /**
   * Test for {@link PortalInfo#command_MOVE(ColumnInfo, ColumnInfo)}.
   */
  public void test_column_MOVE_backward() throws Exception {
    PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(4);",
            "    setColumnWidth(0, 100.0);",
            "    setColumnWidth(1, 200.0);",
            "    setColumnWidth(2, 300.0);",
            "    setColumnWidth(3, 400.0);",
            "    {",
            "      Portlet portlet_0 = new Portlet();",
            "      add(portlet_0, 0);",
            "    }",
            "    {",
            "      Portlet portlet_1 = new Portlet();",
            "      add(portlet_1, 1);",
            "    }",
            "    {",
            "      Portlet portlet_2 = new Portlet();",
            "      add(portlet_2, 2);",
            "    }",
            "    {",
            "      Portlet portlet_3 = new Portlet();",
            "      add(portlet_3, 3);",
            "    }",
            "  }",
            "}");
    portal.refresh();
    //
    ColumnInfo column;
    {
      List<ColumnInfo> columns = portal.getColumns();
      column = columns.get(2);
      portal.command_MOVE(column, columns.get(1));
    }
    assertEditor(
        "public class Test extends Portal {",
        "  public Test() {",
        "    super(4);",
        "    setColumnWidth(0, 100.0);",
        "    setColumnWidth(1, 300.0);",
        "    setColumnWidth(2, 200.0);",
        "    setColumnWidth(3, 400.0);",
        "    {",
        "      Portlet portlet_0 = new Portlet();",
        "      add(portlet_0, 0);",
        "    }",
        "    {",
        "      Portlet portlet_2 = new Portlet();",
        "      add(portlet_2, 1);",
        "    }",
        "    {",
        "      Portlet portlet_1 = new Portlet();",
        "      add(portlet_1, 2);",
        "    }",
        "    {",
        "      Portlet portlet_3 = new Portlet();",
        "      add(portlet_3, 3);",
        "    }",
        "  }",
        "}");
    assertEquals(1, portal.getColumns().indexOf(column));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Portlet: CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ColumnInfo#command_CREATE(PortletInfo, PortletInfo)}.
   */
  public void test_portlet_CREATE_withNext() throws Exception {
    final PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(3);",
            "    {",
            "      Portlet portlet_0 = new Portlet();",
            "      add(portlet_0, 0);",
            "    }",
            "    {",
            "      Portlet portlet_1 = new Portlet();",
            "      add(portlet_1, 1);",
            "    }",
            "    {",
            "      Portlet portlet_2 = new Portlet();",
            "      add(portlet_2, 2);",
            "    }",
            "  }",
            "}");
    portal.refresh();
    //
    PortletInfo portlet = createJavaInfo("com.extjs.gxt.ui.client.widget.custom.Portlet");
    PortletInfo nextPortlet = portal.getPortlets().get(1);
    portal.getColumns().get(1).command_CREATE(portlet, nextPortlet);
    assertEditor(
        "public class Test extends Portal {",
        "  public Test() {",
        "    super(3);",
        "    {",
        "      Portlet portlet_0 = new Portlet();",
        "      add(portlet_0, 0);",
        "    }",
        "    {",
        "      Portlet portlet = new Portlet();",
        "      portlet.setHeading('New Portlet');",
        "      portlet.setCollapsible(true);",
        "      add(portlet, 1);",
        "    }",
        "    {",
        "      Portlet portlet_1 = new Portlet();",
        "      add(portlet_1, 1);",
        "    }",
        "    {",
        "      Portlet portlet_2 = new Portlet();",
        "      add(portlet_2, 2);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link ColumnInfo#command_CREATE(PortletInfo, PortletInfo)}.
   */
  public void test_portlet_CREATE_noNext() throws Exception {
    final PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(3);",
            "    {",
            "      Portlet portlet_0 = new Portlet();",
            "      add(portlet_0, 0);",
            "    }",
            "    {",
            "      Portlet portlet_1 = new Portlet();",
            "      add(portlet_1, 1);",
            "    }",
            "    {",
            "      Portlet portlet_2 = new Portlet();",
            "      add(portlet_2, 2);",
            "    }",
            "  }",
            "}");
    portal.refresh();
    //
    PortletInfo portlet = createJavaInfo("com.extjs.gxt.ui.client.widget.custom.Portlet");
    portal.getColumns().get(1).command_CREATE(portlet, null);
    assertEditor(
        "public class Test extends Portal {",
        "  public Test() {",
        "    super(3);",
        "    {",
        "      Portlet portlet_0 = new Portlet();",
        "      add(portlet_0, 0);",
        "    }",
        "    {",
        "      Portlet portlet_1 = new Portlet();",
        "      add(portlet_1, 1);",
        "    }",
        "    {",
        "      Portlet portlet = new Portlet();",
        "      portlet.setHeading('New Portlet');",
        "      portlet.setCollapsible(true);",
        "      add(portlet, 1);",
        "    }",
        "    {",
        "      Portlet portlet_2 = new Portlet();",
        "      add(portlet_2, 2);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Portlet: MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ColumnInfo#command_MOVE(PortletInfo, PortletInfo)}.
   */
  public void test_portlet_MOVE_inSameColumn() throws Exception {
    final PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(1);",
            "    {",
            "      Portlet portlet_0_0 = new Portlet();",
            "      add(portlet_0_0, 0);",
            "    }",
            "    {",
            "      Portlet portlet_0_1 = new Portlet();",
            "      add(portlet_0_1, 0);",
            "    }",
            "  }",
            "}");
    portal.refresh();
    //
    ExecutionUtils.run(portal, new RunnableEx() {
      public void run() throws Exception {
        List<PortletInfo> portlets = portal.getPortlets();
        PortletInfo portlet = portlets.get(1);
        PortletInfo nextPortlet = portlets.get(0);
        portal.getColumns().get(0).command_MOVE(portlet, nextPortlet);
      }
    });
    assertEditor(
        "public class Test extends Portal {",
        "  public Test() {",
        "    super(1);",
        "    {",
        "      Portlet portlet_0_1 = new Portlet();",
        "      add(portlet_0_1, 0);",
        "    }",
        "    {",
        "      Portlet portlet_0_0 = new Portlet();",
        "      add(portlet_0_0, 0);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link ColumnInfo#command_MOVE(PortletInfo, PortletInfo)}.
   */
  public void test_portlet_MOVE_fromOtherColumn() throws Exception {
    final PortalInfo portal =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    super(3);",
            "    {",
            "      Portlet portlet_1 = new Portlet();",
            "      add(portlet_1, 1);",
            "    }",
            "    {",
            "      Portlet portlet_2 = new Portlet();",
            "      add(portlet_2, 2);",
            "    }",
            "  }",
            "}");
    portal.refresh();
    portal.startEdit();
    //
    ExecutionUtils.run(portal, new RunnableEx() {
      public void run() throws Exception {
        List<PortletInfo> portlets = portal.getPortlets();
        PortletInfo portlet = portlets.get(0);
        portal.getColumns().get(0).command_MOVE(portlet, null);
      }
    });
    assertEditor(
        "public class Test extends Portal {",
        "  public Test() {",
        "    super(3);",
        "    {",
        "      Portlet portlet_1 = new Portlet();",
        "      add(portlet_1, 0);",
        "    }",
        "    {",
        "      Portlet portlet_2 = new Portlet();",
        "      add(portlet_2, 2);",
        "    }",
        "  }",
        "}");
  }
}