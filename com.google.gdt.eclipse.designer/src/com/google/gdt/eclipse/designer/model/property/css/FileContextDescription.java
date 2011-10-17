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
package com.google.gdt.eclipse.designer.model.property.css;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.parser.CssEditContext;

import org.eclipse.core.resources.IFile;

import org.apache.commons.lang.ObjectUtils;

/**
 * {@link ContextDescription} for context in standalone CSS file.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
public class FileContextDescription extends ContextDescription {
  private final IFile m_file;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FileContextDescription(IFile file) throws Exception {
    super(new CssEditContext(file));
    m_file = file;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IFile} resource.
   */
  public IFile getFile() {
    return m_file;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isStale() throws Exception {
    String fileContent = IOUtils2.readString(m_file);
    String contentContent = getContext().getText();
    return !ObjectUtils.equals(fileContent, contentContent);
  }

  @Override
  public String getStyleName(CssRuleNode rule) {
    String selector = rule.getSelector().getValue();
    if (selector.startsWith(".")) {
      return selector.substring(1);
    }
    return null;
  }

  @Override
  public void commit() throws Exception {
    getContext().commit();
  }

  @Override
  public void dispose() throws Exception {
    getContext().disconnect();
  }
}
