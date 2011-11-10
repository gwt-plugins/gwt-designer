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
package com.google.gdt.eclipse.designer.smart.gef.part.form;

import com.google.gdt.eclipse.designer.gef.part.UIObjectEditPart;
import com.google.gdt.eclipse.designer.smart.gef.policy.DynamicFormLayoutEditPolicy;
import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smart.model.form.DynamicFormInfo;
import com.google.gdt.eclipse.designer.smart.model.form.FormItemInfo;

import org.eclipse.wb.core.gef.policy.layout.generic.GenericEditPolicyFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidator;
import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidators;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;

/**
 * {@link EditPart} for {@link DynamicFormInfo}.
 * 
 * @author scheglov_ke
 * @coverage SmartGWT.gef.part
 */
public final class DynamicFormEditPart extends UIObjectEditPart {
  private final DynamicFormInfo m_form;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DynamicFormEditPart(DynamicFormInfo canvas) {
    super(canvas);
    m_form = canvas;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refreshEditPolicies() {
    super.refreshEditPolicies();
    if (m_form.isAbsoluteItemLayout()) {
      installEditPolicy(EditPolicy.LAYOUT_ROLE, new DynamicFormLayoutEditPolicy(m_form));
    } else {
      installEditPolicy(EditPolicy.LAYOUT_ROLE, createFlowContainerPolicy());
    }
  }

  /**
   * @return the {@link LayoutEditPolicy} configured for {@link FormItemInfo}.
   */
  private LayoutEditPolicy createFlowContainerPolicy() {
    return GenericEditPolicyFactory.createFlow(m_form, new FlowContainer() {
      ContainerObjectValidator m_validator = ContainerObjectValidators.forList(new String[]{
          "com.smartgwt.client.widgets.form.fields.FormItem",
          "com.smartgwt.client.widgets.Canvas"});

      ////////////////////////////////////////////////////////////////////////////
      //
      // Commands
      //
      ////////////////////////////////////////////////////////////////////////////
      public void command_CREATE(Object newObject, Object referenceObject) throws Exception {
        if (newObject instanceof FormItemInfo) {
          m_form.command_CREATE((FormItemInfo) newObject, (FormItemInfo) referenceObject);
        }
        if (newObject instanceof CanvasInfo) {
          m_form.command_CREATE((CanvasInfo) newObject, (FormItemInfo) referenceObject);
        }
      }

      public void command_MOVE(Object moveObject, Object referenceObject) throws Exception {
        if (moveObject instanceof FormItemInfo) {
          m_form.command_MOVE((FormItemInfo) moveObject, (FormItemInfo) referenceObject);
        }
        if (moveObject instanceof CanvasInfo) {
          m_form.command_MOVE((CanvasInfo) moveObject, (FormItemInfo) referenceObject);
        }
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Validation
      //
      ////////////////////////////////////////////////////////////////////////////
      public boolean isHorizontal() {
        return false;
      }

      public boolean isRtl() {
        return false;
      }

      public boolean validateReference(Object reference) {
        return m_validator.validate(m_form, reference);
      }

      public boolean validateComponent(Object component) {
        return m_validator.validate(m_form, component);
      }
    });
  }
}
