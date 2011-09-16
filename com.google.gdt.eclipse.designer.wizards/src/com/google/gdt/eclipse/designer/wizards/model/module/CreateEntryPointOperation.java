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

import com.google.gdt.eclipse.designer.model.module.AbstractModuleElement;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider.ModuleModification;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.wizards.model.mvp.CreateViewOperation;
import com.google.gdt.eclipse.designer.wizards.model.mvp.CreateViewOperation.ViewConfiguration;
import com.google.gdt.eclipse.designer.wizards.model.mvp.ViewComposite;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import java.util.Map;

/**
 * Operation for creating new GWT EntryPoint, for GWT 2.1+.
 * 
 * @author sablin_aa
 * @coverage gwt.wizard.operation
 */
public class CreateEntryPointOperation extends CreateEntryPointOperationPre21 {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreateEntryPointOperation(IPackageFragmentRoot root) {
    super(root);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuration
  //
  ////////////////////////////////////////////////////////////////////////////
  public static abstract class EntryPointConfiguration extends EntryPointPre21Configuration {
    public abstract boolean isUseMvp();

    public abstract ViewConfiguration getViewConfiguration();

    public abstract String getMappersPackageName();

    public String getClientFactoryPackageName() {
      return getPackageName();
    }

    public String getClientFactoryName() {
      return "ClientFactory";
    }

    @Override
    public Map<String, String> getVariables() {
      Map<String, String> variables = super.getVariables();
      variables.put("entryPointPackageName", getPackageName());
      variables.put("mappersPackageName", getMappersPackageName());
      variables.put("clientFactoryPackageName", getClientFactoryPackageName());
      variables.put("clientFactoryName", getClientFactoryName());
      {
        // add View variables
        ViewConfiguration viewConfiguration = getViewConfiguration();
        if (viewConfiguration != null) {
          variables.putAll(viewConfiguration.getVariables());
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
  /**
   * Create EntryPoint.
   */
  public IFile create(EntryPointConfiguration configuration) throws Exception {
    String className = configuration.getEntryPointName();
    String packageName = configuration.getPackageName();
    IPackageFragment packageFragment = getPackage(root, packageName);
    IFile file;
    if (Utils.supportMvp(packageFragment.getJavaProject()) && configuration.isUseMvp()) {
      // prepare variables
      Map<String, String> variables = configuration.getVariables();
      variables.put("className", className);
      // create files from MVP templates
      createMvpFactoryInterface(configuration);
      {
        // create View
        ViewConfiguration viewConfiguration = configuration.getViewConfiguration();
        CreateViewOperation viewOperation = new CreateViewOperation(root);
        viewOperation.create(viewConfiguration);
      }
      createMvpFactoryImplementation(configuration);
      createMvpMappers(configuration);
      // create EntryPoint
      file =
          createFileFromTemplate(packageFragment, className + ".java", "mvp/Module.java", variables);
    } else {
      // single java template
      file = super.create(configuration);
    }
    return file;
  }

  private void createMvpMappers(EntryPointConfiguration configuration) throws Exception {
    String packageName = configuration.getMappersPackageName();
    IPackageFragment packageFragment = getPackage(root, packageName);
    // prepare variables
    Map<String, String> variables = configuration.getVariables();
    // create ActivityMapper
    variables.put("className", "AppActivityMapper");
    variables.put("packageName", packageName);
    createFileFromTemplate(
        packageFragment,
        "AppActivityMapper.java",
        "mvp/AppActivityMapper.java",
        variables);
    // create HistoryMapper
    variables.put("className", "AppPlaceHistoryMapper");
    createFileFromTemplate(
        packageFragment,
        "AppPlaceHistoryMapper.java",
        "mvp/AppPlaceHistoryMapper.java",
        variables);
  }

  private IFile createMvpFactoryInterface(EntryPointConfiguration configuration) throws Exception {
    final String className = configuration.getClientFactoryName();
    final String packageName = configuration.getClientFactoryPackageName();
    IPackageFragment packageFragment = getPackage(root, packageName);
    // prepare variables
    Map<String, String> variables = configuration.getVariables();
    variables.put("className", className);
    variables.put("packageName", packageName);
    IFile file =
        createFileFromTemplate(
            packageFragment,
            className + ".java",
            "mvp/ClientFactory.java",
            variables);
    // store PersistentProperty for ClientFactory
    ViewComposite.setClientFactoryName(packageFragment, packageName + "." + className);
    return file;
  }

  private IFile createMvpFactoryImplementation(EntryPointConfiguration configuration)
      throws Exception {
    final String className = configuration.getClientFactoryName();
    final String packageName = configuration.getClientFactoryPackageName();
    IPackageFragment packageFragment = getPackage(root, packageName);
    // prepare variables
    Map<String, String> variables = configuration.getVariables();
    variables.put("className", className + "Impl");
    variables.put("packageName", packageName);
    IFile file =
        createFileFromTemplate(
            packageFragment,
            className + "Impl.java",
            "mvp/ClientFactoryImpl.java",
            variables);
    // add definition for factory to xxx.gwt.xml
    {
      ModuleDescription moduleDescription = Utils.getSingleModule(packageFragment);
      DefaultModuleProvider.modify(moduleDescription, new ModuleModification() {
        public void modify(ModuleElement moduleElement) throws Exception {
          AbstractModuleElement replaceElement = new AbstractModuleElement("replace-with");
          moduleElement.addChild(replaceElement);
          replaceElement.setAttribute("class", packageName + "." + className + "Impl");
          // 
          AbstractModuleElement whenElement = new AbstractModuleElement("when-type-is");
          replaceElement.addChild(whenElement);
          whenElement.setAttribute("class", packageName + "." + className);
        }
      });
    }
    return file;
  }
}
