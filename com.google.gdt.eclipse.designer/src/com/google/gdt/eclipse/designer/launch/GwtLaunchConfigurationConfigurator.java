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
package com.google.gdt.eclipse.designer.launch;

import com.google.gdt.eclipse.designer.common.Constants;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * Performs configuration for new GWT launch configuration.
 * <p>
 * See (Case 36683).
 * 
 * @author scheglov_ke
 * @coverage gwt.launch
 */
public final class GwtLaunchConfigurationConfigurator implements ILaunchConfigurationListener {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GwtLaunchConfigurationConfigurator() {
    DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ILaunchConfigurationListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void launchConfigurationAdded(final ILaunchConfiguration configuration) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        configureAdded(configuration);
      }
    });
  }

  public void launchConfigurationChanged(ILaunchConfiguration configuration) {
  }

  public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures newly added GWT launch configuration.
   */
  private void configureAdded(ILaunchConfiguration configuration) throws CoreException {
    String typeID = configuration.getType().getIdentifier();
    if (typeID.equals(Constants.LAUNCH_TYPE_ID_SHELL)
        || typeID.equals(Constants.LAUNCH_TYPE_ID_COMPILER)
        || typeID.equals(Constants.LAUNCH_TYPE_ID_JUNIT)) {
      ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
      {
        String vmArgs = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
        if (vmArgs.indexOf("-Xmx") == -1) {
          if (vmArgs.length() != 0) {
            vmArgs += " ";
          }
          vmArgs += "-Xmx256m";
        }
        wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArgs);
      }
      wc.doSave();
    }
  }
}
