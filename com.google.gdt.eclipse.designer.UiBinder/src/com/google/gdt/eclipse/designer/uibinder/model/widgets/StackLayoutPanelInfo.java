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
import com.google.gdt.eclipse.designer.model.widgets.panels.IStackLayoutPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.DoublePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
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
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Model for <code>com.google.gwt.user.client.ui.StackLayoutPanel</code> in UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class StackLayoutPanelInfo extends ComplexPanelInfo
    implements
      IStackLayoutPanelInfo<WidgetInfo> {
  private final Property m_unitProperty = new UnitAttributeProperty(this);
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
  public StackLayoutPanelInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    contributeHeaderProperties();
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
   * Ensures that "customHeader" {@link WidgetInfo} are moved to their "content" {@link WidgetInfo}.
   * Problem is that when we parse UiBinder template, we bind children to their hierarchical
   * children, and "customHeader" is child of "panel", but "sibling" of its "content"
   * {@link WidgetInfo}. So, we fix this here.
   */
  private void tweakCustomHeadersHierarchy() {
    addBroadcastListener(new ObjectInfoTreeComplete() {
      public void invoke() throws Exception {
        List<?> layoutDataList =
            (List<?>) ReflectionUtils.getFieldObject(getObject(), "layoutData");
        for (Object layoutData : layoutDataList) {
          // prepare "header" model
          Object headerWrapper = ReflectionUtils.getFieldObject(layoutData, "header");
          Object headerObject = ReflectionUtils.getFieldObject(headerWrapper, "widget");
          WidgetInfo headerModel = (WidgetInfo) getChildByObject(headerObject);
          // if has "custom header", move it to "widget"
          if (headerModel != null) {
            Object widgetObject = ReflectionUtils.getFieldObject(layoutData, "widget");
            WidgetInfo widgetModel = (WidgetInfo) getChildByObject(widgetObject);
            removeChild(headerModel);
            widgetModel.addChild(headerModel);
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
   * Ensures that given {@link WidgetInfo} is shown on this {@link StackLayoutPanelInfo}.
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
    properties.add(m_unitProperty);
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    registerAttributeValue("unit", ReflectionUtils.getFieldObject(getObject(), "unit"));
    super.refresh_afterCreate();
    ensureNotEmpty();
    showActiveWidget();
  }

  private void ensureNotEmpty() throws Exception {
    XmlObjectUtils.executeScriptParameter(this, "refresh_beforeAssociation");
  }

  private void showActiveWidget() throws Exception {
    WidgetInfo active = m_stackContainer.getActive();
    if (active != null) {
      int index = getWidgetIndex(active);
      ReflectionUtils.invokeMethod(getObject(), "showWidget(int,int,boolean)", index, 0, false);
    }
  }

  private int getWidgetIndex(WidgetInfo widget) throws Exception {
    return (Integer) ReflectionUtils.invokeMethod(
        getObject(),
        "getWidgetIndex(com.google.gwt.user.client.ui.Widget)",
        widget.getObject());
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
   * Object that describes header for widgets on {@link StackLayoutPanelInfo}.
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
    List<?> layoutDataList = (List<?>) ReflectionUtils.getFieldObject(getObject(), "layoutData");
    List<WidgetInfo> widgets = getChildrenWidgets();
    for (int index = 0; index < widgets.size(); index++) {
      WidgetInfo widget = widgets.get(index);
      Object headerWidget = ReflectionUtils.getFieldObject(layoutDataList.get(index), "header");
      Object headerElement = state.getUIObjectUtils().getElement(headerWidget);
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
    getHeaderTextProperty(component).setValue("New widget");
    getHeaderSizeProperty(component).setValue(2.0);
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
    final Object headerText = getHeaderTextProperty(widget).getValue();
    final Object headerSize = getHeaderSizeProperty(widget).getValue();
    commands.add(new PanelClipboardCommand<StackLayoutPanelInfo>(widget) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(StackLayoutPanelInfo panel, WidgetInfo widget) throws Exception {
        panel.command_CREATE2(widget, null);
        panel.getHeaderTextProperty(widget).setValue(headerText);
        panel.getHeaderSizeProperty(widget).setValue(headerSize);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Header properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("0.#",
      new DecimalFormatSymbols(Locale.ENGLISH));

  /**
   * Contributes "HeaderText" and "HeaderSize" properties to each {@link WidgetInfo} child.
   */
  private void contributeHeaderProperties() {
    addBroadcastListener(new XmlObjectAddProperties() {
      public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
        if (object instanceof WidgetInfo && getChildren().contains(object)) {
          WidgetInfo widget = (WidgetInfo) object;
          {
            Property textProperty = getHeaderTextProperty(widget);
            if (textProperty != null) {
              properties.add(textProperty);
            }
          }
          {
            Property sizeProperty = getHeaderSizeProperty(widget);
            properties.add(sizeProperty);
          }
        }
      }
    });
  }

  /**
   * @return the existing or new "HeaderText" property.
   */
  private Property getHeaderTextProperty(final XmlObjectInfo widget) throws Exception {
    Property property = (Property) widget.getArbitraryValue(this);
    if (property == null) {
      final DocumentElement headerElement = getHeaderElement(widget);
      if (headerElement.getTagLocal().equals("header")) {
        property = new XmlProperty(widget, "HeaderText", StringPropertyEditor.INSTANCE) {
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
   * @return the existing or new "HeaderSize" property.
   */
  private Property getHeaderSizeProperty(final WidgetInfo widget) throws Exception {
    Property property = (Property) widget.getArbitraryValue(this);
    if (property == null) {
      property = new XmlProperty(widget, "HeaderSize", DoublePropertyEditor.INSTANCE) {
        @Override
        public boolean isModified() throws Exception {
          return getValue() != UNKNOWN_VALUE;
        }

        @Override
        public Object getValue() throws Exception {
          Object panelObject = StackLayoutPanelInfo.this.getObject();
          List<?> dataList = (List<?>) ReflectionUtils.getFieldObject(panelObject, "layoutData");
          Object widgetData = dataList.get(getWidgetIndex(widget));
          return ReflectionUtils.getFieldObject(widgetData, "headerSize");
        }

        @Override
        protected void setValueEx(Object value) throws Exception {
          if (value instanceof Double) {
            String sizeString = SIZE_FORMAT.format(value);
            DocumentElement headerElement = getHeaderElement(widget);
            headerElement.setAttribute("size", sizeString);
          }
        }
      };
      property.setCategory(PropertyCategory.system(7));
    }
    return property;
  }

  /**
   * @return the existing or new "header" element of "stack".
   */
  private static DocumentElement getHeaderElement(XmlObjectInfo widget) {
    DocumentElement widgetElement = widget.getElement();
    DocumentElement stackElement = widgetElement.getParent();
    int widgetIndex = stackElement.indexOf(widgetElement);
    if (widgetIndex == 0) {
      String tag = stackElement.getTagNS() + "header";
      DocumentElement headerElement = new DocumentElement(tag);
      stackElement.addChild(headerElement, 0);
      return headerElement;
    } else {
      return stackElement.getChildAt(0);
    }
  }
}
