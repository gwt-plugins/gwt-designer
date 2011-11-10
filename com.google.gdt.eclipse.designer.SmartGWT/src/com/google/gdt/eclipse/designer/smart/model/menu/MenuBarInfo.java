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
package com.google.gdt.eclipse.designer.smart.model.menu;

import com.google.gdt.eclipse.designer.smart.model.ArrayChildrenContainerUtils;
import com.google.gdt.eclipse.designer.smart.model.ComponentConfiguratorBeforeAssociation;
import com.google.gdt.eclipse.designer.smart.model.LayoutInfo;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;
import org.eclipse.wb.internal.core.model.nonvisual.ArrayObjectInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.menu.MenuBar</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class MenuBarInfo extends LayoutInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuBarInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    // listeners
    ensureAtLeastOneMenu();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  private void ensureAtLeastOneMenu() throws Exception {
    new ComponentConfiguratorBeforeAssociation(this) {
      @Override
      protected void configure() throws Exception {
        if (getMenus().isEmpty()) {
          JavaInfoUtils.executeScript(MenuBarInfo.this, CodeUtils.getSource(
              "import com.smartgwt.client.widgets.menu.Menu;",
              "Menu menu = new com.smartgwt.client.widgets.menu.Menu();",
              "menu.setTitle('Empty MenuBar');",
              "object.setMenus(new com.smartgwt.client.widgets.menu.Menu[] {menu});"));
        }
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the list of children {@link MenuInfo}.
   */
  public List<MenuInfo> getMenus() {
    return getChildren(MenuInfo.class);
  }

  /**
   * @return the {@link ArrayObjectInfo} for "setFields" invocation.
   */
  public AbstractArrayObjectInfo getMenusArrayInfo() throws Exception {
    return ArrayChildrenContainerUtils.getMethodParameterArrayInfo(
        this,
        "setMenus",
        "com.smartgwt.client.widgets.menu.Menu");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(MenuInfo newMenu, MenuInfo referenceMenu) throws Exception {
    AbstractArrayObjectInfo arrayInfo = getMenusArrayInfo();
    arrayInfo.command_CREATE(newMenu, referenceMenu);
  }

  public void command_MOVE(MenuInfo moveMenu, MenuInfo referenceMenu) throws Exception {
    AbstractArrayObjectInfo arrayInfo = getMenusArrayInfo();
    arrayInfo.command_MOVE(moveMenu, referenceMenu);
  }
}
