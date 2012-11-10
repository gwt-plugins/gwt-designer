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
package com.google.gdt.eclipse.designer.gxt.gef.policy;

import com.google.gdt.eclipse.designer.gxt.model.widgets.PortalInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.PortalInfo.ColumnInfo;
import com.google.gdt.eclipse.designer.gxt.model.widgets.PortletInfo;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2.IPasteProcessor;
import org.eclipse.wb.core.gef.policy.layout.flow.AbstractFlowLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;

/**
 * {@link LayoutEditPolicy} for {@link ColumnInfo} in {@link PortalInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.gef.policy
 */
public final class PortalLayoutEditPolicy extends AbstractFlowLayoutEditPolicy {
  public static final ILayoutRequestValidator VALIDATOR = LayoutRequestValidators.or(
      LayoutRequestValidators.modelType(ColumnInfo.class),
      LayoutRequestValidators.modelType(PortletInfo.class));
  private final PortalInfo m_portal;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PortalLayoutEditPolicy(PortalInfo portal) {
    m_portal = portal;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Decorate child
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    Object model = child.getModel();
    if (model instanceof ColumnInfo) {
      ColumnInfo column = (ColumnInfo) model;
      child.installEditPolicy(
          EditPolicy.SELECTION_ROLE,
          new PortalColumnSelectionEditPolicy(column));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart.getModel() instanceof ColumnInfo;
  }

  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return VALIDATOR;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractFlowLayoutEditPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isHorizontal(Request request) {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(final Object newObject, final Object referenceObject) {
    return new EditCommand(m_portal) {
      @Override
      protected void executeEdit() throws Exception {
        command_CREATE(newObject, referenceObject);
      }
    };
  }

  @Override
  protected Command getPasteCommand(final PasteRequest request, final Object referenceObject) {
    return LayoutPolicyUtils2.getPasteCommand(
        m_portal,
        request,
        JavaInfo.class,
        new IPasteProcessor<JavaInfo>() {
          public void process(JavaInfo newObject) throws Exception {
            command_CREATE(newObject, referenceObject);
          }
        });
  }

  @Override
  protected Command getMoveCommand(final Object moveObject, final Object referenceObject) {
    return new EditCommand(m_portal) {
      @Override
      protected void executeEdit() throws Exception {
        command_MOVE(moveObject, referenceObject);
      }
    };
  }

  @Override
  protected Command getAddCommand(final Object addObject, final Object referenceObject) {
    return new EditCommand(m_portal) {
      @Override
      protected void executeEdit() throws Exception {
        command_ADD(addObject, referenceObject);
      }
    };
  }

  private void command_CREATE(Object newObject, Object referenceObject) throws Exception {
    if (newObject instanceof PortletInfo) {
      PortletInfo portlet = (PortletInfo) newObject;
      ColumnInfo nextColumn = (ColumnInfo) referenceObject;
      ColumnInfo column = m_portal.command_CREATE(nextColumn);
      column.command_CREATE(portlet, null);
    }
  }

  private void command_MOVE(Object object, Object referenceObject) throws Exception {
    if (object instanceof ColumnInfo) {
      ColumnInfo column = (ColumnInfo) object;
      ColumnInfo nextColumn = (ColumnInfo) referenceObject;
      m_portal.command_MOVE(column, nextColumn);
    }
    if (object instanceof PortletInfo) {
      PortletInfo portlet = (PortletInfo) object;
      ColumnInfo nextColumn = (ColumnInfo) referenceObject;
      ColumnInfo column = m_portal.command_CREATE(nextColumn);
      column.command_MOVE(portlet, null);
    }
  }

  private void command_ADD(Object addObject, Object referenceObject) throws Exception {
    command_MOVE(addObject, referenceObject);
  }
}