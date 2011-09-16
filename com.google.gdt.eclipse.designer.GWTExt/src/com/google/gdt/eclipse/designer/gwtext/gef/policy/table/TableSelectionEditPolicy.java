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
package com.google.gdt.eclipse.designer.gwtext.gef.policy.table;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gwtext.model.layout.table.TableLayoutInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridSelectionEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

import java.util.List;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link TableLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.gef.TableLayout
 */
public final class TableSelectionEditPolicy extends AbstractGridSelectionEditPolicy {
  private final TableLayoutInfo m_layout;
  private final WidgetInfo m_component;
  private final TableGridHelper m_gridHelper = new TableGridHelper(this, false);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableSelectionEditPolicy(TableLayoutInfo panel, WidgetInfo component) {
    super(component);
    m_layout = panel;
    m_component = component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isActiveLayout() {
    return true;
  }

  @Override
  protected IGridInfo getGridInfo() {
    return m_layout.getGridInfo();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handlesList = Lists.newArrayList();
    // add move handle
    handlesList.add(createMoveHandle());
    // add span handles
    {
      handlesList.add(createSpanHandle(IPositionConstants.NORTH, 0.25));
      handlesList.add(createSpanHandle(IPositionConstants.WEST, 0.25));
      handlesList.add(createSpanHandle(IPositionConstants.EAST, 0.75));
      handlesList.add(createSpanHandle(IPositionConstants.SOUTH, 0.75));
    }
    //
    return handlesList;
  }

  @Override
  protected void showPrimarySelection() {
    super.showPrimarySelection();
    m_gridHelper.showGridFeedback();
  }

  @Override
  protected void hideSelection() {
    m_gridHelper.eraseGridFeedback();
    super.hideSelection();
  }

  @Override
  protected Figure createAlignmentFigure(IAbstractComponentInfo component, boolean horizontal) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Span
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command createSpanCommand(final boolean horizontal, final Rectangle cells) {
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        m_layout.command_setCells(m_component, cells, true);
      }
    };
  }
}
