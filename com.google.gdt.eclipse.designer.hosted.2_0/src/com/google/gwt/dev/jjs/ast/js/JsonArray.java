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
import com.google.gwt.dev.jjs.ast.JExpression;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.jjs.ast.JVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * A JSON-style list of JS expressions.
 */
public class JsonArray extends JExpression {

  public List<JExpression> exprs = new ArrayList<JExpression>();

  private final JClassType jsoType;

  public JsonArray(SourceInfo sourceInfo, JClassType jsoType) {
    super(sourceInfo);
    this.jsoType = jsoType;
  }

  public JType getType() {
    return jsoType;
  }

  public boolean hasSideEffects() {
    for (int i = 0, c = exprs.size(); i < c; ++i) {
      if (exprs.get(i).hasSideEffects()) {
        return true;
      }
    }
    return false;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
      visitor.accept(exprs);
    }
    visitor.endVisit(this, ctx);
  }

}
