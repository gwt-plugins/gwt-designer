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
import com.google.gdt.eclipse.designer.model.widgets.panels.IHorizontalSplitPanelInfo;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for {@link IHorizontalSplitPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class HorizontalSplitPanelLayoutEditPolicy<T extends IWidgetInfo>
    extends
      SplitPanelLayoutEditPolicy<T> {
  private final IHorizontalSplitPanelInfo<T> m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private HorizontalSplitPanelLayoutEditPolicy(IHorizontalSplitPanelInfo<T> panel) {
    super(panel);
    m_panel = panel;
  }

  public static <T extends IWidgetInfo> HorizontalSplitPanelLayoutEditPolicy<T> create(IHorizontalSplitPanelInfo<T> panel) {
    return new HorizontalSplitPanelLayoutEditPolicy<T>(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addFeedbacks() throws Exception {
    if (m_panel.getLeftWidget() == null) {
      addFeedback0(0, 0, 0.5, 1, new Insets(0, 0, 0, 5), "left");
    }
    if (m_panel.getRightWidget() == null) {
      addFeedback0(0.5, 0, 1, 1, new Insets(0, 5, 0, 0), "right");
    }
  }
}
