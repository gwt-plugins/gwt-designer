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
package com.google.gdt.eclipse.designer.gef.policy;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.ToolkitProvider;
import com.google.gdt.eclipse.designer.gef.WidgetsLayoutRequestValidator;
import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ILayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.ILayoutPanelInfo.ResizeDirection;
import com.google.gdt.eclipse.designer.model.widgets.panels.LayoutPanelInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
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
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsolutePolicyUtils;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.AbstractAlignmentActionsSupport;
import org.eclipse.wb.internal.core.gef.policy.snapping.IAbsoluteLayoutCommands;
import org.eclipse.wb.internal.core.laf.BaselineSupportHelper;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link LayoutEditPolicy} for {@link LayoutPanelInfo}.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class LayoutPanelLayoutEditPolicy<W extends IWidgetInfo>
    extends
      AbsoluteBasedLayoutEditPolicy<W> {
  private final ILayoutPanelInfo<W> m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private LayoutPanelLayoutEditPolicy(ILayoutPanelInfo<W> panel) {
    super(panel.getUnderlyingModel());
    m_panel = panel;
    createPlacementsSupport(IAbsoluteLayoutCommands.EMPTY);
  }

  public static <W extends IWidgetInfo> LayoutPanelLayoutEditPolicy<W> create(ILayoutPanelInfo<W> panel) {
    return new LayoutPanelLayoutEditPolicy<W>(panel);
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
    if (childModel instanceof IWidgetInfo) {
      EditPolicy policy = new LayoutPanelSelectionEditPolicy<W>(m_panel);
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, policy);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IVisualDataProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<W> getAllComponents() {
    List<W> components = m_panel.getChildrenWidgets();
    return new ArrayList<W>(components);
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
    //return AbsolutePolicyUtils.DEFAULT_CONTAINER_GAP;
    return 0;
  }

  public Dimension getContainerSize() {
    Dimension size = m_panel.getModelBounds().getSize().getCopy();
    Insets insets = m_panel.getClientAreaInsets();
    return size.shrink(insets.getWidth(), insets.getHeight());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hint
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getLocationHintString(EditPart editPart, int x, int y) {
    W widget = toWidget(editPart);
    return m_panel.getLocationHint(widget, x, y);
  }

  @Override
  protected Point getLocationHintLocation(EditPart editPart,
      Rectangle widgetBounds,
      Dimension hintSize) {
    Point location = new Point();
    W widget = toWidget(editPart);
    if (m_panel.getLocationHint_isTrailing(widget, true)) {
      location.x = widgetBounds.right() - hintSize.width;
    } else {
      location.x = widgetBounds.left();
    }
    if (m_panel.getLocationHint_isTrailing(widget, false)) {
      location.y = widgetBounds.bottom();
    } else {
      location.y = widgetBounds.top() - hintSize.height;
    }
    return location;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(CreateRequest request) {
    final W component = toWidget(request.getNewObject());
    return new EditCommand(m_panel) {
      @Override
      protected void executeEdit() throws Exception {
        placementsSupport.commitAdd();
        Rectangle widgetModelBounds = component.getModelBounds();
        m_panel.command_CREATE2(component, null);
        {
          Point location = widgetModelBounds.getLocation();
          m_panel.command_LOCATION(component, location);
        }
        if (m_resizeOnCreate) {
          Dimension size = widgetModelBounds.getSize();
          m_panel.command_SIZE(component, size, ResizeDirection.TRAILING, ResizeDirection.TRAILING);
        }
      }
    };
  }

  @Override
  protected void doPasteComponent(Point pasteLocation, PastedComponentInfo pastedWidget)
      throws Exception {
    W widget = toWidget(pastedWidget.getComponent());
    m_panel.command_CREATE2(widget, null);
    {
      Point widgetLocation_inPasteBounds = pastedWidget.getBounds().getLocation();
      Point widgetLocation = pasteLocation.getTranslated(widgetLocation_inPasteBounds);
      m_panel.command_LOCATION(widget, widgetLocation);
    }
    {
      Dimension size = pastedWidget.getBounds().getSize();
      m_panel.command_SIZE(widget, size, ResizeDirection.TRAILING, ResizeDirection.TRAILING);
    }
  }

  @Override
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    return new EditCommand(m_panel) {
      @Override
      protected void executeEdit() throws Exception {
        placementsSupport.commit();
        for (W widget : toWidgets(editParts)) {
          Point location = widget.getModelBounds().getLocation();
          m_panel.command_LOCATION(widget, location);
        }
      }
    };
  }

  @Override
  protected Command getAddCommand(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    return new EditCommand(m_panel) {
      @Override
      protected void executeEdit() throws Exception {
        placementsSupport.commitAdd();
        for (W widget : toWidgets(editParts)) {
          Rectangle bounds = widget.getModelBounds();
          m_panel.command_MOVE2(widget, null);
          m_panel.command_LOCATION(widget, bounds.getLocation());
        }
      }
    };
  }

  @Override
  protected Command getResizeCommand(final ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    return new EditCommand(m_panel) {
      @Override
      protected void executeEdit() throws Exception {
        List<W> widgets = toWidgets(editParts);
        for (W widget : widgets) {
          Rectangle bounds = widget.getModelBounds();
          ResizeDirection hDirection =
              getDirection(
                  request.getResizeDirection(),
                  IPositionConstants.WEST,
                  IPositionConstants.EAST);
          ResizeDirection vDirection =
              getDirection(
                  request.getResizeDirection(),
                  IPositionConstants.NORTH,
                  IPositionConstants.SOUTH);
          m_panel.command_SIZE(widget, bounds.getSize(), hDirection, vDirection);
        }
      }

      private ResizeDirection getDirection(int direction, int leading, int trailing) {
        if ((direction & leading) != 0) {
          return ResizeDirection.LEADING;
        }
        if ((direction & trailing) != 0) {
          return ResizeDirection.TRAILING;
        }
        return ResizeDirection.NONE;
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Casting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the casted model.
   */
  @SuppressWarnings("unchecked")
  private W toWidget(Object o) {
    return (W) o;
  }

  /**
   * @return the casted model.
   */
  private W toWidget(EditPart editPart) {
    return toWidget(editPart.getModel());
  }

  /**
   * @return the casted models.
   */
  private List<W> toWidgets(List<EditPart> editParts) {
    List<W> models = Lists.newArrayList();
    for (EditPart editPart : editParts) {
      models.add(toWidget(editPart));
    }
    return models;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractAlignmentActionsSupport<W> getAlignmentActionsSupport() {
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
