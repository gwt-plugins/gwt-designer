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
import com.google.gdt.eclipse.designer.model.widgets.panels.ISplitPanelInfo;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for {@link ISplitPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public abstract class SplitPanelLayoutEditPolicy<T extends IWidgetInfo>
    extends
      WidgetPositionLayoutEditPolicy<T, String> {
  private final ISplitPanelInfo<T> m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SplitPanelLayoutEditPolicy(ISplitPanelInfo<T> panel) {
    super(panel.getUnderlyingModel());
    m_panel = panel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void addFeedback0(double px1,
      double py1,
      double px2,
      double py2,
      Insets insets,
      String region) {
    addFeedback(px1, py1, px2, py2, insets, region, region);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation of commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(T component, String region) throws Exception {
    m_panel.command_CREATE(component, region);
  }

  @Override
  protected void command_MOVE(T component, String region) throws Exception {
    m_panel.command_MOVE(component, region);
  }
}
