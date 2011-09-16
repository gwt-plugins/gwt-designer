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
package com.google.gdt.eclipse.designer.wizards.model.common;

import com.google.gdt.eclipse.designer.model.module.AbstractModuleElement;

import org.eclipse.wb.internal.core.utils.xml.DocumentTextNode;

/**
 * @author scheglov_ke
 * @coverage gwt.model.web
 */
public class ServletNameElement extends AbstractModuleElement {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ServletNameElement() {
    super("servlet-name");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getName() {
    DocumentTextNode textNode = getTextNode();
    return textNode.getText();
  }

  public void setName(String name) {
    DocumentTextNode textNode = getTextNode();
    if (textNode != null) {
      textNode.setText(name);
    } else {
      textNode = new DocumentTextNode(false);
      textNode.setText(name);
      setTextNode(textNode);
    }
  }
}
