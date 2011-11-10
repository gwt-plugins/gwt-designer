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
package com.google.gdt.eclipse.designer.gef.policy;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.widgets.cell.CellTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.cell.ColumnInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.RectangleFigure;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.SideResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.core.model.property.Property;

import java.util.List;

/**
 * {@link SelectionEditPolicy} that shows simple rectangle selection around {@link EditPart} and one
 * column resize {@link Handle}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class CellTableColumnSelectionEditPolicy extends SelectionEditPolicy {
  private static final String REQ_RESIZE = "resize";
  private final ColumnInfo m_column;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CellTableColumnSelectionEditPolicy(CellTableInfo table, ColumnInfo column) {
    m_column = column;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    // create move column handle
    {
      MoveHandle moveHandle = new MoveHandle(getHost());
      moveHandle.setForeground(IColorConstants.red);
      handles.add(moveHandle);
    }
    // create resize column handle
    {
      Handle resizeHandle = new SideResizeHandle(getHost(), IPositionConstants.RIGHT, 10, true);
      ResizeTracker tracker = new ResizeTracker(getHost(), IPositionConstants.EAST, REQ_RESIZE);
      resizeHandle.setDragTrackerTool(tracker);
      handles.add(resizeHandle);
    }
    // done
    return handles;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Routing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean understandsRequest(Request request) {
    return super.understandsRequest(request) || request.getType() == REQ_RESIZE;
  }

  @Override
  //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "BC_UNCONFIRMED_CAST")
  public Command getCommand(Request request) {
    return getResizeCommand((ChangeBoundsRequest) request);
  }

  @Override
  public void showSourceFeedback(Request request) {
    if (request instanceof ChangeBoundsRequest) {
      showResizeFeedback((ChangeBoundsRequest) request);
    }
  }

  @Override
  public void eraseSourceFeedback(Request request) {
    if (request instanceof ChangeBoundsRequest) {
      eraseResizeFeedback((ChangeBoundsRequest) request);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  private Figure m_resizeFeedback;
  private TextFeedback m_textFeedback;

  private Command getResizeCommand(ChangeBoundsRequest request) {
    final Rectangle newBounds = request.getTransformedRectangle(getHostFigure().getBounds());
    return new EditCommand(m_column) {
      @Override
      protected void executeEdit() throws Exception {
        Property widthProperty = m_column.getWidthProperty();
        if (widthProperty != null) {
          widthProperty.setValue(newBounds.width + "px");
        }
      }
    };
  }

  private void showResizeFeedback(ChangeBoundsRequest request) {
    if (m_resizeFeedback == null) {
      // create selection feedback
      {
        m_resizeFeedback = new RectangleFigure();
        m_resizeFeedback.setForeground(IColorConstants.red);
        addFeedback(m_resizeFeedback);
      }
      // create text feedback
      {
        m_textFeedback = new TextFeedback(getFeedbackLayer());
        m_textFeedback.add();
      }
    }
    // prepare bounds
    Rectangle bounds;
    {
      Figure hostFigure = getHostFigure();
      bounds = request.getTransformedRectangle(hostFigure.getBounds());
      FigureUtils.translateFigureToAbsolute(hostFigure, bounds.expand(1, 1));
    }
    // update selection feedback
    m_resizeFeedback.setBounds(bounds);
    // update text feedback
    m_textFeedback.setText(Integer.toString(bounds.width - 2));
    m_textFeedback.setLocation(request.getLocation().getTranslated(10, 10));
  }

  private void eraseResizeFeedback(ChangeBoundsRequest request) {
    // erase selection feedback
    removeFeedback(m_resizeFeedback);
    m_resizeFeedback = null;
    // erase text feedback
    m_textFeedback.remove();
    m_textFeedback = null;
  }
}