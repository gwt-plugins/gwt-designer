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
package com.google.gdt.eclipse.designer.uibinder.gef.part;

import com.google.gdt.eclipse.designer.uibinder.gef.policy.GridLayoutEditPolicy;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.GridInfo;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.GridInfo.GridElement;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;

/**
 * {@link EditPart} for {@link GridElement}.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.gef
 */
public class GridEditPart extends PanelEditPart {
  private final GridInfo m_grid;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridEditPart(GridInfo grid) {
    super(grid);
    m_grid = grid;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(EditPolicy.LAYOUT_ROLE, new GridLayoutEditPolicy(m_grid));
  }
}
