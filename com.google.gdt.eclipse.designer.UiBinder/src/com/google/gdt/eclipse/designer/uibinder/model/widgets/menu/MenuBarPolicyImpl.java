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

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.UIObjectInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import java.util.List;

/**
 * Implementation of {@link IMenuPolicy} for {@link MenuBarInfo}.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
final class MenuBarPolicyImpl implements IMenuPolicy {
  private final MenuBarInfo m_menu;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  MenuBarPolicyImpl(MenuBarInfo menu) {
    m_menu = menu;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean validateCreate(Object newObject) {
    return isValidObjectType(newObject);
  }

  @SuppressWarnings("unchecked")
  public boolean validatePaste(final Object mementoObject) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        if (mementoObject instanceof List) {
          List<XmlObjectMemento> mementos = (List<XmlObjectMemento>) mementoObject;
          for (XmlObjectMemento memento : mementos) {
            XmlObjectInfo component = memento.create(m_menu);
            if (!isValidObjectType(component)) {
              return false;
            }
          }
          return true;
        }
        return false;
      }
    }, false);
  }

  public boolean validateMove(Object object) {
    if (isValidObjectType(object)) {
      ObjectInfo component = (ObjectInfo) object;
      // don't move item on its child menu
      return !component.isParentOf(m_menu);
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public void commandCreate(Object newObject, Object nextObject) throws Exception {
    UIObjectInfo newItem = (UIObjectInfo) newObject;
    UIObjectInfo nextItem = (UIObjectInfo) nextObject;
    // add new item
    XmlObjectUtils.add(newItem, Associations.direct(), m_menu, nextItem);
    // schedule selection
    MenuObjectInfoUtils.setSelectingObject(newItem);
  }

  @SuppressWarnings("unchecked")
  public List<?> commandPaste(Object mementoObject, Object nextObject) throws Exception {
    List<UIObjectInfo> pastedObjects = Lists.newArrayList();
    List<XmlObjectMemento> mementos = (List<XmlObjectMemento>) mementoObject;
    for (XmlObjectMemento memento : mementos) {
      UIObjectInfo newItem = (UIObjectInfo) memento.create(m_menu);
      commandCreate(newItem, nextObject);
      memento.apply();
      pastedObjects.add(newItem);
    }
    return pastedObjects;
  }

  public void commandMove(Object object, Object nextObject) throws Exception {
    UIObjectInfo item = (UIObjectInfo) object;
    UIObjectInfo nextItem = (UIObjectInfo) nextObject;
    // move item
    XmlObjectUtils.move(item, Associations.direct(), m_menu, nextItem);
    // schedule selection
    MenuObjectInfoUtils.setSelectingObject(item);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if object has valid type.
   */
  private static boolean isValidObjectType(Object object) {
    return object instanceof MenuItemInfo || object instanceof MenuItemSeparatorInfo;
  }
}