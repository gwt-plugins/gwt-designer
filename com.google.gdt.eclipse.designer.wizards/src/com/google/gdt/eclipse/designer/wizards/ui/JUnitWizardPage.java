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
package com.google.gdt.eclipse.designer.wizards.ui;

import com.google.gdt.eclipse.designer.ToolkitProvider;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider.ModuleModification;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.wizards.Activator;
import com.google.gdt.eclipse.designer.wizards.WizardUtils;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.wizards.TemplateDesignWizardPage;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.SelectionDialog;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.List;

/**
 * @author lobas_av
 * @author mitin_aa
 * @coverage gwt.wizard.ui
 */
public final class JUnitWizardPage extends TemplateDesignWizardPage {
  private final StringButtonDialogField m_classUnderTestField;
  private IStatus m_classUnderTestStatus;
  private String m_moduleId = "";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JUnitWizardPage() {
    setTitle("GWT JUnit Test Case");
    setDescription("Create GWT JUnit Test Case");
    setImageDescriptor(Activator.getImageDescriptor("wizards/JUnit/banner.png"));
    //
    m_classUnderTestStatus = new Status(IStatus.OK, Activator.PLUGIN_ID, IStatus.OK, null, null);
    m_classUnderTestField = new StringButtonDialogField(new IStringButtonAdapter() {
      public void changeControlPressed(DialogField field) {
        handleChooseClassUnderTest();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createControl(Composite parent) {
    initializeDialogUnits(parent);
    //
    int numColumns = 4;
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(composite).columns(numColumns);
    //
    createClassUnderTestContols(composite, numColumns);
    createSeparator(composite, numColumns);
    createContainerControls(composite, numColumns);
    createPackageControls(composite, numColumns);
    createTypeNameControls(composite, numColumns);
    createSuperClassControls(composite, numColumns);
    //
    setControl(composite);
  }

  private void createClassUnderTestContols(Composite parent, int numColumns) {
    m_classUnderTestField.setLabelText("Class under test:");
    m_classUnderTestField.setButtonLabel("Browse...");
    m_classUnderTestField.doFillIntoGrid(parent, numColumns);
    m_classUnderTestField.getTextControl(null).setEnabled(false);
  }

  @Override
  protected void setFocus() {
    m_classUnderTestField.setFocus();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handling
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initTypePage(IJavaElement element) {
    super.initTypePage(element);
    setSuperClass("com.google.gwt.junit.client.GWTTestCase", false);
    handleSelectClassUnderTest(element);
  }

  protected final void handleChooseClassUnderTest() {
    try {
      List<IJavaProject> projectsList = Utils.getGWTProjects();
      IJavaProject[] projectsArray = projectsList.toArray(new IJavaProject[projectsList.size()]);
      IJavaSearchScope scope = SearchEngine.createJavaSearchScope(projectsArray);
      SelectionDialog dialog =
          JavaUI.createTypeDialog(
              getShell(),
              getWizard().getContainer(),
              scope,
              IJavaElementSearchConstants.CONSIDER_CLASSES,
              false);
      dialog.setTitle("Class Under Test");
      dialog.setMessage("Test stubs will be generated for class:");
      dialog.setInitialSelections(new Object[]{m_classUnderTestField.getText()});
      if (dialog.open() == Window.OK) {
        handleSelectClassUnderTest((IJavaElement) dialog.getResult()[0]);
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  private void handleSelectClassUnderTest(IJavaElement element) {
    try {
      if (element == null || !element.exists() || element.getJavaProject() == null) {
        setErrorState();
      } else {
        IJavaProject javaProject = element.getJavaProject();
        if (Utils.isGWTProject(javaProject)) {
          IPackageFragmentRoot testSourceFragmentRoot = handleTestSourceFolder(javaProject);
          IPackageFragment elementPackage = handleTestPackage(element, testSourceFragmentRoot);
          // handle class under test
          IType classUnderTestType = (IType) element.getAncestor(IJavaElement.TYPE);
          if (classUnderTestType == null) {
            ICompilationUnit compilationUnit =
                (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
            if (compilationUnit != null) {
              classUnderTestType = compilationUnit.findPrimaryType();
            }
          }
          if (classUnderTestType == null) {
            setErrorState();
          } else {
            m_classUnderTestField.setText(classUnderTestType.getFullyQualifiedName());
            setTypeName(classUnderTestType.getElementName() + "Test", true);
            m_classUnderTestStatus =
                new Status(IStatus.OK, Activator.PLUGIN_ID, IStatus.OK, null, null);
            //
            ModuleDescription module = Utils.getSingleModule(elementPackage);
            if (module == null) {
              setErrorState("GWT module for "
                  + classUnderTestType.getFullyQualifiedName()
                  + " not found.");
            } else {
              m_moduleId = module.getId();
            }
          }
        } else {
          setErrorState();
        }
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
      setErrorState("InternalError: " + e.getMessage());
    } finally {
      doStatusUpdate();
    }
  }

  private IPackageFragmentRoot handleTestSourceFolder(IJavaProject javaProject) throws Exception {
    String testSourceFolderName =
        com.google.gdt.eclipse.designer.Activator.getStore().getString(
            Constants.P_GWT_TESTS_SOURCE_FOLDER);
    IFolder testSourceFolder = javaProject.getProject().getFolder(testSourceFolderName);
    IPackageFragmentRoot testSourceFragmentRoot =
        (IPackageFragmentRoot) JavaCore.create(testSourceFolder);
    // check create
    if (!testSourceFolder.exists()
        || testSourceFragmentRoot == null
        || !testSourceFragmentRoot.exists()) {
      // create folder
      if (!testSourceFolder.exists()) {
        testSourceFolder.create(true, false, null);
      }
      IClasspathEntry[] classpath = javaProject.getRawClasspath();
      // find last source entry
      int insertIndex = -1;
      for (int i = 0; i < classpath.length; i++) {
        if (classpath[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
          insertIndex = i + 1;
        }
      }
      // insert new source to entries
      IClasspathEntry testSourceEntry = JavaCore.newSourceEntry(testSourceFolder.getFullPath());
      if (insertIndex == -1) {
        classpath = (IClasspathEntry[]) ArrayUtils.add(classpath, testSourceEntry);
      } else {
        classpath = (IClasspathEntry[]) ArrayUtils.add(classpath, insertIndex, testSourceEntry);
      }
      // modify classpath
      javaProject.setRawClasspath(classpath, javaProject.getOutputLocation(), null);
      testSourceFragmentRoot = (IPackageFragmentRoot) JavaCore.create(testSourceFolder);
    }
    //
    setPackageFragmentRoot(testSourceFragmentRoot, true);
    return testSourceFragmentRoot;
  }

  private IPackageFragment handleTestPackage(IJavaElement element,
      IPackageFragmentRoot testSourceFragmentRoot) throws Exception {
    IPackageFragment testPackage = null;
    IPackageFragment elementPackage =
        (IPackageFragment) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
    if (elementPackage != null) {
      testPackage = testSourceFragmentRoot.getPackageFragment(elementPackage.getElementName());
      if (testPackage == null || !testPackage.exists()) {
        testPackage =
            testSourceFragmentRoot.createPackageFragment(
                elementPackage.getElementName(),
                true,
                null);
      }
    }
    //
    setPackageFragment(testPackage, true);
    return elementPackage;
  }

  private void setErrorState() {
    setErrorState("Select GWT project and class under test.");
  }

  private void setErrorState(String message) {
    m_classUnderTestStatus =
        new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, message, null);
    m_moduleId = "";
  }

  protected void updatePageStatus() {
    updateStatus(new IStatus[]{
        m_classUnderTestStatus,
        fContainerStatus,
        fPackageStatus,
        fTypeNameStatus,
        fSuperClassStatus});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Finish
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    //  create GWTTest
    {
      InputStream is;
      try {
        IProject project = getJavaProject().getProject();
        String templatePath = WizardUtils.getTemplatePath(project) + "/TestCase.jvt";
        is = Activator.getFile(templatePath);
      } catch (Throwable e) {
        throw ReflectionUtils.propagate(e);
      }
      try {
        fillTypeFromTemplate(newType, imports, monitor, is);
      } finally {
        IOUtils.closeQuietly(is);
      }
    }
    // ensure com.google.gwt.junit.JUnit import
    try {
      IPackageFragment newTypePackage = newType.getPackageFragment();
      ModuleDescription module = Utils.getSingleModule(newTypePackage);
      DefaultModuleProvider.modify(module, new ModuleModification() {
        public void modify(ModuleElement moduleElement) throws Exception {
          if (moduleElement.getInheritsElement("com.google.gwt.junit.JUnit") == null) {
            moduleElement.addInheritsElement("com.google.gwt.junit.JUnit");
          }
        }
      });
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Substitution support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final ToolkitDescription getToolkitDescription() {
    return ToolkitProvider.DESCRIPTION;
  }

  @Override
  protected String performSubstitutions(String code, ImportsManager imports) {
    code = super.performSubstitutions(code, imports);
    code = StringUtils.replace(code, "%ModuleName%", m_moduleId);
    return code;
  }
}