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
package com.google.gdt.eclipse.designer.gxt.databinding.model.widgets;

import com.google.gdt.eclipse.designer.gxt.databinding.Activator;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.presentation.JavaInfoObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.swt.graphics.Image;

/**
 * 
 * @author lobas_av
 * 
 */
public class BindingsWidgetObserveInfo extends WidgetObserveInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindingsWidgetObserveInfo(PropertiesSupport propertiesSupport) throws Exception {
    super(propertiesSupport.getClass(),
        StringReferenceProvider.EMPTY,
        new JavaInfoObservePresentation(null) {
          @Override
          public String getText() throws Exception {
            return "Bindings";
          }

          @Override
          public String getTextForBinding() throws Exception {
            return "Bindings";
          }

          @Override
          public Image getImage() throws Exception {
            return Activator.getImage("Bindings.png");
          }
        },
        null,
        propertiesSupport);
    // prepare properties
    m_properties = m_propertiesSupport.getProperties(getObjectType());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WidgetObserveInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void update() throws Exception {
  }

  @Override
  public WidgetObserveInfo resolveReference(ASTNode node) throws Exception {
    return null;
  }

  @Override
  public WidgetObserveInfo resolve(JavaInfo javaInfo) {
    return null;
  }

  @Override
  public void ensureConvertToField() throws Exception {
  }
}