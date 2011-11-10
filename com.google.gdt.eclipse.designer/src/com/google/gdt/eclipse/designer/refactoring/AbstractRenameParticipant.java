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
package com.google.gdt.eclipse.designer.refactoring;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

/**
 * Abstract GWT rename participant.
 * 
 * @author scheglov_ke
 * @coverage gwt.refactoring.participants
 */
public abstract class AbstractRenameParticipant extends RenameParticipant {
  protected IType m_type;

  ////////////////////////////////////////////////////////////////////////////
  //
  // RefactoringParticipant
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getName() {
    return "GWT rename participant";
  }

  @Override
  protected boolean initialize(Object element) {
    Assert.isTrue(
        element instanceof IType,
        "Only IType can be renamed, but {0} received. Check participant enablement filters.",
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
  public abstract Change createChangeEx(IProgressMonitor pm) throws Exception;
}
