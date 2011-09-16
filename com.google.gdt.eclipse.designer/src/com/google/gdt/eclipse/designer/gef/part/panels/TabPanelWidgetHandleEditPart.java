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

import com.google.gdt.eclipse.designer.gef.policy.AbstractWidgetHandleDirectTextEditPolicy;
import com.google.gdt.eclipse.designer.model.widgets.panels.AbstractWidgetHandle;
import com.google.gdt.eclipse.designer.model.widgets.panels.TabPanelInfo;

import org.eclipse.wb.gef.core.EditPart;

/**
 * {@link EditPart} for {@link TabPanelInfo} header.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.part
 */
public final class TabPanelWidgetHandleEditPart extends AbstractWidgetHandleEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TabPanelWidgetHandleEditPart(AbstractWidgetHandle<?> handle) {
    super(handle);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    AbstractWidgetHandleDirectTextEditPolicy.install(this, "TabText");
  }
}
