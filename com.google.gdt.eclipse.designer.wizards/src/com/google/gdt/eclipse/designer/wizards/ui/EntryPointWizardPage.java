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

import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.model.module.EntryPointElement;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider.ModuleModification;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.wizards.Activator;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.List;

/**
 * @author lobas_av
 * @coverage gwt.wizard.ui
 */
public final class EntryPointWizardPage extends GwtWizardPage {
  private static final List<String> INTERFACES_NAMES =
      ImmutableList.of(Constants.CLASS_ENTRY_POINT);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EntryPointWizardPage() {
    setTitle("Create EntryPoint");
    setImageDescriptor(Activator.getImageDescriptor("wizards/EntryPoint/banner.gif"));
    setDescription("Create empty EntryPoint");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuration
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initTypePage(IJavaElement elem) {
    super.initTypePage(elem);
    setSuperInterfaces(INTERFACES_NAMES, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(final IType newType,
      ImportsManager imports,
      IProgressMonitor monitor) throws CoreException {
    // create EntryPoint class
    InputStream stream = getTemplate("EntryPoint.jvt");
    try {
      fillTypeFromTemplate(newType, imports, monitor, stream);
    } finally {
      IOUtils.closeQuietly(stream);
    }
    // add new entry-point definition
    try {
      IPackageFragment newTypePackage = newType.getPackageFragment();
      ModuleDescription module = Utils.getSingleModule(newTypePackage);
      DefaultModuleProvider.modify(module, new ModuleModification() {
        public void modify(ModuleElement moduleElement) throws Exception {
          EntryPointElement entryPointElement = new EntryPointElement();
          moduleElement.addChild(entryPointElement);
          entryPointElement.setClassName(newType.getFullyQualifiedName());
        }
      });
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }
}