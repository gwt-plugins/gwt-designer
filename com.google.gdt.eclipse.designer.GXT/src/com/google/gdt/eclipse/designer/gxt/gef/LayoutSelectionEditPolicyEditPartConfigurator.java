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
package com.google.gdt.eclipse.designer.gxt.gef;

import com.google.gdt.eclipse.designer.gxt.model.layout.LayoutInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.events.IEditPartListener;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;

/**
 * {@link IEditPartConfigurator} to set automatically {@link SelectionEditPolicy} that corresponds
 * to {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef
 */
public final class LayoutSelectionEditPolicyEditPartConfigurator implements IEditPartConfigurator {
  private static final String SELECTION_ID = "ExtGWT.LayoutInfo.SelectionEditPolicy";

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartConfigurator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditPart context, EditPart editPart) {
    if (editPart.getModel() instanceof LayoutContainerInfo) {
      LayoutContainerInfo container = (LayoutContainerInfo) editPart.getModel();
      if (container.hasLayout()) {
        new LayoutTracker(container, editPart);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutTracker
  //
  ////////////////////////////////////////////////////////////////////////////
  public static class LayoutTracker {
    private final LayoutContainerInfo m_container;
    private final EditPart m_containerPart;
    private LayoutInfo m_layout;
    private Class<?> m_policyClass;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public LayoutTracker(LayoutContainerInfo container, EditPart editPart) {
      m_container = container;
      m_containerPart = editPart;
      container.addBroadcastListener(new ObjectEventListener() {
        @Override
        public void refreshed() throws Exception {
          update();
        }
      });
      m_containerPart.addEditPartListener(new IEditPartListener() {
        public void childAdded(EditPart child, int index) {
          decorateChild(child);
        }

        public void removingChild(EditPart child, int index) {
        }
      });
      update();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Implementation
    //
    ////////////////////////////////////////////////////////////////////////////
    private void update() {
      LayoutInfo layout = m_container.getLayout();
      // check for new LayoutInfo, remove old SelectionEditPolicy's
      {
        if (m_layout != null && layout != m_layout) {
          undecorateChildren();
        }
        m_layout = layout;
      }
      // prepare new SelectionEditPolicy class
      if (layout != null) {
        try {
          String policyName = getSelectionClassName(layout);
          m_policyClass = m_container.getClass().getClassLoader().loadClass(policyName);
        } catch (Throwable e) {
          m_policyClass = null;
        }
        decorateChildren();
      }
    }

    private void undecorateChildren() {
      for (EditPart child : m_containerPart.getChildren()) {
        if (child.getModel() instanceof WidgetInfo) {
          WidgetInfo widget = (WidgetInfo) child.getModel();
          undecorateWidget(child, widget);
        }
      }
    }

    private void undecorateWidget(EditPart child, WidgetInfo widget) {
      child.installEditPolicy(SELECTION_ID, null);
    }

    private void decorateChildren() {
      for (EditPart child : m_containerPart.getChildren()) {
        decorateChild(child);
      }
    }

    private void decorateChild(EditPart child) {
      Object model = child.getModel();
      if (m_layout.isManagedObject(model)) {
        WidgetInfo widget = (WidgetInfo) model;
        decorateWidget(child, widget);
      }
    }

    private void decorateWidget(EditPart child, WidgetInfo widget) {
      if (m_policyClass != null) {
        if (child.getEditPolicy(SELECTION_ID) == null) {
          if (!decorateChild_childOnly(child)) {
            decorateChild_layoutAndChild(child);
          }
        }
      }
    }

    private boolean decorateChild_childOnly(final EditPart child) {
      return ExecutionUtils.runIgnore(new RunnableEx() {
        public void run() throws Exception {
          Constructor<?> constructor = m_policyClass.getConstructors()[0];
          EditPolicy editPolicy = (EditPolicy) constructor.newInstance(child.getModel());
          child.installEditPolicy(SELECTION_ID, editPolicy);
        }
      });
    }

    private void decorateChild_layoutAndChild(final EditPart child) {
      ExecutionUtils.runIgnore(new RunnableEx() {
        public void run() throws Exception {
          Constructor<?> constructor = m_policyClass.getConstructors()[0];
          EditPolicy editPolicy = (EditPolicy) constructor.newInstance(m_layout, child.getModel());
          child.installEditPolicy(SELECTION_ID, editPolicy);
        }
      });
    }

    private String getSelectionClassName(LayoutInfo layout) {
      String layoutName = layout.getClass().getName();
      String prefix = "com.google.gdt.eclipse.designer.gxt.model.layout.";
      String suffix = "LayoutInfo";
      if (layoutName.startsWith(prefix) && layoutName.endsWith(suffix)) {
        layoutName = StringUtils.substring(layoutName, prefix.length());
        layoutName = StringUtils.substring(layoutName, 0, -suffix.length());
      }
      return "com.google.gdt.eclipse.designer.gxt.gef.policy." + layoutName + "SelectionEditPolicy";
    }
  }
}
