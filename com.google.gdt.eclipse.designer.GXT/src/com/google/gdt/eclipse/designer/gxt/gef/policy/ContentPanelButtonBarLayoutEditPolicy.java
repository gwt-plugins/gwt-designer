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

import com.google.gdt.eclipse.designer.gxt.model.widgets.ButtonInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ContentPanelInfo;

import org.eclipse.wb.core.gef.policy.layout.flow.ObjectFlowLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for button bar of {@link ContentPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.policy
 */
public final class ContentPanelButtonBarLayoutEditPolicy
    extends
      ObjectFlowLayoutEditPolicy<ButtonInfo> {
  public static final ILayoutRequestValidator VALIDATOR =
      LayoutRequestValidators.modelType(ButtonInfo.class);
  private final ContentPanelInfo m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ContentPanelButtonBarLayoutEditPolicy(ContentPanelInfo panel) {
    super(panel);
    m_panel = panel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return VALIDATOR;
  }

  @Override
  protected boolean isHorizontal(Request request) {
    return true;
  }

  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart.getModel() instanceof ButtonInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(ButtonInfo newObject, ButtonInfo referenceObject) throws Exception {
    m_panel.command_ButtonBar_CREATE(newObject, referenceObject);
  }

  @Override
  protected void command_MOVE(ButtonInfo object, ButtonInfo referenceObject) throws Exception {
    m_panel.command_ButtonBar_MOVE(object, referenceObject);
  }
}