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
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

import org.eclipse.wb.draw2d.IPositionConstants;

/**
 * Test for {@link AbsolutePanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class AbsolutePanelGefTest extends UiBinderGefTest {
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
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean m_initialized = false;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (!m_initialized) {
      UiBinderContext.disposeSharedGWTState();
      prepareBox();
      forgetCreatedResources();
      m_initialized = true;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE() throws Exception {
    AbsolutePanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:AbsolutePanel/>",
            "</ui:UiBinder>");
    //
    loadCreationBox();
    canvas.sideMode().create(100, 50);
    canvas.moveTo(panel, 30, 60);
    canvas.click();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='30' top='60'>",
        "      <t:Box/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_PASTE() throws Exception {
    AbsolutePanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:AbsolutePanel>",
            "    <g:at left='5' top='5'>",
            "      <t:Box wbp:name='existing'/>",
            "    </g:at>",
            "  </g:AbsolutePanel>",
            "</ui:UiBinder>");
    // do copy/paste
    {
      WidgetInfo existing = getObjectByName("existing");
      doCopyPaste(existing);
    }
    canvas.sideMode().create(100, 50);
    canvas.moveTo(panel, 200, 100).click();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='5' top='5'>",
        "      <t:Box wbp:name='existing'/>",
        "    </g:at>",
        "    <g:at left='200' top='100'>",
        "      <t:Box width='100px' height='50px'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_MOVE() throws Exception {
    AbsolutePanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:AbsolutePanel>",
            "    <g:at left='5' top='5'>",
            "      <t:Box wbp:name='box'/>",
            "    </g:at>",
            "  </g:AbsolutePanel>",
            "</ui:UiBinder>");
    WidgetInfo box = getObjectByName("box");
    //
    canvas.sideMode().beginMove(box);
    canvas.dragTo(panel, 30, 40).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='30' top='40'>",
        "      <t:Box wbp:name='box'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:AbsolutePanel wbp:name='panel' width='200px' height='150px'/>",
        "    <t:Box wbp:name='box'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    ComplexPanelInfo panel = getObjectByName("panel");
    WidgetInfo box = getObjectByName("box");
    //
    canvas.sideMode().beginMove(box);
    canvas.dragTo(panel, 20, 30).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:AbsolutePanel wbp:name='panel' width='200px' height='150px'>",
        "      <g:at left='20' top='30'>",
        "        <t:Box wbp:name='box'/>",
        "      </g:at>",
        "    </g:AbsolutePanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // RESIZE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_resize_WEST() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='150' top='100'>",
        "      <t:Box wbp:name='box' width='100px' height='50px'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
    WidgetInfo box = getObjectByName("box");
    //
    canvas.beginResize(box, IPositionConstants.WEST);
    canvas.dragOn(-25, 0).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='125' top='100'>",
        "      <t:Box wbp:name='box' width='125px' height='50px'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  public void test_resize_EAST() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='150' top='100'>",
        "      <t:Box wbp:name='box' width='100px' height='50px'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
    WidgetInfo box = getObjectByName("box");
    //
    canvas.beginResize(box, IPositionConstants.EAST);
    canvas.dragOn(25, 0).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='150' top='100'>",
        "      <t:Box wbp:name='box' width='125px' height='50px'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  public void test_resize_NORTH() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='150' top='100'>",
        "      <t:Box wbp:name='box' width='100px' height='50px'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
    WidgetInfo box = getObjectByName("box");
    //
    canvas.beginResize(box, IPositionConstants.NORTH);
    canvas.dragOn(0, 25).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='150' top='125'>",
        "      <t:Box wbp:name='box' width='100px' height='25px'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  public void test_resize_SOUTH() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='150' top='100'>",
        "      <t:Box wbp:name='box' width='100px' height='50px'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
    WidgetInfo box = getObjectByName("box");
    //
    canvas.beginResize(box, IPositionConstants.SOUTH);
    canvas.dragOn(0, 25).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='150' top='100'>",
        "      <t:Box wbp:name='box' width='100px' height='75px'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE() throws Exception {
    AbsolutePanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:AbsolutePanel/>",
            "</ui:UiBinder>");
    //
    loadCreationBox();
    tree.moveOn(panel).click();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='0' top='0'>",
        "      <t:Box/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_PASTE() throws Exception {
    AbsolutePanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:AbsolutePanel>",
            "    <g:at left='5' top='5'>",
            "      <t:Box wbp:name='existing'/>",
            "    </g:at>",
            "  </g:AbsolutePanel>",
            "</ui:UiBinder>");
    // do copy/paste
    {
      WidgetInfo existing = getObjectByName("existing");
      doCopyPaste(existing);
    }
    tree.moveOn(panel).click();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='5' top='5'>",
        "      <t:Box wbp:name='existing'/>",
        "    </g:at>",
        "    <g:at left='0' top='0'>",
        "      <t:Box/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_MOVE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='5' top='5'>",
        "      <t:Box wbp:name='box_1'/>",
        "    </g:at>",
        "    <g:at left='5' top='100'>",
        "      <t:Box wbp:name='box_2'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
    WidgetInfo box_1 = getObjectByName("box_1");
    WidgetInfo box_2 = getObjectByName("box_2");
    //
    tree.startDrag(box_2).dragBefore(box_1).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:AbsolutePanel>",
        "    <g:at left='5' top='100'>",
        "      <t:Box wbp:name='box_2'/>",
        "    </g:at>",
        "    <g:at left='5' top='5'>",
        "      <t:Box wbp:name='box_1'/>",
        "    </g:at>",
        "  </g:AbsolutePanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:AbsolutePanel wbp:name='panel' width='200px' height='150px'/>",
        "    <t:Box wbp:name='box'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    ComplexPanelInfo panel = getObjectByName("panel");
    WidgetInfo box = getObjectByName("box");
    //
    tree.startDrag(box).dragOn(panel).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:AbsolutePanel wbp:name='panel' width='200px' height='150px'>",
        "      <g:at left='0' top='0'>",
        "        <t:Box wbp:name='box'/>",
        "      </g:at>",
        "    </g:AbsolutePanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void test_tearDown() throws Exception {
    super.test_tearDown();
    UiBinderContext.disposeSharedGWTState();
  }
}