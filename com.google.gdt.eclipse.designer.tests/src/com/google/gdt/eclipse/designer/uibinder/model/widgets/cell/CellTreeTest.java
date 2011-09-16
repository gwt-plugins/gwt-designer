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
 * Test for {@link com.google.gwt.user.cellview.client.CellTree}.
 * 
 * @author scheglov_ke
 */
public class CellTreeTest extends UiBinderModelTest {
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
            "  @UiField(provided=true) CellTree cellTree;",
            "  public Test() {",
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
        "      <c:CellTree wbp:name='cellTree' ui:field='cellTree'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo cellTree = getObjectByName("cellTree");
    // we have actual object
    Object cellTreeObject = cellTree.getObject();
    assertEquals(
        "com.google.gwt.user.cellview.client.CellTree",
        cellTreeObject.getClass().getName());
    // has reasonable size (we fill it with sample content)
    {
      Rectangle bounds = cellTree.getBounds();
      assertThat(bounds.width).isGreaterThan(300).isLessThan(400);
      assertThat(bounds.height).isGreaterThan(45);
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
    WidgetInfo newTree = createObject("com.google.gwt.user.cellview.client.CellTree");
    flowContainer_CREATE(panel, newTree, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder xmlns:c='urn:import:com.google.gwt.user.cellview.client'>",
        "  <g:FlowPanel>",
        "    <c:CellTree ui:field='cellTree'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(
        "import com.google.gwt.user.cellview.client.CellTree;",
        "import com.google.gwt.view.client.TreeViewModel;",
        "import com.google.gwt.view.client.AbstractDataProvider;",
        "import com.google.gwt.view.client.ListDataProvider;",
        "import com.google.gwt.view.client.SelectionModel.AbstractSelectionModel;",
        "import com.google.gwt.view.client.NoSelectionModel;",
        "import com.google.gwt.view.client.TreeViewModel.NodeInfo;",
        "import com.google.gwt.view.client.TreeViewModel.DefaultNodeInfo;",
        "import com.google.gwt.cell.client.TextCell;",
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField(provided=true) CellTree cellTree = new CellTree(",
        "    new TreeViewModel() {",
        "      final AbstractDataProvider<String> dataProvider = new ListDataProvider<String>();",
        "      final AbstractSelectionModel<String> selectionModel = new NoSelectionModel<String>();",
        "      @Override",
        "      public <T> NodeInfo<?> getNodeInfo(T value) {",
        "        return new DefaultNodeInfo<String>(dataProvider, new TextCell(), selectionModel, null);",
        "      }",
        "      @Override",
        "      public boolean isLeaf(Object value) {",
        "        return true;",
        "      }",
        "    }, null);",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }
}