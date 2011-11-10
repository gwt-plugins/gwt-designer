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
package com.google.gdt.eclipse.designer.wizards.model.project;

import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.model.web.WebUtils;
import com.google.gdt.eclipse.designer.preferences.MainPreferencePage;
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.wizards.Activator;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.wizards.DesignerJavaProjectWizard;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author scheglov_ke
 * @coverage gwt.wizard.ui
 */
public class ProjectWizard extends DesignerJavaProjectWizard {
  private CreateModuleWizardPage m_createModulePage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ProjectWizard() {
    super();
    setDefaultPageImageDescriptor(Activator.getImageDescriptor("wizards/project/banner.gif"));
    setWindowTitle("New GWT Java Project");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addPages() {
    if (!MainPreferencePage.validateLocation() && !Utils.hasGPE()) {
      addPage(new ConfigureWizardPage());
    }
    // add standard Java pages
    super.addPages();
    // insert "GWT module" page after first standard Java project wizard page
    {
      // prepare pages
      List<WizardPage> pages = getPagesList();
      // insert page
      {
        m_createModulePage = new CreateModuleWizardPage();
        int index = getWizardNewProjectIndex(pages);
        pages.add(index + 1, m_createModulePage);
        m_createModulePage.setWizard(this);
      }
    }
  }

  /**
   * @return the internal {@link List} of {@link WizardPage}'s from super-wizard.
   */
  @SuppressWarnings("unchecked")
  private List<WizardPage> getPagesList() {
    try {
      Field pagesField = Wizard.class.getDeclaredField("pages");
      pagesField.setAccessible(true);
      return (List<WizardPage>) pagesField.get(this);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  /**
   * @return the index of first standard Java project wizard page.
   */
  private static int getWizardNewProjectIndex(List<WizardPage> pages) {
    for (int i = 0; i < pages.size(); i++) {
      if (pages.get(i) instanceof WizardNewProjectCreationPage) {
        return i;
      }
    }
    return 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Finish
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean performFinish() {
    boolean goodFinish = super.performFinish();
    if (goodFinish) {
      try {
        IJavaProject javaProject = getCreatedElement();
        // configure project
        configureProjectAsGWTProject(javaProject);
        // create module
        m_createModulePage.createModule(javaProject);
      } catch (Throwable e) {
        DesignerPlugin.log(e);
        return false;
      }
    }
    return goodFinish;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Project configuring
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures given {@link IJavaProject} as GWT project - adds GWT library and nature.
   */
  public static final void configureProjectAsGWTProject(IJavaProject javaProject) throws Exception {
    IProject project = javaProject.getProject();
    // add GWT classpath
    if (!ProjectUtils.hasType(javaProject, "com.google.gwt.core.client.GWT")) {
      IClasspathEntry entry;
      if (Utils.hasGPE()) {
        entry = JavaCore.newContainerEntry(new Path("com.google.gwt.eclipse.core.GWT_CONTAINER"));
      } else {
        entry = JavaCore.newVariableEntry(new Path("GWT_HOME/gwt-user.jar"), null, null);
      }
      ProjectUtils.addClasspathEntry(javaProject, entry);
    }
    // add GWT nature
    {
      ProjectUtils.addNature(project, Constants.NATURE_ID);
      if (Utils.hasGPE()) {
        ProjectUtils.addNature(project, "com.google.gwt.eclipse.core.gwtNature");
      }
    }
    // continue
    {
      String webFolderName = WebUtils.getWebFolderName(project);
      // create folders
      ensureCreateFolder(project, webFolderName);
      ensureCreateFolder(project, webFolderName + "/WEB-INF");
      IFolder classesFolder = ensureCreateFolder(project, webFolderName + "/WEB-INF/classes");
      IFolder libFolder = ensureCreateFolder(project, webFolderName + "/WEB-INF/lib");
      // set output
      javaProject.setOutputLocation(classesFolder.getFullPath(), null);
      // copy gwt-servlet.jar
      if (!libFolder.getFile("gwt-servlet.jar").exists()) {
        String servletJarLocation = Utils.getGWTLocation(project) + "/gwt-servlet.jar";
        File srcFile = new File(servletJarLocation);
        File destFile = new File(libFolder.getLocation().toFile(), "gwt-servlet.jar");
        FileUtils.copyFile(srcFile, destFile);
        libFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
      }
    }
  }

  private static IFolder ensureCreateFolder(IProject project, String name) throws CoreException {
    IFolder folder = project.getFolder(new Path(name));
    if (!folder.exists()) {
      folder.create(true, true, null);
    }
    return folder;
  }
}