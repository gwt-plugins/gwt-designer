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

import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 
 * @author lobas_av
 * @coverage gwt.launch
 */
public class JUnitGwtLaunchConfigurationDelegate extends JUnitLaunchConfigurationDelegate {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Class path
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {
    String[] classpath = getClasspathAll(configuration);
    // remove patch from launch class path
    for (int i = 0; i < classpath.length; i++) {
      String element = classpath[i];
      if (element.indexOf("gwt-user-patch.jar") != -1) {
        classpath = (String[]) ArrayUtils.remove(classpath, i);
        break;
      }
    }
    return classpath;
  }

  private String[] getClasspathAll(ILaunchConfiguration configuration) throws CoreException {
    List<String> entries = new ArrayList<String>();
    // add source folders
    IProject project;
    {
      String projectName =
          configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
      project = Utils.getProject(projectName);
      AbstractGwtLaunchConfigurationDelegate.addSourceFolders(
          new HashSet<IProject>(),
          entries,
          project);
    }
    // add usual project classpath entries
    // we add them after source folders because GWT Shell compiles sources itself
    {
      String[] originalEntries = super.getClasspath(configuration);
      CollectionUtils.addAll(entries, originalEntries);
    }
    // add GWT DEV jar
    entries.add(Utils.getDevLibPath(project).toPortableString());
    //
    return entries.toArray(new String[entries.size()]);
  }
}