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
package com.google.gdt.eclipse.designer.mobile.device;

import com.google.gdt.eclipse.designer.core.model.widgets.generic.GwtGefTest;
import com.google.gdt.eclipse.designer.mobile.device.DeviceSelectionSupport.Orientation;
import com.google.gdt.eclipse.designer.mobile.device.model.DeviceInfo;
import com.google.gdt.eclipse.designer.model.widgets.IUIObjectInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;

import org.eclipse.wb.core.gef.policy.selection.TopSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Test for {@link DeviceSelectionSupport} in GEF, using Java.
 * 
 * @author scheglov_ke
 */
public class DeviceSelectionSupportJavaGefTest extends GwtGefTest {
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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Use {@link DeviceSelectionSupport#setDevice(IUIObjectInfo, DeviceInfo)} directly.
   */
  public void test_setDevice() throws Exception {
    ComplexPanelInfo originalPanel;
    {
      originalPanel =
          openJavaInfo(
              "// filler filler filler filler filler",
              "// filler filler filler filler filler",
              "public class Test extends FlowPanel {",
              "  public Test() {",
              "  }",
              "}");
      GraphicalEditPart panelEditPart = canvas.getEditPart(originalPanel);
      Figure panelFigure = panelEditPart.getFigure();
      // no device yet
      assertEquals(new Rectangle(20, 20, 450, 300), panelFigure.getBounds());
      assertTrue(hasTopSelectionPolicy(panelEditPart));
      // set device
      DeviceSelectionSupport.setDevice(originalPanel, DeviceManager.getDevice("generic.qvga"));
      assertEquals(new Rectangle(25, 25, 240, 320), panelFigure.getBounds());
      assertFalse(hasTopSelectionPolicy(panelEditPart));
    }
    // parse again
    {
      openSourcePage();
      m_lastEditor.replaceSubstring(0, 0, " ");
      m_lastEditor.commitChanges();
      openDesignPage();
      fetchContentFields();
    }
    // ...still has device
    {
      ComplexPanelInfo newPanel = (ComplexPanelInfo) m_contentJavaInfo;
      assertNotSame(originalPanel, newPanel);
      // has device
      assertNotNull(DeviceSelectionSupport.getDevice(newPanel));
      // device bounds applied
      GraphicalEditPart panelEditPart = canvas.getEditPart(newPanel);
      Figure panelFigure = panelEditPart.getFigure();
      assertEquals(new Rectangle(25, 25, 240, 320), panelFigure.getBounds());
      assertFalse(hasTopSelectionPolicy(panelEditPart));
      // remove device
      DeviceSelectionSupport.setDevice(newPanel, null);
      assertEquals(new Rectangle(20, 20, 240, 320), panelFigure.getBounds());
      assertTrue(hasTopSelectionPolicy(panelEditPart));
    }
  }

  /**
   * Use {@link DeviceSelectionSupport#setOrientation(IUIObjectInfo, Orientation)}.
   */
  public void test_setOrientation() throws Exception {
    ComplexPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "  }",
            "}");
    GraphicalEditPart panelEditPart = canvas.getEditPart(panel);
    Figure panelFigure = panelEditPart.getFigure();
    // set device
    {
      DeviceSelectionSupport.setDevice(panel, DeviceManager.getDevice("generic.qvga"));
      assertEquals(new Rectangle(25, 25, 240, 320), panelFigure.getBounds());
      assertSame(Orientation.PORTRAIT, DeviceSelectionSupport.getOrientation(panel));
    }
    // set orientation
    {
      DeviceSelectionSupport.setOrientation(panel, Orientation.LANDSCAPE);
      assertEquals(new Rectangle(25, 25, 320, 240), panelFigure.getBounds());
      assertSame(Orientation.LANDSCAPE, DeviceSelectionSupport.getOrientation(panel));
    }
  }

  /**
   * Use {@link MenuItem} to choose device.
   */
  public void test_useDeviceMenu() throws Exception {
    ComplexPanelInfo panel =
        openJavaInfo(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends FlowPanel {",
            "  public Test() {",
            "  }",
            "}");
    GraphicalEditPart panelEditPart = canvas.getEditPart(panel);
    Figure panelFigure = panelEditPart.getFigure();
    // no device yet
    assertEquals(new Rectangle(20, 20, 450, 300), panelFigure.getBounds());
    // set device
    {
      UiContext context = new UiContext();
      // click ToolItem to open Menu
      ToolItem deviceToolItem = context.getToolItem("No device");
      context.click(deviceToolItem, SWT.ARROW);
      // click "device" MenuItem
      {
        Menu menu = (Menu) deviceToolItem.getData("designTimeMenu");
        MenuItem menuItem = context.getMenuItem(menu, "QVGA");
        context.click(menuItem);
      }
    }
    // new bounds
    assertEquals(new Rectangle(25, 25, 240, 320), panelFigure.getBounds());
    // flip orientation
    {
      UiContext context = new UiContext();
      ToolItem deviceToolItem = context.getToolItem("Flip orientation");
      // PORTRAIT -> LANDSCAPE
      context.click(deviceToolItem);
      assertEquals(new Rectangle(25, 25, 320, 240), panelFigure.getBounds());
      // LANDSCAPE -> PORTRAIT
      context.click(deviceToolItem);
      assertEquals(new Rectangle(25, 25, 240, 320), panelFigure.getBounds());
    }
  }

  /**
   * @return <code>true</code> if given {@link EditPart} has {@link TopSelectionEditPolicy}.
   */
  private static boolean hasTopSelectionPolicy(EditPart editPart) {
    EditPolicy policy = editPart.getEditPolicy(EditPolicy.SELECTION_ROLE);
    return policy instanceof TopSelectionEditPolicy;
  }
}
