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
package com.google.gdt.eclipse.designer.mobile.device;

import com.google.gdt.eclipse.designer.mobile.device.model.DeviceInfo;
import com.google.gdt.eclipse.designer.model.widgets.IUIObjectInfo;
import com.google.gdt.eclipse.designer.uibinder.gef.UiBinderGefTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.ComplexPanelInfo;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

/**
 * Test for {@link DeviceSelectionSupport} in GEF, using UiBinder.
 * 
 * @author scheglov_ke
 */
public class DeviceSelectionSupportXmlGefTest extends UiBinderGefTest {
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
          openEditor(
              "// filler filler filler filler filler",
              "// filler filler filler filler filler",
              "<ui:UiBinder>",
              "  <g:FlowPanel/>",
              "</ui:UiBinder>");
      GraphicalEditPart panelEditPart = canvas.getEditPart(originalPanel);
      Figure panelFigure = panelEditPart.getFigure();
      // no device yet
      assertEquals(new Rectangle(20, 20, 450, 300), panelFigure.getBounds());
      // set device
      DeviceSelectionSupport.setDevice(originalPanel, DeviceManager.getDevice("generic.qvga"));
      assertEquals(new Rectangle(25, 25, 240, 320), panelFigure.getBounds());
    }
    // parse again
    {
      openSourcePage();
      m_lastContext.getDocument().replace(0, 0, " ");
      openDesignPage();
      fetchContentFields();
    }
    // ...still has device
    {
      assertNotSame(originalPanel, m_contentObject);
      GraphicalEditPart panelEditPart = canvas.getEditPart(m_contentObject);
      Figure panelFigure = panelEditPart.getFigure();
      assertEquals(new Rectangle(25, 25, 240, 320), panelFigure.getBounds());
    }
  }
}
