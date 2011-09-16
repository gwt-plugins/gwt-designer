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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;

import java.util.List;

/**
 * Model for <code>com.google.gwt.user.client.ui.DeckPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
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
  public DeckPanelInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
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
}
