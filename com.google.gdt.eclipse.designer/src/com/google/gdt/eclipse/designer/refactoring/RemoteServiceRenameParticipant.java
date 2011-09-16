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

import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.refactoring.CompilationUnitChange;
import org.eclipse.wb.internal.core.utils.refactoring.RefactoringUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.text.edits.TextEdit;

/**
 * We should participate in rename of <code>RemoteService</code> interface to change module.xml,
 * Async and Impl classes.
 * 
 * @author scheglov_ke
 * @coverage gwt.refactoring.participants
 */
public class RemoteServiceRenameParticipant extends AbstractRenameParticipant {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Change
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Change createChangeEx(IProgressMonitor pm) throws Exception {
    CompositeChange compositeChange = new CompositeChange("Remote service rename");
    // prepare types
    IType serviceType = m_type;
    IType asyncType = GwtRefactoringUtils.getServiceAsyncType(serviceType, pm);
    IType implType = GwtRefactoringUtils.getServiceImplType(serviceType, pm);
    // prepare names
    String oldServiceName = serviceType.getElementName();
    String newServiceName = getArguments().getNewName();
    // rename Async
    if (!Utils.hasGPE()) {
      if (asyncType != null) {
        String newAsyncName = newServiceName + "Async";
        Change renameChange = RefactoringUtils.createRenameTypeChange(asyncType, newAsyncName, pm);
        // renameChange also changes serviceType compilation unit, so merge changes for its file
        RefactoringUtils.mergeTextChange(this, renameChange);
        // after merge add remainder to the composite change
        compositeChange.add(renameChange);
      }
    }
    // Impl
    if (implType != null) {
      compositeChange.add(createImplChange(
          serviceType,
          implType,
          oldServiceName,
          newServiceName,
          pm));
    }
    //
    return compositeChange;
  }

  private CompositeChange createImplChange(IType serviceType,
      IType implType,
      String oldServiceName,
      String newServiceName,
      IProgressMonitor pm) throws Exception, CoreException {
    CompositeChange implChange = new CompositeChange("Implementation change");
    String newImplName = newServiceName + "Impl";
    // change module XML
    {
      String oldServletPath = "/" + oldServiceName;
      String newServletPath = "/" + newServiceName;
      implChange.add(GwtRefactoringUtils.module_replaceServletPath(
          implType,
          oldServletPath,
          newServletPath));
    }
    // change name in remote service
    implChange.add(replaceServiceNameInAST(serviceType, oldServiceName, newServiceName));
    // change web.xml
    implChange.add(GwtRefactoringUtils.web_replaceServletPath(
        implType,
        oldServiceName,
        newServiceName));
    // rename Impl (participant for Impl rename will modify module)
    {
      Change implRenameChange = RefactoringUtils.createRenameTypeChange(implType, newImplName, pm);
      RefactoringUtils.mergeTextChanges(implChange, implRenameChange);
      implChange.add(implRenameChange);
    }
    // final change
    return implChange;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Change} for modifying service name in AST.
   */
  private Change replaceServiceNameInAST(IType serviceType,
      final String oldServletName,
      final String newServletName) throws Exception {
    ICompilationUnit serviceCompilationUnit = serviceType.getCompilationUnit();
    // parse service type into AST and change servlet path
    CompilationUnit serviceUnit = CodeUtils.parseCompilationUnit(serviceCompilationUnit);
    serviceUnit.recordModifications();
    serviceUnit.accept(new ASTVisitor() {
      @Override
      public void endVisit(StringLiteral node) {
        if (oldServletName.equals(node.getLiteralValue())) {
          node.setLiteralValue(newServletName);
        }
      }
    });
    // create text edits corresponding to that changes in AST
    TextEdit astTextEdit;
    {
      String source = serviceCompilationUnit.getBuffer().getContents();
      Document document = new Document(source);
      astTextEdit = serviceUnit.rewrite(document, serviceType.getJavaProject().getOptions(true));
    }
    // merge AST edit with existing edit for service type file
    {
      IFile serviceTypeFile = (IFile) serviceType.getUnderlyingResource();
      TextChange existingTextChange = getTextChange(serviceTypeFile);
      if (existingTextChange != null) {
        existingTextChange.addEdit(astTextEdit);
        return null;
      } else {
        CompilationUnitChange serviceUnitChange =
            new CompilationUnitChange(serviceType.getFullyQualifiedName(), serviceCompilationUnit);
        serviceUnitChange.setEdit(astTextEdit);
        return serviceUnitChange;
      }
    }
  }
}
