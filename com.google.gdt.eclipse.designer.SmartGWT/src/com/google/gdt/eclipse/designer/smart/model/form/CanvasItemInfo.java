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
package com.google.gdt.eclipse.designer.smart.model.form;

import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.form.fields.CanvasItem</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class CanvasItemInfo extends FormItemInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CanvasItemInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Live" support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Dimension getLivePreferredSize() {
    Dimension size = super.getLivePreferredSize();
    CanvasInfo canvas = getCanvas();
    if (size == null && canvas != null) {
      size = canvas.getPreferredSize();
    }
    return size;
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public CanvasInfo getCanvas() {
    List<CanvasInfo> children = getChildren(CanvasInfo.class);
    return children.size() > 0 ? children.get(0) : null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    // detach canvas
    if (isCreated()) {
      ReflectionUtils.invokeMethod(
          getObject(),
          "setCanvas(com.smartgwt.client.widgets.Canvas)",
          (Object) null);
    }
    //
    super.refresh_dispose();
  }
}
