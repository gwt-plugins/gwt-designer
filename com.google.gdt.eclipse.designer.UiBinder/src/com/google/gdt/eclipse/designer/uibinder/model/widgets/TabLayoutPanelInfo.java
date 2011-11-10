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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.model.widgets.panels.AbstractWidgetHandle;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;

import java.util.List;
import java.util.Map;

/**
 * Model for <code>com.google.gwt.user.client.ui.TabLayoutPanel</code> in UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class TabLayoutPanelInfo extends ComplexPanelInfo {
  private final Property m_barUnitProperty = new UnitAttributeProperty(this, "barUnit", "barUnit");
  private final Property m_barHeightProperty = new TabLayoutPanelBarHeightProperty(this);
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
  public TabLayoutPanelInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    contributeTabTextProperty();
    tweakCustomHeadersHierarchy();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new RootLayoutPanelTopBoundsSupport(this);
  }

  /**
   * Adds support for "customHeader".
   */
  private void tweakCustomHeadersHierarchy() {
    addBroadcastListener(new ObjectInfoTreeComplete() {
      public void invoke() throws Exception {
        List<?> tabs = (List<?>) ReflectionUtils.getFieldObject(getObject(), "tabs");
        for (WidgetInfo widget : getChildrenWidgets()) {
          int index = getWidgetIndex(widget);
          if (index == -1) {
            moveWidgetToContent(tabs, widget);
          }
        }
      }

      private void moveWidgetToContent(List<?> tabs, WidgetInfo tabWidget) throws Exception {
        for (int i = 0; i < tabs.size(); i++) {
          Object tab = tabs.get(i);
          if (ReflectionUtils.getFieldObject(tab, "widget") == tabWidget.getObject()) {
            Object contentObject = getWidgetObject(i);
            WidgetInfo contentInfo = (WidgetInfo) getChildByObject(contentObject);
            removeChild(tabWidget);
            contentInfo.addChild(tabWidget);
          }
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
   * @return the active (visible) {@link WidgetInfo}.
   */
  public WidgetInfo getActiveWidget() {
    return m_stackContainer.getActive();
  }

  /**
   * Ensures that given {@link WidgetInfo} is shown on this {@link TabLayoutPanelInfo}.
   */
  public void showWidget(WidgetInfo widget) {
    m_stackContainer.setActive(widget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    properties.add(m_barUnitProperty);
    properties.add(m_barHeightProperty);
    return properties;
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
    WidgetInfo active = getActiveWidget();
    if (active != null) {
      ReflectionUtils.invokeMethod(
          getObject(),
          "selectTab(com.google.gwt.user.client.ui.Widget)",
          active.getObject());
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
  public void command_APPEND_after(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
    getTabTextProperty(component).setValue("New tab");
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
  protected void clipboardCopy_addPanelCommands(List<ClipboardCommand> commands) throws Exception {
    super.clipboardCopy_addPanelCommands(commands);
  }

  @Override
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
    final String tabText = (String) PropertyUtils.getByPath(widget, "TabText").getValue();
    commands.add(new PanelClipboardCommand<TabLayoutPanelInfo>(widget) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(TabLayoutPanelInfo panel, WidgetInfo widget) throws Exception {
        panel.command_CREATE2(widget, null);
        PropertyUtils.getByPath(widget, "TabText").setValue(tabText);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Header
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Object that describes header for widgets.
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
    m_widgetHandles.clear();
    //
    List<?> tabs = (List<?>) ReflectionUtils.getFieldObject(getObject(), "tabs");
    for (WidgetInfo widget : getChildrenWidgets()) {
      int index = getWidgetIndex(widget);
      Object tabWidget = tabs.get(index);
      Object tabElement = state.getUIObjectUtils().getElement(tabWidget);
      // prepare bounds
      Rectangle tabBounds = state.getAbsoluteBounds(tabElement);
      absoluteToRelative(tabBounds);
      // add handle object
      m_widgetHandles.add(new WidgetHandle(widget));
      m_widgetToHandleBounds.put(widget, tabBounds);
    }
  }

  /**
   * @return the widget at given index.
   */
  private Object getWidgetObject(int index) throws Exception {
    return ReflectionUtils.invokeMethod(getObject(), "getWidget(int)", index);
  }

  /**
   * @return the index of widget tab.
   */
  private int getWidgetIndex(WidgetInfo widget) throws Exception {
    return (Integer) ReflectionUtils.invokeMethod(
        getObject(),
        "getWidgetIndex(com.google.gwt.user.client.ui.Widget)",
        widget.getObject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Header properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contributes "TabText" property to each {@link WidgetInfo} child.
   */
  private void contributeTabTextProperty() {
    addBroadcastListener(new XmlObjectAddProperties() {
      public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
        if (object instanceof WidgetInfo && getChildren().contains(object)) {
          WidgetInfo widget = (WidgetInfo) object;
          Property textProperty = getTabTextProperty(widget);
          if (textProperty != null) {
            properties.add(textProperty);
          }
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
      final DocumentElement headerElement = getHeaderElement(widget);
      if (headerElement.getTagLocal().equals("header")) {
        property = new XmlProperty(widget, "TabText", StringPropertyEditor.INSTANCE) {
          @Override
          public boolean isModified() throws Exception {
            return getValue() != UNKNOWN_VALUE;
          }

          @Override
          public Object getValue() throws Exception {
            return headerElement.getTextNode().getText();
          }

          @Override
          protected void setValueEx(Object value) throws Exception {
            if (value instanceof String) {
              headerElement.setText((String) value, false);
            }
          }
        };
        property.setCategory(PropertyCategory.system(7));
      }
    }
    return property;
  }

  /**
   * @return the existing or new "header" element of "tab".
   */
  private static DocumentElement getHeaderElement(XmlObjectInfo widget) {
    DocumentElement widgetElement = widget.getElement();
    DocumentElement tabElement = widgetElement.getParent();
    int widgetIndex = tabElement.indexOf(widgetElement);
    if (widgetIndex == 0) {
      String tag = tabElement.getTagNS() + "header";
      DocumentElement headerElement = new DocumentElement(tag);
      tabElement.addChild(headerElement, 0);
      return headerElement;
    } else {
      return tabElement.getChildAt(0);
    }
  }
}
