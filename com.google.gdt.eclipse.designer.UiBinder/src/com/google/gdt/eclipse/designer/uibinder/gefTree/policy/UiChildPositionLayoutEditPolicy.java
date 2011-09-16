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
package com.google.gdt.eclipse.designer.uibinder.gefTree.policy;

import com.google.gdt.eclipse.designer.uibinder.model.util.UiChildSupport.Position;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.policy.validator.ComponentClassLayoutRequestValidator;
import org.eclipse.wb.core.gefTree.policy.ObjectLayoutEditPolicy;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for {@link Position}.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.gefTree
 */
public final class UiChildPositionLayoutEditPolicy extends ObjectLayoutEditPolicy<WidgetInfo> {
  private final ILayoutRequestValidator m_validator;
  private final Position m_position;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UiChildPositionLayoutEditPolicy(Position position) {
    super(position);
    m_position = position;
    {
      String widgetClassName = position.getWidgetClass().getCanonicalName();
      m_validator = new ComponentClassLayoutRequestValidator(widgetClassName);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart.getModel() instanceof WidgetInfo;
  }

  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return m_validator;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Command getCommand(Request request) {
    if (request.getType() == Request.REQ_CREATE
        || request.getType() == Request.REQ_PASTE
        || request.getType() == Request.REQ_ADD) {
      if (!m_position.canAddChild()) {
        return null;
      }
    }
    return super.getCommand(request);
  }

  @Override
  protected void command_CREATE(WidgetInfo widget, WidgetInfo reference) throws Exception {
    m_position.command_CREATE(widget, reference);
  }

  @Override
  protected void command_MOVE(WidgetInfo widget, WidgetInfo reference) throws Exception {
    m_position.command_MOVE(widget, reference);
  }
}