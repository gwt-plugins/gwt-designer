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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.ui.dialogs.ITypeInfoFilterExtension;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Accepts all types.
 * 
 * @author scheglov_ke
 * @coverage gwt.util.beanSelection
 */
public class AllTypesChooseBeanContributor implements IChooseBeanContributor {
  public String getName() {
    return "All Types";
  }

  public ITypeInfoFilterExtension getFilter(IPackageFragment pkg, IProgressMonitor monitor) {
    return null;
  }

  public ImageDescriptor getImage() {
    return null;
  }
}
