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
package com.google.gdt.eclipse.designer.gxt.gef.part;

import com.google.gdt.eclipse.designer.gxt.gef.policy.DropLayoutEditPolicy;
import com.google.gdt.eclipse.designer.gxt.gef.policy.UnknownLayoutEditPolicy;
import com.google.gdt.eclipse.designer.gxt.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;

import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * {@link EditPart} for {@link LayoutContainerInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.part
 */
public class LayoutContainerEditPart extends ComponentEditPart {
  protected final LayoutContainerInfo m_container;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutContainerEditPart(LayoutContainerInfo container) {
    super(container);
    m_container = container;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  private LayoutInfo m_currentLayout;

  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    // support for dropping LayoutInfo's
    if (m_container.hasLayout()) {
      installEditPolicy(new DropLayoutEditPolicy(m_container));
    }
  }

  @Override
  protected void refreshEditPolicies() {
    super.refreshEditPolicies();
    // support for dropping components
    if (m_container.hasLayout()) {
      LayoutInfo layout = m_container.getLayout();
      if (m_currentLayout != layout) {
        m_currentLayout = layout;
        LayoutEditPolicy policy = LayoutPolicyUtils.createLayoutEditPolicy(this, layout);
        if (policy == null) {
          policy = new UnknownLayoutEditPolicy();
        }
        installEditPolicy(EditPolicy.LAYOUT_ROLE, policy);
      }
    }
  }
}
