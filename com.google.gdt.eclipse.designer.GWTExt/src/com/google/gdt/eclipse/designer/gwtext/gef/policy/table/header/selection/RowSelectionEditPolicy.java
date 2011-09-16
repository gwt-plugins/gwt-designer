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
package com.google.gdt.eclipse.designer.gwtext.gef.policy.table.header.selection;

import com.google.gdt.eclipse.designer.gwtext.gef.policy.table.header.edit.RowHeaderEditPart;

import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link RowHeaderEditPart}.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.gef.TableLayout
 */
public final class RowSelectionEditPolicy extends DimensionSelectionEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
    super(mainPolicy);
  }
}
