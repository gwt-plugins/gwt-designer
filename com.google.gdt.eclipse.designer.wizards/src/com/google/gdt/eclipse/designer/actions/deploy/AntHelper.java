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
package com.google.gdt.eclipse.designer.actions.deploy;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import java.io.File;

/**
 * Helper class for running ANT scripts.
 * 
 * @author scheglov_ke
 * @coverage gwt.deploy
 */
public class AntHelper {
  private final File m_antFile;
  private final File m_workingDir;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AntHelper(File antFile, File workingDir) {
    m_workingDir = workingDir;
    m_antFile = antFile;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Execute
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Executes configured script and returns created {@link ILaunch}.
   */
  public ILaunch execute(IProgressMonitor monitor) throws CoreException {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfigurationType type =
        launchManager.getLaunchConfigurationType("org.eclipse.ant.AntLaunchConfigurationType"/*IAntLaunchConfigurationConstants.ID_ANT_LAUNCH_CONFIGURATION_TYPE*/);
    String name = launchManager.generateUniqueLaunchConfigurationNameFrom(m_antFile.getName());
    ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, name);
    // initialize with default values
    {
      workingCopy.setAttribute(
          IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER,
          "org.eclipse.ant.ui.AntClasspathProvider");
      workingCopy.setAttribute(
          "org.eclipse.ant.ui.DEFAULT_VM_INSTALL"/*IAntUIConstants.ATTR_DEFAULT_VM_INSTALL*/,
          true);
      applySeparateVMAttributes(workingCopy);
    }
    // set ANT file location and working directory
    workingCopy.setAttribute(
        "org.eclipse.ui.externaltools.ATTR_LOCATION"/*IExternalToolConstants.ATTR_LOCATION*/,
        m_antFile.getAbsolutePath());
    workingCopy.setAttribute(
        "org.eclipse.ui.externaltools.ATTR_WORKING_DIRECTORY"/*IExternalToolConstants.ATTR_WORKING_DIRECTORY*/,
        m_workingDir.getAbsolutePath());
    // launch
    workingCopy.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
    return workingCopy.launch(ILaunchManager.RUN_MODE, monitor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copied from AntJRETab
  //
  ////////////////////////////////////////////////////////////////////////////
  private void applySeparateVMAttributes(ILaunchConfigurationWorkingCopy configuration) {
    configuration.setAttribute(
        IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
        "org.eclipse.ant.internal.ui.antsupport.InternalAntRunner");
    configuration.setAttribute(
        DebugPlugin.ATTR_PROCESS_FACTORY_ID,
        "org.eclipse.ant.ui.remoteAntProcessFactory"/*IAntUIConstants.REMOTE_ANT_PROCESS_FACTORY_ID*/);
  }
}