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
package com.google.gdt.eclipse.designer.model.widgets.menu;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.widgets.UIObjectInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.generation.GenerationUtils;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.variable.EmptyPureVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import java.util.List;

/**
 * Implementation of {@link IMenuPolicy} for {@link MenuBarInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
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
          List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) mementoObject;
          for (JavaInfoMemento memento : mementos) {
            JavaInfo component = memento.create(m_menu);
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
    if (newItem instanceof MenuItemInfo) {
      if (newItem.getCreationSupport() instanceof ImplicitFactoryCreationSupport) {
        VariableSupport variableSupport = new EmptyPureVariableSupport(newItem);
        StatementGenerator statementGenerator = GenerationUtils.getStatementGenerator(newItem);
        JavaInfoUtils.add(newItem, variableSupport, statementGenerator, null, m_menu, nextItem);
      } else {
        JavaInfoUtils.add(newItem, getNewItemAssociation(), m_menu, nextItem);
      }
    } else if (newItem instanceof MenuItemSeparatorInfo) {
      JavaInfoUtils.add(newItem, getNewSeparatorAssociation(), m_menu, nextItem);
    }
    // schedule selection
    MenuObjectInfoUtils.setSelectingObject(newItem);
  }

  @SuppressWarnings("unchecked")
  public List<?> commandPaste(final Object mementoObject, final Object nextObject) throws Exception {
    List<UIObjectInfo> pastedObjects = Lists.newArrayList();
    List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) mementoObject;
    for (JavaInfoMemento memento : mementos) {
      UIObjectInfo newItem = (UIObjectInfo) memento.create(m_menu);
      commandCreate(newItem, nextObject);
      memento.apply();
      pastedObjects.add(newItem);
    }
    return pastedObjects;
  }

  public void commandMove(Object object, Object nextObject) throws Exception {
    JavaInfo item = (JavaInfo) object;
    JavaInfo nextItem = (JavaInfo) nextObject;
    // move item
    if (item instanceof MenuItemInfo) {
      JavaInfoUtils.move(item, getNewItemAssociation(), m_menu, nextItem);
    } else if (item instanceof MenuItemSeparatorInfo) {
      JavaInfoUtils.move(item, getNewSeparatorAssociation(), m_menu, nextItem);
    }
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

  private AssociationObject getNewItemAssociation() {
    return AssociationObjects.invocationChild("%parent%.addItem(%child%)", false);
  }

  private AssociationObject getNewSeparatorAssociation() {
    return AssociationObjects.invocationChild("%parent%.addSeparator(%child%)", false);
  }
}