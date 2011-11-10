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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Simple container class for attributes of IMarker.
 * 
 * @author scheglov_ke
 * @coverage gwt.compilation.participant
 */
public class MarkerInfoImportModule extends MarkerInfo {
  public static final String MODULE_NAME_TO_IMPORT = "gwt.moduleNameToImport";
  private final String m_moduleNameToImport;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MarkerInfoImportModule(IResource resource,
      int start,
      int end,
      int line,
      int severity,
      String message,
      String moduleNameToImport) {
    super(resource, start, end, line, IMarker.SEVERITY_ERROR, message);
    m_moduleNameToImport = moduleNameToImport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureMarker(IMarker marker) throws CoreException {
    super.configureMarker(marker);
    marker.setAttribute(MODULE_NAME_TO_IMPORT, m_moduleNameToImport);
  }
}
