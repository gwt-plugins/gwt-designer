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
package com.google.gdt.eclipse.designer.model.property.css;

import com.google.gdt.eclipse.designer.Activator;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for CSS files editing.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 */
public class StylesEditDialog extends ResizableDialog {
  protected final java.util.List<IFile> m_files;
  protected String m_selectionValue;
  private final boolean m_showApplyButton;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StylesEditDialog(Shell parentShell,
      java.util.List<IFile> files,
      String selectionValue,
      boolean showApplyButton) {
    super(parentShell, Activator.getDefault());
    m_files = files;
    m_selectionValue = selectionValue;
    m_showApplyButton = showApplyButton;
  }

  public StylesEditDialog(Shell parentShell, java.util.List<IFile> files, String selectionValue) {
    this(parentShell, files, selectionValue, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the initial/selected value.
   */
  public String getSelectionValue() {
    return m_selectionValue;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  protected StylesEditComposite m_editComposite;

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    area.setLayout(new FillLayout());
    createAreaContents(area);
    return area;
  }

  protected void createAreaContents(Composite area) {
    m_editComposite = new StylesEditComposite(area, m_files, SWT.NONE, null);
    m_editComposite.initializeState(m_selectionValue);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog: shell
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("CSS Style Editor");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog: buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    super.createButtonsForButtonBar(parent);
    if (m_showApplyButton) {
      createButton(parent, IDialogConstants.PROCEED_ID, "Apply", false);
    }
  }

  @Override
  protected void buttonPressed(int buttonId) {
    super.buttonPressed(buttonId);
    if (buttonId == IDialogConstants.PROCEED_ID) {
      applyChanges();
      setReturnCode(buttonId);
      close();
    }
  }

  @Override
  protected void okPressed() {
    applyChanges();
    super.okPressed();
  }

  protected void applyChanges() {
    doSaveStyles();
    m_selectionValue = m_editComposite.getSelectedRule();
  }

  /**
   * Saves CSS file styles.
   */
  protected void doSaveStyles() {
    try {
      m_editComposite.saveCurrentRule();
      m_editComposite.saveContexts();
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }
}
