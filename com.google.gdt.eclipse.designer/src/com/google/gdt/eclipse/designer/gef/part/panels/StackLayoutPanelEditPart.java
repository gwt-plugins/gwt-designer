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
package com.google.gdt.eclipse.designer.gef.part.panels;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gef.policy.TabLikePanelHandleLayoutEditPolicy;
import com.google.gdt.eclipse.designer.gef.policy.TabLikePanelWidgetLayoutEditPolicy;
import com.google.gdt.eclipse.designer.model.widgets.panels.StackLayoutPanelInfo;

import org.eclipse.wb.gef.core.EditPart;

import java.util.List;

/**
 * {@link EditPart} for {@link StackLayoutPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.part
 */
public final class StackLayoutPanelEditPart extends PanelEditPart {
  private final StackLayoutPanelInfo m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StackLayoutPanelEditPart(StackLayoutPanelInfo panel) {
    super(panel);
    m_panel = panel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(TabLikePanelWidgetLayoutEditPolicy.create(m_panel, false));
    installEditPolicy(TabLikePanelHandleLayoutEditPolicy.create(m_panel, false));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<?> getModelChildren() {
    List<Object> children = Lists.newArrayList();
    children.addAll(super.getModelChildren());
    children.addAll(m_panel.getWidgetHandles());
    return children;
  }
}
