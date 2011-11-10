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
 * Used in object literals to specify property values by name.
 */
public class JsPropertyInitializer extends JsNode<JsPropertyInitializer> {

  private JsExpression labelExpr;

  private JsExpression valueExpr;

  public JsPropertyInitializer(SourceInfo sourceInfo) {
    super(sourceInfo);
  }

  public JsPropertyInitializer(SourceInfo sourceInfo, JsExpression labelExpr, JsExpression valueExpr) {
    super(sourceInfo);
    this.labelExpr = labelExpr;
    this.valueExpr = valueExpr;
  }

  public JsExpression getLabelExpr() {
    return labelExpr;
  }

  public JsExpression getValueExpr() {
    return valueExpr;
  }

  public boolean hasSideEffects() {
    return labelExpr.hasSideEffects() || valueExpr.hasSideEffects();
  }

  public void setLabelExpr(JsExpression labelExpr) {
    this.labelExpr = labelExpr;
  }

  public void setValueExpr(JsExpression valueExpr) {
    this.valueExpr = valueExpr;
  }

  public void traverse(JsVisitor v, JsContext<JsPropertyInitializer> ctx) {
    if (v.visit(this, ctx)) {
      labelExpr = v.accept(labelExpr);
      valueExpr = v.accept(valueExpr);
    }
    v.endVisit(this, ctx);
  }
}
