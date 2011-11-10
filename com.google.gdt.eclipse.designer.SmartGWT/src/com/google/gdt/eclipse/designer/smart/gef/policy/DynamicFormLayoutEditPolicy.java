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
import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.form.DynamicFormAlignmentSupport;
import com.google.gdt.eclipse.designer.smart.model.form.DynamicFormInfo;
import com.google.gdt.eclipse.designer.smart.model.form.FormItemInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.core.model.AbstractComponentInfo;
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
 * {@link LayoutEditPolicy} for {@link DynamicFormInfo}.
 * 
 * @author scheglov_ke
 * @coverage SmartGWT.gef.policy
 */
public final class DynamicFormLayoutEditPolicy extends AbsoluteBasedLayoutEditPolicy<FormItemInfo> {
  private final DynamicFormInfo m_form;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DynamicFormLayoutEditPolicy(DynamicFormInfo canvas) {
    super(canvas);
    m_form = canvas;
    createPlacementsSupport(IAbsoluteLayoutCommands.EMPTY);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return INSTANCE;
  }

  public static final ILayoutRequestValidator INSTANCE = LayoutRequestValidators.or(
      LayoutRequestValidators.modelType(FormItemInfo.class),
      LayoutRequestValidators.modelType(CanvasInfo.class));

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    Object childModel = child.getModel();
    if (childModel instanceof FormItemInfo) {
      EditPolicy policy = new AbsoluteLayoutSelectionEditPolicy<AbstractComponentInfo>();
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, policy);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IVisualDataProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<FormItemInfo> getAllComponents() {
    List<FormItemInfo> components = m_form.getItems();
    return new ArrayList<FormItemInfo>(components);
  }

  public int getBaseline(IAbstractComponentInfo component) {
    return BaselineSupportHelper.getBaseline(((AbstractComponentInfo) component).getComponentObject());
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
    Dimension size = m_form.getModelBounds().getSize().getCopy();
    Insets insets = m_form.getClientAreaInsets();
    return size.shrink(insets.getWidth(), insets.getHeight());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(CreateRequest request) {
    Object newObject = request.getNewObject();
    if (newObject instanceof FormItemInfo) {
      final FormItemInfo item = (FormItemInfo) newObject;
      return new EditCommand(m_form) {
        @Override
        protected void executeEdit() throws Exception {
          placementsSupport.commitAdd();
          Rectangle widgetModelBounds = item.getModelBounds();
          m_form.command_CREATE(item, null);
          m_form.command_BOUNDS(item, widgetModelBounds.getLocation(), widgetModelBounds.getSize());
        }
      };
    }
    if (newObject instanceof CanvasInfo) {
      final CanvasInfo canvas = (CanvasInfo) newObject;
      return new EditCommand(m_form) {
        @Override
        protected void executeEdit() throws Exception {
          placementsSupport.commitAdd();
          Rectangle widgetModelBounds = canvas.getModelBounds();
          m_form.command_CREATE(canvas, null);
          m_form.command_BOUNDS(
              (FormItemInfo) canvas.getParentJava(),
              widgetModelBounds.getLocation(),
              widgetModelBounds.getSize());
        }
      };
    }
    return null;
  }

  @Override
  protected void doPasteComponent(Point pasteLocation, PastedComponentInfo pastedWidget)
      throws Exception {
    IAbstractComponentInfo component = pastedWidget.getComponent();
    if (component instanceof FormItemInfo) {
      FormItemInfo item = (FormItemInfo) component;
      // create
      m_form.command_CREATE(item, null);
      Rectangle widgetBounds = pastedWidget.getBounds();
      m_form.command_BOUNDS(
          item,
          pasteLocation.getTranslated(widgetBounds.getLocation()),
          widgetBounds.getSize());
    }
    if (component instanceof CanvasInfo) {
      CanvasInfo canvas = (CanvasInfo) component;
      // create
      m_form.command_CREATE(canvas, null);
      Rectangle widgetBounds = pastedWidget.getBounds();
      m_form.command_BOUNDS(
          (FormItemInfo) canvas.getParentJava(),
          pasteLocation.getTranslated(widgetBounds.getLocation()),
          widgetBounds.getSize());
    }
  }

  @Override
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    return new EditCommand(m_form) {
      @Override
      protected void executeEdit() throws Exception {
        ArrayList<IAbstractComponentInfo> models = Lists.newArrayList();
        for (EditPart editPart : editParts) {
          models.add((IAbstractComponentInfo) editPart.getModel());
        }
        placementsSupport.commit();
        for (IAbstractComponentInfo widget : models) {
          Rectangle bounds = widget.getModelBounds();
          if (widget instanceof FormItemInfo) {
            m_form.command_BOUNDS((FormItemInfo) widget, bounds.getLocation(), null);
          }
          if (widget instanceof CanvasInfo) {
            m_form.command_BOUNDS(
                (FormItemInfo) widget.getUnderlyingModel().getParent(),
                bounds.getLocation(),
                null);
          }
        }
      }
    };
  }

  @Override
  protected Command getAddCommand(ChangeBoundsRequest request) {
    final List<EditPart> editParts = request.getEditParts();
    return new EditCommand(m_form) {
      @Override
      protected void executeEdit() throws Exception {
        ArrayList<IAbstractComponentInfo> models = Lists.newArrayList();
        for (EditPart editPart : editParts) {
          models.add((IAbstractComponentInfo) editPart.getModel());
        }
        placementsSupport.commitAdd();
        for (IAbstractComponentInfo widget : models) {
          Rectangle bounds = widget.getModelBounds();
          if (widget instanceof FormItemInfo) {
            m_form.command_MOVE((FormItemInfo) widget, null);
            m_form.command_BOUNDS((FormItemInfo) widget, bounds.getLocation(), null);
          }
          if (widget instanceof CanvasInfo) {
            m_form.command_MOVE((CanvasInfo) widget, null);
            m_form.command_BOUNDS(
                (FormItemInfo) widget.getUnderlyingModel().getParent(),
                bounds.getLocation(),
                null);
          }
        }
      }
    };
  }

  @Override
  protected Command getResizeCommand(final ChangeBoundsRequest request) {
    return new EditCommand(m_form) {
      @Override
      protected void executeEdit() throws Exception {
        for (EditPart editPart : request.getEditParts()) {
          IAbstractComponentInfo widget = (IAbstractComponentInfo) editPart.getModel();
          Rectangle bounds = widget.getModelBounds();
          if (widget instanceof FormItemInfo) {
            m_form.command_BOUNDS((FormItemInfo) widget, bounds.getLocation(), bounds.getSize());
          }
          if (widget instanceof CanvasInfo) {
            m_form.command_BOUNDS(
                (FormItemInfo) widget.getUnderlyingModel().getParent(),
                bounds.getLocation(),
                bounds.getSize());
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
  protected AbstractAlignmentActionsSupport<FormItemInfo> getAlignmentActionsSupport() {
    return new DynamicFormAlignmentSupport(m_form);
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
