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
package com.google.gdt.eclipse.designer.gxt.gef.part;

import com.google.gdt.eclipse.designer.gef.part.panels.AbstractWidgetHandleEditPart;
import com.google.gdt.eclipse.designer.gef.policy.AbstractWidgetHandleDirectTextEditPolicy;
import com.google.gdt.eclipse.designer.gxt.model.widgets.TabPanelInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.TabPanelInfo.Header;

import org.eclipse.wb.gef.core.EditPart;

/**
 * {@link EditPart} for {@link TabPanelInfo.Header}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.part
 */
public final class TabPanelHeaderEditPart extends AbstractWidgetHandleEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TabPanelHeaderEditPart(Header handle) {
    super(handle);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    AbstractWidgetHandleDirectTextEditPolicy.install(this, "text");
  }
}
