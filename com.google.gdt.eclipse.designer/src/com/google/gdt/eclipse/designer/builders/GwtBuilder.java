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
package com.google.gdt.eclipse.designer.builders;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.builders.participant.MyCompilationParticipant;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.refactoring.GwtRefactoringUtils;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.actions.OverrideMethodsAction;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;

import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link IncrementalProjectBuilder} for building <code>RemoteSErvice</code> Async types.
 * 
 * @author scheglov_ke
 * @coverage gwt.builder
 */
public class GwtBuilder extends IncrementalProjectBuilder {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Build time fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private IResourceDelta m_delta;
  private List<IFile> m_serviceFiles;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Build
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  @SuppressWarnings("rawtypes")
  protected IProject[] build(int kind, Map args, final IProgressMonitor _monitor)
      throws CoreException {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        buildEx(_monitor);
      }
    });
    _monitor.done();
    return null;
  }

  private void buildEx(final IProgressMonitor _monitor) throws Exception {
    IProgressMonitor monitor = new SubProgressMonitor(_monitor, 3);
    // prepare state
    {
      IProject project = getProject();
      m_delta = getDelta(project);
    }
    // respond to delta
    if (m_delta != null) {
      if (Activator.getStore().getBoolean(Constants.P_BUILDER_GENERATE_ASYNC)) {
        monitor.beginTask("Building Remote Service's...", 2);
        //
        monitor.subTask("Looking for RemoteService Files");
        findRemoteServiceFiles();
        monitor.worked(1);
        //
        monitor.subTask("Updating RemoteService's Async and Impl parts");
        updateRemoteServices();
        monitor.worked(1);
      }
      {
        monitor.beginTask("Checking *.GWT.XML modifications...", 1);
        checkModuleFileModification();
        monitor.worked(1);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Async interfaces generation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Prepares {@link IFile}'s with <code>RemoveService</code> for current build type (incremental or
   * full).
   */
  private void findRemoteServiceFiles() throws CoreException {
    m_serviceFiles = Lists.newArrayList();
    // visit delta
    IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
      public boolean visit(IResourceDelta delta) throws CoreException {
        IResource resource = delta.getResource();
        switch (resource.getType()) {
          case IResource.PROJECT :
            IProject project = (IProject) resource;
            // we are interesting only in GWT projects
            return Utils.isGWTProject(project);
          case IResource.FILE :
            if (Utils.isRemoteService(resource)) {
              m_serviceFiles.add((IFile) resource);
            }
        }
        return true;
      }
    };
    m_delta.accept(visitor);
  }

  /**
   * Updates Async and Impl parts for each modified <code>RemoteService</code>.
   */
  private void updateRemoteServices() throws Exception {
    for (IFile serviceFile : m_serviceFiles) {
      ICompilationUnit serviceUnit = (ICompilationUnit) JavaCore.create(serviceFile);
      IPackageFragment servicePackage = (IPackageFragment) serviceUnit.getParent();
      //
      generateAsync(servicePackage, serviceUnit);
      updateImpl(servicePackage, serviceUnit);
    }
  }

  /**
   * Generates Async type for given <code>RemoteService</code>.
   */
  private void generateAsync(IPackageFragment servicePackage, ICompilationUnit serviceUnit)
      throws Exception {
    IJavaProject javaProject = serviceUnit.getJavaProject();
    // parse service unit
    CompilationUnit serviceRoot = Utils.parseUnit(serviceUnit);
    // prepare AST and start modifications recording
    AST ast = serviceRoot.getAST();
    serviceRoot.recordModifications();
    // modify imports (-com.google.gwt.*, -*Exception, +AsyncCallback) 
    {
      List<ImportDeclaration> imports = DomGenerics.imports(serviceRoot);
      // remove useless imports
      for (Iterator<ImportDeclaration> I = imports.iterator(); I.hasNext();) {
        ImportDeclaration importDeclaration = I.next();
        String importName = importDeclaration.getName().getFullyQualifiedName();
        if (importName.startsWith("com.google.gwt.user.client.rpc.")
            || importName.equals("com.google.gwt.core.client.GWT")
            || importName.endsWith("Exception")) {
          I.remove();
        }
      }
    }
    // add Async to the name
    TypeDeclaration serviceType = (TypeDeclaration) serviceRoot.types().get(0);
    String remoteServiceAsyncName = serviceType.getName().getIdentifier() + "Async";
    serviceType.setName(serviceRoot.getAST().newSimpleName(remoteServiceAsyncName));
    // update interfaces
    updateInterfacesOfAsync(javaProject, serviceRoot, serviceType);
    // change methods, fields and inner classes
    {
      List<BodyDeclaration> bodyDeclarations = DomGenerics.bodyDeclarations(serviceType);
      for (Iterator<BodyDeclaration> I = bodyDeclarations.iterator(); I.hasNext();) {
        BodyDeclaration bodyDeclaration = I.next();
        if (bodyDeclaration instanceof MethodDeclaration) {
          MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
          // make return type void
          Type returnType;
          {
            returnType = methodDeclaration.getReturnType2();
            methodDeclaration.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
          }
          // process JavaDoc
          {
            Javadoc javadoc = methodDeclaration.getJavadoc();
            if (javadoc != null) {
              List<TagElement> tags = DomGenerics.tags(javadoc);
              for (Iterator<TagElement> tagIter = tags.iterator(); tagIter.hasNext();) {
                TagElement tag = tagIter.next();
                if ("@gwt.typeArgs".equals(tag.getTagName())) {
                  tagIter.remove();
                } else if ("@return".equals(tag.getTagName())) {
                  if (!tag.fragments().isEmpty()) {
                    tag.setTagName("@param callback the callback to return");
                  } else {
                    tagIter.remove();
                  }
                } else if ("@wbp.gwt.Request".equals(tag.getTagName())) {
                  tagIter.remove();
                  addImport(serviceRoot, "com.google.gwt.http.client.Request");
                  methodDeclaration.setReturnType2(ast.newSimpleType(ast.newName("Request")));
                }
              }
              // remove empty JavaDoc
              if (tags.isEmpty()) {
                methodDeclaration.setJavadoc(null);
              }
            }
          }
          // add AsyncCallback parameter
          {
            addImport(serviceRoot, "com.google.gwt.user.client.rpc.AsyncCallback");
            // prepare "callback" type
            Type callbackType;
            {
              callbackType = ast.newSimpleType(ast.newName("AsyncCallback"));
              Type objectReturnType = getObjectType(returnType);
              ParameterizedType parameterizedType = ast.newParameterizedType(callbackType);
              DomGenerics.typeArguments(parameterizedType).add(objectReturnType);
              callbackType = parameterizedType;
            }
            // prepare "callback" parameter
            SingleVariableDeclaration asyncCallback = ast.newSingleVariableDeclaration();
            asyncCallback.setType(callbackType);
            asyncCallback.setName(ast.newSimpleName("callback"));
            // add "callback" parameter
            DomGenerics.parameters(methodDeclaration).add(asyncCallback);
          }
          // remove throws
          methodDeclaration.thrownExceptions().clear();
        } else if (bodyDeclaration instanceof FieldDeclaration
            || bodyDeclaration instanceof TypeDeclaration) {
          // remove the fields and inner classes
          I.remove();
        }
      }
    }
    // apply modifications to prepare new source code
    String newSource;
    {
      String source = serviceUnit.getBuffer().getContents();
      Document document = new Document(source);
      // prepare text edits
      MultiTextEdit edits =
          (MultiTextEdit) serviceRoot.rewrite(document, javaProject.getOptions(true));
      removeAnnotations(serviceType, source, edits);
      // prepare new source code
      edits.apply(document);
      newSource = document.get();
    }
    // update compilation unit
    {
      ICompilationUnit unit =
          servicePackage.createCompilationUnit(
              remoteServiceAsyncName + ".java",
              newSource,
              true,
              null);
      unit.getBuffer().save(null, true);
    }
  }

  private static void addImport(CompilationUnit compilationUnit, String qualifiedName) {
    AST ast = compilationUnit.getAST();
    List<ImportDeclaration> imports = DomGenerics.imports(compilationUnit);
    // check for existing ImportDeclaration
    for (ImportDeclaration importDeclaration : imports) {
      if (importDeclaration.getName().toString().equals(qualifiedName)) {
        return;
      }
    }
    // add new ImportDeclaration
    ImportDeclaration importDeclaration = ast.newImportDeclaration();
    importDeclaration.setName(ast.newName(qualifiedName));
    imports.add(importDeclaration);
  }

  /**
   * Keeps Async interfaces for services and remove other interfaces.
   */
  private static void updateInterfacesOfAsync(IJavaProject javaProject,
      CompilationUnit serviceUnit,
      TypeDeclaration serviceType) throws Exception {
    String serviceName = AstNodeUtils.getFullyQualifiedName(serviceType, false);
    AST ast = serviceType.getAST();
    for (Iterator<?> I = serviceType.superInterfaceTypes().iterator(); I.hasNext();) {
      Type type = (Type) I.next();
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(type);
      if (AstNodeUtils.isSuccessorOf(typeBinding, Constants.CLASS_REMOTE_SERVICE)) {
        String superServiceName = AstNodeUtils.getFullyQualifiedName(typeBinding, false);
        String superAsyncName = superServiceName + "Async";
        if (javaProject.findType(superAsyncName) != null) {
          if (type instanceof SimpleType) {
            {
              SimpleType simpleType = (SimpleType) type;
              String superAsyncNameSimple = CodeUtils.getShortClass(superAsyncName);
              simpleType.setName(ast.newSimpleName(superAsyncNameSimple));
            }
            if (!CodeUtils.isSamePackage(serviceName, superAsyncName)) {
              addImport(serviceUnit, superAsyncName);
            }
            continue;
          }
        }
      }
      I.remove();
    }
  }

  /**
   * Removes annotations of service {@link TypeDeclaration}.
   */
  private static void removeAnnotations(TypeDeclaration serviceType,
      String source,
      MultiTextEdit edits) {
    int typePos = serviceType.getStartPosition();
    {
      Javadoc javadoc = serviceType.getJavadoc();
      if (javadoc != null) {
        typePos = javadoc.getStartPosition() + javadoc.getLength();
        while (Character.isWhitespace(source.charAt(typePos))) {
          typePos++;
        }
      }
    }
    int pureTypePos =
        StringUtils.indexOfAny(
            source,
            new String[]{"public ", "protected ", "class ", "interface "});
    if (pureTypePos != -1 && pureTypePos != typePos) {
      edits.addChild(new DeleteEdit(typePos, pureTypePos - typePos));
    }
  }

  private void updateImpl(IPackageFragment servicePackage, ICompilationUnit serviceUnit)
      throws Exception {
    // find single Impl type 
    ICompilationUnit implUnit;
    {
      IType implType = GwtRefactoringUtils.getServiceImplType(serviceUnit.findPrimaryType(), null);
      if (implType == null) {
        return;
      }
      implUnit = implType.getCompilationUnit();
    }
    // prepare AST unit and type
    CompilationUnit implRoot = Utils.parseUnit(implUnit);
    TypeDeclaration implType = (TypeDeclaration) implRoot.types().get(0);
    // use standard JDT operation
    final IWorkspaceRunnable workspaceRunnable =
        OverrideMethodsAction.createRunnable(implRoot, implType.resolveBinding(), null, -1, false);
    // execute in UI because operation works with widgets during apply
    ExecutionUtils.runLogUI(new RunnableEx() {
      public void run() throws Exception {
        workspaceRunnable.run(null);
      }
    });
    implUnit.save(null, true);
    implUnit.getBuffer().save(null, true);
  }

  /**
   * @return the {@link Type} that extends {@link Object}, even if given is primitive.
   */
  private static Type getObjectType(Type type) {
    if (type.isPrimitiveType()) {
      String identifier = ((PrimitiveType) type).getPrimitiveTypeCode().toString();
      if (identifier.equals("int")) {
        identifier = "Integer";
      } else {
        identifier = StringUtils.capitalize(identifier);
      }
      SimpleName typeName = type.getAST().newSimpleName(identifier);
      return type.getAST().newSimpleType(typeName);
    } else {
      return type;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Recompile on *.gwt.xml modification
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Rebuilds GWT project if any of the *.gwt.xml file was modified. We need this to force out
   * {@link MyCompilationParticipant} to validate again source, because *.gwt.xml may be
   * included/removed some other GWT module and some types become accessible/inaccessible in
   * "source" classpath.
   */
  private void checkModuleFileModification() throws CoreException {
    // optimization for tests
    if (!MyCompilationParticipant.ENABLED) {
      return;
    }
    //
    // prepare projects with changed *.gwt.xml files
    final Set<IProject> projectsToBuild = Sets.newHashSet();
    IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
      public boolean visit(IResourceDelta delta) throws CoreException {
        IResource resource = delta.getResource();
        switch (resource.getType()) {
          case IResource.PROJECT :
            // process only GWT projects
            IProject project = (IProject) resource;
            return Utils.isGWTProject(project);
          case IResource.FOLDER : {
            // process only resources in "src" folders
            IJavaProject javaProject = JavaCore.create(getProject());
            return javaProject.isOnClasspath(resource);
          }
          case IResource.FILE :
            if (Utils.getExactModule(resource) != null) {
              projectsToBuild.add(resource.getProject());
            }
        }
        return true;
      }
    };
    m_delta.accept(visitor);
    // run "Clean" job for found projects
    if (!projectsToBuild.isEmpty()) {
      WorkspaceJob cleanJob = new WorkspaceJob("GWT project rebuild") {
        @Override
        public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
          for (IProject project : projectsToBuild) {
            project.build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
          }
          return Status.OK_STATUS;
        }
      };
      cleanJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
      cleanJob.setUser(true);
      cleanJob.schedule();
    }
  }
}