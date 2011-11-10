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
package com.google.gdt.eclipse.designer.gwtext.gef.policy;

import com.google.gdt.eclipse.designer.gef.policy.WidgetFlowLayoutEditPolicy;
import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for flow {@link Layout_Info}.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.gef.policy
 */
public abstract class AbstractFlowLayoutEditPolicy extends WidgetFlowLayoutEditPolicy<WidgetInfo> {
  private final LayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractFlowLayoutEditPolicy(LayoutInfo layout) {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation of commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(WidgetInfo newObject, WidgetInfo referenceObject) throws Exception {
    m_layout.command_CREATE(newObject, referenceObject);
  }

  @Override
  protected void command_MOVE(WidgetInfo object, WidgetInfo referenceObject) throws Exception {
    m_layout.command_MOVE(object, referenceObject);
  }
}
