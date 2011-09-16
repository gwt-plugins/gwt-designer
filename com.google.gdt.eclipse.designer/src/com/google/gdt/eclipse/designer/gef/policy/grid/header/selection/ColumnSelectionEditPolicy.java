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
package com.google.gdt.eclipse.designer.gef.policy.grid.header.selection;

import com.google.gdt.eclipse.designer.gef.policy.grid.header.edit.ColumnHeaderEditPart;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.ColumnInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;

import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link ColumnHeaderEditPart}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class ColumnSelectionEditPolicy extends DimensionSelectionEditPolicy<ColumnInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
    super(mainPolicy);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Keyboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    super.performRequest(request);
    if (request instanceof KeyRequest) {
      KeyRequest keyRequest = (KeyRequest) request;
      if (keyRequest.isPressed()) {
        char c = keyRequest.getCharacter();
        if (c == 'd' || c == 'u') {
          setAlignment(ColumnInfo.Alignment.UNKNOWN);
        } else if (c == 'l') {
          setAlignment(ColumnInfo.Alignment.LEFT);
        } else if (c == 'c') {
          setAlignment(ColumnInfo.Alignment.CENTER);
        } else if (c == 'r') {
          setAlignment(ColumnInfo.Alignment.RIGHT);
        } else if (c == 'f') {
        }
      }
    }
  }

  /**
   * Sets the alignment for {@link ColumnInfo}.
   */
  private void setAlignment(final ColumnInfo.Alignment alignment) {
    final HTMLTableInfo panel = getPanel();
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        getDimension().setAlignment(alignment);
      }
    });
  }
}
