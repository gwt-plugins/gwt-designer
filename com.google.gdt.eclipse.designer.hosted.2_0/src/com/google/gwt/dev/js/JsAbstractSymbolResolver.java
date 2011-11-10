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

import com.google.gwt.dev.js.ast.JsCatch;
import com.google.gwt.dev.js.ast.JsContext;
import com.google.gwt.dev.js.ast.JsExpression;
import com.google.gwt.dev.js.ast.JsFunction;
import com.google.gwt.dev.js.ast.JsNameRef;
import com.google.gwt.dev.js.ast.JsProgram;
import com.google.gwt.dev.js.ast.JsScope;
import com.google.gwt.dev.js.ast.JsVisitor;

import java.util.Stack;

/**
 * Base class for any recursive resolver classes.
 */
public abstract class JsAbstractSymbolResolver extends JsVisitor {

  private final Stack<JsScope> scopeStack = new Stack<JsScope>();

  @Override
  public void endVisit(JsCatch x, JsContext<JsCatch> ctx) {
    popScope();
  }

  @Override
  public void endVisit(JsFunction x, JsContext<JsExpression> ctx) {
    popScope();
  }

  @Override
  public void endVisit(JsNameRef x, JsContext<JsExpression> ctx) {
    if (x.isResolved()) {
      return;
    }

    resolve(x);
  }

  @Override
  public void endVisit(JsProgram x, JsContext<JsProgram> ctx) {
    popScope();
  }

  @Override
  public boolean visit(JsCatch x, JsContext<JsCatch> ctx) {
    pushScope(x.getScope());
    return true;
  }

  @Override
  public boolean visit(JsFunction x, JsContext<JsExpression> ctx) {
    pushScope(x.getScope());
    return true;
  }

  @Override
  public boolean visit(JsProgram x, JsContext<JsProgram> ctx) {
    pushScope(x.getScope());
    return true;
  }

  protected JsScope getScope() {
    return scopeStack.peek();
  }

  protected abstract void resolve(JsNameRef x);
  
  private void popScope() {
    scopeStack.pop();
  }

  private void pushScope(JsScope scope) {
    scopeStack.push(scope);
  }
}
