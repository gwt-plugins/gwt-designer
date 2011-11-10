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

/**
 * Test for <code>com.google.gwt.user.client.ui.DockPanel</code> widget in GEF.
 * 
 * @author scheglov_ke
 */
public class DockPanelGefTest extends UiBinderGefTest {
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
  // Canvas.CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_CREATE_WEST() throws Exception {
    check_canvas_CREATE(0.1, 0.5, "WEST");
  }

  public void test_canvas_CREATE_NORTH() throws Exception {
    check_canvas_CREATE(0.5, 0.1, "NORTH");
  }

  public void test_canvas_CREATE_EAST() throws Exception {
    check_canvas_CREATE(0.9, 0.5, "EAST");
  }

  public void test_canvas_CREATE_SOUTH() throws Exception {
    check_canvas_CREATE(0.5, 0.9, "SOUTH");
  }

  public void test_canvas_CREATE_CENTER() throws Exception {
    check_canvas_CREATE(0.5, 0.5, "CENTER");
  }

  private void check_canvas_CREATE(double x, double y, String direction) throws Exception {
    DockPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockPanel/>",
            "</ui:UiBinder>");
    //
    loadButton();
    canvas.moveTo(panel, x, y).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockPanel>",
        "    <g:Dock direction='" + direction + "'>",
        "      <g:Button/>",
        "    </g:Dock>",
        "  </g:DockPanel>",
        "</ui:UiBinder>");
  }

  /**
   * Only one CENTER allowed.
   */
  public void test_canvas_CREATE_hasCENTER() throws Exception {
    DockPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockPanel>",
            "    <g:Dock direction='CENTER'>",
            "      <g:Button text='Existing CENTER'/>",
            "    </g:Dock>",
            "  </g:DockPanel>",
            "</ui:UiBinder>");
    //
    loadButton();
    canvas.moveTo(panel, 0.5, 0.5);
    canvas.assertCommandNull();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Canvas.PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_PASTE_onIt() throws Exception {
    DockPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockPanel>",
            "    <g:Dock direction='WEST'>",
            "      <g:Button wbp:name='button' text='The button'/>",
            "    </g:Dock>",
            "  </g:DockPanel>",
            "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    doCopyPaste(button);
    canvas.moveTo(panel, 0.9, 0.5).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockPanel>",
        "    <g:Dock direction='WEST'>",
        "      <g:Button wbp:name='button' text='The button'/>",
        "    </g:Dock>",
        "    <g:Dock direction='EAST'>",
        "      <g:Button text='The button'/>",
        "    </g:Dock>",
        "  </g:DockPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_PASTE_it() throws Exception {
    ComplexPanelInfo flowPanel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:DockPanel wbp:name='panel'>",
            "      <g:Dock direction='WEST'>",
            "        <g:Button/>",
            "      </g:Dock>",
            "    </g:DockPanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    // do copy/paste
    DockPanelInfo panel = getObjectByName("panel");
    doCopyPaste(panel);
    canvas.moveTo(flowPanel, 0.9, 0.1).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DockPanel wbp:name='panel'>",
        "      <g:Dock direction='WEST'>",
        "        <g:Button/>",
        "      </g:Dock>",
        "    </g:DockPanel>",
        "    <g:DockPanel>",
        "      <g:Dock direction='WEST'>",
        "        <g:Button/>",
        "      </g:Dock>",
        "    </g:DockPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canvas_MOVE() throws Exception {
    DockPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockPanel>",
            "    <g:Dock direction='WEST'>",
            "      <g:Button wbp:name='button' text='The button'/>",
            "    </g:Dock>",
            "  </g:DockPanel>",
            "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 0.9, 0.5).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockPanel>",
        "    <g:Dock direction='EAST'>",
        "      <g:Button wbp:name='button' text='The button'/>",
        "    </g:Dock>",
        "  </g:DockPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "    <g:DockPanel wbp:name='panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    DockPanelInfo panel = getObjectByName("panel");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 0.1, 0.5).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DockPanel wbp:name='panel'>",
        "      <g:Dock direction='WEST'>",
        "        <g:Button wbp:name='button'/>",
        "      </g:Dock>",
        "    </g:DockPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE() throws Exception {
    DockPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:DockPanel/>",
            "</ui:UiBinder>");
    //
    WidgetInfo newButton = loadButton();
    tree.moveOn(panel).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockPanel>",
        "    <g:Dock direction='WEST'>",
        "      <g:Button/>",
        "    </g:Dock>",
        "  </g:DockPanel>",
        "</ui:UiBinder>");
    tree.assertPrimarySelected(newButton);
  }

  public void test_tree_PASTE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockPanel>",
        "    <g:Dock direction='EAST'>",
        "      <g:Button wbp:name='button' text='The button'/>",
        "    </g:Dock>",
        "  </g:DockPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    doCopyPaste(button);
    tree.moveBefore(button).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockPanel>",
        "    <g:Dock direction='WEST'>",
        "      <g:Button text='The button'/>",
        "    </g:Dock>",
        "    <g:Dock direction='EAST'>",
        "      <g:Button wbp:name='button' text='The button'/>",
        "    </g:Dock>",
        "  </g:DockPanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_MOVE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockPanel>",
        "    <g:Dock direction='WEST'>",
        "      <g:Button wbp:name='button_1' text='Button 1'/>",
        "    </g:Dock>",
        "    <g:Dock direction='EAST'>",
        "      <g:Button wbp:name='button_2' text='Button 2'/>",
        "    </g:Dock>",
        "  </g:DockPanel>",
        "</ui:UiBinder>");
    //
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    tree.startDrag(button_2).dragBefore(button_1).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:DockPanel>",
        "    <g:Dock direction='EAST'>",
        "      <g:Button wbp:name='button_2' text='Button 2'/>",
        "    </g:Dock>",
        "    <g:Dock direction='WEST'>",
        "      <g:Button wbp:name='button_1' text='Button 1'/>",
        "    </g:Dock>",
        "  </g:DockPanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "    <g:DockPanel wbp:name='panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    DockPanelInfo panel = getObjectByName("panel");
    WidgetInfo button = getObjectByName("button");
    //
    tree.startDrag(button).dragOn(panel).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DockPanel wbp:name='panel'>",
        "      <g:Dock direction='WEST'>",
        "        <g:Button wbp:name='button'/>",
        "      </g:Dock>",
        "    </g:DockPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }
}