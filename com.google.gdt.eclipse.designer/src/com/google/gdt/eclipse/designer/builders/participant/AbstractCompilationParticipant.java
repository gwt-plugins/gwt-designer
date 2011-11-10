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

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.List;

/**
 * Abstract compilation participant for creating problem markers. We use it in GWT for checking that
 * all imported/used classes are in inherited modules.
 * 
 * @author scheglov_ke
 * @coverage gwt.compilation.participant
 */
public abstract class AbstractCompilationParticipant extends CompilationParticipant {
  private final String m_markerID;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractCompilationParticipant(String marker_id) {
    m_markerID = marker_id;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Compilation problems
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void processAnnotations(final BuildContext[] contexts) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        processAnnotationsEx(contexts);
      }
    });
  }

  /**
   * Implementation of {@link #processAnnotations(BuildContext[])} that can throw any
   * {@link Exception}.
   */
  private void processAnnotationsEx(BuildContext[] contexts) throws Exception {
    // prepare resources and markers
    final List<IFile> resources = Lists.newArrayList();
    final List<MarkerInfo> newMarkers = Lists.newArrayList();
    for (BuildContext context : contexts) {
      IFile file = context.getFile();
      // prepare AST unit for this file
      ICompilationUnit modelUnit = JavaCore.createCompilationUnitFrom(file);
      CompilationUnit astUnit = Utils.parseUnit(modelUnit);
      // add resources and markers
      resources.add(file);
      addMarkers(newMarkers, file, modelUnit, astUnit);
    }
    // delete markers from resources and add new markers
    {
      IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
        public void run(IProgressMonitor monitor) throws CoreException {
          // delete markers
          for (IFile resource : resources) {
            resource.deleteMarkers(m_markerID, false, IResource.DEPTH_INFINITE);
          }
          // add markers
          for (MarkerInfo markerInfo : newMarkers) {
            markerInfo.createMarker(m_markerID);
          }
        }
      };
      ResourcesPlugin.getWorkspace().run(runnable, null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Compiling
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract void addMarkers(List<MarkerInfo> newMarkers,
      IFile file,
      ICompilationUnit modelUnit,
      CompilationUnit astUnit) throws Exception;
}
