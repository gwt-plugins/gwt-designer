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
package com.google.gdt.eclipse.designer.model.widgets;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Model for GWT <code>ImageBundle</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class ImageBundleInfo extends JavaInfo {
  private static final String IMAGE_PROTOTYPE =
      "com.google.gwt.user.client.ui.AbstractImagePrototype";
  private final List<ImageBundlePrototypeDescription> m_prototypes = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImageBundleInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    // prepare prototypes
    for (Method method : getDescription().getComponentClass().getMethods()) {
      if (method.getReturnType().getName().equals(IMAGE_PROTOTYPE)) {
        m_prototypes.add(new ImageBundlePrototypeDescription(this, method));
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the descriptions for <code>AbstractImagePrototype</code> methods.
   */
  public List<ImageBundlePrototypeDescription> getPrototypes() {
    return m_prototypes;
  }
}
