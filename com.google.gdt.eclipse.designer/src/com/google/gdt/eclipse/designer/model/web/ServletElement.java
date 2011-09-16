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
package com.google.gdt.eclipse.designer.model.web;

import com.google.gdt.eclipse.designer.model.module.AbstractModuleElement;

import java.util.List;

/**
 * @author scheglov_ke
 * @coverage gwt.model.web
 */
public class ServletElement extends AbstractModuleElement {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ServletElement() {
    super("servlet");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // servlet-name
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getName() {
    List<ServletNameElement> elements = getChildren(ServletNameElement.class);
    return !elements.isEmpty() ? elements.get(0).getName() : null;
  }

  public void setName(String name) {
    List<ServletNameElement> elements = getChildren(ServletNameElement.class);
    ServletNameElement nameElement;
    if (!elements.isEmpty()) {
      nameElement = elements.get(0);
    } else {
      nameElement = new ServletNameElement();
      addChild(nameElement);
    }
    nameElement.setName(name);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // servlet-class
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getClassName() {
    List<ServletClassElement> elements = getChildren(ServletClassElement.class);
    return !elements.isEmpty() ? elements.get(0).getClassName() : null;
  }

  public void setClassName(String name) {
    List<ServletClassElement> elements = getChildren(ServletClassElement.class);
    ServletClassElement classElement;
    if (!elements.isEmpty()) {
      classElement = elements.get(0);
    } else {
      classElement = new ServletClassElement();
      addChild(classElement);
    }
    classElement.setClassName(name);
  }
}
