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
package com.google.gdt.eclipse.designer.gxt.databinding.ui.contentproviders;

import com.google.gdt.eclipse.designer.gxt.databinding.DatabindingsProvider;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.BindingInfo;

import org.eclipse.wb.internal.core.databinding.ui.editor.EmptyPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;

/**
 * {@link IUiContentProvider} which is a container for other {@link IUiContentProvider}'s.
 * 
 * @author lobas_av
 * @coverage bindings.gxt.ui
 */
public class UIContentContainer<T extends BindingInfo>
    extends
      org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.UIContentContainer<T> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UIContentContainer(DatabindingsProvider provider, T binding, String errorPrefix)
      throws Exception {
    super(binding, errorPrefix);
    m_binding.createContentProviders(m_providers, EmptyPageListener.INSTANCE, provider);
  }
}