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
package com.google.gdt.eclipse.designer.uibinder.gef.part;

import com.google.gdt.eclipse.designer.uibinder.model.widgets.GridInfo.GridElement;

import org.eclipse.wb.core.gef.policy.selection.MoveSelectionEditPolicy;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.core.xml.gef.part.AbstractComponentEditPart;

/**
 * {@link EditPart} for {@link GridElement}.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.gef
 */
public class GridGridElementEditPart extends AbstractComponentEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridGridElementEditPart(GridElement element) {
    super(element);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(EditPolicy.SELECTION_ROLE, new MoveSelectionEditPolicy(IColorConstants.blue));
  }
}
