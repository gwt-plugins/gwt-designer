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
package com.google.gdt.eclipse.designer.gxt.databinding.model.beans;

import com.google.gdt.eclipse.designer.gxt.databinding.model.ObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.ui.providers.TypeImageProvider;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.SimpleObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public class BeanObserveInfo extends ObserveInfo {
  private final BeanSupport m_beanSupport;
  private final IObservePresentation m_presentation;
  private final List<BeanPropertyObserveInfo> m_properties;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeanObserveInfo(BeanSupport beanSupport,
      Class<?> objectType,
      IReferenceProvider referenceProvider,
      IObservePresentation presentation) throws Exception {
    super(objectType, referenceProvider);
    setBindingDecoration(true);
    m_beanSupport = beanSupport;
    m_properties = beanSupport.getProperties(getObjectType(), null, true);
    m_presentation = presentation;
  }

  public BeanObserveInfo(BeanSupport beanSupport, ObserveInfo parent, Class<?> objectType)
      throws Exception {
    super(objectType, parent.getReferenceProvider());
    m_beanSupport = beanSupport;
    m_properties = beanSupport.getProperties(getObjectType(), parent, false);
    m_presentation = parent.getPresentation();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeanPropertyObserveInfo resolvePropertyReference(String reference, Object errorSupport)
      throws Exception {
    for (BeanPropertyObserveInfo property : m_properties) {
      if (reference.equals(property.getReference())) {
        return property;
      }
    }
    //
    if (errorSupport != null) {
      Boolean[] error = (Boolean[]) errorSupport;
      error[0] = true;
    }
    //
    String propertyName = StringUtils.remove(reference, '"');
    IReferenceProvider referenceProvider = new StringReferenceProvider(reference);
    IObservePresentation presentation =
        new SimpleObservePresentation(propertyName, propertyName, TypeImageProvider.OBJECT_IMAGE);
    IObserveDecorator decorator = IObserveDecorator.DEFAULT;
    //
    return new BeanPropertyObserveInfo(Object.class,
        null,
        referenceProvider,
        presentation,
        decorator);
  }

  public BeanPropertyObserveInfo getSelfProperty() {
    return m_properties.isEmpty() ? null : m_properties.get(0);
  }

  public BeanSupport getBeanSupport() {
    return m_beanSupport;
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
      return CoreUtils.cast(m_properties);
    }
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObserveType
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveType getType() {
    return ObserveType.BEANS;
  }
}