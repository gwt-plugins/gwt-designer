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
package com.google.gdt.eclipse.designer.wizards.model.module;

import com.google.gdt.eclipse.designer.model.web.WebUtils;
import com.google.gdt.eclipse.designer.wizards.model.common.AbstractCreateOperation;
import com.google.gdt.eclipse.designer.wizards.model.module.CreateEntryPointOperation.EntryPointConfiguration;
import com.google.gdt.eclipse.designer.wizards.model.module.CreateEntryPointOperationPre21.EntryPointPre21Configuration;
import com.google.gdt.eclipse.designer.wizards.model.mvp.CreateViewOperation.ViewConfiguration;

import org.eclipse.wb.core.editor.IDesignerEditor;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.util.WorkspaceUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.ui.ide.IDE;

import java.util.HashMap;
import java.util.Map;

/**
 * Operation for creating new GWT module, for GWT 1.6+.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage gwt.wizard.operation
 */
public class CreateModuleOperation extends AbstractCreateOperation {
  protected final IPackageFragmentRoot root;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreateModuleOperation(IPackageFragmentRoot root) {
    this.root = root;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuration
  //
  ////////////////////////////////////////////////////////////////////////////
  public static abstract class ModuleConfiguration {
    public abstract String getPackageName();

    public abstract String getModuleName();

    public abstract boolean isCreateEntryPoint();

    public abstract EntryPointConfiguration getEntryPointConfiguration();

    public Map<String, String> getVariables() {
      Map<String, String> variables = new HashMap<String, String>();
      variables.put("basePackageName", getPackageName());
      variables.put("moduleName", getModuleName());
      {
        // add EntryPoint variables
        EntryPointPre21Configuration entryPointConfiguration = getEntryPointConfiguration();
        if (entryPointConfiguration != null) {
          variables.putAll(entryPointConfiguration.getVariables());
        }
      }
      return variables;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation 
  //
  ////////////////////////////////////////////////////////////////////////////
  public IFile create(ModuleConfiguration configuration) throws Exception {
    String moduleName = configuration.getModuleName();
    String packageName = configuration.getPackageName();
    // create packages
    IPackageFragment packageFragment = getPackage(root, packageName);
    getPackage(root, packageName + ".client");
    getPackage(root, packageName + ".server");
    // 
    // create folders
    IJavaProject javaProject = packageFragment.getJavaProject();
    IProject project = javaProject.getProject();
    String webFolderName = WebUtils.getWebFolderName(project);
    IFolder webFolder = project.getFolder(webFolderName);
    IFolder webInfFolder = project.getFolder(webFolderName + "/WEB-INF");
    // create module
    IFile file;
    if (configuration.isCreateEntryPoint()) {
      // prepare variables
      Map<String, String> variables = configuration.getVariables();
      variables.put("packageName", packageName);
      variables.put("className", moduleName);
      // prepare 'bootstrap' variable
      String bootstrapPrefix = packageName + "." + moduleName;
      variables.put("bootstrap", bootstrapPrefix + "/" + bootstrapPrefix + ".nocache.js");
      // create module
      file =
          createFileFromTemplate(
              packageFragment,
              moduleName + ".gwt.xml",
              "Module.gwt.xml",
              variables);
      // create EntryPoint
      CreateEntryPointOperation entryPointOperation = new CreateEntryPointOperation(root);
      entryPointOperation.create(configuration.getEntryPointConfiguration());
      // create files from templates
      createFileFromTemplate(webFolder, moduleName + ".html", "Module.html", variables);
      createFileFromTemplate(webFolder, moduleName + ".css", "Module.css", variables);
      copyTemplateFiles(webFolder, "images");
      // configure web.xml
      if (!webInfFolder.getFile("web.xml").exists()) {
        variables.put("welcome-file-name", moduleName);
        createFileFromTemplate(webInfFolder, "web.xml", "web.xml", variables);
      }
      // open entry point in editor
      {
        String qualifiedEntryPoint = packageName + ".client." + moduleName;
        IType type = WorkspaceUtils.waitForType(root.getJavaProject(), qualifiedEntryPoint);
        IDE.openEditor(
            DesignerPlugin.getActivePage(),
            (IFile) type.getUnderlyingResource(),
            IDesignerEditor.ID);
      }
    } else {
      // create empty module
      file =
          createFile(
              packageFragment,
              moduleName + ".gwt.xml",
              "<module>\r\n\t<inherits name=\"com.google.gwt.user.User\"/>\r\n</module>");
    }
    return file;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utilities 
  //
  ////////////////////////////////////////////////////////////////////////////
  public static IFile create(final IPackageFragmentRoot root,
      final String packageName,
      final String moduleName,
      final boolean isCreateEntryPoint,
      final boolean isMvpEntryPoint,
      final boolean isMvpViewJavaTemplate) throws Exception {
    final String VIEW_NAME = "Sample";
    final String clientPackageName = packageName + ".client";
    final ViewConfiguration viewConfiguration = new ViewConfiguration() {
      @Override
      public String getViewPackageName() {
        return clientPackageName + ".ui";
      }

      @Override
      public String getViewName() {
        return VIEW_NAME + "View";
      }

      @Override
      public boolean isUseJavaTemplate() {
        return isMvpViewJavaTemplate;
      }

      @Override
      public String getPlacePackageName() {
        return clientPackageName + ".place";
      }

      @Override
      public String getPlaceName() {
        return VIEW_NAME + "Place";
      }

      @Override
      public String getActivityPackageName() {
        return clientPackageName + ".activity";
      }

      @Override
      public String getActivityName() {
        return VIEW_NAME + "Activity";
      }

      @Override
      public String getClientFactoryPackageName() {
        return clientPackageName;
      }

      @Override
      public String getClientFactoryName() {
        return "ClientFactory";
      }

      @Override
      public Map<String, String> getVariables() {
        Map<String, String> variables = super.getVariables();
        variables.put("basePackageName", packageName);
        variables.put("entryPointPackageName", clientPackageName);
        return variables;
      }
    };
    final EntryPointConfiguration entryPointConfiguration = new EntryPointConfiguration() {
      @Override
      public String getPackageName() {
        return clientPackageName;
      }

      @Override
      public String getEntryPointName() {
        return moduleName;
      }

      @Override
      public boolean isUseMvp() {
        return isMvpEntryPoint;
      }

      @Override
      public ViewConfiguration getViewConfiguration() {
        return viewConfiguration;
      }

      @Override
      public String getMappersPackageName() {
        return clientPackageName + ".mvp";
      }

      @Override
      public String getClientFactoryPackageName() {
        return getViewConfiguration().getClientFactoryPackageName();
      }

      @Override
      public String getClientFactoryName() {
        return getViewConfiguration().getClientFactoryName();
      }
    };
    ModuleConfiguration moduleConfiguration = new ModuleConfiguration() {
      @Override
      public String getPackageName() {
        return packageName;
      }

      @Override
      public String getModuleName() {
        return moduleName;
      }

      @Override
      public boolean isCreateEntryPoint() {
        return isCreateEntryPoint;
      }

      @Override
      public EntryPointConfiguration getEntryPointConfiguration() {
        return entryPointConfiguration;
      }
    };
    CreateModuleOperation operation = new CreateModuleOperation(root);
    return operation.create(moduleConfiguration);
  }
}
