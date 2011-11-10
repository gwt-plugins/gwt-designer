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
package com.google.gdt.eclipse.designer.gxt.model.widgets.menu;

import com.google.gdt.eclipse.designer.gxt.model.widgets.ComponentInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.ConstructorChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.swt.graphics.Image;

/**
 * Model for <code>com.extjs.gxt.ui.client.widget.menu.MenuBarItem</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public final class MenuBarItemInfo extends ComponentInfo implements IAdaptable {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuBarItemInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    createChildMenu_forNewMenuBarItem();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createChildMenu_forNewMenuBarItem() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (child == MenuBarItemInfo.this) {
          createChildMenu(child);
        }
      }
    });
  }

  private void createChildMenu(JavaInfo child) throws Exception {
    // prepare "null" expression to replace with Slider
    Expression menuExpression;
    {
      ClassInstanceCreation fieldCreation =
          ((ConstructorCreationSupport) child.getCreationSupport()).getCreation();
      menuExpression = DomGenerics.arguments(fieldCreation).get(1);
    }
    // create new Menu
    JavaInfo menu =
        JavaInfoUtils.createJavaInfo(
            getEditor(),
            "com.extjs.gxt.ui.client.widget.menu.Menu",
            new ConstructorCreationSupport());
    // prepare "new Menu()" source
    String menuSource;
    {
      StatementTarget statementTarget =
          new StatementTarget(AstNodeUtils.getEnclosingStatement(menuExpression), true);
      NodeTarget nodeTarget = new NodeTarget(statementTarget);
      menuSource = menu.getCreationSupport().add_getSource(nodeTarget);
    }
    // replace "null" with "new Menu()"
    menuExpression = getEditor().replaceExpression(menuExpression, menuSource);
    menu.setVariableSupport(new EmptyVariableSupport(menu, menuExpression));
    menu.getCreationSupport().add_setSourceExpression(menuExpression);
    // add Menu as child
    menu.setAssociation(new ConstructorChildAssociation());
    addChild(menu);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MenuInfo} child.
   */
  public MenuInfo getSubMenu() {
    return getChildren(MenuInfo.class).get(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    JavaInfoUtils.executeScript(this, "object.parent.setActiveItem(object, true)");
    super.refresh_fetch();
    ReflectionUtils.invokeMethod(
        ReflectionUtils.invokeMethod(getObject(), "getParent()"),
        "onDeactivate(com.extjs.gxt.ui.client.widget.menu.MenuBarItem)",
        getObject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IMenuItemInfo m_itemImpl = new MenuItemImpl();

  public <T> T getAdapter(Class<T> adapter) {
    if (adapter.isAssignableFrom(IMenuItemInfo.class)) {
      return adapter.cast(m_itemImpl);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuItemInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link IMenuItemInfo}.
   * 
   * @author scheglov_ke
   */
  private final class MenuItemImpl extends AbstractMenuObject implements IMenuItemInfo {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MenuItemImpl() {
      super(MenuBarItemInfo.this);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Model
    //
    ////////////////////////////////////////////////////////////////////////////
    public Object getModel() {
      return MenuBarItemInfo.this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    public Image getImage() {
      return null;
    }

    public Rectangle getBounds() {
      return MenuBarItemInfo.this.getBounds();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuItemInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuInfo getMenu() {
      MenuInfo menu = getSubMenu();
      return MenuObjectInfoUtils.getMenuInfo(menu);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Policy
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuPolicy getPolicy() {
      return IMenuPolicy.NOOP;
    }
  }
}
