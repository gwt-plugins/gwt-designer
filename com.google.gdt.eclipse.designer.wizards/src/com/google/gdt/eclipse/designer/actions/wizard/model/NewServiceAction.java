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
package com.google.gdt.eclipse.designer.actions.wizard.model;

import com.google.gdt.eclipse.designer.wizards.model.service.ServiceWizard;

import org.eclipse.wb.internal.core.wizards.AbstractOpenWizardDelegate;

import org.eclipse.jface.wizard.IWizard;

/**
 * Action for adding new GWT RemoteService.
 * 
 * @author scheglov_le
 * @coverage gwt.actions
 */
public class NewServiceAction extends AbstractOpenWizardDelegate {
  @Override
  protected IWizard createWizard() {
    return new ServiceWizard();
  }
}
