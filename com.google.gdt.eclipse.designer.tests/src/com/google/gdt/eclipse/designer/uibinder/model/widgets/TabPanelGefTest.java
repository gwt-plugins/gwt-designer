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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.uibinder.gef.UiBinderGefTest;

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;

/**
 * Test for {@link TabPanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class TabPanelGefTest extends UiBinderGefTest {
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
    TabPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel/>",
            "</ui:UiBinder>");
    //
    loadButton();
    canvas.moveTo(panel, 10, 10).click();
    assertXML(
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='New tab'>",
        "      <g:Button width='5cm' height='3cm'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_PASTE() throws Exception {
    TabPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel>",
            "    <g:Tab text='AAA'>",
            "      <g:Button wbp:name='button' text='A'/>",
            "    </g:Tab>",
            "  </g:TabPanel>",
            "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    doCopyPaste(button);
    canvas.moveTo(panel, 0, 0).click();
    assertXML(
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='New tab'>",
        "      <g:Button width='5cm' height='3cm' text='A'/>",
        "    </g:Tab>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button' text='A'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_MOVE_widget() throws Exception {
    TabPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel>",
            "    <g:Tab text='AAA'>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:Tab>",
            "    <g:Tab text='BBB'>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:Tab>",
            "  </g:TabPanel>",
            "</ui:UiBinder>");
    WidgetInfo button_2 = getObjectByName("button_2");
    //
    tree.select(button_2);
    canvas.beginDrag(button_2).dragTo(panel, 10, 0).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='BBB'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:Tab>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_MOVE_header_before() throws Exception {
    TabPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel>",
            "    <g:Tab text='AAA'>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:Tab>",
            "    <g:Tab text='BBB'>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:Tab>",
            "  </g:TabPanel>",
            "</ui:UiBinder>");
    // drag header of "button_2"
    {
      Rectangle bounds = panel.getWidgetHandles().get(1).getBounds();
      Point handleCenter = bounds.getCenter();
      canvas.moveTo(panel, handleCenter.x, handleCenter.y);
      canvas.beginDrag().dragTo(panel, 0, 5).endDrag();
    }
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='BBB'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:Tab>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_MOVE_header_after() throws Exception {
    TabPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel>",
            "    <g:Tab text='AAA'>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:Tab>",
            "    <g:Tab text='BBB'>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:Tab>",
            "    <g:Tab text='CCC'>",
            "      <g:Button wbp:name='button_3'/>",
            "    </g:Tab>",
            "  </g:TabPanel>",
            "</ui:UiBinder>");
    // drag header of "button_1"
    {
      Rectangle bounds = panel.getWidgetHandles().get(0).getBounds();
      Point handleCenter = bounds.getCenter();
      canvas.moveTo(panel, handleCenter.x, handleCenter.y);
      canvas.beginDrag().dragTo(panel, -1, 0).endDrag();
    }
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='BBB'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:Tab>",
        "    <g:Tab text='CCC'>",
        "      <g:Button wbp:name='button_3'/>",
        "    </g:Tab>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button' text='My Button'/>",
        "    <g:TabPanel wbp:name='panel' width='200px' height='150px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    TabPanelInfo panel = getObjectByName("panel");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginDrag(button).dragTo(panel, 10, 10).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TabPanel wbp:name='panel' width='200px' height='150px'>",
        "      <g:Tab text='New tab'>",
        "        <g:Button wbp:name='button' text='My Button' width='5cm' height='3cm'/>",
        "      </g:Tab>",
        "    </g:TabPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  public void test_canvas_doubleClickHandle() throws Exception {
    TabPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel>",
            "    <g:Tab text='AAA'>",
            "      <g:Button wbp:name='button_1'/>",
            "    </g:Tab>",
            "    <g:Tab text='BBB'>",
            "      <g:Button wbp:name='button_2'/>",
            "    </g:Tab>",
            "  </g:TabPanel>",
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
    TabPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel>",
            "    <g:Tab text='AAA'>",
            "      <g:Button wbp:name='button'/>",
            "    </g:Tab>",
            "  </g:TabPanel>",
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
        "  <g:TabPanel>",
        "    <g:Tab text='123'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tree_CREATE() throws Exception {
    TabPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:TabPanel/>",
            "</ui:UiBinder>");
    //
    WidgetInfo newButton = loadButton();
    tree.moveOn(panel).click();
    assertXML(
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='New tab'>",
        "      <g:Button width='5cm' height='3cm'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    tree.assertPrimarySelected(newButton);
  }

  public void test_tree_PASTE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button' text='A'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    doCopyPaste(button);
    tree.moveAfter(button).click();
    assertXML(
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button' text='A'/>",
        "    </g:Tab>",
        "    <g:Tab text='New tab'>",
        "      <g:Button width='5cm' height='3cm' text='A'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_MOVE() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:Tab>",
        "    <g:Tab text='BBB'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
    WidgetInfo button_1 = getObjectByName("button_1");
    WidgetInfo button_2 = getObjectByName("button_2");
    //
    tree.startDrag(button_2).dragBefore(button_1).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:TabPanel>",
        "    <g:Tab text='BBB'>",
        "      <g:Button wbp:name='button_2'/>",
        "    </g:Tab>",
        "    <g:Tab text='AAA'>",
        "      <g:Button wbp:name='button_1'/>",
        "    </g:Tab>",
        "  </g:TabPanel>",
        "</ui:UiBinder>");
  }

  public void test_tree_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:Button wbp:name='button'/>",
        "    <g:TabPanel wbp:name='panel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    TabPanelInfo panel = getObjectByName("panel");
    WidgetInfo button = getObjectByName("button");
    //
    tree.startDrag(button).dragOn(panel).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:TabPanel wbp:name='panel'>",
        "      <g:Tab text='New tab'>",
        "        <g:Button wbp:name='button' width='5cm' height='3cm'/>",
        "      </g:Tab>",
        "    </g:TabPanel>",
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
    return widget.getBounds().height != 0;
  }
}