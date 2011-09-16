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
package com.google.gdt.eclipse.designer.gxt.actions;

import com.google.gdt.eclipse.designer.actions.AbstractModuleAction;
import com.google.gdt.eclipse.designer.actions.wizard.model.IModuleConfigurator;
import com.google.gdt.eclipse.designer.gxt.Activator;
import com.google.gdt.eclipse.designer.preferences.IPreferenceConstants;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.wizards.AbstractActionDelegate;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.DirectoryDialog;

import java.io.File;

/**
 * Action for configuring GWT module for using Ext-GWT.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.actions
 */
public final class ConfigureExtGwtAction extends AbstractActionDelegate
    implements
      IModuleConfigurator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IActionDelegate
  //
  ////////////////////////////////////////////////////////////////////////////
  public void run(IAction action) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        ModuleDescription module = AbstractModuleAction.getSelectedModule(getSelection());
        configure(module);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IModuleConfigurator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(ModuleDescription module) throws Exception {
    String libraryLocation = getLibraryLocation();
    if (libraryLocation != null) {
      new ConfigureExtGwtOperation(module, libraryLocation).run(null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static String LOCATION_KEY = IPreferenceConstants.TOOLKIT_ID + ".GXT.LibraryLocation";

  /**
   * @return the folder with "gwt.jar" file, or <code>null</code> if user cancelled selection.
   */
  private static String getLibraryLocation() {
    IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
    String libraryLocation = preferenceStore.getString(LOCATION_KEY);
    while (true) {
      DirectoryDialog directoryDialog = new DirectoryDialog(DesignerPlugin.getShell());
      directoryDialog.setText("Ext GWT Location");
      directoryDialog.setMessage("Choose folder with extracted Ext GWT, it should have gxt.jar file.\n"
          + "You can download it from http://www.extjs.com/products/gxt/");
      directoryDialog.setFilterPath(libraryLocation);
      libraryLocation = directoryDialog.open();
      // cancel
      if (libraryLocation == null) {
        return null;
      }
      // check for "gxt.jar" file
      {
        File file = new File(libraryLocation + "/gxt.jar");
        if (!file.exists() || !file.isFile()) {
          MessageDialog.openError(null, "Invalid folder", "No gxt.jar file in choosen folder.");
          continue;
        }
      }
      // check for "resources" directory
      {
        File file = new File(libraryLocation + "/resources");
        if (!file.exists() || !file.isDirectory()) {
          MessageDialog.openError(
              null,
              "Invalid folder",
              "No 'resources' directory in choosen folder.\n"
                  + "Just gxt.jar file is not enough, we need also images and CSS files.");
          continue;
        }
      }
      // OK
      preferenceStore.setValue(LOCATION_KEY, libraryLocation);
      return libraryLocation;
    }
  }
}