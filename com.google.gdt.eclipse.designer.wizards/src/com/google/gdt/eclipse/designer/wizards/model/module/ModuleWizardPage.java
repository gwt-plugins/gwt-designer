/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gdt.eclipse.designer.wizards.model.module;

import com.google.gdt.eclipse.designer.actions.wizard.model.IModuleConfigurator;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.wizards.model.common.AbstractGwtWizardPage;
import com.google.gdt.eclipse.designer.wizards.model.common.IMessageContainer;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Wizard page for module creation.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage gwt.wizard.ui
 */
public class ModuleWizardPage extends AbstractGwtWizardPage {
  private final IPackageFragmentRoot initialRoot;
  private ModuleComposite moduleComposite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ModuleWizardPage(IPackageFragmentRoot root) {
    initialRoot = root;
    setTitle("New GWT Module");
    setMessage("Create a new GWT module");
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
      moduleComposite = new ModuleComposite(parent, SWT.NONE, messagesContainer, initialRoot);
      GridDataFactory.create(moduleComposite).grab().fill();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void createModule() throws Exception {
    IPackageFragmentRoot root = moduleComposite.getRoot();
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
