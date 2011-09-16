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

import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;

import org.eclipse.wb.core.model.ObjectInfo;

/**
 * GWT-Ext provider for layout assistant pages.
 * 
 * @author sablin_aa
 * @coverage GWTExt.model.layout.assistant
 */
public abstract class LayoutAssistantSupport<T extends LayoutInfo>
    extends
      org.eclipse.wb.core.editor.actions.assistant.LayoutAssistantSupport {
  protected final T m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutAssistantSupport(T layout) {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutAssistantSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final String getConstraintsPageTitle() {
    return "Layout Data";
  }

  @Override
  protected final ObjectInfo getContainer() {
    return m_layout.getContainer();
  }
}