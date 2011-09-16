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

import com.google.gdt.eclipse.designer.model.widgets.panels.IDockPanelInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateText;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StringComboPropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;

import java.util.List;

/**
 * Model for <code>com.google.gwt.user.client.ui.DockPanel</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public class DockPanelInfo extends CellPanelInfo implements IDockPanelInfo<WidgetInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DockPanelInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    contributeDirectionProperty();
    decorateWidget_withDirection();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasts
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Decorate {@link WidgetInfo} text with direction.
   */
  private void decorateWidget_withDirection() {
    addBroadcastListener(new ObjectInfoPresentationDecorateText() {
      public void invoke(ObjectInfo object, String[] text) throws Exception {
        if (object instanceof WidgetInfo && object.getParent() == DockPanelInfo.this) {
          String directionText = getDirection((WidgetInfo) object);
          text[0] = directionText + " - " + text[0];
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Direction
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean hasCenterWidget() {
    for (WidgetInfo widget : getChildrenWidgets()) {
      String direction = getDirection(widget);
      if ("CENTER".equals(direction)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return the name of direction.
   */
  private static String getDirection(WidgetInfo widget) {
    DocumentElement dockElement = widget.getElement().getParent();
    return dockElement.getAttribute("direction");
  }

  public void setDirection(WidgetInfo widget, String direction) throws Exception {
    DocumentElement dockElement = widget.getElement().getParent();
    dockElement.setAttribute("direction", direction);
    ExecutionUtils.refresh(this);
  }

  /**
   * Contributes <code>Direction</code> property for each child {@link WidgetInfo}.
   */
  private void contributeDirectionProperty() {
    addBroadcastListener(new XmlObjectAddProperties() {
      public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
        if (object instanceof WidgetInfo && object.getParent() == DockPanelInfo.this) {
          WidgetInfo widget = (WidgetInfo) object;
          // prepare "Direction" property
          Property directionProperty = (Property) widget.getArbitraryValue(this);
          if (directionProperty == null) {
            directionProperty = createDirectionProperty(widget);
            widget.putArbitraryValue(this, directionProperty);
          }
          // add "Direction" property
          properties.add(directionProperty);
        }
      }

      private Property createDirectionProperty(final WidgetInfo widget) throws Exception {
        PropertyEditor propertyEditor =
            new StringComboPropertyEditor("NORTH",
                "SOUTH",
                "WEST",
                "EAST",
                "CENTER",
                "LINE_START",
                "LINE_END");
        XmlProperty property = new XmlProperty(widget, "Direction", propertyEditor) {
          @Override
          public boolean isModified() throws Exception {
            return true;
          }

          @Override
          public Object getValue() throws Exception {
            return getDirection(widget);
          }

          @Override
          public void setValue(Object value) throws Exception {
            if (value instanceof String) {
              setDirection(widget, (String) value);
            }
          }
        };
        property.setCategory(PropertyCategory.system(7));
        return property;
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
    final String directionField = getDirection(widget);
    commands.add(new PanelClipboardCommand<DockPanelInfo>(widget) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(DockPanelInfo panel, WidgetInfo widget) throws Exception {
        panel.command_CREATE2(widget, null);
        panel.setDirection(widget, directionField);
      }
    });
  }
}
