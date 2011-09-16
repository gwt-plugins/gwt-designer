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

import com.google.gdt.eclipse.designer.gef.policy.grid.header.edit.RowHeaderEditPart;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.grid.RowInfo;

import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link RowHeaderEditPart}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class RowSelectionEditPolicy extends DimensionSelectionEditPolicy<RowInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
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
        // vertical
        if (c == 'd' || c == 'D' || c == 'u' || c == 'U') {
          setAlignment(RowInfo.Alignment.UNKNOWN);
        } else if (c == 't') {
          setAlignment(RowInfo.Alignment.TOP);
        } else if (c == 'm' || c == 'M' || c == 'c' || c == 'C') {
          setAlignment(RowInfo.Alignment.MIDDLE);
        } else if (c == 'b') {
          setAlignment(RowInfo.Alignment.BOTTOM);
        }
      }
    }
  }

  /**
   * Sets the alignment for {@link RowInfo}.
   */
  private void setAlignment(final RowInfo.Alignment alignment) {
    final HTMLTableInfo panel = getPanel();
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        getDimension().setAlignment(alignment);
      }
    });
  }
}
