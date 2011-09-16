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
package com.google.gdt.eclipse.designer.wizards.model.mvp;

import com.google.gdt.eclipse.designer.wizards.Activator;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for new GWT MVP view.
 * 
 * @author sablin_aa
 * @coverage gwt.wizard.ui
 */
public class ViewWizard extends Wizard implements INewWizard {
  private IPackageFragment initialPkg;
  private ViewWizardPage viewPage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewWizard() {
    setDefaultPageImageDescriptor(Activator.getImageDescriptor("wizards/module/banner.gif"));
    setWindowTitle("New GWT MVP View");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addPages() {
    viewPage = new ViewWizardPage(initialPkg);
    addPage(viewPage);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // INewWizard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    try {
      Object selectedObject = selection.getFirstElement();
      if (selectedObject instanceof IJavaElement) {
        IJavaElement element = (IJavaElement) selectedObject;
        initialPkg = (IPackageFragment) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Finish
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean performFinish() {
    try {
      viewPage.createView();
      return true;
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
    return false;
  }
}
