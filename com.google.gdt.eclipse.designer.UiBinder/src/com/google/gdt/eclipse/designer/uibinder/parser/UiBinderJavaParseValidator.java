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
package com.google.gdt.eclipse.designer.uibinder.parser;

import com.google.gdt.eclipse.designer.uibinder.IExceptionConstants;

import org.eclipse.wb.internal.core.parser.IParseValidator;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * {@link IParseValidator} to prevent opening UiBinder Java class.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.parser
 */
public class UiBinderJavaParseValidator implements IParseValidator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IParseValidator INSTANCE = new UiBinderJavaParseValidator();

  private UiBinderJavaParseValidator() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IParseValidator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void validate(final AstEditor editor) throws Exception {
    if (editor.getSource().contains("createAndBindUi")) {
      editor.getAstUnit().accept(new ASTVisitor() {
        @Override
        public void endVisit(MethodInvocation node) {
          if (isBindInvocation(node)) {
            String unitName = editor.getModelUnit().getElementName();
            throw new DesignerException(IExceptionConstants.DONT_OPEN_JAVA, unitName);
          }
        }

        private boolean isBindInvocation(MethodInvocation invocation) {
          Expression expression = invocation.getExpression();
          return invocation.getName().getIdentifier().equals("createAndBindUi")
              && expression != null
              && AstNodeUtils.isSuccessorOf(expression, "com.google.gwt.uibinder.client.UiBinder");
        }
      });
    }
  }
}
