/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gdt.eclipse.designer.smart.gef.policy;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.ToolkitProvider;
import com.google.gdt.eclipse.designer.gef.WidgetsLayoutRequestValidator;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.smart.model.CanvasAlignmentSupport;
import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.WidgetCanvasInfo;

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

/**
 * {@link LayoutEditPolicy} for {@link CanvasInfo}.
 * 
 * @author scheglov_ke
 * @coverage SmartGWT.gef.policy
 */
public final class CanvasLayoutEditPolicy extends AbsoluteBasedLayoutEditPolicy<WidgetInfo> {
  private final CanvasInfo m_canvas;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CanvasLayoutEditPolicy(CanvasInfo canvas) {
    super(canvas);
    m_canvas = canvas;
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
  @Override
  protected void decorateChild(EditPart child) {
    Object childModel = child.getModel();
    if (childModel instanceof WidgetInfo) {
      EditPolicy policy = new AbsoluteLayoutSelectionEditPolicy<WidgetInfo>();
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, policy);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IVisualDataProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<WidgetInfo> getAllComponents() {
    List<WidgetInfo> components = m_canvas.getWidgets();
    return new ArrayList<WidgetInfo>(components);
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
    Dimension size = m_canvas.getModelBounds().getSize().getCopy();
    Insets insets = m_canvas.getClientAreaInsets();
    return size.shrink(insets.getWidth(), insets.getHeight());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(CreateRequest request) {
    final WidgetInfo component = (WidgetInfo) request.getNewObject();
    return new EditCommand(m_canvas) {
      @Override
      protected void executeEdit() throws Exception {
        placementsSupport.commitAdd();
        Rectangle widgetModelBounds = component.getModelBounds();
        m_canvas.command_absolute_CREATE(component, null);
        Point location = widgetModelBounds.getLocation();
        Dimension size = getSize(widgetModelBounds);
        CanvasInfo boundsCanvas = getBoundsCanvas(component);
        if (boundsCanvas != null) {
          m_canvas.command_BOUNDS(boundsCanvas, location, size);
        }
      }

      private Dimension getSize(Rectangle widgetModelBounds) {
        boolean setSize = component.shouldSetReasonableSize();
        return setSize || m_resizeOnCreate ? widgetModelBounds.getSize() : null;
      }
    };
  }

  @Override
  protected void doPasteComponent(Point pasteLocation, PastedComponentInfo pastedWidget)
      throws Exception {
    WidgetInfo component = (WidgetInfo) pastedWidget.getComponent();
    // create
    m_canvas.command_absolute_CREATE(component, null);
    m_canvas.command_BOUNDS(
        getBoundsCanvas(component),
        pasteLocation.getTranslated(pastedWidget.getBounds().getLocation()),
        pastedWidget.getBounds().getSize());
  }

  @Override
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    return new EditCommand(m_canvas) {
      @Override
      protected void executeEdit() throws Exception {
        ArrayList<IAbstractComponentInfo> models = Lists.newArrayList();
        for (EditPart editPart : editParts) {
          models.add((IAbstractComponentInfo) editPart.getModel());
        }
        placementsSupport.commit();
        for (IAbstractComponentInfo widget : models) {
          Rectangle bounds = widget.getModelBounds();
          WidgetInfo component = (WidgetInfo) widget;
          CanvasInfo boundsCanvas = getBoundsCanvas(component);
          if (boundsCanvas != null) {
            m_canvas.command_BOUNDS(boundsCanvas, bounds.getLocation(), null);
          }
        }
      }
    };
  }

  @Override
  protected Command getAddCommand(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    return new EditCommand(m_canvas) {
      @Override
      protected void executeEdit() throws Exception {
        ArrayList<IAbstractComponentInfo> models = Lists.newArrayList();
        for (EditPart editPart : editParts) {
          models.add((IAbstractComponentInfo) editPart.getModel());
        }
        placementsSupport.commitAdd();
        for (IAbstractComponentInfo widget : models) {
          Rectangle bounds = widget.getModelBounds();
          WidgetInfo component = (WidgetInfo) widget;
          m_canvas.command_absolute_MOVE(component, null);
          CanvasInfo boundsCanvas = getBoundsCanvas(component);
          if (boundsCanvas != null) {
            m_canvas.command_BOUNDS(boundsCanvas, bounds.getLocation(), null);
          }
        }
      }
    };
  }

  @Override
  protected Command getResizeCommand(final ChangeBoundsRequest request) {
    return new EditCommand(m_canvas) {
      @Override
      protected void executeEdit() throws Exception {
        for (EditPart editPart : request.getEditParts()) {
          IAbstractComponentInfo widget = (IAbstractComponentInfo) editPart.getModel();
          Rectangle bounds = widget.getModelBounds();
          WidgetInfo component = (WidgetInfo) widget;
          CanvasInfo boundsCanvas = getBoundsCanvas(component);
          if (boundsCanvas != null) {
            m_canvas.command_BOUNDS(boundsCanvas, bounds.getLocation(), bounds.getSize());
          }
        }
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractAlignmentActionsSupport<WidgetInfo> getAlignmentActionsSupport() {
    return new CanvasAlignmentSupport(m_canvas);
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils 
  //
  ////////////////////////////////////////////////////////////////////////////
  public CanvasInfo getBoundsCanvas(WidgetInfo widget) {
    if (widget instanceof CanvasInfo) {
      return (CanvasInfo) widget;
    }
    if (widget.getParentJava() instanceof WidgetCanvasInfo) {
      return (CanvasInfo) widget.getParentJava();
    }
    return null;
  }
}
