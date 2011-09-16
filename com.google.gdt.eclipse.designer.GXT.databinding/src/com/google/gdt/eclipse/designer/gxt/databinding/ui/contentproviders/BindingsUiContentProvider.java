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

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gxt.databinding.DatabindingsProvider;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.BindingsInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.FieldBindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.widgets.WidgetObserveInfo;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.TabContainerConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.TabContainerUiContentProvider;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.custom.CTabItem;

import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public class BindingsUiContentProvider extends TabContainerUiContentProvider {
  protected final DatabindingsProvider m_provider;
  protected final BindingsInfo m_binding;
  private List<WidgetObserveInfo> m_fields;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindingsUiContentProvider(DatabindingsProvider provider,
      TabContainerConfiguration configuration,
      BindingsInfo binding) {
    super(configuration);
    m_provider = provider;
    m_binding = binding;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TabContainerUIContentProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void fillFields(List<WidgetObserveInfo> fields, WidgetObserveInfo field) {
    if (field.isField()) {
      fields.add(field);
    }
    for (WidgetObserveInfo childField : field.getChildren()) {
      fillFields(fields, childField);
    }
  }

  @Override
  protected void chooseAddPage(MenuManager menuManager, final int insertIndex) throws Exception {
    if (m_fields == null) {
      m_fields = Lists.newArrayList();
      WidgetObserveInfo root =
          (WidgetObserveInfo) m_provider.getWidgetsContainer().getObservables().get(0);
      fillFields(m_fields, root);
    }
    //
    for (final WidgetObserveInfo field : m_fields) {
      menuManager.add(new Action(field.getPresentation().getTextForBinding()) {
        @Override
        public void run() {
          try {
            FieldBindingInfo binding = m_binding.createFieldBinding(field);
            createPage(insertIndex, new UIContentContainer<FieldBindingInfo>(m_provider,
                binding,
                "Binding: "), true);
            configure();
            m_listener.calculateFinish();
          } catch (Throwable e) {
            DesignerPlugin.log(e);
          }
        }
      });
    }
  }

  @Override
  protected IUiContentProvider createNewPageContentProvider() throws Exception {
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void configute(CTabItem tabItem, int index, IUiContentProvider provider) {
    try {
      UIContentContainer<FieldBindingInfo> fieldProvider =
          (UIContentContainer<FieldBindingInfo>) provider;
      tabItem.setText(getBindingText(fieldProvider.getBinding()));
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  protected String getBindingText(FieldBindingInfo binding) throws Exception {
    return binding.getTargetPresentationText();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() throws Exception {
    List<IUiContentProvider> providers = Lists.newArrayList();
    for (FieldBindingInfo binding : m_binding.getFieldBindings()) {
      providers.add(new UIContentContainer<FieldBindingInfo>(m_provider, binding, "Binding: "));
    }
    updateFromObject(providers);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void saveToObject(List<IUiContentProvider> providers) throws Exception {
    List<FieldBindingInfo> bindings = Lists.newArrayList();
    //
    for (IUiContentProvider provider : providers) {
      UIContentContainer<FieldBindingInfo> fieldProvider =
          (UIContentContainer<FieldBindingInfo>) provider;
      bindings.add(fieldProvider.getBinding());
    }
    //
    m_binding.setFieldBindings(bindings, m_provider);
  }
}