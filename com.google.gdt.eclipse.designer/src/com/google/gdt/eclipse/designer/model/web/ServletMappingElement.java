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
public class ServletMappingElement extends AbstractModuleElement {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ServletMappingElement() {
    super("servlet-mapping");
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
  // url-pattern
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getPattern() {
    List<UrlPatternElement> elements = getChildren(UrlPatternElement.class);
    return !elements.isEmpty() ? elements.get(0).getPattern() : null;
  }

  public void setPattern(String pattern) {
    List<UrlPatternElement> elements = getChildren(UrlPatternElement.class);
    UrlPatternElement classElement;
    if (!elements.isEmpty()) {
      classElement = elements.get(0);
    } else {
      classElement = new UrlPatternElement();
      addChild(classElement);
    }
    classElement.setPattern(pattern);
  }
}
