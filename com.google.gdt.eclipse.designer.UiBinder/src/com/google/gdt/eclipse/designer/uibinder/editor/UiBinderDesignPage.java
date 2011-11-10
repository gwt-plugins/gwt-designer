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
package com.google.gdt.eclipse.designer.uibinder.editor;

import com.google.gdt.eclipse.designer.css.StylesComposite;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderParser;

import org.eclipse.wb.core.controls.flyout.FlyoutControlComposite;
import org.eclipse.wb.core.controls.flyout.IFlyoutPreferences;
import org.eclipse.wb.core.controls.flyout.PluginFlyoutPreferences;
import org.eclipse.wb.gef.core.ICommandExceptionHandler;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.xml.editor.XmlDesignComposite;
import org.eclipse.wb.internal.core.xml.editor.XmlDesignPage;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link XmlDesignPage} for GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.editor
 */
public final class UiBinderDesignPage extends XmlDesignPage {
  private GwtState m_state;
  private ClassLoader m_classLoader;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Control
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected XmlDesignComposite createDesignComposite(Composite parent,
      ICommandExceptionHandler exceptionHandler) {
    if (EnvironmentUtils.DEVELOPER_HOST) {
      return createDesignComposite_withStyles(parent, exceptionHandler);
    }
    return super.createDesignComposite(parent, exceptionHandler);
  }

  private XmlDesignComposite createDesignComposite_withStyles(Composite parent,
      ICommandExceptionHandler exceptionHandler) {
    return new XmlDesignComposite(parent, SWT.NONE, m_editor, exceptionHandler) {
      @Override
      protected void createGEFComposite(Composite parent) {
        PluginFlyoutPreferences preferences =
            new PluginFlyoutPreferences(DesignerPlugin.getPreferences(), "design.css");
        preferences.initializeDefaults(
            IFlyoutPreferences.DOCK_WEST,
            IFlyoutPreferences.STATE_OPEN,
            250);
        FlyoutControlComposite flyComposite =
            new FlyoutControlComposite(parent, SWT.NONE, preferences);
        GridDataFactory.create(flyComposite).grab().fill();
        flyComposite.setTitleText("Styles");
        flyComposite.setMinWidth(250);
        flyComposite.setValidDockLocations(IFlyoutPreferences.DOCK_WEST
            | IFlyoutPreferences.DOCK_EAST);
        // create CSS
        new StylesComposite(flyComposite.getFlyoutParent(), SWT.NONE);
        // usual GEF composite
        super.createGEFComposite(flyComposite.getClientParent());
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Render
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean shouldShowProgress() {
    return m_state == null;
  }

  @Override
  protected XmlObjectInfo parse() throws Exception {
    // prepare UiBinderContext
    UiBinderContext context;
    if (m_state == null) {
      context = new UiBinderContext(m_file, m_document);
    } else {
      context = new UiBinderContext(m_state, m_classLoader, m_file, m_document);
    }
    // do parse
    try {
      UiBinderParser parser = new UiBinderParser(context);
      return parser.parse();
    } finally {
      // remember GwtState, even if parsing failed, GwtState may be created
      if (m_state == null) {
        m_state = context.getState();
        m_classLoader = context.getClassLoader();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void disposeContext(boolean force) {
    super.disposeContext(force);
    // dispose GWTState
    if (force && m_state != null) {
      m_state.dispose();
      m_state = null;
      m_classLoader = null;
    }
  }
}
