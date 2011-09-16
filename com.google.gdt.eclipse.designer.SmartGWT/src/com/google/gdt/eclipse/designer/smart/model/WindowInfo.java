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
package com.google.gdt.eclipse.designer.smart.model;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationAssociation;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Model for <code>com.smartgwt.client.widgets.Window</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class WindowInfo extends LayoutInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WindowInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setObject(Object object) throws Exception {
    super.setObject(object);
    // no center positioning at design time
    if (!isPlaceholder()) {
      ReflectionUtils.invokeMethod(object, "setAutoCenter(java.lang.Boolean)", false);
    }
  }

  @Override
  protected void refresh_dispose_detach(WidgetInfo child) throws Exception {
    // detach items 
    if (child instanceof CanvasInfo) {
      CanvasInfo childCanvas = (CanvasInfo) child;
      Association association = childCanvas.getAssociation();
      if (association instanceof InvocationAssociation) {
        InvocationAssociation invocationAssociation = (InvocationAssociation) association;
        if (AstNodeUtils.getMethodSignature(invocationAssociation.getInvocation()).startsWith(
            "addItem(com.smartgwt.client.widgets.Canvas")) {
          if (childCanvas.isCreated()) {
            ReflectionUtils.invokeMethod(
                getObject(),
                "removeItem(com.smartgwt.client.widgets.Canvas)",
                childCanvas.getObject());
            return;
          }
        }
      }
    } else {
      // TODO
    }
    super.refresh_dispose_detach(child);
  }
}
