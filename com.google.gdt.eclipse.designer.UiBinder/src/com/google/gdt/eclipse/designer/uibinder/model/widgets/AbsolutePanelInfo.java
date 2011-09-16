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

import com.google.gdt.eclipse.designer.model.widgets.panels.IAbsolutePanelInfo;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;
import org.eclipse.wb.internal.core.model.layout.absolute.OrderingSupport;
import org.eclipse.wb.internal.core.model.util.AbsoluteLayoutCreationFlowSupport;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.editor.DesignContextMenuProvider;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.property.converter.IntegerConverter;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;

import java.util.List;

/**
 * Model for <code>com.google.gwt.user.client.ui.AbsolutePanel</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public class AbsolutePanelInfo extends ComplexPanelInfo implements IAbsolutePanelInfo<WidgetInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsolutePanelInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    contributeWidgetContextMenu();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  private void contributeWidgetContextMenu() {
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object instanceof WidgetInfo && object.getParent() == AbsolutePanelInfo.this) {
          WidgetInfo component = (WidgetInfo) object;
          contributeWidgetContextMenu(manager, component);
        }
      }
    });
  }

  /**
   * Contributes {@link Action}'s into {@link WidgetInfo} context menu.
   */
  private void contributeWidgetContextMenu(IMenuManager manager, final WidgetInfo widget) {
    // order
    {
      List<WidgetInfo> widgets = getChildrenWidgets();
      new OrderingSupport(widgets, widget).contributeActions(manager);
    }
    // auto-size
    {
      IAction action =
          new ObjectInfoAction(widget, "Autosize widget",
              DesignerPlugin.getImageDescriptor("info/layout/absolute/fit_to_size.png")) {
            @Override
            protected void runEx() throws Exception {
              widget.getSizeSupport().setSize(null);
            }
          };
      manager.appendToGroup(DesignContextMenuProvider.GROUP_CONSTRAINTS, action);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_BOUNDS(WidgetInfo widget, Point location, Dimension size) throws Exception {
    Assert.isTrue(getChildren().contains(widget), "%s is not child of %s.", widget, this);
    if (size != null) {
      widget.getSizeSupport().setSize(size);
    }
    if (location != null) {
      setLocation(widget, location);
    }
    // check creation flow
    if (location != null
        && (widget.getModelBounds() != null || EnvironmentUtils.isTestingTime())
        && useCreationFlow()) {
      AbsoluteLayoutCreationFlowSupport.apply(this, getChildrenWidgets(), widget, location, size);
    }
  }

  /**
   * Modifies location of {@link WidgetInfo}.
   * 
   * @param location
   *          new location, not <code>null</code>.
   */
  private void setLocation(WidgetInfo widget, Point location) throws Exception {
    String xString = IntegerConverter.INSTANCE.toSource(this, location.x);
    String yString = IntegerConverter.INSTANCE.toSource(this, location.y);
    //
    DocumentElement positionElement = widget.getElement().getParent();
    positionElement.setAttribute("left", xString);
    positionElement.setAttribute("top", yString);
  }

  private boolean useCreationFlow() {
    IPreferenceStore preferences = getDescription().getToolkit().getPreferences();
    return preferences.getBoolean(IPreferenceConstants.P_CREATION_FLOW);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
    final Rectangle modelBounds = widget.getModelBounds();
    commands.add(new PanelClipboardCommand<AbsolutePanelInfo>(widget) {
      private static final long serialVersionUID = 0L;

      @Override
      protected void add(AbsolutePanelInfo panel, WidgetInfo widget) throws Exception {
        panel.command_CREATE2(widget, null);
        panel.command_BOUNDS(widget, modelBounds.getLocation(), null);
      }
    });
  }
}
