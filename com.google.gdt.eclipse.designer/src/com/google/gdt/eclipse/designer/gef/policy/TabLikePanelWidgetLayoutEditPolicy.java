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

import com.google.gdt.eclipse.designer.gef.WidgetsLayoutRequestValidator;
import com.google.gdt.eclipse.designer.model.widgets.IWidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.AbstractWidgetHandle;
import com.google.gdt.eclipse.designer.model.widgets.panels.IFlowLikePanelInfo;

import org.eclipse.wb.core.gef.policy.layout.flow.ObjectFlowLayoutEditPolicy;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for panel with widgets and tabs/headers, operations with
 * {@link IWidgetInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class TabLikePanelWidgetLayoutEditPolicy<W extends IWidgetInfo, P extends IWidgetInfo & IFlowLikePanelInfo<W>>
    extends
      ObjectFlowLayoutEditPolicy<W> {
  private final P m_panel;
  private final boolean m_horizontal;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private TabLikePanelWidgetLayoutEditPolicy(P panel, boolean horizontal) {
    super(panel);
    m_panel = panel;
    m_horizontal = horizontal;
  }

  public static <W extends IWidgetInfo, P extends IWidgetInfo & IFlowLikePanelInfo<W>> TabLikePanelWidgetLayoutEditPolicy<W, P> create(P panel,
      boolean horizontal) {
    return new TabLikePanelWidgetLayoutEditPolicy<W, P>(panel, horizontal);
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
    return WidgetsLayoutRequestValidator.INSTANCE;
  }

  @Override
  protected final boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart.getModel() instanceof AbstractWidgetHandle;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(W component, W reference) throws Exception {
    m_panel.command_CREATE2(component, reference);
  }

  @Override
  protected void command_MOVE(W component, W reference) throws Exception {
    m_panel.command_MOVE2(component, reference);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  @SuppressWarnings("unchecked")
  protected W getReferenceObjectModel(Object referenceObject) {
    if (referenceObject != null) {
      return ((AbstractWidgetHandle<W>) referenceObject).getWidget();
    } else {
      return null;
    }
  }
}
