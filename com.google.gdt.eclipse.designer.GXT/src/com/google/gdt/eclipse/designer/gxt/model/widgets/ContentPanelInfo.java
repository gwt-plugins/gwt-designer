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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * Model for <code>com.extjs.gxt.ui.client.widget.ContentPanel</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public class ContentPanelInfo extends LayoutContainerInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ContentPanelInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setObject(Object object) throws Exception {
    super.setObject(object);
    // animation at design time may cause screen shot with temporary state
    if (!isPlaceholder() && !isRendered()) {
      ReflectionUtils.invokeMethod(object, "setAnimCollapse(boolean)", false);
    }
  }

  @Override
  protected void fetchClientAreaInsets() throws Exception {
    if (!isPlaceholder() && isRendered()) {
      Object panelEl = JavaInfoUtils.executeScript(this, "object.el()");
      Object bodyEl = JavaInfoUtils.executeScript(this, "object.getBody()");
      Rectangle panelBounds = GxtUtils.getAbsoluteBounds(panelEl);
      Rectangle bodyBounds = GxtUtils.getAbsoluteBounds(bodyEl);
      Insets insets =
          new Insets(bodyBounds.y - panelBounds.y,
              bodyBounds.x - panelBounds.x,
              panelBounds.bottom() - bodyBounds.bottom(),
              panelBounds.right() - bodyBounds.right());
      insets.add(m_margins);
      insets.add(GxtUtils.getBorders(bodyEl));
      setClientAreaInsets(insets);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setTopComponent()
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean hasTopComponent() {
    return ReflectionUtils.invokeMethodEx(getObject(), "getTopComponent()") != null;
  }

  public void setTopComponent(ComponentInfo component) throws Exception {
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.setTopComponent(%child%)", true);
    if (component.getParent() == null) {
      JavaInfoUtils.add(component, associationObject, this, null);
    } else {
      JavaInfoUtils.move(component, associationObject, this, null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setBottomComponent()
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean hasBottomComponent() {
    return ReflectionUtils.invokeMethodEx(getObject(), "getBottomComponent()") != null;
  }

  public void setBottomComponent(ComponentInfo component) throws Exception {
    AssociationObject associationObject =
        AssociationObjects.invocationChild("%parent%.setBottomComponent(%child%)", true);
    if (component.getParent() == null) {
      JavaInfoUtils.add(component, associationObject, this, null);
    } else {
      JavaInfoUtils.move(component, associationObject, this, null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ButtonBar
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the bounds of "ButtonBar" relative to panel, may be empty, but not <code>null</code>.
   */
  public Rectangle getButtonBarBounds() {
    Object buttonBar = ReflectionUtils.invokeMethodEx(getObject(), "getButtonBar()");
    Object buttonBarElement = getUIObjectUtils().getElement(buttonBar);
    Rectangle bounds = getState().getAbsoluteBounds(buttonBarElement);
    bounds.translate(getModelBounds().getLocation().getNegated());
    return bounds;
  }

  /**
   * @return {@link ButtonInfo}-s added using <code>addButton()</code>.
   */
  public List<ButtonInfo> getButtonBarButtons() {
    List<ButtonInfo> barButtons = Lists.newArrayList();
    List<ButtonInfo> allButtons = getChildren(ButtonInfo.class);
    for (ButtonInfo button : allButtons) {
      if (button.getAssociation() instanceof InvocationChildAssociation) {
        InvocationChildAssociation association =
            (InvocationChildAssociation) button.getAssociation();
        if (association.getDescription().getSignature().equals(
            "addButton(com.extjs.gxt.ui.client.widget.button.Button)")) {
          barButtons.add(button);
        }
      }
    }
    return barButtons;
  }

  /**
   * Support for <code>ContentPanel.addButton()</code>.
   */
  public void command_ButtonBar_CREATE(ButtonInfo button, ButtonInfo nextButton) throws Exception {
    JavaInfoUtils.add(button, getButtonBarAssociation(), this, nextButton);
  }

  /**
   * Support for <code>ContentPanel.addButton()</code>.
   */
  public void command_ButtonBar_MOVE(ButtonInfo button, ButtonInfo nextButton) throws Exception {
    JavaInfoUtils.move(button, getButtonBarAssociation(), this, nextButton);
  }

  private AssociationObject getButtonBarAssociation() {
    return AssociationObjects.invocationChild("%parent%.addButton(%child%)", true);
  }
}
