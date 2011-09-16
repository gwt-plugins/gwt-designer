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
package com.google.gdt.eclipse.designer.gxt.gef.part;

import com.google.gdt.eclipse.designer.gxt.gef.policy.ColumnConfigSelectionEditPolicy;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ColumnConfigInfo;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

/**
 * {@link EditPart} for {@link ColumnConfigInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.part
 */
public class ColumnConfigEditPart extends GraphicalEditPart {
  protected ColumnConfigInfo m_column;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnConfigEditPart(ColumnConfigInfo column) {
    m_column = column;
    setModel(m_column);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    return new Figure();
  }

  @Override
  protected void refreshVisuals() {
    Rectangle bounds = m_column.getBounds();
    getFigure().setBounds(bounds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(EditPolicy.SELECTION_ROLE, new ColumnConfigSelectionEditPolicy(m_column));
  }
}
