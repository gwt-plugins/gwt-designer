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

import com.google.gdt.eclipse.designer.gxt.Activator;
import com.google.gdt.eclipse.designer.gxt.model.widgets.FieldInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.GridInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.form.FormPanelInfo;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingsProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractJavaInfoPropertiesManager;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;

/**
 * 
 * @author lobas_av
 * 
 */
public class JavaInfoPropertiesManager extends AbstractJavaInfoPropertiesManager {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfoPropertiesManager(IDatabindingsProvider provider, JavaInfo javaInfoRoot) {
    super(provider, javaInfoRoot);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractJavaInfoPropertiesManager
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isCreateProperty(ObjectInfo objectInfo) throws Exception {
    if (objectInfo instanceof JavaInfo) {
      JavaInfo javaInfo = (JavaInfo) objectInfo;
      if (JavaInfoUtils.hasTrueParameter(javaInfo, "databinding.disable")) {
        return false;
      }
    }
    return objectInfo instanceof FieldInfo
        || objectInfo instanceof FormPanelInfo
        || objectInfo instanceof GridInfo;
  }

  @Override
  protected AbstractBindingsProperty createProperty(ObjectInfo objectInfo) throws Exception {
    return new BindingsProperty(new Context(Activator.getDefault(), m_provider, objectInfo));
  }
}