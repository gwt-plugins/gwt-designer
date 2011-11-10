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
package com.google.gwt.dev.jjs.ast;

import com.google.gwt.dev.jjs.SourceInfo;

/**
 * Java statement that has an associated label.
 */
public class JLabeledStatement extends JStatement {

  private JStatement body;
  private final JLabel label;

  public JLabeledStatement(SourceInfo info, JLabel label, JStatement body) {
    super(info);
    this.label = label;
    this.body = body;
  }

  public JStatement getBody() {
    return body;
  }

  public JLabel getLabel() {
    return label;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
      visitor.accept(label);
      body = visitor.accept(body);
    }
    visitor.endVisit(this, ctx);
  }
}
