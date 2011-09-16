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
package com.google.gdt.eclipse.designer.gwtext.actions;

import com.google.gdt.eclipse.designer.gwtext.Activator;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider.ModuleModification;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Operation for configuring GWT module for using GWT-Ext.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.actions
 */
public final class ConfigureGwtExtOperation extends WorkspaceModifyOperation {
  private final ModuleDescription m_module;
  private final IJavaProject m_javaProject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ConfigureGwtExtOperation(ModuleDescription module) {
    m_module = module;
    m_javaProject = JavaCore.create(m_module.getProject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
      InterruptedException {
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        execute0();
      }
    });
  }

  private void execute0() throws Exception {
    Bundle bundle = Platform.getBundle("com.google.gdt.eclipse.designer.GWTExt");
    // ensure jar
    if (!ProjectUtils.hasType(m_javaProject, "com.gwtext.client.widgets.Component")) {
      ProjectUtils.addJar(m_javaProject, bundle, "resources/gwtext.jar", null);
    }
    // ensure ExtJS public resources
    if (!Utils.isExistingResource(m_module, "ext-all.js")) {
      InputStream zipFile = Activator.getFile("resources/publicResources.zip");
      IFolder publicFolder = m_module.getModulePublicFolder();
      IFolder extFolder = publicFolder.getFolder(new Path("js/ext"));
      extractZip(zipFile, extFolder);
    }
    // add elements into module
    DefaultModuleProvider.modify(m_module, new ModuleModification() {
      public void modify(ModuleElement moduleElement) throws Exception {
        moduleElement.addStylesheetElement("js/ext/resources/css/ext-all.css");
        moduleElement.addScriptElement("js/ext/adapter/ext/ext-base.js");
        moduleElement.addScriptElement("js/ext/ext-all.js");
        moduleElement.addInheritsElement("com.gwtext.GwtExt");
      }
    });
  }

  private static void extractZip(InputStream zipFile, IFolder targetFolder) throws Exception {
    ZipInputStream zipInputStream = new ZipInputStream(zipFile);
    while (true) {
      ZipEntry zipEntry = zipInputStream.getNextEntry();
      if (zipEntry == null) {
        break;
      }
      if (!zipEntry.isDirectory()) {
        String entryName = zipEntry.getName();
        byte[] byteArray = IOUtils.toByteArray(zipInputStream);
        IOUtils2.setFileContents(
            targetFolder.getFile(new Path(entryName)),
            new ByteArrayInputStream(byteArray));
      }
      zipInputStream.closeEntry();
    }
    zipInputStream.close();
  }
}