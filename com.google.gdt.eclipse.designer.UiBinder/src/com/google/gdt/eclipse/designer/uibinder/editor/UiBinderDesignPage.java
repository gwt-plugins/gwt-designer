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

import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderParser;

import org.eclipse.wb.internal.core.xml.editor.XmlDesignPage;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

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
