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
 * Binary operator expression.
 */
public class JBinaryOperation extends JExpression {

  private JExpression lhs;
  private final JBinaryOperator op;
  private JExpression rhs;
  private JType type;

  public JBinaryOperation(SourceInfo info, JType type, JBinaryOperator op,
      JExpression lhs, JExpression rhs) {
    super(info);
    this.op = op;
    this.type = type;
    this.lhs = lhs;
    this.rhs = rhs;
  }

  public JExpression getLhs() {
    return lhs;
  }

  public JBinaryOperator getOp() {
    return op;
  }

  public JExpression getRhs() {
    return rhs;
  }

  public JType getType() {
    if (isAssignment()) {
      // Use the type of the lhs
      return getLhs().getType();
    } else {
      // Most binary operators never change type
      return type;
    }
  }

  public boolean hasSideEffects() {
    return op.isAssignment() || getLhs().hasSideEffects()
        || getRhs().hasSideEffects();
  }

  public boolean isAssignment() {
    return op.isAssignment();
  }

  public void setType(JType newType) {
    type = newType;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
      lhs = visitor.accept(lhs);
      rhs = visitor.accept(rhs);
    }
    visitor.endVisit(this, ctx);
  }

}
