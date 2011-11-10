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
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.wizards.Activator;
import com.google.gdt.eclipse.designer.wizards.WizardUtils;

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.dialogfields.StatusUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.wizards.TemplateDesignWizardPage;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;

import java.io.InputStream;

/**
 * Abstract page for any template-based UI element in GWT.
 * 
 * @author scheglov_ke
 * @coverage gwt.wizard.ui
 */
public abstract class GwtWizardPage extends TemplateDesignWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void updateStatus(IStatus status) {
    super.updateStatus(status);
    // check for client package
    if (!status.matches(IStatus.ERROR)) {
      try {
        if (!Utils.isModuleSourcePackage(getPackageFragment())) {
          super.updateStatus(StatusUtils.createError("GWT widgets can be used only in client package of some GWT module."));
        }
      } catch (Throwable e) {
        super.updateStatus(StatusUtils.createError("Exception: " + e.getMessage()));
      }
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Template
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final InputStream getTemplate(String templateName) {
    try {
      IProject project = getJavaProject().getProject();
      String templatePath = WizardUtils.getTemplatePath(project) + "visual/" + templateName;
      return Activator.getFile(templatePath);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }
}
