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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * 
 * @author lobas_av
 * @coverage gwt.launch
 */
public class JUnitGwtLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
  public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
    ILaunchConfigurationTab[] tabs =
        new ILaunchConfigurationTab[]{
            new org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationTab(),
            new JavaArgumentsTab(),
            new JavaClasspathTab(),
            new JavaJRETab(),
            new SourceLookupTab(),
            new EnvironmentTab(),
            new CommonTab()};
    setTabs(tabs);
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    super.setDefaults(config);
    try {
      AbstractUIPlugin junitPlugin = JdtUiUtils.getBundleActivator("org.eclipse.jdt.junit");
      IPreferenceStore preferenceStore = junitPlugin.getPreferenceStore();
      String arg =
          preferenceStore.getBoolean("org.eclipse.jdt.junit.enable_assertions") ? "-ea" : "";
      config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, arg);
    } catch (Exception e) {
      DesignerPlugin.log(e);
    }
  }
}