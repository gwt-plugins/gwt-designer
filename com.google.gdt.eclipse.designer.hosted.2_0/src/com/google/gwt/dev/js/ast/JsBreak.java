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

/**
 * Represents the JavaScript break statement.
 */
public final class JsBreak extends JsStatement {

  private final JsNameRef label;

  public JsBreak(SourceInfo sourceInfo) {
    this(sourceInfo, null);
  }

  public JsBreak(SourceInfo sourceInfo, JsNameRef label) {
    super(sourceInfo);
    this.label = label;
  }

  public JsNameRef getLabel() {
    return label;
  }

  public void traverse(JsVisitor v, JsContext<JsStatement> ctx) {
    if (v.visit(this, ctx)) {
      if (label != null) {
        v.accept(label);
      }
    }
    v.endVisit(this, ctx);
  }

  public boolean unconditionalControlBreak() {
    return true;
  }
}
