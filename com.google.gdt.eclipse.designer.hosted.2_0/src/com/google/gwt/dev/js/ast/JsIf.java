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
 * Represents a JavaScript if statement.
 */
public final class JsIf extends JsStatement {

  private JsExpression ifExpr;

  private JsStatement thenStmt;

  private JsStatement elseStmt;

  public JsIf(SourceInfo sourceInfo) {
    super(sourceInfo);
  }

  public JsIf(SourceInfo sourceInfo, JsExpression ifExpr, JsStatement thenStmt,
      JsStatement elseStmt) {
    super(sourceInfo);
    this.ifExpr = ifExpr;
    this.thenStmt = thenStmt;
    this.elseStmt = elseStmt;
  }

  public JsStatement getElseStmt() {
    return elseStmt;
  }

  public JsExpression getIfExpr() {
    return ifExpr;
  }

  public JsStatement getThenStmt() {
    return thenStmt;
  }

  public void setElseStmt(JsStatement elseStmt) {
    this.elseStmt = elseStmt;
  }

  public void setIfExpr(JsExpression ifExpr) {
    this.ifExpr = ifExpr;
  }

  public void setThenStmt(JsStatement thenStmt) {
    this.thenStmt = thenStmt;
  }

  public void traverse(JsVisitor v, JsContext<JsStatement> ctx) {
    if (v.visit(this, ctx)) {
      ifExpr = v.accept(ifExpr);
      thenStmt = v.accept(thenStmt);
      if (elseStmt != null) {
        elseStmt = v.accept(elseStmt);
      }
    }
    v.endVisit(this, ctx);
  }
}
