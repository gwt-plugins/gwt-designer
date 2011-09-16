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
package com.google.gdt.eclipse.designer.gxt.databinding;

import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.core.editor.IDesignerEditor;
import org.eclipse.wb.core.editor.IEditorPage;
import org.eclipse.wb.core.editor.IEditorPageFactory;
import org.eclipse.wb.internal.core.databinding.ui.BindingDesignPage;

import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;

import java.util.List;

/**
 * 
 * @author lobas_av
 */
public class DesignPageFactory implements IEditorPageFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IDesignPageFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public void createPages(IDesignerEditor editor, List<IEditorPage> pages) {
    if (isGWT(editor) && isGXT(editor)) {
      BindingDesignPage.addPage(pages);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean isGWT(IDesignerEditor editor) {
    try {
      IImportDeclaration[] imports = editor.getCompilationUnit().getImports();
      for (IImportDeclaration importDeclaration : imports) {
        String elementName = importDeclaration.getElementName();
        if (elementName.startsWith("com.google.gwt") || elementName.startsWith("com.extjs.gxt")) {
          return true;
        }
      }
    } catch (Throwable e) {
    }
    return false;
  }

  private static boolean isGXT(IDesignerEditor editor) {
    try {
      IJavaProject javaProject = editor.getCompilationUnit().getJavaProject();
      if (Utils.isGWTProject(javaProject)
          && javaProject.findType("com.extjs.gxt.ui.client.GXT") != null) {
        return true;
      }
    } catch (Throwable e) {
    }
    return false;
  }
}