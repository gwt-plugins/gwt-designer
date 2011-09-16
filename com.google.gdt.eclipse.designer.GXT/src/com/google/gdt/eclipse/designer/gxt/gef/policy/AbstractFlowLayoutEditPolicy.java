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
package com.google.gdt.eclipse.designer.gxt.gef.policy;

import com.google.gdt.eclipse.designer.gef.policy.WidgetFlowLayoutEditPolicy;
import com.google.gdt.eclipse.designer.gxt.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for flow {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.policy
 */
public abstract class AbstractFlowLayoutEditPolicy extends WidgetFlowLayoutEditPolicy<WidgetInfo> {
  private final LayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractFlowLayoutEditPolicy(LayoutInfo layout) {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation of commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(WidgetInfo newObject, WidgetInfo referenceObject) throws Exception {
    m_layout.command_CREATE(newObject, referenceObject);
  }

  @Override
  protected void command_MOVE(WidgetInfo object, WidgetInfo referenceObject) throws Exception {
    m_layout.command_MOVE(object, referenceObject);
  }
}
