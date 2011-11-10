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

import org.eclipse.wb.draw2d.IPositionConstants;

/**
 * Test for {@link DockLayoutPanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class DockLayoutPanelGefTest extends UiBinderGefTest {
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
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE_WEST() throws Exception {
    DockLayoutPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'/>",
            "</ui:UiBinder>");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, 10, 100).click();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:west size='1.0'>",
        "      <g:Button/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_CREATE_EAST() throws Exception {
    DockLayoutPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'/>",
            "</ui:UiBinder>");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, -10, 100).click();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:east size='1.0'>",
        "      <g:Button/>",
        "    </g:east>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_CREATE_CENTER() throws Exception {
    DockLayoutPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'/>",
            "</ui:UiBinder>");
    //
    loadButton();
    canvas.create();
    canvas.moveTo(panel, 0.5, 0.5).click();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:center>",
        "      <g:Button/>",
        "    </g:center>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_PASTE_onIt() throws Exception {
    DockLayoutPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'>",
            "    <g:west size='1.5'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:west>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    doCopyPaste(button);
    canvas.moveTo(panel, 0.9, 0.5).click();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:west size='1.5'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:west>",
        "    <g:east size='1.0'>",
        "      <g:Button/>",
        "    </g:east>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_MOVE() throws Exception {
    DockLayoutPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'>",
            "    <g:west size='2.0'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:west>",
            "  </g:DockLayoutPanel>",
            "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 0.5, -10).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:south size='2.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:south>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DockLayoutPanel wbp:name='panel' unit='CM' width='250px' height='200px'/>",
        "    <g:Button wbp:name='button' text='My Button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    ComplexPanelInfo panel = getObjectByName("panel");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 10, 0.5).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DockLayoutPanel wbp:name='panel' unit='CM' width='250px' height='200px'>",
        "      <g:west size='1.0'>",
        "        <g:Button wbp:name='button' text='My Button'/>",
        "      </g:west>",
        "    </g:DockLayoutPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // RESIZE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_resize_WEST() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='PX'>",
        "    <g:west size='150.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginResize(button, IPositionConstants.EAST);
    canvas.dragOn(50, 0).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='PX'>",
        "    <g:west size='200.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_resize_EAST() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='PX'>",
        "    <g:east size='150.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:east>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginResize(button, IPositionConstants.WEST);
    canvas.dragOn(-50, 0).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='PX'>",
        "    <g:east size='200.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:east>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_resize_NORTH() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='PX'>",
        "    <g:north size='150.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:north>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginResize(button, IPositionConstants.SOUTH);
    canvas.dragOn(0, 50).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='PX'>",
        "    <g:north size='200.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:north>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_resize_SOUTH() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='PX'>",
        "    <g:south size='150.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:south>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginResize(button, IPositionConstants.NORTH);
    canvas.dragOn(0, 50).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='PX'>",
        "    <g:south size='100.0'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:south>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE() throws Exception {
    DockLayoutPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockLayoutPanel unit='CM'/>",
            "</ui:UiBinder>");
    //
    WidgetInfo newButton = loadButton();
    tree.moveOn(panel).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:west size='1.0'>",
        "      <g:Button/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    tree.assertPrimarySelected(newButton);
  }

  public void test_tree_PASTE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:north size='1.5'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:north>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    doCopyPaste(button);
    tree.moveBefore(button).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:west size='1.0'>",
        "      <g:Button/>",
        "    </g:west>",
        "    <g:north size='1.5'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:north>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_MOVE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:west size='1.1'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:west>",
        "    <g:east size='1.2'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:east>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    //
    tree.startDrag(button_2).dragBefore(button_1).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockLayoutPanel unit='CM'>",
        "    <g:east size='1.2'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:east>",
        "    <g:west size='1.1'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:west>",
        "  </g:DockLayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DockLayoutPanel wbp:name='panel' unit='CM'/>",
        "    <g:Button wbp:name='button' text='My Button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    DockLayoutPanelInfo panel = getObjectByName("panel");
    WidgetInfo button = getObjectByName("button");
    //
    tree.startDrag(button).dragOn(panel).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DockLayoutPanel wbp:name='panel' unit='CM'>",
        "      <g:west size='1.0'>",
        "        <g:Button wbp:name='button' text='My Button'/>",
        "      </g:west>",
        "    </g:DockLayoutPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }
}
