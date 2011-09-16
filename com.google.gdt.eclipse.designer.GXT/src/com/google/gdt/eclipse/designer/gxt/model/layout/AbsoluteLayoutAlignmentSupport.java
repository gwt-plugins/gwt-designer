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

import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.SimpleAlignmentActionsSupport;

/**
 * Helper for adding alignment actions for {@link AbsoluteLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public final class AbsoluteLayoutAlignmentSupport extends SimpleAlignmentActionsSupport<WidgetInfo> {
  private final AbsoluteLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteLayoutAlignmentSupport(AbsoluteLayoutInfo layout) {
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isComponentInfo(ObjectInfo object) {
    return object instanceof WidgetInfo;
  }

  @Override
  protected boolean isValidObjectOnRootPath(IAbstractComponentInfo parent) {
    // We don't have good implementation, because AbsoluteLayout of first Widget
    // is used to align ALL widgets. But this is incorrect. So, disabled for now.
    return false;
  }

  @Override
  protected LayoutContainerInfo getLayoutContainer() {
    return m_layout.getContainer();
  }

  @Override
  protected void commandChangeBounds(WidgetInfo component, Point location, Dimension size)
      throws Exception {
    m_layout.command_BOUNDS(component, location, size);
  }
}