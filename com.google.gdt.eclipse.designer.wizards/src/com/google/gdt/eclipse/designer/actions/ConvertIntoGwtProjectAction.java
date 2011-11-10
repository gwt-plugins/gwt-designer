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
package com.google.gdt.eclipse.designer.actions;

import com.google.gdt.eclipse.designer.wizards.model.project.ProjectWizard;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Action for converting selected project into GWT project.
 * 
 * @author scheglov_ke
 * @coverage gwt.actions
 */
public class ConvertIntoGwtProjectAction extends Action implements IWorkbenchWindowActionDelegate {
  private IJavaProject m_javaProject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IWorkbenchWindowActionDelegate
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbenchWindow window) {
  }

  public void dispose() {
  }

  public void selectionChanged(IAction action, ISelection selection) {
    m_javaProject = getSelectedProject(selection);
    action.setEnabled(m_javaProject != null);
  }

  public void run(IAction action) {
    if (!MessageDialog.openConfirm(
        DesignerPlugin.getShell(),
        "Confirm",
        "Do you really want to convert project '"
            + m_javaProject.getElementName()
            + "' into GWT project?")) {
      return;
    }
    //
    try {
      ProjectWizard.configureProjectAsGWTProject(m_javaProject);
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Finds {@link IProject} corresponding given {@link ISelection}.
   */
  private static IJavaProject getSelectedProject(ISelection selection) {
    try {
      if (selection instanceof IStructuredSelection) {
        IStructuredSelection structuredSelection = (IStructuredSelection) selection;
        Object selectedObject = structuredSelection.getFirstElement();
        return getProjectFromResource(selectedObject);
      } else if (selection instanceof ITextSelection) {
        IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
        IEditorInput editorInput = activeEditor.getEditorInput();
        if (editorInput instanceof IFileEditorInput) {
          IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
          return getProjectFromResource(fileEditorInput.getFile());
        }
      }
    } catch (Throwable e) {
    }
    return null;
  }

  /**
   * If given object is {@link IResource} instance, finds corresponding {@link IProject}.
   */
  private static IJavaProject getProjectFromResource(Object o) throws CoreException {
    if (o instanceof IAdaptable) {
      o = ((IAdaptable) o).getAdapter(IResource.class);
    }
    if (o instanceof IResource) {
      IResource resource = (IResource) o;
      IProject project = resource.getProject();
      if (project.hasNature(JavaCore.NATURE_ID)) {
        return JavaCore.create(project);
      }
    }
    return null;
  }
}
