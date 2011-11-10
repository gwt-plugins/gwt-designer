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
import com.google.gdt.eclipse.designer.model.widgets.panels.AbsolutePanelAlignmentSupport;
import com.google.gdt.eclipse.designer.model.widgets.panels.AbsolutePanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.IAbsolutePanelInfo;

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
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.AbstractAlignmentActionsSupport;
import org.eclipse.wb.internal.core.gef.policy.snapping.IAbsoluteLayoutCommands;
import org.eclipse.wb.internal.core.laf.BaselineSupportHelper;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link LayoutEditPolicy} for {@link AbsolutePanelInfo}.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class AbsolutePanelLayoutEditPolicy<T extends IWidgetInfo>
    extends
      AbsoluteBasedLayoutEditPolicy<T> {
  private final IAbsolutePanelInfo<T> m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private AbsolutePanelLayoutEditPolicy(IAbsolutePanelInfo<T> panel) {
    super(panel.getUnderlyingModel());
    m_panel = panel;
    createPlacementsSupport(IAbsoluteLayoutCommands.EMPTY);
  }

  public static <T extends IWidgetInfo> AbsolutePanelLayoutEditPolicy<T> create(IAbsolutePanelInfo<T> panel) {
    return new AbsolutePanelLayoutEditPolicy<T>(panel);
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
      EditPolicy policy = new AbsoluteLayoutSelectionEditPolicy<T>();
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, policy);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IVisualDataProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<T> getAllComponents() {
    List<T> components = m_panel.getChildrenWidgets();
    return new ArrayList<T>(components);
  }

  public int getBaseline(IAbstractComponentInfo component) {
    return BaselineSupportHelper.getBaseline(component.getObject());
  }

  public Dimension getComponentPreferredSize(IAbstractComponentInfo component) {
    return component.getPreferredSize();
  }

  public Dimension getContainerSize() {
    Dimension size = m_panel.getModelBounds().getSize().getCopy();
    Insets insets = m_panel.getClientAreaInsets();
    return size.shrink(insets.getWidth(), insets.getHeight());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(CreateRequest request) {
    final T widget = toWidget(request.getNewObject());
    return new EditCommand(m_panel) {
      @Override
      protected void executeEdit() throws Exception {
        placementsSupport.commitAdd();
        Rectangle widgetModelBounds = widget.getModelBounds();
        m_panel.command_CREATE2(widget, null);
        Point location = widgetModelBounds.getLocation();
        Dimension size = getSize(widgetModelBounds);
        m_panel.command_BOUNDS(widget, location, size);
      }

      private Dimension getSize(Rectangle widgetModelBounds) {
        boolean setSize = widget.shouldSetReasonableSize();
        return setSize || m_resizeOnCreate ? widgetModelBounds.getSize() : null;
      }
    };
  }

  @Override
  protected void doPasteComponent(Point pasteLocation, PastedComponentInfo pastedWidget)
      throws Exception {
    T widget = toWidget(pastedWidget.getComponent());
    // create
    m_panel.command_CREATE2(widget, null);
    m_panel.command_BOUNDS(
        widget,
        pasteLocation.getTranslated(pastedWidget.getBounds().getLocation()),
        pastedWidget.getBounds().getSize());
  }

  @Override
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    return new EditCommand(m_panel) {
      @Override
      protected void executeEdit() throws Exception {
        List<T> models = toWidgets(editParts);
        placementsSupport.commit();
        for (T widget : models) {
          Rectangle bounds = widget.getModelBounds();
          m_panel.command_BOUNDS(widget, bounds.getLocation(), null);
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
        List<T> models = toWidgets(editParts);
        placementsSupport.commitAdd();
        for (T widget : models) {
          Rectangle bounds = widget.getModelBounds();
          m_panel.command_MOVE2(widget, null);
          m_panel.command_BOUNDS(widget, bounds.getLocation(), null);
        }
      }
    };
  }

  @Override
  protected Command getResizeCommand(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    return new EditCommand(m_panel) {
      @Override
      protected void executeEdit() throws Exception {
        List<T> models = toWidgets(editParts);
        for (T widget : models) {
          Rectangle bounds = widget.getModelBounds();
          m_panel.command_BOUNDS(widget, bounds.getLocation(), bounds.getSize());
        }
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
  private T toWidget(Object o) {
    return (T) o;
  }

  /**
   * @return the casted models.
   */
  private List<T> toWidgets(List<EditPart> editParts) {
    List<T> models = Lists.newArrayList();
    for (EditPart editPart : editParts) {
      models.add(toWidget(editPart.getModel()));
    }
    return models;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractAlignmentActionsSupport<T> getAlignmentActionsSupport() {
    return AbsolutePanelAlignmentSupport.create(m_panel);
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
