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
import com.google.gdt.eclipse.designer.mobile.device.model.AbstractDeviceInfo;
import com.google.gdt.eclipse.designer.mobile.device.model.CategoryInfo;
import com.google.gdt.eclipse.designer.mobile.device.model.DeviceInfo;

import org.xml.sax.Attributes;

import java.util.Iterator;
import java.util.List;

/**
 * {@link Command} changing {@link AbstractDeviceInfo} "visible" property.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public final class ElementVisibilityCommand extends Command {
  public static final String ID = "visible";
  private final String m_id;
  private final boolean m_visible;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ElementVisibilityCommand(AbstractDeviceInfo element, boolean visible) {
    m_id = element.getId();
    m_visible = visible;
  }

  public ElementVisibilityCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
    m_visible = "true".equals(attributes.getValue("visible"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execute
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    // try category
    {
      CategoryInfo category = DeviceManager.getCategory(m_id);
      if (category != null) {
        category.setVisible(m_visible);
      }
    }
    // try device
    {
      DeviceInfo device = DeviceManager.getDevice(m_id);
      if (device != null) {
        device.setVisible(m_visible);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Save
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    super.addAttributes();
    addAttribute("id", m_id);
    addAttribute("visible", m_visible);
  }

  @Override
  public void addToCommandList(List<Command> commands) {
    for (Iterator<Command> I = commands.iterator(); I.hasNext();) {
      Command command = I.next();
      if (command instanceof ElementVisibilityCommand) {
        ElementVisibilityCommand elementVisibilityCommand = (ElementVisibilityCommand) command;
        if (elementVisibilityCommand.m_id.equals(m_id)) {
          I.remove();
        }
      }
    }
    commands.add(this);
  }
}
