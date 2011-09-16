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
package com.google.gdt.eclipse.designer.smart.model.menu;

import com.google.gdt.eclipse.designer.smart.model.support.SmartClientUtils;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

/**
 * Model for any item whose body represented by <code>com.smartgwt.client.widgets.Canvas</code>. See
 * <code>com.smartgwt.client.widgets.toolbar.ToolStripSeparator</code>,
 * <code>com.smartgwt.client.widgets.toolbar.ToolStripSpacer</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class ToolStripCanvasInfo extends AbstractComponentInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToolStripCanvasInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToolStripInfo getToolStrip() {
    return (ToolStripInfo) getParentJava();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    // extract bounds
    ToolStripInfo toolStrip = getToolStrip();
    int index = toolStrip.getChildrenReal().indexOf(this);
    Object canvas = toolStrip.getCanvases()[index];
    // translate canvas
    Rectangle bounds =
        SmartClientUtils.getAbsoluteBounds(canvas).getTranslated(
            toolStrip.getAbsoluteBounds().getLocation().getNegated());
    // set bounds
    setModelBounds(bounds);
    //
    super.refresh_fetch();
  }
}
