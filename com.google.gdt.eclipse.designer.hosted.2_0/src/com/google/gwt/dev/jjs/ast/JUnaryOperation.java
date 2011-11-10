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
 * Java prefix or postfix operation expression.
 */
public abstract class JUnaryOperation extends JExpression {

  private JExpression arg;
  private final JUnaryOperator op;

  public JUnaryOperation(SourceInfo info, JUnaryOperator op, JExpression arg) {
    super(info);
    this.op = op;
    this.arg = arg;
  }

  public JExpression getArg() {
    return arg;
  }

  public JUnaryOperator getOp() {
    return op;
  }

  public JType getType() {
    // Unary operators don't change the type of their expression
    return arg.getType();
  }

  public boolean hasSideEffects() {
    return getOp().isModifying() || arg.hasSideEffects();
  }

  public void traverse(JVisitor visitor, Context ctx) {
    arg = visitor.accept(arg);
  }
}
