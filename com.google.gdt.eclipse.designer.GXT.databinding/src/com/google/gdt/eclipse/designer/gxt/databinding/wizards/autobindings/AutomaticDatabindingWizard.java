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
package com.google.gdt.eclipse.designer.gxt.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.wizards.autobindings.AutomaticDatabindingSecondPage;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.IAutomaticDatabindingProvider;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.wizards.ErrorMessageWizardPage;

import org.eclipse.jdt.core.IJavaProject;

/**
 * 
 * @author lobas_av
 * 
 */
public final class AutomaticDatabindingWizard
    extends
      org.eclipse.wb.internal.core.databinding.wizards.autobindings.AutomaticDatabindingWizard {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AutomaticDatabindingWizard() {
    setWindowTitle("New GXT Automatic Databinding");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addPages() {
    if (checkSelection()) {
      IAutomaticDatabindingProvider databindingProvider = GxtDatabindingProvider.create();
      // prepare selection
      String beanClassName = getSelectionBeanClass(getSelection());
      // create first page: via standard "New Java Wizard"
      AutomaticDatabindingFirstPage firstPage =
          new AutomaticDatabindingFirstPage(databindingProvider, beanClassName);
      firstPage.setTitle("Java Class");
      firstPage.setDescription("Create a new Java class.");
      m_mainPage = firstPage;
      addPage(firstPage);
      firstPage.setInitialSelection(getSelection());
      // create second page: databindings
      AutomaticDatabindingSecondPage secondPage =
          new AutomaticDatabindingSecondPage(firstPage, databindingProvider, beanClassName);
      secondPage.setTitle("Databindings");
      secondPage.setDescription("Bind Java Bean to GWT widgets.");
      addPage(secondPage);
    }
  }

  private boolean checkSelection() {
    IJavaProject javaProject = getJavaProject();
    if (javaProject == null
        || !ProjectUtils.hasType(javaProject, "com.extjs.gxt.ui.client.widget.Component")) {
      addPage(new ErrorMessageWizardPage("Project is not configured for GXT.",
          "Use \"Configure for using Ext GWT (GXT)\" action on module."));
      return false;
    }
    return true;
  }
}