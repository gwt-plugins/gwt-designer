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

import com.google.gdt.eclipse.designer.mobile.device.model.DeviceInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.xml.sax.Attributes;

/**
 * Abstract {@link Command} that works with {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public abstract class DeviceAbstractCommand extends Command {
  protected final String m_id;
  protected final String m_name;
  protected final String m_imagePath;
  protected final Rectangle m_displayBounds;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceAbstractCommand(String id, String name, String imagePath, Rectangle displayBounds) {
    m_id = id;
    m_name = name;
    m_imagePath = imagePath;
    m_displayBounds = displayBounds;
  }

  public DeviceAbstractCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
    m_name = attributes.getValue("name");
    m_imagePath = attributes.getValue("image");
    m_displayBounds =
        new Rectangle(Integer.parseInt(attributes.getValue("displayX")),
            Integer.parseInt(attributes.getValue("displayY")),
            Integer.parseInt(attributes.getValue("displayWidth")),
            Integer.parseInt(attributes.getValue("displayHeight")));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    addAttribute("id", m_id);
    addAttribute("name", m_name);
    addAttribute("image", m_imagePath);
    addAttribute("displayX", "" + m_displayBounds.x);
    addAttribute("displayY", "" + m_displayBounds.y);
    addAttribute("displayWidth", "" + m_displayBounds.width);
    addAttribute("displayHeight", "" + m_displayBounds.height);
  }
}
