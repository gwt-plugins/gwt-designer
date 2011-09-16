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

import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.model.module.PublicElement;
import com.google.gdt.eclipse.designer.util.resources.IResourcesProvider;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;

import java.io.InputStream;
import java.util.List;

public abstract class ModuleDescription
    implements
      com.google.gdt.eclipse.designer.hosted.IModuleDescription {
  // brought down from IFile
  public abstract InputStream getContents() throws Exception;

  /**
   * @return the {@link IResourcesProvider} implementation to read "source" resources.
   */
  public abstract IResourcesProvider getResourcesProvider() throws Exception;

  public abstract IProject getProject();

  public IJavaProject getJavaProject() {
    return JavaCore.create(getProject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the fully qualified ID of module.
   */
  public abstract String getId();

  /**
   * @return the short, unqualified name of the module.
   */
  public abstract String getSimpleName();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Module packages/folders access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IFolder} that contains given module.
   */
  public abstract IFolder getModuleFolder();

  /**
   * @return the {@link IFolder} of some "public" folder of GWT module.
   */
  public IFolder getModulePublicFolder() throws Exception {
    // prepare "public" path relative module 
    String path;
    {
      ModuleElement moduleElement = Utils.readModule(this);
      List<PublicElement> publicElements = moduleElement.getPublicElements();
      if (!publicElements.isEmpty()) {
        path = publicElements.get(0).getPath();
      } else {
        path = "public";
      }
    }
    // convert into IFolder
    IFolder moduleFolder = getModuleFolder();
    return moduleFolder.getFolder(path);
  }

  /**
   * @return the {@link IPackageFragment} that contains given module file.
   */
  public IPackageFragment getModulePackage() throws CoreException {
    IFolder moduleFolder = getModuleFolder();
    return (IPackageFragment) JavaCore.create(moduleFolder);
  }
}
