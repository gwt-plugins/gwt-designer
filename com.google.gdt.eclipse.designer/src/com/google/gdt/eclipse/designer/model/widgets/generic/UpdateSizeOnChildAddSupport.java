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
package com.google.gdt.eclipse.designer.model.widgets.generic;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;

/**
 * Some panels, such as <code>SimplePanel</code> require "technical" size <code>"100%"</code> for
 * child widget(s) to fill this panel client area.
 * <p>
 * Supports for following parameters:
 * <ul>
 * <li><b>onChildAdd.setWidth</b> specifies width to set automatically when {@link WidgetInfo} is
 * added to this container.</li>
 * <li><b>onChildAdd.setHeight</b> specifies height to set automatically when {@link WidgetInfo} is
 * added to this container.</li>
 * </ul>
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class UpdateSizeOnChildAddSupport extends JavaEventListener {
  private final WidgetInfo m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UpdateSizeOnChildAddSupport(WidgetInfo panel) {
    m_panel = panel;
    m_panel.addBroadcastListener(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcast
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
    setSize(child);
  }

  @Override
  public void moveAfter(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent) throws Exception {
    if (newParent != oldParent) {
      setSize(child);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private void setSize(JavaInfo child) throws Exception {
    if (child instanceof WidgetInfo && child.getParent() == m_panel) {
      WidgetInfo widget = (WidgetInfo) child;
      String width = getSizeString(widget, "onChildAdd.setWidth");
      String height = getSizeString(widget, "onChildAdd.setHeight");
      widget.getSizeSupport().setSize(width, height);
    }
  }

  private String getSizeString(WidgetInfo widget, String key) {
    String size = m_panel.getDescription().getParameter(key);
    if ("null".equals(size)) {
      size = null;
    }
    return size;
  }
}