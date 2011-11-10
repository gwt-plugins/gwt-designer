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
 * Java array reference expression.
 */
public class JArrayRef extends JExpression {

  private JExpression indexExpr;
  private JExpression instance;

  public JArrayRef(SourceInfo info, JExpression instance, JExpression indexExpr) {
    super(info);
    this.instance = instance;
    this.indexExpr = indexExpr;
  }

  public JArrayType getArrayType() {
    JType type = instance.getType();
    if (type instanceof JNullType) {
      return null;
    }
    return (JArrayType) ((JReferenceType) type).getUnderlyingType();
  }

  public JExpression getIndexExpr() {
    return indexExpr;
  }

  public JExpression getInstance() {
    return instance;
  }

  public JType getType() {
    JArrayType arrayType = getArrayType();
    return (arrayType == null) ? JNullType.INSTANCE
        : arrayType.getElementType();
  }

  public boolean hasSideEffects() {
    // TODO: make the last test better when we have null tracking.
    return instance.hasSideEffects() || indexExpr.hasSideEffects()
        || instance.getType() == JNullType.INSTANCE;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
      instance = visitor.accept(instance);
      indexExpr = visitor.accept(indexExpr);
    }
    visitor.endVisit(this, ctx);
  }

}
