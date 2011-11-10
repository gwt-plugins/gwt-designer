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
package com.google.gdt.eclipse.designer.gef.part;

import com.google.gdt.eclipse.designer.gef.policy.UIObjectSelectionEditPolicy;
import com.google.gdt.eclipse.designer.model.widgets.UIObjectInfo;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;

import org.eclipse.swt.SWT;

/**
 * {@link EditPart} for {@link UIObjectInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.part
 */
public class UIObjectEditPart extends AbstractComponentEditPart {
  private final UIObjectInfo m_object;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UIObjectEditPart(UIObjectInfo object) {
    super(object);
    m_object = object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void drawCustomBorder(Figure figure, Graphics graphics) {
    if (m_object.shouldDrawDotsBorder()) {
      graphics.setForegroundColor(IColorConstants.gray);
      graphics.setLineStyle(SWT.LINE_DOT);
      Rectangle area = figure.getClientArea();
      graphics.drawRectangle(0, 0, area.width - 1, area.height - 1);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    if (!m_object.isRoot()) {
      installEditPolicy(EditPolicy.SELECTION_ROLE, new UIObjectSelectionEditPolicy(m_object));
    }
  }
}
