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
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.DOMUtils;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Model for GWT <code>StackPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class StackPanelInfo extends ComplexPanelInfo implements IStackPanelInfo<WidgetInfo> {
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
  public StackPanelInfo(AstEditor editor,
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
   * Ensures that given {@link WidgetInfo} become visible on design canvas, may performs refresh().
   */
  public void showWidget(WidgetInfo widget) {
    m_stackContainer.setActive(widget);
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
      ReflectionUtils.invokeMethod(getObject(), "showStack(int)", index);
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
  @Override
  public void command_CREATE2(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
    super.command_CREATE2(component, nextComponent);
    showWidget(component);
  }

  @Override
  public void command_MOVE2(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
    super.command_MOVE2(component, nextComponent);
    showWidget(component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
    final Object stackText = getPropertyValue(widget, "Association/stackText");
    final Object asHTML = getPropertyValue(widget, "Association/asHTML");
    commands.add(new PanelClipboardCommand<StackPanelInfo>(widget) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(StackPanelInfo panel, WidgetInfo widget) throws Exception {
        panel.command_CREATE2(widget, null);
        setPropertyValue(widget, "Association/stackText", stackText);
        setPropertyValue(widget, "Association/asHTML", asHTML);
      }

      private void setPropertyValue(WidgetInfo widget, String path, Object value) throws Exception {
        if (value != Property.UNKNOWN_VALUE) {
          Property property = PropertyUtils.getByPath(widget, path);
          property.setValue(value);
        }
      }
    });
  }

  private static Object getPropertyValue(WidgetInfo widget, String path) throws Exception {
    Property property = PropertyUtils.getByPath(widget, path);
    return property != null ? property.getValue() : Property.UNKNOWN_VALUE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Header
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Object that describes header for widgets on {@link StackPanelInfo}.
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
    DOMUtils dom = getDOMUtils();
    m_widgetHandles.clear();
    // prepare Table body
    Object bodyElement;
    {
      Object panelObject = getObject();
      Object tableElement = state.getUIObjectUtils().getElement(panelObject);
      bodyElement = dom.getChild(tableElement, 0);
    }
    // prepare handle object for each widget
    List<WidgetInfo> widgets = getChildrenWidgets();
    for (int index = 0; index < widgets.size(); index++) {
      WidgetInfo widget = widgets.get(index);
      Object headerElement = dom.getChild(bodyElement, 2 * index + 0);
      // prepare bounds
      Rectangle headerBounds = state.getAbsoluteBounds(headerElement);
      absoluteToRelative(headerBounds);
      // add handle object
      m_widgetHandles.add(new WidgetHandle(widget));
      m_widgetToHandleBounds.put(widget, headerBounds);
    }
  }
}
