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
package com.google.gdt.eclipse.designer;

import org.eclipse.wb.internal.core.model.description.IToolkitProvider;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;

/**
 * Implementation of {@link IToolkitProvider} for RCP.
 * 
 * @author scheglov_ke
 * @coverage gwt
 */
public final class ToolkitProvider implements IToolkitProvider {
  public static final ToolkitDescription DESCRIPTION = GwtToolkitDescription.INSTANCE;
  static {
    ((GwtToolkitDescription) DESCRIPTION).initialize();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IToolkitProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToolkitDescription getDescription() {
    return DESCRIPTION;
  }
}
