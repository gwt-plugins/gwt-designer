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
package com.google.gdt.eclipse.designer.util.type;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Helper for selecting type.
 * 
 * @author scheglov_ke
 * @coverage gwt.util.beanSelection
 */
public class ChooseBeanDialog {
  /**
   * Ask user choose type, accessible from given project and filtered using given contributors.
   */
  public static String chooseType(IPackageFragment packageFragment,
      IChooseBeanContributor[] contributors) throws JavaModelException {
    IJavaProject javaProject = packageFragment.getJavaProject();
    IJavaSearchScope searchScope =
        SearchEngine.createJavaSearchScope(new IJavaElement[]{javaProject});
    IRunnableContext context = DesignerPlugin.getActiveWorkbenchWindow();
    // prepare extension
    ChooseBeanTypeSelectionExtension extension =
        new ChooseBeanTypeSelectionExtension(packageFragment, searchScope, contributors);
    // prepare dialog dialog
    SelectionDialog dialog =
        JavaUI.createTypeDialog(
            DesignerPlugin.getShell(),
            context,
            searchScope,
            IJavaElementSearchConstants.CONSIDER_CLASSES,
            false,
            "",
            extension);
    dialog.setTitle("Choose a Widget");
    dialog.setMessage("Choose a GWT Widget (? = any character, * = any string)");
    // open and resutrn result
    if (dialog.open() == Window.OK) {
      IType type = (IType) dialog.getResult()[0];
      return type.getFullyQualifiedName();
    }
    // cancelled
    return null;
  }
}
