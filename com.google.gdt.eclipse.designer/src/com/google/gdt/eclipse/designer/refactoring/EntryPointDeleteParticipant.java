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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

/**
 * Removes EntryPoint declaration from module.xml during EntryPoint delete.
 * 
 * @author scheglov_ke
 * @coverage gwt.refactoring.participants
 */
public class EntryPointDeleteParticipant extends AbstractDeleteParticipant {
  @Override
  protected Change createChangeEx(IProgressMonitor pm) throws Exception {
    CompositeChange compositeChange = new CompositeChange("EntryPoint delete");
    // add changes
    IType entryPointType = m_type;
    compositeChange.add(GwtRefactoringUtils.module_removeEntryPoint(entryPointType));
    //
    return compositeChange;
  }
}
