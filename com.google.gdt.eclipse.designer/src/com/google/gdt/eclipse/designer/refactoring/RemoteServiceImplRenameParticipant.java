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
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

/**
 * Participates in rename of <code>RemoteService</code> impl.
 * 
 * @author scheglov_ke
 * @coverage gwt.refactoring.participants
 */
public class RemoteServiceImplRenameParticipant extends AbstractRenameParticipant {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Change
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Change createChangeEx(IProgressMonitor pm) throws Exception {
    CompositeChange compositeChange = new CompositeChange("Remote service Impl rename");
    // prepare context
    String packageName = m_type.getPackageFragment().getElementName();
    String newSimpleName = getArguments().getNewName();
    String newTypeName = packageName + "." + newSimpleName;
    // add changes
    compositeChange.add(GwtRefactoringUtils.module_replaceServletClass(m_type, newTypeName));
    compositeChange.add(GwtRefactoringUtils.web_replaceServletClass(m_type, newTypeName));
    return compositeChange;
  }
}
