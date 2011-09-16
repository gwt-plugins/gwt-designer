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

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gxt.model.widgets.TabPanelInfo;

import org.eclipse.wb.gef.core.EditPart;

import java.util.List;

/**
 * {@link EditPart} for {@link TabPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.part
 */
public class TabPanelEditPart extends ComponentEditPart {
  protected final TabPanelInfo m_portal;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TabPanelEditPart(TabPanelInfo portal) {
    super(portal);
    m_portal = portal;
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
    children.addAll(m_portal.getHeaders());
    return children;
  }

  @Override
  protected EditPart createEditPart(Object model) {
    return super.createEditPart(model);
  }
}
