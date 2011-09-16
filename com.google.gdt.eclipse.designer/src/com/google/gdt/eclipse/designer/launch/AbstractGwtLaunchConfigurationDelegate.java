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
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.Version;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract {@link LaunchConfigurationDelegate} for GWT.
 * 
 * @author scheglov_ke
 * @coverage gwt.launch
 */
public abstract class AbstractGwtLaunchConfigurationDelegate extends JavaLaunchDelegate {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final Version getVersion(ILaunchConfiguration configuration) throws CoreException {
    IJavaProject javaProject = getJavaProject(configuration);
    return Utils.getVersion(javaProject);
  }

  protected String addSharedArguments(String flags, ILaunchConfiguration configuration)
      throws CoreException {
    flags = addIfPresent(flags, configuration, Constants.LAUNCH_ATTR_LOG_LEVEL, "-logLevel");
    flags = addIfPresent(flags, configuration, Constants.LAUNCH_ATTR_DIR_GEN, "-gen");
    flags = addIfPresent(flags, configuration, Constants.LAUNCH_ATTR_DIR_WAR, "-war");
    return flags;
  }

  protected static String addIfPresent(String flags,
      ILaunchConfiguration configuration,
      String key,
      String flag) throws CoreException {
    String value = configuration.getAttribute(key, "").trim();
    if (!StringUtils.isEmpty(value)) {
      flags += " " + flag + " " + value;
    }
    return flags;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Class path
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {
    return getClasspathAll(configuration);
  }

  public String[] getClasspathAll(ILaunchConfiguration configuration) throws CoreException {
    List<String> entries = new ArrayList<String>();
    // add source folders
    IProject project;
    {
      String projectName =
          configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
      project = Utils.getProject(projectName);
      addSourceFolders(new HashSet<IProject>(), entries, project);
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

  /**
   * GWT requires source folders in classpath (because it has its own compiler), so we should add
   * all source folders for given project and its required projects.
   */
  public static void addSourceFolders(Set<IProject> visitedProjects,
      List<String> entries,
      IProject project) throws CoreException {
    // HACK: see above
    if (project.getName().endsWith("-design")) {
      return;
    }
    // check for recursion
    if (visitedProjects.contains(project)) {
      return;
    }
    visitedProjects.add(project);
    //
    IJavaProject javaProject = JavaCore.create(project);
    // add source folders for given project
    {
      IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
      for (int i = 0; i < packageFragmentRoots.length; i++) {
        IPackageFragmentRoot packageFragmentRoot = packageFragmentRoots[i];
        if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
          entries.add(packageFragmentRoot.getResource().getLocation().toPortableString());
        }
      }
    }
    // process required projects
    {
      IProject[] referencedProjects = project.getReferencedProjects();
      for (int i = 0; i < referencedProjects.length; i++) {
        IProject referencedProject = referencedProjects[i];
        addSourceFolders(visitedProjects, entries, referencedProject);
      }
    }
  }
}
