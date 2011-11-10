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
package com.google.gdt.eclipse.designer.gpe.preferences;

import org.eclipse.wb.core.controls.jface.preference.ComboFieldEditor;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * GPE-style preference page for WindowBuilder.
 * 
 * @coverage gwt.gpe
 */
public final class DesignerPreferencePage extends FieldEditorPreferencePage
    implements
      IWorkbenchPreferencePage,
      IPreferenceConstants {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignerPreferencePage() {
    super(GRID);
    setPreferenceStore(DesignerPlugin.getPreferences());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FieldEditorPreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createFieldEditors() {
    // hint
    {
      Label label = new Label(getFieldEditorParent(), SWT.NONE);
      label.setText("Close and re-open any editors to see the effects of these preferences.");
      GridDataFactory.create(label).spanH(2).alignHF();
    }
    {
      // editor layout mode
      ComboFieldEditor editorLayout =
          new ComboFieldEditor(P_EDITOR_LAYOUT, "Editor layout:", new String[][]{
              new String[]{
                  "On separate notebook tabs (Source first)",
                  "" + V_EDITOR_LAYOUT_PAGES_SOURCE},
              new String[]{
                  "On separate notebook tabs (Design first)",
                  "" + V_EDITOR_LAYOUT_PAGES_DESIGN},
              new String[]{
                  "Above each other with a split pane (Source first)",
                  "" + V_EDITOR_LAYOUT_SPLIT_VERTICAL_SOURCE},
              new String[]{
                  "Above each other with a split pane (Design first)",
                  "" + V_EDITOR_LAYOUT_SPLIT_VERTICAL_DESIGN},
              new String[]{
                  "Side by side with a split pane (Source first)",
                  "" + V_EDITOR_LAYOUT_SPLIT_HORIZONTAL_SOURCE},
              new String[]{
                  "Side by side with a split pane (Design first)",
                  "" + V_EDITOR_LAYOUT_SPLIT_HORIZONTAL_DESIGN},}, getFieldEditorParent());
      addField(editorLayout);
      // sync delay
      IntegerFieldEditor syncDelay =
          new IntegerFieldEditor(P_EDITOR_LAYOUT_SYNC_DELAY,
              "Sync Delay (ms):",
              getFieldEditorParent());
      syncDelay.setErrorMessage("Default syncronization delay in milliseconds must be an integer value in 250-10000 range.");
      syncDelay.setEmptyStringAllowed(false);
      syncDelay.setValidRange(-1, Integer.MAX_VALUE);
      syncDelay.getTextControl(getFieldEditorParent()).setToolTipText(
          "Set the default syncronization delay in milliseconds, -1 for syncronization on save");
      addField(syncDelay);
    }
    {
      // From com.google.gdt.eclipse.designer.preferences.MainPreferencePage
      /*
      checkButton(
      	group,
      	2,
      	"Show important properties dialog on component adding",
      	IPreferenceConstants.P_GENERAL_IMPORTANT_PROPERTIES_AFTER_ADD);
      */
      // Field-Editor version:
      addField(new BooleanFieldEditor(P_GENERAL_IMPORTANT_PROPERTIES_AFTER_ADD,
          "Show important properties dialog on component adding",
          getFieldEditorParent()));
    }
    {
      // highlight visited/executed lines
      addField(new BooleanFieldEditor(P_HIGHLIGHT_VISITED,
          "Highlight visited/executed lines in source after parse",
          getFieldEditorParent()));
      addField(new ColorFieldEditor(P_HIGHLIGHT_VISITED_COLOR,
          "Visited line highlight color:",
          getFieldEditorParent()));
    }
    {
      addField(new BooleanFieldEditor(P_COMMON_SHOW_DEBUG_INFO,
          "Show debug information in console",
          getFieldEditorParent()));
      if (EnvironmentUtils.IS_LINUX) {
        addField(new BooleanFieldEditor(P_COMMON_LINUX_DISABLE_SCREENSHOT_WORKAROUNDS,
            "Disable Preview Window flickering workarounds (Linux only)",
            getFieldEditorParent()));
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench) {
  }

  @Override
  protected void performDefaults() {
    super.performDefaults();
    IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();
    store.setToDefault(P_EDITOR_LAYOUT);
    store.setToDefault(P_EDITOR_LAYOUT_SYNC_DELAY);
    store.setToDefault(P_HIGHLIGHT_VISITED);
    store.setToDefault(P_HIGHLIGHT_VISITED_COLOR);
    store.setToDefault(P_COMMON_SHOW_DEBUG_INFO);
    store.setToDefault(P_COMMON_LINUX_DISABLE_SCREENSHOT_WORKAROUNDS);
  }
}
