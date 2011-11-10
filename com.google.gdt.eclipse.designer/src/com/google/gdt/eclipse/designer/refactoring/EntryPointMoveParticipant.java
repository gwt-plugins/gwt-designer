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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.ltk.core.refactoring.Change;

/**
 * Participates in <code>EntryPoint</code> move to change module.xml
 * 
 * @author scheglov_ke
 * @coverage gwt.refactoring.participants
 */
public class EntryPointMoveParticipant extends AbstractMoveParticipant {
  @Override
  protected Change createChange(IPackageFragment targetPackage, IProgressMonitor pm)
      throws Exception {
    String newTypeName = targetPackage.getElementName() + "." + m_type.getElementName();
    return GwtRefactoringUtils.module_replaceEntryPoint(m_type, newTypeName);
  }
}
