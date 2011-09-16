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

import com.google.gdt.eclipse.designer.wizards.model.common.AbstractCreateOperation;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import java.util.HashMap;
import java.util.Map;

/**
 * Operation for creating new GWT EntryPoint.
 * 
 * @author sablin_aa
 * @coverage gwt.wizard.operation
 */
public class CreateEntryPointOperationPre21 extends AbstractCreateOperation {
  protected final IPackageFragmentRoot root;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreateEntryPointOperationPre21(IPackageFragmentRoot root) {
    this.root = root;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuration
  //
  ////////////////////////////////////////////////////////////////////////////
  public static abstract class EntryPointPre21Configuration {
    public abstract String getPackageName();

    public abstract String getEntryPointName();

    public Map<String, String> getVariables() {
      Map<String, String> variables = new HashMap<String, String>();
      variables.put("packageName", getPackageName());
      variables.put("entryPointName", getEntryPointName());
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
  public IFile create(EntryPointPre21Configuration configuration) throws Exception {
    String className = configuration.getEntryPointName();
    String packageName = configuration.getPackageName();
    IPackageFragment packageFragment = getPackage(root, packageName);
    // prepare variables
    Map<String, String> variables = configuration.getVariables();
    variables.put("className", className);
    // create files from templates
    return createFileFromTemplate(packageFragment, className + ".java", "Module.java", variables);
  }
}
