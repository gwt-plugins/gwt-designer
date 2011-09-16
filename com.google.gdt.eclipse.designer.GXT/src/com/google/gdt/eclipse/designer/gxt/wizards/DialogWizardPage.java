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
package com.google.gdt.eclipse.designer.gxt.wizards;

import com.google.gdt.eclipse.designer.gxt.Activator;
import com.google.gdt.eclipse.designer.wizards.ui.GwtWizardPage;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * Wizard page for <code>com.extjs.gxt.ui.client.widget.Dialog</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.wizard
 */
public final class DialogWizardPage extends GwtWizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DialogWizardPage() {
    setTitle("Create Dialog");
    setImageDescriptor(Activator.getImageDescriptor("wizards/Dialog/banner.png"));
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
    setSuperClass("com.extjs.gxt.ui.client.widget.Dialog", false);
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Template
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final InputStream getTemplate2(String path) {
    try {
      return Activator.getFile("templates/" + path);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }
}
