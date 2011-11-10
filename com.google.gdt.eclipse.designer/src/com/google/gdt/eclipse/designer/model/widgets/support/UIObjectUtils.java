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
package com.google.gdt.eclipse.designer.model.widgets.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils.getConstructor;
import static org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils.invokeMethod;
import static org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils.invokeMethodEx;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utilities for manipulation of GWT <code>UIObject</code> and <code>Widget</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class UIObjectUtils {
  private final GwtState state;
  private final DOMUtils dom;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UIObjectUtils(GwtState state) {
    this.state = state;
    dom = state.getDomUtils();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void setSize(Object object, String width, String height) throws Exception {
    invokeMethod(object, "setSize(java.lang.String,java.lang.String)", width, height);
  }

  public Object createLabel() throws Exception {
    Class<?> class_Label = loadClass("com.google.gwt.user.client.ui.Label");
    return class_Label.newInstance();
  }

  public Object createLabel(String text) throws Exception {
    Class<?> class_Label = loadClass("com.google.gwt.user.client.ui.Label");
    return getConstructor(class_Label, String.class).newInstance(text);
  }

  public Object getElement(Object widget) {
    return invokeMethodEx(widget, "getElement()");
  }

  public Object getRootPanel() {
    Class<?> class_RootPanel = loadClass("com.google.gwt.user.client.ui.RootPanel");
    return invokeMethodEx(class_RootPanel, "get()");
  }

  public Object getRootLayoutPanel() throws Exception {
    Class<?> class_RootLayoutPanel = loadClass("com.google.gwt.user.client.ui.RootLayoutPanel");
    return invokeMethod(class_RootLayoutPanel, "get()");
  }

  public Object getRootPanelElement() throws Exception {
    Object rootPanel = getRootPanel();
    return getElement(rootPanel);
  }

  public void addToRootPanel(Object widget) throws Exception {
    invokeMethod(getRootPanel(), "add(com.google.gwt.user.client.ui.Widget)", widget);
  }

  public void add(Object hasWidgets, Object widget) throws Exception {
    invokeMethod(hasWidgets, "add(com.google.gwt.user.client.ui.Widget)", widget);
  }

  public static void remove(Object hasWidgets, Object widget) throws Exception {
    invokeMethod(hasWidgets, "remove(com.google.gwt.user.client.ui.Widget)", widget);
  }

  /**
   * Removes all widgets from <code>RootPanel</code>. If <code>RootPanel</code> marked with
   * <code>"__wbp_liveRootPanel"</code> title, then only "live" <code>Widget</code> will be removed.
   * In other case <code>Panel.clear()</code> will be user to remove all widgets.
   */
  public void clearRootPanel() throws Exception {
    // try to find "live" widget and remove only it
    {
      Object liveWidget = getLiveWidget();
      if (liveWidget != null) {
        removeWidgetFromPanel(liveWidget);
        return;
      }
    }
    // clear RootLayoutPanel instance, so force its re-initialization
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        Class<?> class_RootLayoutPanel = loadClass("com.google.gwt.user.client.ui.RootLayoutPanel");
        ReflectionUtils.setField(class_RootLayoutPanel, "singleton", null);
      }
    });
    // we removed "hiddenDiv", so clear it in "UiBinderUtil"
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        Class<?> clazz = loadClass("com.google.gwt.uibinder.client.UiBinderUtil");
        ReflectionUtils.setField(clazz, "hiddenDiv", null);
      }
    });
    // we removed "hiddenDiv", so clear it in "HTMLPanel"
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        Class<?> clazz = loadClass("com.google.gwt.user.client.ui.HTMLPanel");
        ReflectionUtils.setField(clazz, "hiddenDiv", null);
      }
    });
    // "normal" path - remove all elements
    Object rootPanel = getRootPanel();
    if (!m_liveManager) {
      // first step: clear() to remove widgets
      clearComplexPanel(rootPanel);
      // second step: remove all survived elements
      Object rootPanelElement = getElement(rootPanel);
      removeBodyChildren(rootPanelElement);
      removeBodyChildren(getRootPanelElement());
    }
  }

  /**
   * This is hang safe implementation of <code>HasWidgets.clear()</code>.
   */
  private static void clearComplexPanel(Object panel) throws Exception {
    // remove each child Widget, ignore exceptions
    for (Object widget : (List<?>) getPanelWidgets(panel)) {
      try {
        remove(panel, widget);
      } catch (Throwable e) {
      }
    }
    // in any case clear WidgetCollection
    {
      Object collection = invokeMethod(panel, "getChildren()");
      ReflectionUtils.setField(collection, "size", 0);
    }
  }

  /**
   * Removes all children Elements from given Body element. Does not remove history frame.
   */
  private void removeBodyChildren(Object body) throws Exception {
    Object[] chilren = dom.getChildren(body);
    for (Object child : chilren) {
      if (isWaitDivCSS(child) || isHistoryFrame(child) || isLayoutDiv(child)) {
        continue;
      }
      dom.removeChild(body, child);
    }
  }

  /**
   * @return <code>true</code> if given Element is CSS wait DIV.
   */
  public boolean isWaitDivCSS(Object element) {
    if (dom.getTagName(element).equals("DIV")) {
      String className = dom.getClassName(element);
      return StringUtils.startsWith(className, "wbp__wait_stylesheet");
    }
    return false;
  }

  /**
   * @return <code>true</code> if given Element is history frame.
   */
  public boolean isHistoryFrame(Object element) {
    String id = dom.getId(element);
    return "__gwt_historyFrame".equals(id);
  }

  /**
   * @return <code>true</code> if given Element is "Layout" support DIV, used for calculating sizes
   *         of CM, MM, IN, etc units.
   */
  private boolean isLayoutDiv(final Object element) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        Class<?> classLayout = loadClass("com.google.gwt.layout.client.LayoutImpl");
        return element == ReflectionUtils.getFieldObject(classLayout, "fixedRuler");
      }
    }, false);
  }

  /**
   * Sets "visible" property of all (except given) direct children of <code>RootPanel</code> to
   * <code>false</code>.
   */
  public List<Object> hideRootPanelWidgets(Object excludeWidget) throws Exception {
    List<Object> widgetsToShow = Lists.newArrayList();
    for (Object widget : getRootPanelWidgets()) {
      if (widget != excludeWidget && isObjectVisible(widget)) {
        widgetsToShow.add(widget);
        setObjectVisible(widget, false);
      }
    }
    return widgetsToShow;
  }

  /**
   * Sets "visible" property of given widgets to <code>true</code>.
   */
  public static void showWidgets(List<Object> widgetsToShow) throws Exception {
    for (Object widget : widgetsToShow) {
      setObjectVisible(widget, true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Live support
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_liveManager;
  private Object m_liveWidget;

  /**
   * Specifies if "live" manager is now active.
   */
  public void setLiveManager(boolean liveManager) {
    m_liveManager = liveManager;
  }

  /**
   * @return the <code>Widget</code> that is currently in process of creating and temporary added to
   *         <code>RootPanel</code>.
   */
  public Object getLiveWidget() throws Exception {
    for (Object widget : getRootPanelWidgets()) {
      if (widget == m_liveWidget || "__wbp_liveWidget".equals(getObjectTitle(widget))) {
        return widget;
      }
    }
    return m_liveWidget = null;
  }

  public void setLiveWidget(Object widget) {
    m_liveWidget = widget;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Low level
  //
  ////////////////////////////////////////////////////////////////////////////
  private Class<?> loadClass(final String name) {
    return ExecutionUtils.runObject(new RunnableObjectEx<Class<?>>() {
      public Class<?> runObject() throws Exception {
        return state.getClassLoader().loadClass(name);
      }
    });
  }

  public Class<?> getClassOfDOM() {
    return loadClass("com.google.gwt.user.client.DOM");
  }

  public Class<?> getClassOfWindow() {
    return loadClass("com.google.gwt.user.client.Window");
  }

  public Class<?> getClassOfElement() {
    return loadClass("com.google.gwt.user.client.Element");
  }

  public Object getRootPanelWidgetCollection() throws Exception {
    Object rootPanel = getRootPanel();
    return ReflectionUtils.invokeMethod(rootPanel, "getChildren()");
  }

  public List<?> getRootPanelWidgets() throws Exception {
    Object rootPanel = getRootPanel();
    return getPanelWidgets(rootPanel);
  }

  public static List<?> getPanelWidgets(Object panel) throws Exception {
    Iterator<?> iterator = (Iterator<?>) invokeMethod(panel, "iterator()");
    return Lists.newArrayList(iterator);
  }

  /**
   * Removes <code>Widget</code> from its parent <code>Panel</code>. If exception happens (for
   * example in <code>onDetach()</code>), it still will be removed from <code>Panel</code> children.
   */
  private void removeWidgetFromPanel(Object widget) {
    Object parent = getParent(widget);
    try {
      remove(parent, widget);
    } catch (Throwable e) {
      try {
        Object children = invokeMethod(parent, "getChildren()");
        invokeMethod(children, "remove(com.google.gwt.user.client.ui.Widget)", widget);
      } catch (Throwable e2) {
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Panel
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object getParent(Object widget) {
    return invokeMethodEx(widget, "getParent()");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // UIObject access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the "title" property, or <code>null</code> in case of any exception.
   */
  public static String getObjectTitle(Object o) throws Exception {
    try {
      return (String) invokeMethod(o, "getTitle()");
    } catch (Throwable e) {
      return null;
    }
  }

  /**
   * @return <code>true</code> if given <code>Widget</code> has parent.
   */
  public static boolean hasParent(Object widget) throws Exception {
    return invokeMethod(widget, "getParent()") != null;
  }

  /**
   * @return <code>true</code> if given <code>Widget</code> is attached.
   */
  public static boolean isAttached(Object widget) throws Exception {
    return (Boolean) invokeMethod(widget, "isAttached()");
  }

  /**
   * We have to catch exceptions because in GWT-Ext some components may return "undefined" from
   * native <code>isVisible()</code> function.
   */
  public static boolean isObjectVisible(Object o) throws Exception {
    try {
      return (Boolean) invokeMethod(o, "isVisible()");
    } catch (Throwable e) {
      return false;
    }
  }

  public static void setObjectVisible(Object o, boolean visible) throws Exception {
    invokeMethod(o, "setVisible(boolean)", visible);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Script
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Evaluates script which returns "boolean".
   * <p>
   * GWT specific and platform-dependent variables are included, such as "isMacOSX", "isWindows",
   * "isWebKit", etc.
   */
  public boolean evaluateScriptBoolean(String script) {
    // no script
    if (StringUtils.isEmpty(script)) {
      return false;
    }
    // evaluate script
    return (Boolean) executeScript(script);
  }

  /**
   * Evaluates script.
   * <p>
   * GWT specific and platform-dependent variables are provided, such as "isMacOSX", "isWindows",
   * "isWebKit", etc.
   */
  public Object executeScript(String script) {
    return executeScript(script, Maps.<String, Object>newHashMap());
  }

  /**
   * Evaluates script.
   * <p>
   * GWT specific and platform-dependent variables are included, such as "isMacOSX", "isWindows",
   * "isWebKit", etc.
   */
  public Object executeScript(String script, Map<String, Object> _variables) {
    script = "import com.google.gwt.user.client.DOM;\n" + script;
    // prepare variables
    Map<String, Object> variables;
    {
      variables = Maps.newHashMap();
      if (_variables != null) {
        variables.putAll(_variables);
      }
      // widgets
      variables.put("rootPanel", getRootPanel());
      // environment
      variables.put("isWindows", EnvironmentUtils.IS_WINDOWS);
      variables.put("isLinux", EnvironmentUtils.IS_LINUX);
      variables.put("isMacOSX", EnvironmentUtils.IS_MAC);
      variables.put("isStrict", state.isStrictMode());
      variables.put("isExplorer", state.isBrowserExplorer());
      variables.put("isWebKit", state.isBrowserWebKit());
    }
    // execute script
    return ScriptUtils.evaluate(state.getClassLoader(), script, variables);
  }
}
