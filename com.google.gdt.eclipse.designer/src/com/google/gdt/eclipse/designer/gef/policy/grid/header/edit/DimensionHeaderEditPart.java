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
package com.google.gdt.eclipse.designer.gef.policy.grid.header.edit;

import com.google.gdt.eclipse.designer.model.widgets.panels.grid.DimensionInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;

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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * {@link EditPart} for column/row header of {@link HTMLTableInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public abstract class DimensionHeaderEditPart<T extends DimensionInfo> extends GraphicalEditPart
    implements
      IHeaderMenuProvider {
  protected static final Color COLOR_NORMAL = Headers.COLOR_HEADER;
  protected static final Color COLOR_EMPTY = new Color(null, 255, 235, 235);
  protected static final Font DEFAULT_FONT = new Font(null, "Arial", 7, SWT.NONE);
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final HTMLTableInfo m_panel;
  protected final T m_dimension;
  private final Figure m_containerFigure;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionHeaderEditPart(HTMLTableInfo panel, T dimension, Figure containerFigure) {
    m_panel = panel;
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
   * @return the index of this {@link DimensionInfo}.
   */
  public final int getIndex() {
    return m_dimension.getIndex();
  }

  /**
   * @return the host {@link HTMLTableInfo}.
   */
  public final HTMLTableInfo getPanel() {
    return m_panel;
  }

  /**
   * @return the {@link DimensionInfo} model.
   */
  public final T getDimension() {
    return m_dimension;
  }

  /**
   * @return the offset of {@link Figure} with headers relative to the absolute layer.
   */
  public final Point getOffset() {
    Point offset = new Point(0, 0);
    FigureUtils.translateFigureToAbsolute2(m_containerFigure, offset);
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
    // update background
    {
      getFigure().setBackground(COLOR_NORMAL);
      if (m_dimension.isEmpty()) {
        getFigure().setBackground(COLOR_EMPTY);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    super.performRequest(request);
    if (request.getType() == Request.REQ_OPEN) {
      editDimension();
    }
  }

  /**
   * Opens the {@link DimensionInfo} edit dialog.
   */
  protected abstract void editDimension();
}
