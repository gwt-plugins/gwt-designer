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

import com.google.gdt.eclipse.designer.uibinder.gef.policy.GridRowLayoutEditPolicy;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.GridInfo.Row;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;

/**
 * {@link EditPart} for {@link Row}.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.gef
 */
public final class GridRowEditPart extends GridGridElementEditPart {
  private final Row m_row;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridRowEditPart(Row row) {
    super(row);
    m_row = row;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(EditPolicy.LAYOUT_ROLE, new GridRowLayoutEditPolicy(m_row));
  }
}
