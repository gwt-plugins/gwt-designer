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

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

/**
 * Model for GWT <code>HorizontalSplitPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class HorizontalSplitPanelInfo extends SplitPanelInfo
    implements
      IHorizontalSplitPanelInfo<WidgetInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public HorizontalSplitPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetInfo getLeftWidget() {
    return getWidgetAssociatedByMethod("setLeftWidget", "setStartOfLineWidget");
  }

  public WidgetInfo getRightWidget() {
    return getWidgetAssociatedByMethod("setRightWidget", "setEndOfLineWidget");
  }

  @Override
  public String getEmptyRegion() {
    if (getLeftWidget() == null) {
      return "left";
    }
    if (getRightWidget() == null) {
      return "right";
    }
    return null;
  }
}
