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

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;
import org.eclipse.wb.internal.core.xml.editor.XmlDesignPage;

import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import org.osgi.framework.Bundle;

/**
 * Editor for GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.editor
 */
public final class UiBinderEditor extends AbstractXmlEditor {
  public static final String ID = "com.google.gdt.eclipse.designer.uibinder.editor.UiBinderEditor";
  private static final String GPE_BUNDLE_ID = "com.google.gwt.eclipse.core";
  private static final String GPE_TEXT_EDITOR_ID = GPE_BUNDLE_ID
      + ".uibinder.sse.UiBinderXmlTextEditor";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected StructuredTextEditor createEditorXml() {
    if (!EnvironmentUtils.isTestingTime()) {
      try {
        Bundle bundle = Platform.getBundle(GPE_BUNDLE_ID);
        if (bundle != null) {
          Class<?> clazz = bundle.loadClass(GPE_TEXT_EDITOR_ID);
          return (StructuredTextEditor) clazz.newInstance();
        }
      } catch (Throwable e) {
      }
    }
    return super.createEditorXml();
  }

  @Override
  protected XmlDesignPage createDesignPage() {
    return new UiBinderDesignPage();
  }
}
