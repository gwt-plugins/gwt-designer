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
package com.google.gdt.eclipse.designer.model.widgets.panels;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

/**
 * Model for GWT <code>VerticalSplitPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class VerticalSplitPanelInfo extends SplitPanelInfo
    implements
      IVerticalSplitPanelInfo<WidgetInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VerticalSplitPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetInfo getTopWidget() {
    return getWidgetAssociatedByMethod("setTopWidget");
  }

  public WidgetInfo getBottomWidget() {
    return getWidgetAssociatedByMethod("setBottomWidget");
  }

  @Override
  public String getEmptyRegion() {
    if (getTopWidget() == null) {
      return "top";
    }
    if (getBottomWidget() == null) {
      return "bottom";
    }
    return null;
  }
}
