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

import com.google.gdt.eclipse.designer.gef.WidgetsLayoutRequestValidator;
import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.policy.layout.flow.ObjectFlowLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.core.model.IObjectInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * Abstract {@link LayoutEditPolicy} for typical {@link IWidgetInfo} flow container.
 * 
 * @author sablin_aa
 * @coverage gwt.gef.policy
 */
public abstract class WidgetFlowLayoutEditPolicy<T extends IWidgetInfo>
    extends
      ObjectFlowLayoutEditPolicy<T> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetFlowLayoutEditPolicy(IObjectInfo host) {
    super(host);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Decoration
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    if (child.getModel() instanceof WidgetInfo) {
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, new NonResizableSelectionEditPolicy());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final ILayoutRequestValidator getRequestValidator() {
    return WidgetsLayoutRequestValidator.INSTANCE;
  }

  @Override
  protected final boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart.getModel() instanceof WidgetInfo;
  }
}
