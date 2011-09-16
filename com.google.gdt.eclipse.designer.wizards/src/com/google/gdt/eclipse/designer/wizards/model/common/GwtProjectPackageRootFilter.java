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
package com.google.gdt.eclipse.designer.wizards.model.common;

import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.jdt.ui.IPackageRootFilter;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * {@link IPackageRootFilter} that selects only GWT projects.
 * 
 * @author scheglov_ke
 * @coverage gwt.wizard
 */
public class GwtProjectPackageRootFilter implements IPackageRootFilter {
  public boolean select(IJavaProject javaProject) {
    return Utils.isGWTProject(javaProject);
  }

  public boolean select(IPackageFragmentRoot packageFragmentRoot) {
    return true;
  }
}
