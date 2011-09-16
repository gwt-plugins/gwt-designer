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

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link com.google.gwt.user.cellview.client.CellList}.
 * 
 * @author scheglov_ke
 */
public class CellListTest extends UiBinderModelTest {
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
            "  @UiField(provided=true) CellList cellList;",
            "  public Test() {",
            "    cellList = new CellList(new TextCell());",
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
        "  <g:AbsolutePanel>",
        "    <g:at left='0' top='0'>",
        "      <c:CellList wbp:name='cellList' ui:field='cellList'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
    refresh();
    CellListInfo cellList = getObjectByName("cellList");
    // we have actual object
    Object cellListObject = cellList.getObject();
    assertEquals(
        "com.google.gwt.user.cellview.client.CellList",
        cellListObject.getClass().getName());
    {
      Object cell = ReflectionUtils.invokeMethod(cellListObject, "getCell()");
      assertEquals("com.google.gwt.cell.client.TextCell", cell.getClass().getName());
    }
    // has reasonable size (we fill it with items)
    {
      Rectangle bounds = cellList.getBounds();
      assertThat(bounds.width).isGreaterThan(100).isLessThan(200);
      assertThat(bounds.height).isGreaterThan(80);
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
    CellListInfo newList = createObject("com.google.gwt.user.cellview.client.CellList");
    flowContainer_CREATE(panel, newList, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder xmlns:c='urn:import:com.google.gwt.user.cellview.client'>",
        "  <g:FlowPanel>",
        "    <c:CellList ui:field='cellList'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(
        "import com.google.gwt.user.cellview.client.CellList;",
        "import com.google.gwt.cell.client.AbstractCell;",
        "import com.google.gwt.cell.client.Cell.Context;",
        "import com.google.gwt.safehtml.shared.SafeHtmlBuilder;",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField(provided=true) CellList<Object> cellList = new CellList<Object>(new AbstractCell<Object>(){",
        "    @Override",
        "    public void render(Context context, Object value, SafeHtmlBuilder sb) {",
        "      // TODO",
        "    }",
        "  });",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }
}