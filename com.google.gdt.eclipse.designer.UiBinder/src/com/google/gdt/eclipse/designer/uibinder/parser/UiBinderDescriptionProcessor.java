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
package com.google.gdt.eclipse.designer.uibinder.parser;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.description.IDescriptionProcessor;

import java.util.List;

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
    for (GenericPropertyDescription propertyDescription : properties) {
      if (propertyDescription.getTitle().contains("(")
          && propertyDescription.getType() != String.class) {
        propertyDescription.setEditor(null);
      }
    }
  }
}
