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
package com.google.gdt.eclipse.designer.smart.model;

import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;

import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationAssociation;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

/**
 * Model for <code>com.smartgwt.client.widgets.layout.Layout</code>.
 * 
 * @author scheglov_ke
 * @coverage SmartGWT.model
 */
public class LayoutInfo extends CanvasInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutInfo(AstEditor editor,
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
   * @return array of children <code>com.smartgwt.client.widgets.Canvas</code>'s.
   */
  public Object[] getCanvases() {
    return (Object[]) ReflectionUtils.invokeMethodEx(getObject(), "getMembers()");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_dispose_detach(WidgetInfo child) throws Exception {
    // detach members 
    if (child instanceof CanvasInfo) {
      CanvasInfo childCanvas = (CanvasInfo) child;
      Association association = childCanvas.getAssociation();
      if (association instanceof InvocationAssociation) {
        InvocationAssociation invocationAssociation = (InvocationAssociation) association;
        String signature = AstNodeUtils.getMethodSignature(invocationAssociation.getInvocation());
        if (signature.startsWith("addMember(com.smartgwt.client.widgets.Canvas")
            || signature.startsWith("addMembers(com.smartgwt.client.widgets.Canvas")) {
          if (childCanvas.isCreated()) {
            ReflectionUtils.invokeMethod(
                getObject(),
                "removeMember(com.smartgwt.client.widgets.Canvas)",
                childCanvas.getObject());
            return;
          }
        }
      }
    } else {
      // TODO
    }
    super.refresh_dispose_detach(child);
  }
}
