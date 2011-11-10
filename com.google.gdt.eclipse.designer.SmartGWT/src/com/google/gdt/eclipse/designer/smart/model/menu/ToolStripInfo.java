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
package com.google.gdt.eclipse.designer.smart.model.menu;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.smart.model.LayoutInfo;
import com.google.gdt.eclipse.designer.smart.model.form.FormItemInfo;
import com.google.gdt.eclipse.designer.smart.model.support.SmartClientUtils;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Model for <code>com.smartgwt.client.widgets.toolbar.ToolStrip</code>.
 * 
 * @author sablin_aa
 * @coverage SmartGWT.model
 */
public class ToolStripInfo extends LayoutInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToolStripInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return true if flow orientation is horizontal.
   */
  public boolean isHorizontal() throws Exception {
    Object verticalValue = getPropertyByTitle("vertical").getValue();
    return !Boolean.TRUE.equals(verticalValue);
  }

  /**
   * @return {@link List} of children models having real object.
   */
  public List<AbstractComponentInfo> getChildrenReal() {
    List<AbstractComponentInfo> children = getChildren(AbstractComponentInfo.class);
    List<AbstractComponentInfo> toExclude = Lists.newArrayList();
    for (AbstractComponentInfo childInfo : children) {
      if (isChildhasFakeObject(childInfo)) {
        toExclude.add(childInfo);
      }
    }
    children.removeAll(toExclude);
    return children;
  }

  public boolean isChildhasFakeObject(AbstractComponentInfo childInfo) {
    return childInfo instanceof ToolStripResizerInfo;
  }

  public int getFormItemShift() {
    return 4;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_fetch() throws Exception {
    super.refresh_fetch();
    // calculate FormItem's bounds 
    calculate_formItems_bounds();
    // calculate "resizer"s bounds
    calculate_resizers_bounds();
  }

  /**
   * Recalculate bounds for {@link FormItemInfo}s.
   */
  private void calculate_formItems_bounds() {
    List<FormItemInfo> formItemInfos = getChildren(FormItemInfo.class);
    if (formItemInfos.isEmpty()) {
      // no processing required
      return;
    }
    List<AbstractComponentInfo> childrenComponents = getChildrenReal();
    Object[] childrenCanvases = getCanvases();
    for (FormItemInfo formItemInfo : formItemInfos) {
      // locate DynamicForm
      Object form = ReflectionUtils.invokeMethodEx(formItemInfo.getObject(), "getForm()");
      if (form == null) {
        form = childrenCanvases[childrenComponents.indexOf(formItemInfo)];
      }
      if (form != null) {
        // apply form bounds
        Rectangle bounds =
            SmartClientUtils.getAbsoluteBounds(form).getTranslated(
                getAbsoluteBounds().getLocation().getNegated());
        // shift on parent
        int inset = getFormItemShift();
        bounds.translate(-inset, -inset);
        // set bounds
        formItemInfo.setModelBounds(bounds);
      }
    }
  }

  /**
   * Calculate bounds for {@link ToolStripResizerInfo}s.
   */
  private void calculate_resizers_bounds() throws Exception {
    Rectangle modelBounds = getModelBounds();
    AbstractComponentInfo priorChild = null;
    for (AbstractComponentInfo childInfo : getChildren(AbstractComponentInfo.class)) {
      if (childInfo instanceof ToolStripResizerInfo) {
        if (priorChild == null) {
          continue;
        }
        // calculate
        Rectangle priorBounds = priorChild.getModelBounds();
        if (isHorizontal()) {
          childInfo.setModelBounds(new Rectangle(priorBounds.x + priorBounds.width,
              0,
              ToolStripResizerInfo.DEFAULT_SIZE,
              modelBounds.height));
        } else {
          childInfo.setModelBounds(new Rectangle(0,
              priorBounds.y + priorBounds.height,
              modelBounds.width,
              ToolStripResizerInfo.DEFAULT_SIZE));
        }
      }
      priorChild = childInfo;
    }
  }
}
