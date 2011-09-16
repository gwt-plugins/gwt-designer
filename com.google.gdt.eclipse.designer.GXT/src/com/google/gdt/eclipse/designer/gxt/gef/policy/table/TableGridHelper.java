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
package com.google.gdt.eclipse.designer.gxt.gef.policy.table;

import com.google.gdt.eclipse.designer.gxt.model.layout.table.TableLayoutInfo;

import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridHelper;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.gef.graphical.policies.GraphicalEditPolicy;

/**
 * Helper for displaying grid for {@link TableLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.TableLayout
 */
public final class TableGridHelper extends AbstractGridHelper {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableGridHelper(GraphicalEditPolicy editPolicy, boolean forTarget) {
    super(editPolicy, forTarget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected IGridInfo getGridInfo() {
    return ((TableLayoutInfo) getAbstractLayout()).getGridInfo();
  }
}
