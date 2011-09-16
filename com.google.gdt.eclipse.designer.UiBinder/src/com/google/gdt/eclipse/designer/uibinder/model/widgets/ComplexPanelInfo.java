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

import com.google.gdt.eclipse.designer.model.widgets.panels.IComplexPanelInfo;

import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import java.util.List;

/**
 * Model for <code>com.google.gwt.user.client.ui.ComplexPanel</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public class ComplexPanelInfo extends PanelInfo implements IComplexPanelInfo<WidgetInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComplexPanelInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE2(WidgetInfo widget, WidgetInfo nextWidget) throws Exception {
    XmlObjectUtils.flowContainerCreate(this, widget, nextWidget);
  }

  public void command_MOVE2(WidgetInfo widget, WidgetInfo nextWidget) throws Exception {
    XmlObjectUtils.flowContainerMove(this, widget, nextWidget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds commands for coping child {@link WidgetInfo}.
   */
  @Override
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
    commands.add(new PanelClipboardCommand<ComplexPanelInfo>(widget) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(ComplexPanelInfo panel, WidgetInfo widget) throws Exception {
        panel.command_CREATE2(widget, null);
      }
    });
  }
}
