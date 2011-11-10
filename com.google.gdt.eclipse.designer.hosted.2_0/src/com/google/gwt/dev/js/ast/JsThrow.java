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
 * A JavaScript <code>throw</code> statement.
 */
public class JsThrow extends JsStatement {

  private JsExpression expr;

  public JsThrow(SourceInfo sourceInfo) {
    super(sourceInfo);
  }

  public JsThrow(SourceInfo sourceInfo, JsExpression expr) {
    super(sourceInfo);
    this.expr = expr;
  }

  public JsExpression getExpr() {
    return expr;
  }

  public void setExpr(JsExpression expr) {
    this.expr = expr;
  }

  public void traverse(JsVisitor v, JsContext<JsStatement> ctx) {
    if (v.visit(this, ctx)) {
      expr = v.accept(expr);
    }
    v.endVisit(this, ctx);
  }

  public boolean unconditionalControlBreak() {
    return true;
  }
}
