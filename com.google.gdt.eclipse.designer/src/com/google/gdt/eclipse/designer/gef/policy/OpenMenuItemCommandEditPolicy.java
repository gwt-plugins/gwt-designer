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
package com.google.gdt.eclipse.designer.gef.policy;

import com.google.gdt.eclipse.designer.model.widgets.menu.MenuItemInfo;

import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;

/**
 * Instance of {@link EditPolicy} that calls {@link MenuItemInfo#openCommand()}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class OpenMenuItemCommandEditPolicy extends EditPolicy {
  private final MenuItemInfo m_item;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public OpenMenuItemCommandEditPolicy(MenuItemInfo item) {
    m_item = item;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void performRequest(Request request) {
    if (Request.REQ_OPEN.equals(request.getType())) {
      m_item.openCommand();
    }
    super.performRequest(request);
  }
}
