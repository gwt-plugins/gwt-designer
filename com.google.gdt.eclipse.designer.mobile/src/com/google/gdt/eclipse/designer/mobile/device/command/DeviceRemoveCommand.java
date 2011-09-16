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
package com.google.gdt.eclipse.designer.mobile.device.command;

import com.google.gdt.eclipse.designer.mobile.device.DeviceManager;
import com.google.gdt.eclipse.designer.mobile.device.model.DeviceInfo;

import org.xml.sax.Attributes;

/**
 * Implementation of {@link Command} that removes {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public final class DeviceRemoveCommand extends Command {
  public static final String ID = "deviceRemove";
  private final String m_id;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceRemoveCommand(DeviceInfo device) {
    m_id = device.getId();
  }

  public DeviceRemoveCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    DeviceInfo device = DeviceManager.getDevice(m_id);
    if (device != null) {
      device.getCategory().removeDevice(device);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    addAttribute("id", m_id);
  }
}
