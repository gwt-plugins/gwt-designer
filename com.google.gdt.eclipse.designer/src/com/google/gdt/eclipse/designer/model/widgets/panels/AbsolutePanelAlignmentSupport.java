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
package com.google.gdt.eclipse.designer.model.widgets.panels;

import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.SimpleAlignmentActionsSupport;

/**
 * Helper for adding alignment actions for {@link IAbsolutePanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class AbsolutePanelAlignmentSupport<T extends IWidgetInfo>
    extends
      SimpleAlignmentActionsSupport<T> {
  private final IAbsolutePanelInfo<T> m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private AbsolutePanelAlignmentSupport(IAbsolutePanelInfo<T> panel) {
    m_panel = panel;
  }

  public static <T extends IWidgetInfo> AbsolutePanelAlignmentSupport<T> create(IAbsolutePanelInfo<T> panel) {
    return new AbsolutePanelAlignmentSupport<T>(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isComponentInfo(ObjectInfo object) {
    return object instanceof IWidgetInfo;
  }

  @Override
  protected boolean isValidObjectOnRootPath(IAbstractComponentInfo parent) {
    return parent instanceof IAbsolutePanelInfo;
  }

  @Override
  protected IAbstractComponentInfo getLayoutContainer() {
    return m_panel;
  }

  @Override
  protected void commandChangeBounds(T component, Point location, Dimension size) throws Exception {
    if (component.getParent() instanceof IAbsolutePanelInfo) {
      @SuppressWarnings("unchecked")
      IAbsolutePanelInfo<T> panel = (IAbsolutePanelInfo<T>) component.getParent();
      panel.command_BOUNDS(component, location, size);
    }
  }
}