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

import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.ListGridInfo;

import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.menu.Menu</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class MenuInfo extends ListGridInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the list of children {@link MenuInfo}.
   */
  public List<MenuItemInfo> getItems() {
    return getChildren(MenuItemInfo.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_dispose_detach() throws Exception {
    // detach items
    if (isCreated()) {
      for (MenuItemInfo menuItem : getItems()) {
        if (menuItem.isCreated()) {
          ReflectionUtils.invokeMethod(
              getObject(),
              "removeItem(com.smartgwt.client.widgets.menu.MenuItem)",
              menuItem.getObject());
        }
      }
    }
    super.refresh_dispose_detach();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Fetch
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Rectangle fetchAbsoluteBounds(Object element) {
    Association association = getAssociation();
    if (association instanceof InvocationAssociation) {
      MethodInvocation methodInvocation = ((InvocationAssociation) association).getInvocation();
      if (getParent() instanceof CanvasInfo
          && "addChild(com.smartgwt.client.widgets.Canvas)".equals(AstNodeUtils.getMethodSignature(methodInvocation))) {
        return super.fetchAbsoluteBounds(element);
      }
    }
    return new Rectangle(0, 0, 0, 0);
  }
}
