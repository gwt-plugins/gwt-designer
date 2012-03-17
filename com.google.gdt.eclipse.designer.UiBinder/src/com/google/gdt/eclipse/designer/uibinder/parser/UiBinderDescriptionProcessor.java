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
package com.google.gdt.eclipse.designer.uibinder.parser;

import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.description.IDescriptionProcessor;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * {@link IDescriptionProcessor} for UiBinder.
 * 
 * @author scheglov_ke
 * @coverage XWT.parser
 */
public class UiBinderDescriptionProcessor implements IDescriptionProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IDescriptionProcessor INSTANCE = new UiBinderDescriptionProcessor();

  private UiBinderDescriptionProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDescriptionProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(EditorContext context, ComponentDescription componentDescription)
      throws Exception {
    if (context instanceof UiBinderContext) {
      removeAmbiguousProperties(componentDescription);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link XmlObjectInfo} is UiBinder.
   */
  public static boolean isUiBinder(XmlObjectInfo object) {
    EditorContext context = object.getContext();
    return context instanceof UiBinderContext;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * UiBinder does not allow attributes for setter which differs only in type, so we should not
   * create {@link Property}s for them, unless String.
   */
  private void removeAmbiguousProperties(ComponentDescription componentDescription) {
    List<GenericPropertyDescription> properties = componentDescription.getProperties();
    // prepare duplicate names of properties
    Set<String> propertyNames = Sets.newHashSet();
    Set<String> duplicateNames = Sets.newHashSet();
    for (GenericPropertyDescription propertyDescription : properties) {
      String title = propertyDescription.getTitle();
      String name = StringUtils.substringBefore(title, "(");
      if (propertyNames.contains(name)) {
        duplicateNames.add(name);
      } else {
        propertyNames.add(name);
      }
    }
    // if name of property is duplicate, remove it
    for (GenericPropertyDescription propertyDescription : properties) {
      String title = propertyDescription.getTitle();
      String name = StringUtils.substringBefore(title, "(");
      if (duplicateNames.contains(name) && propertyDescription.getType() != String.class) {
        propertyDescription.setEditor(null);
      }
    }
  }
}
