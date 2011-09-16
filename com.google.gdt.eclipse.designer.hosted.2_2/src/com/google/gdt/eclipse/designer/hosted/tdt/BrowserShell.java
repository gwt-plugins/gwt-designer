/*
 * Copyright 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gdt.eclipse.designer.hosted.tdt;

import com.google.gdt.eclipse.designer.hosted.IBrowserShell;
import com.google.gwt.dev.shell.designtime.ModuleSpace;

/**
 * Represents an individual browser window and all of its controls.
 * @coverage gwtHosted
 */
public abstract class BrowserShell implements IBrowserShell {
  private IBrowserShellHost host;
  protected ModuleSpace moduleSpace;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void setHost(IBrowserShellHost host) {
    this.host = host;
  }

  public final IBrowserShellHost getHost() {
    return host;
  }

  public void dispose() {
    host = null;
  }

  public final ModuleSpace getModuleSpace() {
    return moduleSpace;
  }
}
