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
package com.google.gdt.eclipse.designer.uibinder.model.widgets.cell;

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link com.google.gwt.user.cellview.client.CellTable}.
 * 
 * @author scheglov_ke
 */
public class CellTableTest extends UiBinderModelTest {
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
            "  @UiField(provided=true) CellTable cellTable;",
            "  public Test() {",
            "    cellTable = new CellTable();",
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
        "      <c:CellTable wbp:name='cellTable' ui:field='cellTable'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo cellTable = getObjectByName("cellTable");
    // we have actual object
    Object cellTableObject = cellTable.getObject();
    assertEquals(
        "com.google.gwt.user.cellview.client.CellTable",
        cellTableObject.getClass().getName());
    // has reasonable size (we fill it with columns and items)
    {
      Rectangle bounds = cellTable.getBounds();
      assertThat(bounds.width).isGreaterThan(200).isLessThan(300);
      assertThat(bounds.height).isGreaterThan(150);
    }
  }

  /**
   * We should support creation of custom "CellTable" subclasses.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?48616
   */
  public void test_parseProvided_customSubclass() throws Exception {
    setFileContentSrc(
        "test/client/MyCellTable.java",
        getSourceDQ(
            "package test.client;",
            "import com.google.gwt.user.cellview.client.*;",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyCellTable extends CellTable {",
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
            "  @UiField(provided=true) MyCellTable cellTable;",
            "  public Test() {",
            "    cellTable = new MyCellTable();",
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
        "      <c:CellTable wbp:name='cellTable' ui:field='cellTable'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo cellTable = getObjectByName("cellTable");
    // we have actual object
    Object cellTableObject = cellTable.getObject();
    assertEquals("test.client.MyCellTable", cellTableObject.getClass().getName());
    // has reasonable size (we fill it with columns and items)
    {
      Rectangle bounds = cellTable.getBounds();
      assertThat(bounds.width).isGreaterThan(200).isLessThan(300);
      assertThat(bounds.height).isGreaterThan(150);
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
    CellTableInfo newTable = createObject("com.google.gwt.user.cellview.client.CellTable");
    flowContainer_CREATE(panel, newTable, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder xmlns:c='urn:import:com.google.gwt.user.cellview.client'>",
        "  <g:FlowPanel>",
        "    <c:CellTable ui:field='cellTable'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(
        "import com.google.gwt.user.cellview.client.CellTable;",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField(provided=true) CellTable<Object> cellTable = new CellTable<Object>();",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }
}