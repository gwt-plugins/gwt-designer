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
package com.google.gdt.eclipse.designer.uibinder.wizards;

import com.google.gdt.eclipse.designer.wizards.ui.GwtWizardPage;

import org.eclipse.wb.internal.core.utils.IOUtils2;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;

import org.apache.commons.lang.StringUtils;

import java.io.InputStream;

/**
 * Abstract {@link WizardPage} for GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.wizards
 */
public abstract class UiBinderWizardPage extends GwtWizardPage {
  private IFile m_javaFile;
  private IFile m_uiFile;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UiBinderWizardPage() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    String templatePath = getTemplatePath_Java();
    InputStream template = Activator.getFile(templatePath);
    fillTypeFromTemplate(newType, imports, monitor, template);
    m_javaFile = (IFile) newType.getUnderlyingResource();
  }

  protected void createUI() throws Exception {
    IType newType = getCreatedType();
    // prepare template
    String template;
    {
      String templatePath = getTemplatePath_UI();
      InputStream templateStream = Activator.getFile(templatePath);
      template = IOUtils2.readString(templateStream);
    }
    // prepare content
    String content;
    {
      String qualifiedTypeName = newType.getFullyQualifiedName();
      content = StringUtils.replace(template, "%TypeName%", qualifiedTypeName);
    }
    // create UiBinder file
    IFolder folder = (IFolder) getPackageFragment().getUnderlyingResource();
    m_uiFile = folder.getFile(newType.getElementName() + ".ui.xml");
    IOUtils2.setFileContents(m_uiFile, content);
    m_uiFile.setCharset("UTF-8", null);
  }

  /**
   * @return the path to the Java file template in {@link Activator}.
   */
  protected abstract String getTemplatePath_Java();

  /**
   * @return the path to the UiBinder file template in {@link Activator}.
   */
  protected abstract String getTemplatePath_UI();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the created Java {@link IFile}.
   */
  public IFile getFileJava() {
    return m_javaFile;
  }

  /**
   * @return the create UiBinder {@link IFile}.
   */
  public IFile getFileUI() {
    return m_uiFile;
  }
}