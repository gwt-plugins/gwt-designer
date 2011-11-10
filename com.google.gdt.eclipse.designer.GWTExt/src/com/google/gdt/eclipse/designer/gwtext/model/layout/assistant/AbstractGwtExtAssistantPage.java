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
package com.google.gdt.eclipse.designer.gwtext.model.layout.assistant;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;

import org.eclipse.swt.widgets.Composite;

/**
 * GWT-Ext assistant for <code>Layout</code> & <code>LayoutData</code>.
 * 
 * @author sablin_aa
 * @coverage GWTExt.model.layout.assistant
 */
public abstract class AbstractGwtExtAssistantPage extends AbstractAssistantPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractGwtExtAssistantPage(Composite parent, Object selection) {
    super(parent, selection);
  }
}