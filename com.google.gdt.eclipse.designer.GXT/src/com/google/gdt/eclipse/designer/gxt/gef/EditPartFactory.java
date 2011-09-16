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
package com.google.gdt.eclipse.designer.gxt.gef;

import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.gxt.model.widgets.menu.ItemInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.menu.MenuBarInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.menu.MenuBarItemInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.menu.MenuInfo;

import org.eclipse.wb.core.gef.MatchingEditPartFactory;
import org.eclipse.wb.core.gef.part.menu.MenuEditPartFactory;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;

/**
 * {@link IEditPartFactory} for Ext-GWT widgets.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef
 */
public final class EditPartFactory implements IEditPartFactory {
  private final IEditPartFactory[] FACTORIES = {MENU_FACTORY, MATCHING_FACTORY};
  private final static IEditPartFactory MATCHING_FACTORY =
      new MatchingEditPartFactory(ImmutableList.of("com.google.gdt.eclipse.designer.gxt.model.widgets"),
          ImmutableList.of("com.google.gdt.eclipse.designer.gxt.gef.part"));

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditPart createEditPart(EditPart context, Object model) {
    // use generic EditPart for placeholders
    if (model instanceof JavaInfo) {
      JavaInfo javaInfo = (JavaInfo) model;
      if (javaInfo.isPlaceholder()) {
        return null;
      }
    }
    // ask factories
    for (IEditPartFactory factory : FACTORIES) {
      EditPart editPart = factory.createEditPart(null, model);
      if (editPart != null) {
        return editPart;
      }
    }
    // no
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Factories
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final IEditPartFactory MENU_FACTORY = new IEditPartFactory() {
    public EditPart createEditPart(EditPart context, Object model) {
      if (model instanceof MenuBarInfo) {
        MenuBarInfo menu = (MenuBarInfo) model;
        IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menu);
        return MenuEditPartFactory.createMenu(menu, menuObject);
      }
      if (model instanceof MenuBarItemInfo) {
        MenuBarItemInfo item = (MenuBarItemInfo) model;
        IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(item);
        return MenuEditPartFactory.createMenuItem(item, itemObject);
      }
      if (model instanceof MenuInfo) {
        MenuInfo menu = (MenuInfo) model;
        if (menu.isSubMenu()) {
          IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menu);
          return MenuEditPartFactory.createMenu(menu, menuObject);
        } else {
          IMenuPopupInfo popupObject = MenuObjectInfoUtils.getMenuPopupInfo(menu);
          return MenuEditPartFactory.createPopupMenu(menu, popupObject);
        }
      }
      if (model instanceof ItemInfo) {
        ItemInfo item = (ItemInfo) model;
        IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(item);
        return MenuEditPartFactory.createMenuItem(item, itemObject);
      }
      if (model instanceof IMenuInfo) {
        IMenuInfo menu = (IMenuInfo) model;
        return MenuEditPartFactory.createMenu(menu, menu);
      }
      return null;
    }
  };
}