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