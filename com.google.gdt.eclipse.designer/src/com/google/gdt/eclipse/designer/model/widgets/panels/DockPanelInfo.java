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

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateText;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Model for GWT <code>com.google.gwt.user.client.ui.DockPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class DockPanelInfo extends CellPanelInfo implements IDockPanelInfo<WidgetInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DockPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    // decorate Widget's text with direction
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
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        for (WidgetInfo widget : getChildrenWidgets()) {
          String direction = getDirection(widget);
          if ("CENTER".equals(direction)) {
            return true;
          }
        }
        return false;
      }
    }, false);
  }

  /**
   * @return the name of direction field.
   */
  private static String getDirection(WidgetInfo widget) throws Exception {
    Property directionProperty = widget.getPropertyByTitle("Direction");
    return PropertyUtils.getText(directionProperty);
  }

  public final void setDirection(WidgetInfo widget, String directionField) throws Exception {
    Property directionProperty = widget.getPropertyByTitle("Direction");
    if (directionProperty != null) {
      Object direction = getDirectionValue(directionField);
      directionProperty.setValue(direction);
    }
  }

  private Object getDirectionValue(String directionField) {
    Class<?> panelClass = getDescription().getComponentClass();
    return ReflectionUtils.getFieldObject(panelClass, directionField);
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
