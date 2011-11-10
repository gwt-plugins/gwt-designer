/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
