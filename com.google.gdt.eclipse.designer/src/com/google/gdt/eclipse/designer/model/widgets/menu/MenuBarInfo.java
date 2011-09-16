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
package com.google.gdt.eclipse.designer.model.widgets.menu;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.widgets.UIObjectInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.model.widgets.support.UIObjectUtils;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.JavaMenuMenuObject;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Model for <code>MenuBar</code> widget.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class MenuBarInfo extends WidgetInfo implements IAdaptable {
  private final MenuBarInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuBarInfo(AstEditor editor,
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
   * @return {@link MenuItemInfo} children.
   */
  public List<MenuItemInfo> getItems() {
    return getChildren(MenuItemInfo.class);
  }

  /**
   * @return {@link MenuItemInfo} or {@link MenuItemSeparatorInfo} children.
   */
  public List<UIObjectInfo> getAllItems() {
    List<UIObjectInfo> items = Lists.newArrayList();
    for (ObjectInfo child : getChildren()) {
      if (child instanceof MenuItemInfo || child instanceof MenuItemSeparatorInfo) {
        items.add((UIObjectInfo) child);
      }
    }
    return items;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    fetchVisualData();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean isTopLevel() {
    return !(getParent() instanceof MenuItemInfo);
  }

  private boolean isVertical() {
    return ReflectionUtils.getFieldBoolean(getObject(), "vertical");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visual data
  //
  ////////////////////////////////////////////////////////////////////////////
  private void fetchVisualData() throws Exception {
    if (isTopLevel()) {
      fetchAsTopMenu();
    } else {
      fetchAsSubMenu();
    }
  }

  private void fetchAsTopMenu() throws Exception {
    fetchItemBounds();
  }

  private void fetchAsSubMenu() throws Exception {
    Object popup = showThisMenuPopup();
    try {
      fetchFromMenuPopup(popup);
    } finally {
      callMenuBar_close(getObject());
    }
  }

  private Object showThisMenuPopup() throws Exception {
    Object parentItem = getParentJava().getObject();
    Object parentMenu = ReflectionUtils.invokeMethod(parentItem, "getParentMenu()");
    ReflectionUtils.invokeMethod(parentItem, "setEnabled(boolean)", true);
    call_MenuBar_doItemAction(parentMenu, parentItem);
    //
    removeFirstItemSelection();
    return ReflectionUtils.getFieldObject(parentMenu, "popup");
  }

  private static void callMenuBar_close(Object menuBar) throws Exception {
    try {
      // GWT 2.1
      ReflectionUtils.invokeMethod(menuBar, "close(boolean)", false);
    } catch (Throwable e) {
      // GWT < 2.1
      ReflectionUtils.invokeMethod(menuBar, "close()");
    }
  }

  private static void call_MenuBar_doItemAction(Object menu, Object item) throws Exception {
    try {
      // GWT 2.0.1
      ReflectionUtils.invokeMethod(
          menu,
          "doItemAction(com.google.gwt.user.client.ui.MenuItem,boolean,boolean)",
          item,
          false,
          false);
    } catch (Throwable e) {
      // GWT < 2.0.1
      ReflectionUtils.invokeMethod(
          menu,
          "doItemAction(com.google.gwt.user.client.ui.MenuItem,boolean)",
          item,
          false);
    }
  }

  private void removeFirstItemSelection() throws Exception {
    Object selectedItem = ReflectionUtils.getFieldObject(getObject(), "selectedItem");
    if (selectedItem != null) {
      ReflectionUtils.invokeMethod(selectedItem, "setSelectionStyle(boolean)", false);
    }
  }

  private void fetchFromMenuPopup(Object popup) throws Exception {
    fetchSubMenuBounds(popup);
    fetchItemBounds();
    setImage(getPopupImage(popup));
  }

  private void fetchSubMenuBounds(Object popup) throws Exception {
    Object menuElement = getState().getUIObjectUtils().getElement(popup);
    fetchBounds(menuElement);
  }

  private Image getPopupImage(Object popup) throws Exception {
    Rectangle popupBounds = getReasonablePopupBounds(popup);
    setPopupPosition(popup, popupBounds.x, popupBounds.y);
    // make shot of whole browser - we don't have other way to get screen shot of menu
    List<Object> hiddenWidgets = getUIObjectUtils().hideRootPanelWidgets(popup);
    try {
      Image browserImage = getState().getShell().createBrowserScreenshot();
      return UiUtils.getCroppedImage(browserImage, popupBounds.getSwtRectangle());
    } finally {
      UIObjectUtils.showWidgets(hiddenWidgets);
    }
  }

  private Rectangle getReasonablePopupBounds(Object popup) throws Exception {
    Dimension popupSize = getPopupSize(popup);
    return new Rectangle(50, 50, popupSize.width, popupSize.height);
  }

  private Dimension getPopupSize(Object popup) throws Exception {
    GwtState state = getState();
    Object popupElement = state.getUIObjectUtils().getElement(popup);
    return state.getAbsoluteBounds(popupElement).getSize();
  }

  private static void setPopupPosition(Object popup, int left, int top) throws Exception {
    ReflectionUtils.invokeMethod(popup, "setPopupPosition(int,int)", left, top);
  }

  private void fetchItemBounds() throws Exception {
    GwtState state = getState();
    for (UIObjectInfo item : getAllItems()) {
      Object itemElement = item.getElement();
      Rectangle itemBounds = state.getAbsoluteBounds(itemElement);
      absoluteToRelative(itemBounds);
      item.setBounds(itemBounds);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IMenuInfo m_menuImpl = new MenuImpl();
  private final IMenuPolicy m_menuPolicyImpl = new MenuBarPolicyImpl(this);

  public <T> T getAdapter(Class<T> adapter) {
    if (adapter.isAssignableFrom(IMenuInfo.class)) {
      return adapter.cast(m_menuImpl);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractMenuImpl
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Abstract superclass for {@link IMenuObjectInfo} implementations.
   * 
   * @author scheglov_ke
   */
  private abstract class MenuAbstractImpl extends JavaMenuMenuObject {
    public MenuAbstractImpl() {
      super(m_this);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link IMenuInfo}.
   * 
   * @author scheglov_ke
   */
  private final class MenuImpl extends MenuAbstractImpl implements IMenuInfo {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Model
    //
    ////////////////////////////////////////////////////////////////////////////
    public Object getModel() {
      return m_this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    public Image getImage() {
      return m_this.getImage();
    }

    public Rectangle getBounds() {
      return m_this.getBounds();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean isHorizontal() {
      return !isVertical();
    }

    public List<IMenuItemInfo> getItems() {
      List<IMenuItemInfo> items = Lists.newArrayList();
      for (ObjectInfo object : getAllItems()) {
        IMenuItemInfo item = MenuObjectInfoUtils.getMenuItemInfo(object);
        items.add(item);
      }
      return items;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Policy
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuPolicy getPolicy() {
      return m_menuPolicyImpl;
    }
  }
}
