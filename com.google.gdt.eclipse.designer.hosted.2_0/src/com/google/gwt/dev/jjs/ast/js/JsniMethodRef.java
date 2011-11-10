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
package com.google.gwt.dev.jjs.ast.js;

import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.jjs.ast.Context;
import com.google.gwt.dev.jjs.ast.JClassType;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JMethodCall;
import com.google.gwt.dev.jjs.ast.JNullLiteral;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.jjs.ast.JVisitor;

/**
 * A call to a JSNI method.
 */
public class JsniMethodRef extends JMethodCall {

  private final String ident;
  private final JClassType jsoType;

  public JsniMethodRef(SourceInfo info, String ident, JMethod method,
      JClassType jsoType) {
    // Just use a null literal as the qualifier on a non-static method
    super(info, method.isStatic() ? null : JNullLiteral.INSTANCE, method);
    this.ident = ident;
    this.jsoType = jsoType;
  }

  public String getIdent() {
    return ident;
  }

  @Override
  public JType getType() {
    return jsoType;
  }

  @Override
  public boolean hasSideEffects() {
    return false;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
    }
    visitor.endVisit(this, ctx);
  }
}
