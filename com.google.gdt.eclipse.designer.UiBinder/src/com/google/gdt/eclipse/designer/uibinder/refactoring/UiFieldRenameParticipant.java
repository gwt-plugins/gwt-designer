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
package com.google.gdt.eclipse.designer.uibinder.refactoring;

import com.google.gdt.eclipse.designer.uibinder.editor.UiBinderPairResourceProvider;
import com.google.gdt.eclipse.designer.uibinder.model.util.NameSupport;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.refactoring.RefactoringUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentAttribute;
import org.eclipse.wb.internal.core.utils.xml.DocumentModelVisitor;
import org.eclipse.wb.internal.core.utils.xml.FileDocumentEditContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import org.apache.commons.lang.StringUtils;

/**
 * Participates in rename of "@UiField".
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.refactoring
 */
public class UiFieldRenameParticipant extends RenameParticipant {
  protected IField m_field;
  private String m_oldName;
  private String m_newName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // RefactoringParticipant
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getName() {
    return "UiField rename participant";
  }

  @Override
  protected boolean initialize(Object element) {
    m_field = (IField) element;
    return true;
  }

  @Override
  public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) {
    return new RefactoringStatus();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Change
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final Change createChange(final IProgressMonitor pm) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Change>() {
      public Change runObject() throws Exception {
        return createChangeEx(pm);
      }
    }, null);
  }

  /**
   * Implementation of {@link #createChange(IProgressMonitor)} that can throw {@link Exception}.
   */
  private Change createChangeEx(IProgressMonitor pm) throws Exception {
    CompositeChange compositeChange = new CompositeChange("synthetic");
    compositeChange.markAsSynthetic();
    // prepare context
    m_oldName = m_field.getElementName();
    m_newName = getArguments().getNewName();
    // add changes
    addHandlerMethodChanges(compositeChange, pm);
    compositeChange.add(createTemplateChange());
    return compositeChange;
  }

  /**
   * Adds {@link TextEdit}s into existing Java file {@link Change}.
   */
  private void addHandlerMethodChanges(CompositeChange compositeChange, IProgressMonitor pm)
      throws Exception {
    TextChange change = new CompilationUnitChange("(No matter)", m_field.getCompilationUnit());
    change.setEdit(new MultiTextEdit());
    // update handler methods
    String oldMethodPrefix = "on" + StringUtils.capitalize(m_oldName);
    String newMethodPrefix = "on" + StringUtils.capitalize(m_newName);
    IType type = (IType) m_field.getParent();
    for (IMethod method : type.getMethods()) {
      // prepare @UiHandler annotation
      IAnnotation annotation = getHandlerAnnotation(method);
      if (annotation == null) {
        continue;
      }
      // update @UiHandler name
      {
        ISourceRange annoRange = annotation.getSourceRange();
        ISourceRange nameRange = annotation.getNameRange();
        int nameEnd = nameRange.getOffset() + nameRange.getLength();
        int annoEnd = annoRange.getOffset() + annoRange.getLength();
        change.addEdit(new ReplaceEdit(nameEnd, annoEnd - nameEnd, "(\"" + m_newName + "\")"));
      }
      // rename method
      String methodName = method.getElementName();
      if (methodName.startsWith(oldMethodPrefix)) {
        String newName = newMethodPrefix + StringUtils.removeStart(methodName, oldMethodPrefix);
        Change renameChange = createRenameChange(method, newName, pm);
        compositeChange.add(renameChange);
        RefactoringUtils.mergeTextChange(this, renameChange);
      }
    }
    // merge edits into existing (rename) change
    RefactoringUtils.mergeTextChange(this, change);
  }

  /**
   * @return the "@UiHandler" {@link IAnnotation}, may be <code>null</code>.
   */
  private IAnnotation getHandlerAnnotation(IMethod method) throws Exception {
    for (IAnnotation annotation : method.getAnnotations()) {
      if (annotation.getElementName().equals("UiHandler")
          && annotation.getMemberValuePairs()[0].getValue().equals(m_oldName)) {
        return annotation;
      }
    }
    return null;
  }

  /**
   * @return the {@link Change} to rename given {@link IMethod}.
   */
  private static Change createRenameChange(IMethod method, String newName, IProgressMonitor pm)
      throws Exception {
    RenameSupport renameSupport =
        RenameSupport.create(method, newName, RenameSupport.UPDATE_REFERENCES);
    RenameRefactoring refactoring =
        (RenameRefactoring) ReflectionUtils.getFieldObject(renameSupport, "fRefactoring");
    refactoring.checkAllConditions(pm);
    return refactoring.createChange(pm);
  }

  /**
   * @return the <code>*.ui.xml</code> file {@link Change}.
   */
  private Change createTemplateChange() throws Exception {
    if (NameSupport.isRenaming()) {
      return null;
    }
    IFile javaFile = (IFile) m_field.getUnderlyingResource();
    IFile uiFile = UiBinderPairResourceProvider.INSTANCE.getPair(javaFile);
    // field from not UiBinder unit
    if (uiFile == null) {
      return null;
    }
    // prepare change
    return RefactoringUtils.modifyXML(uiFile, new DocumentModelVisitor() {
      @Override
      public void visit(DocumentAttribute attribute) {
        if (attribute.getName().endsWith(":field") && attribute.getValue().equals(m_oldName)) {
          attribute.setValue(m_newName);
        }
      }
    }, new FileDocumentEditContext(uiFile));
  }
}
