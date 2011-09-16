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
import com.google.gdt.eclipse.designer.mobile.device.model.CategoryInfo;

import org.xml.sax.Attributes;

/**
 * {@link Command} for adding new {@link CategoryInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public final class CategoryAddCommand extends Command {
  public static final String ID = "categoryAdd";
  private final String m_id;
  private final String m_name;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CategoryAddCommand(String id, String name) {
    m_id = id;
    m_name = name;
  }

  public CategoryAddCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
    m_name = attributes.getValue("name");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execute
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    CategoryInfo category = new CategoryInfo(m_id, m_name);
    DeviceManager.getCategories().add(category);
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
    addAttribute("name", m_name);
  }
}
