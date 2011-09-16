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
package com.google.gdt.eclipse.designer.smart.model.menu;

import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.menu.MenuButton</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class MenuButtonInfo extends CanvasInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuButtonInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the children {@link MenuInfo}.
   */
  public MenuInfo getMenu() {
    List<MenuInfo> children = getChildren(MenuInfo.class);
    return children.isEmpty() ? (MenuInfo) null : children.get(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    // detach Menu
    if (isCreated()) {
      ReflectionUtils.invokeMethod(
          getObject(),
          "setMenu(com.smartgwt.client.widgets.menu.Menu)",
          (Object) null);
    }
    //
    super.refresh_dispose();
  }
}
