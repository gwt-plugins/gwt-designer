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

import com.google.gdt.eclipse.designer.wizards.model.common.AbstractGwtWizardPage;
import com.google.gdt.eclipse.designer.wizards.model.common.IMessageContainer;
import com.google.gdt.eclipse.designer.wizards.model.mvp.CreateViewOperation.ViewConfiguration;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Wizard page for MVP view creation.
 * 
 * @author sablin_aa
 * @coverage gwt.wizard.ui
 */
public class ViewWizardPage extends AbstractGwtWizardPage {
  private final IPackageFragment initialPkg;
  private ViewComposite viewComposite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewWizardPage(IPackageFragment initialPkg) {
    this.initialPkg = initialPkg;
    setTitle("New GWT view");
    setMessage("Create a new GWT view");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createPageControls(Composite parent) {
    // create GWT module parameters composite
    {
      IMessageContainer messagesContainer = IMessageContainer.Util.forWizardPage(this);
      viewComposite = new ViewComposite(parent, SWT.NONE, messagesContainer, initialPkg);
      GridDataFactory.create(viewComposite).grab().fill();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void createView() throws Exception {
    IPackageFragmentRoot root = viewComposite.getRoot();
    ViewConfiguration configuration = new ViewConfiguration() {
      @Override
      public String getViewName() {
        return viewComposite.getViewName();
      }

      @Override
      public String getViewPackageName() {
        return viewComposite.getViewPackageName();
      }

      @Override
      public boolean isUseJavaTemplate() {
        return viewComposite.isUseJavaTemplate();
      }

      @Override
      public String getPlaceName() {
        return viewComposite.getPlaceName();
      }

      @Override
      public String getPlacePackageName() {
        return viewComposite.getPlacePackageName();
      }

      @Override
      public String getActivityName() {
        return viewComposite.getActivityName();
      }

      @Override
      public String getActivityPackageName() {
        return viewComposite.getActivityPackageName();
      }

      @Override
      public String getClientFactoryName() {
        return viewComposite.getClientFactoryName();
      }

      @Override
      public String getClientFactoryPackageName() {
        return viewComposite.getClientFactoryPackageName();
      }
    };
    CreateViewOperation operation = new CreateViewOperation(root);
    operation.create(configuration);
  }
}
