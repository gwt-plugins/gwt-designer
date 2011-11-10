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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.menu.MenuInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.ASTNode;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Helper for ensuring {@link LayoutDataInfo} child for managed {@link WidgetInfo} on container.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public abstract class LayoutDataSupport {
  private final JavaInfo m_host;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutDataSupport(JavaInfo host) {
    m_host = host;
    m_host.addBroadcastListener(new ObjectInfoTreeComplete() {
      public void invoke() throws Exception {
        LayoutContainerInfo container = getContainer();
        // create virtual LayoutData
        if (isActiveOnContainer(container)) {
          ensureLayoutDatas();
        }
      }
    });
    m_host.addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        // delete this Layout
        if (child == m_host) {
          onDelete();
        }
        // delete Widget from this container
        if (child instanceof WidgetInfo && isActiveOnContainer(parent)) {
          onWidgetRemoveBefore((WidgetInfo) child);
        }
      }

      @Override
      public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
        // delete LayoutData - create virtual
        if (child instanceof LayoutDataInfo && parent instanceof WidgetInfo) {
          WidgetInfo widget = (WidgetInfo) parent;
          if (isActiveOnContainer(widget.getParent()) && shouldCreateLayoutData(widget)) {
            ensureLayoutData(widget);
          }
        }
        // delete Widget_Info from this container
        if (child instanceof WidgetInfo && isActiveOnContainer(parent)) {
          WidgetInfo widget = (WidgetInfo) child;
          if (widget.isDeleted()) {
            onWidgetRemoveAfter(widget);
          }
        }
      }
    });
    m_host.addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        // this Layout added, create virtual LayoutData
        if (child == m_host) {
          onSet();
        }
        // new Widget added, create virtual LayoutData
        if (child instanceof WidgetInfo && isActiveOnContainer(parent)) {
          WidgetInfo widget = (WidgetInfo) child;
          if (isManaged0(widget)) {
            onWidgetAddAfter(widget);
          }
        }
      }

      @Override
      public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
          throws Exception {
        // move Widget FROM this container
        if (child instanceof WidgetInfo && isActiveOnContainer(oldParent) && newParent != oldParent) {
          WidgetInfo widget = (WidgetInfo) child;
          onWidgetRemoveBefore(widget);
          deleteLayoutData(widget);
        }
      }

      @Override
      public void moveAfter(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
          throws Exception {
        // move Widget FROM this container
        if (child instanceof WidgetInfo && isActiveOnContainer(oldParent) && newParent != oldParent) {
          WidgetInfo widget = (WidgetInfo) child;
          onWidgetRemoveAfter(widget);
        }
        // move Widget TO this container
        if (child instanceof WidgetInfo && isActiveOnContainer(newParent) && newParent != oldParent) {
          WidgetInfo widget = (WidgetInfo) child;
          onWidgetAddAfter(widget);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link LayoutContainerInfo} that contains this {@link LayoutDataSupport}.
   */
  public abstract LayoutContainerInfo getContainer();

  /**
   * @return <code>true</code> if this {@link LayoutDataSupport} is active.
   */
  protected abstract boolean isActiveOnContainer(ObjectInfo container);

  /**
   * @return the {@link WidgetInfo} that are managed by this {@link LayoutDataSupport}. This
   *         excludes for example indirectly exposed {@link WidgetInfo}'s.
   */
  public final List<WidgetInfo> getWidgets() {
    List<WidgetInfo> widgets = Lists.newArrayList();
    for (WidgetInfo widget : getContainer().getWidgets()) {
      if (isManaged0(widget)) {
        widgets.add(widget);
      }
    }
    return widgets;
  }

  /**
   * @return <code>true</code> if given {@link WidgetInfo} is managed by this
   *         {@link LayoutDataSupport}. This excludes for example indirectly exposed
   *         {@link WidgetInfo}'s.
   */
  protected boolean isManaged0(WidgetInfo widget) {
    if (widget instanceof MenuInfo) {
      return false;
    }
    if (JavaInfoUtils.isIndirectlyExposed(widget)) {
      return false;
    }
    // right now support only "add()" method
    if (widget.getAssociation() instanceof InvocationChildAssociation) {
      InvocationChildAssociation association = (InvocationChildAssociation) widget.getAssociation();
      if (association.getDescription().getName().equals("add")) {
        return true;
      }
      return false;
    }
    // may be exposed
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout notifications
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method is invoked when this {@link LayoutDataSupport} is set on its
   * {@link LayoutContainerInfo}.
   */
  protected void onSet() throws Exception {
    ensureLayoutDatas();
  }

  /**
   * This method is invoked when this {@link LayoutDataSupport} is deleted from its
   * {@link LayoutContainerInfo}.
   */
  protected void onDelete() throws Exception {
    for (WidgetInfo widget : getWidgets()) {
      deleteLayoutData(widget);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Widget notifications
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Notification that given {@link WidgetInfo} was added to container: new widget or move from
   * other container.
   */
  protected void onWidgetAddAfter(WidgetInfo widget) throws Exception {
    ensureLayoutData(widget);
  }

  /**
   * Notification that given {@link WidgetInfo} will be removed from container.
   */
  protected void onWidgetRemoveBefore(WidgetInfo widget) throws Exception {
  }

  /**
   * Notification that given {@link WidgetInfo} was removed from container.
   */
  protected void onWidgetRemoveAfter(WidgetInfo widget) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutData management
  //
  ////////////////////////////////////////////////////////////////////////////
  private static String KEY_DONT_CREATE_VIRTUAL_DATA =
      "don't create virtual LayoutData for this Widget_Info";

  /**
   * We may be {@link WidgetInfo} that virtual {@link LayoutDataInfo} should not be created for it,
   * when we intentionally delete {@link LayoutDataInfo}, for example during process of moving this
   * {@link WidgetInfo} from this {@link LayoutDataSupport} or deleting this
   * {@link LayoutDataSupport}.
   * 
   * @return <code>true</code> if for given {@link WidgetInfo} we should create
   *         {@link LayoutDataInfo}.
   */
  private boolean shouldCreateLayoutData(WidgetInfo widget) {
    return widget.getArbitraryValue(KEY_DONT_CREATE_VIRTUAL_DATA) == null;
  }

  /**
   * Delete {@link LayoutDataInfo} associated with given {@link WidgetInfo}.
   * <p>
   * Note that this is different than {@link LayoutDataInfo#delete()} because we don't remove
   * implicit/virtual {@link LayoutDataInfo} from list of children in
   * {@link CreationSupport#delete()}. {@link CreationSupport#delete()} has to remove only
   * {@link ASTNode}'s related with {@link LayoutDataInfo}. So, we need separate operation to remove
   * {@link LayoutDataInfo} from list of children.
   */
  protected void deleteLayoutData(WidgetInfo widget) throws Exception {
    widget.putArbitraryValue(KEY_DONT_CREATE_VIRTUAL_DATA, Boolean.TRUE);
    try {
      LayoutDataInfo layoutData = getLayoutData(widget);
      if (layoutData != null) {
        layoutData.delete();
        // if implicit/virtual, so still alive, force remove from children
        if (!layoutData.isDeleted()) {
          widget.removeChild(layoutData);
        }
      }
    } finally {
      widget.removeArbitraryValue(KEY_DONT_CREATE_VIRTUAL_DATA);
    }
  }

  /**
   * @return {@link LayoutDataInfo} associated with given {@link WidgetInfo}, or <code>null</code>
   *         if no {@link LayoutDataInfo} expected for this {@link LayoutDataSupport}.
   */
  public static LayoutDataInfo getLayoutData(WidgetInfo widget) {
    for (ObjectInfo object : widget.getChildren()) {
      if (object instanceof LayoutDataInfo) {
        return (LayoutDataInfo) object;
      }
    }
    return null;
  }

  /**
   * Ensures {@link LayoutDataInfo} for managed {@link WidgetInfo}.
   */
  private void ensureLayoutDatas() throws Exception {
    for (WidgetInfo widget : getWidgets()) {
      ensureLayoutData(widget);
    }
  }

  /**
   * Ensure that if {@link LayoutDataInfo} should exist for given component, there is "real"
   * {@link LayoutDataInfo}, or create "virtual"/"implicit" {@link LayoutDataInfo}.
   */
  private void ensureLayoutData(WidgetInfo widget) throws Exception {
    if (hasLayoutData()) {
      LayoutDataInfo layoutData = getLayoutData(widget);
      if (layoutData == null) {
        createVirtualLayoutData(widget);
      }
    }
  }

  /**
   * Creates virtual {@link LayoutDataInfo} for given {@link WidgetInfo}.
   * <p>
   * "Virtual" {@link LayoutDataInfo} is placeholder for "layout data" when "layout data" should
   * exist, but does not exist yet in source code. Most layout managers in this case use
   * "layout data" with some default values. So, we show these values in properties and allow to
   * change them, at this moment we "materialize" {@link LayoutDataInfo} in source code.
   */
  private void createVirtualLayoutData(WidgetInfo widget) throws Exception {
    Object dataObject = getDefaultVirtualDataObject();
    // create model
    JavaInfo layoutData;
    {
      AstEditor editor = getContainer().getEditor();
      CreationSupport creationSupport = new VirtualLayoutDataCreationSupport(widget, dataObject);
      layoutData = JavaInfoUtils.createJavaInfo(editor, getLayoutDataClass(), creationSupport);
    }
    // configure
    layoutData.setVariableSupport(new VirtualLayoutDataVariableSupport(layoutData));
    layoutData.setAssociation(new EmptyAssociation());
    // add to widget
    widget.addChild(layoutData);
  }

  /**
   * @return default object used for "virtual" {@link LayoutDataInfo}.
   */
  protected final Object getDefaultVirtualDataObject() throws Exception {
    String script = JavaInfoUtils.getParameter(m_host, "layout-data.virtual");
    Assert.isNotNull2(
        script,
        "No 'layout-data.virtual' script for creating virtual LayoutData object. {0}",
        m_host);
    script = StringUtils.replace(script, "%LDC%", getLayoutDataClass().getName());
    return ScriptUtils.evaluate(JavaInfoUtils.getClassLoader(m_host), script);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Virtual Layout Data Support
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_LAYOUT_DATA_HAS = "layout-data.has";
  private static final String KEY_LAYOUT_DATA_CLASS = "layout-data.class";
  private Class<?> m_layoutDataClass;

  /**
   * @return <code>true</code> if this layout has layout data, for user.
   */
  private boolean hasLayoutData() {
    return JavaInfoUtils.hasTrueParameter(m_host, KEY_LAYOUT_DATA_HAS);
  }

  /**
   * @return {@link Class} of layout data objects.
   */
  private Class<?> getLayoutDataClass() throws Exception {
    if (m_layoutDataClass == null) {
      // extract class name
      String layoutDataClassName = JavaInfoUtils.getParameter(m_host, KEY_LAYOUT_DATA_CLASS);
      Assert.isTrue2(
          !StringUtils.isEmpty(layoutDataClassName),
          "No 'layout-data.class' parameter for {0}.",
          m_host);
      // load class
      m_layoutDataClass = JavaInfoUtils.getClassLoader(m_host).loadClass(layoutDataClassName);
    }
    return m_layoutDataClass;
  }
}