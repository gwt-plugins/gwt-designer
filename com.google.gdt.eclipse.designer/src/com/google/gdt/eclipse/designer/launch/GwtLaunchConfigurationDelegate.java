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

import com.google.gdt.eclipse.designer.actions.AbstractModuleAction;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.lang.StringUtils;

/**
 * @author scheglov_ke
 * @coverage gwt.launch
 */
public class GwtLaunchConfigurationDelegate extends AbstractGwtLaunchConfigurationDelegate {
  private IJavaProject m_javaProject;
  private String m_moduleName;
  private ModuleDescription m_module;
  private String m_moduleHtml;

  ////////////////////////////////////////////////////////////////////////////
  //
  // LaunchCheck
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean preLaunchCheck(final ILaunchConfiguration configuration,
      final String mode,
      IProgressMonitor monitor) throws CoreException {
    try {
      prepareModule(configuration);
      // check exist module HTML
      if (!Utils.isExistingResource(m_module, m_moduleHtml)) {
        // prepare dialog return code
        final int[] returnCode = new int[1];
        // show launch configure dialog
        Display.getDefault().syncExec(new Runnable() {
          public void run() {
            returnCode[0] =
                DebugUITools.openLaunchConfigurationDialog(
                    DesignerPlugin.getShell(),
                    configuration,
                    "org.eclipse.debug.ui.launchGroup." + mode,
                    null);
          }
        });
        // check button if cancel don't launch
        if (returnCode[0] == Window.CANCEL) {
          return false;
        }
        // prepare again
        prepareModule(configuration);
      }
    } catch (Throwable e) {
      AbstractModuleAction.showException(e);
      return false;
    }
    return super.preLaunchCheck(configuration, mode, monitor);
  }

  private void prepareModule(final ILaunchConfiguration configuration) throws Exception {
    m_javaProject = getJavaProject(configuration);
    m_moduleName = configuration.getAttribute(Constants.LAUNCH_ATTR_MODULE, (String) null);
    m_module = Utils.getModule(m_javaProject, m_moduleName);
    Assert.isNotNull2(
        m_module,
        "Unable to find file for module {0} in {1}",
        m_moduleName,
        m_javaProject);
    // prepare HTML
    m_moduleHtml = configuration.getAttribute(Constants.LAUNCH_ATTR_MODULE_HTML, (String) null);
    if (m_moduleHtml == null) {
      m_moduleHtml = Utils.getDefaultHTMLName(m_moduleName);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Main type
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getMainTypeName(ILaunchConfiguration configuration) throws CoreException {
    return Constants.GWT_DEV_MODE_CLASS;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Program arguments
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
    // prepare startup URL, with optional parameters
    String startupUrl = m_moduleHtml;
    {
      String parameters = configuration.getAttribute(Constants.LAUNCH_ATTR_PARAMETERS, "");
      if (parameters.length() != 0) {
        startupUrl += parameters;
      }
    }
    // append URL
    String urlArg;
    {
      String externalURL = configuration.getAttribute(Constants.LAUNCH_ATTR_URL, (String) null);
      if (!StringUtils.isEmpty(externalURL)) {
        urlArg = " -startupUrl " + externalURL + " " + m_moduleName;
      } else {
        urlArg = " -startupUrl " + startupUrl + " " + m_moduleName;
      }
    }
    // other flags
    String flags = "";
    if (configuration.getAttribute(Constants.LAUNCH_ATTR_NO_SERVER, false)) {
      flags += " -noserver";
    }
    flags = addIfPresent(flags, configuration, Constants.LAUNCH_ATTR_PORT, "-port");
    flags = addIfPresent(flags, configuration, Constants.LAUNCH_ATTR_WHITE_LIST, "-whitelist");
    flags = addIfPresent(flags, configuration, Constants.LAUNCH_ATTR_BLACK_LIST, "-blacklist");
    flags = addSharedArguments(flags, configuration);
    // done
    return super.getProgramArguments(configuration) + flags + urlArg;
  }
}
