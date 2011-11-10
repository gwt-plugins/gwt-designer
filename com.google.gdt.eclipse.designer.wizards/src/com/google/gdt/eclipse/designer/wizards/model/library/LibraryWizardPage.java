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
package com.google.gdt.eclipse.designer.wizards.model.library;

import com.google.gdt.eclipse.designer.wizards.model.common.AbstractGwtWizardPage;
import com.google.gdt.eclipse.designer.wizards.model.common.IMessageContainer;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Wizard page for GWT library creation.
 * 
 * @author scheglov_ke
 * @coverage gwt.wizard.ui
 */
public class LibraryWizardPage extends AbstractGwtWizardPage {
  private final IPackageFragmentRoot m_initialRoot;
  private LibraryComposite m_moduleComposite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LibraryWizardPage(IPackageFragmentRoot root) {
    m_initialRoot = root;
    setTitle("New GWT Library");
    setMessage("Create a new GWT library with shared Widgets, RemoteService's, public resources, etc.");
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
      m_moduleComposite = new LibraryComposite(parent, SWT.NONE, messagesContainer, m_initialRoot);
      GridDataFactory.create(m_moduleComposite).grab().fill();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void createLibrary() throws Exception {
    IPackageFragmentRoot root = m_moduleComposite.getRoot();
    String packageName = m_moduleComposite.getPackageName();
    String moduleName = m_moduleComposite.getModuleName();
    boolean createHTML = m_moduleComposite.createHTML();
    boolean createServerPackage = m_moduleComposite.createServerPackage();
    //
    CreateLibraryOperation operation = new CreateLibraryOperation(root);
    operation.create(packageName, moduleName, createHTML, createServerPackage);
  }
}
