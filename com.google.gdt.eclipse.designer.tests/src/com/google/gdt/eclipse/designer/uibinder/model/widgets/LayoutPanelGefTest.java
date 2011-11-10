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

import org.eclipse.wb.core.gef.policy.layout.generic.AbstractPopupFigure;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.draw2d.FigureVisitor;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.StrValue;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;

/**
 * Test for {@link LayoutPanelInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class LayoutPanelGefTest extends UiBinderGefTest {
  private static final String MM_100 = Expectations.get("26.5", new StrValue[]{
      new StrValue("scheglov-win", "26.5"),
      new StrValue("flanker-windows", "26.5")});
  private static final String MM_50 = Expectations.get("13.2", new StrValue[]{
      new StrValue("scheglov-win", "13.2"),
      new StrValue("flanker-windows", "13.2")});

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
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    LayoutPanelInfo frame =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel/>",
            "</ui:UiBinder>");
    //
    loadCreationBox();
    canvas.sideMode().create(100, 50);
    canvas.moveTo(frame, 30, 40).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='30px' width='100px' top='40px' height='50px'>",
        "      <t:Box/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_CREATE_andResize() throws Exception {
    LayoutPanelInfo frame =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel/>",
            "</ui:UiBinder>");
    //
    loadCreationBox();
    canvas.sideMode().create(100, 50);
    canvas.moveTo(frame, 30, 40).beginDrag().dragOn(150, 75).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='30px' width='200px' top='40px' height='100px'>",
        "      <t:Box/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_PASTE() throws Exception {
    LayoutPanelInfo frame =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer left='10px' right='10px' top='100px' bottom='50px'>",
            "      <t:Box wbp:name='boxA'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    // copy/paste "boxA"
    {
      WidgetInfo boxA = getObjectByName("boxA");
      doCopyPaste(boxA);
    }
    canvas.sideMode().create(100, 50);
    canvas.moveTo(frame, 60, 120).click();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='10px' right='10px' top='100px' bottom='50px'>",
        "      <t:Box wbp:name='boxA'/>",
        "    </g:layer>",
        "    <g:layer left='60px' width='100px' top='120px' height='50px'>",
        "      <t:Box/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_LeftTop_PX() throws Exception {
    LayoutPanelInfo frame =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer left='10px' width='100px' top='20px' height='50px'>",
            "      <t:Box wbp:name='box'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    WidgetInfo box = getObjectByName("box");
    //
    canvas.sideMode().beginMove(box);
    canvas.dragTo(frame, 30, 40).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='30px' width='100px' top='40px' height='50px'>",
        "      <t:Box wbp:name='box'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_MOVE_RightBottom_PX() throws Exception {
    LayoutPanelInfo frame =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer right='10px' width='100px' bottom='20px' height='50px'>",
            "      <t:Box wbp:name='box'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    WidgetInfo box = getObjectByName("box");
    //
    canvas.sideMode().beginMove(box);
    canvas.rightSide().bottomSide().dragTo(frame, 450 - 30, 300 - 40).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer right='30px' width='100px' bottom='40px' height='50px'>",
        "      <t:Box wbp:name='box'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_MOVE_LeftTop_MM() throws Exception {
    LayoutPanelInfo frame =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:LayoutPanel>",
            "    <g:layer left='10mm' width='100px' top='20mm' height='50px'>",
            "      <t:Box wbp:name='box'/>",
            "    </g:layer>",
            "  </g:LayoutPanel>",
            "</ui:UiBinder>");
    WidgetInfo box = getObjectByName("box");
    //
    canvas.sideMode().beginMove(box);
    canvas.dragTo(frame, 100, 50).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='" + MM_100 + "mm' width='100px' top='" + MM_50 + "mm' height='50px'>",
        "      <t:Box wbp:name='box'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ADD() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:LayoutPanel wbp:name='panel' width='200px' height='150px'/>",
        "    <g:Button wbp:name='button' width='80px' height='30px'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    WidgetInfo panel = getObjectByName("panel");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.sideMode().beginMove(button);
    canvas.dragTo(panel, 30, 40).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:LayoutPanel wbp:name='panel' width='200px' height='150px'>",
        "      <g:layer left='30px' width='80px' top='40px' height='30px'>",
        "        <g:Button wbp:name='button' width='80px' height='30px'/>",
        "      </g:layer>",
        "    </g:LayoutPanel>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // RESIZE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_RESIZE_LeftWidth_T() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='100px' width='150px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginResize(button, IPositionConstants.EAST);
    canvas.dragOn(50, 0).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='100px' width='200px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_RESIZE_LeftWidth_L() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='100px' width='150px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginResize(button, IPositionConstants.WEST);
    canvas.dragOn(-50, 0).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='50px' width='200px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_RESIZE_TopHeight_T() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer top='100px' height='100px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginResize(button, IPositionConstants.SOUTH);
    canvas.dragOn(0, 50).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer top='100px' height='150px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  public void test_RESIZE_TopHeight_L() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer top='100px' height='100px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.beginResize(button, IPositionConstants.NORTH);
    canvas.dragOn(0, -50).endDrag();
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer top='50px' height='150px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ALIGN_horizontal() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer left='50px' width='150px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.select(button);
    // prepare figure
    AbstractPopupFigure alignFigure = getAlignFigure(16, 8);
    assertNotNull(alignFigure);
    assertNotNull(getAlignFigureImage(alignFigure));
    // test menu
    IMenuManager menu = getAlignFigureMenu(alignFigure);
    assertNotNull(findChildAction(menu, "none"));
    assertNotNull(findChildAction(menu, "left + width"));
    assertNotNull(findChildAction(menu, "right + width"));
    assertNotNull(findChildAction(menu, "left + right"));
    {
      IAction action = findChildAction(menu, "right + width");
      action.setChecked(true);
      action.run();
      assertXML(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:LayoutPanel>",
          "    <g:layer width='150px' right='250px'>",
          "      <g:Button wbp:name='button'/>",
          "    </g:layer>",
          "  </g:LayoutPanel>",
          "</ui:UiBinder>");
    }
  }

  public void test_ALIGN_vertical() throws Exception {
    openEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:LayoutPanel>",
        "    <g:layer top='50px' height='100px'>",
        "      <g:Button wbp:name='button'/>",
        "    </g:layer>",
        "  </g:LayoutPanel>",
        "</ui:UiBinder>");
    WidgetInfo button = getObjectByName("button");
    //
    canvas.select(button);
    // prepare figure
    AbstractPopupFigure alignFigure = getAlignFigure(8, 16);
    assertNotNull(alignFigure);
    assertNotNull(getAlignFigureImage(alignFigure));
    // test menu
    IMenuManager menu = getAlignFigureMenu(alignFigure);
    assertNotNull(findChildAction(menu, "none"));
    assertNotNull(findChildAction(menu, "top + height"));
    assertNotNull(findChildAction(menu, "bottom + height"));
    assertNotNull(findChildAction(menu, "top + bottom"));
    {
      IAction action = findChildAction(menu, "top + bottom");
      action.setChecked(true);
      action.run();
      assertXML(
          "// filler filler filler filler filler",
          "// filler filler filler filler filler",
          "<ui:UiBinder>",
          "  <g:LayoutPanel>",
          "    <g:layer top='50px' bottom='150px'>",
          "      <g:Button wbp:name='button'/>",
          "    </g:layer>",
          "  </g:LayoutPanel>",
          "</ui:UiBinder>");
    }
  }

  private AbstractPopupFigure getAlignFigure(final int width, final int height) {
    final AbstractPopupFigure[] popupFigure = {null};
    Figure layer = m_viewerCanvas.getLayer(IEditPartViewer.CLICKABLE_LAYER);
    layer.accept(new FigureVisitor() {
      private final int MARGIN = 6;

      @Override
      public void endVisit(Figure figure) {
        if (figure instanceof AbstractPopupFigure) {
          Dimension size = figure.getSize();
          if (size.width == width + MARGIN && size.height == height + MARGIN) {
            popupFigure[0] = (AbstractPopupFigure) figure;
          }
        }
      }
    }, true);
    return popupFigure[0];
  }

  private static Image getAlignFigureImage(AbstractPopupFigure figure) {
    return (Image) ReflectionUtils.invokeMethodEx(figure, "getImage()");
  }

  private static IMenuManager getAlignFigureMenu(AbstractPopupFigure figure) {
    IMenuManager menu = new MenuManager();
    ReflectionUtils.invokeMethodEx(figure, "fillMenu(org.eclipse.jface.action.IMenuManager)", menu);
    return menu;
  }
}