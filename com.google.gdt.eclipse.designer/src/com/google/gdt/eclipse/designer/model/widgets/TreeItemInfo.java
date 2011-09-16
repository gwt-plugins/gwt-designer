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
package com.google.gdt.eclipse.designer.model.widgets;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.util.List;

/**
 * Model for <code>TreeItem</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class TreeItemInfo extends UIObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeItemInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link WidgetInfo} that was used to created this {@link TreeItemInfo}, may be
   *         <code>null</code>.
   */
  public WidgetInfo getWidget() {
    List<WidgetInfo> widgets = getChildren(WidgetInfo.class);
    return !widgets.isEmpty() ? widgets.get(0) : null;
  }

  /**
   * @return the {@link TreeItemInfo} children.
   */
  public List<TreeItemInfo> getItems() {
    return getChildren(TreeItemInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link TreeItemInfo} based on {@link WidgetInfo} to this {@link TreeInfo}.
   */
  public void command_CREATE(WidgetInfo widget, TreeItemInfo nextItem) throws Exception {
    TreeInfo.command_CREATE_Widget(this, widget);
  }

  /**
   * Executed after any create/move operation on this {@link TreeItemInfo}.
   */
  public void command_TARGET_after(Object o, Object next) throws Exception {
    getPropertyByTitle("state").setValue(true);
  }
}
