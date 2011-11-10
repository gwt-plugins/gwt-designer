/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gdt.eclipse.designer.gxt.model.widgets.menu;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;

import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;

import java.util.List;

/**
 * Utilities for GXT menu.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public final class MenuUtils {
  private static final String KEY_MENU_UTILS_ITEM = "Surrogate IMenuItemInfo for Component_Info";

  /**
   * @return the {@link IMenuItemInfo}'s of given {@link MenuBarInfo}.
   */
  public static List<IMenuItemInfo> getItems(MenuBarInfo container) {
    List<IMenuItemInfo> items = Lists.newArrayList();
    for (MenuBarItemInfo item : container.getItems()) {
      IMenuItemInfo itemObject = getMenuItem(item);
      if (itemObject != null) {
        items.add(itemObject);
      }
    }
    return items;
  }

  /**
   * @return the {@link IMenuItemInfo}'s of given {@link MenuInfo}.
   */
  public static List<IMenuItemInfo> getItems(MenuInfo container) {
    List<IMenuItemInfo> items = Lists.newArrayList();
    for (ComponentInfo item : container.getItems()) {
      IMenuItemInfo itemObject = getMenuItem(item);
      if (itemObject != null) {
        items.add(itemObject);
      }
    }
    return items;
  }

  /**
   * @return the {@link IMenuItemInfo} wrapper for given {@link Object}.
   */
  public static IMenuItemInfo getMenuItem(ComponentInfo component) {
    {
      IMenuItemInfo item = MenuObjectInfoUtils.getMenuItemInfo(component);
      if (item != null) {
        return item;
      }
    }
    {
      IMenuItemInfo item = (IMenuItemInfo) component.getArbitraryValue(KEY_MENU_UTILS_ITEM);
      if (item == null) {
        item = new ComponentMenuItemInfo(component);
        component.putArbitraryValue(KEY_MENU_UTILS_ITEM, item);
      }
      return item;
    }
  }

  /**
   * Sets {@link ComponentInfo} which is menu item and should be selected (and expanded) during
   * refresh.
   */
  public static void setSelectingItem(ComponentInfo component) {
    IMenuItemInfo item = getMenuItem(component);
    MenuObjectInfoUtils.setSelectingObject(item);
  }
}
