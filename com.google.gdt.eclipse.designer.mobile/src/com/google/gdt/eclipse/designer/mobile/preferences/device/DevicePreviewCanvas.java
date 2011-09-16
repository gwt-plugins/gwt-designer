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
package com.google.gdt.eclipse.designer.mobile.preferences.device;

import com.google.gdt.eclipse.designer.mobile.device.model.DeviceInfo;

import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * {@link Canvas} for displaying {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public final class DevicePreviewCanvas extends Canvas {
  private DeviceInfo m_device;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DevicePreviewCanvas(Composite parent, int style) {
    super(parent, style);
    addListener(SWT.Paint, new Listener() {
      public void handleEvent(Event event) {
        if (m_device != null) {
          DrawUtils.drawScaledImage(event.gc, m_device.getImage(), getClientArea());
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link DeviceInfo} for display.
   */
  public void setDevice(DeviceInfo device) {
    m_device = device;
    redraw();
  }
}
