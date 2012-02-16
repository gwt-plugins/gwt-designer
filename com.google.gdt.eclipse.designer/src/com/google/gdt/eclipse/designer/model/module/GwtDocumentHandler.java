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
    } else if ("property-provider".equals(name)) {
      return new PropertyProviderElement();
    }
    return new DocumentElement();
  }
}
