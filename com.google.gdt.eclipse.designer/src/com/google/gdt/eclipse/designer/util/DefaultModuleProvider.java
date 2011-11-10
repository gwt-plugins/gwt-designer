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
package com.google.gdt.eclipse.designer.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.model.module.GwtDocumentEditContext;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import java.util.List;

/**
 * {@link IModuleProvider} for module {@link IFile}.
 * 
 * @author scheglov
 * @coverage gwt.util
 */
public final class DefaultModuleProvider implements IModuleProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IModuleProvider INSTANCE = new DefaultModuleProvider();

  private DefaultModuleProvider() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  public static interface ModuleModification {
    void modify(ModuleElement moduleElement) throws Exception;
  }

  public static void modify(ModuleDescription moduleDescription, ModuleModification modification)
      throws Exception {
    if (moduleDescription instanceof DefaultModuleDescription) {
      IFile moduleFile = ((DefaultModuleDescription) moduleDescription).getFile();
      GwtDocumentEditContext context = new GwtDocumentEditContext(moduleFile);
      try {
        ModuleElement moduleElement = context.getModuleElement();
        modification.modify(moduleElement);
        context.commit();
      } finally {
        context.disconnect();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IModuleProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public ModuleDescription getModuleDescription(IJavaProject javaProject, String id)
      throws Exception {
    IFile moduleFile = getModuleFile(javaProject, id);
    return moduleFile != null ? new DefaultModuleDescription(moduleFile) : null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public ModuleDescription getExactModule(Object object) {
    if (object instanceof IFile) {
      IFile moduleFile = (IFile) object;
      if (isModuleFile(moduleFile)) {
        return new DefaultModuleDescription(moduleFile);
      }
    }
    return null;
  }

  public List<ModuleDescription> getModules(IJavaProject javaProject) throws Exception {
    List<IFile> moduleFiles = getModuleFiles(javaProject);
    return toModuleDescriptions(moduleFiles);
  }

  public List<ModuleDescription> getModules(IResource resource) throws Exception {
    List<IFile> moduleFiles = getModuleFiles(resource);
    return toModuleDescriptions(moduleFiles);
  }

  /**
   * @return the {@link ModuleDescription}s for given module {@link IFile}s.
   */
  private static List<ModuleDescription> toModuleDescriptions(List<IFile> moduleFiles) {
    List<ModuleDescription> modules = Lists.newArrayList();
    for (IFile moduleFile : moduleFiles) {
      modules.add(new DefaultModuleDescription(moduleFile));
    }
    return modules;
  }

  /**
   * @return the module {@link IFile}s to which belongs given {@link IResource}, may be
   *         <code>null</code> if no module found.
   */
  private static List<IFile> getModuleFiles(IResource resource) throws Exception {
    // try Java packages
    {
      List<IFile> modules = getModuleFiles_java(resource);
      if (!modules.isEmpty()) {
        return modules;
      }
    }
    // use resources
    return getModuleFiles_resource(resource);
  }

  /**
   * @return the module files to which belongs given {@link IResource}, may be <code>null</code> if
   *         no module found. Climbs up by {@link IPackageFragment}'s hierarchy, good for (Case
   *         33265), i.e. Maven-like source folder structure.
   */
  private static List<IFile> getModuleFiles_java(IResource resource) throws Exception {
    IProject project = resource.getProject();
    IJavaProject javaProject = JavaCore.create(project);
    // prepare package name
    String packageName;
    {
      // prepare folder
      IFolder folder;
      if (resource instanceof IFolder) {
        folder = (IFolder) resource;
      } else if (resource.getParent() instanceof IFolder) {
        folder = (IFolder) resource.getParent();
      } else {
        return ImmutableList.of();
      }
      // prepare package fragment
      IJavaElement javaElement = JavaCore.create(folder);
      if (javaElement instanceof IPackageFragment) {
        IPackageFragment pkgFragment = (IPackageFragment) javaElement;
        packageName = pkgFragment.getElementName();
      } else {
        return ImmutableList.of();
      }
    }
    // prepare source folders
    List<IPackageFragmentRoot> sourceFolders = Lists.newArrayList();
    for (IPackageFragmentRoot packageFragmentRoot : javaProject.getPackageFragmentRoots()) {
      if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
        sourceFolders.add(packageFragmentRoot);
      }
    }
    // search in this package and packages above
    for (; packageName.length() != 0; packageName = CodeUtils.getPackage(packageName)) {
      for (IPackageFragmentRoot packageFragmentRoot : sourceFolders) {
        IPackageFragment packageFragment = packageFragmentRoot.getPackageFragment(packageName);
        if (packageFragment.exists()) {
          IResource underlyingResource = packageFragment.getUnderlyingResource();
          if (underlyingResource instanceof IFolder) {
            IFolder folder = (IFolder) underlyingResource;
            List<IFile> moduleFiles = getModuleFiles(folder, false);
            if (!moduleFiles.isEmpty()) {
              return moduleFiles;
            }
          }
        }
      }
    }
    // no modules
    return ImmutableList.of();
  }

  /**
   * @return file files for given {@link IResource} inside of module, or <code>null</code> is no
   *         module found. Climbs up by {@link IFolder}'s hierarchy, good for case when module file
   *         is not in any {@link IPackageFragment}.
   */
  private static List<IFile> getModuleFiles_resource(IResource resource) throws Exception {
    // prepare folder
    IFolder folder;
    if (resource instanceof IFolder) {
      folder = (IFolder) resource;
    } else if (resource.getParent() instanceof IFolder) {
      folder = (IFolder) resource.getParent();
    } else {
      return ImmutableList.of();
    }
    // get first module file in folder
    return getModuleFilesUp(folder);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Module file utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public static boolean isModuleFile(IFile file) {
    return file.getName().endsWith(Constants.GWT_XML_EXT);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Module files searching
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the module {@link IFile} with given id, may be <code>null</code>.
   */
  private static IFile getModuleFile(IJavaProject javaProject, String moduleId) throws Exception {
    String moduleFileName = moduleId.replace('.', '/') + ".gwt.xml";
    for (IPackageFragmentRoot packageFragmentRoot : javaProject.getPackageFragmentRoots()) {
      // check only in source folders
      if (packageFragmentRoot.getKind() != IPackageFragmentRoot.K_SOURCE) {
        continue;
      }
      // check IFolder of source folder
      IContainer sourceFolder = (IContainer) packageFragmentRoot.getUnderlyingResource();
      IFile moduleFile = sourceFolder.getFile(new Path(moduleFileName));
      if (moduleFile.exists()) {
        return moduleFile;
      }
    }
    // not found
    return null;
  }

  /**
   * @return all module files in given {@link IJavaProject}.
   */
  private static List<IFile> getModuleFiles(IJavaProject javaProject) throws Exception {
    List<IFile> moduleFiles = Lists.newArrayList();
    for (IPackageFragmentRoot packageFragmentRoot : javaProject.getPackageFragmentRoots()) {
      // check only in source folders
      if (packageFragmentRoot.getKind() != IPackageFragmentRoot.K_SOURCE) {
        continue;
      }
      // check packages
      for (IJavaElement rootChild : packageFragmentRoot.getChildren()) {
        // check only packages
        if (rootChild instanceof IPackageFragment) {
          IPackageFragment pkg = (IPackageFragment) rootChild;
          for (Object object : pkg.getNonJavaResources()) {
            // check only files
            if (object instanceof IFile) {
              IFile file = (IFile) object;
              // check that file is GWT module file
              if (isModuleFile(file)) {
                moduleFiles.add(file);
              }
            }
          }
        }
      }
    }
    return moduleFiles;
  }

  /**
   * @return all module files in given {@link IFolder}.
   */
  private static List<IFile> getModuleFiles(IFolder folder, boolean searchInChildren)
      throws Exception {
    List<IFile> moduleFiles = Lists.newArrayList();
    //
    if (folder != null) {
      for (IResource resource : folder.members()) {
        if (resource instanceof IFile) {
          IFile file = (IFile) resource;
          if (isModuleFile(file)) {
            moduleFiles.add(file);
          }
        } else if (searchInChildren && resource instanceof IFolder) {
          moduleFiles.addAll(getModuleFiles((IFolder) resource, searchInChildren));
        }
      }
    }
    //
    return moduleFiles;
  }

  /**
   * Get module files in given {@link IFolder} or any {@link IFolder}'s above in hierarchy. It will
   * stop after finding first {@link IFolder} with at least one module file.
   * <p>
   * We use this method to find module file for some object inside of module packages/folders
   * structure.
   * 
   * @return found module files, may be empty {@link List}.
   */
  private static List<IFile> getModuleFilesUp(IFolder folder) throws Exception {
    IResource folderResource = folder;
    while (folderResource instanceof IFolder) {
      List<IFile> moduleFiles = getModuleFiles((IFolder) folderResource, false);
      if (!moduleFiles.isEmpty()) {
        return moduleFiles;
      }
      folderResource = folderResource.getParent();
    }
    // no module files
    return ImmutableList.of();
  }
}
