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
 * Java literal expression that evaluates to a string.
 */
public class JStringLiteral extends JValueLiteral {

  private final JNonNullType stringType;
  private final String value;

  /**
   * These are only supposed to be constructed by JProgram.
   */
  JStringLiteral(SourceInfo sourceInfo, String value, JNonNullType stringType) {
    super(sourceInfo);
    this.value = value;
    this.stringType = stringType;
  }

  @Override
  public JValueLiteral cloneFrom(JValueLiteral value) {
    throw new UnsupportedOperationException();
  }

  public JType getType() {
    return stringType;
  }

  public String getValue() {
    return value;
  }

  public Object getValueObj() {
    return value;
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
    }
    visitor.endVisit(this, ctx);
  }
}
