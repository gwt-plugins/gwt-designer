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
import com.google.gwt.dev.jjs.SourceOrigin;

/**
 * Java null literal expression.
 */
public class JNullLiteral extends JValueLiteral {

  public static final JNullLiteral INSTANCE = new JNullLiteral(
      SourceOrigin.UNKNOWN);

  private JNullLiteral(SourceInfo sourceInfo) {
    super(sourceInfo);
  }

  @Override
  JValueLiteral cloneFrom(JValueLiteral value) {
    throw new UnsupportedOperationException();
  }

  public JType getType() {
    return JNullType.INSTANCE;
  }

  public Object getValueObj() {
    return null;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
    }
    visitor.endVisit(this, ctx);
  }

  /**
   * Note, if this ever becomes not-a-singleton, we'll need to check the
   * SourceInfo == SourceOrigin.UNKNOWN.
   */
  private Object readResolve() {
    return INSTANCE;
  }
}
