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
package com.google.gdt.eclipse.designer.model.module;

import org.eclipse.wb.internal.core.utils.xml.AbstractDocumentHandler;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.Model;

/**
 * Document handler for reading GWT module.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.module
 */
public class GwtDocumentHandler extends AbstractDocumentHandler {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the loaded GWT model root, i.e. {@link ModuleElement}.
   */
  public ModuleElement getModuleElement() {
    return (ModuleElement) getRootNode();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Nodes creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected DocumentElement getDocumentNode(String name, DocumentElement parent) {
    if ("module".equals(name)) {
      Model model = new Model();
      ModuleElement moduleElement = new ModuleElement();
      moduleElement.setModel(model);
      return moduleElement;
    } else if ("inherits".equals(name)) {
      return new InheritsElement();
    } else if ("entry-point".equals(name)) {
      return new EntryPointElement();
    } else if ("source".equals(name)) {
      return new SourceElement();
    } else if ("exclude".equals(name)) {
      return new ExcludeElement();
    } else if ("super-source".equals(name)) {
      return new SuperSourceElement();
    } else if ("servlet".equals(name)) {
      return new ServletElement();
    } else if ("public".equals(name)) {
      return new PublicElement();
    } else if ("script".equals(name)) {
      return new ScriptElement();
    } else if ("stylesheet".equals(name)) {
      return new StylesheetElement();
    } else if ("extend-property".equals(name)) {
      return new ExtendPropertyElement();
    } else if ("set-property-fallback".equals(name)) {
      return new SetPropertyFallbackElement();
    }
    return new DocumentElement();
  }
}
