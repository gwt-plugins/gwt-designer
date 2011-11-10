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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a JavaScript block statement.
 */
public class JsBlock extends JsStatement {

  private final List<JsStatement> stmts = new ArrayList<JsStatement>();

  public JsBlock(SourceInfo sourceInfo) {
    super(sourceInfo);
  }

  public List<JsStatement> getStatements() {
    return stmts;
  }

  public boolean isGlobalBlock() {
    return false;
  }

  public void traverse(JsVisitor v, JsContext<JsStatement> ctx) {
    if (v.visit(this, ctx)) {
      v.acceptWithInsertRemove(stmts);
    }
    v.endVisit(this, ctx);
  }
}
