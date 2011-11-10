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
package com.google.gdt.eclipse.designer.smart.wizards;

import com.google.gdt.eclipse.designer.smart.Activator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * Wizard page for <code>com.smartgwt.client.widgets.Dialog</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.wizard
 */
public final class DialogWizardPage extends SmartGwtWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DialogWizardPage() {
    setTitle("Create Dialog");
    setImageDescriptor(Activator.getImageDescriptor("wizards/banner.png"));
    setDescription("Create empty Dialog");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuration
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initTypePage(IJavaElement elem) {
    super.initTypePage(elem);
    setSuperClass("com.smartgwt.client.widgets.Dialog", false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    InputStream stream = getTemplate2("Dialog.jvt");
    try {
      fillTypeFromTemplate(newType, imports, monitor, stream);
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }
}
