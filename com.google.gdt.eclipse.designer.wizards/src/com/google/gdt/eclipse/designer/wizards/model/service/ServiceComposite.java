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

import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.wizards.model.common.AbstractGwtComposite;
import com.google.gdt.eclipse.designer.wizards.model.common.ClientPackageSelectionDialogField;
import com.google.gdt.eclipse.designer.wizards.model.common.IMessageContainer;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Composite that ask user for parameters of new GWT RemoteModule.
 * 
 * @author scheglov_ke
 * @coverage gwt.wizard.ui
 */
public class ServiceComposite extends AbstractGwtComposite {
  ClientPackageSelectionDialogField m_clientPackageField;
  private final StringDialogField m_serviceField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ServiceComposite(Composite parent,
      int style,
      IMessageContainer messageContainer,
      IPackageFragment selectedPackage) {
    super(parent, style, messageContainer);
    //
    int columns = 3;
    setLayout(new GridLayout(columns, false));
    // client package
    {
      m_clientPackageField = new ClientPackageSelectionDialogField("Client package:", "&Browse...");
      m_clientPackageField.setDialogFieldListener(m_validateListener);
      DialogFieldUtils.fillControls(this, m_clientPackageField, columns, 60);
    }
    // service name
    {
      m_serviceField = new StringDialogField();
      m_serviceField.setDialogFieldListener(m_validateListener);
      m_serviceField.setLabelText("Service &name:");
      DialogFieldUtils.fillControls(this, m_serviceField, columns, 60);
      m_serviceField.getTextControl(null).setFocus();
    }
    // initialize fields
    m_clientPackageField.setPackageFragment(selectedPackage);
    m_serviceField.setText("");
    Label label = new Label(this, SWT.NONE);
    label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
    label.setText("Note: make sure that Eclipse auto-build is turned on.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String validate() {
    IPackageFragment packageFragment = m_clientPackageField.getPackageFragment();
    // validate package
    {
      if (packageFragment == null) {
        return "Select client package.";
      }
      try {
        if (!Utils.isModuleSourcePackage(packageFragment)) {
          return "Package " + packageFragment.getElementName() + " is not a client package.";
        }
      } catch (Throwable e) {
        return "Exception: " + e.getMessage();
      }
    }
    // validate service name
    {
      String serviceName = m_serviceField.getText();
      if (serviceName.length() == 0) {
        return "Service name can not be empty.";
      }
      // check that service name is valid identifier
      IStatus status = JavaConventions.validateIdentifier(serviceName, null, null);
      if (status.getSeverity() == IStatus.ERROR) {
        return status.getMessage();
      }
      // check that there are no class with same name
      try {
        String qualifiedServiceName = packageFragment.getElementName() + "." + serviceName;
        if (packageFragment.getJavaProject().findType(qualifiedServiceName) != null) {
          return "Type with such name already exists.";
        }
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public IPackageFragment getPackageFragment() {
    return m_clientPackageField.getPackageFragment();
  }

  public String getServiceName() {
    return m_serviceField.getText();
  }
}
