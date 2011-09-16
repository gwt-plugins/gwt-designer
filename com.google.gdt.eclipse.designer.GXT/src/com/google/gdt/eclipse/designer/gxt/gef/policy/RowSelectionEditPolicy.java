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
import com.google.gdt.eclipse.designer.gxt.model.layout.RowDataInfo;
import com.google.gdt.eclipse.designer.gxt.model.layout.RowLayoutInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.graphical.handles.Handle;

import java.util.List;

/**
 * {@link SelectionLayoutEditPolicy} for {@link RowLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.policy
 */
public final class RowSelectionEditPolicy extends AbstractResizeSelectionEditPolicy {
  private final WidgetInfo m_widget;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowSelectionEditPolicy(WidgetInfo widget) {
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
    handles.add(createResizeHandle(IPositionConstants.EAST, 0.25, IColorConstants.green));
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
    // EAST
    if (m_resizeDirection == IPositionConstants.EAST) {
      final double newWidth = getNewWidth();
      m_command = new EditCommand(m_widget) {
        @Override
        protected void executeEdit() throws Exception {
          RowLayoutInfo.getRowData(m_widget).setWidth(newWidth);
        }
      };
    }
    // SOUTH
    if (m_resizeDirection == IPositionConstants.SOUTH) {
      final double newHeight = getNewHeight();
      m_command = new EditCommand(m_widget) {
        @Override
        protected void executeEdit() throws Exception {
          RowLayoutInfo.getRowData(m_widget).setHeight(newHeight);
        }
      };
    }
  }

  private double getNewWidth() {
    RowDataInfo rowData = RowLayoutInfo.getRowData(m_widget);
    if (m_ctrlPressed) {
      double width = getCurrentPercentWidth(rowData);
      width += m_sizeDelta.width / 100.0;
      width = roundPercentToFive(width);
      //
      m_tooltip = "width: " + getPercentString(width) + "\nRelease Ctrl to set width in pixels";
      return width;
    } else {
      double width = m_newSize.width;
      width = roundToFive(width);
      if (width < 0) {
        width = -1;
      }
      //
      m_tooltip = "width: " + (int) width + "\nPress Ctrl to set width in percents";
      return width;
    }
  }

  private double getCurrentPercentWidth(RowDataInfo rowData) {
    double width = rowData.getWidth();
    if (width < 0 || width > 1) {
      width = (double) getHostFigure().getSize().width / getParentWidth();
    }
    return width;
  }

  private double getNewHeight() {
    RowDataInfo rowData = RowLayoutInfo.getRowData(m_widget);
    if (m_ctrlPressed) {
      double height = getCurrentPercentHeight(rowData);
      height += m_sizeDelta.height / 100.0;
      height = roundPercentToFive(height);
      //
      m_tooltip = "height: " + getPercentString(height) + "\nRelease Ctrl to set height in pixels";
      return height;
    } else {
      double height = m_newSize.height;
      height = roundToFive(height);
      if (height < 0) {
        height = -1;
      }
      //
      m_tooltip = "height: " + (int) height + "\nPress Ctrl to set height in percents";
      return height;
    }
  }

  private double getCurrentPercentHeight(RowDataInfo rowData) {
    double height = rowData.getHeight();
    if (height < 0 || height > 1) {
      height = (double) getHostFigure().getSize().height / getParentHeight();
    }
    return height;
  }

  private static String getPercentString(double value) {
    return (int) (value * 100) + "%";
  }

  private static double roundPercentToFive(double value) {
    value = Math.max(value, 0.0);
    value = Math.min(value, 1.0);
    value *= 100;
    value = roundToFive(value);
    value /= 100;
    return value;
  }

  private static double roundToFive(double value) {
    return (int) value / 5 * 5;
  }
}