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
package com.google.gwt.dev.js.ast;

import com.google.gwt.dev.jjs.SourceInfo;

/**
 * Represents a JavaScript conditional expression.
 */
public final class JsConditional extends JsExpression {

  private JsExpression elseExpr;

  private JsExpression testExpr;

  private JsExpression thenExpr;

  public JsConditional(SourceInfo sourceInfo) {
    super(sourceInfo);
  }

  public JsConditional(SourceInfo sourceInfo, JsExpression testExpr,
      JsExpression thenExpr, JsExpression elseExpr) {
    super(sourceInfo);
    this.testExpr = testExpr;
    this.thenExpr = thenExpr;
    this.elseExpr = elseExpr;
  }

  public JsExpression getElseExpression() {
    return elseExpr;
  }

  public JsExpression getTestExpression() {
    return testExpr;
  }

  public JsExpression getThenExpression() {
    return thenExpr;
  }

  @Override
  public boolean hasSideEffects() {
    return testExpr.hasSideEffects() || thenExpr.hasSideEffects()
        || elseExpr.hasSideEffects();
  }

  public boolean isDefinitelyNotNull() {
    return thenExpr.isDefinitelyNotNull() && elseExpr.isDefinitelyNotNull();
  }

  public boolean isDefinitelyNull() {
    return thenExpr.isDefinitelyNull() && elseExpr.isDefinitelyNull();
  }

  public void setElseExpression(JsExpression elseExpr) {
    this.elseExpr = elseExpr;
  }

  public void setTestExpression(JsExpression testExpr) {
    this.testExpr = testExpr;
  }

  public void setThenExpression(JsExpression thenExpr) {
    this.thenExpr = thenExpr;
  }

  public void traverse(JsVisitor v, JsContext<JsExpression> ctx) {
    if (v.visit(this, ctx)) {
      testExpr = v.accept(testExpr);
      thenExpr = v.accept(thenExpr);
      elseExpr = v.accept(elseExpr);
    }
    v.endVisit(this, ctx);
  }
}
