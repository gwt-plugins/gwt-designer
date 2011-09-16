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
package com.google.gdt.eclipse.designer.gwtext.model.layout.assistant;

import com.google.gdt.eclipse.designer.gwtext.model.layout.table.TableLayoutInfo;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.swt.widgets.Composite;

import java.util.List;

/**
 * Assistant pages provider for {@link TableLayoutInfo}.
 * 
 * @author sablin_aa
 * @coverage GWTExt.model.layout.assistant
 */
public final class TableLayoutAssistant extends LayoutAssistantSupport<TableLayoutInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableLayoutAssistant(TableLayoutInfo layout) {
    super(layout);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractAssistantPage createLayoutPage(Composite parent) {
    return new TableLayoutAssistantPage(parent, m_layout);
  }

  @Override
  protected AbstractAssistantPage createConstraintsPage(Composite parent, List<ObjectInfo> objects) {
    return new TableLayoutDataAssistantPage(parent, objects);
  }
}