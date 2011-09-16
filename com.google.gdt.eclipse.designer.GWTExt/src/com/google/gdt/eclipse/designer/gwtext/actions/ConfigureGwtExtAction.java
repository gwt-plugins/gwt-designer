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

import com.google.gdt.eclipse.designer.actions.AbstractModuleAction;
import com.google.gdt.eclipse.designer.actions.wizard.model.IModuleConfigurator;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.wizards.AbstractActionDelegate;

import org.eclipse.jface.action.IAction;

/**
 * Action for configuring GWT module for using GWT-Ext.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.actions
 */
public final class ConfigureGwtExtAction extends AbstractActionDelegate
    implements
      IModuleConfigurator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IActionDelegate
  //
  ////////////////////////////////////////////////////////////////////////////
  public void run(IAction action) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        ModuleDescription module = AbstractModuleAction.getSelectedModule(getSelection());
        configure(module);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IModuleConfigurator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(ModuleDescription module) throws Exception {
    new ConfigureGwtExtOperation(module).run(null);
  }
}