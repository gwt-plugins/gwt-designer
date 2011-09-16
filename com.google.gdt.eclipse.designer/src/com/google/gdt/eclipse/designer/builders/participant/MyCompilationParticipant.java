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
package com.google.gdt.eclipse.designer.builders.participant;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.ModuleVisitor;
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.util.resources.IResourcesProvider;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Compilation participant that check that all imported packages are "source" packages of some
 * inherited module.
 * 
 * @author scheglov_ke
 * @coverage gwt.compilation.participant
 */
public final class MyCompilationParticipant extends AbstractCompilationParticipant {
  public static boolean ENABLED = true;
  /**
   * If the <code>com.google.gdt.eclipse.designer.wizards</code> plug-in is present, then it
   * indicates that the user has the pre-GPE integrated version of GWT Designer.
   */
  public static boolean WIZARD_PLUGIN_PRESENT =
      Platform.getBundle("com.google.gdt.eclipse.designer.wizards") != null;
  public static final String MARKER_ID = "com.google.gdt.eclipse.designer.problem";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MyCompilationParticipant() {
    super(MARKER_ID);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Active check
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isActive(IJavaProject project) {
    return ENABLED && WIZARD_PLUGIN_PRESENT && Utils.isGWTProject(project);
  }

  @Override
  public boolean isAnnotationProcessor() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Compiling
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addMarkers(List<MarkerInfo> newMarkers,
      IFile file,
      ICompilationUnit modelUnit,
      CompilationUnit astUnit) throws Exception {
    // look if checking is enabled
    {
      IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
      if (!preferenceStore.getBoolean(Constants.P_BUILDER_CHECK_CLIENT_CLASSPATH)) {
        return;
      }
    }
    // check if unit is in source package
    if (!Utils.isModuleSourcePackage((IPackageFragment) modelUnit.getParent())) {
      return;
    }
    //
    {
      ModuleDescription moduleDescription = Utils.getSingleModule(file);
      if (moduleDescription != null) {
        // prepare document
        IDocument document;
        {
          String contents = IOUtils2.readString(file);
          document = new Document(contents);
        }
        // add error markers for not imported types
        IResourcesProvider resourcesProvider = moduleDescription.getResourcesProvider();
        try {
          addMarkers_notImportedTypes(
              newMarkers,
              resourcesProvider,
              moduleDescription,
              astUnit,
              file,
              document);
        } finally {
          resourcesProvider.dispose();
        }
      }
    }
  }

  /**
   * Adds error markers for types that are not visible in inherited "source" packages.
   */
  private void addMarkers_notImportedTypes(final List<MarkerInfo> newMarkers,
      final IResourcesProvider resourcesProvider,
      ModuleDescription moduleDescription,
      CompilationUnit astUnit,
      final IFile file,
      final IDocument document) throws Exception {
    final IJavaProject javaProject = JavaCore.create(file.getProject());
    // prepare list of source packages
    final List<SourcePackageDescriptor> sourcePackages = Lists.newArrayList();
    ModuleVisitor.accept(moduleDescription, new ModuleVisitor() {
      @Override
      public void visitSourcePackage(ModuleElement module, String packageName, boolean superSource)
          throws Exception {
        sourcePackages.add(new SourcePackageDescriptor(packageName, superSource));
      }
    });
    // validate all types in CompilationUnit
    astUnit.accept(new ASTVisitor() {
      private final Set<String> m_validClasses = Sets.newTreeSet();
      private final Set<String> m_invalidClasses = Sets.newTreeSet();

      @Override
      public boolean visit(SingleMemberAnnotation node) {
        return false;
      }

      @Override
      public boolean visit(NormalAnnotation node) {
        return false;
      }

      @Override
      public void postVisit(final ASTNode node) {
        ExecutionUtils.runIgnore(new RunnableEx() {
          public void run() throws Exception {
            postVisitEx(node);
          }
        });
      }

      private void postVisitEx(ASTNode node) throws Exception {
        // ignore imports
        if (AstNodeUtils.getEnclosingNode(node, ImportDeclaration.class) != null) {
          return;
        }
        // check known cases
        if (node instanceof SimpleType) {
          SimpleType simpleType = (SimpleType) node;
          ITypeBinding typeBinding = simpleType.resolveBinding();
          checkNode(node, typeBinding);
        } else if (node instanceof SimpleName) {
          SimpleName simpleName = (SimpleName) node;
          if (simpleName.resolveBinding().getKind() == IBinding.TYPE
              && node.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY) {
            ITypeBinding typeBinding = simpleName.resolveTypeBinding();
            checkNode(node, typeBinding);
          }
        }
      }

      private void checkNode(ASTNode node, ITypeBinding typeBinding) throws Exception {
        if (typeBinding != null) {
          // ignore generics type variable
          if (typeBinding.isTypeVariable()) {
            return;
          }
          // only top level types can be found as source
          while (typeBinding.getDeclaringClass() != null) {
            typeBinding = typeBinding.getDeclaringClass();
          }
          // check this type
          String typeName = AstNodeUtils.getFullyQualifiedName(typeBinding, true);
          if (isSecondarySourceType(typeName)) {
            return;
          }
          checkClass(node, typeName);
        }
      }

      private boolean isSecondarySourceType(String typeName) throws Exception {
        // usually secondary type can not be found using this way
        IType type = javaProject.findType(typeName);
        if (type == null) {
          return true;
        }
        // "secondary source type" has compilation unit
        ICompilationUnit compilationUnit = type.getCompilationUnit();
        if (compilationUnit == null) {
          return false;
        }
        // check if type name in same as unit name
        String unitName = compilationUnit.getElementName();
        unitName = StringUtils.removeEnd(unitName, ".java");
        return !typeName.endsWith("." + unitName);
      }

      /**
       * Check that class with given name is defined in this or inherited module.
       */
      private void checkClass(ASTNode node, String className) throws Exception {
        if (!isValid(className)) {
          markAsInvalid(node, className);
        }
      }

      /**
       * @return <code>true</code> if given class is valid.
       */
      private boolean isValid(String className) {
        // check cached valid classes
        if (m_validClasses.contains(className)) {
          return true;
        }
        // check cached invalid classes
        if (m_invalidClasses.contains(className)) {
          return false;
        }
        // no information in caches, do checks 
        for (SourcePackageDescriptor sourcePackageDescriptor : sourcePackages) {
          if (sourcePackageDescriptor.isValidClass(resourcesProvider, className)) {
            m_validClasses.add(className);
            return true;
          }
        }
        // mark as invalid
        m_invalidClasses.add(className);
        return false;
      }

      private void markAsInvalid(ASTNode node, String className) throws Exception {
        String message =
            className
                + " can not be found in source packages. "
                + "Check the inheritance chain from your module; "
                + "it may not be inheriting a required module or a module "
                + "may not be adding its source path entries properly.";
        String moduleNameToImport = getEnclosingModule(resourcesProvider, className);
        newMarkers.add(createMarkerInfo_importModule(
            file,
            document,
            node.getStartPosition(),
            node.getLength(),
            message,
            moduleNameToImport));
      }
    });
  }

  /**
   * @return the name of GWT module that contains given class, may be <code>null</code>.
   */
  private static String getEnclosingModule(IResourcesProvider resourcesProvider, String className)
      throws Exception {
    String packageName = CodeUtils.getPackage(className);
    while (packageName.length() != 0) {
      List<String> files = resourcesProvider.listFiles(packageName.replace('.', '/'));
      for (String file : files) {
        if (file.indexOf('/') == -1 && file.endsWith(".gwt.xml")) {
          String shortModuleName = StringUtils.substring(file, 0, -".gwt.xml".length());
          return packageName + "." + shortModuleName;
        }
      }
      // go up
      packageName = CodeUtils.getPackage(packageName);
    }
    return null;
  }

  private static MarkerInfo createMarkerInfo_importModule(IResource resource,
      IDocument document,
      int start,
      int length,
      String message,
      String moduleNameToImport) throws BadLocationException {
    int line = document.getLineOfOffset(start);
    return new MarkerInfoImportModule(resource,
        start,
        start + length,
        line,
        IMarker.SEVERITY_ERROR,
        message,
        moduleNameToImport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source package
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Descriptor for source package.
   */
  private static final class SourcePackageDescriptor {
    private final String m_packageName;
    private final boolean m_superSource;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SourcePackageDescriptor(String packageName, boolean superSource) {
      m_packageName = packageName;
      m_superSource = superSource;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Checks if class with given name exists in this package.
     */
    public boolean isValidClass(IResourcesProvider resourcesProvider, String className) {
      // prepare path to the source of class with given name
      String sourceFilePath;
      if (m_superSource) {
        sourceFilePath = (m_packageName + "." + className).replace('.', '/') + ".java";
      } else {
        if (!className.startsWith(m_packageName)) {
          return false;
        }
        sourceFilePath = className.replace('.', '/') + ".java";
      }
      // check that we can find source for class with given name
      InputStream resourceAsStream = null;
      try {
        resourceAsStream = resourcesProvider.getResourceAsStream(sourceFilePath);
        return resourceAsStream != null;
      } catch (Throwable e) {
        return false;
      } finally {
        IOUtils.closeQuietly(resourceAsStream);
      }
    }
  }
}
