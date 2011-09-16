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
package com.google.gdt.eclipse.designer.gwtext.model.widgets;

import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.EvaluationEventListener;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoChildBeforeAssociation;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.AbstractDescription;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * Model for <code>CycleButton</code>.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage GWTExt.model
 */
public class CycleButtonInfo extends ButtonInfo {
  private final CycleButtonInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CycleButtonInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addBroadcastListeners_forItems();
    addBroadcastListener(new JavaInfoChildBeforeAssociation(this));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<CheckItemInfo> getCheckItems() {
    return getChildren(CheckItemInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    renderItems();
  }

  /**
   * Render items to force fetching default property values.
   */
  private void renderItems() throws Exception {
    ReflectionUtils.invokeMethod(getObject(), "showMenu()");
    try {
      for (CheckItemInfo checkItem : getCheckItems()) {
        checkItem.getDescription().visit(checkItem, AbstractDescription.STATE_OBJECT_READY);
      }
    } finally {
      ReflectionUtils.invokeMethod(getObject(), "hideMenu()");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners for items
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addBroadcastListeners_forItems() {
    addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (parent == m_this && !isDeleting()) {
          if (getCheckItems().isEmpty()) {
            // add default item after deleting last one
            addDefaultCheckItem();
          } else {
            // ensure that even if checked item was deleted, we check left item
            ensureCheckItems_model();
          }
        }
      }
    });
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        // CycleButton must have at least one CheckItem
        if (child == m_this) {
          addDefaultCheckItem();
        }
        // ensure that after adding at least one CheckItem is checked {
        if (parent == m_this && child instanceof CheckItemInfo) {
          ensureCheckItems_model();
        }
      }
    });
    // ensure that at one and only one CheckItem object is checked
    addBroadcastListener(new EvaluationEventListener() {
      @Override
      public void evaluateBefore(EvaluationContext context, ASTNode node) throws Exception {
        if (isButtonInvocation(node)) {
          ensureCheckItems_object();
        }
      }

      private boolean isButtonInvocation(ASTNode node) {
        if (node instanceof MethodInvocation) {
          MethodInvocation invocation = (MethodInvocation) node;
          return isRepresentedBy(invocation.getExpression());
        }
        return false;
      }
    });
  }

  private void addDefaultCheckItem() throws Exception {
    CheckItemInfo item =
        (CheckItemInfo) JavaInfoUtils.createJavaInfo(
            getEditor(),
            "com.gwtext.client.widgets.menu.CheckItem",
            new ConstructorCreationSupport());
    new FlowContainerFactory(this, false).get().get(0).command_CREATE(item, null);
    item.getPropertyByTitle("text").setValue("Default");
    item.setCheckedProperty(true);
  }

  private void ensureCheckItems_object() throws Exception {
    List<CheckItemInfo> items = getCheckItems();
    // only one CheckItem can be checked
    int checkedCount = 0;
    for (CheckItemInfo item : items) {
      if (item.getObject() != null && item.isChecked()) {
        if (checkedCount == 1) {
          item.setChecked(false);
        } else {
          checkedCount++;
        }
      }
    }
    // at least one CheckItem should be checked
    if (checkedCount == 0 && !items.isEmpty()) {
      CheckItemInfo item_Info = items.get(0);
      if (item_Info.getObject() != null) {
        item_Info.setChecked(true);
      }
    }
  }

  private void ensureCheckItems_model() throws Exception {
    List<CheckItemInfo> items = getCheckItems();
    // only one CheckItem can be checked
    int checkedCount = 0;
    for (CheckItemInfo item : items) {
      if (item.getObject() != null) {
        if (item.isCheckedProperty()) {
          if (checkedCount == 1) {
            item.setCheckedProperty(false);
          } else {
            checkedCount++;
          }
        }
      }
    }
    // at least one CheckItem should be checked
    if (checkedCount == 0 && !items.isEmpty()) {
      items.get(0).setCheckedProperty(true);
    }
  }
}
