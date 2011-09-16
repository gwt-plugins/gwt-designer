/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.model.widgets.panels;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.model.widgets.CompositeInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.DOMUtils;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Model for GWT <code>com.google.gwt.user.client.ui.TabPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class TabPanelInfo extends CompositeInfo implements IFlowLikePanelInfo<WidgetInfo> {
  private final StackContainerSupport<WidgetInfo> m_stackContainer =
      new StackContainerSupport<WidgetInfo>(this) {
        @Override
        protected List<WidgetInfo> getChildren() {
          return getChildrenWidgets();
        }
      };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TabPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addClipboardSupport();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link WidgetInfo} children.
   */
  public List<WidgetInfo> getChildrenWidgets() {
    return getChildren(WidgetInfo.class);
  }

  /**
   * Ensures that given {@link WidgetInfo} become visible on design canvas, may performs refresh().
   */
  public void showWidget(WidgetInfo widget) {
    m_stackContainer.setActive(widget);
  }

  /**
   * @return the active (visible) {@link WidgetInfo}.
   */
  public WidgetInfo getActiveWidget() {
    return m_stackContainer.getActive();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    // may be placeholder
    if (isPlaceholder()) {
      return;
    }
    // OK, real TabPanel
    showActiveWidget();
  }

  private void showActiveWidget() throws Exception {
    WidgetInfo active = m_stackContainer.getActive();
    if (active != null) {
      int index =
          (Integer) ReflectionUtils.invokeMethod(
              getObject(),
              "getWidgetIndex(com.google.gwt.user.client.ui.Widget)",
              active.getObject());
      ReflectionUtils.invokeMethod(getObject(), "selectTab(int)", index);
    }
  }

  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    // may be placeholder
    if (isPlaceholder()) {
      return;
    }
    // OK, real TabPanel
    prepareWidgetHandles();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE2(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
    ComplexPanelInfo.command_CREATE2(this, component, nextComponent);
    showWidget(component);
  }

  public void command_MOVE2(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
    ComplexPanelInfo.command_MOVE2(this, component, nextComponent);
    showWidget(component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addClipboardSupport() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
          throws Exception {
        if (javaInfo == TabPanelInfo.this) {
          for (WidgetInfo widget : getChildrenWidgets()) {
            addWidgetCommand(commands, widget);
          }
        }
      }

      private void addWidgetCommand(List<ClipboardCommand> commands, WidgetInfo widget)
          throws Exception {
        final String tabText = (String) PropertyUtils.getByPath(widget, "TabText").getValue();
        commands.add(new PanelClipboardCommand<TabPanelInfo>(widget) {
          private static final long serialVersionUID = 0L;

          @Override
          protected void add(TabPanelInfo panel, WidgetInfo widget) throws Exception {
            panel.command_CREATE2(widget, null);
            PropertyUtils.getByPath(widget, "TabText").setValue(tabText);
          }
        });
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Header
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Object that describes header for widgets on {@link TabPanelInfo}.
   */
  public class WidgetHandle extends AbstractWidgetHandle<WidgetInfo> {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public WidgetHandle(WidgetInfo widget) {
      super(widget);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public Rectangle getBounds() {
      return m_widgetToHandleBounds.get(m_widget);
    }

    @Override
    public void show() {
      showWidget(m_widget);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WidgetHandle objects
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<WidgetHandle> m_widgetHandles = Lists.newArrayList();
  private final Map<WidgetInfo, Rectangle> m_widgetToHandleBounds = Maps.newHashMap();

  /**
   * Return list of all {@link WidgetHandle}'s.
   */
  public List<WidgetHandle> getWidgetHandles() {
    return m_widgetHandles;
  }

  /**
   * Fills {@link #m_widgetHandles}.
   */
  private void prepareWidgetHandles() throws Exception {
    GwtState state = getState();
    DOMUtils dom = state.getDomUtils();
    m_widgetHandles.clear();
    m_widgetToHandleBounds.clear();
    // prepare table row with tabs
    Object tabBarRowElement;
    {
      Object panelObject = getObject();
      Object tabBarWidget = ReflectionUtils.invokeMethod(panelObject, "getTabBar()");
      Object tabBarElement = state.getUIObjectUtils().getElement(tabBarWidget);
      Object tabBarBodyElement = dom.getChild(tabBarElement, 0);
      tabBarRowElement = dom.getChild(tabBarBodyElement, 0);
    }
    // fetch tabs bounds for each widget
    List<WidgetInfo> widgets = getChildrenWidgets();
    for (int index = 0; index < widgets.size(); index++) {
      WidgetInfo widget = widgets.get(index);
      // use "1 +" because first TD is "gwt-TabBarFirst"
      Object tabElement = dom.getChild(tabBarRowElement, 1 + index);
      tabElement = dom.getChild(tabElement, 0);
      Rectangle tabBounds = state.getAbsoluteBounds(tabElement);
      absoluteToRelative(tabBounds);
      // remember tab and bounds
      m_widgetHandles.add(new WidgetHandle(widget));
      m_widgetToHandleBounds.put(widget, tabBounds);
    }
  }
}
