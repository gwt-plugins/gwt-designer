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
package com.google.gdt.eclipse.designer.gwtext.gef.policy;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.ToolkitProvider;
import com.google.gdt.eclipse.designer.gef.WidgetsLayoutRequestValidator;
import com.google.gdt.eclipse.designer.gwtext.model.layout.AbsoluteLayoutInfo;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.ContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteBasedLayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteLayoutSelectionEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsolutePolicyUtils;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.AbstractAlignmentActionsSupport;
import org.eclipse.wb.internal.core.gef.policy.snapping.IAbsoluteLayoutCommands;
import org.eclipse.wb.internal.core.laf.BaselineSupportHelper;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link LayoutEditPolicy} for {@link AbsoluteLayoutInfo}.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage GWTExt.gef.policy
 */
public final class AbsoluteLayoutEditPolicy extends AbsoluteBasedLayoutEditPolicy<WidgetInfo> {
  private final AbsoluteLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteLayoutEditPolicy(AbsoluteLayoutInfo panel) {
    super(panel);
    m_layout = panel;
    createPlacementsSupport(IAbsoluteLayoutCommands.EMPTY);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return WidgetsLayoutRequestValidator.INSTANCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<EditPart, EditPolicy> m_oldSelectionPolicies = Maps.newHashMap();

  @Override
  protected void decorateChild(EditPart child) {
    Object childModel = child.getModel();
    if (childModel instanceof WidgetInfo) {
      EditPolicy policy = new AbsoluteLayoutSelectionEditPolicy<WidgetInfo>();
      m_oldSelectionPolicies.put(child, child.getEditPolicy(EditPolicy.SELECTION_ROLE));
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, policy);
    }
  }

  @Override
  protected void undecorateChild(EditPart child) {
    child.installEditPolicy(EditPolicy.SELECTION_ROLE, m_oldSelectionPolicies.get(child));
    super.undecorateChild(child);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IVisualDataProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<WidgetInfo> getAllComponents() {
    List<WidgetInfo> components = m_layout.getContainer().getChildrenWidgets();
    return Lists.<WidgetInfo>newArrayList(components);
  }

  public int getBaseline(IAbstractComponentInfo component) {
    return BaselineSupportHelper.getBaseline(component.getObject());
  }

  public Dimension getComponentPreferredSize(IAbstractComponentInfo component) {
    return component.getPreferredSize();
  }

  @Override
  public int getComponentGapValue(IAbstractComponentInfo component1,
      IAbstractComponentInfo component2,
      int direction) {
    return AbsolutePolicyUtils.DEFAULT_COMPONENT_GAP;
  }

  @Override
  public int getContainerGapValue(IAbstractComponentInfo component, int direction) {
    return AbsolutePolicyUtils.DEFAULT_CONTAINER_GAP;
  }

  public Dimension getContainerSize() {
    ContainerInfo container = m_layout.getContainer();
    Dimension size = container.getModelBounds().getSize();
    Insets insets = container.getClientAreaInsets();
    return new Dimension(size.width - insets.getWidth(), size.height - insets.getHeight());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(CreateRequest request) {
    final WidgetInfo component = (WidgetInfo) request.getNewObject();
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        placementsSupport.commitAdd();
        Rectangle widgetModelBounds = component.getModelBounds();
        m_layout.command_CREATE(component, null);
        Point location = widgetModelBounds.getLocation();
        Dimension size = getSize(widgetModelBounds);
        m_layout.command_BOUNDS(component, location, size);
      }

      private Dimension getSize(Rectangle widgetModelBounds) {
        boolean hasForcedSize =
            component.getDescription().getParameter("liveComponent.forcedSize.width") != null;
        return hasForcedSize || m_resizeOnCreate ? widgetModelBounds.getSize() : null;
      }
    };
  }

  @Override
  protected void doPasteComponent(Point pasteLocation, PastedComponentInfo pastedWidget)
      throws Exception {
    WidgetInfo component = (WidgetInfo) pastedWidget.getComponent();
    // create
    m_layout.command_CREATE(component, null);
    m_layout.command_BOUNDS(
        component,
        pasteLocation.getTranslated(pastedWidget.getBounds().getLocation()),
        pastedWidget.getBounds().getSize());
  }

  @Override
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        ArrayList<IAbstractComponentInfo> models = Lists.newArrayList();
        for (EditPart editPart : editParts) {
          models.add((IAbstractComponentInfo) editPart.getModel());
        }
        placementsSupport.commit();
        for (IAbstractComponentInfo widget : models) {
          Rectangle bounds = widget.getModelBounds();
          m_layout.command_BOUNDS((WidgetInfo) widget, bounds.getLocation(), null);
        }
      }
    };
  }

  @Override
  protected Command getAddCommand(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        ArrayList<IAbstractComponentInfo> models = Lists.newArrayList();
        for (EditPart editPart : editParts) {
          models.add((IAbstractComponentInfo) editPart.getModel());
        }
        placementsSupport.commitAdd();
        for (IAbstractComponentInfo widget : models) {
          Rectangle bounds = widget.getModelBounds();
          m_layout.command_MOVE((WidgetInfo) widget, null);
          m_layout.command_BOUNDS((WidgetInfo) widget, bounds.getLocation(), null);
        }
      }
    };
  }

  @Override
  protected Command getResizeCommand(final ChangeBoundsRequest request) {
    return new EditCommand(m_layout) {
      @Override
      protected void executeEdit() throws Exception {
        for (EditPart editPart : request.getEditParts()) {
          IAbstractComponentInfo widget = (IAbstractComponentInfo) editPart.getModel();
          Rectangle bounds = widget.getModelBounds();
          m_layout.command_BOUNDS((WidgetInfo) widget, bounds.getLocation(), bounds.getSize());
        }
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection actions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractAlignmentActionsSupport<WidgetInfo> getAlignmentActionsSupport() {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc 
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ToolkitDescription getToolkit() {
    return ToolkitProvider.DESCRIPTION;
  }
}
