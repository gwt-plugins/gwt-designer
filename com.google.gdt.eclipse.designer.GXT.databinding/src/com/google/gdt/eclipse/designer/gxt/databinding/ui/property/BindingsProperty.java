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
package com.google.gdt.eclipse.designer.gxt.databinding.ui.property;

import com.google.gdt.eclipse.designer.gxt.databinding.DatabindingsProvider;
import com.google.gdt.eclipse.designer.gxt.databinding.model.ObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.widgets.JavaInfoReferenceProvider;
import com.google.gdt.eclipse.designer.gxt.databinding.model.widgets.WidgetObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.ui.providers.BindingLabelProvider;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.model.SynchronizeManager;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingsProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.BindingAction;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jface.action.IMenuManager;

import java.util.List;

/**
 * 
 * @author lobas_av
 * 
 */
public class BindingsProperty extends AbstractBindingsProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindingsProperty(Context context) {
    super(context);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // BindingsProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Property[] createProperties() throws Exception {
    DatabindingsProvider provider = (DatabindingsProvider) m_context.provider;
    m_context.observeObject = provider.getWidgetsContainer().resolve(m_context.javaInfo());
    Assert.isNotNull(m_context.observeObject, SynchronizeManager.class.getName()
        + " isn't work ("
        + m_context.objectInfo
        + ")");
    List<IObserveInfo> observes =
        m_context.observeObject.getChildren(ChildrenContext.ChildrenForPropertiesTable);
    //
    Property[] properties = new Property[observes.size()];
    for (int i = 0; i < properties.length; i++) {
      properties[i] = new ObserveProperty(m_context, observes.get(i));
    }
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean checkEquals(IObserveInfo iobserve) throws Exception {
    if (iobserve instanceof WidgetObserveInfo) {
      WidgetObserveInfo widgetObserve = (WidgetObserveInfo) iobserve;
      return m_context.objectInfo == widgetObserve.getJavaInfo();
    }
    //
    String reference = JavaInfoReferenceProvider.getReference(m_context.javaInfo());
    ObserveInfo observe = (ObserveInfo) iobserve;
    return reference.equals(observe.getReference());
  }

  @Override
  protected void addBindingAction(IMenuManager menu,
      IBindingInfo binding,
      IObserveInfo observeProperty,
      boolean isTarget) throws Exception {
    BindingAction action = new BindingAction(m_context, binding);
    action.setText(observeProperty.getPresentation().getText()
        + ": "
        + BindingLabelProvider.INSTANCE.getColumnText(binding, isTarget ? 2 : 1));
    action.setIcon(BindingLabelProvider.INSTANCE.getColumnImage(binding, 0));
    menu.add(action);
  }
}