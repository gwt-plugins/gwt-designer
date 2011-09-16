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

import java.util.List;

/**
 * Implementation of {@link Command} that moves {@link CategoryInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public final class CategoryMoveCommand extends Command {
  public static final String ID = "categoryMove";
  private final String m_id;
  private final String m_nextCategoryId;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CategoryMoveCommand(CategoryInfo category, CategoryInfo nextCategory) {
    m_id = category.getId();
    m_nextCategoryId = nextCategory != null ? nextCategory.getId() : null;
  }

  public CategoryMoveCommand(Attributes attributes) {
    m_id = attributes.getValue("id");
    m_nextCategoryId = attributes.getValue("nextCategory");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void execute() {
    CategoryInfo category = DeviceManager.getCategory(m_id);
    if (category == null) {
      return;
    }
    // don't move before itself, this is no-op
    if (m_id.equals(m_nextCategoryId)) {
      return;
    }
    // remove source
    List<CategoryInfo> categories = DeviceManager.getCategories();
    categories.remove(category);
    // add to new location
    CategoryInfo nextCategory = DeviceManager.getCategory(m_nextCategoryId);
    if (nextCategory != null) {
      int index = categories.indexOf(nextCategory);
      categories.add(index, category);
    } else {
      categories.add(category);
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
    addAttribute("nextCategory", m_nextCategoryId);
  }
}
