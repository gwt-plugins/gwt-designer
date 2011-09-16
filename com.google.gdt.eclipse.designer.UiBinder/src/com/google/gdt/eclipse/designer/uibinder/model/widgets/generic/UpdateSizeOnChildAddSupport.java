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
package com.google.gdt.eclipse.designer.uibinder.model.widgets.generic;

import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAdd;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectMove;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

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
 * @coverage GWT.UiBinder.model
 */
public final class UpdateSizeOnChildAddSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UpdateSizeOnChildAddSupport(XmlObjectInfo root) {
    root.addBroadcastListener(new XmlObjectAdd() {
      @Override
      public void after(ObjectInfo parent, XmlObjectInfo child) throws Exception {
        setSize(child);
      }
    });
    root.addBroadcastListener(new XmlObjectMove() {
      @Override
      public void after(XmlObjectInfo child, ObjectInfo oldParent, ObjectInfo newParent)
          throws Exception {
        if (newParent != oldParent) {
          setSize(child);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates size of {@link WidgetInfo}.
   */
  private void setSize(ObjectInfo child) throws Exception {
    if (child instanceof WidgetInfo && child.getParent() instanceof WidgetInfo) {
      WidgetInfo widget = (WidgetInfo) child;
      WidgetInfo parent = (WidgetInfo) child.getParent();
      String width = getSizeString(parent, "onChildAdd.setWidth");
      String height = getSizeString(parent, "onChildAdd.setHeight");
      widget.getSizeSupport().setSize(width, height);
    }
  }

  /**
   * @return the size part, may be <code>null</code>.
   */
  private String getSizeString(WidgetInfo parent, String key) {
    String size = XmlObjectUtils.getParameter(parent, key);
    if ("null".equals(size)) {
      size = null;
    }
    return size;
  }
}