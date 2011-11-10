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
package com.google.gdt.eclipse.designer.gxt.wizards;

import com.google.gdt.eclipse.designer.wizards.ui.GwtWizard;

import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.wizards.ErrorMessageWizardPage;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.wizard.Wizard;

/**
 * Abstract {@link Wizard} for GXT.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.wizard
 */
public abstract class GxtWizard extends GwtWizard {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean validateSelection() {
    IJavaProject javaProject = getJavaProject();
    if (javaProject == null
        || !ProjectUtils.hasType(javaProject, "com.extjs.gxt.ui.client.widget.Component")) {
      addPage(new ErrorMessageWizardPage("Project is not configured for GXT.",
          "Use \"Configure for using Ext GWT (GXT)\" action on module."));
      return false;
    }
    return super.validateSelection();
  }
}
