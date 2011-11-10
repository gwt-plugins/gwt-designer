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

import org.eclipse.wb.internal.core.utils.refactoring.RefactoringUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * We should participate in move to change module.xml and move Async class.
 * 
 * @author scheglov_ke
 * @coverage gwt.refactoring.participants
 */
public class RemoteServiceMoveParticipant extends AbstractMoveParticipant {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Change
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Change createChange(IPackageFragment targetPackage, IProgressMonitor pm)
      throws Exception {
    CompositeChange compositeChange = new CompositeChange("Remote service move");
    // add Async changes
    IType asyncType = GwtRefactoringUtils.getServiceAsyncType(m_type, pm);
    if (asyncType != null) {
      String asyncTypeName = asyncType.getFullyQualifiedName();
      // when we move service, Eclipse adds import for Async type,
      // but we don't need it, so remove this import from text change
      {
        TextChange textChange = getTextChange(m_type.getUnderlyingResource());
        if (textChange != null) {
          removeInsertEdits_forSubString(textChange.getEdit(), "import " + asyncTypeName);
          removeInsertEdits_forNewLine(textChange.getEdit());
        }
      }
      // remove Async type, our builder will regenerate it near to moved service type
      compositeChange.add(RefactoringUtils.createDeleteTypeChange(asyncType));
    }
    // final change
    return compositeChange;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Removes {@link InsertEdit}'s with text containing given substring.
   */
  private static void removeInsertEdits_forSubString(TextEdit edit, String subString) {
    if (edit instanceof MultiTextEdit) {
      MultiTextEdit multiTextEdit = (MultiTextEdit) edit;
      for (TextEdit child : multiTextEdit.getChildren()) {
        if (child instanceof InsertEdit) {
          InsertEdit insertEdit = (InsertEdit) child;
          if (insertEdit.getText().indexOf(subString) != -1) {
            multiTextEdit.removeChild(insertEdit);
          }
        } else {
          removeInsertEdits_forSubString(child, subString);
        }
      }
    }
  }

  /**
   * Removes {@link InsertEdit}'s with "new line only" text.
   */
  private static void removeInsertEdits_forNewLine(TextEdit edit) {
    if (edit instanceof MultiTextEdit) {
      MultiTextEdit multiTextEdit = (MultiTextEdit) edit;
      for (TextEdit child : multiTextEdit.getChildren()) {
        if (child instanceof InsertEdit) {
          InsertEdit insertEdit = (InsertEdit) child;
          if (insertEdit.getText().equals("\n")) {
            multiTextEdit.removeChild(insertEdit);
          }
        } else {
          removeInsertEdits_forNewLine(child);
        }
      }
    }
  }
}
