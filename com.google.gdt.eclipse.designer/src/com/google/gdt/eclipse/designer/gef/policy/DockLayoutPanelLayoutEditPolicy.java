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
import com.google.gdt.eclipse.designer.model.widgets.panels.DockLayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.IDockLayoutPanelInfo;

import org.eclipse.wb.core.gef.policy.helpers.SelectionPolicyDecorationHelper;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for {@link DockLayoutPanelInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class DockLayoutPanelLayoutEditPolicy<T extends IWidgetInfo>
    extends
      WidgetPositionLayoutEditPolicy<T, String> {
  private final IDockLayoutPanelInfo<T> m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private DockLayoutPanelLayoutEditPolicy(IDockLayoutPanelInfo<T> panel) {
    super(panel.getUnderlyingModel());
    m_panel = panel;
    new SelectionPolicyDecorationHelper(this) {
      @Override
      protected boolean shouldChangePolicy(EditPart child) {
        return child.getModel() instanceof IWidgetInfo;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected EditPolicy getNewPolicy(EditPart child) {
        T widget = (T) child.getModel();
        return DockLayoutPanelSelectionEditPolicy.create(m_panel, widget);
      }
    };
  }

  public static <T extends IWidgetInfo> DockLayoutPanelLayoutEditPolicy<T> create(IDockLayoutPanelInfo<T> panel) {
    return new DockLayoutPanelLayoutEditPolicy<T>(panel);
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(T widget, String edge) throws Exception {
    m_panel.command_CREATE2(widget, null);
    m_panel.setEdge(widget, edge);
    m_panel.setReasonableSize(widget);
  }

  @Override
  protected void command_MOVE(T widget, String edge) throws Exception {
    m_panel.setEdge(widget, edge);
  }

  @Override
  protected void command_ADD(T widget, String edge) throws Exception {
    m_panel.command_MOVE2(widget, null);
    m_panel.setEdge(widget, edge);
    m_panel.setReasonableSize(widget);
  }
}
