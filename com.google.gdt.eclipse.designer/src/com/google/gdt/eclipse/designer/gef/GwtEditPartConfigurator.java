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
package com.google.gdt.eclipse.designer.gef;

import com.google.gdt.eclipse.designer.gef.policy.OpenMenuItemCommandEditPolicy;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.menu.MenuItemInfo;
import com.google.gdt.eclipse.designer.preferences.IPreferenceConstants;

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.gef.core.EditPart;

/**
 * Configures GWT related {@link EditPart}'s.
 * 
 * @author scheglov_ke
 * @coverage gwt.gef.policy
 */
public final class GwtEditPartConfigurator implements IEditPartConfigurator {
  public void configure(EditPart context, EditPart editPart) {
    if (editPart.getModel() instanceof WidgetInfo) {
      WidgetInfo widget = (WidgetInfo) editPart.getModel();
      if (widget.getDescription().getToolkit().getId().equals(IPreferenceConstants.TOOLKIT_ID)) {
        configure(editPart, widget);
      }
    }
    // policies for MenuItem
    if (editPart.getModel() instanceof MenuItemInfo) {
      MenuItemInfo item = (MenuItemInfo) editPart.getModel();
      editPart.installEditPolicy(new OpenMenuItemCommandEditPolicy(item));
    }
  }

  /**
   * Implementation of {@link #configure(EditPart, EditPart)} for GWT {@link WidgetInfo}.
   */
  private void configure(EditPart editPart, WidgetInfo widget) {
  }
}
