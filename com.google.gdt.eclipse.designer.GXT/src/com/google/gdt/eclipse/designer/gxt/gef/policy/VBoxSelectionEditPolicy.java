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
package com.google.gdt.eclipse.designer.gxt.gef.policy;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gef.policy.AbstractResizeSelectionEditPolicy;
import com.google.gdt.eclipse.designer.gxt.model.layout.VBoxLayoutInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.graphical.handles.Handle;

import java.util.List;

/**
 * {@link SelectionLayoutEditPolicy} for {@link VBoxLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.policy
 */
public final class VBoxSelectionEditPolicy extends AbstractResizeSelectionEditPolicy {
  private final WidgetInfo m_widget;
  private int m_flex;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VBoxSelectionEditPolicy(WidgetInfo widget) {
    m_widget = widget;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    handles.add(createResizeHandle(IPositionConstants.SOUTH, 0.25, IColorConstants.green));
    return handles;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void updateTooltipCommand() {
    if (m_resizeDirection == IPositionConstants.SOUTH) {
      prepareNewFlex();
      m_command = new EditCommand(m_widget) {
        @Override
        protected void executeEdit() throws Exception {
          VBoxLayoutInfo.getVBoxData(m_widget).setFlex(m_flex);
        }
      };
    }
  }

  private void prepareNewFlex() {
    m_flex = VBoxLayoutInfo.getVBoxData(m_widget).getFlex();
    m_flex += m_sizeDelta.height / 50;
    m_flex = Math.max(m_flex, 0);
    m_tooltip = "flex: " + m_flex;
  }
}