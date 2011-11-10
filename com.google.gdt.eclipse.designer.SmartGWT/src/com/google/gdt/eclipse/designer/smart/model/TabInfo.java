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
package com.google.gdt.eclipse.designer.smart.model;

import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.core.model.broadcast.JavaInfoChildBeforeAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

/**
 * Model for <code>com.smartgwt.client.widgets.tab.Tab</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class TabInfo extends JsObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TabInfo(AstEditor editor, ComponentDescription description, CreationSupport creationSupport)
      throws Exception {
    super(editor, description, creationSupport);
    addBroadcastListener(new JavaInfoChildBeforeAssociation(this));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public TabSetInfo getTabSet() {
    return (TabSetInfo) getParentJava();
  }

  @Override
  public boolean isCreated() {
    return super.isCreated() && getTabSet().isCreated();
  }

  /**
   * Makes this item selected.
   */
  public void doSelect() {
    getTabSet().setSelectedTab(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    TabSetInfo tabSet = getTabSet();
    if (tabSet.getSelectedTab() == this) {
      // calculate bounds only for selected tab 
      Rectangle bounds = tabSet.getAbsoluteBounds().getCopy();
      Integer barThickness = tabSet.getTabBarThickness();
      String barPosition = tabSet.getTabBarPosition();
      // cut bar area
      if (barPosition.equalsIgnoreCase("TOP")) {
        bounds.moveY(barThickness);
      } else if (barPosition.equalsIgnoreCase("LEFT")) {
        bounds.moveX(barThickness);
      } else if (barPosition.equalsIgnoreCase("BOTTOM")) {
        bounds.resize(0, -barThickness);
      } else if (barPosition.equalsIgnoreCase("RIGHT")) {
        bounds.resize(-barThickness, 0);
      }
      // crop insets
      bounds.crop(tabSet.getTabInsets());
      // IE in strict mode always has border 2px
      {
        GwtState state = tabSet.getState();
        if (state.isStrictMode() && state.isBrowserExplorer()) {
          bounds.translate(2, 2);
        }
      }
      // ready
      tabSet.absoluteToRelative(bounds);
      setModelBounds(bounds);
      // process children
      super.refresh_fetch();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Notification that this {@link TabInfo} was used as target of CREATE or ADD operation.
   */
  public void command_TARGET_after(CanvasInfo control) throws Exception {
    doSelect();
  }
}
