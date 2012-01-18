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
package com.google.gdt.eclipse.designer.uibinder.model.util;

import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderDescriptionProcessor;

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectResolveTag;
import org.eclipse.wb.internal.core.xml.model.utils.NamespacesHelper;

import org.apache.commons.lang.StringUtils;

/**
 * {@link XmlObjectResolveTag} for UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class UiBinderTagResolver extends NamespacesHelper {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UiBinderTagResolver(XmlObjectInfo rootObject) {
    super(rootObject.getCreationSupport().getElement().getRoot());
    rootObject.addBroadcastListener(new XmlObjectResolveTag() {
      public void invoke(XmlObjectInfo object, Class<?> clazz, String[] namespace, String[] tag)
          throws Exception {
        resolveTag(object, clazz, namespace, tag);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // XMLObjectResolveTag
  //
  ////////////////////////////////////////////////////////////////////////////
  private void resolveTag(XmlObjectInfo object, Class<?> clazz, String[] namespace, String[] tag)
      throws Exception {
    if (tag[0] == null && UiBinderDescriptionProcessor.isUiBinder(object)) {
      String className = clazz.getName();
      namespace[0] = getNamespace(className);
      tag[0] = StringUtils.substringAfterLast(className, ".");
    }
  }

  /**
   * @return the namespace (existing or added) to use for given class.
   */
  private String getNamespace(String className) {
    String packageName = StringUtils.substringBeforeLast(className, ".");
    String packageURI = "urn:import:" + packageName;
    return ensureName(packageURI, "p");
  }

  @Override
  protected String getNewName(String uri, String base) {
    if (uri.equals("urn:import:com.google.gwt.widget.client") && !m_names.contains("w")) {
      return "w";
    }
    return super.getNewName(uri, base);
  }
}
