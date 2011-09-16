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
package com.google.gdt.eclipse.designer.mobile.gef;

import com.google.gdt.eclipse.designer.mobile.device.DeviceSelectionSupport;
import com.google.gdt.eclipse.designer.mobile.device.model.IDeviceView;
import com.google.gdt.eclipse.designer.model.widgets.IUIObjectInfo;

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.core.gef.policy.selection.EmptySelectionEditPolicy;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.events.IEditPartListener;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.swt.graphics.Image;

/**
 * Configures {@link GraphicalEditPart} for {@link IUIObjectInfo} to show device image.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.gef
 */
public class MobileEditPartConfigurator implements IEditPartConfigurator {
  private static final Point DEVICE_LOCATION = new Point(5, 5);

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartConfigurator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditPart context, EditPart editPart) {
    if (editPart.getModel() instanceof IUIObjectInfo) {
      IUIObjectInfo object = (IUIObjectInfo) editPart.getModel();
      if (object.getUnderlyingModel().isRoot()) {
        new RootEditPartHandler(context.getViewer(), (GraphicalEditPart) editPart, object);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handler
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class RootEditPartHandler {
    private final GraphicalEditPart m_editPart;
    private final IUIObjectInfo m_object;
    private EditPolicy m_selectionPolicy;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public RootEditPartHandler(IEditPartViewer viewer,
        GraphicalEditPart editPart,
        IUIObjectInfo object) {
      m_editPart = editPart;
      m_object = object;
      // add/remove device figure
      final EditPart rootEditPart = viewer.getRootEditPart();
      final IEditPartListener listener = new IEditPartListener() {
        public void childAdded(EditPart child, int index) {
          Layer deviceLayer = child.getViewer().getLayer(IEditPartViewer.PRIMARY_LAYER_SUB_1);
          deviceLayer.add(m_deviceFigure);
          // refresh now, when EditPart is added
          refresh();
        }

        public void removingChild(EditPart child, int index) {
          FigureUtils.removeFigure(m_deviceFigure);
          rootEditPart.removeEditPartListener(this);
        }
      };
      rootEditPart.addEditPartListener(listener);
      // update EditPart figure and selection policy
      object.addBroadcastListener(new ObjectEventListener() {
        @Override
        public void refreshed2() throws Exception {
          refresh();
        }
      });
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Notify
    //
    ////////////////////////////////////////////////////////////////////////////
    private final Figure m_deviceFigure = new Figure() {
      @Override
      protected void paintClientArea(final Graphics graphics) {
        ExecutionUtils.runIgnore(new RunnableEx() {
          public void run() throws Exception {
            IDeviceView device = DeviceSelectionSupport.getDeviceView(m_object);
            if (device != null) {
              Image image = device.getImage();
              if (image != null) {
                graphics.drawImage(image, 0, 0);
              }
            }
          }
        });
      }
    };

    private void refresh() {
      refreshSelectionPolicy();
      refreshVisuals();
    }

    /**
     * Removes or restores {@link EditPolicy#SELECTION_ROLE}.
     */
    private void refreshSelectionPolicy() {
      // if has device
      if (DeviceSelectionSupport.getDeviceView(m_object) != null) {
        // if was no device yet
        if (m_selectionPolicy == null) {
          m_selectionPolicy = m_editPart.getEditPolicy(EditPolicy.SELECTION_ROLE);
        }
        // can not resize on device
        m_editPart.installEditPolicy(EditPolicy.SELECTION_ROLE, new EmptySelectionEditPolicy());
        return;
      }
      // restore original "selection" if was device before
      if (m_selectionPolicy != null) {
        m_editPart.installEditPolicy(EditPolicy.SELECTION_ROLE, m_selectionPolicy);
        m_selectionPolicy = null;
      }
    }

    /**
     * Updates bounds of {@link #m_deviceFigure} and {@link #m_editPart}.
     */
    private void refreshVisuals() {
      ExecutionUtils.runIgnore(new RunnableEx() {
        public void run() throws Exception {
          refreshVisualsEx();
        }
      });
    }

    /**
     * Implementation for {@link #refreshVisuals()}.
     */
    private void refreshVisualsEx() throws Exception {
      IDeviceView device = DeviceSelectionSupport.getDeviceView(m_object);
      if (device != null) {
        // update figure with device image
        {
          Image image = device.getImage();
          m_deviceFigure.setLocation(DEVICE_LOCATION);
          m_deviceFigure.setSize(image.getBounds().width, image.getBounds().height);
        }
        // update EditPart figure
        {
          Rectangle bounds = device.getDisplayBounds().getCopy();
          bounds.translate(DEVICE_LOCATION);
          m_editPart.getFigure().setBounds(bounds);
        }
      } else {
        Rectangle bounds = m_object.getBounds();
        bounds =
            new Rectangle(AbstractComponentEditPart.TOP_LOCATION.x,
                AbstractComponentEditPart.TOP_LOCATION.y,
                bounds.width,
                bounds.height);
        m_editPart.getFigure().setBounds(bounds);
      }
    }
  }
}
