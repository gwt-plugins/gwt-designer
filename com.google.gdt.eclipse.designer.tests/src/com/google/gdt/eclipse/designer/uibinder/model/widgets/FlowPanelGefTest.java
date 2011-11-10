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
 * Test for <code>com.google.gwt.user.client.ui.FlowPanel</code> widget in GEF.
 * 
 * @author scheglov_ke
 */
public class FlowPanelGefTest extends UiBinderGefTest {
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
    ComplexPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    //
    WidgetInfo newButton = loadButton();
    canvas.moveTo(panel, 0.5, 0.5).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    canvas.assertPrimarySelected(newButton);
  }

  public void test_canvas_PASTE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' text='The button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    //
    WidgetInfo button = getObjectByName("button");
    doCopyPaste(button);
    canvas.moveTo(button, 0.1, 0.5).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button text='The button'/>",
        "    <g:Button wbp:name='button' text='The button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_MOVE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button_1' text='Button 1'/>",
        "    <g:Button wbp:name='button_2' text='Button 2'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    //
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    canvas.beginDrag(button_2).dragTo(button_1, 0, 0.5).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button_2' text='Button 2'/>",
        "    <g:Button wbp:name='button_1' text='Button 1'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_ADD() throws Exception {
    ComplexPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:FlowPanel width='200px' height='200px'>",
            "      <g:Button wbp:name='button' text='The button'/>",
            "    </g:FlowPanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    //
    WidgetInfo button = getObjectByName("button");
    canvas.beginDrag(button).dragTo(panel, 0.9, 0.5).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:FlowPanel width='200px' height='200px'/>",
        "    <g:Button wbp:name='button' text='The button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE() throws Exception {
    ComplexPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>");
    //
    WidgetInfo newButton = loadButton();
    tree.moveOn(panel).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    tree.assertPrimarySelected(newButton);
  }

  public void test_tree_PASTE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' text='The button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    //
    WidgetInfo button = getObjectByName("button");
    doCopyPaste(button);
    tree.moveBefore(button).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button text='The button'/>",
        "    <g:Button wbp:name='button' text='The button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_MOVE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button_1' text='Button 1'/>",
        "    <g:Button wbp:name='button_2' text='Button 2'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    //
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    tree.startDrag(button_2).dragBefore(button_1).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button_2' text='Button 2'/>",
        "    <g:Button wbp:name='button_1' text='Button 1'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_ADD() throws Exception {
    ComplexPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel>",
            "    <g:FlowPanel width='200px' height='200px'>",
            "      <g:Button wbp:name='button' text='The button'/>",
            "    </g:FlowPanel>",
            "  </g:FlowPanel>",
            "</ui:UiBinder>");
    //
    WidgetInfo button = getObjectByName("button");
    tree.startDrag(button).dragOn(panel).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:FlowPanel width='200px' height='200px'/>",
        "    <g:Button wbp:name='button' text='The button'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }
}