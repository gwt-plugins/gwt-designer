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

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.DeleteParticipant;

/**
 * Abstract GWT delete participant.
 * 
 * @author scheglov_ke
 * @coverage gwt.refactoring.participants
 */
public abstract class AbstractDeleteParticipant extends DeleteParticipant {
  protected IType m_type;

  ////////////////////////////////////////////////////////////////////////////
  //
  // RefactoringParticipant
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getName() {
    return "GWT delete participant";
  }

  @Override
  protected boolean initialize(Object element) {
    Assert.isTrue(
        element instanceof IType,
        "Only IType can be deleted, but {0} received. Check participant enablement filters.",
        element);
    m_type = (IType) element;
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
  public final Change createChange(final IProgressMonitor pm) throws CoreException {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Change>() {
      public Change runObject() throws Exception {
        return createChangeEx(pm);
      }
    }, null);
  }

  /**
   * Implementation of {@link #createChange(IProgressMonitor)} that can throw any {@link Exception}.
   */
  protected abstract Change createChangeEx(IProgressMonitor pm) throws Exception;
}
