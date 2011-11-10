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
package com.google.gdt.eclipse.designer.gxt.databinding.ui.contentproviders;

import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.FormBindingInfo;

import org.eclipse.wb.internal.core.databinding.ui.editor.DialogFieldUiContentProvider;
import org.eclipse.wb.internal.core.utils.dialogfields.BooleanDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;

/**
 * 
 * @author lobas_av
 * 
 */
public class AutobindUiContentProvider extends DialogFieldUiContentProvider {
  private final FormBindingInfo m_binding;
  private final BooleanDialogField m_dialogField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AutobindUiContentProvider(FormBindingInfo binding) {
    m_binding = binding;
    m_dialogField = new BooleanDialogField();
    m_dialogField.setLabelText("Autobind:");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public DialogField getDialogField() {
    return m_dialogField;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() throws Exception {
    m_dialogField.setSelection(m_binding.isAutobind());
  }

  public void saveToObject() throws Exception {
    m_binding.setAutobind(m_dialogField.getSelection());
  }
}