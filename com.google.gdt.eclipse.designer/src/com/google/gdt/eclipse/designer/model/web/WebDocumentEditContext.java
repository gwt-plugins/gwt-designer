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
import org.eclipse.wb.internal.core.utils.xml.FileDocumentEditContext;

import org.eclipse.core.resources.IFile;

/**
 * Edit context for "web.xml" file.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.web
 */
public final class WebDocumentEditContext extends FileDocumentEditContext {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WebDocumentEditContext(IFile file) throws Exception {
    super(file);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the loaded WEB model root, i.e. {@link WebAppElement}.
   */
  public WebAppElement getWebAppElement() {
    return (WebAppElement) getRoot();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handler
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractDocumentHandler createDocumentHandler() {
    return new WebDocumentHandler();
  }
}
