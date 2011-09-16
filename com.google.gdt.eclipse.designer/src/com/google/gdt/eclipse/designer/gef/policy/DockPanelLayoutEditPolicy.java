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
package com.google.gdt.eclipse.designer.gef.policy;

import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.DockPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.IDockPanelInfo;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for {@link DockPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class DockPanelLayoutEditPolicy<T extends IWidgetInfo>
    extends
      WidgetPositionLayoutEditPolicy<T, String> {
  private final IDockPanelInfo<T> m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private DockPanelLayoutEditPolicy(IDockPanelInfo<T> panel) {
    super(panel.getUnderlyingModel());
    m_panel = panel;
  }

  public static <T extends IWidgetInfo> DockPanelLayoutEditPolicy<T> create(IDockPanelInfo<T> panel) {
    return new DockPanelLayoutEditPolicy<T>(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addFeedbacks() throws Exception {
    addFeedback(0, 0, 1, 0.25, new Insets(0, 0, 1, 0), "North", "NORTH");
    addFeedback(0, 0.75, 1, 1, new Insets(1, 0, 0, 0), "South", "SOUTH");
    addFeedback(0, 0.25, 0.25, 0.75, new Insets(1, 0, 1, 1), "West", "WEST");
    addFeedback(0.75, 0.25, 1, 0.75, new Insets(1, 1, 1, 0), "East", "EAST");
    if (!m_panel.hasCenterWidget()) {
      addFeedback(0.25, 0.25, 0.75, 0.75, new Insets(1, 1, 1, 1), "Center", "CENTER");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(T component, String direction) throws Exception {
    m_panel.command_CREATE2(component, null);
    m_panel.setDirection(component, direction);
  }

  @Override
  protected void command_MOVE(T component, String direction) throws Exception {
    m_panel.setDirection(component, direction);
  }

  @Override
  protected void command_ADD(T component, String direction) throws Exception {
    m_panel.command_MOVE2(component, null);
    m_panel.setDirection(component, direction);
  }
}
