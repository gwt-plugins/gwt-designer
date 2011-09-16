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
import com.google.gdt.eclipse.designer.mobile.device.model.DeviceInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;

import org.eclipse.swt.graphics.Image;

import org.xml.sax.Attributes;

/**
 * Implementation of {@link Command} that adds new {@link DeviceInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public final class DeviceAddCommand extends DeviceAbstractCommand {
  public static final String ID = "deviceAdd";
  private final String m_categoryId;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeviceAddCommand(CategoryInfo category,
      String id,
      String name,
      String imagePath,
      Rectangle displayBounds) {
    super(id, name, imagePath, displayBounds);
    m_categoryId = category.getId();
  }

  public DeviceAddCommand(Attributes attributes) {
    super(attributes);
    m_categoryId = attributes.getValue("categoryId");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        CategoryInfo category = DeviceManager.getCategory(m_categoryId);
        if (category != null) {
          Image image = SwtResourceManager.getImage(m_imagePath);
          category.addDevice(new DeviceInfo(m_id, m_name, m_imagePath, image, m_displayBounds));
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addAttributes() {
    super.addAttributes();
    addAttribute("categoryId", m_categoryId);
  }
}
