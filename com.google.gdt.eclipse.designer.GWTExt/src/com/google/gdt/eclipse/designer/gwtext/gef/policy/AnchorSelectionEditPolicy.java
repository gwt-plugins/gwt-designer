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
package com.google.gdt.eclipse.designer.gwtext.gef.policy;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gef.policy.AbstractResizeSelectionEditPolicy;
import com.google.gdt.eclipse.designer.gwtext.model.layout.AnchorLayoutInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.graphical.handles.Handle;

import java.util.List;

/**
 * {@link SelectionLayoutEditPolicy} for {@link WidgetInfo} on {@link AnchorLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.gef.policy
 */
public class AnchorSelectionEditPolicy extends AbstractResizeSelectionEditPolicy {
  private final WidgetInfo m_widget;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AnchorSelectionEditPolicy(WidgetInfo widget) {
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
    handles.add(createResizeHandle(IPositionConstants.EAST, 0.25, IColorConstants.green));
    return handles;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void updateTooltipCommand() {
    // EAST
    if (m_resizeDirection == IPositionConstants.EAST) {
      if (m_ctrlPressed) {
        int offset = getParentWidth() - m_newSize.width;
        final String width = offset > 0 ? "-" + offset : "" + offset;
        m_tooltip = "width: " + width + "\nRelease Ctrl to set width in percents";
        m_command = new EditCommand(m_widget) {
          @Override
          protected void executeEdit() throws Exception {
            AnchorLayoutInfo.getAnchorData(m_widget).setAnchorWidth(width);
          }
        };
      } else {
        final String width = m_newSize.width * 100 / getParentWidth() + "%";
        m_tooltip = "width: " + width + "\nPress Ctrl to set offset from right";
        m_command = new EditCommand(m_widget) {
          @Override
          protected void executeEdit() throws Exception {
            AnchorLayoutInfo.getAnchorData(m_widget).setAnchorWidth(width);
          }
        };
      }
    }
    // SOUTH
    if (m_resizeDirection == IPositionConstants.SOUTH) {
      if (m_ctrlPressed) {
        int offset = getParentHeight() - m_newSize.height;
        final String height = offset > 0 ? "-" + offset : "" + offset;
        m_tooltip = "height: " + height + "\nRelease Ctrl to set height in percents";
        m_command = new EditCommand(m_widget) {
          @Override
          protected void executeEdit() throws Exception {
            AnchorLayoutInfo.getAnchorData(m_widget).setAnchorHeight(height);
          }
        };
      } else {
        final String height = m_newSize.height * 100 / getParentHeight() + "%";
        m_tooltip = "height: " + height + "\nPress Ctrl to set offset from bottom";
        m_command = new EditCommand(m_widget) {
          @Override
          protected void executeEdit() throws Exception {
            AnchorLayoutInfo.getAnchorData(m_widget).setAnchorHeight(height);
          }
        };
      }
    }
  }
}