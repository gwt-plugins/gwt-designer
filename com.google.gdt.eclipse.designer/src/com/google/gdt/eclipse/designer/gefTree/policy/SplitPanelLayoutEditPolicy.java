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
package com.google.gdt.eclipse.designer.gefTree.policy;

import com.google.gdt.eclipse.designer.gef.WidgetsLayoutRequestValidator;
import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ISplitPanelInfo;

import org.eclipse.wb.core.gefTree.policy.ObjectLayoutEditPolicy;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;

import java.util.List;

/**
 * {@link LayoutEditPolicy} for {@link ISplitPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gefTree
 */
public final class SplitPanelLayoutEditPolicy<T extends IWidgetInfo>
    extends
      ObjectLayoutEditPolicy<T> {
  private final ISplitPanelInfo<T> m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private SplitPanelLayoutEditPolicy(ISplitPanelInfo<T> panel) {
    super(panel.getUnderlyingModel());
    m_panel = panel;
  }

  public static <T extends IWidgetInfo> SplitPanelLayoutEditPolicy<T> create(ISplitPanelInfo<T> panel) {
    return new SplitPanelLayoutEditPolicy<T>(panel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart.getModel() instanceof IWidgetInfo;
  }

  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return WidgetsLayoutRequestValidator.INSTANCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(Object newObject, Object referenceObject) {
    if (m_panel.getEmptyRegion() == null) {
      return null;
    }
    return super.getCreateCommand(newObject, referenceObject);
  }

  @Override
  protected Command getAddCommand(List<EditPart> addParts, Object referenceObject) {
    if (m_panel.getEmptyRegion() == null) {
      return null;
    }
    return super.getAddCommand(addParts, referenceObject);
  }

  @Override
  protected void command_CREATE(T component, T reference) throws Exception {
    String region = m_panel.getEmptyRegion();
    if (region != null) {
      m_panel.command_CREATE(component, region);
    }
  }

  @Override
  protected void command_MOVE(T component, T reference) throws Exception {
    m_panel.command_MOVE(component, reference);
  }

  @Override
  protected void command_ADD(T component, T reference) throws Exception {
    String region = m_panel.getEmptyRegion();
    if (region != null) {
      m_panel.command_MOVE(component, region);
    }
  }
}