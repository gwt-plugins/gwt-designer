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

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gef.policy.TabLikePanelHandleLayoutEditPolicy;
import com.google.gdt.eclipse.designer.gef.policy.TabLikePanelWidgetLayoutEditPolicy;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.TabPanelInfo;

import org.eclipse.wb.gef.core.EditPart;

import java.util.List;

/**
 * {@link EditPart} for {@link TabPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.gef
 */
public final class TabPanelEditPart extends UIObjectEditPart {
  private final TabPanelInfo m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TabPanelEditPart(TabPanelInfo panel) {
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
    installEditPolicy(TabLikePanelWidgetLayoutEditPolicy.create(m_panel, true));
    installEditPolicy(TabLikePanelHandleLayoutEditPolicy.create(m_panel, true));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<?> getModelChildren() {
    List<Object> children = Lists.newArrayList();
    children.add(m_panel.getActiveWidget());
    children.addAll(m_panel.getWidgetHandles());
    return children;
  }
}
