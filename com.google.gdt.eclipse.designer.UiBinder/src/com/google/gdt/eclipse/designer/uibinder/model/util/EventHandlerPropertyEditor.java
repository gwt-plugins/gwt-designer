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
package com.google.gdt.eclipse.designer.uibinder.model.util;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.swt.graphics.Point;

/**
 * {@link PropertyEditor} for {@link EventHandlerProperty}.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
final class EventHandlerPropertyEditor extends TextDisplayPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new EventHandlerPropertyEditor();

  private EventHandlerPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    EventHandlerProperty handlerProperty = (EventHandlerProperty) property;
    MethodDeclaration methodDeclaration = handlerProperty.getMethodDeclaration(false);
    if (methodDeclaration != null) {
      return methodDeclaration.getName().getIdentifier();
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void doubleClick(Property property, Point location) throws Exception {
    final EventHandlerProperty eventProperty = (EventHandlerProperty) property;
    ExecutionUtils.run(eventProperty.getObject(), new RunnableEx() {
      public void run() throws Exception {
        eventProperty.openListener();
      }
    });
  }
}
