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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.model.widgets.IUIObjectInfo;
import com.google.gdt.eclipse.designer.model.widgets.IUIObjectSizeSupport;
import com.google.gdt.eclipse.designer.model.widgets.support.DOMUtils;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.model.widgets.support.UIObjectUtils;
import com.google.gdt.eclipse.designer.uibinder.model.util.NameProperty;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderRenderer;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Translatable;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for <code>UiObject</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public class UIObjectInfo extends AbstractComponentInfo implements IUIObjectInfo {
  private final Property m_variableProperty = new NameProperty(this);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UIObjectInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public UiBinderContext getContext() {
    return (UiBinderContext) super.getContext();
  }

  public final GwtState getState() {
    return getContext().getState();
  }

  /**
   * @return the {@link UIObjectUtils} to work with <code>UIObject</code>.
   */
  public final UIObjectUtils getUIObjectUtils() {
    return getState().getUIObjectUtils();
  }

  /**
   * @return the {@link DOMUtils} to work with <code>Element</code>.
   */
  public final DOMUtils getDOM() {
    return getState().getDomUtils();
  }

  @Override
  public Rectangle getAbsoluteBounds() {
    return m_absoluteBounds;
  }

  /**
   * @return the DOM margins.
   */
  public final Insets getMargins() {
    return m_margins;
  }

  /**
   * @return the DOM borders.
   */
  public final Insets getBorders() {
    return m_borders;
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
  public Object getDOMElement() throws Exception {
    return getState().getUIObjectUtils().getElement(getObject());
  }

  /**
   * @return <code>true</code> if this {@link UIObjectInfo} has no visible border, so artificial
   *         border should be drawn.
   */
  @Override
  public boolean shouldDrawDotsBorder() {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        String script = XmlObjectUtils.getParameter(UIObjectInfo.this, "shouldDrawBorder");
        if (!StringUtils.isEmpty(script)) {
          return (Boolean) XmlObjectUtils.executeScript(UIObjectInfo.this, script);
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
  private final UIObjectSizeSupport m_sizeSupport = new UIObjectSizeSupport(this);

  public final IUIObjectSizeSupport getSizeSupport() {
    return m_sizeSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final void saveEdit() throws Exception {
    super.saveEdit();
    getContext().saveFormEditor();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    properties.add(m_sizeSupport.getSizeProperty());
    properties.add(m_variableProperty);
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<Object> m_liveProcessHiddenWidgets;

  @Override
  public void refresh_dispose() throws Exception {
    // GWT does not allow any operation after disposing GWTState
    if (getState().isDisposed()) {
      return;
    }
    // clear RootPanel
    if (isRoot()) {
      getUIObjectUtils().clearRootPanel();
    }
    // dispose children
    super.refresh_dispose();
  }

  @Override
  public void refresh_beforeCreate() throws Exception {
    call_Impl_enter();
    super.refresh_beforeCreate();
  }

  @Override
  protected void refresh_create() throws Exception {
    UiBinderRenderer renderer = new UiBinderRenderer(this);
    renderer.render();
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
      ExecutionUtils.runIgnore(new RunnableEx() {
        public void run() throws Exception {
          ClassLoader classLoader = getContext().getClassLoader();
          Class<?> class_Impl = classLoader.loadClass("com.google.gwt.core.client.impl.Impl");
          ReflectionUtils.invokeMethod(class_Impl, "enter()");
        }
      });
    }
  }

  /**
   * Call exit() as if this UI was create as result of call from JavaScript.
   */
  private void call_Impl_exit() {
    if (isRoot()) {
      ExecutionUtils.runIgnore(new RunnableEx() {
        public void run() throws Exception {
          ClassLoader classLoader = getContext().getClassLoader();
          Class<?> class_Impl = classLoader.loadClass("com.google.gwt.core.client.impl.Impl");
          ReflectionUtils.invokeMethod(class_Impl, "exit(boolean)", true);
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh: fetch
  //
  ////////////////////////////////////////////////////////////////////////////
  private Rectangle m_absoluteBounds;
  protected Insets m_margins;
  protected Insets m_borders;
  protected Insets m_paddings;

  @Override
  protected void refresh_fetch() throws Exception {
    Object element = getDOMElement();
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
      if (getParent() instanceof AbstractComponentInfo) {
        AbstractComponentInfo parent = (AbstractComponentInfo) getParent();
        Rectangle parentAbsoluteBounds = parent.getAbsoluteBounds();
        parentBounds.x -= parentAbsoluteBounds.x;
        parentBounds.y -= parentAbsoluteBounds.y;
      } else {
        parentBounds.x = parentBounds.y = 0;
      }
      setBounds(parentBounds);
    }
    // borders/margins/paddings
    fetchSpacing();
    // continue for children
    super.refresh_fetch();
  }

  protected void fetchBounds(Object element) throws Exception {
    m_absoluteBounds = fetchAbsoluteBounds(element);
    {
      Rectangle bounds = m_absoluteBounds.getCopy();
      if (getParent() instanceof AbstractComponentInfo) {
        AbstractComponentInfo parent = (AbstractComponentInfo) getParent();
        Rectangle parentAbsoluteBounds = parent.getAbsoluteBounds();
        bounds.x -= parentAbsoluteBounds.x;
        bounds.y -= parentAbsoluteBounds.y;
        bounds.translate(parent.getClientAreaInsets().getNegated());
      }
      setModelBounds(bounds);
    }
  }

  protected Rectangle fetchModelBounds(Object element) {
    return getState().getModelBounds(element);
  }

  protected Rectangle fetchAbsoluteBounds(Object element) {
    Rectangle absoluteBounds = getState().getAbsoluteBounds(element);
    // Case 43858: https://fogbugz.instantiations.com/default.php?43858
    if (isRoot()) {
      if (absoluteBounds.x < 0) {
        absoluteBounds.x = 0;
      }
      if (absoluteBounds.y < 0) {
        absoluteBounds.y = 0;
      }
    }
    return absoluteBounds;
  }

  protected void fetchImage(Object element) throws Exception {
    Image browserScreenshot = getState().createBrowserScreenshot();
    Image objectImage =
        UiUtils.getCroppedImage(browserScreenshot, m_absoluteBounds.getSwtRectangle());
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
      String script = XmlObjectUtils.getParameter(this, "GWT.requiresMessagesLoop");
      return getUIObjectUtils().evaluateScriptBoolean(script);
    }
  }

  /**
   * Fetch spacing of UI Object.
   */
  protected void fetchSpacing() throws Exception {
    GwtState state = getState();
    Object element = getDOMElement();
    m_margins = state.getMargins(element);
    m_borders = state.getBorders(element);
    m_paddings = state.getPaddings(element);
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
