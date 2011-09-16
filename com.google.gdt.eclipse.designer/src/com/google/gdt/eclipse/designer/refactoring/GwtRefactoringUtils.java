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
package com.google.gdt.eclipse.designer.refactoring;

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.model.module.EntryPointElement;
import com.google.gdt.eclipse.designer.model.module.GwtDocumentEditContext;
import com.google.gdt.eclipse.designer.model.module.ServletElement;
import com.google.gdt.eclipse.designer.model.web.WebDocumentEditContext;
import com.google.gdt.eclipse.designer.model.web.WebUtils;
import com.google.gdt.eclipse.designer.util.DefaultModuleDescription;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.refactoring.RefactoringUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.DocumentModelVisitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.Change;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * GWT refactoring utilities.
 * 
 * @author scheglov_ke
 * @coverage gwt.refactoring.participants
 */
public class GwtRefactoringUtils extends RefactoringUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // GWT module refactoring utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link Change} for changing entry point from existing {@link IType} to type with new
   *         name.
   */
  public static Change module_replaceEntryPoint(IType type, final String newTypeName)
      throws Exception {
    final String oldTypeName = type.getFullyQualifiedName();
    return modifyModule(type, new DocumentModelVisitor() {
      @Override
      public void endVisit(DocumentElement element) {
        if (element instanceof EntryPointElement) {
          EntryPointElement entryPointElement = (EntryPointElement) element;
          if (entryPointElement.getClassName().equals(oldTypeName)) {
            entryPointElement.setClassName(newTypeName);
          }
        }
      }
    });
  }

  /**
   * @return {@link Change} for changing servlet from existing {@link IType} to type with new name.
   */
  public static Change module_replaceServletClass(IType type, final String newTypeName)
      throws Exception {
    final String oldTypeName = type.getFullyQualifiedName();
    return modifyModule(type, new DocumentModelVisitor() {
      @Override
      public void endVisit(DocumentElement element) {
        if (element instanceof ServletElement) {
          ServletElement servletElement = (ServletElement) element;
          if (servletElement.getClassName().equals(oldTypeName)) {
            servletElement.setClassName(newTypeName);
          }
        }
      }
    });
  }

  /**
   * @return {@link Change} for changing path of servlet with given {@link IType} and path to new
   *         path.
   */
  public static Change module_replaceServletPath(IType type,
      final String oldPath,
      final String newPath) throws Exception {
    final String oldTypeName = type.getFullyQualifiedName();
    return modifyModule(type, new DocumentModelVisitor() {
      @Override
      public void endVisit(DocumentElement element) {
        if (element instanceof ServletElement) {
          ServletElement servletElement = (ServletElement) element;
          if (servletElement.getClassName().equals(oldTypeName)
              && servletElement.getPath().equals(oldPath)) {
            servletElement.setPath(newPath);
          }
        }
      }
    });
  }

  /**
   * @return {@link Change} for removing servlet with given {@link IType}.
   */
  public static Change module_removeServlet(IType type) throws Exception {
    final String typeName = type.getFullyQualifiedName();
    return modifyModule(type, new DocumentModelVisitor() {
      @Override
      public void endVisit(DocumentElement element) {
        if (element instanceof ServletElement) {
          ServletElement servletElement = (ServletElement) element;
          if (servletElement.getClassName().equals(typeName)) {
            servletElement.remove();
          }
        }
      }
    });
  }

  /**
   * @return {@link Change} for removing <code>EntryPoint</code> with given {@link IType}.
   */
  public static Change module_removeEntryPoint(IType type) throws Exception {
    final String typeName = type.getFullyQualifiedName();
    return modifyModule(type, new DocumentModelVisitor() {
      @Override
      public void endVisit(DocumentElement element) {
        if (element instanceof EntryPointElement) {
          EntryPointElement entryPointElement = (EntryPointElement) element;
          if (entryPointElement.getClassName().equals(typeName)) {
            entryPointElement.remove();
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // web.xml refactoring utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link Change} for changing servlet from existing {@link IType} to type with new name.
   */
  public static Change web_replaceServletClass(IType type, final String newTypeName)
      throws Exception {
    final String oldTypeName = type.getFullyQualifiedName();
    return modifyWeb(type, new DocumentModelVisitor() {
      @Override
      public void endVisit(DocumentElement element) {
        if (element instanceof com.google.gdt.eclipse.designer.model.web.ServletElement) {
          com.google.gdt.eclipse.designer.model.web.ServletElement servlet =
              (com.google.gdt.eclipse.designer.model.web.ServletElement) element;
          if (servlet.getClassName().equals(oldTypeName)) {
            servlet.setClassName(newTypeName);
          }
        }
      }
    });
  }

  /**
   * @return {@link Change} for changing name of servlet in "servlet" element.
   */
  public static Change web_replaceServletPath(IType type, final String oldName, final String newName)
      throws Exception {
    return modifyWeb(type, new DocumentModelVisitor() {
      @Override
      public void endVisit(DocumentElement element) {
        if (element instanceof com.google.gdt.eclipse.designer.model.web.ServletElement) {
          com.google.gdt.eclipse.designer.model.web.ServletElement servlet =
              (com.google.gdt.eclipse.designer.model.web.ServletElement) element;
          if (servlet.getName().equals(oldName)) {
            servlet.setName(newName);
          }
        }
        if (element instanceof com.google.gdt.eclipse.designer.model.web.ServletMappingElement) {
          com.google.gdt.eclipse.designer.model.web.ServletMappingElement mapping =
              (com.google.gdt.eclipse.designer.model.web.ServletMappingElement) element;
          if (mapping.getName().equals(oldName)) {
            mapping.setName(newName);
          }
          String pattern = mapping.getPattern();
          if (pattern.endsWith("/" + oldName)) {
            pattern = StringUtils.removeEnd(pattern, oldName) + newName;
            mapping.setPattern(pattern);
          }
        }
      }
    });
  }

  /**
   * @return {@link Change} for removing servlet with given {@link IType}.
   */
  public static Change web_removeServlet(IType type) throws Exception {
    final String typeName = type.getFullyQualifiedName();
    return modifyWeb(type, new DocumentModelVisitor() {
      Map<String, com.google.gdt.eclipse.designer.model.web.ServletMappingElement> mappingElements =
          Maps.newTreeMap();
      String servletNameToRemove;

      @Override
      public void endVisit(DocumentElement element) {
        if (element instanceof com.google.gdt.eclipse.designer.model.web.ServletElement) {
          com.google.gdt.eclipse.designer.model.web.ServletElement servlet =
              (com.google.gdt.eclipse.designer.model.web.ServletElement) element;
          if (servlet.getClassName().equals(typeName)) {
            servlet.remove();
            servletNameToRemove = servlet.getName();
            removeServletMapping();
          }
        }
        if (element instanceof com.google.gdt.eclipse.designer.model.web.ServletMappingElement) {
          com.google.gdt.eclipse.designer.model.web.ServletMappingElement mapping =
              (com.google.gdt.eclipse.designer.model.web.ServletMappingElement) element;
          mappingElements.put(mapping.getName(), mapping);
          removeServletMapping();
        }
      }

      private void removeServletMapping() {
        if (servletNameToRemove != null) {
          if (mappingElements.containsKey(servletNameToRemove)) {
            mappingElements.get(servletNameToRemove).remove();
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // XML change internal utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link Change} for modifications in module file done by {@link DocumentModelVisitor}.
   */
  private static Change modifyModule(IType type, DocumentModelVisitor visitor) throws Exception {
    ModuleDescription moduleDescription = Utils.getSingleModule(type);
    if (moduleDescription instanceof DefaultModuleDescription) {
      IFile moduleFile = ((DefaultModuleDescription) moduleDescription).getFile();
      GwtDocumentEditContext context = new GwtDocumentEditContext(moduleFile);
      return modifyXML(moduleFile, visitor, context);
    }
    return null;
  }

  /**
   * @return {@link Change} for modifications in web.xml file done by {@link DocumentModelVisitor}.
   */
  private static Change modifyWeb(IType type, DocumentModelVisitor visitor) throws Exception {
    IProject project = type.getCompilationUnit().getUnderlyingResource().getProject();
    String webFolderName = WebUtils.getWebFolderName(project);
    IFile webFile = project.getFile(webFolderName + "/WEB-INF/web.xml");
    if (webFile.exists()) {
      WebDocumentEditContext context = new WebDocumentEditContext(webFile);
      return modifyXML(webFile, visitor, context);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // RemoteService utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return "async" type for given RemoteService type, or <code>null</code>.
   */
  public static IType getServiceAsyncType(IType serviceType, IProgressMonitor pm)
      throws JavaModelException {
    String asyncTypeName = serviceType.getFullyQualifiedName() + "Async";
    return serviceType.getJavaProject().findType(asyncTypeName, pm);
  }

  /**
   * @return single "impl" type for given RemoteService type, or <code>null</code>.
   */
  public static IType getServiceImplType(IType serviceType, IProgressMonitor pm)
      throws JavaModelException {
    ITypeHierarchy hierarchy = serviceType.newTypeHierarchy(pm);
    IType[] subtypes = hierarchy.getAllSubtypes(serviceType);
    return subtypes.length == 1 ? subtypes[0] : null;
  }
}
