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
package com.google.gdt.eclipse.designer.gwtext.wizards.window;

import com.google.gdt.eclipse.designer.gwtext.wizards.GwtExtWizard;

import org.eclipse.wb.internal.core.wizards.AbstractDesignWizardPage;

/**
 * Wizard for <code>com.gwtext.client.widgets.Window</code>.
 * 
 * @author sablin_aa
 * @coverage GWTExt.wizard
 */
public final class WindowWizard extends GwtExtWizard {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WindowWizard() {
    setWindowTitle("New GWT-Ext Window");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractDesignWizardPage createMainPage() {
    return new WindowWizardPage();
  }
}
