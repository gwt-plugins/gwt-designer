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
package com.google.gdt.eclipse.designer.gxt.databinding.wizards.autobindings;

import com.google.gdt.eclipse.designer.ToolkitProvider;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.databinding.wizards.autobindings.IAutomaticDatabindingProvider;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.dialogfields.StatusUtils;

import org.eclipse.core.runtime.IStatus;

/**
 * 
 * @author lobas_av
 * 
 */
public final class AutomaticDatabindingFirstPage
    extends
      org.eclipse.wb.internal.core.databinding.wizards.autobindings.AutomaticDatabindingFirstPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AutomaticDatabindingFirstPage(IAutomaticDatabindingProvider databindingProvider,
      String initialBeanClassName) {
    super(databindingProvider, initialBeanClassName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void updateStatus(IStatus status) {
    super.updateStatus(status);
    // check for client package
    if (!status.matches(IStatus.ERROR)) {
      try {
        if (!Utils.isModuleSourcePackage(getPackageFragment())) {
          super.updateStatus(StatusUtils.createError("GWT widgets can be used only in client package of some GWT module."));
        }
      } catch (Throwable e) {
        super.updateStatus(StatusUtils.createError("Exception: " + e.getMessage()));
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Substitution support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ToolkitDescription getToolkitDescription() {
    return ToolkitProvider.DESCRIPTION;
  }
}