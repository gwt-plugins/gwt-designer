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
 * An AST node whose evaluation results in the string name of its node.
 */
public class JNameOf extends JExpression {

  private final HasName node;
  private final JType stringType;

  public JNameOf(SourceInfo info, JProgram program, HasName node) {
    super(info);
    this.node = node;
    stringType = program.getTypeJavaLangString();
  }

  public HasName getNode() {
    return node;
  }

  public JType getType() {
    return stringType;
  }

  @Override
  public boolean hasSideEffects() {
    return false;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
      // Intentionally not visiting referenced node
    }
    visitor.endVisit(this, ctx);
  }

}
