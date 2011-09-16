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
package com.google.gdt.eclipse.designer.model.widgets.panels;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddAfter;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * Model for <code>com.google.gwt.user.client.ui.DeckPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class DeckPanelInfo extends ComplexPanelInfo {
  private final StackContainerSupport<WidgetInfo> m_stackContainer =
      new StackContainerSupport<WidgetInfo>(this) {
        @Override
        protected List<WidgetInfo> getChildren() {
          return getChildrenWidgets();
        }
      };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeckPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    manageShowWidget();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ensures that given {@link WidgetInfo} is shown on this {@link DeckPanelInfo}.
   */
  public void showWidget(WidgetInfo widget) throws Exception {
    m_stackContainer.setActive(widget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    showActiveWidget();
  }

  private void showActiveWidget() throws Exception {
    WidgetInfo active = m_stackContainer.getActive();
    if (active != null) {
      int index =
          (Integer) ReflectionUtils.invokeMethod(
              getObject(),
              "getWidgetIndex(com.google.gwt.user.client.ui.Widget)",
              active.getObject());
      ReflectionUtils.invokeMethod(getObject(), "showWidget(int)", index);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_TARGET_after(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
    showWidget(component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates <code>DeckPanel.showWidget()</code> invocation on widget delete/move.
   */
  private void manageShowWidget() {
    addBroadcastListener(new ObjectInfoChildAddAfter() {
      public void invoke(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (!GlobalState.isParsing() && isDeckChild(child)) {
          if (getChildrenWidgets().size() == 1) {
            addMethodInvocation("showWidget(int)", "0");
          }
        }
      }
    });
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void childRemoveAfter(ObjectInfo parent, ObjectInfo child) throws Exception {
        if (isDeckChild(child)) {
          if (getChildrenWidgets().isEmpty()) {
            // if last widget removed, remove showWidget()
            removeMethodInvocations("showWidget(int)");
          } else {
            // if some widget was removed, we don't care if it show shown, we always show "0"
            for (MethodInvocation invocation : getMethodInvocations("showWidget(int)")) {
              getEditor().replaceInvocationArgument(invocation, 0, "0");
            }
          }
        }
      }
    });
  }

  /**
   * @return <code>true</code> if given object is child of this {@link DeckPanelInfo}.
   */
  private boolean isDeckChild(ObjectInfo child) {
    return child instanceof WidgetInfo && child.getParent() == this;
  }
}
