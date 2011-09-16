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
package com.google.gdt.eclipse.designer.actions.wizard.ui;

import com.google.gdt.eclipse.designer.wizards.ui.DialogBoxWizard;

import org.eclipse.wb.internal.core.wizards.AbstractOpenWizardDelegate;

import org.eclipse.jface.wizard.IWizard;

/**
 * Action for adding new GWT Dialog.
 * 
 * @author scheglov_le
 * @coverage gwt.actions
 */
public class DialogBoxAction extends AbstractOpenWizardDelegate {
  @Override
  protected IWizard createWizard() {
    return new DialogBoxWizard();
  }
}
