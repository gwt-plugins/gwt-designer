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
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;

import org.apache.commons.lang.StringUtils;

/**
 * @author lobas_av
 * @coverage gwt.launch
 */
public class GwtCompilerLaunchConfigurationDelegate extends AbstractGwtLaunchConfigurationDelegate {
  @Override
  public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
    String arguments = super.getProgramArguments(configuration);
    arguments = addSharedArguments(arguments, configuration);
    arguments = addIfPresent(arguments, configuration, Constants.LAUNCH_ATTR_STYLE, "-style");
    arguments += " " + configuration.getAttribute(Constants.LAUNCH_ATTR_MODULE, (String) null);
    return arguments;
  }

  @Override
  public void launch(final ILaunchConfiguration configuration,
      String mode,
      final ILaunch launch,
      IProgressMonitor monitor) throws CoreException {
    DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
      public void handleDebugEvents(DebugEvent[] events) {
        for (int i = 0; i < events.length; i++) {
          DebugEvent event = events[i];
          Object source = event.getSource();
          // check event type
          if (event.getKind() == DebugEvent.TERMINATE && source instanceof IProcess) {
            IProcess process = (IProcess) source;
            // check process
            if (launch == process.getLaunch()) {
              // remove debug events listener
              DebugPlugin.getDefault().removeDebugEventListener(this);
              handleRefreshResult(configuration);
              return;
            }
          }
        }
      }
    });
    super.launch(configuration, mode, launch, monitor);
  }

  protected final void handleRefreshResult(ILaunchConfiguration configuration) {
    try {
      String outPath = configuration.getAttribute("-out", "");
      if (StringUtils.isEmpty(outPath)) {
        outPath = configuration.getAttribute(Constants.LAUNCH_ATTR_MODULE, (String) null);
      }
      String projectName = configuration.getAttribute(Constants.LAUNCH_ATTR_PROJECT, (String) null);
      IProject project = Utils.getJavaModel().getJavaProject(projectName).getProject();
      IContainer refreshFolder = project.getFolder(outPath).getParent();
      if (refreshFolder.exists() && refreshFolder.getProject() == project) {
        refreshFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
      }
    } catch (Throwable e) {
    }
  }

  @Override
  public String getMainTypeName(ILaunchConfiguration configuration) throws CoreException {
    return Constants.GWT_COMPILER_CLASS;
  }
}