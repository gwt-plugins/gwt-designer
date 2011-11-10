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
package com.google.gdt.eclipse.designer.gwtext.model.widgets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * Model for <code>BoxComponent</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model
 */
public class BoxComponentInfo extends ComponentInfo {
  private final BoxComponentInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BoxComponentInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addBoxComponentListeners();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // BoxComponent (not subclass) support
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addBoxComponentListeners() {
    // check if exactly BoxComponent
    {
      String componentClassName = getDescription().getComponentClass().getName();
      if (!"com.gwtext.client.widgets.BoxComponent".equals(componentClassName)) {
        return;
      }
    }
    // add listeners
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void bindComponents(List<JavaInfo> components) throws Exception {
        String signature_setEl = "setEl(com.google.gwt.user.client.Element)";
        for (MethodInvocation invocation : getMethodInvocations(signature_setEl)) {
          List<Expression> arguments = DomGenerics.arguments(invocation);
          Expression elementExpression = arguments.get(0);
          if (AstNodeUtils.isMethodInvocation(elementExpression, "getElement()")) {
            Expression widgetExpression = ((MethodInvocation) elementExpression).getExpression();
            for (JavaInfo component : components) {
              if (component.isRepresentedBy(widgetExpression)) {
                addChild(component);
                component.setAssociation(new EmptyAssociation());
              }
            }
          }
        }
      }

      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        if (child == m_this) {
          JavaInfo newHTML =
              JavaInfoUtils.createJavaInfo(
                  getEditor(),
                  "com.google.gwt.user.client.ui.HTML",
                  new ConstructorCreationSupport());
          LocalUniqueVariableSupport variableSupport = new LocalUniqueVariableSupport(newHTML);
          JavaInfoUtils.add(
              newHTML,
              variableSupport,
              PureFlatStatementGenerator.INSTANCE,
              AssociationObjects.invocationChild("%parent%.setEl(%child%.getElement())", true),
              m_this,
              null);
          variableSupport.inline();
          newHTML.getPropertyByTitle("html").setValue("New BoxComponent");
        }
      }
    });
  }
}
