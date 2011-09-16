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
import com.google.gdt.eclipse.designer.gxt.model.layout.BorderLayoutInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.helpers.SelectionEditPolicyRefreshHelper;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;

import java.util.List;

/**
 * {@link SelectionLayoutEditPolicy} for {@link BorderLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.policy
 */
public final class BorderSelectionEditPolicy extends AbstractResizeSelectionEditPolicy {
  private final WidgetInfo m_widget;
  private float m_newBorderSize;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BorderSelectionEditPolicy(WidgetInfo widget) {
    m_widget = widget;
    new SelectionEditPolicyRefreshHelper(this);
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
    String region = BorderLayoutInfo.getBorderData(m_widget).getRegion();
    if ("WEST".equals(region)) {
      addResizeHandle(handles, IPositionConstants.EAST);
    }
    if ("EAST".equals(region)) {
      addResizeHandle(handles, IPositionConstants.WEST);
    }
    if ("NORTH".equals(region)) {
      addResizeHandle(handles, IPositionConstants.SOUTH);
    }
    if ("SOUTH".equals(region)) {
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
    prepareNewBorderSize();
    m_command = new EditCommand(m_widget) {
      @Override
      protected void executeEdit() throws Exception {
        BorderLayoutInfo.getBorderData(m_widget).setSize(m_newBorderSize);
      }
    };
  }

  private void prepareNewBorderSize() {
    int newSize = isHorizontal() ? m_newSize.width : m_newSize.height;
    if (m_ctrlPressed) {
      int parentSize = isHorizontal() ? getParentWidth() : getParentHeight();
      m_newBorderSize = (float) newSize / parentSize;
      m_newBorderSize = roundPercentToFive(m_newBorderSize);
      //
      m_tooltip =
          "size: " + getPercentString(m_newBorderSize) + "\nRelease Ctrl to set size in pixels";
    } else {
      m_newBorderSize = newSize;
      m_newBorderSize = roundToFive(m_newBorderSize);
      //
      m_tooltip = "size: " + (int) m_newBorderSize + "\nPress Ctrl to set size in percents";
    }
  }

  private boolean isHorizontal() {
    return m_resizeDirection == IPositionConstants.WEST
        || m_resizeDirection == IPositionConstants.EAST;
  }

  private static String getPercentString(float value) {
    return (int) (value * 100) + "%";
  }

  private static float roundPercentToFive(float value) {
    value = Math.max(value, 0.0f);
    value = Math.min(value, 0.95f);
    value *= 100;
    value = roundToFive(value);
    value /= 100;
    return value;
  }

  private static float roundToFive(float value) {
    value = Math.max(value, 0.0f);
    return (int) value / 5 * 5;
  }
}