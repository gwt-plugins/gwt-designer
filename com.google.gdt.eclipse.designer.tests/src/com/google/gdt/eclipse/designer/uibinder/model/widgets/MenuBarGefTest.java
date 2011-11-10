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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.uibinder.gef.UiBinderGefTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.menu.MenuBarInfo;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.menu.MenuItemInfo;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.menu.MenuItemSeparatorInfo;

/**
 * Test for {@link MenuBarInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class MenuBarGefTest extends UiBinderGefTest {
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
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void ___test_canvas_CREATE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='bar'>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    MenuBarInfo panel = getObjectByName("bar");
    //
    loadButton();
    canvas.moveTo(panel, 10, 10).click();
    assertXML(
        "<ui:UiBinder>",
        "  <g:StackLayoutPanel>",
        "    <g:stack>",
        "      <g:header size='2'>New widget</g:header>",
        "      <g:Button/>",
        "    </g:stack>",
        "  </g:StackLayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE_MenuBar() throws Exception {
    ComplexPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    //
    loadCreationTool("com.google.gwt.user.client.ui.MenuBar");
    canvas.moveTo(panel, 0.5, 0.5).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_CREATE_MenuItem() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='menuBar'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    MenuBarInfo menuBar = getObjectByName("menuBar");
    //
    MenuItemInfo newItem = loadCreationTool("com.google.gwt.user.client.ui.MenuItem");
    canvas.moveTo(menuBar, 5, 0.5).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='menuBar'>",
        "      <g:MenuItem text='New item'/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    canvas.assertNotNullEditPart(newItem);
    canvas.assertPrimarySelected(newItem);
  }

  public void test_canvas_CREATE_MenuItemSeparator() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='menuBar'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    MenuBarInfo menuBar = getObjectByName("menuBar");
    //
    MenuItemSeparatorInfo newSeparator =
        loadCreationTool("com.google.gwt.user.client.ui.MenuItemSeparator");
    canvas.moveTo(menuBar, 5, 0.5).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='menuBar'>",
        "      <g:MenuItemSeparator/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    canvas.assertNotNullEditPart(newSeparator);
    canvas.assertPrimarySelected(newSeparator);
  }

  public void test_canvas_CREATE_subMenu() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='menuBar'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    MenuBarInfo menuBar = getObjectByName("menuBar");
    //
    loadCreationTool("com.google.gwt.user.client.ui.MenuItem", "withSubMenu");
    canvas.moveTo(menuBar, 5, 0.5).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='menuBar'>",
        "      <g:MenuItem text='New menu'>",
        "        <g:MenuBar vertical='true'/>",
        "      </g:MenuItem>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_CREATE_MenuItem_intoSubMenu() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar>",
        "      <g:MenuItem wbp:name='subMenuItem' text='My menu'>",
        "        <g:MenuBar wbp:name='subMenu' vertical='true'/>",
        "      </g:MenuItem>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    MenuItemInfo subMenuItem = getObjectByName("subMenuItem");
    MenuBarInfo subMenu = getObjectByName("subMenu");
    //
    loadCreationTool("com.google.gwt.user.client.ui.MenuItem");
    // initially no EditPart for "subMenu"
    canvas.assertNullEditPart(subMenu);
    // move on "subMenuItem", show "subMenu"
    canvas.moveTo(subMenuItem, 0.5, 0.5);
    canvas.assertNotNullEditPart(subMenu);
    // move on "subMenu" and click
    canvas.moveTo(subMenu, 0.5, 0.5).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar>",
        "      <g:MenuItem wbp:name='subMenuItem' text='My menu'>",
        "        <g:MenuBar wbp:name='subMenu' vertical='true'>",
        "          <g:MenuItem text='New item'/>",
        "        </g:MenuBar>",
        "      </g:MenuItem>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE_MenuBar() throws Exception {
    ComplexPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    //
    loadCreationTool("com.google.gwt.user.client.ui.MenuBar");
    tree.moveOn(panel).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_CREATE_MenuItem() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='menuBar'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    MenuBarInfo menuBar = getObjectByName("menuBar");
    //
    loadCreationTool("com.google.gwt.user.client.ui.MenuItem");
    tree.moveOn(menuBar).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:MenuBar wbp:name='menuBar'>",
        "      <g:MenuItem text='New item'/>",
        "    </g:MenuBar>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }
}