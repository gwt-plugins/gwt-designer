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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;

/**
 * Some panels, such as <code>SimplePanel</code> require "technical" size <code>"100%"</code> for
 * child widget(s) to fill this panel client area. But when we move widget out, we should not keep
 * this "technical" size, because <code>"100%"</code> for widget on <code>RootPanel</code> looks
 * very bad. On same time size for widgets on <code>AbsolutePanel</code> is usually reasonable, so
 * should be kept.
 * <p>
 * Supports for following parameters:
 * <ul>
 * <li><b>onChildOut.removeSize</b> specifies when size should be removed. Following values are
 * possible:
 * <ul>
 * <li><b>always</b> - remove size always, no matter what is new target container.</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class UpdateSizeOnChildOutSupport extends ObjectEventListener {
  private final WidgetInfo m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UpdateSizeOnChildOutSupport(WidgetInfo panel) {
    m_panel = panel;
    m_panel.addBroadcastListener(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcast
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void childRemoveBefore(ObjectInfo parent, ObjectInfo child) throws Exception {
    String removePolicy = m_panel.getDescription().getParameter("onChildOut.removeSize");
    if (child instanceof WidgetInfo && parent == m_panel) {
      WidgetInfo widget = (WidgetInfo) child;
      if ("always".equals(removePolicy)) {
        widget.getSizeSupport().setSize(null);
      }
    }
  }
}