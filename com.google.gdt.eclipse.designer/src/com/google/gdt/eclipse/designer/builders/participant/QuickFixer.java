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
package com.google.gdt.eclipse.designer.builders.participant;

import com.google.gdt.eclipse.designer.model.module.InheritsElement;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider.ModuleModification;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.IMarkerResolutionGenerator2;

/**
 * Implementation of {@link IMarkerResolutionGenerator} for fixing GWT problems.
 * 
 * @author scheglov_ke
 * @coverage gwt.compilation.participant
 */
public class QuickFixer implements IMarkerResolutionGenerator2 {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IMarkerResolutionGenerator2
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean hasResolutions(IMarker marker) {
    return marker.getAttribute(MarkerInfoImportModule.MODULE_NAME_TO_IMPORT, null) != null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMarkerResolutionGenerator
  //
  ////////////////////////////////////////////////////////////////////////////
  public IMarkerResolution[] getResolutions(IMarker marker) {
    String moduleNameToImport =
        marker.getAttribute(MarkerInfoImportModule.MODULE_NAME_TO_IMPORT, null);
    if (moduleNameToImport != null) {
      return new IMarkerResolution[]{new ModuleImportResolution(moduleNameToImport)};
    }
    return new IMarkerResolution[0];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ModuleImportResolution
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ModuleImportResolution implements IMarkerResolution {
    private final String m_moduleNameToImport;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ModuleImportResolution(String moduleNameToImport) {
      m_moduleNameToImport = moduleNameToImport;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMarkerResolution
    //
    ////////////////////////////////////////////////////////////////////////////
    public String getLabel() {
      return "Import GWT module " + m_moduleNameToImport;
    }

    public void run(final IMarker marker) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          ModuleDescription moduleDescription = Utils.getSingleModule(marker.getResource());
          DefaultModuleProvider.modify(moduleDescription, new ModuleModification() {
            public void modify(ModuleElement moduleElement) throws Exception {
              InheritsElement inheritsElement = new InheritsElement();
              moduleElement.addChild(inheritsElement);
              inheritsElement.setName(m_moduleNameToImport);
            }
          });
        }
      });
    }
  }
}
