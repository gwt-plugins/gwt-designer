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

import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.AbstractWidgetHandle;
import com.google.gdt.eclipse.designer.model.widgets.panels.IFlowLikePanelInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.flow.AbstractFlowLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for panel with widgets and tabs/headers, operations with
 * {@link AbstractWidgetHandle}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class TabLikePanelHandleLayoutEditPolicy<W extends IWidgetInfo, P extends IWidgetInfo & IFlowLikePanelInfo<W>>
    extends
      AbstractFlowLayoutEditPolicy {
  private static final ILayoutRequestValidator VALIDATOR =
      LayoutRequestValidators.modelType(AbstractWidgetHandle.class);
  private final P m_panel;
  private final boolean m_horizontal;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private TabLikePanelHandleLayoutEditPolicy(P panel, boolean horizontal) {
    m_panel = panel;
    m_horizontal = horizontal;
  }

  public static <W extends IWidgetInfo, P extends IWidgetInfo & IFlowLikePanelInfo<W>> TabLikePanelHandleLayoutEditPolicy<W, P> create(P panel,
      boolean horizontal) {
    return new TabLikePanelHandleLayoutEditPolicy<W, P>(panel, horizontal);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isHorizontal(Request request) {
    return m_horizontal;
  }

  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return VALIDATOR;
  }

  @Override
  protected boolean isRequestCondition(Request _request) {
    if (_request instanceof ChangeBoundsRequest) {
      ChangeBoundsRequest request = (ChangeBoundsRequest) _request;
      Object type = request.getType();
      GraphicalEditPart host = getHost();
      ILayoutRequestValidator validator = getRequestValidator();
      boolean isMove =
          Request.REQ_MOVE.equals(type) && validator.validateMoveRequest(host, request);
      boolean isAdd = Request.REQ_ADD.equals(type) && validator.validateMoveRequest(host, request);
      return isMove || isAdd;
    }
    return false;
  }

  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart.getModel() instanceof AbstractWidgetHandle;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getMoveCommand(Object moveObject, Object referenceObject) {
    final W component = getWidget(moveObject);
    final W reference = getWidget(referenceObject);
    return new EditCommand(m_panel) {
      @Override
      protected void executeEdit() throws Exception {
        m_panel.command_MOVE2(component, reference);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IWidgetInfo} from given {@link AbstractWidgetHandle} object, may be
   *         <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  private W getWidget(Object referenceObject) {
    if (referenceObject != null) {
      return ((AbstractWidgetHandle<W>) referenceObject).getWidget();
    } else {
      return null;
    }
  }
}
