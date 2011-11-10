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
import com.google.gdt.eclipse.designer.gxt.model.widgets.ContainerInfo;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.JavaMenuMenuObject;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Model for <code>com.extjs.gxt.ui.client.widget.menu.Menu</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public final class MenuInfo extends ContainerInfo implements IAdaptable {
  private final MenuInfo m_this = this;

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
   * @return {@link ComponentInfo} children.
   */
  public List<ComponentInfo> getItems() {
    return getChildren(ComponentInfo.class);
  }

  /**
   * @return <code>true</code> if this {@link MenuInfo} is sub-menu of {@link MenuItemInfo}.
   */
  public boolean isSubMenu() {
    return getParent() instanceof MenuBarItemInfo || getParent() instanceof MenuItemInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    if (!isSubMenu()) {
      JavaInfoUtils.executeScript(this, "object.showAt(0, 0);");
    }
    super.refresh_fetch();
    {
      Object element = getElement();
      Image browserScreenshot = getState().createBrowserScreenshot();
      Rectangle absoluteBounds = getState().getAbsoluteBounds(element);
      Image objectImage =
          UiUtils.getCroppedImage(browserScreenshot, absoluteBounds.getSwtRectangle());
      setImage(objectImage);
    }
    if (!isSubMenu()) {
      JavaInfoUtils.executeScript(this, "object.hide();");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IMenuPopupInfo m_popupImpl = new MenuPopupImpl();
  private final IMenuInfo m_menuImpl = new MenuImpl();
  private final IMenuPolicy m_menuPolicyImpl = new MenuPolicyImpl(this);

  public <T> T getAdapter(Class<T> adapter) {
    if (adapter.isAssignableFrom(IMenuPopupInfo.class)) {
      return adapter.cast(m_popupImpl);
    }
    if (adapter.isAssignableFrom(IMenuInfo.class)) {
      return adapter.cast(m_menuImpl);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractMenuImpl
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Abstract superclass for {@link IMenuObjectInfo} implementations.
   * 
   * @author scheglov_ke
   */
  private abstract class MenuAbstractImpl extends JavaMenuMenuObject {
    public MenuAbstractImpl() {
      super(m_this);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuPopupInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link IMenuPopupInfo}.
   * 
   * @author scheglov_ke
   */
  private final class MenuPopupImpl extends MenuAbstractImpl implements IMenuPopupInfo {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Model
    //
    ////////////////////////////////////////////////////////////////////////////
    public Object getModel() {
      return m_this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    public Image getImage() {
      return getDescription().getIcon();
    }

    public Rectangle getBounds() {
      // prepare information about parent
      ComponentInfo parentControl = (ComponentInfo) getParent();
      Insets parentInsets = parentControl.getClientAreaInsets();
      Rectangle parentArea = parentControl.getBounds().getCropped(parentInsets);
      // prepare size of "popup"
      int width;
      int height;
      {
        Image image = getImage();
        width = image.getBounds().width;
        height = image.getBounds().height;
      }
      // prepare bounds for "popup"
      int x = parentInsets.left + 3;
      int y = parentArea.height - parentInsets.bottom - 3 - height;
      return new Rectangle(x, y, width, height);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMenuPopupInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuInfo getMenu() {
      return m_menuImpl;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Policy
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuPolicy getPolicy() {
      return m_menuPolicyImpl;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link IMenuInfo}.
   * 
   * @author scheglov_ke
   */
  private final class MenuImpl extends MenuAbstractImpl implements IMenuInfo {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Model
    //
    ////////////////////////////////////////////////////////////////////////////
    public Object getModel() {
      return isSubMenu() ? m_this : this;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    public Image getImage() {
      return m_this.getImage();
    }

    public Rectangle getBounds() {
      return m_this.getBounds();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public boolean isHorizontal() {
      return false;
    }

    public List<IMenuItemInfo> getItems() {
      return MenuUtils.getItems(m_this);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Policy
    //
    ////////////////////////////////////////////////////////////////////////////
    public IMenuPolicy getPolicy() {
      return m_menuPolicyImpl;
    }
  }
}
