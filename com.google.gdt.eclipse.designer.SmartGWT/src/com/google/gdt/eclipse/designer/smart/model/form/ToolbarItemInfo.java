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

import com.google.gdt.eclipse.designer.smart.model.ArrayChildrenContainerUtils;
import com.google.gdt.eclipse.designer.smart.model.StatefulCanvasInfo;

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.form.fields.ToolbarItem</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class ToolbarItemInfo extends CanvasItemInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToolbarItemInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<StatefulCanvasInfo> getButtons() {
    return getChildren(StatefulCanvasInfo.class);
  }

  public boolean isHorizontal() {
    Boolean orientation = (Boolean) ReflectionUtils.invokeMethodEx(getObject(), "getVertical()");
    return orientation == null ? true : !orientation;
  }

  /**
   * @return the {@link AbstractArrayObjectInfo} for "setButtons" invocation.
   */
  public AbstractArrayObjectInfo getButtonsArrayInfo() throws Exception {
    return ArrayChildrenContainerUtils.getMethodParameterArrayInfo(
        this,
        "setButtons",
        "com.smartgwt.client.widgets.StatefulCanvas");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    // correct corrupted bounds
    Point shiftPoint = getBounds().getLocation().getNegated();
    for (StatefulCanvasInfo button : getButtons()) {
      Rectangle buttonBounds = new Rectangle(button.getModelBounds());
      buttonBounds.translate(shiftPoint);
      button.setBounds(buttonBounds);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  public void command_CREATE(StatefulCanvasInfo newButton, StatefulCanvasInfo referenceButton)
      throws Exception {
    AbstractArrayObjectInfo arrayInfo = getButtonsArrayInfo();
    arrayInfo.command_CREATE(newButton, referenceButton);
  }

  public void command_MOVE(StatefulCanvasInfo moveButton, StatefulCanvasInfo referenceButton)
      throws Exception {
    AbstractArrayObjectInfo arrayInfo = getButtonsArrayInfo();
    arrayInfo.command_MOVE(moveButton, referenceButton);
  }
}
