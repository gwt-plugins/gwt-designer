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
package com.google.gdt.eclipse.designer.gxt.model.layout.assistant;

import com.google.gdt.eclipse.designer.gxt.model.layout.LayoutInfo;

import org.eclipse.wb.core.model.ObjectInfo;

/**
 * GXT provider for layout assistant pages.
 * 
 * @author sablin_aa
 * @coverage ExtGWT.model.layout.assistant
 */
public abstract class LayoutAssistantSupport<T extends LayoutInfo>
    extends
      org.eclipse.wb.core.editor.actions.assistant.LayoutAssistantSupport {
  protected final T m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutAssistantSupport(T layout) {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutAssistantSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final String getConstraintsPageTitle() {
    return "Layout Data";
  }

  @Override
  protected final ObjectInfo getContainer() {
    return m_layout.getContainer();
  }
}