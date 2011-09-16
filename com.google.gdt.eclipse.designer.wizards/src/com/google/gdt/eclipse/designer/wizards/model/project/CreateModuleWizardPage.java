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
package com.google.gdt.eclipse.designer.wizards.model.project;

import com.google.gdt.eclipse.designer.actions.wizard.model.IModuleConfigurator;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.wizards.model.common.AbstractGwtWizardPage;
import com.google.gdt.eclipse.designer.wizards.model.common.IMessageContainer;
import com.google.gdt.eclipse.designer.wizards.model.module.CreateModuleOperation;
import com.google.gdt.eclipse.designer.wizards.model.module.ModuleComposite;

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Wizard page that allows optional module creation.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage gwt.wizard.ui
 */
public class CreateModuleWizardPage extends AbstractGwtWizardPage {
  private Button createModuleButton;
  private Group moduleGroup;
  private ModuleComposite moduleComposite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreateModuleWizardPage() {
    setTitle("New GWT Module");
    setMessage("You can create module in new GWT project");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createPageControls(Composite parent) {
    // create check button
    {
      createModuleButton = new Button(parent, SWT.CHECK);
      createModuleButton.setText("Create GWT module");
      createModuleButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          boolean want = createModuleButton.getSelection();
          UiUtils.changeControlEnable(moduleGroup, want);
          if (want) {
            moduleComposite.validateAll();
          } else {
            setErrorMessage(null);
          }
        }
      });
    }
    // create group for GWT module parameters
    {
      moduleGroup = new Group(parent, SWT.NONE);
      GridDataFactory.create(moduleGroup).grabH().fillH();
      moduleGroup.setLayout(new GridLayout());
      moduleGroup.setText("New GWT module");
      {
        IMessageContainer messagesContainer = IMessageContainer.Util.forWizardPage(this);
        moduleComposite = new ModuleComposite(moduleGroup, SWT.NONE, messagesContainer);
        GridDataFactory.create(moduleComposite).grab().fill();
      }
    }
    UiUtils.changeControlEnable(moduleGroup, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void createModule(IJavaProject javaProject) throws Exception {
    if (createModuleButton.getSelection()) {
      IPackageFragmentRoot root = CodeUtils.getPackageFragmentRoot(javaProject);
      final String packageName = moduleComposite.getPackageName();
      final String moduleName = moduleComposite.getModuleName();
      final boolean isCreateEntryPoint = moduleComposite.createEntryPoint();
      final boolean isMvpEntryPoint = moduleComposite.createEntryPointMVP();
      // create module
      IFile moduleFile =
          CreateModuleOperation.create(
              root,
              packageName,
              moduleName,
              isCreateEntryPoint,
              isMvpEntryPoint,
              false);
      ModuleDescription moduleDescription = Utils.getExactModule(moduleFile);
      // apply configurator
      {
        IModuleConfigurator configurator = moduleComposite.getConfigurator();
        if (configurator != null) {
          configurator.configure(moduleDescription);
        }
      }
    }
  }
}
