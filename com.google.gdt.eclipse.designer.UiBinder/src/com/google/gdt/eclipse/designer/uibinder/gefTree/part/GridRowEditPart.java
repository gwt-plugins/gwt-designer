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
package com.google.gdt.eclipse.designer.uibinder.gefTree.part;

import com.google.gdt.eclipse.designer.uibinder.gefTree.policy.GridRowLayoutEditPolicy;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.GridInfo.Row;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.core.xml.gefTree.part.XmlObjectEditPart;

/**
 * {@link EditPart} for {@link Row}.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.gef
 */
public final class GridRowEditPart extends XmlObjectEditPart {
  private final Row m_row;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridRowEditPart(Row row) {
    super(row);
    m_row = row;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(EditPolicy.LAYOUT_ROLE, new GridRowLayoutEditPolicy(m_row));
  }
}
