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

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Test for {@link StackPanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class StackPanelGefTest extends UiBinderGefTest {
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
    StackPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel/>",
            "</ui:UiBinder>");
    //
    loadButton();
    canvas.moveTo(panel, 10, 10).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button width='100%' height='100%' g:StackPanel-text='New widget'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_PASTE() throws Exception {
    StackPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel>",
            "    <g:Button wbp:name='button' g:StackPanel-text='Existing'/>",
            "  </g:StackPanel>",
            "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    doCopyPaste(button);
    canvas.moveTo(panel, 0, 0).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button width='100%' height='100%' g:StackPanel-text='New widget'/>",
        "    <g:Button wbp:name='button' g:StackPanel-text='Existing'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_MOVE_widget() throws Exception {
    StackPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel>",
            "    <g:Button wbp:name='button_1' text='AAA' g:StackPanel-text='AAA'/>",
            "    <g:Button wbp:name='button_2' text='BBB' g:StackPanel-text='BBB'/>",
            "  </g:StackPanel>",
            "</ui:UiBinder>");
    WidgetInfo button_2 = getObjectByName("button_2");
    //
    tree.select(button_2);
    canvas.beginDrag(button_2).dragTo(panel, 10, 5).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button_2' text='BBB' g:StackPanel-text='BBB'/>",
        "    <g:Button wbp:name='button_1' text='AAA' g:StackPanel-text='AAA'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_MOVE_widget_header_before() throws Exception {
    StackPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel>",
            "    <g:Button wbp:name='button_1' g:StackPanel-text='AAA'/>",
            "    <g:Button wbp:name='button_2' g:StackPanel-text='BBB'/>",
            "  </g:StackPanel>",
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
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button_2' g:StackPanel-text='BBB'/>",
        "    <g:Button wbp:name='button_1' g:StackPanel-text='AAA'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_MOVE_widget_header_after() throws Exception {
    StackPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel>",
            "    <g:Button wbp:name='button_1' g:StackPanel-text='AAA'/>",
            "    <g:Button wbp:name='button_2' g:StackPanel-text='BBB'/>",
            "    <g:Button wbp:name='button_3' g:StackPanel-text='CCC'/>",
            "  </g:StackPanel>",
            "</ui:UiBinder>");
    // drag header of "button_1"
    {
      Rectangle bounds = panel.getWidgetHandles().get(0).getBounds();
      Point handleCenter = bounds.getCenter();
      canvas.moveTo(panel, handleCenter.x, handleCenter.y);
      canvas.beginDrag().dragTo(panel, 0.5, -1).endDrag();
    }
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button_2' g:StackPanel-text='BBB'/>",
        "    <g:Button wbp:name='button_3' g:StackPanel-text='CCC'/>",
        "    <g:Button wbp:name='button_1' g:StackPanel-text='AAA'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "    <g:StackPanel wbp:name='panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    StackPanelInfo panel = getObjectByName("panel");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 10, 10).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:StackPanel wbp:name='panel'>",
        "      <g:Button wbp:name='button' width='100%' height='100%' g:StackPanel-text='New widget'/>",
        "    </g:StackPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_doubleClickHandle() throws Exception {
    StackPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel>",
            "    <g:Button wbp:name='button_1' g:StackPanel-text='AAA'/>",
            "    <g:Button wbp:name='button_2' g:StackPanel-text='BBB'/>",
            "  </g:StackPanel>",
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
    StackPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel>",
            "    <g:Button g:StackPanel-text='AAA'/>",
            "  </g:StackPanel>",
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
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button g:StackPanel-text='123'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE() throws Exception {
    StackPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:StackPanel/>",
            "</ui:UiBinder>");
    //
    WidgetInfo newButton = loadButton();
    tree.moveOn(panel).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button width='100%' height='100%' g:StackPanel-text='New widget'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
    tree.assertPrimarySelected(newButton);
  }

  public void test_tree_PASTE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button' g:StackPanel-text='Existing'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    doCopyPaste(button);
    tree.moveBefore(button).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button width='100%' height='100%' g:StackPanel-text='New widget'/>",
        "    <g:Button wbp:name='button' g:StackPanel-text='Existing'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_MOVE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button_1' g:StackPanel-text='AAA'/>",
        "    <g:Button wbp:name='button_2' g:StackPanel-text='BBB'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    //
    tree.startDrag(button_2).dragBefore(button_1).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:StackPanel>",
        "    <g:Button wbp:name='button_2' g:StackPanel-text='BBB'/>",
        "    <g:Button wbp:name='button_1' g:StackPanel-text='AAA'/>",
        "  </g:StackPanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "    <g:StackPanel wbp:name='panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    StackPanelInfo panel = getObjectByName("panel");
    WidgetInfo button = getObjectByName("button");
    //
    tree.startDrag(button).dragOn(panel).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:StackPanel wbp:name='panel'>",
        "      <g:Button wbp:name='button' width='100%' height='100%' g:StackPanel-text='New widget'/>",
        "    </g:StackPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if object of given {@link WidgetInfo} is visible.
   */
  private static boolean isVisible(WidgetInfo widget) throws Exception {
    return Boolean.TRUE.equals(ReflectionUtils.invokeMethod(widget.getObject(), "isVisible()"));
  }
}