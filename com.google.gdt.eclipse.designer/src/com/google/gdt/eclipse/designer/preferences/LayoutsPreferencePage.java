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
package com.google.gdt.eclipse.designer.preferences;

import com.google.gdt.eclipse.designer.ToolkitProvider;
import com.google.gdt.eclipse.designer.model.LayoutDataNameSupport;
import com.google.gdt.eclipse.designer.model.LayoutNameSupport;
import com.google.gdt.eclipse.designer.model.widgets.panels.AbsolutePanelPreferenceConstants;

import org.eclipse.wb.core.controls.jface.preference.ComboFieldEditor;
import org.eclipse.wb.core.controls.jface.preference.FieldLayoutPreferencePage;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Main {@link PreferencePage} for Layout Support.
 * 
 * @author sablin_aa
 * @coverage gwt.preferences.ui
 */
public final class LayoutsPreferencePage extends FieldLayoutPreferencePage
    implements
      IWorkbenchPreferencePage,
      AbsolutePanelPreferenceConstants,
      IPreferenceConstants {
  /**
   * @return The {@link IPreferenceStore} of Swing Toolkit Support plugin
   */
  @Override
  public IPreferenceStore getPreferenceStore() {
    return ToolkitProvider.DESCRIPTION.getPreferences();
  }

  @Override
  protected Control createPageContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    GridLayoutFactory.create(container);
    // layout name templates
    {
      String[][] entryNamesAndValues = new String[LayoutNameSupport.TEMPLATES.length][2];
      {
        // prepare entryNamesAndValues
        for (int i = 0; i < LayoutNameSupport.TEMPLATES.length; i++) {
          entryNamesAndValues[i][0] = LayoutNameSupport.TEMPLATES[i];
          entryNamesAndValues[i][1] = LayoutNameSupport.TEMPLATES[i];
        }
      }
      createComboFieldEditor(
          P_LAYOUT_NAME_TEMPLATE,
          "Create variable for Layout using pattern:",
          entryNamesAndValues,
          container);
    }
    // layout data name templates
    {
      String[][] entryNamesAndValues = new String[LayoutDataNameSupport.TEMPLATES.length][2];
      {
        // prepare entryNamesAndValues
        for (int i = 0; i < LayoutDataNameSupport.TEMPLATES.length; i++) {
          entryNamesAndValues[i][0] = LayoutDataNameSupport.TEMPLATES[i];
          entryNamesAndValues[i][1] = LayoutDataNameSupport.TEMPLATES[i];
        }
      }
      createComboFieldEditor(
          P_LAYOUT_DATA_NAME_TEMPLATE,
          "Create variable for LayoutData using pattern:",
          entryNamesAndValues,
          container);
    }
    return container;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Helper method Create {@link #ComboFieldEditor} within own composite on specified parent
   */
  private void createComboFieldEditor(String key,
      String labelText,
      String[][] entryNamesAndValues,
      Composite parent) {
    final Composite composite = new Composite(parent, SWT.NONE);
    addField(new ComboFieldEditor(key, labelText, entryNamesAndValues, composite));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //	IWorkbenchPreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench) {
  }
}