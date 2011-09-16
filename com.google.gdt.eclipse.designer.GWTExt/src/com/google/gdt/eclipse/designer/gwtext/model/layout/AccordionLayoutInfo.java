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
package com.google.gdt.eclipse.designer.gwtext.model.layout;

import com.google.gdt.eclipse.designer.gwtext.model.layout.assistant.AccordionLayoutAssistant;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Model for <code>com.gwtext.client.widgets.layout.AccordionLayout</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model.layout
 */
public final class AccordionLayoutInfo extends FitLayoutInfo {
  private final LayoutStackContainerSupport m_stackContainer =
      new LayoutStackContainerSupport(this);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AccordionLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    ensureTitleForChildPanels();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initializeLayoutAssistant() {
    new AccordionLayoutAssistant(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * <code>AccordionLayout</code> handles only <code>Panel</code> and only with set "title"
   * property. We generate code with "title" for <code>Panel</code> itself, but for example not for
   * <code>TreePanel</code>. So, here we ensure title required by <code>AccordionLayout</code>.
   */
  private void ensureTitleForChildPanels() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (isActiveOnContainer(parent)) {
          Property titleProperty = child.getPropertyByTitle("title");
          if (titleProperty != null && !titleProperty.isModified()) {
            titleProperty.setValue("New Panel");
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    {
      ObjectInfo expandedWidget = m_stackContainer.getActive();
      if (expandedWidget != null) {
        for (WidgetInfo widget : getContainer().getChildrenWidgets()) {
          ReflectionUtils.invokeMethod(
              widget.getObject(),
              "setCollapsed(boolean)",
              widget != expandedWidget);
        }
      }
    }
  }
}