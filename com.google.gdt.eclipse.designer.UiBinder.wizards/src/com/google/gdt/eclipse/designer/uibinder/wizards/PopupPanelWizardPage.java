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

import org.eclipse.wb.internal.core.EnvironmentUtils;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link WizardPage} for {@link PopupPanelWizard}.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.wizards
 */
public final class PopupPanelWizardPage extends UiBinderWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PopupPanelWizardPage() {
    setTitle("Create UiBinder PopupPanel");
    setImageDescriptor(Activator.getImageDescriptor("wizards/PopupPanel/banner.png"));
    setDescription("Create a UiBinder PopupPanel.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuration
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initTypePage(IJavaElement elem) {
    super.initTypePage(elem);
    setSuperClass("com.google.gwt.user.client.ui.PopupPanel", false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Create
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getTemplatePath_Java() {
    return "templates/PopupPanel.jvt";
  }

  @Override
  protected String getTemplatePath_UI() {
    return "templates/PopupPanel.ui.xml";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createLocalControls(Composite parent, int columns) {
    // I always use same names during tests
    if (EnvironmentUtils.DEVELOPER_HOST) {
      setTypeName("PopupPanel_1", true);
    }
  }
}