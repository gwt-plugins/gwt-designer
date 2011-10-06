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

import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.model.web.WebUtils;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider.ModuleModification;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * Operation for configuring GWT module for using Ext-GWT.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.actions
 */
public final class ConfigureExtGwtOperation extends WorkspaceModifyOperation {
  private final ModuleDescription m_module;
  private final IJavaProject m_javaProject;
  private final String m_libraryLocation;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param libraryLocation
   *          path to the folder with "gxt.jar" file.
   */
  public ConfigureExtGwtOperation(ModuleDescription module, String libraryLocation) {
    m_module = module;
    m_libraryLocation = libraryLocation;
    m_javaProject = JavaCore.create(m_module.getProject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
      InterruptedException {
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        execute0();
      }
    });
  }

  private void execute0() throws Exception {
    // ensure jar
    if (!ProjectUtils.hasType(m_javaProject, "com.extjs.gxt.ui.client.widget.Component")) {
      String jarPath = getJarPath(m_libraryLocation);
      if (EnvironmentUtils.isTestingTime()) {
        ProjectUtils.addExternalJar(m_javaProject, jarPath, null);
      } else {
        ProjectUtils.addJar(m_javaProject, jarPath, null);
      }
    }
    // copy resources
    {
      File resourcesSourceDir = new File(new File(m_libraryLocation), "resources");
      IFolder webFolder = WebUtils.getWebFolder(m_javaProject);
      File webDir = webFolder.getLocation().toFile();
      File resourceTargetDir = new File(webDir, "ExtGWT");
      FileUtils.copyDirectory(resourcesSourceDir, resourceTargetDir);
      webFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
    }
    // add elements into module
    DefaultModuleProvider.modify(m_module, new ModuleModification() {
      public void modify(ModuleElement moduleElement) throws Exception {
        moduleElement.addInheritsElement("com.extjs.gxt.ui.GXT");
      }
    });
    // add "stylesheet" into HTML
    {
      IFile htmlFile = Utils.getHTMLFile(m_module);
      if (htmlFile != null) {
        String content = IOUtils2.readString(htmlFile);
        int linkIndex = content.indexOf("<link type=");
        if (linkIndex != -1) {
          String prefix = StringUtilities.getLinePrefix(content, linkIndex);
          String GXTStylesheet =
              "<link type=\"text/css\" rel=\"stylesheet\" href=\"ExtGWT/css/gxt-all.css\">";
          content =
              content.substring(0, linkIndex)
                  + GXTStylesheet
                  + SystemUtils.LINE_SEPARATOR
                  + prefix
                  + content.substring(linkIndex);
          IOUtils2.setFileContents(htmlFile, content);
        }
      }
    }
  }

  static String getJarPath(String libraryLocation) {
    File libraryFolder = new File(libraryLocation);
    for (String jarName : libraryFolder.list()) {
      String jarNameLower = jarName.toLowerCase();
      if (jarNameLower.startsWith("gxt") && jarNameLower.endsWith(".jar")) {
        if (jarNameLower.endsWith("-gwt22.jar")) {
          return libraryLocation + "/" + jarName;
        }
      }
    }
    return libraryLocation + "/gxt.jar";
  }
}