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
 * An AST node whose evaluation results in the string name of its node.
 */
public class JsNameOf extends JsExpression {
  private final JsName name;

  public JsNameOf(SourceInfo info, JsName name) {
    super(info);
    this.name = name;
  }

  public JsNameOf(SourceInfo info, HasName node) {
    this(info, node.getName());
  }

  public JsName getName() {
    return name;
  }

  @Override
  public boolean hasSideEffects() {
    return false;
  }

  @Override
  public boolean isDefinitelyNotNull() {
    // GenerateJsAST would have already replaced unnamed references with null
    return true;
  }

  @Override
  public boolean isDefinitelyNull() {
    return false;
  }

  public void traverse(JsVisitor visitor, JsContext<JsExpression> ctx) {
    if (visitor.visit(this, ctx)) {
    }
    visitor.endVisit(this, ctx);
  }
}
