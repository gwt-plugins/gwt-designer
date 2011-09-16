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
package com.google.gdt.eclipse.designer.gwtext.model.widgets;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoChildBeforeAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import java.util.Iterator;
import java.util.List;

/**
 * Model for <code>Component</code>.
 * 
 * @author sablin_aa
 * @coverage GWTExt.model
 */
public class ComponentInfo extends WidgetInfo {
  private static final String KEY_ROOT_PANEL = "GWT-Ext viewport Panel";
  private final ComponentInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addBroadcastListener(new JavaInfoChildBeforeAssociation(this));
    configureBeforeAssociation();
    hierarchy_removeIfDangling();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * GWT-Ext components often require tweaks before association, and it is easier to do this in
   * script.
   */
  private void configureBeforeAssociation() {
    final String script = JavaInfoUtils.getParameter(this, "GWTExt.configureBeforeAssociation");
    if (script != null) {
      new ComponentConfiguratorBeforeAssociation(this) {
        @Override
        protected void configure() throws Exception {
          ExecutionUtils.runLog(new RunnableEx() {
            public void run() throws Exception {
              JavaInfoUtils.executeScript(m_this, script);
            }
          });
        }
      };
    }
  }

  /**
   * GWT-Ext component (I've checked <code>GridPanel</code> and <code>Button</code>) renders itself
   * on <code>RootPanel</code> when we call <code>getElement()</code>, so it will be attached even
   * if there are no association in source code. To solve this we should remove any dangling models
   * directly after parsing. We can not support "unknown" or "binary" associations.
   */
  private void hierarchy_removeIfDangling() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void bindComponents(List<JavaInfo> components) throws Exception {
        removeBroadcastListener(this);
        boolean isThisRoot = getCreationSupport() instanceof ThisCreationSupport;
        boolean isViewportRoot = getArbitraryValue(KEY_ROOT_PANEL) == Boolean.TRUE;
        boolean hasParent = m_this.getParent() != null;
        if (!isThisRoot && !isViewportRoot && !hasParent) {
          components.remove(m_this);
        }
        // if we have Viewport, we don't need RootPanel
        if (isViewportRoot) {
          removeRootPanel(components);
        }
      }

      private void removeRootPanel(List<JavaInfo> components) throws Exception {
        for (Iterator<JavaInfo> I = components.iterator(); I.hasNext();) {
          JavaInfo javaInfo = I.next();
          if (javaInfo instanceof RootPanelInfo) {
            I.remove();
            // break parent/child association
            javaInfo.removeChild(m_this);
            m_this.setParent(null);
          }
        }
      }
    });
  }

  /**
   * Specifies that given panel was used as argument of <code>new Viewport(panel)</code>, so is
   * root.
   */
  public static void markAsViewportRoot(ComponentInfo panel) {
    panel.putArbitraryValue(KEY_ROOT_PANEL, Boolean.TRUE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    super.refresh_dispose();
    {
      Object object = getObject();
      if (object != null) {
        //ReflectionUtils.invokeMethod(object, "destroy()");
      }
    }
  }
}
