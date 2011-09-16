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
import com.google.gdt.eclipse.designer.model.widgets.panels.IStackPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.DOMUtils;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectMove;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;

import java.util.List;
import java.util.Map;

/**
 * Model for <code>com.google.gwt.user.client.ui.StackPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
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
  public StackPanelInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    contributeStackTextProperty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ensures that given {@link WidgetInfo} is shown on this {@link StackPanelInfo}.
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
    DOMUtils dom = state.getDomUtils();
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_APPEND_after(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
    getStackTextProperty(component).setValue("New widget");
  }

  public void command_TARGET_after(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
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
    final Object stackText = getStackTextProperty(widget).getValue();
    commands.add(new PanelClipboardCommand<StackPanelInfo>(widget) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(StackPanelInfo panel, WidgetInfo widget) throws Exception {
        panel.command_CREATE2(widget, null);
        panel.getStackTextProperty(widget).setValue(stackText);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // StackText property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When {@link WidgetInfo} is moved out of its {@link StackPanelInfo}.
   */
  private void removeStackText_whenMoveOut() {
    addBroadcastListener(new XmlObjectMove() {
      @Override
      public void before(XmlObjectInfo child, ObjectInfo oldParent, ObjectInfo newParent)
          throws Exception {
        if (oldParent == StackPanelInfo.this && newParent != oldParent) {
          Property property = getStackTextProperty(child);
          property.setValue(Property.UNKNOWN_VALUE);
        }
      }
    });
  }

  /**
   * Contributes "StackText" property to each {@link WidgetInfo} child.
   */
  private void contributeStackTextProperty() {
    addBroadcastListener(new XmlObjectAddProperties() {
      public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
        if (object instanceof WidgetInfo && getChildren().contains(object)) {
          Property property = getStackTextProperty(object);
          properties.add(property);
        }
      }
    });
    removeStackText_whenMoveOut();
  }

  /**
   * @return the existing or new "StackText" property.
   */
  private Property getStackTextProperty(final XmlObjectInfo widget) throws Exception {
    Property property = (Property) widget.getArbitraryValue(this);
    if (property == null) {
      final String attributeName = getElement().getTagNS() + "StackPanel-text";
      property = new XmlProperty(widget, "StackText", StringPropertyEditor.INSTANCE) {
        @Override
        public boolean isModified() throws Exception {
          return getValue() != UNKNOWN_VALUE;
        }

        @Override
        public Object getValue() throws Exception {
          String attributeValue = widget.getElement().getAttribute(attributeName);
          return attributeValue != null ? attributeValue : UNKNOWN_VALUE;
        }

        @Override
        protected void setValueEx(Object value) throws Exception {
          if (value instanceof String) {
            widget.setAttribute(attributeName, (String) value);
          }
          if (value == UNKNOWN_VALUE) {
            widget.removeAttribute(attributeName);
          }
        }
      };
      property.setCategory(PropertyCategory.system(7));
    }
    return property;
  }
}
