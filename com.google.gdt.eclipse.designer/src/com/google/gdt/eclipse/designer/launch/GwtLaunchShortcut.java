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
package com.google.gdt.eclipse.designer.launch;

import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.common.GwtLabelProvider;
import com.google.gdt.eclipse.designer.model.web.WebUtils;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs single click launching for GWT applications.
 * 
 * @author scheglov_ke
 * @coverage gwt.launch
 */
public class GwtLaunchShortcut implements ILaunchShortcut {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Launch
  //
  ////////////////////////////////////////////////////////////////////////////
  public void launch(ISelection selection, String mode) {
    try {
      if (selection instanceof IStructuredSelection) {
        Object selectedObject = ((IStructuredSelection) selection).getFirstElement();
        IResource resource =
            (IResource) Platform.getAdapterManager().getAdapter(selectedObject, IResource.class);
        if (resource != null) {
          launch(resource, mode);
        }
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  public void launch(IEditorPart editor, String mode) {
    try {
      IEditorInput input = editor.getEditorInput();
      IResource resource = (IFile) input.getAdapter(IResource.class);
      if (resource != null) {
        launch(resource, mode);
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Launch implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Launches module which contains given resource.
   */
  private void launch(IResource resource, String mode) throws Exception {
    if (resource != null) {
      // try to get exact module
      {
        ModuleDescription module = Utils.getExactModule(resource);
        if (module != null) {
          launch(module, mode);
          return;
        }
      }
      // resource in module
      List<ModuleDescription> modules = Utils.getModules(resource);
      launch(modules, mode);
    }
  }

  /**
   * Launch one of the given modules.
   */
  private void launch(List<ModuleDescription> modules, String mode) throws CoreException {
    // simple cases - zero or one module
    if (modules.isEmpty()) {
      return;
    }
    if (modules.size() == 1) {
      launch(modules.get(0), mode);
      return;
    }
    // complex case - several modules
    ModuleDescription module = chooseModule(modules);
    if (module != null) {
      launch(module, mode);
    }
  }

  /**
   * Launch given module in given mode.
   */
  private void launch(ModuleDescription module, String mode) throws CoreException {
    ILaunchConfiguration configuration = getExistingOrNewConfiguration(module);
    if (configuration != null) {
      DebugUITools.launch(configuration, mode);
    }
  }

  /**
   * Returns existing or new launch configuration for given GWT module file.
   */
  private ILaunchConfiguration getExistingOrNewConfiguration(ModuleDescription module)
      throws CoreException {
    // prepare launch parameters
    ILaunchManager launchManager = getLaunchManager();
    ILaunchConfigurationType configurationType = getConfigurationType();
    // prepare module parameters
    IProject project = module.getProject();
    String moduleId = module.getId();
    String moduleName = module.getSimpleName();
    // try to find existing launch configuration for current module
    {
      // prepare list of candidates
      List<ILaunchConfiguration> candidateConfigurations;
      {
        candidateConfigurations = new ArrayList<ILaunchConfiguration>();
        ILaunchConfiguration[] launchConfigurations =
            launchManager.getLaunchConfigurations(configurationType);
        for (int i = 0; i < launchConfigurations.length; i++) {
          ILaunchConfiguration configuration = launchConfigurations[i];
          if (configuration.getAttribute(Constants.LAUNCH_ATTR_MODULE, "").equals(moduleId)) {
            if (configuration.getAttribute(Constants.LAUNCH_ATTR_PROJECT, "").equals(
                project.getName())) {
              candidateConfigurations.add(configuration);
            }
          }
        }
      }
      // use only one configuration or prompt user to choose one
      int candidateCount = candidateConfigurations.size();
      if (candidateCount == 1) {
        return candidateConfigurations.get(0);
      }
      if (candidateCount > 1) {
        return chooseConfiguration(candidateConfigurations);
      }
    }
    // create new configuration
    {
      // create working copy
      ILaunchConfigurationWorkingCopy wc;
      {
        String configurationName =
            launchManager.generateUniqueLaunchConfigurationNameFrom(moduleName);
        wc = configurationType.newInstance(null, configurationName);
        wc.setAttribute(Constants.LAUNCH_ATTR_PROJECT, project.getName());
        wc.setAttribute(Constants.LAUNCH_ATTR_MODULE, moduleId);
        wc.setAttribute(Constants.LAUNCH_ATTR_LOG_LEVEL, "INFO");
        wc.setAttribute(Constants.LAUNCH_ATTR_DIR_WAR, WebUtils.getWebFolderName(project));
        wc.setAttribute(Constants.LAUNCH_ATTR_STYLE, "OBFUSCATED");
        wc.setMappedResources(new IResource[]{project});
      }
      // create configuration
      return wc.doSave();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Show a selection dialog that allows the user to choose one of the specified launch
   * configurations.
   * 
   * @return the chosen config, or <code>null</code> if the user cancelled the dialog.
   */
  private static ILaunchConfiguration chooseConfiguration(List<ILaunchConfiguration> configurations) {
    ILabelProvider labelProvider = DebugUITools.newDebugModelPresentation();
    //
    ElementListSelectionDialog dialog =
        new ElementListSelectionDialog(DesignerPlugin.getShell(), labelProvider);
    dialog.setElements(configurations.toArray());
    dialog.setTitle("Select GWT application");
    dialog.setMessage("&Select existing configuration:");
    dialog.setMultipleSelection(false);
    //
    int result = dialog.open();
    labelProvider.dispose();
    if (result == Window.OK) {
      return (ILaunchConfiguration) dialog.getFirstResult();
    }
    return null;
  }

  /**
   * Show a selection dialog that allows the user to choose one of the specified modules.
   * 
   * @return the chosen module, or <code>null</code> if the user cancelled the dialog.
   */
  private static ModuleDescription chooseModule(List<ModuleDescription> modules) {
    ElementListSelectionDialog dialog;
    {
      dialog = new ElementListSelectionDialog(DesignerPlugin.getShell(), GwtLabelProvider.INSTANCE);
      dialog.setElements(modules.toArray());
      dialog.setTitle("Select GWT module");
      dialog.setMessage("&Select module to launch:");
      dialog.setMultipleSelection(false);
    }
    //
    int result = dialog.open();
    if (result == Window.OK) {
      return (ModuleDescription) dialog.getFirstResult();
    }
    return null;
  }

  /**
   * Convenient method for launch manager access.
   */
  private static ILaunchManager getLaunchManager() {
    return DebugPlugin.getDefault().getLaunchManager();
  }

  /**
   * Convenient method for launch configuration type access.
   */
  private ILaunchConfigurationType getConfigurationType() {
    return getLaunchManager().getLaunchConfigurationType(getLaunchConfigurationTypeId());
  }

  /**
   * Returns the launch configuration type id of the launch configuration this shortcut will create.
   * Clients can override this method to return the id of their launch configuration.
   * 
   * @return the launch configuration type id of the launch configuration this shortcut will create
   */
  protected String getLaunchConfigurationTypeId() {
    return Constants.LAUNCH_TYPE_ID_SHELL;
  }
}
