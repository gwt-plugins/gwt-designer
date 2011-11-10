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
package com.google.gwt.dev.js;

import com.google.gwt.dev.js.ast.JsArrayAccess;
import com.google.gwt.dev.js.ast.JsArrayLiteral;
import com.google.gwt.dev.js.ast.JsContext;
import com.google.gwt.dev.js.ast.JsExpression;
import com.google.gwt.dev.js.ast.JsFunction;
import com.google.gwt.dev.js.ast.JsInvocation;
import com.google.gwt.dev.js.ast.JsNameRef;
import com.google.gwt.dev.js.ast.JsNew;
import com.google.gwt.dev.js.ast.JsObjectLiteral;
import com.google.gwt.dev.js.ast.JsVisitable;
import com.google.gwt.dev.js.ast.JsVisitor;

/**
 * Searches for method invocations in constructor expressions that would not
 * normally be surrounded by parentheses.
 */
public class JsConstructExpressionVisitor extends JsVisitor {

  public static boolean exec(JsExpression expression) {
    if (JsPrecedenceVisitor.exec(expression) < JsPrecedenceVisitor.PRECEDENCE_NEW) {
      return true;
    }
    JsConstructExpressionVisitor visitor = new JsConstructExpressionVisitor();
    visitor.accept(expression);
    return visitor.containsInvocation;
  }

  private boolean containsInvocation = false;

  private JsConstructExpressionVisitor() {
  }

  /**
   * We only look at the array expression since the index has its own scope.
   */
  @Override
  public boolean visit(JsArrayAccess x, JsContext<JsExpression> ctx) {
    accept(x.getArrayExpr());
    return false;
  }

  /**
   * Array literals have their own scoping.
   */
  @Override
  public boolean visit(JsArrayLiteral x, JsContext<JsExpression> ctx) {
    return false;
  }

  /**
   * Functions have their own scoping.
   */
  @Override
  public boolean visit(JsFunction x, JsContext<JsExpression> ctx) {
    return false;
  }

  @Override
  public boolean visit(JsInvocation x, JsContext<JsExpression> ctx) {
    containsInvocation = true;
    return false;
  }

  @Override
  public boolean visit(JsNameRef x, JsContext<JsExpression> ctx) {
    if (!x.isLeaf()) {
      accept(x.getQualifier());
    }
    return false;
  }

  /**
   * New constructs bind to the nearest set of parentheses.
   */
  @Override
  public boolean visit(JsNew x, JsContext<JsExpression> ctx) {
    return false;
  }

  /**
   * Object literals have their own scope.
   */
  @Override
  public boolean visit(JsObjectLiteral x, JsContext<JsExpression> ctx) {
    return false;
  }

  /**
   * We only look at nodes that would not normally be surrounded by parentheses.
   */
  @SuppressWarnings("cast")
  protected <T extends JsVisitable<T>> T doAccept(T node) {
    /*
     * Extra casts to Object to prevent 'inconvertible types' compile errors due
     * to http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6548436
     * reproducible in jdk1.6.0_02.
     */
    if ((Object) node instanceof JsExpression) {
      JsExpression expression = (JsExpression) (Object) node;
      int precedence = JsPrecedenceVisitor.exec(expression);
      // Only visit expressions that won't automatically be surrounded by
      // parentheses
      if (precedence < JsPrecedenceVisitor.PRECEDENCE_NEW) {
        return node;
      }
    }
    return super.doAccept(node);
  }

}
