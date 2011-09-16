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
package com.google.gdt.eclipse.designer.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.util.resources.DefaultResourcesProvider;
import com.google.gdt.eclipse.designer.util.resources.IResourcesProvider;

import org.eclipse.wb.internal.core.utils.reflect.ProjectClassLoader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;

import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

public class DefaultModuleDescription extends ModuleDescription {
  private final IFile m_file;
  private List<String> m_locations;
  private URL[] m_urls;
  private ClassLoader m_classLoader;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DefaultModuleDescription(IFile moduleFile) {
    m_file = moduleFile;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  public IFile getFile() {
    return m_file;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getId() {
    String simpleName = getSimpleName();
    // try to find parent package (Case 26650)
    IJavaElement parentElement = JavaCore.create(m_file.getParent());
    if (parentElement instanceof IPackageFragment) {
      IPackageFragment pkg = (IPackageFragment) parentElement;
      return pkg.getElementName() + "." + simpleName;
    }
    // use simple name
    return simpleName;
  }

  @Override
  public String getSimpleName() {
    return StringUtils.chomp(m_file.getName(), Constants.GWT_XML_EXT);
  }

  @Override
  public InputStream getContents() throws Exception {
    return m_file.getContents(true);
  }

  @Override
  public IResourcesProvider getResourcesProvider() throws Exception {
    return new DefaultResourcesProvider(this);
  }

  @Override
  public IProject getProject() {
    return m_file.getProject();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Module packages/folders access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IFolder} that contains given module file.
   */
  @Override
  public IFolder getModuleFolder() {
    return (IFolder) m_file.getParent();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IModuleDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<String> getLocations() throws Exception {
    if (m_locations == null) {
      IProject project = getProject();
      IJavaProject javaProject = getJavaProject();
      m_locations = Lists.newArrayList();
      // add source locations
      {
        List<String> sourceLocations = Lists.newArrayList();
        ProjectClassLoader.addSourceLocations(Sets.<IProject>newHashSet(), sourceLocations, project);
        m_locations.addAll(sourceLocations);
      }
      // add binary locations
      {
        String[] classpath = ProjectClassLoader.getClasspath(javaProject);
        Collections.addAll(m_locations, classpath);
      }
      // keep output locations, because there may be Generator implementations
    }
    return m_locations;
  }

  public URL[] getURLs() throws Exception {
    if (m_urls == null) {
      List<String> locations = getLocations();
      m_urls = ProjectClassLoader.toURLs(locations);
    }
    return m_urls;
  }

  public ClassLoader getClassLoader() throws Exception {
    if (m_classLoader == null) {
      URL[] urls = getURLs();
      m_classLoader = new URLClassLoader(urls);
    }
    return m_classLoader;
  }
}
