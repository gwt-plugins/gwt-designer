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
package com.google.gdt.eclipse.designer.gxt.databinding.model;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.BindingInfo;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.IObservePresentationDecorator;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public abstract class ObserveInfo implements IObserveInfo {
  private Class<?> m_objectType;
  private final IReferenceProvider m_referenceProvider;
  private boolean m_setBindingDecoration;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveInfo(Class<?> objectType, String reference) {
    this(objectType, new StringReferenceProvider(reference));
  }

  public ObserveInfo(Class<?> objectType, IReferenceProvider referenceProvider) {
    m_objectType = objectType;
    m_referenceProvider = referenceProvider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link Class} type of observe object or property.
   */
  public final Class<?> getObjectType() {
    return m_objectType;
  }

  protected final void setObjectType(Class<?> objectType) {
    m_objectType = objectType;
  }

  /**
   * @return {@link IReferenceProvider} reference provider on observe object or property.
   */
  public final IReferenceProvider getReferenceProvider() {
    return m_referenceProvider;
  }

  /**
   * @return the reference on observe object or property.
   */
  public final String getReference() throws Exception {
    return m_referenceProvider.getReference();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Binding
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<BindingInfo> m_bindings;

  protected final void setBindingDecoration(boolean setBindingDecoration) {
    m_setBindingDecoration = setBindingDecoration;
  }

  public void createBinding(BindingInfo binding) throws Exception {
    if (m_bindings == null) {
      m_bindings = Lists.newArrayList();
    }
    m_bindings.add(binding);
    updateBindingDecoration();
  }

  public void deleteBinding(BindingInfo binding) throws Exception {
    m_bindings.remove(binding);
    if (m_bindings.isEmpty()) {
      m_bindings = null;
    }
    updateBindingDecoration();
  }

  public List<BindingInfo> getBindings() {
    if (m_bindings == null) {
      return Collections.emptyList();
    }
    return m_bindings;
  }

  private void updateBindingDecoration() throws Exception {
    if (m_setBindingDecoration) {
      IObservePresentation presentation = getPresentation();
      if (presentation instanceof IObservePresentationDecorator) {
        IObservePresentationDecorator presentationDecorator =
            (IObservePresentationDecorator) presentation;
        presentationDecorator.setBindingDecorator(CollectionUtils.isEmpty(m_bindings)
            ? 0
            : SwtResourceManager.TOP_RIGHT);
      }
    }
  }
}