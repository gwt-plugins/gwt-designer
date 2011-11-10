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
package com.google.gwt.dev.jjs.ast;

import com.google.gwt.dev.jjs.SourceInfo;

/**
 * Java initialized local variable statement.
 */
public class JDeclarationStatement extends JStatement {

  public JExpression initializer;
  private JVariableRef variableRef;

  public JDeclarationStatement(SourceInfo info, JVariableRef variableRef,
      JExpression intializer) {
    super(info);
    this.variableRef = variableRef;
    this.initializer = intializer;
    CanHaveInitializer variable = variableRef.getTarget();
    variable.setInitializer(this);
  }

  public JExpression getInitializer() {
    return initializer;
  }

  public JVariableRef getVariableRef() {
    return variableRef;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
      variableRef = (JVariableRef) visitor.accept(variableRef);
      if (initializer != null) {
        initializer = visitor.accept(initializer);
      }
    }
    visitor.endVisit(this, ctx);
  }

}
