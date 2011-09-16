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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;

import java.util.List;

/**
 * {@link StackContainerSupport} for Ext-GWT {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public final class LayoutStackContainerSupport extends StackContainerSupport<WidgetInfo> {
  private final LayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Container
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutStackContainerSupport(LayoutInfo layout) throws Exception {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isActive() {
    return m_layout.isActive();
  }

  @Override
  protected ObjectInfo getContainer() {
    return m_layout.getContainer();
  }

  @Override
  protected List<WidgetInfo> getChildren() {
    return m_layout.getWidgets();
  }
}