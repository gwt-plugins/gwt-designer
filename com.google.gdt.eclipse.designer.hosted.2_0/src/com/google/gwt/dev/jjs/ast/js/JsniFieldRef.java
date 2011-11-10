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
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JField;
import com.google.gwt.dev.jjs.ast.JFieldRef;
import com.google.gwt.dev.jjs.ast.JNullLiteral;
import com.google.gwt.dev.jjs.ast.JVisitor;

/**
 * JSNI reference to a Java field.
 */
public class JsniFieldRef extends JFieldRef {

  private final String ident;
  private final boolean isLvalue;

  public JsniFieldRef(SourceInfo info, String ident, JField field,
      JDeclaredType enclosingType, boolean isLvalue) {
    super(info, field.isStatic() ? null : JNullLiteral.INSTANCE, field,
        enclosingType);
    this.ident = ident;
    this.isLvalue = isLvalue;
  }

  public String getIdent() {
    return ident;
  }

  public boolean isLvalue() {
    return isLvalue;
  }

  @Override
  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
    }
    visitor.endVisit(this, ctx);
  }
}
