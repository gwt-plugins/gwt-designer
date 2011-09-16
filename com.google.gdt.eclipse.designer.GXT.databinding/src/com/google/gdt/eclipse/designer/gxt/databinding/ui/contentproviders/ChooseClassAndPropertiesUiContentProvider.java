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
import com.google.gdt.eclipse.designer.gxt.databinding.model.beans.BeanPropertyObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.beans.BeanSupport;
import com.google.gdt.eclipse.designer.gxt.databinding.ui.providers.PropertyAdapterLabelProvider;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;

import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public abstract class ChooseClassAndPropertiesUiContentProvider
    extends
      org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesUiContentProvider {
  private final BeanSupport m_beanSupport;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ChooseClassAndPropertiesUiContentProvider(ChooseClassAndPropertiesConfiguration configuration,
      BeanSupport beanSupport) {
    super(configuration);
    m_beanSupport = beanSupport;
    if (configuration.getPropertiesLabelProvider() == null) {
      configuration.setPropertiesLabelProvider(new PropertyAdapterLabelProvider());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<PropertyAdapter> getProperties(Class<?> choosenClass) throws Exception {
    List<PropertyAdapter> adapters = Lists.newArrayList();
    for (BeanPropertyObserveInfo property : m_beanSupport.getProperties(choosenClass, null, false)) {
      adapters.add(new PropertyAdapter(property.getPresentation().getText(),
          property.getObjectType()));
    }
    return adapters;
  }
}