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
package com.google.gdt.eclipse.designer.model.widgets;

import com.google.gdt.eclipse.designer.model.widgets.support.DOMUtils;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.model.widgets.support.UIObjectUtils;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Translatable;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for any GWT <code>UIObject</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public class UIObjectInfo extends AbstractComponentInfo implements IUIObjectInfo {
  public static final String STATE_KEY = "GWTState";
  private final UIObjectInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UIObjectInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    m_sizeSupport = createSizeSupport();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGWTStateProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public final GwtState getState() {
    return (GwtState) getEditor().getGlobalValue(STATE_KEY);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link UIObjectUtils} to work with <code>UIObject</code>.
   */
  public final UIObjectUtils getUIObjectUtils() {
    return getState().getUIObjectUtils();
  }

  /**
   * @return the {@link DOMUtils} to work with <code>Element</code>.
   */
  public final DOMUtils getDOMUtils() {
    return getState().getDomUtils();
  }

  /**
   * @return the DOM borders.
   */
  public final Insets getBorders() {
    return m_borders;
  }

  /**
   * @return the DOM margins.
   */
  public final Insets getMargins() {
    return m_margins;
  }

  /**
   * @return the DOM paddings.
   */
  public final Insets getPaddings() {
    return m_paddings;
  }

  /**
   * @return the underlying DOM <code>Element</code>.
   */
  public Object getElement() throws Exception {
    return getState().getUIObjectUtils().getElement(getObject());
  }

  /**
   * @return <code>true</code> if this {@link UIObjectInfo} has no visible border, so artificial
   *         border should be drawn.
   */
  public boolean shouldDrawDotsBorder() {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        String script = JavaInfoUtils.getParameter(m_this, "shouldDrawBorder");
        if (!StringUtils.isEmpty(script)) {
          return (Boolean) JavaInfoUtils.executeScript(m_this, script);
        }
        return false;
      }
    }, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Size
  //
  ////////////////////////////////////////////////////////////////////////////
  private final UIObjectSizeSupport m_sizeSupport;

  public final IUIObjectSizeSupport getSizeSupport() {
    return m_sizeSupport;
  }

  /**
   * Creates instance of {@link UIObjectSizeSupport}. Subclasses in toolkits may override to add
   * support for additional size-related methods.
   */
  protected UIObjectSizeSupport createSizeSupport() {
    return new UIObjectSizeSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    Property sizeProperty = getSizeProperty();
    if (sizeProperty != null) {
      properties.add(sizeProperty);
    }
    return properties;
  }

  protected Property getSizeProperty() {
    return m_sizeSupport.getSizeProperty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<Object> m_liveProcessHiddenWidgets;
  private Rectangle m_absoluteBounds;
  protected Insets m_margins;
  protected Insets m_paddings;
  protected Insets m_borders;

  @Override
  public void refresh_beforeCreate() throws Exception {
    call_Impl_enter();
    super.refresh_beforeCreate();
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    if (isRoot()) {
      UIObjectUtils utils = getUIObjectUtils();
      Object liveWidget = utils.getLiveWidget();
      if (liveWidget != null) {
        m_liveProcessHiddenWidgets = utils.hideRootPanelWidgets(liveWidget);
      }
    }
  }

  @Override
  protected void refresh_afterCreate2() throws Exception {
    super.refresh_afterCreate2();
    call_Impl_exit();
  }

  @Override
  protected void refresh_finish() throws Exception {
    super.refresh_finish();
    if (isRoot()) {
      if (m_liveProcessHiddenWidgets != null) {
        UIObjectUtils.showWidgets(m_liveProcessHiddenWidgets);
        m_liveProcessHiddenWidgets = null;
      }
    }
  }

  @Override
  public void refresh_dispose() throws Exception {
    // GWT does not allow any operation after disposing GWTState
    if (getState().isDisposed()) {
      return;
    }
    // dispose children
    super.refresh_dispose();
    // clear RootPanel
    if (isRoot()) {
      disposeRoot();
    }
  }

  protected void disposeRoot() throws Exception {
    getUIObjectUtils().clearRootPanel();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Impl life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Call enter() as if this UI was create as result of call from JavaScript.
   */
  private void call_Impl_enter() {
    if (isRoot()) {
      try {
        ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
        Class<?> class_Impl = classLoader.loadClass("com.google.gwt.core.client.impl.Impl");
        ReflectionUtils.invokeMethod(class_Impl, "enter()");
      } catch (Throwable e) {
      }
    }
  }

  /**
   * Call exit() as if this UI was create as result of call from JavaScript.
   */
  private void call_Impl_exit() {
    if (isRoot()) {
      try {
        ClassLoader classLoader = JavaInfoUtils.getClassLoader(this);
        Class<?> class_Impl = classLoader.loadClass("com.google.gwt.core.client.impl.Impl");
        ReflectionUtils.invokeMethod(class_Impl, "exit(boolean)", true);
      } catch (Throwable e) {
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh: fetch
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    Object element = getElement();
    // check for messages loop
    if (isRoot()) {
      if (requiresMessagesLoop()) {
        for (int i = 0; i < 5; i++) {
          getState().runMessagesLoop();
          Thread.sleep(1);
        }
      }
    }
    // prepare bounds
    fetchBounds(element);
    // prepare image
    if (isRoot()) {
      fetchImage(element);
    }
    // prepare parent-relative bounds
    {
      Rectangle parentBounds = new Rectangle(m_absoluteBounds);
      if (getParent() instanceof UIObjectInfo) {
        UIObjectInfo parent = (UIObjectInfo) getParent();
        parentBounds.x -= parent.m_absoluteBounds.x;
        parentBounds.y -= parent.m_absoluteBounds.y;
      } else {
        parentBounds.x = parentBounds.y = 0;
      }
      setBounds(parentBounds);
    }
    // borders/margins/paddings
    fetchSpacing();
    // debug output
    /*{
    	System.out.println(this);
    	System.out.println("\tabsolute bounds: " + m_absoluteBounds);
    	System.out.println("\tmodel bounds: " + getModelBounds());
    	System.out.println("\tparent bounds: " + getBounds());
    	System.out.println("\tclientAreaInsets: " + getClientAreaInsets());
    	System.out.println("\tmargins: " + m_margins);
    	System.out.println("\tborders: " + m_borders);
    	System.out.println("\tpaddings: " + m_paddings);
    }*/
    // continue for children
    super.refresh_fetch();
  }

  protected void fetchBounds(Object element) throws Exception {
    m_absoluteBounds = fetchAbsoluteBounds(element);
    {
      Rectangle bounds = m_absoluteBounds.getCopy();
      if (getParent() instanceof UIObjectInfo) {
        UIObjectInfo parent = (UIObjectInfo) getParent();
        bounds.translate(parent.m_absoluteBounds.getLocation().getNegated());
        bounds.translate(parent.getClientAreaInsets().getNegated());
      }
      setModelBounds(bounds);
    }
  }

  protected Rectangle fetchModelBounds(Object element) {
    Rectangle modelBounds = getState().getModelBounds(element);
    applyRectangleLimitation(modelBounds);
    return modelBounds;
  }

  protected Rectangle fetchAbsoluteBounds(Object element) {
    Rectangle absoluteBounds = getState().getAbsoluteBounds(element);
    // This appears to be a bug in GWT [Linux]: DOMImplMozilla.getAbsoluteLeft() returns 
    // negative value if the top element has a border (equals to border size). 
    if (isRoot()) {
      if (absoluteBounds.x < 0) {
        absoluteBounds.x = 0;
      }
      if (absoluteBounds.y < 0) {
        absoluteBounds.y = 0;
      }
    }
    applyRectangleLimitation(absoluteBounds);
    return absoluteBounds;
  }

  /**
   * Too big rectangle can cause Eclipse lock up.
   * <p>
   * http://code.google.com/p/google-web-toolkit/issues/detail?id=6009
   */
  private static void applyRectangleLimitation(Rectangle r) {
    r.width = Math.min(r.width, 3072);
    r.height = Math.min(r.height, 3072);
  }

  protected void fetchImage(Object element) throws Exception {
    GwtState state = getState();
    Image browserScreenshot = state.createBrowserScreenshot();
    // prepare bounds of image
    Rectangle imageBounds = m_absoluteBounds;
    if (state.isStrictMode() && state.isBrowserExplorer()) {
      imageBounds = imageBounds.getTranslated(2, 2);
    }
    // set image
    Image objectImage = UiUtils.getCroppedImage(browserScreenshot, imageBounds.getSwtRectangle());
    setImage(objectImage);
  }

  /**
   * @return <code>true</code> if this {@link UIObjectInfo} or one of its children require running
   *         messages loop before {@link #refresh_fetch()}, to finalize rendering.
   */
  private boolean requiresMessagesLoop() {
    for (UIObjectInfo child : getChildren(UIObjectInfo.class)) {
      if (child.requiresMessagesLoop()) {
        return true;
      }
    }
    {
      String script = JavaInfoUtils.getParameter(this, "GWT.requiresMessagesLoop");
      return getUIObjectUtils().evaluateScriptBoolean(script);
    }
  }

  /**
   * Fetch spacing of UI Object.
   */
  protected void fetchSpacing() throws Exception {
    GwtState state = getState();
    Object element = getElement();
    m_margins = state.getMargins(element);
    m_paddings = state.getPaddings(element);
    m_borders = state.getBorders(element);
    // prepare "clientAreaInsets"
    if (!getState().isBody(element)) {
      fetchClientAreaInsets();
    }
  }

  /**
   * Fetches "client area insets". Usually "margins + borders", but complex components may require
   * special handling.
   */
  protected void fetchClientAreaInsets() throws Exception {
    Insets insets = new Insets();
    insets.add(m_margins);
    insets.add(m_borders);
    setClientAreaInsets(insets);
  }

  /**
   * Transforms given absolute {@link Translatable} into this {@link UIObjectInfo} relative.
   */
  public final void absoluteToRelative(Translatable translatable) {
    translatable.translate(-m_absoluteBounds.x, -m_absoluteBounds.y);
  }

  /**
   * Transforms given absolute {@link Translatable} into this {@link UIObjectInfo} model.
   */
  public final void absoluteToModel(Translatable translatable) {
    absoluteToRelative(translatable);
    {
      Insets clientAreaInsets = getClientAreaInsets();
      translatable.translate(-clientAreaInsets.left, -clientAreaInsets.top);
    }
  }
}
