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
package com.google.gdt.eclipse.designer.gef.policy.grid;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.CellConstraintsSupport;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.ColumnInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.RowInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.generic.AbstractPopupFigure;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridSelectionEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link HTMLTableInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class FlexSelectionEditPolicy extends AbstractGridSelectionEditPolicy {
  private final HTMLTableInfo m_layout;
  private final WidgetInfo m_component;
  private final FlexGridHelper m_gridHelper = new FlexGridHelper(this, false);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FlexSelectionEditPolicy(HTMLTableInfo panel, WidgetInfo component) {
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
    IEditPartViewer viewer = getHost().getViewer();
    final CellConstraintsSupport constraints = HTMLTableInfo.getConstraints((WidgetInfo) component);
    if (horizontal) {
      return new AbstractPopupFigure(viewer, 9, 5) {
        @Override
        protected Image getImage() {
          return constraints.getHorizontalAlignment().getSmallImage();
        }

        @Override
        protected void fillMenu(IMenuManager manager) {
          constraints.fillHorizontalAlignmentMenu(manager);
        }
      };
    } else {
      return new AbstractPopupFigure(viewer, 5, 9) {
        @Override
        protected Image getImage() {
          return constraints.getVerticalAlignment().getSmallImage();
        }

        @Override
        protected void fillMenu(IMenuManager manager) {
          constraints.fillVerticalAlignmentMenu(manager);
        }
      };
    }
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
        m_layout.command_CELLS(m_component, cells);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Keyboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    if (request instanceof KeyRequest) {
      KeyRequest keyRequest = (KeyRequest) request;
      if (keyRequest.isPressed()) {
        char c = keyRequest.getCharacter();
        // horizontal
        if (c == 'd' || c == 'u') {
          setHorizontalAlignment(ColumnInfo.Alignment.UNKNOWN);
        } else if (c == 'l') {
          setHorizontalAlignment(ColumnInfo.Alignment.LEFT);
        } else if (c == 'c') {
          setHorizontalAlignment(ColumnInfo.Alignment.CENTER);
        } else if (c == 'f') {
          setHorizontalAlignment(ColumnInfo.Alignment.FILL);
        } else if (c == 'r') {
          setHorizontalAlignment(ColumnInfo.Alignment.RIGHT);
        }
        // vertical
        if (c == 'D' || c == 'U') {
          setVerticalAlignment(RowInfo.Alignment.UNKNOWN);
        } else if (c == 't') {
          setVerticalAlignment(RowInfo.Alignment.TOP);
        } else if (c == 'm') {
          setVerticalAlignment(RowInfo.Alignment.MIDDLE);
        } else if (c == 'F') {
          setVerticalAlignment(RowInfo.Alignment.FILL);
        } else if (c == 'b') {
          setVerticalAlignment(RowInfo.Alignment.BOTTOM);
        }
      }
    }
  }

  /**
   * Sets the horizontal alignment.
   */
  private void setHorizontalAlignment(final ColumnInfo.Alignment alignment) {
    ExecutionUtils.run(m_layout, new RunnableEx() {
      public void run() throws Exception {
        CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(m_component);
        constraints.setHorizontalAlignment(alignment);
      }
    });
  }

  /**
   * Sets the vertical alignment.
   */
  private void setVerticalAlignment(final RowInfo.Alignment alignment) {
    ExecutionUtils.run(m_layout, new RunnableEx() {
      public void run() throws Exception {
        CellConstraintsSupport constraints = HTMLTableInfo.getConstraints(m_component);
        constraints.setVerticalAlignment(alignment);
      }
    });
  }
}
