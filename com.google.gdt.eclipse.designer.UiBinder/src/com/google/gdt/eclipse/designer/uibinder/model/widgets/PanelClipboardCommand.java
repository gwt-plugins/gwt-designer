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

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;

/**
 * Abstract command for pasting {@link WidgetInfo} on container {@link WidgetInfo}.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public abstract class PanelClipboardCommand<T extends WidgetInfo> extends ClipboardCommand {
  private static final long serialVersionUID = 0L;
  private final XmlObjectMemento m_widgetMemento;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PanelClipboardCommand(WidgetInfo widget) throws Exception {
    m_widgetMemento = XmlObjectMemento.createMemento(widget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClipboardCommand
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  @SuppressWarnings("unchecked")
  public void execute(XmlObjectInfo object) throws Exception {
    if (object instanceof WidgetInfo) {
      T panel = (T) object;
      WidgetInfo widget = (WidgetInfo) m_widgetMemento.create(object);
      add(panel, widget);
      m_widgetMemento.apply();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds given {@link WidgetInfo} to container using its specific way.
   */
  protected abstract void add(T panel, WidgetInfo widget) throws Exception;
}
