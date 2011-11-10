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

import com.google.gdt.eclipse.designer.wizards.model.common.AbstractGwtComposite;
import com.google.gdt.eclipse.designer.wizards.model.common.GwtProjectPackageRootFilter;
import com.google.gdt.eclipse.designer.wizards.model.common.IMessageContainer;

import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.SelectionButtonDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.jdt.ui.PackageRootSelectionDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Composite that ask user for parameters of new GWT library.
 * 
 * @author scheglov_ke
 * @coverage gwt.wizard.ui
 */
public class LibraryComposite extends AbstractGwtComposite {
  private IPackageFragmentRoot m_root;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private PackageRootSelectionDialogField m_sourceFolderField;
  private final StringDialogField m_moduleField;
  private final StringDialogField m_packageField;
  private final SelectionButtonDialogField m_createHTMLField;
  private final SelectionButtonDialogField m_createServerPackageField;
  private final Text m_descriptionText;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LibraryComposite(Composite parent, int style, IMessageContainer messageContainer) {
    this(parent, style, messageContainer, false, null);
  }

  public LibraryComposite(Composite parent,
      int style,
      IMessageContainer messageContainer,
      IPackageFragmentRoot selectedRoot) {
    this(parent, style, messageContainer, true, selectedRoot);
  }

  private LibraryComposite(Composite parent,
      int style,
      IMessageContainer messageContainer,
      boolean selectRoot,
      IPackageFragmentRoot selectedRoot) {
    super(parent, style, messageContainer);
    m_root = selectedRoot;
    //
    int columns = 3;
    setLayout(new GridLayout(columns, false));
    // source folder
    if (selectRoot) {
      m_sourceFolderField = PackageRootSelectionDialogField.create("Source folder:", "&Browse...");
      m_sourceFolderField.setPackageRootFilter(new GwtProjectPackageRootFilter());
      m_sourceFolderField.setUpdateListener(m_validateListener);
      DialogFieldUtils.fillControls(this, m_sourceFolderField, columns, 60);
    }
    // module name
    {
      m_moduleField = new StringDialogField();
      m_moduleField.setDialogFieldListener(m_validateListener);
      m_moduleField.setLabelText("&Module name:");
      DialogFieldUtils.fillControls(this, m_moduleField, columns, 60);
    }
    // package name
    {
      m_packageField = new StringDialogField();
      m_packageField.setDialogFieldListener(m_validateListener);
      m_packageField.setLabelText("&Package name:");
      DialogFieldUtils.fillControls(this, m_packageField, columns, 60);
    }
    // HTML package
    {
      new Label(this, SWT.NONE); // filler
      m_createHTMLField = new SelectionButtonDialogField(SWT.CHECK);
      m_createHTMLField.setLabelText("Create HTML file to allow visual design");
      m_createHTMLField.setSelection(true);
      m_createHTMLField.doFillIntoGrid(this, columns - 1);
    }
    // "server" package
    {
      new Label(this, SWT.NONE); // filler
      m_createServerPackageField = new SelectionButtonDialogField(SWT.CHECK);
      m_createServerPackageField.setLabelText("Create \"server\" package for RemoteService's");
      m_createServerPackageField.setSelection(true);
      m_createServerPackageField.doFillIntoGrid(this, columns - 1);
    }
    // description
    {
      m_descriptionText = new Text(this, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY);
      GridDataFactory.create(m_descriptionText).spanH(columns).grab().fill().hintHC(60).hintVC(5);
    }
    // initialize fields
    {
      if (m_sourceFolderField != null) {
        m_sourceFolderField.setRoot(selectedRoot);
      }
      m_moduleField.setTextWithoutUpdate("WidgetsLibrary");
      m_packageField.setTextWithoutUpdate("com.mycompany.library");
      validateAll();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String validate() {
    m_descriptionText.setText("");
    // validate source folder
    if (m_sourceFolderField != null) {
      if (m_sourceFolderField.getText().length() == 0) {
        return "Source folder name can not be empty.";
      }
      m_root = m_sourceFolderField.getRoot();
      if (m_root == null) {
        return "Source folder is invalid.";
      }
    }
    // validate module name
    {
      String moduleName = m_moduleField.getText();
      if (moduleName.length() == 0) {
        return "Module name can not be empty.";
      }
      // check that module name is valid identifier
      IStatus status = JavaConventions.validateIdentifier(moduleName, null, null);
      if (status.getSeverity() == IStatus.ERROR) {
        return status.getMessage();
      }
    }
    // validate package name
    {
      String packageName = m_packageField.getText();
      if (packageName.length() == 0) {
        return "Package can not be empty.";
      }
      // check that package name is valid
      IStatus status = JavaConventions.validatePackageName(packageName, null, null);
      if (status.getSeverity() == IStatus.ERROR) {
        return status.getMessage();
      }
      // check that package does not exist
      if (m_root != null && m_root.getPackageFragment(packageName).exists()) {
        return "Package " + packageName + " already exists.";
      }
    }
    // update description
    {
      String mainLocation = m_packageField.getText().replace('.', '/');
      String descriptorLocation = mainLocation + "/" + m_moduleField.getText() + ".gwt.xml";
      String clientLocation = mainLocation + "/client";
      String publicLocation = mainLocation + "/public";
      String serverLocation = mainLocation + "/server";
      m_descriptionText.setText("Location of module descriptor file: "
          + descriptorLocation
          + "\n"
          + "Location of client (GWT) part: "
          + clientLocation
          + "\n"
          + "Location of public (static) part: "
          + publicLocation
          + "\n"
          + "Location of server part: "
          + serverLocation);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public IPackageFragmentRoot getRoot() {
    return m_root;
  }

  public String getModuleName() {
    return m_moduleField.getText();
  }

  public String getPackageName() {
    return m_packageField.getText();
  }

  public boolean createHTML() {
    return m_createHTMLField.isSelected();
  }

  public boolean createServerPackage() {
    return m_createServerPackageField.isSelected();
  }
}
