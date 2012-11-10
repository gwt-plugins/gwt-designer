/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gdt.eclipse.designer.gef.policy.grid;

import com.google.gdt.eclipse.designer.gef.WidgetsLayoutRequestValidator;
import com.google.gdt.eclipse.designer.gef.policy.UIObjectSelectionEditPolicy;
import com.google.gdt.eclipse.designer.gef.policy.grid.header.edit.ColumnHeaderEditPart;
import com.google.gdt.eclipse.designer.gef.policy.grid.header.edit.RowHeaderEditPart;
import com.google.gdt.eclipse.designer.gef.policy.grid.header.layout.ColumnsLayoutEditPolicy;
import com.google.gdt.eclipse.designer.gef.policy.grid.header.layout.RowsLayoutEditPolicy;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.ColumnInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.RowInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2.IPasteProcessor;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;

import org.eclipse.jface.action.IMenuManager;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link HTMLTableInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class FlexLayoutEditPolicy extends AbstractGridLayoutEditPolicy {
  private final HTMLTableInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FlexLayoutEditPolicy(HTMLTableInfo panel) {
    super(panel);
    m_layout = panel;
    m_gridTargetHelper = new FlexGridHelper(this, true);
    m_gridSelectionHelper = new FlexGridHelper(this, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return WidgetsLayoutRequestValidator.INSTANCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
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
  protected void decorateChild(EditPart child) {
    if (child.getModel() instanceof WidgetInfo) {
      WidgetInfo widget = (WidgetInfo) child.getModel();
      EditPolicy selectionPolicy = new FlexSelectionEditPolicy(m_layout, widget);
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, new UIObjectSelectionEditPolicy(widget));
      child.installEditPolicy(selectionPolicy);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(CreateRequest request) {
    if (m_target.m_valid) {
      final WidgetInfo component = (WidgetInfo) request.getNewObject();
      return new EditCommand(m_layout) {
        @Override
        protected void executeEdit() throws Exception {
          m_layout.command_CREATE(
              component,
              m_target.m_column,
              m_target.m_columnInsert,
              m_target.m_row,
              m_target.m_rowInsert);
        }
      };
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Command getPasteCommand(PasteRequest request) {
    List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) request.getMemento();
    if (m_target.m_valid && mementos.size() == 1) {
      return LayoutPolicyUtils2.getPasteCommand(
          m_layout,
          request,
          WidgetInfo.class,
          new IPasteProcessor<WidgetInfo>() {
            public void process(WidgetInfo component) throws Exception {
              m_layout.command_CREATE(
                  component,
                  m_target.m_column,
                  m_target.m_columnInsert,
                  m_target.m_row,
                  m_target.m_rowInsert);
            }
          });
    }
    return null;
  }

  @Override
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    if (m_target.m_valid && request.getEditParts().size() == 1) {
      EditPart moveEditPart = request.getEditParts().get(0);
      if (moveEditPart.getModel() instanceof WidgetInfo) {
        final WidgetInfo component = (WidgetInfo) moveEditPart.getModel();
        return new EditCommand(m_layout) {
          @Override
          protected void executeEdit() throws Exception {
            m_layout.command_MOVE(
                component,
                m_target.m_column,
                m_target.m_columnInsert,
                m_target.m_row,
                m_target.m_rowInsert);
          }
        };
      }
    }
    return null;
  }

  @Override
  protected Command getAddCommand(ChangeBoundsRequest request) {
    if (m_target.m_valid && request.getEditParts().size() == 1) {
      EditPart moveEditPart = request.getEditParts().get(0);
      if (moveEditPart.getModel() instanceof WidgetInfo) {
        final WidgetInfo component = (WidgetInfo) moveEditPart.getModel();
        return new EditCommand(m_layout) {
          @Override
          protected void executeEdit() throws Exception {
            m_layout.command_MOVE(
                component,
                m_target.m_column,
                m_target.m_columnInsert,
                m_target.m_row,
                m_target.m_rowInsert);
          }
        };
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Grid target
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Determines parameters of insert feedback.
   * 
   * @return the array of: visual gap, begin/end of insert feedback, begin/end of target feedback.
   */
  public static int[] getInsertFeedbackParameters(Interval interval,
      Interval nextInterval,
      int minGap) {
    int gap = nextInterval.begin - interval.end();
    int visualGap = Math.max(gap, minGap);
    // determine x1/x2
    int x1, x2;
    {
      int a = interval.end();
      int b = nextInterval.begin;
      int x1_2 = a + b - visualGap;
      x1 = x1_2 % 2 == 0 ? x1_2 / 2 : x1_2 / 2 - 1;
      x2 = a + b - x1;
      // we don't want to have insert feedback be same as intervals
      if (x1 == a - 1) {
        x1--;
        x2++;
      }
    }
    //
    return new int[]{visualGap, x1, x2, x1 - minGap, x2 + minGap};
  }

  @Override
  protected void updateGridTarget(Point mouseLocation) throws Exception {
    m_target = new GridTarget();
    // prepare location in model
    Point location = mouseLocation.getCopy();
    PolicyUtils.translateAbsoluteToModel(this, location);
    // prepare grid information
    IGridInfo gridInfo = m_layout.getGridInfo();
    Interval[] columnIntervals = gridInfo.getColumnIntervals();
    Interval[] rowIntervals = gridInfo.getRowIntervals();
    int lastX = columnIntervals.length != 0 ? columnIntervals[columnIntervals.length - 1].end() : 0;
    int lastY = rowIntervals.length != 0 ? rowIntervals[rowIntervals.length - 1].end() : 0;
    // prepare insert bounds
    {
      if (columnIntervals.length != 0) {
        m_target.m_rowInsertBounds.x = columnIntervals[0].begin - INSERT_MARGINS;
        m_target.m_rowInsertBounds.setRight(lastX + INSERT_MARGINS);
      } else {
        m_target.m_rowInsertBounds.x = 0;
        m_target.m_rowInsertBounds.setRight(getHostFigure().getSize().width);
      }
      if (rowIntervals.length != 0) {
        m_target.m_columnInsertBounds.y = rowIntervals[0].begin - INSERT_MARGINS;
        m_target.m_columnInsertBounds.setBottom(lastY + INSERT_MARGINS);
      } else {
        m_target.m_columnInsertBounds.y = 0;
        m_target.m_columnInsertBounds.setBottom(getHostFigure().getSize().height);
      }
    }
    // find existing column
    for (int columnIndex = 0; columnIndex < columnIntervals.length; columnIndex++) {
      boolean isFirst = columnIndex == 0;
      boolean isLast = columnIndex == columnIntervals.length - 1;
      Interval interval = columnIntervals[columnIndex];
      Interval nextInterval = !isLast ? columnIntervals[columnIndex + 1] : null;
      // before first
      if (isFirst) {
        boolean nearBegin = Math.abs(location.x - interval.begin()) < INSERT_COLUMN_SIZE;
        if (nearBegin) {
          m_target.m_column = 0;
          m_target.m_columnInsert = true;
          updateGridFeedback_column(new Interval(0, 0), interval);
          // stop
          break;
        }
      }
      // gap or near to end of interval
      if (!isLast) {
        int gap = nextInterval.begin - interval.end();
        boolean directGap = interval.end() <= location.x && location.x < nextInterval.begin;
        boolean narrowGap = gap < 2 * INSERT_COLUMN_SIZE;
        boolean nearEnd = Math.abs(location.x - interval.end()) < INSERT_COLUMN_SIZE;
        boolean nearBegin = Math.abs(location.x - nextInterval.begin) < INSERT_COLUMN_SIZE;
        if (directGap || narrowGap && (nearEnd || nearBegin)) {
          m_target.m_column = columnIndex + 1;
          m_target.m_columnInsert = true;
          updateGridFeedback_column(interval, nextInterval);
          // stop
          break;
        }
      }
      // prepare "append" parameters
      if (isLast) {
        boolean nearEnd = Math.abs(location.x - interval.end()) < INSERT_COLUMN_SIZE;
        if (nearEnd) {
          m_target.m_column = columnIndex + 1;
          m_target.m_columnInsert = false;
          updateGridFeedback_column(interval, new Interval(interval.end(), 0));
          // stop
          break;
        }
      }
      // column
      if (interval.contains(location.x)) {
        m_target.m_column = columnIndex;
        // feedback
        m_target.m_feedbackBounds.x = interval.begin;
        m_target.m_feedbackBounds.width = interval.length + 1;
        // stop
        break;
      }
    }
    // find existing row
    for (int rowIndex = 0; rowIndex < rowIntervals.length; rowIndex++) {
      boolean isFirst = rowIndex == 0;
      boolean isLast = rowIndex == rowIntervals.length - 1;
      Interval interval = rowIntervals[rowIndex];
      Interval nextInterval = !isLast ? rowIntervals[rowIndex + 1] : null;
      // before first
      if (isFirst) {
        boolean nearBegin = Math.abs(location.y - interval.begin()) < INSERT_ROW_SIZE;
        if (nearBegin) {
          m_target.m_row = 0;
          m_target.m_rowInsert = true;
          updateGridFeedback_row(new Interval(0, 0), interval);
          // stop
          break;
        }
      }
      // gap or near to end of interval
      if (!isLast) {
        int gap = nextInterval.begin - interval.end();
        boolean directGap = interval.end() <= location.y && location.y < nextInterval.begin;
        boolean narrowGap = gap < 2 * INSERT_ROW_SIZE;
        boolean nearEnd = Math.abs(location.y - interval.end()) < INSERT_ROW_SIZE;
        boolean nearBegin = Math.abs(location.y - nextInterval.begin) < INSERT_ROW_SIZE;
        if (directGap || narrowGap && (nearEnd || nearBegin)) {
          m_target.m_row = rowIndex + 1;
          m_target.m_rowInsert = true;
          updateGridFeedback_row(interval, nextInterval);
          // stop
          break;
        }
      }
      // prepare "append" parameters
      if (isLast) {
        boolean nearEnd = Math.abs(location.y - interval.end()) < 2 * INSERT_ROW_SIZE;
        if (nearEnd) {
          m_target.m_row = rowIndex + 1;
          m_target.m_rowInsert = false;
          updateGridFeedback_row(interval, new Interval(interval.end(), 0));
          // stop
          break;
        }
      }
      // row
      if (interval.contains(location.y)) {
        m_target.m_row = rowIndex;
        // feedback
        m_target.m_feedbackBounds.y = interval.begin;
        m_target.m_feedbackBounds.height = interval.length + 1;
        // stop
        break;
      }
    }
    // if still no cell, use (0,0)
    if (m_target.m_column == -1) {
      m_target.m_column = 0;
      Rectangle clientArea = getHostFigure().getClientArea();
      m_target.m_feedbackBounds.x = clientArea.x + 5;
      m_target.m_feedbackBounds.width = clientArea.width - 5 - 5;
    }
    if (m_target.m_row == -1) {
      m_target.m_row = 0;
      Rectangle clientArea = getHostFigure().getClientArea();
      m_target.m_feedbackBounds.y = clientArea.y + 5;
      m_target.m_feedbackBounds.height = clientArea.height - 5 - 5;
    }
  }

  private void updateGridFeedback_column(Interval interval, Interval nextInterval) {
    // prepare parameters
    int[] parameters = getInsertFeedbackParameters(interval, nextInterval, INSERT_COLUMN_SIZE);
    // feedback
    m_target.m_feedbackBounds.x = parameters[3];
    m_target.m_feedbackBounds.width = parameters[4] - parameters[3];
    // insert
    m_target.m_columnInsertBounds.x = parameters[1];
    m_target.m_columnInsertBounds.width = parameters[2] - parameters[1];
  }

  private void updateGridFeedback_row(Interval interval, Interval nextInterval) {
    // prepare parameters
    int[] parameters = getInsertFeedbackParameters(interval, nextInterval, INSERT_ROW_SIZE);
    // feedback
    m_target.m_feedbackBounds.y = parameters[3];
    m_target.m_feedbackBounds.height = parameters[4] - parameters[3];
    // insert
    m_target.m_rowInsertBounds.y = parameters[1];
    m_target.m_rowInsertBounds.height = parameters[2] - parameters[1];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IHeadersProvider 
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutEditPolicy getContainerLayoutPolicy(boolean horizontal) {
    if (horizontal) {
      return new ColumnsLayoutEditPolicy(this, m_layout);
    } else {
      return new RowsLayoutEditPolicy(this, m_layout);
    }
  }

  public List<?> getHeaders(boolean horizontal) {
    return horizontal ? m_layout.getColumns() : m_layout.getRows();
  }

  public EditPart createHeaderEditPart(boolean horizontal, Object model) {
    if (horizontal) {
      return new ColumnHeaderEditPart(m_layout, (ColumnInfo) model, getHostFigure());
    } else {
      return new RowHeaderEditPart(m_layout, (RowInfo) model, getHostFigure());
    }
  }

  public void buildContextMenu(IMenuManager manager, boolean horizontal) {
  }

  public void handleDoubleClick(boolean horizontal) {
  }
}
