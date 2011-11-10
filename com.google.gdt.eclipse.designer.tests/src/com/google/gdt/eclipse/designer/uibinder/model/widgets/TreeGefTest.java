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

import org.eclipse.wb.gef.core.tools.CreationTool;

/**
 * Test for {@link TreeInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class TreeGefTest extends UiBinderGefTest {
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
  public void test_canvas_CREATE() throws Exception {
    TreeInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:Tree/>",
            "</ui:UiBinder>");
    //
    loadTreeItem();
    canvas.moveTo(panel, 10, 10);
    canvas.assertCommandNull();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE_item_last() throws Exception {
    TreeInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:Tree/>",
            "</ui:UiBinder>");
    //
    loadTreeItem();
    tree.moveOn(panel).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem text='New TreeItem'/>",
        "  </g:Tree>",
        "</ui:UiBinder>");
  }

  public void test_tree_CREATE_item_beforeWidget() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:Button wbp:name='button'/>",
        "  </g:Tree>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    loadTreeItem();
    tree.moveBefore(button).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem text='New TreeItem'/>",
        "    <g:Button wbp:name='button'/>",
        "  </g:Tree>",
        "</ui:UiBinder>");
  }

  public void test_tree_CREATE_widget() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem text='TreeItem' wbp:name='item'/>",
        "  </g:Tree>",
        "</ui:UiBinder>");
    TreeItemInfo item = getObjectByName("item");
    //
    loadButton();
    tree.moveBefore(item).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:Button/>",
        "    <g:TreeItem text='TreeItem' wbp:name='item'/>",
        "  </g:Tree>",
        "</ui:UiBinder>");
  }

  public void test_tree_PASTE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem wbp:name='item' text='my TreeItem'/>",
        "  </g:Tree>",
        "</ui:UiBinder>");
    TreeItemInfo item = getObjectByName("item");
    //
    doCopyPaste(item);
    tree.moveAfter(item).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem wbp:name='item' text='my TreeItem'/>",
        "    <g:TreeItem text='my TreeItem'/>",
        "  </g:Tree>",
        "</ui:UiBinder>");
  }

  public void test_tree_MOVE_item() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem wbp:name='item_1' text='AAA'/>",
        "    <g:TreeItem wbp:name='item_2' text='BBB'/>",
        "    <g:TreeItem wbp:name='item_3' text='CCC'/>",
        "  </g:Tree>",
        "</ui:UiBinder>");
    TreeItemInfo item_1 = getObjectByName("item_1");
    TreeItemInfo item_3 = getObjectByName("item_3");
    //
    tree.startDrag(item_3).dragBefore(item_1).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem wbp:name='item_3' text='CCC'/>",
        "    <g:TreeItem wbp:name='item_1' text='AAA'/>",
        "    <g:TreeItem wbp:name='item_2' text='BBB'/>",
        "  </g:Tree>",
        "</ui:UiBinder>");
  }

  public void test_tree_MOVE_widget() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem wbp:name='item_1' text='AAA'/>",
        "    <g:TreeItem wbp:name='item_2' text='BBB'/>",
        "    <g:Button wbp:name='button'/>",
        "  </g:Tree>",
        "</ui:UiBinder>");
    TreeItemInfo item_1 = getObjectByName("item_1");
    WidgetInfo button = getObjectByName("button");
    //
    tree.startDrag(button).dragBefore(item_1).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:Button wbp:name='button'/>",
        "    <g:TreeItem wbp:name='item_1' text='AAA'/>",
        "    <g:TreeItem wbp:name='item_2' text='BBB'/>",
        "  </g:Tree>",
        "</ui:UiBinder>");
  }

  public void test_tree_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Tree>",
        "      <g:TreeItem text='AAA'/>",
        "      <g:TreeItem text='BBB' wbp:name='item'/>",
        "      <g:TreeItem text='CCC'/>",
        "    </g:Tree>",
        "    <g:Tree wbp:name='panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    TreeInfo panel = getObjectByName("panel");
    TreeItemInfo item = getObjectByName("item");
    //
    tree.startDrag(item).dragOn(panel).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Tree>",
        "      <g:TreeItem text='AAA'/>",
        "      <g:TreeItem text='CCC'/>",
        "    </g:Tree>",
        "    <g:Tree wbp:name='panel'>",
        "      <g:TreeItem text='BBB' wbp:name='item'/>",
        "    </g:Tree>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TreeItem
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_treeItem_CREATE_item_last() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem text='Existing' wbp:name='item'/>",
        "  </g:Tree>",
        "</ui:UiBinder>");
    TreeItemInfo item = getObjectByName("item");
    //
    loadTreeItem();
    tree.moveOn(item).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem text='Existing' wbp:name='item' state='true'>",
        "      <g:TreeItem text='New TreeItem'/>",
        "    </g:TreeItem>",
        "  </g:Tree>",
        "</ui:UiBinder>");
  }

  public void test_treeItem_CREATE_item_beforeWidget() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem wbp:name='item' state='true'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:TreeItem>",
        "  </g:Tree>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    loadTreeItem();
    tree.moveBefore(button).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem wbp:name='item' state='true'>",
        "      <g:TreeItem text='New TreeItem'/>",
        "      <g:Button wbp:name='button'/>",
        "    </g:TreeItem>",
        "  </g:Tree>",
        "</ui:UiBinder>");
  }

  public void test_treeItem_CREATE_widget() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem state='true'>",
        "      <g:TreeItem  wbp:name='item'/>",
        "    </g:TreeItem>",
        "  </g:Tree>",
        "</ui:UiBinder>");
    TreeItemInfo item = getObjectByName("item");
    //
    loadButton();
    tree.moveBefore(item).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem state='true'>",
        "      <g:Button/>",
        "      <g:TreeItem  wbp:name='item'/>",
        "    </g:TreeItem>",
        "  </g:Tree>",
        "</ui:UiBinder>");
  }

  public void test_treeItem_MOVE_item() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem state='true'>",
        "      <g:TreeItem wbp:name='item_1' text='AAA'/>",
        "      <g:TreeItem wbp:name='item_2' text='BBB'/>",
        "      <g:TreeItem wbp:name='item_3' text='CCC'/>",
        "    </g:TreeItem>",
        "  </g:Tree>",
        "</ui:UiBinder>");
    TreeItemInfo item_1 = getObjectByName("item_1");
    TreeItemInfo item_3 = getObjectByName("item_3");
    //
    tree.startDrag(item_3).dragBefore(item_1).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem state='true'>",
        "      <g:TreeItem wbp:name='item_3' text='CCC'/>",
        "      <g:TreeItem wbp:name='item_1' text='AAA'/>",
        "      <g:TreeItem wbp:name='item_2' text='BBB'/>",
        "    </g:TreeItem>",
        "  </g:Tree>",
        "</ui:UiBinder>");
  }

  public void test_treeItem_MOVE_widget() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem state='true'>",
        "      <g:TreeItem wbp:name='item_1' text='AAA'/>",
        "      <g:TreeItem wbp:name='item_2' text='BBB'/>",
        "    <g:Button wbp:name='button'/>",
        "    </g:TreeItem>",
        "  </g:Tree>",
        "</ui:UiBinder>");
    TreeItemInfo item_1 = getObjectByName("item_1");
    WidgetInfo button = getObjectByName("button");
    //
    tree.startDrag(button).dragBefore(item_1).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:Tree>",
        "    <g:TreeItem state='true'>",
        "      <g:Button wbp:name='button'/>",
        "      <g:TreeItem wbp:name='item_1' text='AAA'/>",
        "      <g:TreeItem wbp:name='item_2' text='BBB'/>",
        "    </g:TreeItem>",
        "  </g:Tree>",
        "</ui:UiBinder>");
  }

  public void test_treeItem_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Tree>",
        "      <g:TreeItem wbp:name='item'/>",
        "    </g:Tree>",
        "    <g:Button wbp:name='button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    TreeItemInfo item = getObjectByName("item");
    //
    tree.startDrag(button).dragOn(item).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Tree>",
        "      <g:TreeItem wbp:name='item' state='true'>",
        "        <g:Button wbp:name='button'/>",
        "      </g:TreeItem>",
        "    </g:Tree>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tool
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Loads {@link CreationTool} with <code>TreeItem</code>.
   */
  protected final TreeItemInfo loadTreeItem() throws Exception {
    return loadCreationTool("com.google.gwt.user.client.ui.TreeItem");
  }
}