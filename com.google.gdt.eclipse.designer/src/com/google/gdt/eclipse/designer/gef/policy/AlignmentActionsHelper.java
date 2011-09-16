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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.ISelectionEditPolicyListener;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

import java.util.List;

/**
 * Helper for displaying alignment actions for selected {@link EditPart}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public abstract class AlignmentActionsHelper<C extends IAbstractComponentInfo> {
  private final SelectionEditPolicy m_policy;
  private final GraphicalEditPart m_editPart;
  private final IEditPartViewer m_viewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AlignmentActionsHelper(SelectionEditPolicy policy) {
    m_policy = policy;
    m_editPart = policy.getHost();
    m_viewer = m_editPart.getViewer();
    m_policy.addSelectionPolicyListener(new ISelectionEditPolicyListener() {
      public void showSelection(SelectionEditPolicy policy) {
        if (m_editPart.getSelected() == EditPart.SELECTED_PRIMARY) {
          showAlignmentFigures();
        }
      }

      public void hideSelection(SelectionEditPolicy policy) {
        hideAlignmentFigures();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment figures
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final int MIN_LEFT_SPACE = 10;
  private static final int INITIAL_RIGHT_SPACE = 10;
  private static final int FIGURES_SPACE = 10;
  private List<Figure> m_alignmentFigures;

  /**
   * @return the alignment figure for given component and axis.
   */
  protected abstract Figure createAlignmentFigure(C component, boolean horizontal);

  /**
   * Shows alignment figures for host {@link EditPart}.
   */
  private void showAlignmentFigures() {
    if (m_alignmentFigures == null) {
      m_alignmentFigures = Lists.newArrayList();
      showAlignmentsFigures(m_editPart);
    }
  }

  /**
   * Hides alignment figures for host {@link EditPart}.
   */
  private void hideAlignmentFigures() {
    if (m_alignmentFigures != null) {
      for (Figure figure : m_alignmentFigures) {
        FigureUtils.removeFigure(figure);
      }
      m_alignmentFigures = null;
    }
  }

  /**
   * Shows all possible cell figures for given edit part.
   */
  @SuppressWarnings("unchecked")
  private void showAlignmentsFigures(EditPart editPart) {
    C component = (C) editPart.getModel();
    int offset = INITIAL_RIGHT_SPACE;
    {
      Figure horizontalFigure = createAlignmentFigure(component, true);
      if (horizontalFigure != null) {
        offset += horizontalFigure.getSize().width;
        addAlignmentFigure(component, horizontalFigure, offset);
        offset += FIGURES_SPACE;
      }
    }
    {
      Figure verticalFigure = createAlignmentFigure(component, false);
      if (verticalFigure != null) {
        offset += verticalFigure.getSize().width;
        addAlignmentFigure(component, verticalFigure, offset);
        offset += FIGURES_SPACE;
      }
    }
  }

  /**
   * Adds alignment figure at given offset from right side of component's cells.
   */
  private void addAlignmentFigure(IAbstractComponentInfo component, Figure figure, int offset) {
    Figure layer = m_viewer.getLayer(IEditPartViewer.CLICKABLE_LAYER);
    // prepare rectangle for cells used by component (in layer coordinates)
    Rectangle cellRect;
    {
      cellRect = component.getModelBounds().getCopy();
      PolicyUtils.translateModelToFeedback(m_policy, cellRect);
    }
    // prepare location and size
    Point figureLocation;
    {
      Dimension figureSize = figure.getSize();
      figureLocation = new Point(cellRect.right() - offset, cellRect.y - figureSize.height / 2);
      if (figureLocation.x < cellRect.x + MIN_LEFT_SPACE) {
        return;
      }
    }
    // add alignment figure
    layer.add(figure);
    figure.setLocation(figureLocation);
    m_alignmentFigures.add(figure);
  }
}
