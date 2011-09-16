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
package com.google.gdt.eclipse.designer.gxt.databinding.model.widgets;

import com.google.gdt.eclipse.designer.gxt.databinding.model.ObserveInfo;

import org.eclipse.wb.internal.core.databinding.model.IObserveDecoration;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;

import java.util.Collections;
import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public final class WidgetPropertyObserveInfo extends ObserveInfo implements IObserveDecoration {
  private final IObservePresentation m_presentation;
  private List<IObserveInfo> m_properties = Collections.emptyList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetPropertyObserveInfo(IObservePresentation presentation) {
    super(null, StringReferenceProvider.EMPTY);
    m_presentation = presentation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  public IObserveInfo getParent() {
    return null;
  }

  public List<IObserveInfo> getChildren(ChildrenContext context) {
    if (context == ChildrenContext.ChildrenForPropertiesTable) {
      return m_properties;
    }
    return Collections.emptyList();
  }

  public void setProperties(List<IObserveInfo> properties) {
    m_properties = properties;
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
    return IObserveDecorator.BOLD;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObserveType
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveType getType() {
    return ObserveType.WIDGETS;
  }
}