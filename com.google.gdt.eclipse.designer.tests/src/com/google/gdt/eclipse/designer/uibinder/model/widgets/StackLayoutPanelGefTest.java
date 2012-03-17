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

import static com.google.gdt.eclipse.designer.uibinder.model.widgets.StackLayoutPanelInfo.isVisible;

import com.google.gdt.eclipse.designer.uibinder.gef.UiBinderGefTest;

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;

/**
 * Test for {@link StackLayoutPanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class StackLayoutPanelGefTest extends UiBinderGefTest {
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
    StackLayoutPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackLayoutPanel/>",
            "</ui:UiBinder>");
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

  public void test_canvas_PASTE() throws Exception {
    StackLayoutPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackLayoutPanel unit='EM'>",
            "    <g:stack>",
            "      <g:header size='2'>AAA</g:header>",
            "      <g:Button wbp:name='button' text='A'/>",
            "    </g:stack>",
            "  </g:StackLayoutPanel>",
            "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    doCopyPaste(button);
    canvas.moveTo(panel, 0, 0).click();
    assertXML(
        "<ui:UiBinder>",
        "  <g:StackLayoutPanel unit='EM'>",
        "    <g:stack>",
        "      <g:header size='2'>New widget</g:header>",
        "      <g:Button text='A'/>",
        "    </g:stack>",
        "    <g:stack>",
        "      <g:header size='2'>AAA</g:header>",
        "      <g:Button wbp:name='button' text='A'/>",
        "    </g:stack>",
        "  </g:StackLayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_MOVE_widget() throws Exception {
    StackLayoutPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackLayoutPanel unit='EM'>",
            "    <g:stack>",
            "      <g:header size='2'>AAA</g:header>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:stack>",
            "    <g:stack>",
            "      <g:header size='2'>BBB</g:header>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:stack>",
            "  </g:StackLayoutPanel>",
            "</ui:UiBinder>");
    WidgetInfo button_2 = getObjectByName("button_2");
    //
    tree.select(button_2);
    canvas.beginDrag(button_2).dragTo(panel, 10, 0).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackLayoutPanel unit='EM'>",
        "    <g:stack>",
        "      <g:header size='2'>BBB</g:header>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:stack>",
        "    <g:stack>",
        "      <g:header size='2'>AAA</g:header>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:stack>",
        "  </g:StackLayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_MOVE_header_before() throws Exception {
    StackLayoutPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackLayoutPanel unit='EM'>",
            "    <g:stack>",
            "      <g:header size='2'>AAA</g:header>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:stack>",
            "    <g:stack>",
            "      <g:header size='2'>BBB</g:header>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:stack>",
            "  </g:StackLayoutPanel>",
            "</ui:UiBinder>");
    // drag header of "button_2"
    {
      Rectangle bounds = panel.getWidgetHandles().get(1).getBounds();
      Point handleCenter = bounds.getCenter();
      canvas.moveTo(panel, handleCenter.x, handleCenter.y);
      canvas.beginDrag().dragTo(panel, 0.5, 0).endDrag();
    }
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackLayoutPanel unit='EM'>",
        "    <g:stack>",
        "      <g:header size='2'>BBB</g:header>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:stack>",
        "    <g:stack>",
        "      <g:header size='2'>AAA</g:header>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:stack>",
        "  </g:StackLayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_MOVE_header_after() throws Exception {
    StackLayoutPanelInfo panel =
        openEditor(
            "<ui:UiBinder>",
            "  <g:StackLayoutPanel unit='EM'>",
            "    <g:stack>",
            "      <g:header size='2'>AAA</g:header>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:stack>",
            "    <g:stack>",
            "      <g:header size='2'>BBB</g:header>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:stack>",
            "    <g:stack>",
            "      <g:header size='2'>CCC</g:header>",
            "      <g:Button wbp:name='button_3'/>",
            "    </g:stack>",
            "  </g:StackLayoutPanel>",
            "</ui:UiBinder>");
    // drag header of "button_1"
    {
      Rectangle bounds = panel.getWidgetHandles().get(0).getBounds();
      Point handleCenter = bounds.getCenter();
      canvas.moveTo(panel, handleCenter.x, handleCenter.y);
      canvas.beginDrag().dragTo(panel, 0.5, -1).endDrag();
    }
    assertXML(
        "<ui:UiBinder>",
        "  <g:StackLayoutPanel unit='EM'>",
        "    <g:stack>",
        "      <g:header size='2'>BBB</g:header>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:stack>",
        "    <g:stack>",
        "      <g:header size='2'>CCC</g:header>",
        "      <g:Button wbp:name='button_3'/>",
        "    </g:stack>",
        "    <g:stack>",
        "      <g:header size='2'>AAA</g:header>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:stack>",
        "  </g:StackLayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' text='My Button'/>",
        "    <g:StackLayoutPanel wbp:name='panel' unit='EM' width='200px' height='150px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    StackLayoutPanelInfo panel = getObjectByName("panel");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 10, 10).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:StackLayoutPanel wbp:name='panel' unit='EM' width='200px' height='150px'>",
        "      <g:stack>",
        "        <g:header size='2'>New widget</g:header>",
        "        <g:Button wbp:name='button' text='My Button'/>",
        "      </g:stack>",
        "    </g:StackLayoutPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_doubleClickHandle() throws Exception {
    StackLayoutPanelInfo panel =
        openEditor(
            "<ui:UiBinder>",
            "  <g:StackLayoutPanel unit='EM'>",
            "    <g:stack>",
            "      <g:header size='2'>AAA</g:header>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:stack>",
            "    <g:stack>",
            "      <g:header size='2'>BBB</g:header>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:stack>",
            "  </g:StackLayoutPanel>",
            "</ui:UiBinder>");
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    // initially "button_1" is visible
    assertTrue(isVisible(button_1));
    assertFalse(isVisible(button_2));
    // double click handle of "button_1"
    {
      Rectangle bounds = panel.getWidgetHandles().get(1).getBounds();
      Point handleCenter = bounds.getCenter();
      canvas.moveTo(panel, handleCenter.x, handleCenter.y).doubleClick();
    }
    // now "button_2" is visible
    assertFalse(isVisible(button_1));
    assertTrue(isVisible(button_2));
  }

  public void test_canvas_directEditHandle() throws Exception {
    StackLayoutPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackLayoutPanel unit='EM'>",
            "    <g:stack>",
            "      <g:header size='2'>AAA</g:header>",
            "      <g:Button/>",
            "    </g:stack>",
            "  </g:StackLayoutPanel>",
            "</ui:UiBinder>");
    // select header
    {
      Rectangle bounds = panel.getWidgetHandles().get(0).getBounds();
      Point handleCenter = bounds.getCenter();
      canvas.moveTo(panel, handleCenter.x, handleCenter.y).click();
    }
    // do direct edit
    canvas.performDirectEdit("123");
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackLayoutPanel unit='EM'>",
        "    <g:stack>",
        "      <g:header size='2'>123</g:header>",
        "      <g:Button/>",
        "    </g:stack>",
        "  </g:StackLayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE() throws Exception {
    StackLayoutPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackLayoutPanel/>",
            "</ui:UiBinder>");
    //
    WidgetInfo newButton = loadButton();
    tree.moveOn(panel).click();
    assertXML(
        "<ui:UiBinder>",
        "  <g:StackLayoutPanel>",
        "    <g:stack>",
        "      <g:header size='2'>New widget</g:header>",
        "      <g:Button/>",
        "    </g:stack>",
        "  </g:StackLayoutPanel>",
        "</ui:UiBinder>");
    tree.assertPrimarySelected(newButton);
  }

  public void test_tree_PASTE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackLayoutPanel unit='EM'>",
        "    <g:stack>",
        "      <g:header size='2'>AAA</g:header>",
        "      <g:Button wbp:name='button' text='A'/>",
        "    </g:stack>",
        "  </g:StackLayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    doCopyPaste(button);
    tree.moveAfter(button).click();
    assertXML(
        "<ui:UiBinder>",
        "  <g:StackLayoutPanel unit='EM'>",
        "    <g:stack>",
        "      <g:header size='2'>AAA</g:header>",
        "      <g:Button wbp:name='button' text='A'/>",
        "    </g:stack>",
        "    <g:stack>",
        "      <g:header size='2'>New widget</g:header>",
        "      <g:Button text='A'/>",
        "    </g:stack>",
        "  </g:StackLayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_MOVE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackLayoutPanel unit='EM'>",
        "    <g:stack>",
        "      <g:header size='2'>AAA</g:header>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:stack>",
        "    <g:stack>",
        "      <g:header size='2'>BBB</g:header>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:stack>",
        "  </g:StackLayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    //
    tree.startDrag(button_2).dragBefore(button_1).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackLayoutPanel unit='EM'>",
        "    <g:stack>",
        "      <g:header size='2'>BBB</g:header>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:stack>",
        "    <g:stack>",
        "      <g:header size='2'>AAA</g:header>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:stack>",
        "  </g:StackLayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "    <g:StackLayoutPanel wbp:name='panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    StackLayoutPanelInfo panel = getObjectByName("panel");
    WidgetInfo button = getObjectByName("button");
    //
    tree.startDrag(button).dragOn(panel).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:StackLayoutPanel wbp:name='panel'>",
        "      <g:stack>",
        "        <g:header size='2'>New widget</g:header>",
        "        <g:Button wbp:name='button'/>",
        "      </g:stack>",
        "    </g:StackLayoutPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }
}