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
 * Java character literal expression.
 */
public class JCharLiteral extends JValueLiteral {

  public static final JCharLiteral NULL = new JCharLiteral(
      SourceOrigin.UNKNOWN, (char) 0);

  public static JCharLiteral get(char value) {
    return (value == 0) ? NULL : new JCharLiteral(SourceOrigin.UNKNOWN, value);
  }

  private final char value;

  public JCharLiteral(SourceInfo sourceInfo, char value) {
    super(sourceInfo);
    this.value = value;
  }

  @Override
  public JValueLiteral cloneFrom(JValueLiteral value) {
    Object valueObj = value.getValueObj();
    if (valueObj instanceof Character) {
      return value;
    } else if (valueObj instanceof Number) {
      Number number = (Number) valueObj;
      return new JCharLiteral(value.getSourceInfo(), (char) number.intValue());
    }
    return null;
  }

  public JType getType() {
    return JPrimitiveType.CHAR;
  }

  public char getValue() {
    return value;
  }

  @Override
  public Object getValueObj() {
    return Character.valueOf(value);
  }

  public void traverse(JVisitor visitor, Context ctx) {
    if (visitor.visit(this, ctx)) {
    }
    visitor.endVisit(this, ctx);
  }

  private Object readResolve() {
    return (value == 0) ? NULL : this;
  }
}
