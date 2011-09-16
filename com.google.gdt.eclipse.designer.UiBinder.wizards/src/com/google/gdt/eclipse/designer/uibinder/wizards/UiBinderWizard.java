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
package com.google.gdt.eclipse.designer.uibinder.wizards;

import com.google.gdt.eclipse.designer.uibinder.editor.UiBinderEditor;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderParser;
import com.google.gdt.eclipse.designer.wizards.ui.GwtWizard;

import org.eclipse.wb.internal.core.wizards.ErrorMessageWizardPage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.wizard.Wizard;

/**
 * Abstract {@link Wizard} for GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.wizards
 */
public abstract class UiBinderWizard extends GwtWizard {
  private UiBinderWizardPage m_page;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UiBinderWizard() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean validateSelection() {
    IJavaProject javaProject = getJavaProject();
    if (javaProject == null || !UiBinderParser.hasUiBinderSupport(javaProject)) {
      addPage(new ErrorMessageWizardPage("Wrong GWT version.",
          "You need at least GWT 2.1M4 for UiBinder visual editing."));
      return false;
    }
    return super.validateSelection();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addPages() {
    super.addPages();
    m_page = (UiBinderWizardPage) m_mainPage;
  }

  @Override
  protected abstract UiBinderWizardPage createMainPage();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Finish
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void finishPage(IProgressMonitor monitor) throws Exception {
    super.finishPage(monitor);
    m_page.createUI();
  }

  @Override
  protected void openEditor() {
    openResource(m_page.getFileJava(), JavaUI.ID_CU_EDITOR);
    openResource(m_page.getFileUI(), UiBinderEditor.ID);
  }
}