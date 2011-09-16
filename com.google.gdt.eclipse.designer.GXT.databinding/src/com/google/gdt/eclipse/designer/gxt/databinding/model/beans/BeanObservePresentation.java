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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.presentation.ObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.ClassUtils;

/**
 * 
 * @author lobas_av
 * 
 */
public final class BeanObservePresentation extends ObservePresentation {
  private final Class<?> m_objectType;
  private final IReferenceProvider m_reference;
  private final JavaInfo m_javaInfo;
  private final Image m_beanImage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeanObservePresentation(Class<?> objectType,
      IReferenceProvider reference,
      JavaInfo javaInfo,
      Image beanImage) {
    m_objectType = objectType;
    m_reference = reference;
    m_javaInfo = javaInfo;
    m_beanImage = beanImage;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObservePresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Image getInternalImage() throws Exception {
    if (m_beanImage == null && m_javaInfo == null) {
      return null;
    }
    return m_beanImage == null ? m_javaInfo.getPresentation().getIcon() : m_beanImage;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObservePresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getText() throws Exception {
    return m_reference.getReference() + " - " + ClassUtils.getShortClassName(m_objectType);
  }

  public String getTextForBinding() throws Exception {
    return m_reference.getReference();
  }
}