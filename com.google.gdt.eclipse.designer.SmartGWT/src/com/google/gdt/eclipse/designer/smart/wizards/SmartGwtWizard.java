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
package com.google.gdt.eclipse.designer.smart.wizards;

import com.google.gdt.eclipse.designer.wizards.ui.GwtWizard;

import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.wizards.ErrorMessageWizardPage;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.wizard.Wizard;

/**
 * Abstract {@link Wizard} for SmartGWT.
 * 
 * @author scheglov_ke
 * @coverage SmartGWT.wizard
 */
public abstract class SmartGwtWizard extends GwtWizard {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean validateSelection() {
    IJavaProject javaProject = getJavaProject();
    if (javaProject == null
        || !ProjectUtils.hasType(javaProject, "com.smartgwt.client.widgets.BaseWidget")) {
      addPage(new ErrorMessageWizardPage("Project is not configured for SmartGWT.",
          "Use \"Configure for using SmartGWT\" action on module."));
      return false;
    }
    return super.validateSelection();
  }
}
