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
package com.google.gdt.eclipse.designer.wizards.model.common;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract {@link WizardPage} for creating GWT objects.
 * 
 * @author scheglov_ke
 * @coverage gwt.wizard.ui
 */
public abstract class AbstractGwtWizardPage extends WizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractGwtWizardPage() {
    super("__PAGE__");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void createControl(Composite parent) {
    initializeDialogUnits(parent);
    // create composite
    Composite composite = new Composite(parent, SWT.NULL);
    composite.setFont(parent.getFont());
    composite.setLayout(new GridLayout());
    // create controls
    createPageControls(composite);
    // set composite
    Dialog.applyDialogFont(composite);
    setControl(composite);
  }

  /**
   * Creates controls on given Composite. Default layout is {@link GridLayout}.
   * 
   * We should use this method instead of {@link #createControl(Composite)}, because all pages have
   * same font/dialog units algorithm.
   */
  protected abstract void createPageControls(Composite parent);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void setErrorMessage(String message) {
    super.setErrorMessage(message);
    // if we don't have error, mark page as completed, so enable wizard finish
    setPageComplete(message == null);
  }
}
