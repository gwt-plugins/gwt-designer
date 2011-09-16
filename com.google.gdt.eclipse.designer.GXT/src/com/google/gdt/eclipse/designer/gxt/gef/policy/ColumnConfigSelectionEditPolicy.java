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
import com.google.gdt.eclipse.designer.gxt.gef.part.ColumnConfigEditPart;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ColumnConfigInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.RectangleFigure;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.SideResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;

import java.util.List;

/**
 * {@link SelectionEditPolicy} for {@link ColumnConfigEditPart}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.policy
 */
public final class ColumnConfigSelectionEditPolicy extends SelectionEditPolicy {
  private static final String REQ_RESIZE = "resize";
  private final ColumnConfigInfo m_column;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnConfigSelectionEditPolicy(ColumnConfigInfo column) {
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
    MoveHandle moveHandle = new MoveHandle(getHost());
    moveHandle.setForeground(IColorConstants.red);
    handles.add(moveHandle);
    //
    return handles;
  }

  @Override
  protected List<Handle> createStaticHandles() {
    List<Handle> handles = Lists.newArrayList();
    // create resize column handle
    SideResizeHandle resizeHandle =
        new SideResizeHandle(getHost(), IPositionConstants.RIGHT, 10, true);
    resizeHandle.setDragTrackerTool(new ResizeTracker(getHost(),
        IPositionConstants.EAST,
        REQ_RESIZE));
    handles.add(resizeHandle);
    //
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
  public Command getCommand(final Request request) {
    return getResizeCommand((ChangeBoundsRequest) request);
  }

  @Override
  public void showSourceFeedback(Request request) {
    showResizeFeedback((ChangeBoundsRequest) request);
  }

  @Override
  public void eraseSourceFeedback(Request request) {
    eraseResizeFeedback((ChangeBoundsRequest) request);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  private Figure m_resizeFeedback;
  private TextFeedback m_textFeedback;

  //
  private Command getResizeCommand(ChangeBoundsRequest request) {
    final Rectangle newBounds = request.getTransformedRectangle(getHostFigure().getBounds());
    return new EditCommand(m_column) {
      @Override
      protected void executeEdit() throws Exception {
        int newWidth = Math.max(newBounds.width, 0);
        m_column.setWidth(newWidth);
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
      FigureUtils.translateFigureToAbsolute(hostFigure, bounds.shrink(-1, -1));
    }
    // update selection feedback
    m_resizeFeedback.setBounds(bounds);
    // update text feedback
    int newWidth = Math.max(bounds.width - 2, 0);
    m_textFeedback.setText(Integer.toString(newWidth));
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