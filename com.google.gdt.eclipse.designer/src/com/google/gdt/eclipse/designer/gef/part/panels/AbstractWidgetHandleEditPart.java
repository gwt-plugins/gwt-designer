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
package com.google.gdt.eclipse.designer.gef.part.panels;

import com.google.gdt.eclipse.designer.model.widgets.panels.AbstractWidgetHandle;

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

/**
 * {@link EditPart} for {@link AbstractWidgetHandle}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.part
 */
public class AbstractWidgetHandleEditPart extends GraphicalEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractWidgetHandleEditPart(AbstractWidgetHandle<?> handle) {
    setModel(handle);
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
    Rectangle bounds = getHandle().getBounds();
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
    installEditPolicy(new NonResizableSelectionEditPolicy());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    super.performRequest(request);
    if (Request.REQ_OPEN.equals(request.getType())) {
      getHandle().show();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  private AbstractWidgetHandle<?> getHandle() {
    return (AbstractWidgetHandle<?>) getModel();
  }
}
