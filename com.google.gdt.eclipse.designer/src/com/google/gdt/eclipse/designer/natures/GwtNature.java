/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.natures;

import com.google.gdt.eclipse.designer.common.Constants;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * {@link IProjectNature} for GWT projects.
 * 
 * @author scheglov_ke
 * @coverage gwt
 */
public final class GwtNature implements IProjectNature {
  private IProject m_project;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IProjectNature: project
  //
  ////////////////////////////////////////////////////////////////////////////
  public IProject getProject() {
    return m_project;
  }

  public void setProject(IProject project) {
    m_project = project;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IProjectNature: configure
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure() throws CoreException {
    IProjectDescription description = m_project.getDescription();
    ICommand[] oldBuilders = description.getBuildSpec();
    //
    ICommand[] newBuilders = new ICommand[oldBuilders.length + 1];
    System.arraycopy(oldBuilders, 0, newBuilders, 1, oldBuilders.length);
    newBuilders[0] = description.newCommand();
    newBuilders[0].setBuilderName(Constants.BUILDER_ID);
    //
    description.setBuildSpec(newBuilders);
    m_project.setDescription(description, IResource.FORCE, null);
  }

  public void deconfigure() throws CoreException {
    IProjectDescription description = m_project.getDescription();
    ICommand[] oldBuilders = description.getBuildSpec();
    //
    ICommand[] newBuilders = new ICommand[oldBuilders.length - 1];
    int newIndex = 0;
    for (int oldIndex = 0; oldIndex < oldBuilders.length; oldIndex++) {
      ICommand command = oldBuilders[oldIndex];
      if (!command.getBuilderName().equals(Constants.BUILDER_ID)) {
        newBuilders[newIndex++] = command;
      }
    }
    //
    description.setBuildSpec(newBuilders);
    m_project.setDescription(description, IResource.FORCE, null);
  }
}
