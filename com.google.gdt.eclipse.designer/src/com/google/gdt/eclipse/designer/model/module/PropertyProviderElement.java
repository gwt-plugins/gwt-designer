/*******************************************************************************
 * Copyright 2012 Google Inc. All Rights Reserved.
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

import org.eclipse.wb.internal.core.utils.xml.DocumentTextNode;

/**
 * @author scheglov_ke
 * @coverage gwt.model.module
 */
public class PropertyProviderElement extends AbstractModuleElement {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertyProviderElement() {
    super("property-provider");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setName(String name) {
    setAttribute("name", name);
  }

  public String getName() {
    return getAttribute("name");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Script
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setScript(String script) {
    setText(script, true);
  }

  public String getScript() {
    DocumentTextNode textNode = getTextNode();
    if (textNode != null) {
      return textNode.getText();
    }
    return null;
  }
}
