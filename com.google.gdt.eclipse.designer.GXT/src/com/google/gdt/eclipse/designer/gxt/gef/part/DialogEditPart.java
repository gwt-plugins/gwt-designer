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
package com.google.gdt.eclipse.designer.gxt.gef.part;

import com.google.gdt.eclipse.designer.gxt.model.widgets.DialogInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.DialogInfo.DialogButton_Info;

import org.eclipse.wb.gef.core.EditPart;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link EditPart} for {@link DialogInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.part
 */
public class DialogEditPart extends ContentPanelEditPart {
  private final DialogInfo m_dialog;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DialogEditPart(DialogInfo dialog) {
    super(dialog);
    m_dialog = dialog;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<?> getModelChildren() {
    List<Object> children = new ArrayList<Object>(super.getModelChildren());
    children.addAll(m_dialog.getDialogButtons());
    return children;
  }

  @Override
  protected EditPart createEditPart(Object model) {
    if (model instanceof DialogButton_Info) {
      return new DialogButtonEditPart((DialogButton_Info) model);
    }
    return super.createEditPart(model);
  }
}
