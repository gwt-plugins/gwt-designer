/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        invoke0(object, clazz, namespace, tag);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // XMLObject_resolveTag
  //
  ////////////////////////////////////////////////////////////////////////////
  private void invoke0(XmlObjectInfo object, Class<?> clazz, String[] namespace, String[] tag)
      throws Exception {
    if (UiBinderDescriptionProcessor.isUiBinder(object)) {
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
