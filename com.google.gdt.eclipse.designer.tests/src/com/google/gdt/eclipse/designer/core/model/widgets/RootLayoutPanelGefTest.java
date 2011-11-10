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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.widgets.generic.GwtGefTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.LayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootLayoutPanelInfo;
import com.google.gdt.eclipse.designer.parser.ParseFactory;

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
public class RootLayoutPanelGefTest extends GwtGefTest {
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
      ParseFactory.disposeSharedGWTState();
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
    RootLayoutPanelInfo frame =
        openJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "  }",
            "}");
    //
    loadCreationBox();
    canvas.sideMode().create(100, 50);
    canvas.moveTo(frame, 30, 40).click();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Box box = new Box();",
        "      rootPanel.add(box);",
        "      rootPanel.setWidgetLeftWidth(box, 30.0, Unit.PX, 100.0, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(box, 40.0, Unit.PX, 50.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_CREATE_andResize() throws Exception {
    RootLayoutPanelInfo frame =
        openJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "  }",
            "}");
    //
    loadCreationBox();
    canvas.sideMode().create(100, 50);
    canvas.moveTo(frame, 30, 40).beginDrag().dragOn(150, 75).endDrag();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Box box = new Box();",
        "      rootPanel.add(box);",
        "      rootPanel.setWidgetLeftWidth(box, 30.0, Unit.PX, 200.0, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(box, 40.0, Unit.PX, 100.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_PASTE() throws Exception {
    RootLayoutPanelInfo frame =
        openJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Box boxA = new Box();",
            "      rootPanel.add(boxA);",
            "      rootPanel.setWidgetLeftWidth(boxA, 10.0, Unit.PX, 100.0, Unit.PX);",
            "      rootPanel.setWidgetTopHeight(boxA, 10.0, Unit.PX, 50.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    // copy/paste "boxA"
    {
      WidgetInfo boxA = getJavaInfoByName("boxA");
      doCopyPaste(boxA);
    }
    canvas.sideMode().create(100, 50);
    canvas.moveTo(frame, 50, 100).click();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Box boxA = new Box();",
        "      rootPanel.add(boxA);",
        "      rootPanel.setWidgetLeftWidth(boxA, 10.0, Unit.PX, 100.0, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(boxA, 10.0, Unit.PX, 50.0, Unit.PX);",
        "    }",
        "    {",
        "      Box box = new Box();",
        "      rootPanel.add(box);",
        "      rootPanel.setWidgetLeftWidth(box, 50.0, Unit.PX, 100.0, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(box, 100.0, Unit.PX, 50.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_MOVE_LeftTop_PX() throws Exception {
    RootLayoutPanelInfo frame =
        openJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Box box = new Box();",
            "      rootPanel.add(box);",
            "      rootPanel.setWidgetLeftWidth(box, 10.0, Unit.PX, 100.0, Unit.PX);",
            "      rootPanel.setWidgetTopHeight(box, 20.0, Unit.PX, 50.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    WidgetInfo box = getJavaInfoByName("box");
    //
    canvas.sideMode().beginMove(box);
    canvas.dragTo(frame, 30, 40).endDrag();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Box box = new Box();",
        "      rootPanel.add(box);",
        "      rootPanel.setWidgetLeftWidth(box, 30.0, Unit.PX, 100.0, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(box, 40.0, Unit.PX, 50.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_RightBottom_PX() throws Exception {
    RootLayoutPanelInfo frame =
        openJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Box box = new Box();",
            "      rootPanel.add(box);",
            "      rootPanel.setWidgetRightWidth(box, 10.0, Unit.PX, 100.0, Unit.PX);",
            "      rootPanel.setWidgetBottomHeight(box, 20.0, Unit.PX, 50.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    WidgetInfo box = getJavaInfoByName("box");
    //
    canvas.sideMode().beginMove(box);
    canvas.rightSide().bottomSide().dragTo(frame, 450 - 30, 300 - 40).endDrag();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Box box = new Box();",
        "      rootPanel.add(box);",
        "      rootPanel.setWidgetRightWidth(box, 30.0, Unit.PX, 100.0, Unit.PX);",
        "      rootPanel.setWidgetBottomHeight(box, 40.0, Unit.PX, 50.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE_LeftTop_MM() throws Exception {
    RootLayoutPanelInfo frame =
        openJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
            "    {",
            "      Box box = new Box();",
            "      rootPanel.add(box);",
            "      rootPanel.setWidgetLeftWidth(box, 10.0, Unit.MM, 100.0, Unit.PX);",
            "      rootPanel.setWidgetTopHeight(box, 10.0, Unit.MM, 50.0, Unit.PX);",
            "    }",
            "  }",
            "}");
    WidgetInfo box = getJavaInfoByName("box");
    //
    canvas.sideMode().beginMove(box);
    canvas.dragTo(frame, 100, 50).endDrag();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Box box = new Box();",
        "      rootPanel.add(box);",
        "      rootPanel.setWidgetLeftWidth(box, " + MM_100 + ", Unit.MM, 100.0, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(box, " + MM_50 + ", Unit.MM, 50.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ADD() throws Exception {
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      LayoutPanel panel = new LayoutPanel();",
        "      rootPanel.add(panel);",
        "      rootPanel.setWidgetLeftWidth(panel, 10.0, Unit.PX, 200.0, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(panel, 10.0, Unit.PX, 150.0, Unit.PX);",
        "    }",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 50.0, Unit.PX, 80.0, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(button, 200.0, Unit.PX, 30.0, Unit.PX);",
        "    }",
        "  }",
        "}");
    WidgetInfo panel = getJavaInfoByName("panel");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.sideMode().beginMove(button);
    canvas.dragTo(panel, 30, 40).endDrag();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      LayoutPanel panel = new LayoutPanel();",
        "      rootPanel.add(panel);",
        "      rootPanel.setWidgetLeftWidth(panel, 10.0, Unit.PX, 200.0, Unit.PX);",
        "      rootPanel.setWidgetTopHeight(panel, 10.0, Unit.PX, 150.0, Unit.PX);",
        "      {",
        "        Button button = new Button();",
        "        panel.add(button);",
        "        panel.setWidgetLeftWidth(button, 30.0, Unit.PX, 80.0, Unit.PX);",
        "        panel.setWidgetTopHeight(button, 40.0, Unit.PX, 30.0, Unit.PX);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // RESIZE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_RESIZE_LeftWidth_T() throws Exception {
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 100.0, Unit.PX, 150.0, Unit.PX);",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.EAST);
    canvas.dragOn(50, 0).endDrag();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 100.0, Unit.PX, 200.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_RESIZE_LeftWidth_L() throws Exception {
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 100.0, Unit.PX, 150.0, Unit.PX);",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.WEST);
    canvas.dragOn(-50, 0).endDrag();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 50.0, Unit.PX, 200.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_RESIZE_TopHeight_T() throws Exception {
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetTopHeight(button, 100.0, Unit.PX, 100.0, Unit.PX);",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.SOUTH);
    canvas.dragOn(0, 50).endDrag();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetTopHeight(button, 100.0, Unit.PX, 150.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  public void test_RESIZE_TopHeight_L() throws Exception {
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetTopHeight(button, 100.0, Unit.PX, 100.0, Unit.PX);",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
    //
    canvas.beginResize(button, IPositionConstants.NORTH);
    canvas.dragOn(0, -50).endDrag();
    assertEditor(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetTopHeight(button, 50.0, Unit.PX, 150.0, Unit.PX);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ALIGN_horizontal() throws Exception {
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetLeftWidth(button, 50.0, Unit.PX, 150.0, Unit.PX);",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
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
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
          "    {",
          "      Button button = new Button();",
          "      rootPanel.add(button);",
          "      rootPanel.setWidgetRightWidth(button, 250.0, Unit.PX, 150.0, Unit.PX);",
          "    }",
          "  }",
          "}");
    }
  }

  public void test_ALIGN_vertical() throws Exception {
    openJavaInfo(
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
        "    {",
        "      Button button = new Button();",
        "      rootPanel.add(button);",
        "      rootPanel.setWidgetTopHeight(button, 50.0, Unit.PX, 100.0, Unit.PX);",
        "    }",
        "  }",
        "}");
    WidgetInfo button = getJavaInfoByName("button");
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
      assertEditor(
          "public class Test implements EntryPoint {",
          "  public void onModuleLoad() {",
          "    RootLayoutPanel rootPanel = RootLayoutPanel.get();",
          "    {",
          "      Button button = new Button();",
          "      rootPanel.add(button);",
          "      rootPanel.setWidgetTopBottom(button, 50.0, Unit.PX, 150.0, Unit.PX);",
          "    }",
          "  }",
          "}");
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
