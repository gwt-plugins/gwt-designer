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

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.DockLayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.IDockLayoutPanelInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.helpers.SelectionEditPolicyRefreshHelper;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;

import java.util.List;

/**
 * {@link SelectionLayoutEditPolicy} for {@link DockLayoutPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class DockLayoutPanelSelectionEditPolicy<T extends IWidgetInfo>
    extends
      AbstractResizeSelectionEditPolicy {
  private final IDockLayoutPanelInfo<T> m_panel;
  private final T m_widget;
  private double m_newUnitSize;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private DockLayoutPanelSelectionEditPolicy(IDockLayoutPanelInfo<T> panel, T widget) {
    m_panel = panel;
    m_widget = widget;
    new SelectionEditPolicyRefreshHelper(this);
  }

  public static <T extends IWidgetInfo> DockLayoutPanelSelectionEditPolicy<T> create(IDockLayoutPanelInfo<T> panel,
      T widget) {
    return new DockLayoutPanelSelectionEditPolicy<T>(panel, widget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    handles.add(new MoveHandle(getHost()));
    // add resize handle
    String edge = m_panel.getEdge(m_widget);
    if ("WEST".equals(edge)) {
      addResizeHandle(handles, IPositionConstants.EAST);
    }
    if ("EAST".equals(edge)) {
      addResizeHandle(handles, IPositionConstants.WEST);
    }
    if ("NORTH".equals(edge)) {
      addResizeHandle(handles, IPositionConstants.SOUTH);
    }
    if ("SOUTH".equals(edge)) {
      addResizeHandle(handles, IPositionConstants.NORTH);
    }
    return handles;
  }

  private void addResizeHandle(List<Handle> handles, int direction) {
    Handle handle = createResizeHandle(direction, 0.5, IColorConstants.green);
    handles.add(handle);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void updateTooltipCommand() {
    boolean horizontal = isHorizontal();
    {
      int pixels = horizontal ? m_newSize.width : m_newSize.height;
      m_newUnitSize = m_panel.getSizeInUnits(pixels, !horizontal);
      m_tooltip = "size: " + m_panel.getUnitSizeTooltip(m_newUnitSize);
    }
    m_command = new EditCommand(m_widget) {
      @Override
      protected void executeEdit() throws Exception {
        m_panel.setSize(m_widget, m_newUnitSize);
      }
    };
  }

  private boolean isHorizontal() {
    return m_resizeDirection == IPositionConstants.WEST
        || m_resizeDirection == IPositionConstants.EAST;
  }
}