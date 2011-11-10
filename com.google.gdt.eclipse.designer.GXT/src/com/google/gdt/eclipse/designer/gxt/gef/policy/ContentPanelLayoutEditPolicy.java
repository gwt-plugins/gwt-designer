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
package com.google.gdt.eclipse.designer.gxt.gef.policy;

import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ContentPanelInfo;

import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.position.ObjectPositionLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.LocationRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for top/bottom component of {@link ContentPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.policy
 */
public final class ContentPanelLayoutEditPolicy
    extends
      ObjectPositionLayoutEditPolicy<ComponentInfo, String> {
  public static final ILayoutRequestValidator VALIDATOR =
      LayoutRequestValidators.modelType(ComponentInfo.class);
  private final ContentPanelInfo m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ContentPanelLayoutEditPolicy(ContentPanelInfo panel) {
    super(panel);
    m_panel = panel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return VALIDATOR;
  }

  @Override
  protected boolean isRequestCondition(Request request) {
    // active only in top 15 pixels
    if (request instanceof LocationRequest) {
      LocationRequest locationRequest = (LocationRequest) request;
      Point location = locationRequest.getLocation().getCopy();
      PolicyUtils.translateAbsoluteToModel(this, location);
      if (location.y >= 15) {
        eraseTargetFeedback(request);
        return false;
      }
    }
    return super.isRequestCondition(request);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addFeedbacks() throws Exception {
    if (!m_panel.hasTopComponent()) {
      addFeedback(0.0, 0, 0.5, 15, new Insets(0, 0, 0, 1), "Top", "Top");
    }
    if (!m_panel.hasBottomComponent()) {
      addFeedback(0.5, 0, 1.0, 15, new Insets(0, 1, 0, 0), "Bottom", "Bottom");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation of commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(ComponentInfo component, String data) throws Exception {
    if (data == "Top") {
      m_panel.setTopComponent(component);
    }
    if (data == "Bottom") {
      m_panel.setBottomComponent(component);
    }
  }

  @Override
  protected void command_MOVE(ComponentInfo component, String data) throws Exception {
    command_CREATE(component, data);
  }
}