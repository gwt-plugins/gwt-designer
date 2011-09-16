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
package com.google.gdt.eclipse.designer.smart.gef.part;

import com.google.gdt.eclipse.designer.gef.part.UIObjectEditPart;
import com.google.gdt.eclipse.designer.smart.gef.policy.CanvasLayoutEditPolicy;
import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;

/**
 * {@link EditPart} for {@link CanvasInfo}.
 * 
 * @author scheglov_ke
 * @coverage SmartGWT.gef.part
 */
public final class CanvasEditPart extends UIObjectEditPart {
  private final CanvasInfo m_canvas;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CanvasEditPart(CanvasInfo canvas) {
    super(canvas);
    m_canvas = canvas;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    if (m_canvas.isExactlyCanvas()) {
      installEditPolicy(EditPolicy.LAYOUT_ROLE, new CanvasLayoutEditPolicy(m_canvas));
    }
  }
}
