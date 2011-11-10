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
import com.google.gwt.dev.jjs.ast.JNode;
import com.google.gwt.dev.jjs.ast.JType;
import com.google.gwt.dev.jjs.ast.JVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a JS construct that should be emitted as a JSON-style object.
 */
public class JsonObject extends JExpression {

  /**
   * An individual property initializer within a JSON object initializer.
   */
  public static class JsonPropInit extends JNode {

    public JExpression labelExpr;
    public JExpression valueExpr;

    public JsonPropInit(SourceInfo sourceInfo, JExpression labelExpr,
        JExpression valueExpr) {
      super(sourceInfo);
      this.labelExpr = labelExpr;
      this.valueExpr = valueExpr;
    }

    public void traverse(JVisitor visitor, Context ctx) {
      if (visitor.visit(this, ctx)) {
        labelExpr = visitor.accept(labelExpr);
        valueExpr = visitor.accept(valueExpr);
      }
      visitor.endVisit(this, ctx);
    }
  }

  public final List<JsonPropInit> propInits = new ArrayList<JsonPropInit>();
  private final JClassType jsoType;

  public JsonObject(SourceInfo sourceInfo, JClassType jsoType) {
    super(sourceInfo);
    this.jsoType = jsoType;
  }

  public JType getType() {
    return jsoType;
  }

  public boolean hasSideEffects() {
    for (JsonPropInit propInit : propInits) {
      if (propInit.labelExpr.hasSideEffects()
          || propInit.valueExpr.hasSideEffects()) {
        return true;
      }
    }
    return false;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
      visitor.accept(propInits);
    }
    visitor.endVisit(this, ctx);
  }

}
