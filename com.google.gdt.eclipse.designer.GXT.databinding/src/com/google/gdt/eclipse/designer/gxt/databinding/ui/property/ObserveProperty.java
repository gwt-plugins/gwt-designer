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
package com.google.gdt.eclipse.designer.gxt.databinding.ui.property;

import com.google.gdt.eclipse.designer.gxt.databinding.model.ObserveInfo;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractObserveProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;

import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public class ObserveProperty extends AbstractObserveProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveProperty(Context context, IObserveInfo observeProperty) throws Exception {
    super(context, observeProperty);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractObserveProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void getBindings(List<IBindingInfo> bindings, List<Boolean> isTargets) throws Exception {
    ObserveInfo observeProperty = (ObserveInfo) m_observeProperty;
    bindings.addAll(observeProperty.getBindings());
    //
    for (IBindingInfo binding : bindings) {
      isTargets.add(binding.getTargetProperty() == m_observeProperty);
    }
  }

  @Override
  public AbstractBindingProperty createBindingProperty() throws Exception {
    return new BindingProperty(m_context);
  }
}