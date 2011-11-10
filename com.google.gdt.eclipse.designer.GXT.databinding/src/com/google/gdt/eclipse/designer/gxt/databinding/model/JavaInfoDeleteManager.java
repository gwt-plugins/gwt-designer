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

import com.google.gdt.eclipse.designer.gxt.databinding.DatabindingsProvider;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.BindingInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.widgets.JavaInfoReferenceProvider;
import com.google.gdt.eclipse.designer.gxt.databinding.model.widgets.WidgetObserveInfo;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;

import java.util.List;

/**
 * Observe {@link JavaInfo} events for delete bindings that have reference to deleted
 * {@link JavaInfo}.
 * 
 * @author lobas_av
 * @coverage bindings.gxt.model
 */
public final class JavaInfoDeleteManager
    extends
      org.eclipse.wb.internal.core.databinding.model.JavaInfoDeleteManager {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfoDeleteManager(DatabindingsProvider provider) {
    super(provider, provider.getJavaInfoRoot());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void deleteBinding(IBindingInfo ibinding, List<IBindingInfo> bindings) throws Exception {
    BindingInfo binding = (BindingInfo) ibinding;
    binding.delete(CoreUtils.<BindingInfo>cast(bindings), true);
  }

  @Override
  protected boolean accept(ObjectInfo javaInfo) throws Exception {
    return javaInfo instanceof AbstractComponentInfo
        || javaInfo.getParent() instanceof NonVisualBeanContainerInfo;
  }

  @Override
  protected String getReference(ObjectInfo javaInfo) throws Exception {
    return JavaInfoReferenceProvider.getReference((JavaInfo) javaInfo);
  }

  @Override
  protected boolean equals(ObjectInfo javaInfo, String javaInfoReference, IObserveInfo iobserve)
      throws Exception {
    ObserveInfo observe = (ObserveInfo) iobserve;
    return checkWidget((JavaInfo) javaInfo, observe)
        || javaInfoReference.equals(observe.getReference());
  }

  static boolean checkWidget(JavaInfo javaInfo, ObserveInfo observe) {
    if (observe instanceof WidgetObserveInfo) {
      WidgetObserveInfo widgetObserve = (WidgetObserveInfo) observe;
      return javaInfo == widgetObserve.getJavaInfo();
    }
    return false;
  }
}