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
package com.google.gdt.eclipse.designer.gef.policy;

import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.ILocator;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.RelativeLocator;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * {@link SelectionLayoutEditPolicy} for resizing, that displays rectangle and show text hint.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public abstract class AbstractResizeSelectionEditPolicy extends SelectionEditPolicy {
  private final String REQ_RESIZE = getClass().getName() + " RESIZE";
  protected boolean m_ctrlPressed;
  protected int m_resizeDirection;
  protected Dimension m_sizeDelta;
  protected Dimension m_newSize;
  private Rectangle m_newBounds;
  protected Command m_command;
  protected String m_tooltip;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final Handle createResizeHandle(int direction) {
    ResizeHandle handle = new ResizeHandle(getHost(), direction);
    handle.setDragTrackerTool(new ResizeTracker(direction, REQ_RESIZE));
    return handle;
  }

  protected final Handle createResizeHandle(int direction, double percent, final Color fillColor) {
    ILocator locator = createComponentLocator(direction, percent);
    ResizeHandle handle = new ResizeHandle(getHost(), direction, locator) {
      @Override
      protected Color getBorderColor() {
        return IColorConstants.black;
      }

      @Override
      protected Color getFillColor() {
        return isPrimary() ? fillColor : IColorConstants.white;
      }
    };
    handle.setDragTrackerTool(new ResizeTracker(direction, REQ_RESIZE));
    return handle;
  }

  /**
   * @return {@link ILocator} that positions handles on component side.
   */
  private final ILocator createComponentLocator(int direction, double percent) {
    Figure reference = getHostFigure();
    if (direction == IPositionConstants.WEST) {
      return new RelativeLocator(reference, 0, percent);
    } else if (direction == IPositionConstants.EAST) {
      return new RelativeLocator(reference, 1, percent);
    } else if (direction == IPositionConstants.NORTH) {
      return new RelativeLocator(reference, percent, 0);
    } else if (direction == IPositionConstants.SOUTH) {
      return new RelativeLocator(reference, percent, 1);
    }
    throw new IllegalArgumentException("Unknown direction: " + direction);
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
  public Command getCommand(Request request) {
    // use such "indirect" command because when we press Ctrl and _don't_ move mouse after
    // this, we will show correct feedback text (without hint), and set correct m_command,
    // but GEF already asked command and will not ask it again
    return new Command() {
      @Override
      public void execute() throws Exception {
        getHost().getViewer().getEditDomain().executeCommand(m_command);
      }
    };
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

  @Override
  public void performRequest(Request request) {
    super.performRequest(request);
    if (request instanceof KeyRequest) {
      KeyRequest keyRequest = (KeyRequest) request;
      if (keyRequest.getKeyCode() == SWT.CTRL) {
        m_ctrlPressed = keyRequest.isPressed();
      }
      if (isResizing()) {
        updateTooltipCommand();
        updateTooltipText();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  private Figure m_resizeFeedback;
  private TextFeedback m_textFeedback;

  private void showResizeFeedback(ChangeBoundsRequest request) {
    m_ctrlPressed = request.isControlKeyPressed();
    m_sizeDelta = request.getSizeDelta();
    m_resizeDirection = request.getResizeDirection();
    prepareFeedbackSize(request);
    prepareFeedbackBounds(request);
    //
    updateTooltipCommand();
    showRectangleFeedback();
    showTextFeedback(request);
  }

  protected void prepareFeedbackSize(ChangeBoundsRequest request) {
    m_newSize = getHostFigure().getSize();
    m_newSize.expand(m_sizeDelta);
  }

  private void prepareFeedbackBounds(ChangeBoundsRequest request) {
    Figure hostFigure = getHostFigure();
    m_newBounds = hostFigure.getBounds().getCopy();
    // update bounds
    if (m_resizeDirection == IPositionConstants.WEST) {
      m_newBounds.moveX(-m_sizeDelta.width);
    } else if (m_resizeDirection == IPositionConstants.NORTH) {
      m_newBounds.moveY(-m_sizeDelta.height);
    } else {
      m_newBounds.setSize(m_newSize);
    }
    // translate to feedback
    FigureUtils.translateFigureToAbsolute(hostFigure, m_newBounds);
    FigureUtils.translateAbsoluteToFigure2(getFeedbackLayer(), m_newBounds);
  }

  private void showRectangleFeedback() {
    if (m_resizeFeedback == null) {
      m_resizeFeedback = new Figure();
      m_resizeFeedback.setBorder(new LineBorder(IColorConstants.orange));
      addFeedback(m_resizeFeedback);
    }
    // update
    m_resizeFeedback.setBounds(m_newBounds);
  }

  private void showTextFeedback(ChangeBoundsRequest request) {
    if (m_textFeedback == null) {
      m_textFeedback = new TextFeedback(getFeedbackLayer());
      m_textFeedback.add();
    }
    // update
    updateTooltipText();
    m_textFeedback.setLocation(request.getLocation().getTranslated(10, 10));
  }

  private void eraseResizeFeedback(ChangeBoundsRequest request) {
    if (m_resizeFeedback != null) {
      FigureUtils.removeFigure(m_resizeFeedback);
      m_resizeFeedback = null;
    }
    if (m_textFeedback != null) {
      m_textFeedback.remove();
      m_textFeedback = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if resize is in progress.
   */
  protected final boolean isResizing() {
    return m_resizeFeedback != null;
  }

  /**
   * Sets the tooltip text.
   */
  protected final void updateTooltipText() {
    m_textFeedback.setText(m_tooltip);
  }

  /**
   * @return the width of parent figure.
   */
  protected final int getParentWidth() {
    return getHostFigure().getParent().getBounds().width;
  }

  /**
   * @return the height of parent figure.
   */
  protected final int getParentHeight() {
    return getHostFigure().getParent().getBounds().height;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method should update {@link #m_tooltip} and {@link #m_command} fields.
   */
  protected abstract void updateTooltipCommand();
}