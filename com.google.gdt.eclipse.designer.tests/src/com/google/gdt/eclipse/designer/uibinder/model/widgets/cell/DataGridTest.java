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
package com.google.gdt.eclipse.designer.uibinder.model.widgets.cell;

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.util.ScriptUtils;

/**
 * Test for {@link com.google.gwt.user.cellview.client.DataGrid}.
 * 
 * @author scheglov_ke
 */
public class DataGridTest extends UiBinderModelTest {
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
  public void test_parseProvided() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "import com.google.gwt.cell.client.*;",
            "import com.google.gwt.user.cellview.client.*;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField(provided=true) DataGrid DataGrid;",
            "  public Test() {",
            "    DataGrid = new DataGrid();",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder xmlns:c='urn:import:com.google.gwt.user.cellview.client'>",
        "  <g:FlowPanel>",
        "      <c:DataGrid wbp:name='DataGrid' ui:field='dataGrid'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo dataGrid = getObjectByName("DataGrid");
    // we have actual object
    Object dataGridObject = dataGrid.getObject();
    assertEquals(
        "com.google.gwt.user.cellview.client.DataGrid",
        dataGridObject.getClass().getName());
    // we fill it with columns and items
    {
      assertEquals(3, ScriptUtils.evaluate("getColumnCount()", dataGridObject));
      assertEquals(5, ScriptUtils.evaluate("getRowCount()", dataGridObject));
    }
  }

  /**
   * We should support creation of custom "DataGrid" subclasses.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48616
   */
  public void test_parseProvided_customSubclass() throws Exception {
    setFileContentSrc(
        "test/client/MyDataGrid.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.cellview.client.*;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyDataGrid extends DataGrid {",
            "}"));
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "import com.google.gwt.cell.client.*;",
            "import com.google.gwt.user.cellview.client.*;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField(provided=true) MyDataGrid dataGrid;",
            "  public Test() {",
            "    dataGrid = new MyDataGrid();",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder xmlns:c='urn:import:com.google.gwt.user.cellview.client'>",
        "  <g:FlowPanel>",
        "      <c:DataGrid wbp:name='DataGrid' ui:field='dataGrid'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo dataGrid = getObjectByName("DataGrid");
    // we have actual object
    Object dataGridObject = dataGrid.getObject();
    assertEquals("test.client.MyDataGrid", dataGridObject.getClass().getName());
    // we fill it with columns and items
    {
      assertEquals(3, ScriptUtils.evaluate("getColumnCount()", dataGridObject));
      assertEquals(5, ScriptUtils.evaluate("getRowCount()", dataGridObject));
    }
  }

  public void test_CREATE() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    dontUseSharedGWTState();
    ComplexPanelInfo panel =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder xmlns:c='urn:import:com.google.gwt.user.cellview.client'>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    refresh();
    //
    DataGridInfo newGrid = createObject("com.google.gwt.user.cellview.client.DataGrid");
    flowContainer_CREATE(panel, newGrid, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder xmlns:c='urn:import:com.google.gwt.user.cellview.client'>",
        "  <g:FlowPanel>",
        "    <c:DataGrid width='250px' height='200px' ui:field='dataGrid'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(
        "import com.google.gwt.user.cellview.client.DataGrid;",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField(provided=true) DataGrid<Object> dataGrid = new DataGrid<Object>();",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }
}