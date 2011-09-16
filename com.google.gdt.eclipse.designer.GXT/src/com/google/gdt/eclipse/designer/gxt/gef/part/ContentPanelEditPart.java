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

import com.google.gdt.eclipse.designer.gxt.gef.policy.ContentPanelLayoutEditPolicy;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ContentPanelInfo;

import org.eclipse.wb.gef.core.EditPart;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link EditPart} for {@link ContentPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.part
 */
public class ContentPanelEditPart extends LayoutContainerEditPart {
  private final ContentPanelInfo m_panel;
  private final Object m_buttonBar = new Object();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ContentPanelEditPart(ContentPanelInfo panel) {
    super(panel);
    m_panel = panel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    installEditPolicy(new ContentPanelLayoutEditPolicy(m_panel));
    super.createEditPolicies();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<?> getModelChildren() {
    List<Object> children = new ArrayList<Object>(super.getModelChildren());
    children.add(m_buttonBar);
    children.removeAll(m_panel.getButtonBarButtons());
    return children;
  }

  @Override
  protected EditPart createEditPart(Object model) {
    if (model == m_buttonBar) {
      return new ContentPanelButtonBarEditPart(m_panel, model);
    }
    return super.createEditPart(model);
  }
}
