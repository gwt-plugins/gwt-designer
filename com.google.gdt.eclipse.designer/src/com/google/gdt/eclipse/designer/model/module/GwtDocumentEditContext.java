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
import org.eclipse.wb.internal.core.utils.xml.FileDocumentEditContext;

import org.eclipse.core.resources.IFile;

/**
 * Edit context for GWT module definition file.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.module
 */
public final class GwtDocumentEditContext extends FileDocumentEditContext {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GwtDocumentEditContext(IFile file) throws Exception {
    super(file);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the loaded GWT model root, i.e. {@link ModuleElement}.
   */
  public ModuleElement getModuleElement() {
    return (ModuleElement) getRoot();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handler
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractDocumentHandler createDocumentHandler() {
    return new GwtDocumentHandler();
  }
}
