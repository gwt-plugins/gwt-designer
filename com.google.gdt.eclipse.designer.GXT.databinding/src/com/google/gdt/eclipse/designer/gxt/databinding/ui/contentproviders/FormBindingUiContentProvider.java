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
import com.google.gdt.eclipse.designer.gxt.databinding.model.beans.BeanObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.FieldBindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.FormBindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.widgets.WidgetObserveInfo;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.MultiTargetRunnable;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.TabContainerConfiguration;
import org.eclipse.wb.internal.core.utils.dialogfields.BooleanDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;

import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public class FormBindingUiContentProvider extends BindingsUiContentProvider {
  private final BooleanDialogField m_autoBindEditor;
  private final MultiTargetRunnable m_multiTargetRunnable;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormBindingUiContentProvider(DatabindingsProvider provider,
      TabContainerConfiguration configuration,
      BooleanDialogField autoBindEditor,
      MultiTargetRunnable multiTargetRunnable,
      FormBindingInfo binding) {
    super(provider, configuration, binding);
    m_autoBindEditor = autoBindEditor;
    m_multiTargetRunnable = multiTargetRunnable;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TabContainerUIContentProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  @SuppressWarnings("unchecked")
  protected void chooseAddPage(MenuManager menuManager, final int insertIndex) throws Exception {
    List<IUiContentProvider> providers = getProviders();
    WidgetObserveInfo formPanel = (WidgetObserveInfo) m_binding.getTarget();
    for (final WidgetObserveInfo field : formPanel.getChildren()) {
      boolean free = true;
      for (IUiContentProvider provider : providers) {
        UIContentContainer<FieldBindingInfo> fieldProvider =
            (UIContentContainer<FieldBindingInfo>) provider;
        if (field == fieldProvider.getBinding().getTarget()) {
          free = false;
          break;
        }
      }
      if (free) {
        menuManager.add(new Action(field.getPresentation().getTextForBinding()) {
          @Override
          public void run() {
            try {
              FieldBindingInfo binding = m_binding.createFieldBinding(field);
              UIContentContainer<FieldBindingInfo> provider =
                  new UIContentContainer<FieldBindingInfo>(m_provider, binding, "Binding: ");
              createPage(insertIndex, provider, true);
              if (m_multiTargetRunnable != null) {
                addTargetRouter(provider, true);
              }
              configure();
              m_listener.calculateFinish();
            } catch (Throwable e) {
              DesignerPlugin.log(e);
            }
          }
        });
      }
    }
  }

  @Override
  protected String getBindingText(FieldBindingInfo binding) throws Exception {
    String text = super.getBindingText(binding);
    if (binding.isAutobind()) {
      text += " [AUTO]";
    }
    return text;
  }

  @SuppressWarnings("unchecked")
  private void handleAutobind(boolean autobind) {
    try {
      if (autobind) {
        List<FieldBindingInfo> bindings = Lists.newArrayList();
        ((FormBindingInfo) m_binding).createAutobindings(bindings);
        //
        for (FieldBindingInfo binding : bindings) {
          createPage(
              -1,
              new UIContentContainer<FieldBindingInfo>(m_provider, binding, "Binding: "),
              false);
        }
        //
        if (!bindings.isEmpty()) {
          configure();
          m_listener.calculateFinish();
        }
      } else {
        boolean refresh = false;
        for (IUiContentProvider provider : getProviders()) {
          UIContentContainer<FieldBindingInfo> fieldProvider =
              (UIContentContainer<FieldBindingInfo>) provider;
          if (fieldProvider.getBinding().isAutobind()) {
            deleteTabItem(providerToItem(provider));
            refresh = true;
          }
        }
        if (refresh) {
          postDelete();
        }
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void postDelete(IUiContentProvider _provider) throws Exception {
    if (m_multiTargetRunnable != null) {
      UIContentContainer<FieldBindingInfo> provider =
          (UIContentContainer<FieldBindingInfo>) _provider;
      for (IUiContentProvider iprovider : provider.getProviders()) {
        if (iprovider instanceof ChooseClassAndPropertiesUiContentProvider) {
          m_multiTargetRunnable.removeTarget((ChooseClassUiContentProvider) iprovider);
          break;
        }
      }
    }
  }

  private void addTargetRouter(UIContentContainer<FieldBindingInfo> provider, boolean update) {
    for (IUiContentProvider iprovider : provider.getProviders()) {
      if (iprovider instanceof ChooseClassAndPropertiesUiContentProvider) {
        m_multiTargetRunnable.addTarget((ChooseClassUiContentProvider) iprovider, update);
        break;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  @SuppressWarnings("unchecked")
  public void updateFromObject() throws Exception {
    super.updateFromObject();
    //
    m_autoBindEditor.setDialogFieldListener(new IDialogFieldListener() {
      public void dialogFieldChanged(DialogField field) {
        handleAutobind(m_autoBindEditor.getSelection());
      }
    });
    //
    if (m_multiTargetRunnable != null) {
      for (IUiContentProvider provider : getProviders()) {
        UIContentContainer<FieldBindingInfo> fieldProvider =
            (UIContentContainer<FieldBindingInfo>) provider;
        if (!fieldProvider.getBinding().isAutobind()) {
          addTargetRouter(fieldProvider, false);
        }
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void saveToObject() throws Exception {
    BeanObserveInfo gridSelectionModel = ((FormBindingInfo) m_binding).getGridSelectionModel();
    for (IUiContentProvider provider : getProviders()) {
      UIContentContainer<FieldBindingInfo> fieldProvider =
          (UIContentContainer<FieldBindingInfo>) provider;
      fieldProvider.getBinding().setGridSelectionModel(gridSelectionModel);
      fieldProvider.getBinding().updateGridSelectionModel();
    }
    //
    super.saveToObject();
  }
}