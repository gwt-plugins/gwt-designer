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

/**
 * Test for {@link com.google.gwt.user.cellview.client.CellBrowser}.
 * 
 * @author scheglov_ke
 */
public class CellBrowserTest extends UiBinderModelTest {
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
            "  @UiField(provided=true) CellBrowser cellBrowser;",
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
        "      <c:CellBrowser wbp:name='cellBrowser' ui:field='cellBrowser'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo cellBrowser = getObjectByName("cellBrowser");
    // we have actual object
    Object cellBrowserObject = cellBrowser.getObject();
    assertEquals(
        "com.google.gwt.user.cellview.client.CellBrowser",
        cellBrowserObject.getClass().getName());
    // no check for size, because CellBrowser does not resize intself to show content
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
    WidgetInfo newBrowser = createObject("com.google.gwt.user.cellview.client.CellBrowser");
    flowContainer_CREATE(panel, newBrowser, null);
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder xmlns:c='urn:import:com.google.gwt.user.cellview.client'>",
        "  <g:FlowPanel>",
        "    <c:CellBrowser ui:field='cellBrowser'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    assertJava(
        "import com.google.gwt.user.cellview.client.CellBrowser;",
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
        "  @UiField(provided=true) CellBrowser cellBrowser = new CellBrowser(",
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