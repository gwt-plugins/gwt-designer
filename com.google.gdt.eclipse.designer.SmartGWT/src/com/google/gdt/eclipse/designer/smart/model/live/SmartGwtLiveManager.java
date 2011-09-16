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
package com.google.gdt.eclipse.designer.smart.model.live;

import com.google.gdt.eclipse.designer.model.widgets.live.GwtLiveManager;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.UIObjectUtils;
import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;

import org.eclipse.wb.draw2d.geometry.Point;

import org.apache.commons.lang.StringUtils;

/**
 * Default live components manager for SmartGWT <code>Canvas</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class SmartGwtLiveManager extends GwtLiveManager {
  private final UIObjectUtils m_utils;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SmartGwtLiveManager(CanvasInfo canvas) {
    super(canvas);
    m_utils = canvas.getState().getUIObjectUtils();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractLiveComponentsManager
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected CanvasInfo createLiveComponent() throws Exception {
    m_utils.setLiveManager(true);
    // prepare empty RootPanel
    final CanvasInfo parentCanvas;
    {
      String[] sourceLines =
          new String[]{
              "  com.google.gwt.user.client.ui.RootPanel __wbp_panel = com.google.gwt.user.client.ui.RootPanel.get();",
              "  com.smartgwt.client.widgets.Canvas __wbp_canvas = new com.smartgwt.client.widgets.Canvas();",
              "  __wbp_canvas.setTitle(\"__wbp_liveWidget\");",
              "  __wbp_panel.add(__wbp_canvas);",
              "  __wbp_panel.setPixelSize(800, 600);",};
      RootPanelInfo panel = (RootPanelInfo) parse(sourceLines);
      parentCanvas = (CanvasInfo) panel.getChildrenWidgets().get(0);
    }
    // prepare component
    CanvasInfo canvas = createClone();
    // add component on Canvas
    parentCanvas.command_absolute_CREATE(canvas, null);
    parentCanvas.command_BOUNDS(canvas, new Point(10, 10), null);
    // check for forced size
    {
      String width = canvas.getDescription().getParameter("liveComponent.forcedSize.width");
      String height = canvas.getDescription().getParameter("liveComponent.forcedSize.height");
      if (!StringUtils.isEmpty(width) && !StringUtils.isEmpty(height)) {
        m_shouldSetSize = true;
        canvas.getSizeSupport().setSize(width, height);
      }
    }
    // ready to get live values
    return canvas;
  }
}
