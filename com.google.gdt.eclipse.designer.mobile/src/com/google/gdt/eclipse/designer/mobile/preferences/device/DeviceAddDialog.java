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

import com.google.gdt.eclipse.designer.mobile.device.command.Command;
import com.google.gdt.eclipse.designer.mobile.device.command.DeviceAddCommand;
import com.google.gdt.eclipse.designer.mobile.device.model.CategoryInfo;
import com.google.gdt.eclipse.designer.mobile.device.model.DeviceInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.widgets.Composite;

/**
 * Dialog for adding new {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public final class DeviceAddDialog extends DeviceAbstractDialog {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceAddDialog() {
    super("New mobile device", "Add new mobile device.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ///////////////////////////////////////////////////////////////////////////
  /**
   * Creates controls on this dialog.
   */
  @Override
  protected void createControls(Composite container) {
    super.createControls(container);
    m_displayField_x.setText("0");
    m_displayField_y.setText("0");
    m_displayField_width.setText("320");
    m_displayField_height.setText("240");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  public Command getCommand(CategoryInfo targetCategory) {
    Rectangle displayBounds =
        new Rectangle(Integer.parseInt(m_displayField_x.getText()),
            Integer.parseInt(m_displayField_y.getText()),
            Integer.parseInt(m_displayField_width.getText()),
            Integer.parseInt(m_displayField_height.getText()));
    return new DeviceAddCommand(targetCategory,
        "device_" + System.currentTimeMillis(),
        m_nameField.getText(),
        m_imageField.getText(),
        displayBounds);
  }
}
