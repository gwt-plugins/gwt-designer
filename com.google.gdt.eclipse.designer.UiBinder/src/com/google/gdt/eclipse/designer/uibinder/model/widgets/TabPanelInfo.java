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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.model.widgets.panels.AbstractWidgetHandle;
import com.google.gdt.eclipse.designer.model.widgets.panels.IFlowLikePanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.DOMUtils;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectClipboardCopy;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import java.util.List;
import java.util.Map;

/**
 * Model for GWT <code>com.google.gwt.user.client.ui.TabPanel</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
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
  public TabPanelInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    contributeTabTextProperty();
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
    prepareWidgetHandles();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE2(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
    XmlObjectUtils.flowContainerCreate(this, component, nextComponent);
  }

  public void command_MOVE2(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
    XmlObjectUtils.flowContainerMove(this, component, nextComponent);
  }

  public void command_TARGET_after(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
    showWidget(component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TabText property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contributes "TabText" property to each {@link WidgetInfo} child.
   */
  private void contributeTabTextProperty() {
    addBroadcastListener(new XmlObjectAddProperties() {
      public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
        if (object instanceof WidgetInfo && getChildren().contains(object)) {
          Property property = getTabTextProperty(object);
          properties.add(property);
        }
      }
    });
  }

  /**
   * @return the existing or new "TabText" property.
   */
  private Property getTabTextProperty(final XmlObjectInfo widget) throws Exception {
    Property property = (Property) widget.getArbitraryValue(this);
    if (property == null) {
      final DocumentElement tabElement = widget.getElement().getParent();
      property = new XmlProperty(widget, "TabText", StringPropertyEditor.INSTANCE) {
        @Override
        public boolean isModified() throws Exception {
          return getValue() != UNKNOWN_VALUE;
        }

        @Override
        public Object getValue() throws Exception {
          String attributeValue = tabElement.getAttribute("text");
          return attributeValue != null ? attributeValue : UNKNOWN_VALUE;
        }

        @Override
        protected void setValueEx(Object value) throws Exception {
          if (value instanceof String) {
            tabElement.setAttribute("text", (String) value);
          }
        }
      };
      property.setCategory(PropertyCategory.system(7));
    }
    return property;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addClipboardSupport() {
    addBroadcastListener(new XmlObjectClipboardCopy() {
      public void invoke(XmlObjectInfo object, List<ClipboardCommand> commands) throws Exception {
        if (object == TabPanelInfo.this) {
          for (WidgetInfo widget : getChildrenWidgets()) {
            addWidgetCommand(commands, widget);
          }
        }
      }

      private void addWidgetCommand(List<ClipboardCommand> commands, WidgetInfo widget)
          throws Exception {
        final String tabText = (String) getTabTextProperty(widget).getValue();
        commands.add(new PanelClipboardCommand<TabPanelInfo>(widget) {
          private static final long serialVersionUID = 0L;

          @Override
          protected void add(TabPanelInfo panel, WidgetInfo widget) throws Exception {
            panel.command_CREATE2(widget, null);
            panel.getTabTextProperty(widget).setValue(tabText);
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
