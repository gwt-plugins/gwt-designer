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

import com.google.gdt.eclipse.designer.gxt.model.layout.AccordionLayoutInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.ContentPanelInfo;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.events.IEditPartSelectionListener;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.GraphicalEditPolicy;

/**
 * This {@link EditPolicy} activates (expands) its {@link ContentPanelInfo} when user selects it.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.policy
 */
public final class AccordionSelectionEditPolicy extends GraphicalEditPolicy
    implements
      IEditPartSelectionListener {
  private final AccordionLayoutInfo m_layout;
  private final ContentPanelInfo m_panel;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AccordionSelectionEditPolicy(AccordionLayoutInfo layout, ContentPanelInfo panel) {
    m_layout = layout;
    m_panel = panel;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EditPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void activate() {
    super.activate();
    getHost().addSelectionListener(this);
  }

  @Override
  public void deactivate() {
    getHost().removeSelectionListener(this);
    super.deactivate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartSelectionListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void selectionChanged(EditPart editPart) {
    if (editPart.getSelected() == EditPart.SELECTED_PRIMARY) {
      m_layout.setActivePanel(m_panel);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean understandsRequest(Request request) {
    return request.getType() == Request.REQ_SELECTION;
  }

  @Override
  public EditPart getTargetEditPart(Request request) {
    return getHost();
  }
}