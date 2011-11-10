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

import org.apache.commons.lang.StringUtils;

import com.google.gwt.dev.js.ast.JsArrayAccess;
import com.google.gwt.dev.js.ast.JsArrayLiteral;
import com.google.gwt.dev.js.ast.JsBinaryOperation;
import com.google.gwt.dev.js.ast.JsConditional;
import com.google.gwt.dev.js.ast.JsContext;
import com.google.gwt.dev.js.ast.JsExprStmt;
import com.google.gwt.dev.js.ast.JsExpression;
import com.google.gwt.dev.js.ast.JsFunction;
import com.google.gwt.dev.js.ast.JsInvocation;
import com.google.gwt.dev.js.ast.JsNameRef;
import com.google.gwt.dev.js.ast.JsNew;
import com.google.gwt.dev.js.ast.JsObjectLiteral;
import com.google.gwt.dev.js.ast.JsPostfixOperation;
import com.google.gwt.dev.js.ast.JsPrefixOperation;
import com.google.gwt.dev.js.ast.JsRegExp;
import com.google.gwt.dev.js.ast.JsVisitor;

/**
 * Determines if an expression statement needs to be surrounded by parentheses.
 * >>> XXX Instantiations
 * 
 * @author mitin_aa
 */
public class JsniFieldAsMethodReferenceVisitor extends JsVisitor {

  public static boolean exec(JsExprStmt statement) {
    JsniFieldAsMethodReferenceVisitor visitor = new JsniFieldAsMethodReferenceVisitor();
    JsExpression expression = statement.getExpression();
    // Pure function declarations do not need parentheses
    if (expression instanceof JsFunction) {
      return false;
    }
    visitor.accept(statement.getExpression());
    return visitor.needsParentheses;
  }

  private boolean needsParentheses = false;

  private JsniFieldAsMethodReferenceVisitor() {
  }

  public boolean visit(JsArrayAccess x, JsContext<JsExpression> ctx) {
    return false;
  }

  public boolean visit(JsArrayLiteral x, JsContext<JsExpression> ctx) {
    return false;
  }

  public boolean visit(JsBinaryOperation x, JsContext<JsExpression> ctx) {
    return false;
  }

  public boolean visit(JsConditional x, JsContext<JsExpression> ctx) {
    return false;
  }

  public boolean visit(JsFunction x, JsContext<JsExpression> ctx) {
    return false;
  }

  public boolean visit(JsInvocation x, JsContext<JsExpression> ctx) {
    return false;
  }

  public boolean visit(JsNameRef x, JsContext<JsExpression> ctx) {
	needsParentheses = isJsniFieldReference(x.getShortIdent());
    return false;
  }
  private boolean isJsniFieldReference(String name) {
	name = StringUtils.substringAfterLast(name, "@");
	if (!StringUtils.isEmpty(name) && name.indexOf("::") != -1) {
      return !name.endsWith(")");
    }
    return false;
  }

  public boolean visit(JsNew x, JsContext<JsExpression> ctx) {
    return false;
  }

  public boolean visit(JsObjectLiteral x, JsContext<JsExpression> ctx) {
    return false;
  }

  public boolean visit(JsPostfixOperation x, JsContext<JsExpression> ctx) {
    return false;
  }

  public boolean visit(JsPrefixOperation x, JsContext<JsExpression> ctx) {
    return false;
  }

  public boolean visit(JsRegExp x, JsContext<JsExpression> ctx) {
    return false;
  }

}
