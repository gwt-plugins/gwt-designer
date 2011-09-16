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
package com.google.gdt.eclipse.designer.uibinder.model.widgets.menu;

import com.google.gdt.eclipse.designer.uibinder.model.widgets.UIObjectInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAdd;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Model for <code>com.google.gwt.user.client.ui.MenuItem</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class MenuItemInfo extends UIObjectInfo implements IAdaptable {
  private final MenuItemInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuItemInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    addSubMenuOnItemAdding();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addSubMenuOnItemAdding() {
    addBroadcastListener(new XmlObjectAdd() {
      @Override
      public void after(ObjectInfo parent, XmlObjectInfo child) throws Exception {
        if (child == m_this && XmlObjectUtils.hasTrueParameter(m_this, "MenuItem.withSubMenu")) {
          addNewSubMenu();
        }
      }
    });
  }

  private void addNewSubMenu() throws Exception {
    MenuBarInfo subMenu =
        (MenuBarInfo) XmlObjectUtils.createObject(
            getContext(),
            "com.google.gwt.user.client.ui.MenuBar",
            new ElementCreationSupport("vertical"));
    XmlObjectUtils.add(subMenu, Associations.direct(), this, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MenuBarInfo} child or <code>null</code>.
   */
  public MenuBarInfo getSubMenu() {
    List<MenuBarInfo> menuChildren = getChildren(MenuBarInfo.class);
    return GenericsUtils.getFirstOrNull(menuChildren);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IMenuItemInfo m_itemImpl = new MenuItemImpl();

  public <T> T getAdapter(Class<T> adapter) {
    if (adapter.isAssignableFrom(IMenuItemInfo.class)) {
      return adapter.cast(m_itemImpl);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuItemInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link IMenuItemInfo}.
   * 
   * @author scheglov_ke
   */
  private final class MenuItemImpl extends AbstractMenuObject implements IMenuItemInfo {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MenuItemImpl() {
      super(MenuItemInfo.this);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Model
    //
    ////////////////////////////////////////////////////////////////////////////
    public Object getModel() {
      return MenuItemInfo.this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    public Image getImage() {
      return null;
    }

    public Rectangle getBounds() {
      return MenuItemInfo.this.getBounds();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuItemInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuInfo getMenu() {
      MenuBarInfo menu = getSubMenu();
      if (menu != null) {
        return MenuObjectInfoUtils.getMenuInfo(menu);
      } else {
        return null;
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Policy
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuPolicy getPolicy() {
      return IMenuPolicy.NOOP;
    }
  }
}
