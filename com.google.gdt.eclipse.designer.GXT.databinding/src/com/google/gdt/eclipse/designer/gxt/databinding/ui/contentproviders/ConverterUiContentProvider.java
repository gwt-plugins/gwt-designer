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
package com.google.gdt.eclipse.designer.gxt.databinding.ui.contentproviders;

import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.ConverterInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.bindings.FieldBindingInfo;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassUiContentProvider;

/**
 * 
 * @author lobas_av
 * 
 */
public class ConverterUiContentProvider extends ChooseClassUiContentProvider {
  private final FieldBindingInfo m_binding;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ConverterUiContentProvider(ChooseClassConfiguration configuration, FieldBindingInfo binding) {
    super(configuration);
    m_binding = binding;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() throws Exception {
    ConverterInfo converter = m_binding.getConverter();
    setClassName(converter == null ? "N/S" : converter.getClassName());
  }

  public void saveToObject() throws Exception {
    String className = getClassName();
    // check set or clear value
    if ("N/S".equals(className)) {
      m_binding.setConverter(null);
    } else {
      ConverterInfo converter = m_binding.getConverter();
      // check new converter or edit value
      if (converter == null) {
        converter = new ConverterInfo(className);
        m_binding.setConverter(converter);
      } else {
        converter.setClassName(className);
      }
    }
  }
}