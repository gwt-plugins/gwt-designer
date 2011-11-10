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
package com.google.gdt.eclipse.designer.wizards.model.project;

import com.google.gdt.eclipse.designer.preferences.MainPreferencePage;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.apache.commons.lang.ArrayUtils;

/**
 * Wizard page that forces GWT configuring.
 * 
 * @author scheglov_ke
 * @coverage gwt.wizard.ui
 */
public class ConfigureWizardPage extends WizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ConfigureWizardPage() {
    this("You should configure GWT before any GWT project creation.");
  }

  public ConfigureWizardPage(String message) {
    super("GWT Settings");
    setTitle("GWT Settings");
    setMessage(message);
    setPageComplete(false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public void createControl(Composite parent) {
    initializeDialogUnits(parent);
    //
    Composite composite = new Composite(parent, SWT.NULL);
    composite.setFont(parent.getFont());
    composite.setLayout(new GridLayout());
    //
    Link openPreferencesLink = new Link(composite, SWT.NONE);
    openPreferencesLink.setText("<a>Click here to configure GWT...</a>");
    openPreferencesLink.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        PreferencesUtil.createPreferenceDialogOn(
            getShell(),
            MainPreferencePage.ID,
            ArrayUtils.EMPTY_STRING_ARRAY,
            null).open();
        if (MainPreferencePage.validateLocation()) {
          setPageComplete(true);
          getWizard().getContainer().showPage(getNextPage());
        }
      }
    });
    //
    setControl(composite);
    Dialog.applyDialogFont(composite);
  }
}
