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

import com.google.gdt.eclipse.designer.gef.policy.WidgetPositionLayoutEditPolicy;
import com.google.gdt.eclipse.designer.gxt.model.layout.BorderLayoutInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.policy.helpers.SelectionPolicyDecorationHelper;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for {@link BorderLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.policy
 */
public final class BorderLayoutEditPolicy
    extends
      WidgetPositionLayoutEditPolicy<WidgetInfo, String> {
  private final BorderLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BorderLayoutEditPolicy(BorderLayoutInfo layout) {
    super(layout);
    m_layout = layout;
    new SelectionPolicyDecorationHelper(this) {
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return LayoutRequestValidators.componentType("com.extjs.gxt.ui.client.widget.BoxComponent");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addFeedbacks() throws Exception {
    addFeedback(0, 0, 1, 0.25, new Insets(0, 0, 1, 0), "North", "NORTH");
    addFeedback(0, 0.75, 1, 1, new Insets(1, 0, 0, 0), "South", "SOUTH");
    addFeedback(0, 0.25, 0.25, 0.75, new Insets(1, 0, 1, 1), "West", "WEST");
    addFeedback(0.75, 0.25, 1, 0.75, new Insets(1, 1, 1, 0), "East", "EAST");
    addFeedback(0.25, 0.25, 0.75, 0.75, new Insets(1, 1, 1, 1), "Center", "CENTER");
  }

  /**
   * Adds feedback for given constraints.
   */
  private void addFeedback(double px1,
      double py1,
      double px2,
      double py2,
      Insets insets,
      String hint,
      String region) throws Exception {
    if (m_layout.getWidget(region) == null) {
      super.addFeedback(px1, py1, px2, py2, insets, hint, region);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation of commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(WidgetInfo component, String data) throws Exception {
    m_layout.command_CREATE(component, null);
    BorderLayoutInfo.getBorderData(component).setRegion(data);
  }

  @Override
  protected void command_MOVE(WidgetInfo component, String data) throws Exception {
    BorderLayoutInfo.getBorderData(component).setRegion(data);
  }

  @Override
  protected void command_ADD(WidgetInfo component, String data) throws Exception {
    m_layout.command_MOVE(component, null);
    BorderLayoutInfo.getBorderData(component).setRegion(data);
  }
}
