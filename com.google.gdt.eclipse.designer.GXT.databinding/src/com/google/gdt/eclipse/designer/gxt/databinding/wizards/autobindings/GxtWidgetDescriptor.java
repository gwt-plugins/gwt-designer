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
package com.google.gdt.eclipse.designer.gxt.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.AbstractDescriptor;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author lobas_av
 * 
 */
public final class GxtWidgetDescriptor extends AbstractDescriptor {
  private String[] m_propertyClasses;
  private String m_widgetClass;
  private String m_bindingClass = "com.extjs.gxt.ui.client.binding.FieldBinding";
  private boolean m_generic;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getWidgetClass() {
    return m_widgetClass;
  }

  public void setWidgetClass(String widgetClass) {
    m_widgetClass = widgetClass;
  }

  public String getBindingClass() {
    return m_bindingClass;
  }

  public void setBindingClass(String bindingClass) {
    m_bindingClass = bindingClass;
  }

  public boolean isGeneric() {
    return m_generic;
  }

  public void setGeneric() {
    m_generic = true;
  }

  public String getPropertyClass() {
    return m_propertyClasses[m_propertyClasses.length - 1];
  }

  public void setPropertyClass(String classes) {
    m_propertyClasses = StringUtils.split(classes);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isDefault(Object property) {
    PropertyAdapter propertyAdapter = (PropertyAdapter) property;
    Class<?> propertyType = propertyAdapter.getType();
    if (propertyType != null) {
      return ArrayUtils.contains(m_propertyClasses, propertyType.getName());
    }
    return false;
  }
}