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
public class MarkerInfo {
  private final IResource m_resource;
  private final int m_start;
  private final int m_end;
  private final int m_line;
  private final int m_severity;
  private final String m_message;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MarkerInfo(IResource resource, int start, int end, int line, int severity, String message) {
    m_resource = resource;
    m_start = start;
    m_end = end;
    m_line = line;
    m_severity = severity;
    m_message = message;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Copy this info into the provided marker.
   */
  public final IMarker createMarker(String type) throws CoreException {
    IMarker marker = m_resource.createMarker(type);
    marker.setAttribute(IMarker.CHAR_START, m_start);
    marker.setAttribute(IMarker.CHAR_END, m_end);
    marker.setAttribute(IMarker.LINE_NUMBER, m_line);
    marker.setAttribute(IMarker.SEVERITY, m_severity);
    marker.setAttribute(IMarker.MESSAGE, m_message);
    configureMarker(marker);
    return marker;
  }

  /**
   * Configures created {@link IMarker}.
   */
  protected void configureMarker(IMarker marker) throws CoreException {
  }
}
