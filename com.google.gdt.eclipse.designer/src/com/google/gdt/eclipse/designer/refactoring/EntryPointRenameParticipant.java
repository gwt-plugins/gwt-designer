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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

/**
 * Participates in rename of <code>EntryPoint</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.refactoring.participants
 */
public class EntryPointRenameParticipant extends AbstractRenameParticipant {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Change
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Change createChangeEx(IProgressMonitor pm) throws Exception {
    String packageName = m_type.getPackageFragment().getElementName();
    String newSimpleName = getArguments().getNewName();
    String newTypeName = packageName + "." + newSimpleName;
    return GwtRefactoringUtils.module_replaceEntryPoint(m_type, newTypeName);
  }
}
