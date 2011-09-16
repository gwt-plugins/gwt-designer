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
package com.google.gdt.eclipse.designer.gxt.gef.policy.table.header.edit;

import com.google.gdt.eclipse.designer.gxt.model.layout.table.DimensionInfo;
import com.google.gdt.eclipse.designer.gxt.model.layout.table.TableLayoutInfo;

import org.eclipse.wb.core.gef.header.Headers;
import org.eclipse.wb.core.gef.header.IHeaderMenuProvider;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.ParentTargetDragEditPartTracker;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * {@link EditPart} for column/row header of {@link TableLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.TableLayout
 */
public abstract class DimensionHeaderEditPart extends GraphicalEditPart
    implements
      IHeaderMenuProvider {
  protected static final Color COLOR_NORMAL = Headers.COLOR_HEADER;
  protected static final Font DEFAULT_FONT = new Font(null, "Arial", 7, SWT.NONE);
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final TableLayoutInfo m_layout;
  protected final DimensionInfo m_dimension;
  private final Figure m_containerFigure;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionHeaderEditPart(TableLayoutInfo layout,
      DimensionInfo dimension,
      Figure containerFigure) {
    m_layout = layout;
    m_dimension = dimension;
    m_containerFigure = containerFigure;
    setModel(dimension);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the host {@link TableLayoutInfo}.
   */
  public final TableLayoutInfo getLayout() {
    return m_layout;
  }

  /**
   * @return the {@link DimensionInfo} model.
   */
  public final DimensionInfo getDimension() {
    return m_dimension;
  }

  /**
   * @return the offset of {@link Figure} with headers relative to the absolute layer.
   */
  public final Point getOffset() {
    Point offset = new Point(0, 0);
    FigureUtils.translateFigureToAbsolute2(m_containerFigure, offset);
    offset.translate(m_layout.getContainer().getClientAreaInsets());
    return offset;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dragging
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final Tool getDragTrackerTool(Request request) {
    return new ParentTargetDragEditPartTracker(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refreshVisuals() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        getFigure().setToolTipText(m_dimension.getTitle());
        getFigure().setBackground(COLOR_NORMAL);
      }
    });
  }
}
