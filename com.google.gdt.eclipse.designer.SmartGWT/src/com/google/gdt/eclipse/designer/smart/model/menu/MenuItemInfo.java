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

import com.google.gdt.eclipse.designer.smart.model.JsObjectInfo;

import org.eclipse.wb.core.model.broadcast.JavaInfoChildBeforeAssociation;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.menu.MenuItem</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class MenuItemInfo extends JsObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuItemInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addBroadcastListener(new JavaInfoChildBeforeAssociation(this));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the parent {@link MenuInfo}.
   */
  public MenuInfo getMenu() {
    return (MenuInfo) getParentJava();
  }

  /**
   * @return the submenu {@link MenuInfo} if presented.
   */
  public MenuInfo getSubmenu() {
    List<MenuInfo> children = getChildren(MenuInfo.class);
    return children.size() == 0 ? null : children.get(0);
  }

  @Override
  public boolean isCreated() {
    return super.isCreated() && getMenu().isCreated();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    if (isCreated()) {
      ReflectionUtils.invokeMethod(
          getObject(),
          "setSubmenu(com.smartgwt.client.widgets.menu.Menu)",
          (Object) null);
    }
    //
    super.refresh_dispose();
  }
}
