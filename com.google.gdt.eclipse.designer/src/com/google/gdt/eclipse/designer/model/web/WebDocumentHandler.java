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

import org.eclipse.wb.internal.core.utils.xml.AbstractDocumentHandler;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.Model;

/**
 * Document handler for reading "web.xml" file.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.web
 */
public class WebDocumentHandler extends AbstractDocumentHandler {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the loaded WEB model root, i.e. {@link WebAppElement}.
   */
  public WebAppElement getWebAppElement() {
    return (WebAppElement) getRootNode();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Nodes creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected DocumentElement getDocumentNode(String name, DocumentElement parent) {
    if ("web-app".equals(name)) {
      Model model = new Model();
      WebAppElement root = new WebAppElement();
      root.setModel(model);
      return root;
    } else if ("welcome-file-list".equals(name)) {
      return new WelcomeFileListElement();
    } else if ("welcome-file".equals(name)) {
      return new WelcomeFileElement();
    } else if ("servlet".equals(name)) {
      return new ServletElement();
    } else if ("servlet-name".equals(name)) {
      return new ServletNameElement();
    } else if ("servlet-class".equals(name)) {
      return new ServletClassElement();
    } else if ("servlet-mapping".equals(name)) {
      return new ServletMappingElement();
    } else if ("url-pattern".equals(name)) {
      return new UrlPatternElement();
    }
    return new DocumentElement();
  }
}
