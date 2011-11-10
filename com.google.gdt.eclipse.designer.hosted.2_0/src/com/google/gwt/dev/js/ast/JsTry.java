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
 * A JavaScript <code>try</code> statement.
 */
public class JsTry extends JsStatement {

  private final List<JsCatch> catches = new ArrayList<JsCatch>();

  private JsBlock finallyBlock;

  private JsBlock tryBlock;

  public JsTry(SourceInfo sourceInfo) {
    super(sourceInfo);
  }

  public List<JsCatch> getCatches() {
    return catches;
  }

  public JsBlock getFinallyBlock() {
    return finallyBlock;
  }

  public JsBlock getTryBlock() {
    return tryBlock;
  }

  public void setFinallyBlock(JsBlock block) {
    this.finallyBlock = block;
  }

  public void setTryBlock(JsBlock block) {
    tryBlock = block;
  }

  public void traverse(JsVisitor v, JsContext<JsStatement> ctx) {
    if (v.visit(this, ctx)) {
      tryBlock = v.accept(tryBlock);
      v.acceptWithInsertRemove(catches);
      if (finallyBlock != null) {
        finallyBlock = v.accept(finallyBlock);
      }
    }
    v.endVisit(this, ctx);
  }
}
