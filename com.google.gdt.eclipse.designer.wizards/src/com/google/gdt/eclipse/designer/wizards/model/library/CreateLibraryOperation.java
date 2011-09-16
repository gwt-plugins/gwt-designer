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
package com.google.gdt.eclipse.designer.wizards.model.library;

import com.google.gdt.eclipse.designer.wizards.model.common.AbstractCreateOperation;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.ui.ide.IDE;

import java.util.HashMap;
import java.util.Map;

/**
 * Operation for creating new GWT library.
 * 
 * @author scheglov_ke
 * @coverage gwt.wizard.operation
 */
public class CreateLibraryOperation extends AbstractCreateOperation {
  private final IPackageFragmentRoot m_root;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreateLibraryOperation(IPackageFragmentRoot root) {
    m_root = root;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation 
  //
  ////////////////////////////////////////////////////////////////////////////
  public void create(String basePackageName,
      String moduleName,
      boolean createHTML,
      boolean createServerPackage) throws Exception {
    // create packages
    IPackageFragment basePackage = getPackage(m_root, basePackageName);
    getPackage(m_root, basePackageName + ".client");
    if (createServerPackage) {
      getPackage(m_root, basePackageName + ".server");
    }
    // create folders
    IFolder baseFolder = (IFolder) basePackage.getUnderlyingResource();
    IFolder publicFolder = baseFolder.getFolder("public");
    publicFolder.create(false, true, new NullProgressMonitor());
    // create module
    IFile moduleFile;
    {
      // prepare variables
      Map<String, String> variables = new HashMap<String, String>();
      variables.put("basePackage", basePackageName);
      variables.put("className", moduleName);
      // create files from templates
      moduleFile =
          createFileFromTemplate(basePackage, moduleName + ".gwt.xml", "Library.gwt.xml", variables);
      if (createHTML) {
        createFileFromTemplate(publicFolder, moduleName + ".html", "Library.html", variables);
      }
      createFileFromTemplate(publicFolder, moduleName + ".css", "Library.css", variables);
      copyTemplateFiles(publicFolder, "images");
    }
    // open *.gwt.xml file in editor
    IDE.openEditor(DesignerPlugin.getActivePage(), moduleFile);
  }
}
