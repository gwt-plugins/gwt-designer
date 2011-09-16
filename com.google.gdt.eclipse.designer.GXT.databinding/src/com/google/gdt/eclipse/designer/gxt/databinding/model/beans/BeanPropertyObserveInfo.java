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
package com.google.gdt.eclipse.designer.gxt.databinding.model.beans;

import com.google.gdt.eclipse.designer.gxt.databinding.model.ObserveInfo;

import org.eclipse.wb.internal.core.databinding.model.IObserveDecoration;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;

import java.util.Collections;
import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public class BeanPropertyObserveInfo extends ObserveInfo implements IObserveDecoration {
  private final IObserveInfo m_parent;
  private final IObservePresentation m_presentation;
  private final IObserveDecorator m_decorator;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeanPropertyObserveInfo(Class<?> objectType,
      IObserveInfo parent,
      IReferenceProvider referenceProvider,
      IObservePresentation presentation,
      IObserveDecorator decorator) {
    super(objectType, referenceProvider);
    m_parent = parent;
    m_presentation = presentation;
    m_decorator = decorator;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObserveInfo getParent() {
    return m_parent;
  }

  public List<IObserveInfo> getChildren(ChildrenContext context) {
    return Collections.emptyList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObservePresentation getPresentation() {
    return m_presentation;
  }

  public IObserveDecorator getDecorator() {
    return m_decorator;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObserveType
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveType getType() {
    return ObserveType.BEANS;
  }
}