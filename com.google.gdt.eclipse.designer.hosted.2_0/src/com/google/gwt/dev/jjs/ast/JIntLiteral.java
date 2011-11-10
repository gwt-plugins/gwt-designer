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
 * Java integer literal expression.
 */
public class JIntLiteral extends JValueLiteral {

  public static final JIntLiteral ZERO = new JIntLiteral(SourceOrigin.UNKNOWN,
      0);

  public static JIntLiteral get(int value) {
    return (value == 0) ? ZERO : new JIntLiteral(SourceOrigin.UNKNOWN, value);
  }

  private final int value;

  public JIntLiteral(SourceInfo sourceInfo, int value) {
    super(sourceInfo);
    this.value = value;
  }

  @Override
  public JValueLiteral cloneFrom(JValueLiteral value) {
    Object valueObj = value.getValueObj();
    if (valueObj instanceof Character) {
      Character character = (Character) valueObj;
      return new JIntLiteral(value.getSourceInfo(), character.charValue());
    } else if (valueObj instanceof Number) {
      Number number = (Number) valueObj;
      return new JIntLiteral(value.getSourceInfo(), number.intValue());
    }
    return null;
  }

  public JType getType() {
    return JPrimitiveType.INT;
  }

  public int getValue() {
    return value;
  }

  @Override
  public Object getValueObj() {
    return Integer.valueOf(value);
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
    }
    visitor.endVisit(this, ctx);
  }

  private Object readResolve() {
    return (value == 0) ? ZERO : this;
  }
}