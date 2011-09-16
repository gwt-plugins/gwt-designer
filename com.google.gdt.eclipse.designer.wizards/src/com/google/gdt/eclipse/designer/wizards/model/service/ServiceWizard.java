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
package com.google.gdt.eclipse.designer.wizards.model.service;

import com.google.gdt.eclipse.designer.wizards.Activator;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for new GWT RemoteModule.
 * 
 * @author scheglov_ke
 * @coverage gwt.wizard.ui
 */
public class ServiceWizard extends Wizard implements INewWizard {
  private IPackageFragment m_selectedPackage;
  private ServiceWizardPage m_servicePage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ServiceWizard() {
    setDefaultPageImageDescriptor(Activator.getImageDescriptor("wizards/service/banner.gif"));
    setWindowTitle("New GWT RemoteService");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addPages() {
    m_servicePage = new ServiceWizardPage(m_selectedPackage);
    addPage(m_servicePage);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // INewWizard
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    if (selection != null) {
      Object selectedObject = selection.getFirstElement();
      // convert resource to Java model object
      if (selectedObject instanceof IResource) {
        IResource resource = (IResource) selectedObject;
        selectedObject = JavaCore.create(resource);
      }
      // prepare selected package
      if (selectedObject instanceof IJavaElement) {
        IJavaElement selectedJavaElement = (IJavaElement) selectedObject;
        m_selectedPackage =
            (IPackageFragment) selectedJavaElement.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
      } else {
        m_selectedPackage = null;
      }
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
      m_servicePage.createService();
      return true;
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
    return false;
  }
}
