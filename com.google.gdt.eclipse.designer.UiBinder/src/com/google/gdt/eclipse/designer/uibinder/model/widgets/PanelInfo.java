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

import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectClipboardCopy;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;

import java.util.List;

/**
 * Model for <code>com.google.gwt.user.client.ui.Panel</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public class PanelInfo extends WidgetInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PanelInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    addClipboardSupport();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return all {@link WidgetInfo} children.
   */
  public final List<WidgetInfo> getChildrenWidgets() {
    return getChildren(WidgetInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addClipboardSupport() {
    addBroadcastListener(new XmlObjectClipboardCopy() {
      public void invoke(XmlObjectInfo object, List<ClipboardCommand> commands) throws Exception {
        if (object == PanelInfo.this) {
          clipboardCopy_addPanelCommands(commands);
        }
      }
    });
  }

  /**
   * Adds commands for copying this {@link PanelInfo}.
   */
  protected void clipboardCopy_addPanelCommands(List<ClipboardCommand> commands) throws Exception {
    for (WidgetInfo widget : getChildrenWidgets()) {
      clipboardCopy_addWidgetCommands(widget, commands);
    }
  }

  /**
   * Adds commands for coping child {@link WidgetInfo}.
   */
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
  }
}
