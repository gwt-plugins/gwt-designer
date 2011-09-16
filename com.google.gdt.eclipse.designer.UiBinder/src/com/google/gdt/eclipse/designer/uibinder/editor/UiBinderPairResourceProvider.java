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
package com.google.gdt.eclipse.designer.uibinder.editor;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.xml.editor.actions.IPairResourceProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

import org.apache.commons.lang.StringUtils;

/**
 * {@link IPairResourceProvider} for GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.editor
 */
public final class UiBinderPairResourceProvider implements IPairResourceProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IPairResourceProvider INSTANCE = new UiBinderPairResourceProvider();

  private UiBinderPairResourceProvider() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPairResourceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public IFile getPair(final IFile file) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<IFile>() {
      public IFile runObject() throws Exception {
        String fileName = file.getName();
        if (StringUtils.endsWithIgnoreCase(fileName, ".ui.xml")) {
          return getJavaFile(file);
        }
        if (StringUtils.endsWithIgnoreCase(fileName, ".java")) {
          return getUIFile(file);
        }
        return null;
      }
    }, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ui.xml -> Java
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the Java {@link IFile} for given ui.xml one.
   */
  private IFile getJavaFile(IFile uiFile) throws Exception {
    IProject project = uiFile.getProject();
    IJavaProject javaProject = JavaCore.create(project);
    // try to find Java file in same package
    {
      IFolder folder = (IFolder) uiFile.getParent();
      IPackageFragment packageFragment = (IPackageFragment) JavaCore.create(folder);
      // find IType
      String formName = StringUtils.removeEndIgnoreCase(uiFile.getName(), ".ui.xml");
      String typeName = packageFragment.getElementName() + "." + formName;
      IType type = javaProject.findType(typeName);
      if (type != null) {
        return (IFile) type.getCompilationUnit().getUnderlyingResource();
      }
    }
    // no Java file
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Java -> ui.xml
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the ui.xml {@link IFile} for given Java one.
   */
  private IFile getUIFile(IFile javaFile) throws Exception {
    IProject project = javaFile.getProject();
    IJavaProject javaProject = JavaCore.create(project);
    // prepare form name
    String javaFormName = StringUtils.removeEnd(javaFile.getName(), ".java");
    // prepare package name
    String packageName;
    {
      IFolder folder = (IFolder) javaFile.getParent();
      IPackageFragment packageFragmentJava = (IPackageFragment) JavaCore.create(folder);
      packageName = packageFragmentJava.getElementName();
    }
    // try to find ui.xml file in package with same name
    for (IPackageFragmentRoot packageFragmentRoot : javaProject.getPackageFragmentRoots()) {
      IPackageFragment packageFragment = packageFragmentRoot.getPackageFragment(packageName);
      if (packageFragment.exists()) {
        for (Object object : packageFragment.getNonJavaResources()) {
          if (object instanceof IFile) {
            IFile uiFile = (IFile) object;
            String uiFileName = uiFile.getName();
            if (StringUtils.endsWithIgnoreCase(uiFileName, ".ui.xml")) {
              String uiFormName = StringUtils.removeEndIgnoreCase(uiFileName, ".ui.xml");
              if (uiFormName.equals(javaFormName)) {
                return uiFile;
              }
            }
          }
        }
      }
    }
    // no ui.xml file
    return null;
  }
}
