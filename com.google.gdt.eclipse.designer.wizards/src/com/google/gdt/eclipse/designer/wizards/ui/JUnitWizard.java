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

import com.google.gdt.eclipse.designer.wizards.Activator;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.wizards.AbstractDesignWizardPage;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.ui.JavaUI;

/**
 * @author lobas_av
 * @coverage gwt.wizard.ui
 */
public final class JUnitWizard extends GwtWizard {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractDesignWizardPage createMainPage() {
    m_mainPage = new JUnitWizardPage();
    return m_mainPage;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Finish
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
    IPackageFragmentRoot fragmentRoot = m_mainPage.getPackageFragmentRoot();
    if (fragmentRoot != null) {
      IJavaProject project = fragmentRoot.getJavaProject();
      if (project != null && project.findType("junit.framework.TestCase") == null) {
        try {
          ProjectUtils.addPluginLibraries(project, "org.junit");
        } catch (Throwable e) {
          DesignerPlugin.log(e);
          throw new CoreException(new Status(IStatus.ERROR,
              Activator.PLUGIN_ID,
              0,
              "Unable add JUnit jar to classpath " + project.getElementName(),
              e));
        }
      }
    }
    m_mainPage.createType(monitor);
  }

  @Override
  protected void openEditor(IFile file) {
    openResource(file, JavaUI.ID_CU_EDITOR);
  }
}